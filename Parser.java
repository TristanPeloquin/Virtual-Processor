import java.util.LinkedList;
import java.util.Optional;
import java.io.*;

//Used to parse a list of tokens from a lexer in order to encode the instructions to a file to run
//inside the processor with machine language. Refer to the SIA32 document for specifics on the
//instruction architecture.
public class Parser {

    private TokenHandler handler;
    private Word instruction;

    public Parser(LinkedList<Token> tokens) {
        handler = new TokenHandler(tokens);
        instruction = new Word();
        MainMemory.init();
    }

    //Accepts new lines and semi-colons until there are neither of each
    private boolean acceptSeperators() {
        boolean retVal = false;
        while (handler.matchAndRemove(TokenType.SEPERATOR).isPresent()) {
            retVal = true;
        }
        return retVal;
    }

    // The main method of Parser, this will parse the entirety of the token list and output each
    //instruction to a file named "output.txt" in the current working directory
    public void parse() throws Exception {
        PrintWriter writer = new PrintWriter(new FileWriter("output.txt"));
        // Loops until there are no more tokens in the list
        while (handler.moreTokens()) {
            acceptSeperators();
            parseStatement();
            writer.println(instruction.toString());
            instruction = new Word();
        }
        writer.close();
    }

    //Parses each type of statement provided with the assembler, including: math, halt, shift, copy,
    //branch, call, jump, push, load, return, store, peek, pop, and interrupt, and sets the bits in
    //the instruction accordingly
    private void parseStatement() throws Exception {
        if (handler.matchAndRemove(TokenType.MATH).isPresent()) {
            parseMop();
        }
        //Empty since halt requires all 0 bits in the word
        else if(handler.matchAndRemove(TokenType.HALT).isPresent()){}
        else if(handler.matchAndRemove(TokenType.SHIFT).isPresent()){
            parseShift();
        }
        else if(handler.matchAndRemove(TokenType.COPY).isPresent()){
            parseRegisters();
        }
        else if(handler.matchAndRemove(TokenType.BRANCH).isPresent()){
            instruction.setBits(27,28, false);
            instruction.setBit(29, true);
            parseBop();
        }
        else if(handler.matchAndRemove(TokenType.CALL).isPresent()){
            instruction.setBit(27,false);
            instruction.setBit(28, true);
            instruction.setBit(29, false);
            parseBop();
        }
        else if(handler.matchAndRemove(TokenType.JUMP).isPresent()){
            instruction.setBit(27,false);
            instruction.setBit(28, true);
            instruction.setBit(29, false);
            parseRegisters();
        }
        else if(handler.matchAndRemove(TokenType.PUSH).isPresent()){
            instruction.setBit(27, false);
            instruction.setBits(28,29, true);
            parseMop();
        }
        else if(handler.matchAndRemove(TokenType.LOAD).isPresent()){
            instruction.setBit(27, true);
            instruction.setBits(28,29, false);
            parseRegisters();
        }
        else if(handler.matchAndRemove(TokenType.RETURN).isPresent()){
            instruction.setBit(27, true);
            instruction.setBits(28,29, false);
        }
        else if(handler.matchAndRemove(TokenType.STORE).isPresent()){
            instruction.setBit(27, true);
            instruction.setBit(28, false);
            instruction.setBit(29, true);
            parseRegisters();
        }
        else if(handler.matchAndRemove(TokenType.PEEK).isPresent()){
            instruction.setBits(27,28, true);
            instruction.setBit(29, false);
            parseRegisters();
        }
        else if(handler.matchAndRemove(TokenType.POP).isPresent()){
            instruction.setBits(27,28, true);
            instruction.setBit(29, false);
            parseRegisters();
        }
        else if(handler.matchAndRemove(TokenType.INTERRUPT).isPresent()){
            instruction.setBits(27,28, true);
            instruction.setBit(29, false);
        }
    }

    //Parses the shift instruction, which takes two unique arguments: left and right, determining
    //the direction the operation will shift in
    private void parseShift() throws Exception {
        if(handler.matchAndRemove(TokenType.LEFT).isPresent()){
            instruction.setBit(18, true);
            instruction.setBit(19, true);
            instruction.setBit(20, false);
            instruction.setBit(21, false);
            parseRegisters();
        }
        else if(handler.matchAndRemove(TokenType.RIGHT).isPresent()){
            instruction.setBit(18, true);
            instruction.setBit(19, true);
            instruction.setBit(20, false);
            instruction.setBit(21, true);
            parseRegisters();
        }
    }

