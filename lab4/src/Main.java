import java.util.*;

public class Main {
    public static void main(String[] args) {
        int maxRepeat = 5;
        int samplesPerRegex = 8;

        String[] regexes = {
                "(a|b)(c|d)E+G?",
                "P(Q|R|S)T(UV|W|X)*Z+",
                "1(0|1)*2((3|4)^5)36"
        };

        for (int i = 0; i < regexes.length; i++) {
            String regex = regexes[i];
            System.out.println();
            System.out.println("----------- REGEX " + (i+1) + ": " + regex + " -----------");

            List<String> parseTrace = new ArrayList<>();
            RegexGenerator.Node ast;
            try {
                ast = RegexGenerator.parseRegexWithTrace(regex, parseTrace);
            } catch (RuntimeException ex) {
                System.err.println("Parse error for \"" + regex + "\": " + ex.getMessage());
                continue;
            }

            System.out.println("Parse trace:");
            for (String t : parseTrace) System.out.println("  " + t);

            List<String> genTrace = new ArrayList<>();
            List<String> examples = RegexGenerator.generateSamples(ast, samplesPerRegex, maxRepeat, System.nanoTime(), genTrace);

            System.out.println("Generated examples:");
            for (String ex : examples) System.out.println("  " + ex);

            System.out.println("Detailed generation trace for first example:");
            for (String t : genTrace) System.out.println("  " + t);

            System.out.println();
        }
    }
}