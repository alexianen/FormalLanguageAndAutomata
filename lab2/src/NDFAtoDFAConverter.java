import java.util.*;

public class NDFAtoDFAConverter {
    // Returns a new FiniteAutomaton that is deterministic equivalent of input (subset construction).
    public static FiniteAutomaton convert(FiniteAutomaton ndfa) {
        FiniteAutomaton dfa = new FiniteAutomaton();
        dfa.alphabet.addAll(ndfa.alphabet);

        // Represent DFA states as sorted-string sets like "{q0,q1}"
        String startSetKey = makeKey(Collections.singleton(ndfa.startState));
        Queue<Set<String>> queue = new ArrayDeque<>();
        Map<String, Set<String>> keyToSet = new HashMap<>();
        Set<String> seenKeys = new LinkedHashSet<>();

        Set<String> startSet = new LinkedHashSet<>();
        startSet.add(ndfa.startState);
        queue.add(startSet);
        String startKey = makeKey(startSet);
        keyToSet.put(startKey, startSet);
        dfa.setStart(startKey);
        seenKeys.add(startKey);
        dfa.addState(startKey);

        if (containsFinal(startSet, ndfa.finalStates)) {
            dfa.addFinal(startKey);
        }

        while (!queue.isEmpty()) {
            Set<String> currSet = queue.poll();
            String currKey = makeKey(currSet);
            dfa.addState(currKey);

            for (char a : ndfa.alphabet) {
                Set<String> dest = new LinkedHashSet<>();
                for (String q : currSet) {
                    dest.addAll(ndfa.getTransitions(q, a));
                }
                if (dest.isEmpty()) continue;
                String destKey = makeKey(dest);
                if (!keyToSet.containsKey(destKey)) {
                    keyToSet.put(destKey, dest);
                }
                if (!seenKeys.contains(destKey)) {
                    queue.add(dest);
                    seenKeys.add(destKey);
                    if (containsFinal(dest, ndfa.finalStates)) dfa.addFinal(destKey);
                }
                // add deterministic transition: currKey --a--> destKey
                dfa.addTransition(currKey, a, destKey);
            }
        }
        return dfa;
    }

    private static boolean containsFinal(Set<String> s, Set<String> finals) {
        for (String q : s) if (finals.contains(q)) return true;
        return false;
    }

    private static String makeKey(Collection<String> set) {
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return "{" + String.join(",", list) + "}";
    }
}