    //Parses all the different kinds of boolean operations as specified in the SIA32 document and
    //then parses the registers that follow it, setting the bits in the instruction accordingly
    private void parseBop() throws Exception {
        if(handler.matchAndRemove(TokenType.UNEQUAL).isPresent()){
            instruction.setBit(18, false);
            instruction.setBit(19, false);
            instruction.setBit(20, false);
            instruction.setBit(21, true);
        }
        //Empty since equal requires all 0 bits
        else if(handler.matchAndRemove(TokenType.EQUAL).isPresent()){}
        else if(handler.matchAndRemove(TokenType.LESS).isPresent()){
            instruction.setBit(18, false);
            instruction.setBit(19, false);
            instruction.setBit(20, true);
            instruction.setBit(21, false);
        }
        else if(handler.matchAndRemove(TokenType.GEQUAL).isPresent()){
            instruction.setBit(18, false);
            instruction.setBit(19, false);
            instruction.setBit(20, true);
            instruction.setBit(21, true);
        }
        else if(handler.matchAndRemove(TokenType.GREATER).isPresent()){
            instruction.setBit(18, false);
            instruction.setBit(19, true);
            instruction.setBit(20, false);
            instruction.setBit(21, false);
        }
        else if(handler.matchAndRemove(TokenType.LEQUAL).isPresent()){
            instruction.setBit(18, false);
            instruction.setBit(19, true);
            instruction.setBit(20, false);
            instruction.setBit(21, true);
        }
        parseRegisters();
    }

    //Parses all the different kinds of math operations as specified in the SIA32 document and
    //then parses the registers that follow it, setting the bits in the instruction accordingly
    private void parseMop() throws Exception {
        if(handler.matchAndRemove(TokenType.AND).isPresent()){
            instruction.setBit(18, true);
            instruction.setBits(19, 21, false);
        }
        else if(handler.matchAndRemove(TokenType.OR).isPresent()){
            instruction.setBit(18, true);
            instruction.setBits(19, 20, false);
            instruction.setBit(21, true);
        }
        else if(handler.matchAndRemove(TokenType.XOR).isPresent()){
            instruction.setBit(18, true);
            instruction.setBit(19, false);
            instruction.setBit(20, true);
            instruction.setBit(21, false);
        }
        else if(handler.matchAndRemove(TokenType.NOT).isPresent()){
            instruction.setBit(18, true);
            instruction.setBit(19, false);
            instruction.setBit(20, true);
            instruction.setBit(21, true);
        }
        else if(handler.matchAndRemove(TokenType.ADD).isPresent()){
            instruction.setBits(18, 20, true);
            instruction.setBit(21, false);
        }
        else if(handler.matchAndRemove(TokenType.SUBTRACT).isPresent()){
            instruction.setBits(18,21, true);
        }
        else if(handler.matchAndRemove(TokenType.MULTIPLY).isPresent()){
            instruction.setBit(18, false);
            instruction.setBit(19, true);
            instruction.setBit(20, true);
            instruction.setBit(21, true);
        }
        parseRegisters();
    }

    //Parses for each the different kinds of instruction register formats: no register, destination
    //only, two registers, and three registers. After determining which format we are in and
    //encoding the registers into the instruction, sets the immediate to the given value at the end
    private void parseRegisters() throws Exception {
        Optional<Token> register;
        //Finds the first register in the instruction and encodes it - immediate is handled below
        if((register = handler.matchAndRemove(TokenType.REGISTER)).isPresent()){
            encode(register.get(), 22);
        }
        //If there are two registers then continue parsing, otherwise parse the immediate value
        if((register = handler.matchAndRemove(TokenType.REGISTER)).isPresent()){
            encode(register.get(), 13);

            //Pattern continues below, parsing the next register or otherwise parsing the immediate
            //value. This will fail if the user does not provide properly formatted input.
            if((register = handler.matchAndRemove(TokenType.REGISTER)).isPresent()){
                encode(register.get(), 8);
                if((register = handler.matchAndRemove(TokenType.NUMBER)).isPresent()){
                    encode(register.get(), 0, 8);
                }
                instruction.setBit(30, true);
                instruction.setBit(31, false);
            }
            else{
                if((register = handler.matchAndRemove(TokenType.NUMBER)).isPresent()){
                    encode(register.get(), 0, 13);
                }
                instruction.setBit(30, true);
                instruction.setBit(31, true);
            }
        }
        //Parses the immediate value if there is one or zero registers
        else{
            if((register = handler.matchAndRemove(TokenType.NUMBER)).isPresent()){
                encode(register.get(), 0, 18);
            }
            instruction.setBit(30, false);
            instruction.setBit(31, true);
        }
    }

    //Used to encode the immediate value, from the given start to end range, allowing the processor
    //to properly read the instruction
    private void encode(Token number, int start, int end) {
        int num = Integer.valueOf(number.getValue());
        int result;
        int power = end-start-1;
        for (int i = start; i < end; i++) {
            result = (int) Math.pow(2, power);
            if (num >= result) {
                instruction.setBit(i, true);
                num -= result;
            }
            power--;
        }
    }

    //Used for quick writing of the register index to the instruction, meaning from the start value
    //to 5 more bits over
    public void encode(Token number, int start) {
        int num = Integer.valueOf(number.getValue());
        int result;
        int power = 4;
        for (int i = start; i < start+5; i++) {
            result = (int) Math.pow(2, power);
            if (num >= result) {
                instruction.setBit(i, true);
                num -= result;
            }
            power--;
        }
    }

}