import java.util.Arrays;

//Processor contains functionality to emulate an actual CPU, using the fetch->decode->execute->store
//cycle - WIP
public class Processor {

    //Program counter indicates what instruction we read next from memory
    private Word programCounter;
    private Word stackPointer;
    private Word currentInstruction;
    private Bit halted;

    //These variables are used as intermediate storage between steps, primarily decode()->execute()
    private Word opcode;
    private Word immediate;
    private Word rs1;
    private Word rs2;
    private Word function;
    private Word rd;

    //Used to store the result from execute(), used in store()
    private Word result;

    //Public for testing purposes, represents all registers - the 0th register is not writable based
    //on code in store() to prevent writing
    public static Word[] r;

    private enum InstructionType {NO_REG, DEST_ONLY, TWO_REG, THREE_REG};
    private InstructionType instType;

    private enum OperationType {MATH, BRANCH, CALL, PUSH, LOAD, STORE, POP};
    private OperationType opType;

    private ALU alu;


    public Processor(){
        programCounter = new Word();
        stackPointer = new Word();
        programCounter.set(0);
        stackPointer.set(1023);
        halted = new Bit(false);

        immediate = new Word();
        rs1 = new Word();
        rs2 = new Word();
        function = new Word();
        rd = new Word();
        r = new Word[32];

        for(int i = 0; i<32; i++){
            r[i] = new Word();
        }

        alu = new ALU();
    }

    //Main functionality for the processor, fetching, decoding, executing, and then storing the
    //results of instructions
    public void run() throws Exception {
        while(!halted.getValue()){
            fetch();
            decode();
            execute();
            store();
            //Used for debugging, can be commented out for final build
            System.out.println(Arrays.toString(r));
        }
    }

