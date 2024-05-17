import java.io.IOException;
import java.util.LinkedList;
import java.util.HashMap;

//This class analyzes the String that gets passed to it and breaks it down into
//a list of Tokens (see more in the Token class), and throws an exception if 
//it encounters an unknown character or incorrect number format
public class Lexer {
    private StringHandler handler;
    private int lineNum;
    private int charPos;

    // These hash maps allow for quick look ups to see whether a key word
    // or symbol has a defined type
    private static HashMap<String, TokenType> keyWords;

    public Lexer(String content) throws IOException {
        lineNum = 1;
        charPos = 1;
        handler = new StringHandler(content);
        keyWords = new HashMap<>();
        populateHashMaps();
    }

    // Associates all the keys and values for the hash maps, used for quick lookup of key words when
    // tokenizing the contents
    private static void populateHashMaps() {
        keyWords.put("math", TokenType.MATH);
        keyWords.put("add", TokenType.ADD);
        keyWords.put("sub", TokenType.SUBTRACT);
        keyWords.put("mult", TokenType.MULTIPLY);
        keyWords.put("and", TokenType.AND);
        keyWords.put("or", TokenType.OR);
        keyWords.put("not", TokenType.NOT);
        keyWords.put("xor", TokenType.XOR);
        keyWords.put("copy", TokenType.COPY);
        keyWords.put("halt", TokenType.HALT);
        keyWords.put("branch", TokenType.BRANCH);
        keyWords.put("jump", TokenType.JUMP);
        keyWords.put("call", TokenType.CALL);
        keyWords.put("push", TokenType.PUSH);
        keyWords.put("load", TokenType.LOAD);
        keyWords.put("return", TokenType.RETURN);
        keyWords.put("store", TokenType.STORE);
        keyWords.put("peek", TokenType.PEEK);
        keyWords.put("pop", TokenType.POP);
        keyWords.put("intr", TokenType.INTERRUPT);
        keyWords.put("equal", TokenType.EQUAL);
        keyWords.put("unequal", TokenType.UNEQUAL);
        keyWords.put("greater", TokenType.GREATER);
        keyWords.put("less", TokenType.LESS);
        keyWords.put("gequal", TokenType.GEQUAL);
        keyWords.put("lequal", TokenType.LEQUAL);
        keyWords.put("shift", TokenType.SHIFT);
        keyWords.put("left", TokenType.LEFT);
        keyWords.put("right", TokenType.RIGHT);
    }

    // The "main" method of the Lexer, this tokenizes the String that gets
    // passed through the constructor and returns the list of tokens;
    // assumes that a word starts with a letter and a number starts with a digit/'.'
    public LinkedList<Token> lex() throws IllegalArgumentException {
        LinkedList<Token> tokens = new LinkedList<Token>();

        // This loop runs until it reaches the end of the document
        while (!handler.isDone()) {

            // nextChar allows us to analyze the char were at without
            // incrementing the handler index by using peek
            char thisChar = handler.peek(0);
            Token symbol;

            if (thisChar == '#') {
                processComment();
            }

            // if this char is a space or a tab, skip and increment the position
            else if (thisChar == ' ' || thisChar == '\t') {
                handler.swallow(1);
                charPos++;
            }

            // if this char is a new line, add a seperator token, increment the line number,
            // reset the position, and finally skip over the character
            else if (thisChar == '\n') {
                tokens.add(new Token(TokenType.SEPERATOR, lineNum, charPos));
                lineNum++;
                charPos = 0;
                handler.swallow(1);
            }

            // if this char is a return carriage, then skip the character
            else if (thisChar == '\r') {
                handler.swallow(1);
            }

            // if this char is an 'r' then tokenize a register location
            else if(thisChar=='r' && Character.isDigit(handler.peek(1))){
                tokens.add(processRegister());
            }

            // if this char is a letter, process the word and add it to the list
            else if (Character.isLetter(thisChar)) {
                tokens.add(processWord());
            }

            // if this char is a number, process the number and add it to the list
            else if (Character.isDigit(thisChar) || thisChar=='-') {
                tokens.add(processNumber());
            }

            // if none of the above were true, this char is an unknown character
            else {
                throw new IllegalArgumentException("Unknown character '" + thisChar + "' ; Line: " + lineNum + "; Position: " + charPos);
            }
        }
        return tokens;
    }

    //Processes the characters associated with a register, associating the value in the token to the
    //index that the register lies (e.g. r1 will have a value of 1 inside the token)
    private Token processRegister() {

        if(handler.getChar() != 'r'){
            throw new IllegalArgumentException("Invalid token type");
        }
        if(handler.isDone()){
            throw new IllegalArgumentException("Invalid token type");
        }

        //Gets the first digit of the index for the register
        String num = handler.getChar() + "";

        //If the next token is the end of file or a separator, return the current token
        if(handler.isDone() || handler.peek(0) == ' ' || handler.peek(0) == '\r' || handler.peek(0) == '\n'){
            return new Token(TokenType.REGISTER, lineNum, charPos, num);
        }
        //If there is another digit in the register (e.g. r31 has two digits) then return that token
        //with the next digit added on
        else if(!handler.isDone() && Character.isDigit(handler.peek(0))){
            return new Token(TokenType.REGISTER, lineNum, charPos, num + handler.getChar());
        }
        throw new IllegalArgumentException("Invalid token type");
    }

    // This method increments through a series of characters and
    // adds them to a String in an attempt to form a word token.
    // Assumes the first character is a letter
    private Token processWord() throws IllegalArgumentException {
        String word = "";
        int position = charPos;

        // Adds a letter to the String if the handler is not at the end of the
        // document, and continues if the character is a digit, letter, or underscore
        while ((!handler.isDone() && (Character.isDigit(handler.peek(0)) || Character.isLetter(handler.peek(0))
                || handler.peek(0) == '_'))) {
            word += handler.getChar();
            charPos++;
        }

        // Checks if the word is actually a key word in AWK
        if (keyWords.containsKey(word)) {
            return new Token(keyWords.get(word), lineNum, position, word);
        }
        throw new IllegalArgumentException("Invalid token type");
    }

    // This method increments through a series of characters and
    // adds them to a String in an attempt to form a number token.
    // Assumes the first character is a digit.
    private Token processNumber() {
        String number = "";
        int position = charPos;
        // Adds a number or '.' to the String as long as the handler isn't done and the
        // character is either
        // a digit or decimal
        while (!handler.isDone() && (Character.isDigit(handler.peek(0)) || handler.peek(0) == '.' || handler.peek(0) == '-')) {
            number += handler.getChar();
            charPos++;
        }

        // This condition checks if after the number is made we have encountered a
        // non valid character in our number (a letter or decimal)
        if (!handler.isDone() && (Character.isLetter(handler.peek(0)) && handler.peek(0) != '.')) {
            throw new NumberFormatException("Number not valid; Line: " + lineNum + "; Position: " + charPos);
        }

        return new Token(TokenType.NUMBER, lineNum, position, number);
    }

    // This method swallows all characters from the position it is called
    // until it meets a new line character
    private void processComment() {
        handler.swallow(0);
        while (!handler.isDone() && handler.peek(0) != '\n') {
            handler.swallow(1);
        }
    }
}
