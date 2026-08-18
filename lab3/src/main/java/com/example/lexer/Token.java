package main.java.com.example.lexer;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int position;

    public Token(TokenType type, String lexeme, Object literal, int position) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.position = position;
    }

    public TokenType getType() { return type; }
    public String getLexeme() { return lexeme; }
    public Object getLiteral() { return literal; }
    public int getPosition() { return position; }

    @Override
    public String toString() {
        if (literal != null) {
            return String.format("%s('%s', %s)@%d", type, lexeme, literal, position);
        } else {
            return String.format("%s('%s')@%d", type, lexeme, position);
        }
    }
}
