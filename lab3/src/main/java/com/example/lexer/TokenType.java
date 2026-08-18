package main.java.com.example.lexer;

public enum TokenType {
    // Literals
    NUMBER,
    IDENTIFIER,
    FUNCTION,

    // Operators
    PLUS, MINUS, STAR, SLASH, CARET,

    // Grouping / punctuation
    LPAREN, RPAREN, COMMA,

    // End of input
    EOF,

    // Error / unknown
    INVALID
}