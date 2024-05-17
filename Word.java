//Represents a word, which is a collection of 32 bits, which can be either true or false
//Note: the left most bit is at index 0, and right most is at index 31
public class Word {

    private Bit bits[] = new Bit[32];

    // Default constructor initializes all bits to false
    public Word() {
        for (int i = 0; i < 32; i++) {
            bits[i] = new Bit(false);
        }
    }

    //Debugging purposes only - sets the word to the passed value for testing
    public Word(int value){
        set(value);
    }

    // Sets bits to given bit array
    public Word(Bit bits[]) {
        this.bits = bits;
    }

    // Returns a bit value from the given index in the word
    Bit getBit(int i) {
        return new Bit(bits[i].getValue());
    }

    boolean getBitValue(int i){
        return bits[i].getValue();
    }

    // Sets the bit at the given index to the given value
    void setBit(int i, Bit value) {
        bits[i].set(value.getValue());
    }

    // Performs logical AND on all the bits of this word and another word
    Word and(Word other) {
        Word word = new Word();
        for (int i = 0; i < 32; i++) {
            word.setBit(i, bits[i].and(other.getBit(i)));
        }
        return word;
    }

    // Performs logical OR on all the bits of this word and another word
    Word or(Word other) {
        Word word = new Word();
        for (int i = 0; i < 32; i++) {
            word.setBit(i, bits[i].or(other.getBit(i)));
        }
        return word;
    }

    // Performs logical XOR on all the bits of this word and another word
    Word xor(Word other) {
        Word word = new Word();
        for (int i = 0; i < 32; i++) {
            word.setBit(i, bits[i].xor(other.getBit(i)));
        }
        return word;
    }

    // Performs logical NOT on all the bits of this word
    Word not() {
        Word word = new Word();
        for (int i = 0; i < 32; i++) {
            word.setBit(i, bits[i].not());
        }
        return word;
    }

    // Returns a new word with all bits shifted to the right by
    // the given amount
    Word rightShift(int amount) {
        if(amount<0){
            throw new IllegalArgumentException("Can not shift by negative amount");
        }
        Word word = new Word();
        for (int i = 0; i < 32 - amount; i++) {
            word.setBit(i + amount, bits[i]);
        }
        return word;
    }

    // Returns a new word with all bits shifted to the left by
    // the given amount
    Word leftShift(int amount) {
        if(amount<0){
            throw new IllegalArgumentException("Can not shift by negative amount");
        }
        Word word = new Word();
        for (int i = amount; i < 32; i++) {
            word.setBit(i - amount, bits[i]);
        }
        return word;
    }

    void setBit(int i, boolean value){
        bits[i] = new Bit(value);
    }

    void setBits(int start, int end, boolean value){
        for(int i = start; i<=end; i++){
            bits[i] = new Bit(value);
        }
    }

    Word increment(){
        Word word = new Word();
        Bit out = new Bit(true);
        for(int i = 31; i >= 0; i--){
            word.setBit(i, bits[i].xor(out));
            out = bits[i].and(out);
        }
        return word;
    }

    Word decrement(){
        Bit bit1, bit2;
        Bit out = new Bit(false);
        Word retVal = new Word();
        for(int i = 31; i>=0; i--){
            bit1 = bits[i];
            bit2 = new Bit(true);
            retVal.setBit(i, (bit1.xor(bit2)).xor(out));
            out = bit1.and(bit2).or((bit1.xor(bit2)).and(out));
        }
        return retVal;
    }

    // Converts the word to a 32-bit unsigned number
    long getUnsigned() {
        long num = 0;

        // Loops through all bits and sums using the power method,
        // which essentially means if a bit is activated, add 2^32-i (the reverse index)
        // to the sum
        for (int i = 0; i < 32; i++) {
            if (bits[i].getValue()) {
                num += Math.pow(2, 31 - i);
            }
        }
        return num;
    }

    // Converts the word to a 32-bit signed number, using the left
    // most bit as the sign (1 = negative, 0 = positive)
    int getSigned() {
        int num = 0;

        if(!bits[0].getValue()){
            // Loops through all bits except the first and sums using the power method,
            // which essentially means if a bit is activated, add 2^31-i (the reverse index)
            // to the sum
            for (int i = 1; i < 32; i++) {
                if (bits[i].getValue()) {
                    num += Math.pow(2, 31 - i);
                }
            }
        }

        //If the most significant bit is set, then perform the same operation as above but reversed
        //for negative numbers
        else{
            for (int i = 1; i < 32; i++) {
                if (!bits[i].getValue()) {
                    num -= Math.pow(2, 31 - i);
                }
            }
            num-=1;
        }
        return num;
    }

    // Copies all the bits of another word to this word
    void copy(Word other) {
        for (int i = 0; i < 32; i++) {
            bits[i] = other.getBit(i);
        }
    }

    // Sets the bits of this word to the given signed number
    void set(int value) {
        double power;
        // Clears the bits of this word
        for (int i = 0; i < 32; i++) {
            bits[i] = new Bit(false);
        }

        // If the value is negative uses the 2's complement rule to set the value
        if (value < 0) {
            value*=-1;
            for (int i = 0; i < 32; i++) {
                power = Math.pow(2, 31 - i);
                if (value >= power) {
                    bits[i].set(true);
                    value -= power;
                }
            }
            for(int i = 0; i<32; i++){
                bits[i].toggle();
            }
            Bit temp;
            Bit carry = new Bit(true);
            //Adds one to ensure proper negation when adding with this words complement
            for(int i = 31; i>=0; i--){
                temp = bits[i];
                bits[i] = bits[i].xor(carry);
                carry = temp.and(carry);
            }
        }

        //Base case if we are not using a negative number
        else{
            for (int i = 0; i < 32; i++) {
                power = Math.pow(2, 31 - i);
                if (value >= power) {
                    bits[i].set(true);
                    value -= power;
                }
            }
        }

    }

    Word setWord(String bits){
        Word word = new Word();
        int limit = 32;
        int index = 0;
        for(int i = 0; i<limit; i++){
            if(bits.charAt(i) == '1'){
                word.setBit(index, new Bit(true));
                index++;
            }
            else if(bits.charAt(i) == ' '){
                limit++;
            }
            else{
                word.setBit(index, new Bit(false));
                index++;
            }
        }
        return word;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < 31; i++) {
            s += bits[i].toString();
        }
        s += bits[31].toString();
        return s;
    }
}