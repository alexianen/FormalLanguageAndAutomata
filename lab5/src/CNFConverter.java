import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple CFG -> CNF converter.
 *
 * Representation:
 * - Nonterminals and terminals are String tokens.
 * - Epsilon is represented as an empty RHS (rhs.size() == 0).
 *
 * This is the same implementation I shared earlier but with no main() inside,
 * so a separate Main class can call it.
 */
public class CNFConverter {

    // Production class
    public static class Production {
        public final String lhs;
        public final List<String> rhs; // empty list = epsilon

        public Production(String lhs, List<String> rhs) {
            this.lhs = lhs;
            this.rhs = Collections.unmodifiableList(new ArrayList<>(rhs));
        }

        public static Production of(String lhs, String... rhs) {
            return new Production(lhs, Arrays.asList(rhs));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Production)) return false;
            Production p = (Production) o;
            return lhs.equals(p.lhs) && rhs.equals(p.rhs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lhs, rhs);
        }

        @Override
        public String toString() {
            if (rhs.isEmpty()) return lhs + " -> ε";
            return lhs + " -> " + String.join(" ", rhs);
        }
    }

    // Grammar container
    public static class Grammar {
        public Set<String> nonTerminals = new LinkedHashSet<>();
        public Set<String> terminals = new LinkedHashSet<>();
        public String startSymbol;
        public Set<Production> productions = new LinkedHashSet<>();
    }

    private int freshCounter = 0;

    private String freshNonTerminal() {
        return "X_" + (freshCounter++);
    }

    /**
     * Main entry: convert grammar g into CNF and return new Grammar.
     */
    public Grammar toCNF(Grammar g) {
        // Work on a copy
        Grammar grammar = copyGrammar(g);

        // 0. If start appears on RHS or start derives epsilon, create new start S0 -> S
        String oldStart = grammar.startSymbol;
        String newStart = oldStart;
        boolean needNewStart = grammar.productions.stream().anyMatch(p -> p.rhs.contains(oldStart))
                || grammar.productions.stream().anyMatch(p -> p.lhs.equals(oldStart) && p.rhs.isEmpty());
        if (needNewStart) {
            newStart = oldStart + "_S0";
            grammar.nonTerminals.add(newStart);
            grammar.productions.add(new Production(newStart, Arrays.asList(oldStart)));
            grammar.startSymbol = newStart;
        }

        // 1. Remove ε-productions
        removeEpsilonProductions(grammar);

        // 2. Remove unit productions (renaming)
        removeUnitProductions(grammar);

        // 3. Remove non-productive symbols
        removeNonProductive(grammar);

        // 4. Remove inaccessible symbols
        removeInaccessible(grammar);

        // 5. Replace terminals in RHSs of length >= 2 with preterminals
        replaceTerminalsInLongProductions(grammar);

        // 6. Binarize productions having length > 2
        binarizeProductions(grammar);

        // Final cleaning: update terminal/nonterminal sets
        rebuildSymbolSets(grammar);

        return grammar;
    }

    private Grammar copyGrammar(Grammar g) {
        Grammar copy = new Grammar();
        copy.nonTerminals.addAll(g.nonTerminals);
        copy.terminals.addAll(g.terminals);
        copy.startSymbol = g.startSymbol;
        for (Production p : g.productions) {
            copy.productions.add(new Production(p.lhs, p.rhs));
        }
        return copy;
    }

    // Remove epsilons by computing nullable set and creating new productions with nullable symbols removed
    private void removeEpsilonProductions(Grammar g) {
        Set<String> nullable = new HashSet<>();
        // Find nullable nonterminals
        boolean changed;
        do {
            changed = false;
            for (Production p : g.productions) {
                if (!nullable.contains(p.lhs)) {
                    if (p.rhs.isEmpty() || p.rhs.stream().allMatch(nullable::contains)) {
                        nullable.add(p.lhs);
                        changed = true;
                    }
                }
            }
        } while (changed);

        // If no nullable, nothing to do
        if (nullable.isEmpty()) return;

        Set<Production> newProds = new LinkedHashSet<>();

        for (Production p : g.productions) {
            if (p.rhs.isEmpty()) {
                // drop explicit epsilon productions for now (we'll handle start symbol later)
                continue;
            }
            // For each subset of nullable positions, create new production with those removed
            int n = p.rhs.size();
            int subsets = 1 << n;
            for (int mask = 0; mask < subsets; mask++) {
                List<String> rhs2 = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    String sym = p.rhs.get(i);
                    if ((mask & (1 << i)) != 0 && nullable.contains(sym)) {
                        // remove this nullable symbol
                    } else {
                        rhs2.add(sym);
                    }
                }
                // Do not create epsilon here unless LHS is start symbol (we handle start elsewhere)
                if (rhs2.isEmpty()) {
                    if (p.lhs.equals(g.startSymbol)) {
                        newProds.add(new Production(p.lhs, rhs2)); // epsilon allowed for start
                    }
                } else {
                    newProds.add(new Production(p.lhs, rhs2));
                }
            }
        }

        g.productions = newProds;
    }

    // Remove unit productions (A -> B) where B is a single nonterminal
    private void removeUnitProductions(Grammar g) {
        // compute unit pairs (A ->* B via only unit productions)
        Map<String, Set<String>> unitReach = new HashMap<>();
        for (String A : g.nonTerminals) {
            Set<String> reach = new HashSet<>();
            Deque<String> stack = new ArrayDeque<>();
            stack.push(A);
            while (!stack.isEmpty()) {
                String x = stack.pop();
                for (Production p : g.productions) {
                    if (p.lhs.equals(x) && p.rhs.size() == 1 && g.nonTerminals.contains(p.rhs.get(0))) {
                        String y = p.rhs.get(0);
                        if (!reach.contains(y)) {
                            reach.add(y);
                            stack.push(y);
                        }
                    }
                }
            }
            unitReach.put(A, reach);
        }

        Set<Production> newProds = new LinkedHashSet<>();
        for (Production p : g.productions) {
            // keep non-unit productions
            if (!(p.rhs.size() == 1 && g.nonTerminals.contains(p.rhs.get(0)))) {
                newProds.add(p);
            }
        }
        // Add productions A -> alpha for every (A ->* B) and (B -> alpha) where alpha is not unit
        for (String A : g.nonTerminals) {
            Set<String> reach = unitReach.getOrDefault(A, Collections.emptySet());
            for (String B : reach) {
                for (Production p : g.productions) {
                    if (p.lhs.equals(B) && !(p.rhs.size() == 1 && g.nonTerminals.contains(p.rhs.get(0)))) {
                        newProds.add(new Production(A, p.rhs));
                    }
                }
            }
        }
        g.productions = newProds;
    }

    // Remove non-productive symbols (that cannot derive terminal strings)
    private void removeNonProductive(Grammar g) {
        Set<String> productive = new HashSet<>();
        boolean changed;
        do {
            changed = false;
            for (Production p : g.productions) {
                if (!productive.contains(p.lhs)) {
                    boolean allGood = true;
                    for (String sym : p.rhs) {
                        if (g.nonTerminals.contains(sym) && !productive.contains(sym)) {
                            allGood = false;
                            break;
                        }
                        // terminal is fine
                    }
                    if (allGood) {
                        productive.add(p.lhs);
                        changed = true;
                    }
                }
            }
        } while (changed);

        // Remove productions whose LHS is not productive or RHS contains non-productive nonterminals
        g.productions = g.productions.stream()
                .filter(p -> productive.contains(p.lhs))
                .filter(p -> p.rhs.stream().allMatch(sym -> !g.nonTerminals.contains(sym) || productive.contains(sym)))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        g.nonTerminals.retainAll(productive);
    }

    // Remove inaccessible (unreachable) symbols from start
    private void removeInaccessible(Grammar g) {
        Set<String> reachable = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(g.startSymbol);
        reachable.add(g.startSymbol);
        while (!stack.isEmpty()) {
            String A = stack.pop();
            for (Production p : g.productions) {
                if (p.lhs.equals(A)) {
                    for (String sym : p.rhs) {
                        if (g.nonTerminals.contains(sym) && !reachable.contains(sym)) {
                            reachable.add(sym);
                            stack.push(sym);
                        }
                    }
                }
            }
        }
        g.productions = g.productions.stream().filter(p -> reachable.contains(p.lhs)).collect(Collectors.toCollection(LinkedHashSet::new));
        g.nonTerminals.retainAll(reachable);
    }

    // For RHS length >= 2, replace terminals with new preterminals (e.g., T_a -> a)
    private void replaceTerminalsInLongProductions(Grammar g) {
        Map<String, String> terminalToPreterm = new HashMap<>();
        Set<Production> newProds = new LinkedHashSet<>(g.productions);

        for (Production p : new ArrayList<>(g.productions)) {
            if (p.rhs.size() >= 2) {
                List<String> rhs2 = new ArrayList<>();
                boolean changed = false;
                for (String sym : p.rhs) {
                    if (g.terminals.contains(sym)) {
                        // replace terminal with preterminal
                        String pre;
                        if (terminalToPreterm.containsKey(sym)) {
                            pre = terminalToPreterm.get(sym);
                        } else {
                            pre = "T_" + sym;
                            // ensure no name collision
                            while (g.nonTerminals.contains(pre)) {
                                pre = "T_" + sym + "_" + freshCounter++;
                            }
                            terminalToPreterm.put(sym, pre);
                            g.nonTerminals.add(pre);
                            // add pre -> terminal production
                            newProds.add(new Production(pre, Arrays.asList(sym)));
                        }
                        rhs2.add(pre);
                        changed = true;
                    } else {
                        rhs2.add(sym);
                    }
                }
                if (changed) {
                    // replace original production with new one
                    newProds.remove(p);
                    newProds.add(new Production(p.lhs, rhs2));
                }
            }
        }

        g.productions = newProds;
    }

    // Convert productions with RHS length > 2 into binary productions using fresh nonterminals
    private void binarizeProductions(Grammar g) {
        Set<Production> newProds = new LinkedHashSet<>();
        for (Production p : g.productions) {
            if (p.rhs.size() <= 2) {
                newProds.add(p);
            } else {
                List<String> symbols = new ArrayList<>(p.rhs);
                String prev = p.lhs;
                while (symbols.size() > 2) {
                    String first = symbols.remove(0);
                    String newNT = freshNonTerminal();
                    g.nonTerminals.add(newNT);
                    newProds.add(new Production(prev, Arrays.asList(first, newNT)));
                    prev = newNT;
                }
                newProds.add(new Production(prev, Arrays.asList(symbols.get(0), symbols.get(1))));
            }
        }
        g.productions = newProds;
    }

    private void rebuildSymbolSets(Grammar g) {
        Set<String> nts = new LinkedHashSet<>();
        Set<String> ts = new LinkedHashSet<>();
        for (Production p : g.productions) {
            nts.add(p.lhs);
            for (String s : p.rhs) {
                if (Character.isUpperCase(s.charAt(0)) || s.startsWith("T_") || s.startsWith("X_") || s.matches(".*[A-Z].*")) {
                    if (g.nonTerminals.contains(s)) nts.add(s);
                    else if (g.terminals.contains(s)) ts.add(s);
                    else {
                        if (s.length() == 1 && Character.isLowerCase(s.charAt(0))) ts.add(s);
                        else nts.add(s);
                    }
                } else {
                    ts.add(s);
                }
            }
        }
        g.nonTerminals = nts;
        g.terminals = ts;
    }

    // -------------------
    // Utilities & demo helpers
    // -------------------

    public static void printGrammar(Grammar g) {
        System.out.println("Start symbol: " + g.startSymbol);
        System.out.println("Nonterminals: " + g.nonTerminals);
        System.out.println("Terminals: " + g.terminals);
        System.out.println("Productions:");
        for (Production p : g.productions) {
            System.out.println("  " + p);
        }
        System.out.println();
    }

    // Build the Variant 16 grammar from the problem statement
    public static Grammar buildVariant16() {
        Grammar g = new Grammar();
        g.nonTerminals.addAll(Arrays.asList("S", "A", "B", "C", "D"));
        g.terminals.addAll(Arrays.asList("a", "b"));
        g.startSymbol = "S";

        // 1. S -> a b A B   (abAB)
        g.productions.add(new Production("S", Arrays.asList("a", "b", "A", "B")));
        // 2. A -> a S a b   (aSab)
        g.productions.add(new Production("A", Arrays.asList("a", "S", "a", "b")));
        // 3. A -> B S
        g.productions.add(new Production("A", Arrays.asList("B", "S")));
        // 4. A -> a A
        g.productions.add(new Production("A", Arrays.asList("a", "A")));
        // 5. A -> b
        g.productions.add(new Production("A", Arrays.asList("b")));
        // 6. B -> B A
        g.productions.add(new Production("B", Arrays.asList("B", "A")));
        // 7. B -> a b a b B   (ababB)
        g.productions.add(new Production("B", Arrays.asList("a", "b", "a", "b", "B")));
        // 8. B -> b
        g.productions.add(new Production("B", Arrays.asList("b")));
        // 9. B -> ε
        g.productions.add(new Production("B", new ArrayList<>())); // epsilon
        // 10. C -> A S
        g.productions.add(new Production("C", Arrays.asList("A", "S")));
        // D has no productions in the statement

        return g;
    }
}