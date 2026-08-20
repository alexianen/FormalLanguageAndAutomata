import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Simple regex-based lexer. Scans input from start to end.
 * Uses TokenType patterns in a fixed order (keywords before identifier).
 */
public class Lexer {
    private final String input;
    private final int length;
    private int pos = 0;

    public Lexer(String input) {
        this.input = input;
        this.length = input.length();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < length) {
            boolean matched = false;

            // Try token types in a particular order so keywords win over identifiers
            for (TokenType tt : new TokenType[]{
                    TokenType.PRINT,
                    TokenType.WHITESPACE,
                    TokenType.COMMENT,
                    TokenType.NUMBER,
                    TokenType.IDENTIFIER,
                    TokenType.PLUS,
                    TokenType.MINUS,
                    TokenType.MUL,
                    TokenType.DIV,
                    TokenType.LPAREN,
                    TokenType.RPAREN,
                    TokenType.ASSIGN,
                    TokenType.SEMICOLON
            }) {
                if (tt.getPattern() == null) continue;
                Matcher m = tt.getPattern().matcher(input.substring(pos));
                if (m.find() && m.start() == 0) {
                    String lexeme = m.group();
                    if (tt != TokenType.WHITESPACE && tt != TokenType.COMMENT) {
                        tokens.add(new Token(tt, lexeme, pos));
                    }
                    pos += lexeme.length();
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                // No pattern matched at the current position -> unknown/illegal char
                throw new RuntimeException("Unexpected character at pos " + pos + ": '" + input.charAt(pos) + "'");
            }
        }
        tokens.add(new Token(TokenType.EOF, "<EOF>", pos));
        return tokens;
    }
}