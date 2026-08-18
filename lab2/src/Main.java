import java.util.*;

public class Main {
    public static void main(String[] args) {
        FiniteAutomaton fa = buildVariantAutomaton();

        System.out.println("=== Given Finite Automaton ===");
        System.out.println("States: " + fa.states);
        System.out.println("Alphabet: " + fa.alphabet);
        System.out.println("Start: " + fa.startState);
        System.out.println("Finals: " + fa.finalStates);
        System.out.println("Transitions:");
        System.out.println(fa.transitionsToString());

        boolean det = fa.isDeterministic();
        System.out.println("Deterministic? " + det);

        // Convert to regular grammar
        RegularGrammar rg = fa.toRegularGrammar();
        System.out.println("\n=== Regular Grammar (right-linear) ===");
        System.out.println(rg.toString());

        // Classify grammar in Chomsky hierarchy
        Set<String> nonterminals = new HashSet<>(fa.states);
        Set<Character> terminals = new HashSet<>(fa.alphabet);
        String classification = rg.classify(nonterminals, terminals);
        System.out.println("Grammar classification: " + classification);

        // If nondeterministic, convert to DFA
        if (!det) {
            System.out.println("\n=== Converting NDFA -> DFA (subset construction) ===");
            FiniteAutomaton dfa = NDFAtoDFAConverter.convert(fa);
            System.out.println("DFA States: " + dfa.states);
            System.out.println("DFA Start: " + dfa.startState);
            System.out.println("DFA Finals: " + dfa.finalStates);
            System.out.println("DFA Transitions:");
            System.out.println(dfa.transitionsToString());
        } else {
            System.out.println("FA already deterministic, no conversion needed.");
        }
    }

    private static FiniteAutomaton buildVariantAutomaton() {
        FiniteAutomaton fa = new FiniteAutomaton();
        fa.addState("q0"); fa.addState("q1"); fa.addState("q2"); fa.addState("q3");
        fa.setStart("q0");
        fa.addFinal("q3");

        fa.addTransition("q0", 'a', "q1");
        fa.addTransition("q1", 'b', "q1");
        fa.addTransition("q1", 'b', "q2"); // nondeterministic branching
        fa.addTransition("q2", 'a', "q2");
        fa.addTransition("q2", 'b', "q3");
        fa.addTransition("q0", 'b', "q0");

        return fa;
    }
}