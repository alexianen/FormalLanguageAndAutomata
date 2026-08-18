package main.java.com.example.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private final int length;
    private final List<Token> tokens = new ArrayList<>();
    private int pos = 0;

    public Lexer(String input) {
        this.input = input != null ? input : "";
        this.length = this.input.length();
    }

    public List<Token> tokenize() {
        tokens.clear();
        pos = 0;
        while (!isAtEnd()) {
            skipWhitespaceAndComments();
            if (isAtEnd()) break;
            int start = pos;
            char c = advance();
            switch (c) {
                case '+': addToken(TokenType.PLUS, "+", start); break;
                case '-': addToken(TokenType.MINUS, "-", start); break;
                case '*': addToken(TokenType.STAR, "*", start); break;
                case '/': addToken(TokenType.SLASH, "/", start); break;
                case '^': addToken(TokenType.CARET, "^", start); break;
                case '(': addToken(TokenType.LPAREN, "(", start); break;
                case ')': addToken(TokenType.RPAREN, ")", start); break;
                case ',': addToken(TokenType.COMMA, ",", start); break;
                default:
                    if (isDigit(c) || (c == '.' && peekIsDigit())) {
                        pos = start; // rewind to start of number
                        scanNumber();
                    } else if (isAlpha(c)) {
                        pos = start;
                        scanIdentifierOrFunction();
                    } else {
                        addToken(TokenType.INVALID, String.valueOf(c), start);
                    }
            }
        }
        tokens.add(new Token(TokenType.EOF, "", null, pos));
        return List.copyOf(tokens);
    }

    private void scanNumber() {
        int start = pos;
        boolean sawDot = false;

        // integer part (may be empty when number starts with '.')
        while (isDigit(peek())) advance();

        // fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            sawDot = true;
            advance(); // consume '.'
            while (isDigit(peek())) advance();
        } else if (peek() == '.') {
            // a solitary '.' not followed by digit: treat earlier portion as integer and leave '.' for next token
            // but we already consumed digits, so do nothing here (do not consume the dot)
        }

        // optional: scientific notation (e.g., 1.2e-3)
        if ((peek() == 'e' || peek() == 'E')) {
            int save = pos;
            advance();
            if (peek() == '+' || peek() == '-') advance();
            if (!isDigit(peek())) {
                // not a valid exponent: rollback to before 'e'
                pos = save;
            } else {
                while (isDigit(peek())) advance();
            }
        }

        String lexeme = input.substring(start, pos);
        try {
            Double value = Double.parseDouble(lexeme);
            addToken(TokenType.NUMBER, lexeme, value, start);
        } catch (NumberFormatException ex) {
            addToken(TokenType.INVALID, lexeme, start);
        }
    }

    private void scanIdentifierOrFunction() {
        int start = pos;
        advance(); // already know first char is alpha
        while (isAlphaNumeric(peek())) advance();
        String lexeme = input.substring(start, pos);
        String lower = lexeme.toLowerCase();
        if (lower.equals("sin") || lower.equals("cos")) {
            addToken(TokenType.FUNCTION, lexeme, start);
        } else {
            addToken(TokenType.IDENTIFIER, lexeme, start);
        }
    }

    private void skipWhitespaceAndComments() {
        while (!isAtEnd()) {
            char c = peek();
            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }
            // line comment starting with //
            if (c == '/' && peekNext() == '/') {
                // consume until end of line
                advance(); // '/'
                advance(); // second '/'
                while (!isAtEnd() && peek() != '\n' && peek() != '\r') advance();
                continue;
            }
            break;
        }
    }

    private boolean isAtEnd() { return pos >= length; }
    private char advance() { return input.charAt(pos++); }
    private char peek() { return isAtEnd() ? '\0' : input.charAt(pos); }
    private char peekNext() { return (pos + 1) >= length ? '\0' : input.charAt(pos + 1); }
    private boolean peekIsDigit() { return Character.isDigit(peek()); }

    private void addToken(TokenType type, String lexeme, int position) {
        tokens.add(new Token(type, lexeme, null, position));
    }

    private void addToken(TokenType type, String lexeme, Object literal, int position) {
        tokens.add(new Token(type, lexeme, literal, position));
    }

    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private boolean isAlpha(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'; }
    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }
}