
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//Instructions: when running, please provide the name to the file where your code is located, such
//as "code.txt", or run in debug mode, such as with "debug "copy r1 5"" to run instructions directly

//Runs the processor on the given arguments, where arg[0] = the file path for the assembly code,
//also includes a debugging option to allow for testing with a String for the code
public class Main {
    public static void main(String args[]) throws Exception {
        String code;
        // This condition allows for simple input of a String without
        // the need for a file, intended for debugging
        if (args[0].equals("debug")) {
            code = args[1];
        } else {
            code = new String(Files.readAllBytes(Paths.get(args[0])));
        }
        //Tokenizes and parses the file/code and output to a file in the CWD named "output.txt"
        Lexer lexer = new Lexer(code);
        LinkedList<Token> tokens = lexer.lex();
        Parser parser = new Parser(tokens);
        parser.parse();

        //Reads the list from the output file that the parser created
        BufferedReader reader = new BufferedReader(new FileReader("output.txt"));
        List<String> lines = reader.lines().toList();

        //Loads the list into an array and loads it into memory and runs the processor on said
        //instructions
        String[] linesArray = lines.toArray(new String[0]);
        MainMemory.load(linesArray);
        Processor processor = new Processor();
        processor.run();
    }
}
