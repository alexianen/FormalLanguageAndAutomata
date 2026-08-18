package main.java.com.example.lexer;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] examples = new String[] {
                "sin(3.1415) + cos(0) - 2.5 * x ^ 2",
                "42 + 0.5 - .25 // numbers including leading-dot",
                "a_b + funcName(1, 2.0e-3) // identifiers and exponent",
                "1..2 // malformed should be tokenized as 1 . . 2 or produce INVALID tokens"
        };

        for (String ex : examples) {
            System.out.println("Input: " + ex);
            Lexer lexer = new Lexer(ex);
            List<Token> tokens = lexer.tokenize();
            for (Token t : tokens) {
                System.out.println("  " + t);
            }
            System.out.println();
        }
    }
}