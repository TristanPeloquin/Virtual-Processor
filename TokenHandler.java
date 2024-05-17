import java.util.LinkedList;
import java.util.Optional;

//Helper to allow for easier handling of the token list passed by the lexer
public class TokenHandler {

    private LinkedList<Token> tokens;

    public TokenHandler(LinkedList<Token> tokens) {
        this.tokens = tokens;
    }

    // Peeks at the next token if it is within the bounds of the list
    public Optional<Token> peek(int j) {
        if (j < tokens.size())
            return Optional.ofNullable(tokens.get(j));
        return Optional.empty();
    }

    // Returns true if there are more tokens in the list
    public boolean moreTokens() {
        return !tokens.isEmpty();
    }

    // Removes the token if the type matches with the first one in the list,
    // else returns an empty optional
    public Optional<Token> matchAndRemove(TokenType t) {
        if (tokens.size() <= 0)
            return Optional.empty();
        if (tokens.getFirst().getType().equals(t)) {
            return Optional.of(tokens.removeFirst());
        }
        return Optional.empty();
    }

}