import java.util.*;

/**
 * RegexGenerator
 *
 * Supports a small regex dialect:
 * - literals (single characters, letters/digits)
 * - concatenation (implicit)
 * - alternation: a|b
 * - grouping: ( ... )
 * - postfix: * (Kleene star), + (one-or-more), ? (zero-or-one)
 * - exact repetition: ^n (e.g., (3|4)^5 -> choose 3 or 4 exactly 5 times)
 *
 * Repetition for * and + is bounded by maxRepeat to avoid extremely long strings.
 *
 * Usage: change main() regex list or pass other regexes in code.
 */
public class RegexGenerator {

    // Parser with tracing
    static class Parser {
        private final String s;
        private int pos;
        private final List<String> trace;

        Parser(String s) {
            this.s = s;
            this.pos = 0;
            this.trace = new ArrayList<>();
        }

        Node parse() {
            trace.add("Start parsing: \"" + s + "\"");
            Node n = parseUnion();
            if (pos < s.length()) {
                throw new RuntimeException("Unexpected char at pos " + pos + ": '" + s.charAt(pos) + "'");
            }
            trace.add("Finished parsing, AST: " + n);
            return n;
        }

        List<String> getTrace() {
            return trace;
        }

        // union := concat ('|' concat)*
        private Node parseUnion() {
            Node left = parseConcat();
            while (peek() == '|') {
                consume(); // '|'
                trace.add("Found '|', parse right side of alternation at pos " + pos);
                Node right = parseConcat();
                left = new AltNode(left, right);
                trace.add("Built AltNode: " + left);
            }
            return left;
        }

        // concat := repeat+
        private Node parseConcat() {
            List<Node> parts = new ArrayList<>();
            while (pos < s.length() && peek() != ')' && peek() != '|') {
                Node r = parseRepeat();
                parts.add(r);
            }
            if (parts.isEmpty()) {
                // empty concatenation -> empty string node
                return new EmptyNode();
            } else if (parts.size() == 1) {
                return parts.get(0);
            } else {
                Node concat = new ConcatNode(parts);
                trace.add("Built ConcatNode with " + parts.size() + " children");
                return concat;
            }
        }

        // repeat := primary (('*' | '+' | '?' | '^' number) )?
        private Node parseRepeat() {
            Node prim = parsePrimary();
            while (true) {
                if (peek() == '*') {
                    consume();
                    prim = new StarNode(prim);
                    trace.add("Applied '*' to node -> StarNode: " + prim);
                } else if (peek() == '+') {
                    consume();
                    prim = new PlusNode(prim);
                    trace.add("Applied '+' to node -> PlusNode: " + prim);
                } else if (peek() == '?') {
                    consume();
                    prim = new QuestionNode(prim);
                    trace.add("Applied '?' to node -> QuestionNode: " + prim);
                } else if (peek() == '^') {
                    consume();
                    int num = parseNumber();
                    prim = new ExactRepeatNode(prim, num);
                    trace.add("Applied '^" + num + "' to node -> ExactRepeatNode: " + prim);
                } else {
                    break;
                }
            }
            return prim;
        }

        // primary := literal | '(' union ')'
        private Node parsePrimary() {
            char c = peek();
            if (c == '(') {
                consume(); // '('
                trace.add("Start group at pos " + pos);
                Node inside = parseUnion();
                if (peek() != ')') {
                    throw new RuntimeException("Unclosed '(' at pos " + pos);
                }
                consume(); // ')'
                trace.add("Closed group at pos " + pos + ", group node: " + inside);
                return inside;
            } else {
                // literal single character
                if (c == '\0') {
                    throw new RuntimeException("Unexpected end of pattern");
                }
                consume();
                Node lit = new LiteralNode(String.valueOf(c));
                trace.add("Parsed literal '" + c + "'");
                return lit;
            }
        }

        private int parseNumber() {
            StringBuilder sb = new StringBuilder();
            while (pos < s.length() && Character.isDigit(s.charAt(pos))) {
                sb.append(s.charAt(pos));
                pos++;
            }
            if (sb.length() == 0) throw new RuntimeException("Expected number after ^ at pos " + pos);
            return Integer.parseInt(sb.toString());
        }

