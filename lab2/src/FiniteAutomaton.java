import java.util.*;

public class FiniteAutomaton {
    public final Set<String> states = new LinkedHashSet<>();
    public final Set<Character> alphabet = new LinkedHashSet<>();
    public String startState;
    public final Set<String> finalStates = new LinkedHashSet<>();
    // transitions: (from, symbol) -> set of to states (allow nondet)
    private final Map<String, Map<Character, Set<String>>> transitions = new HashMap<>();

    public FiniteAutomaton() {}

    public void addState(String q) { states.add(q); }

    public void setStart(String q) { startState = q; addState(q); }

    public void addFinal(String q) { finalStates.add(q); addState(q); }

    public void addTransition(String from, char symbol, String to) {
        addState(from); addState(to);
        alphabet.add(symbol);
        transitions.computeIfAbsent(from, k -> new HashMap<>())
                .computeIfAbsent(symbol, k -> new LinkedHashSet<>())
                .add(to);
    }

    public Set<String> getTransitions(String from, char symbol) {
        return transitions.getOrDefault(from, Collections.emptyMap())
                .getOrDefault(symbol, Collections.emptySet());
    }

    public Map<Character, Set<String>> getTransitionsFrom(String from) {
        return transitions.getOrDefault(from, Collections.emptyMap());
    }

    public boolean isDeterministic() {
        for (String s : states) {
            Map<Character, Set<String>> map = transitions.get(s);
            if (map == null) continue;
            for (Map.Entry<Character, Set<String>> e : map.entrySet()) {
                if (e.getValue().size() > 1) return false;
            }
        }
        return true;
    }

    // Convert to a RegularGrammar (right-linear)
    public RegularGrammar toRegularGrammar() {
        RegularGrammar g = new RegularGrammar(startState);
        for (String p : states) {
            Map<Character, Set<String>> map = transitions.getOrDefault(p, Collections.emptyMap());
            for (Map.Entry<Character, Set<String>> e : map.entrySet()) {
                char a = e.getKey();
                for (String q : e.getValue()) {
                    // production: p -> a q
                    g.addProduction(p, String.valueOf(a) + q);
                    // if q is final, also add p -> a
                    if (finalStates.contains(q)) {
                        g.addProduction(p, String.valueOf(a));
                    }
                }
            }
        }
        // If start is final, add S -> epsilon
        if (finalStates.contains(startState)) {
            g.addProduction(startState, ""); // epsilon represented as empty string
        }
        return g;
    }

    // For printing convenience
    public String transitionsToString() {
        StringBuilder sb = new StringBuilder();
        for (String p : states) {
            Map<Character, Set<String>> map = transitions.getOrDefault(p, Collections.emptyMap());
            for (char a : alphabet) {
                Set<String> to = map.getOrDefault(a, Collections.emptySet());
                if (!to.isEmpty()) {
                    sb.append(String.format("%s --%s--> %s%n", p, a, to));
                }
            }
        }
        return sb.toString();
    }
}