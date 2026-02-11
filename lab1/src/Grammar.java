import java.util.*;

public class Grammar {
    private Set<String> nonTerminals; // VN = {S, A, B}
    private Set<String> terminals; // VT = {a, b, c, d}
    private Map<String, List<String>> ruleProductions; // P
    private String startSymbol; // S

    public Grammar() { // defining the rules for the language
        this.nonTerminals = new HashSet<>(Arrays.asList("S", "A", "B"));
        this.terminals = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        this.ruleProductions = new HashMap<>();
        this.startSymbol = "S";
        initializeRules();
    }

    private void initializeRules() {
        ruleProductions.put("S", Arrays.asList("bS", "dA"));
        ruleProductions.put("A", Arrays.asList("aA", "dB", "b"));
        ruleProductions.put("B", Arrays.asList("cB", "a"));
    }


    public String generateString() { // using a loop and random picker to create random words
        String currentString = startSymbol;
        Random random = new Random();

        while(containsNonTerminal(currentString)) { // as long as string doesn't contain a terminal...
            String lastChar = String.valueOf(currentString.charAt(currentString.length()-1)); // identify the non-terminal in the string
            List<String> rules = ruleProductions.get(lastChar); // see the list of all possible rules for that character
            String option = rules.get(random.nextInt(rules.size())); // choose one randomly
            currentString = currentString.substring(0, currentString.length()-1) + option; // replace the non-terminal with chosen rule
            // for the line above: we remove the very last character, we take a rule from the map and attach to the cut string. it works cause it's RHG (right hand grammar)
        }
        return currentString;
    }


    private boolean containsNonTerminal(String s) { // helper method to see if a string contains non-terminals
        for (char c: s.toCharArray()) {
            if (nonTerminals.contains(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }

    public FiniteAutomaton toFiniteAutomaton() { // translating the rules above so that the finite automaton can read them (into delta...)
        Map<String, Map<Character, String>> delta = new HashMap<>();
        String finalState = "q_f";

        Set<String> Q = new HashSet<>(nonTerminals); // defining the full set of states Q (non-terminals + the final state)
        Q.add(finalState);

        for (String nt : nonTerminals) {  // initializing the inner maps for each state
            delta.put(nt, new HashMap<>());
        }
        delta.put(finalState, new HashMap<>());

        // looping through grammar rules to fill delta
        for (String leftSide : ruleProductions.keySet()) {
            for (String rightSide : ruleProductions.get(leftSide)) {
                char terminal = rightSide.charAt(0);

                if (rightSide.length() > 1) {
                    // if X -> terminal + non-terminal
                    String destination = rightSide.substring(1);
                    delta.get(leftSide).put(terminal, destination);
                } else {
                    // if X -> terminal only
                    delta.get(leftSide).put(terminal, finalState);
                }
            }
        }

        Set<String> F = new HashSet<>();  // set of final states F
        F.add(finalState);

        // Convert Set<String> terminals to Set<Character> sigma
        Set<Character> sigma = new HashSet<>();
        for (String t : terminals) {
            sigma.add(t.charAt(0));
        }

        // return new object using constructor
        return new FiniteAutomaton(Q, terminals, delta, startSymbol, F);    }
}

