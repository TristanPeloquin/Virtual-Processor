
//ALU, or Arithmatic Logic Unit, supports addition, subtraction, multiplication, and, or, xor, not,
// left shift, and right shift of words by using the doOperation() method, and sets result to
// the result of performing the operation on op1 and op2.
public class ALU {

    public Word op1;
    public Word op2;
    public Word result;

    public ALU(){
        op1 = new Word();
        op2 = new Word();
        result = new Word();
    }

    //Uses an array of bits to determine the operation to use - here are the possible combinations:
    //1000 - and
    //1010 - xor
    //1011 - not
    //1100 - left shift
    //1101 - right shift
    //1110 - add
    //1111 - subtract
    //0111 - multiply
    //Does not return anything, instead it sets the value of result to the result of the operation
    //Throws an exception if the operation is not defined as above
    public void doOperation(Bit[] operation){
        //The following if tree determines the operation
        if(operation[0].getValue()){
            if(operation[1].getValue()){
                if(operation[2].getValue()){
                    if(operation[3].getValue()){
                        subtract();
                        return;
                    }
                    add();
                    return;
                }
                if(operation[3].getValue()){
                    //Determines the amount to shift by given the last 5 bits of op2 - this means
                    //the max to shift by is 31
                    int amount = 0;
                    for(int i = 31; i>=26; i--){
                        if(op2.getBit(i).getValue()){
                            amount += Math.pow(2, 31-i);
                        }
                    }
                    result = op1.rightShift(amount);
                    return;
                }
                //Determines the amount to shift by given the last 5 bits of op2 - this means
                //the max to shift by is 31
                int amount = 0;
                for(int i = 31; i>=26; i--){
                    if(op2.getBit(i).getValue()){
                        amount += Math.pow(2, 31-i);
                    }
                }
                result = op1.leftShift(amount);
                return;
            }
            if(operation[2].getValue()){
                if(operation[3].getValue()){
                    //Only nots op1 without considering op2
                    result = op1.not();
                    return;
                }
                result = op1.xor(op2);
                return;
            }
            if(operation[3].getValue()){
                result = op1.or(op2);
                return;
            }
            result = op1.and(op2);
            return;
        }
        if(operation[1].getValue()){
            if(operation[2].getValue()){
                if(operation[3].getValue()){
                    multiply();
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Provided operation does not exist");
    }

    //Simply adds both op1 and op2 together and sets result
    private void add(){
        result = add2(op1, op2);
    }

    //Adds two words together using the following formulas:
    //sum = X XOR Y XOR Cin
    //Cout = X AND Y OR ((X XOR Y) AND Cin)
    private Word add2(Word word1, Word word2){
        Bit bit1, bit2;
        //Initializes the carry in/out as false to prime the loop
        Bit out = new Bit(false);
        Word retVal = new Word();
        //Loops through all bits within both words and uses the above formulas to set the result/out
        for(int i = 31; i>=0; i--){
            bit1 = word1.getBit(i);
            bit2 = word2.getBit(i);
            retVal.setBit(i, (bit1.xor(bit2)).xor(out));
            out = bit1.and(bit2).or((bit1.xor(bit2)).and(out));
        }
        return retVal;
    }

    //Adds four words together using an integer carry to keep track of how many carries remain while
    //simply XORing everything together to get the return value word. This method is primarily used
    //in multiply to reduce the amount of adds we are using.
    public Word add4(Word word1, Word word2, Word word3, Word word4){
        Bit[] bits = new Bit[4];
        int carry = 0;
        Bit out = new Bit(false);
        Word retVal = new Word();

        //Loops through all bits in all 4 words
        for(int i = 31; i>=0; i--){
            bits[0] = word1.getBit(i);
            bits[1] = word2.getBit(i);
            bits[2] = word3.getBit(i);
            bits[3] = word4.getBit(i);

            //Sets the return value to the XOR of all bits plus carry, which is abstracted to one
            //bit as explained below
            retVal.setBit(i, bits[0].xor(bits[1]).xor(bits[2]).xor(bits[3]).xor(out));

            //This calculates the amount of carry - for every two bits that are true, we have
            //another carry to the next set of bits
            for(int j = 0; j<4; j++){
                if(bits[j].getValue()){
                    carry++;
                }
            }
            carry = (carry/2);

            //Abstracting the carry to a single bit, we consider only if the carry is odd then we
            //should add one to our bits and vice versa for even (e.g. 1+1=0 w/ carry 1+1+1=1 w/
            //carry) - the actual amount of carry is handled by the integer carry, so we only need
            //to worry about if we should add a carry to our value
            out = carry%2!=0 ? new Bit(true) : new Bit(false);
        }
        return retVal;
    }

    //Subtracts op2 from op1 by essentially flipping op2's bits and adding one to get its negative
    //and then adding that to op1 so instead of (a-b) we actually perform (a + (-b))
    private void subtract(){
        Word one = new Word();
        one.setBit(31, new Bit(true));
        op2 = add2(op2.not(), one);
        result = add2(op1, op2);
    }

    //Performs multiplication on op1 and op2 by utilizing add4 and add2 in a series of rounds on the
    //intermediate sums as explained below
    private void multiply(){
        Word[] intermediates = new Word[32];

        //Calculates the intermediate values by performing multiplication on each bit of the
        //multiplicand (op2) which essentially either copies op1 if its 1, or all 0's if 0
        for (int i = 31; i >= 0; i--) {
            if (op2.getBit(i).getValue()) {
                intermediates[31 - i] = op1.leftShift(31 - i);
            } else {
                intermediates[31 - i] = new Word();
            }
        }

        //ROUND 1:
        //Loops through all intermediary multiplication results and adds them using add4, leaving us
        //with a total of 8 sums remaining
        Word[] sums = new Word[8];
        for(int i = 0; i<32; i+=4){
            sums[i/4] = add4(intermediates[i], intermediates[i+1], intermediates[i+2], intermediates[i+3]);
        }

        //ROUND 2:
        //Uses add4 twice to get us down to 2 remaining sums
        Word sum1 = add4(sums[0], sums[1], sums[2], sums[3]);
        Word sum2 = add4(sums[4], sums[5], sums[6], sums[7]);

        //ROUND 3:
        //Finally sets the result to sum of the last two sums
        result = add2(sum1, sum2);
    }

}