    //Fetches the current instruction from the place in memory where programCounter is pointing for
    //use in the decode step of the processor
    public void fetch(){
        try {
            currentInstruction = MainMemory.read(programCounter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        programCounter = programCounter.increment();
    }

    //Decodes the instruction according to the first two bits in the opcode, storing the bits in
    //our intermediate storage variables for use in the execute step of the processor; refer to the
    //SIA 32 document for further information on the architecture of instructions.
    public void decode(){
        Word mask = new Word();
        mask.setBits(27, 31, true);
        opcode = currentInstruction.and(mask);

        //The following determines what kind of operation is being performed, used in execute and
        //store
        boolean[] opBits = new boolean[]{opcode.getBitValue(27), opcode.getBitValue(28),
                opcode.getBitValue(29)};
        if(!opBits[0] && !opBits[1] && !opBits[2])
            opType = OperationType.MATH;
        else if(!opBits[0] && !opBits[1] && opBits[2])
            opType = OperationType.BRANCH;
        else if(!opBits[0] && opBits[1] && !opBits[2])
            opType = OperationType.CALL;
        else if(!opBits[0] && opBits[1] && opBits[2])
            opType = OperationType.PUSH;
        else if(opBits[0] && !opBits[1] && !opBits[2])
            opType = OperationType.LOAD;
        else if(opBits[0] && !opBits[1] && opBits[2])
            opType = OperationType.STORE;
        else if(opBits[0] && opBits[1] && !opBits[2])
            opType = OperationType.POP;

        //The following code sets the intermediate variables according to the opcode, using a mask
        //to get each of the individual sets of bits and store them in the appropriate intermediate
        //storage variables (immediate, rs1, rs2, function, rd).

        //00: A no register operation, we only set the opcode and the immediate value
        if(!opcode.getBitValue(30) && !opcode.getBitValue(31)){
            mask = mask.not();
            mask.setBit(29, new Bit(false));
            mask.setBit(28, new Bit(false));
            mask.setBit(27, new Bit(false));
            immediate = currentInstruction.and(mask).rightShift(5);
            instType = InstructionType.NO_REG;
            return;
        }
        //10 and 01 and 11: For conciseness, this sets the rd and function for the next opcodes
        if(opcode.getBitValue(30) || opcode.getBitValue(31)){
            mask = new Word();
            mask.setBits(22, 27, true);
            rd = currentInstruction.and(mask).rightShift(5);

            mask = new Word();
            mask.setBits(18, 22, true);
            function = currentInstruction.and(mask).rightShift(10);
        }
        //01: Destination only operation
        if(!opcode.getBitValue(30) && opcode.getBitValue(31)){
            mask = new Word();
            mask.setBits(0, 18, true);
            immediate = currentInstruction.and(mask).rightShift(14);
            instType = InstructionType.DEST_ONLY;
        }
        //10: Three register operation
        else if(opcode.getBitValue(30) && !opcode.getBitValue(31)){
            mask = new Word();
            mask.setBits(13, 18, true);
            rs2 =  currentInstruction.and(mask).rightShift(14);

            mask = new Word();
            mask.setBits(8, 13, true);
            rs1 =  currentInstruction.and(mask).rightShift(19);

            mask = new Word();
            mask.setBits(0, 8, true);
            immediate =  currentInstruction.and(mask).rightShift(24);
            instType = InstructionType.THREE_REG;
        }
        //11: Two register operation
        else if(opcode.getBitValue(30) && opcode.getBitValue(31)){
            mask = new Word();
            mask.setBits(13, 18, true);
            rs1 =  currentInstruction.and(mask).rightShift(14);

            mask = new Word();
            mask.setBits(0, 13, true);
            immediate = currentInstruction.and(mask).rightShift(19);
            instType = InstructionType.TWO_REG;
        }
    }

    //Checks the opcode to determine what kind of operation we are performing, as explained below.
    //Sets result to the result of the operation in order to store into the register destination in
    //store(). See the SIA32 document for more cohesive documentation. Here is a quick reference guide:
    //000 - math
    //001 - branch
    //011 - push
    //100 - load
    //101 - store
    //110 - pop/interrupt
    public void execute() throws Exception {
        Bit[] funcOp = new Bit[]{function.getBit(28), function.getBit(29),
                function.getBit(30), function.getBit(31)};
        Bit[] addOp = new Bit[]{new Bit(true), new Bit(true),
                new Bit(true), new Bit(false)};
        //If the opcode is "00000" then we set the halted bit to indicate the processor should stop
        for(int i = 27; i<32; i++){
            if(!opcode.getBitValue(i)){
                halted.set();
            }
            else{
                halted.clear();
                break;
            }
        }
        if(halted.getValue()){
            return;
        }

        //The following conditions each perform their respective operations depending on the type as
        //decoded in decode() - see the SIA32 document for more information, these will only be
        //briefly annotated for quick reference

        //Stores a value in a register or performs a math operation between two registers
        if(opType == OperationType.MATH){
            if(instType == InstructionType.DEST_ONLY){
                result = immediate;
            }
            //Uses the value at the register destination and rs1 (stores back into rd in store())
            else if(instType == InstructionType.TWO_REG){
                result = mop(r[(int)rd.getUnsigned()], r[(int)rs1.getUnsigned()], funcOp);
            }
            //Uses rs1 and rs2 for the values of the ALU
            else if(instType == InstructionType.THREE_REG){
                result = mop(r[(int)rs1.getUnsigned()], r[(int)rs2.getUnsigned()], funcOp);
            }
        }

        //The equivalent of an if statement, either jumps to immediate arbitrarily or performs a
        //boolean operation to determine which branch to take
        else if(opType == OperationType.BRANCH){

            //Equivalent to goto in C
            if(instType == InstructionType.NO_REG){
                result = immediate;
            }
            else if(instType == InstructionType.DEST_ONLY){
                result = mop(programCounter, immediate, addOp);
            }

            //Performs a boolean op. to determine where to set the programCounter
            else if(instType == InstructionType.THREE_REG){
                if(bop(r[(int)rs1.getUnsigned()], r[(int)rs2.getUnsigned()])){
                    result = mop(programCounter, immediate, addOp);
                }
                else{
                    result = programCounter;
                }
            }
            else if(instType == InstructionType.TWO_REG){
                if(bop(r[(int)rs1.getUnsigned()], r[(int)rd.getUnsigned()])){
                    result = mop(programCounter, immediate, addOp);
                }
                else{
                    result = programCounter;
                }
            }
        }

        //Similar to branch except it pushes the programCounter onto the stack to pop later (return)
        else if(opType == OperationType.CALL){
            if(instType == InstructionType.NO_REG){
                result = immediate;
            }
            else if(instType == InstructionType.DEST_ONLY){
                result = mop(r[(int) rd.getUnsigned()], immediate, addOp);
            }

            //Conditional call will only jump if the condition is true
            else if(instType == InstructionType.THREE_REG){
                if(bop(r[(int)rs1.getUnsigned()], r[(int)rs2.getUnsigned()])){
                    push(programCounter);
                    result = mop(r[(int)rd.getUnsigned()], immediate, addOp);
                }
                else{
                    result = programCounter;
                }
            }
            else if(instType == InstructionType.TWO_REG){
                if(bop(r[(int)rs1.getUnsigned()], r[(int)rd.getUnsigned()])){
                    push(programCounter);
                    result = mop(programCounter, immediate, addOp);
                }
                else{
                    result = programCounter;
                }
            }
        }

        //Pushes the given register onto the memory stack
        else if(opType == OperationType.PUSH){
            if(instType == InstructionType.DEST_ONLY){
                result = mop(r[(int) rd.getUnsigned()], immediate, funcOp);
            }
            else if(instType == InstructionType.THREE_REG) {
                result = mop(r[(int) rs1.getUnsigned()], r[(int) rs2.getUnsigned()], funcOp);
            }
            else if(instType == InstructionType.TWO_REG){
                result = mop(r[(int) rd.getUnsigned()], r[(int) rs1.getUnsigned()], funcOp);
            }
        }

        //Loads a value from the given address (value from registers) from the memory into a given
        //register
        else if(opType == OperationType.LOAD){
            if(instType == InstructionType.NO_REG){
                result = pop();
            }
            else if(instType == InstructionType.DEST_ONLY){
                Word address = mop(r[(int) rd.getUnsigned()], immediate, addOp);
                result = MainMemory.read(address);
            }
            else if(instType == InstructionType.THREE_REG){
                Word address = mop(r[(int)rs1.getUnsigned()], r[(int)rs2.getUnsigned()], addOp);
                result = MainMemory.read(address);
            }
            else if(instType == InstructionType.TWO_REG){
                Word address = mop(r[(int)rs1.getUnsigned()], immediate, addOp);
                result = MainMemory.read(address);
            }
        }

        //Stores the given value into memory at a location of the users choosing
        else if(opType == OperationType.STORE){
            if(instType == InstructionType.DEST_ONLY){
                result = immediate;
            }
            else if(instType == InstructionType.THREE_REG){
                result = r[(int)rs2.getUnsigned()];
            }
            else if(instType == InstructionType.TWO_REG){
                result = r[(int)rs1.getUnsigned()];
            }
        }

        //Either pops a value off the stack or peeks into the stack to find a value at a certain
        //offset from the stackPointer
        else if(opType == OperationType.POP){
            if(instType == InstructionType.DEST_ONLY){
                result = pop();
            }
            else if(instType == InstructionType.THREE_REG){
                Word temp = mop(r[(int)rs1.getUnsigned()], r[(int)rs2.getUnsigned()], addOp);
                result = MainMemory.read(mop(stackPointer, temp, addOp));
            }
            else if(instType == InstructionType.TWO_REG){
                Word temp = mop(r[(int)rs1.getUnsigned()], immediate, addOp);
                result = MainMemory.read(mop(stackPointer, temp, addOp));
            }
        }
    }

    //Helper method for code reuse, prepares the ALU and then performs the given function, returning
    //the result
    private Word mop(Word op1, Word op2, Bit[] function){
        alu.op1 = op1;
        alu.op2 = op2;
        alu.doOperation(function);
        return alu.result;
    }

    //Helper method to determine what boolean operation to use and increase code readability, uses
    //subtraction between two words to compare them (e.g. if the result is 0 they are equivalent)
    private boolean bop(Word op1, Word op2){
        boolean[] bits = new boolean[]{function.getBitValue(28), function.getBitValue(29),
                function.getBitValue(30), function.getBitValue(31)};
        Bit[] subOp = new Bit[]{new Bit(true), new Bit(true),
                new Bit(true), new Bit(true)};

        //Equals: checks if all the bits are 0 to ensure equality
        if(!bits[0] && !bits[1] && !bits[2] && !bits[3]){
            alu.op1 = op1;
            alu.op2 = op2;
            alu.doOperation(subOp);
            for(int i = 0; i<32; i++){
                if(alu.result.getBitValue(i)){
                    return false;
                }
            }
            return true;
        }

        //Not Equal: Opposite of equals, checks if there is at least one 1 to indicate not equals
        else if(!bits[0] && !bits[1] && !bits[2] && bits[3]){
            alu.op1 = op1;
            alu.op2 = op2;
            alu.doOperation(subOp);
            for(int i = 0; i<32; i++){
                if(alu.result.getBitValue(i)){
                    return true;
                }
            }
            return false;
        }

        //Less Than: if the first bit is a 1 (indicating it is negative) then it is less than
        else if(!bits[0] && !bits[1] && bits[2] && !bits[3]){
            alu.op1 = op1;
            alu.op2 = op2;
            alu.doOperation(subOp);
            return alu.result.getBitValue(0);
        }

        //Greater Than or Equal: first checks if a reversed subtraction is negative, if not then
        //checks for equality
        else if(!bits[0] && !bits[1] && bits[2] && bits[3]){
            alu.op1 = op2;
            alu.op2 = op1;
            alu.doOperation(subOp);
            if(alu.result.getBitValue(0)){
                return true;
            }
            else{
                for(int i = 0; i<32; i++){
                    if(!alu.result.getBitValue(i)){
                        return true;
                    }
                }
                return false;
            }
        }

        //Greater Than: Same as less than except the operation is reversed (op2 - op1)
        else if(!bits[0] && bits[1] && !bits[2] && !bits[3]){
            alu.op1 = op2;
            alu.op2 = op1;
            alu.doOperation(subOp);
            return alu.result.getBitValue(0);
        }

        //Less Than or Equal: Same as greater than or equal except the operation is reversed
        else if(!bits[0] && bits[1] && !bits[2] && bits[3]){
            alu.op1 = op1;
            alu.op2 = op2;
            alu.doOperation(subOp);
            if(alu.result.getBitValue(0)){
                return true;
            }
            else{
                for(int i = 0; i<32; i++){
                    if(!alu.result.getBitValue(i)){
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    //Helper method for code readability, simply pushes the given word on to the stack and
    //decrements the stack pointer
    private void push(Word word) throws Exception {
        MainMemory.write(stackPointer, word);
        stackPointer = stackPointer.decrement();
    }

    //Helper method for code readability, increments the stack pointer and reads the first value off
    //the stack, replacing the read word with an empty word
    private Word pop() throws Exception{
        stackPointer = stackPointer.increment();
        Word retVal = MainMemory.read(stackPointer);
        MainMemory.write(stackPointer, new Word());
        return retVal;
    }

    //Stores the result (as calculated in execute()) into a register, memory location, or program
    //counter as indicated by the operation type. The following is briefly annotated for quick
    //reference. See the SIA32 document for more information.
    public void store() throws Exception {
        Bit[] addOp = new Bit[]{new Bit(true), new Bit(true),
                new Bit(true), new Bit(false)};
        //Calculates the register number rd represents for use in many of the store operations
        int regNum = 0;
        for(int i = 27; i<32; i++){
            if(rd.getBitValue(i)){
                regNum += (int)(Math.pow(2, 31-i));
            }
        }

        //Math: stores the result of the ALU into the indicated register
        if(opType == OperationType.MATH){
            if(instType == InstructionType.NO_REG){}
            else if(regNum!=0){
                r[regNum] = result;
            }
        }

        //Branch: sets the program counter to the result for the next clock cycle
        else if(opType == OperationType.BRANCH){
            programCounter = result;
        }

        //Call: Pushes the program counter onto the stack to return back with load/pop
        else if(opType == OperationType.CALL){
            if(instType == InstructionType.DEST_ONLY || instType == InstructionType.NO_REG){
                push(programCounter);
                programCounter = result;
            }
            else{
                programCounter = result;
            }
        }

        //Push: pushes the result onto the stack unless it is No R
        else if(opType == OperationType.PUSH){
            if(instType != InstructionType.NO_REG){
                push(result);
            }
        }

        //Load: execute() read from memory, here we either set the program counter (for return) or
        //set the given register accordingly
        else if(opType == OperationType.LOAD){
            if(instType == InstructionType.NO_REG){
                programCounter = result;
            }
            else{
                if(regNum!=0){
                    r[regNum] = result;
                }
            }
        }

        //Store: Stores the result into memory at the specified location, adding together registers
        //if necessary to find said location
        else if(opType == OperationType.STORE){
            if(instType == InstructionType.DEST_ONLY){
                MainMemory.write(r[regNum], result);
            }
            else if(instType == InstructionType.THREE_REG){
                MainMemory.write(mop(r[(int)rs1.getUnsigned()], r[(int)rd.getUnsigned()], addOp), result);
            }
            else if(instType == InstructionType.TWO_REG){
                MainMemory.write(mop(r[(int)rd.getUnsigned()], immediate, addOp), result);
            }
        }

        //Pop/Interrupt/Peek: if it is an interrupt [WIP - fill in when done], otherwise just set
        //the register according to what was popped off the stack
        else if(opType == OperationType.POP){
            if(instType == InstructionType.NO_REG){
                push(programCounter);
                programCounter = result;
            }
            else{
                r[regNum] = result;
            }
        }
    }
}