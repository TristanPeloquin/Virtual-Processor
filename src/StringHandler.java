
import java.io.IOException;

//This class "handles" the String that gets passed to it, allowing the Lexer
//to peek ahead and a get the character at an incremented index, as well as other
//functionality
public class StringHandler {
    
    private String content;
    private int index;

    public StringHandler(String content) throws IOException{
        index = 0;
        this.content = content;
    }

    //Peeks ahead of the index by a specified amount, returns a char
    public char peek(int i){
        return content.charAt(index+i);
    }

    //Similar to peek, but returns the String from the index to the
    //specified amount i
    public String peekString(int i){
        if(i+index>content.length()){
            return null;
        }
        return content.substring(index, i+index);
    }

    //Note: increments the index whenever it is called, as opposed to peek()
    public char getChar(){
        index++;
        return content.charAt(index-1);
    }

    //Will skip the number of indexes specified
    public void swallow(int i){
        index += i;
    }

    //Returns true if the document is empty or the index has reached the end
    public boolean isDone(){
        return content.isEmpty() || content.length() <= index;
    }

    //Returns the remaining content in the document
    public String remainder(){
        return content.substring(index);
    }



}
