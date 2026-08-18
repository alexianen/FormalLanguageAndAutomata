import java.util.*;

public class RegularGrammar {
    // Nonterminals are the FA states; terminals are a,b
    public final Map<String, Set<String>> productions = new LinkedHashMap<>();
    public final String start;

    public RegularGrammar(String start) {
        this.start = start;
    }

    public void addProduction(String fromNonterminal, String rhs) {
        productions.computeIfAbsent(fromNonterminal, k -> new LinkedHashSet<>()).add(rhs);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> e : productions.entrySet()) {
            String A = e.getKey();
            sb.append(A).append(" -> ");
            List<String> r = new ArrayList<>();
            for (String rhs : e.getValue()) {
                if (rhs.isEmpty()) r.add("ε");
                else r.add(rhs);
            }
            sb.append(String.join(" | ", r)).append("\n");
        }
        return sb.toString();
    }

    public String classify(Set<String> nonterminals, Set<Character> terminals) {
        boolean allLhsSingleNt = true;
        boolean isType3 = true;
        for (Map.Entry<String, Set<String>> e : productions.entrySet()) {
            String lhs = e.getKey();
            if (!nonterminals.contains(lhs) || lhs.isEmpty()) {
                allLhsSingleNt = false;
            }
            for (String rhs : e.getValue()) {
                if (rhs.isEmpty()) {
                    // epsilon allowed only on start
                    if (!lhs.equals(start)) isType3 = false;
                    continue;
                }

                if (rhs.length() == 1) {
                    char c = rhs.charAt(0);
                    if (!terminals.contains(c)) isType3 = false;
                } else if (rhs.length() >= 2) {
                    char first = rhs.charAt(0);
                    String rest = rhs.substring(1);
                    // rest must be exactly the name of a single nonterminal (state name)
                    if (!terminals.contains(first) || !nonterminals.contains(rest)) {
                        isType3 = false;
                    }
                } else {
                    isType3 = false;
                }
            }
        }
        if (!allLhsSingleNt) return "Type-0 (unrestricted)";
        if (isType3) return "Type-3 (regular)";
        return "Type-2 (context-free)";
    }
}