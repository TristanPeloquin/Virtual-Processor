
//Represents the memory for the processor, allowing the user to read, write, and load to/from the
//memory
//Note - requires the user to call MainMemory.init() to initialize the memory
public class MainMemory {

    private static Word[] stack = new Word[1024];
    private static boolean isInit = false;

    //Initializes the stack and indicates the other methods that it has been initialized
    public static void init(){
        for(int i = 0; i<1024; i++){
            stack[i] = new Word();
        }
        isInit = true;
    }

    //Returns the word at the given address in the stack
    public static Word read(Word address) throws Exception {
        if(!isInit){
            throw new Exception("Memory has not been initialized");
        }
        return stack[(int)address.getUnsigned()];
    }

    //Writes word value to the given value to the given address in the stack
    public static void write(Word address, Word value) throws Exception {
        if(!isInit){
            throw new Exception("Memory has not been initialized");
        }
        stack[(int)address.getUnsigned()] = value;
    }

    //Loads the given data into memory - assumes data is formatted such that every 32 characters is
    //one word and is formatted such that a bit is '1' or '0'
    public static void load(String[] data) throws Exception {
        if(!isInit){
            throw new Exception("Memory has not been initialized");
        }
        int i = 0;

        //Sets each bit in the stack, starting from 0, to the corresponding bit in the data
        for (String word : data) {
            for (int j = 0; j < 32; j++) {
                if (word.charAt(j) == '1') {
                    stack[i].setBit(j, new Bit(true));
                } else {
                    stack[i].setBit(j, new Bit(false));
                }
            }
            i++;
        }
    }

}
