import java.util.regex.Pattern;

/**
 * Token types with regex patterns used by the Lexer.
 */
public enum TokenType {
    // Keywords (match before IDENTIFIER)
    PRINT("^print\\b"),

    // Single-character tokens
    PLUS("^\\+"),
    MINUS("^\\-"),
    MUL("^\\*"),
    DIV("^/"),
    LPAREN("^\\("),
    RPAREN("^\\)"),
    ASSIGN("^="),
    SEMICOLON("^;"),

    // Literals and identifiers
    NUMBER("^\\d+(?:\\.\\d+)?"),
    IDENTIFIER("^[A-Za-z_][A-Za-z0-9_]*"),

    // Whitespace and comments (skipped by lexer)
    WHITESPACE("^\\s+"),
    COMMENT("^//.*(?:\\n|$)"),

    // End of input marker (no regex)
    EOF("");

    private final Pattern pattern;

    TokenType(String regex) {
        if (regex.isEmpty()) {
            this.pattern = null;
        } else {
            this.pattern = Pattern.compile(regex);
        }
    }

    public Pattern getPattern() {
        return pattern;
    }
}