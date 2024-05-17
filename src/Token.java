
//This class is a representation of a "token", which contains information regarding
//what kind of token it is, where it resides on a document, and  optionally 
//its value (ex. "hello")
public class Token {

    private TokenType type;
    private String value;
    private int lineNum;
    private int charPos;

    public Token(TokenType type, int lineNum, int charPos) {
        this.type = type;
        this.lineNum = lineNum;
        this.charPos = charPos;
    }

    public Token(TokenType type, int lineNum, int charPos, String value) {
        this(type, lineNum, charPos);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }

    public String toString() {
        return type + "(" + lineNum + "," + charPos + "): \"" + value + "\"";
    }

}