        private char peek() {
            if (pos >= s.length()) return '\0';
            return s.charAt(pos);
        }

        private void consume() {
            pos++;
        }
    }

    // AST
    interface Node {
        String generate(Random rnd, int maxRepeat, List<String> genTrace);
    }

    static class EmptyNode implements Node {
        @Override
        public String generate(Random rnd, int maxRepeat, List<String> genTrace) {
            genTrace.add("Generate Empty -> \"\"");
            return "";
        }

        @Override public String toString() { return "ε"; }
    }

    static class LiteralNode implements Node {
        final String lit;
        LiteralNode(String lit) { this.lit = lit; }
        @Override
        public String generate(Random rnd, int maxRepeat, List<String> genTrace) {
            genTrace.add("Literal '" + lit + "' -> \"" + lit + "\"");
            return lit;
        }
        @Override public String toString() { return lit; }
    }

    static class ConcatNode implements Node {
        final List<Node> parts;
        ConcatNode(List<Node> parts) { this.parts = parts; }
        @Override
        public String generate(Random rnd, int maxRepeat, List<String> genTrace) {
            genTrace.add("Concat: start (" + parts.size() + " parts)");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.size(); i++) {
                Node p = parts.get(i);
                genTrace.add("Concat: generate part " + (i+1) + "/" + parts.size());
                String s = p.generate(rnd, maxRepeat, genTrace);
                genTrace.add("Concat: part " + (i+1) + " produced \"" + s + "\"");
                sb.append(s);
            }
            String res = sb.toString();
            genTrace.add("Concat: result -> \"" + res + "\"");
            return res;
        }
        @Override public String toString() { return "Concat" + parts; }
    }

    static class AltNode implements Node {
        final Node left, right;
        AltNode(Node l, Node r) { this.left = l; this.right = r; }
        @Override
        public String generate(Random rnd, int maxRepeat, List<String> genTrace) {
            // choose left or right at random
            boolean chooseLeft = rnd.nextBoolean();
            genTrace.add("Alt: choose " + (chooseLeft ? "left" : "right"));
            Node chosen = chooseLeft ? left : right;
            String s = chosen.generate(rnd, maxRepeat, genTrace);
            genTrace.add("Alt: chosen produced \"" + s + "\"");
            return s;
        }
        @Override public String toString() { return "(" + left + "|" + right + ")"; }
    }

    static class StarNode implements Node {
        final Node child;
        StarNode(Node child) { this.child = child; }
        @Override
        public String generate(Random rnd, int maxRepeat, List<String> genTrace) {
            int times = rnd.nextInt(maxRepeat + 1); // 0..maxRepeat
            genTrace.add("Star: repeat 0.." + maxRepeat + " -> chosen " + times);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; i++) {
                genTrace.add("Star: iteration " + (i+1));
                sb.append(child.generate(rnd, maxRepeat, genTrace));
            }
            String res = sb.toString();
            genTrace.add("Star: result -> \"" + res + "\"");
            return res;
        }
        @Override public String toString() { return "(" + child + ")*"; }
    }

    static class PlusNode implements Node {
        final Node child;
        PlusNode(Node child) { this.child = child; }
        @Override
        public String generate(Random rnd, int maxRepeat, List<String> genTrace) {
            // one or more: choose in 1..maxRepeat
            int times = 1 + rnd.nextInt(maxRepeat); // 1..maxRepeat
            genTrace.add("Plus: repeat 1.." + maxRepeat + " -> chosen " + times);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; i++) {
                genTrace.add("Plus: iteration " + (i+1));
                sb.append(child.generate(rnd, maxRepeat, genTrace));
            }
            String res = sb.toString();
            genTrace.add("Plus: result -> \"" + res + "\"");
            return res;
        }
        @Override public String toString() { return "(" + child + ")+"; }
    }

    static class QuestionNode implements Node {
        final Node child;
        QuestionNode(Node child) { this.child = child; }
        @Override
        public String generate(Random rnd, int maxRepeat, List<String> genTrace) {
            boolean take = rnd.nextBoolean();
            genTrace.add("Question: choose " + (take ? "present" : "absent"));
            if (!take) {
                genTrace.add("Question: result -> \"\"");
                return "";
            } else {
                String s = child.generate(rnd, maxRepeat, genTrace);
                genTrace.add("Question: result -> \"" + s + "\"");
                return s;
            }
        }
        @Override public String toString() { return "(" + child + ")?"; }
    }

    static class ExactRepeatNode implements Node {
        final Node child;
        final int count;
        ExactRepeatNode(Node child, int count) { this.child = child; this.count = count; }
        @Override
        public String generate(Random rnd, int maxRepeat, List<String> genTrace) {
            genTrace.add("ExactRepeat: repeat exactly " + count + " times");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                genTrace.add("ExactRepeat: iteration " + (i+1));
                sb.append(child.generate(rnd, maxRepeat, genTrace));
            }
            String res = sb.toString();
            genTrace.add("ExactRepeat: result -> \"" + res + "\"");
            return res;
        }
        @Override public String toString() { return "(" + child + ")^" + count; }
    }

    // Utility: parse a regex into AST and return trace info
    static Node parseRegexWithTrace(String regex, List<String> parseTrace) {
        Parser p = new Parser(regex);
        Node ast = p.parse();
        parseTrace.addAll(p.getTrace());
        return ast;
    }

    // Generate N examples with optional detailed trace for the first one
    static List<String> generateSamples(Node ast, int samples, int maxRepeat, long seed, List<String> outTraceForFirst) {
        Random rnd = new Random(seed);
        Set<String> seen = new LinkedHashSet<>();
        List<String> results = new ArrayList<>();
        int attempts = 0;
        while (results.size() < samples && attempts < samples * 20) { // limit attempts
            List<String> genTrace = new ArrayList<>();
            String s = ast.generate(rnd, maxRepeat, genTrace);
            if (!seen.contains(s)) {
                seen.add(s);
                results.add(s);
                if (results.size() == 1 && outTraceForFirst != null) {
                    outTraceForFirst.addAll(genTrace);
                }
            }
            attempts++;
        }
        return results;
    }

    public static void main(String[] args) {
        // Variant 1 regexes (as provided)
        String r1 = "(a|b)(c|d)E+G?";         // (a|b)(c|d)E+G?
        String r2 = "P(Q|R|S)T(UV|W|X)+Z+";   // slight change: the original had (UV|W|X)*Z+; I use + on that group? keep as provided in image: (UV|W|X)*Z+ -> but we'll show both possibilities.
        // Based on your text the second is: P(Q|R|S)T(UV|W|X)*Z+
        String r2_a = "P(Q|R|S)T(UV|W|X)*Z+";
        // Third: 1(0|1)*2((3|4)^5)36
        String r3 = "1(0|1)*2((3|4)^5)36";

        int maxRepeat = 5; // limit for * and + expansions
        int samplesPerRegex = 8;

        List<String> regexes = Arrays.asList(r1, r2_a, r3);

        for (int i = 0; i < regexes.size(); i++) {
            String regex = regexes.get(i);
            System.out.println("=== Regex " + (i+1) + ": " + regex + " ===");
            List<String> parseTrace = new ArrayList<>();
            Node ast;
            try {
                ast = parseRegexWithTrace(regex, parseTrace);
            } catch (RuntimeException ex) {
                System.err.println("Parse error: " + ex.getMessage());
                continue;
            }

            System.out.println("Parse trace (short):");
            for (String t : parseTrace) {
                System.out.println("  " + t);
            }

            List<String> genTrace = new ArrayList<>();
            List<String> examples = generateSamples(ast, samplesPerRegex, maxRepeat, System.nanoTime(), genTrace);

            System.out.println("Generated examples:");
            for (String ex : examples) {
                System.out.println("  " + ex);
            }
            System.out.println("Detailed generation trace for first example:");
            for (String t : genTrace) {
                System.out.println("  " + t);
            }
            System.out.println();
        }

        // Demonstrate alternate second regex variant if needed:
        System.out.println("Note: if your second regex instead meant P(Q|R|S)T(UV|W|X)*Z+ (as in the image) we've already used that.");
        System.out.println("You can change maxRepeat to control the * and + expansions (default " + maxRepeat + ").");
    }
}