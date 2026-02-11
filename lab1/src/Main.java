import java.util.*;

public class Main {
    public static void main(String[] args) {
        Grammar grammar = new Grammar(); // initializing the grammar
        System.out.println("Generated Strings:"); // generating random strings
        List<String> generatedWords = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String word = grammar.generateString();
            generatedWords.add(word);
            System.out.println((i + 1) + ". " + word);
        }

        FiniteAutomaton fa = grammar.toFiniteAutomaton(); // convert grammar to finite automata

        System.out.println("\nValidation Results:"); // validating the generated strings
        for (String word : generatedWords) {
            boolean isAccepted = fa.stringBelongToLanguage(word);
            System.out.println(word + ": " + isAccepted);
        }

        String invalid = "abcdefg"; // invalid string
        System.out.println("\nTesting invalid string '" + invalid + "':");
        System.out.println("result: " + fa.stringBelongToLanguage(invalid));
    }
}