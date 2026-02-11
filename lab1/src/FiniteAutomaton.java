import java.util.*;

public class FiniteAutomaton
{
    private Set<String> Q; // state names
    private Set<String> sigma; // alphabet
    private Map<String, Map<Character, String>> delta; // transitions (key1: current state, key2: character read, value: destination state)
    private String q0;// start state
    private Set<String> F; // final state

    public FiniteAutomaton(Set<String> Q, Set<String> sigma,
                           Map<String, Map<Character, String>> delta,
                           String q0, Set<String> F) {
        this.Q = Q;
        this.sigma = sigma;
        this.delta = delta;
        this.q0 = q0;
        this.F = F;
    }

    public boolean stringBelongToLanguage(String input) { // check if the string belongs to our language
        String currentState = q0;

        for (char a : input.toCharArray()) {
            // transition: delta(currentState, a).
            if (delta.containsKey(currentState) && delta.get(currentState).containsKey(a)) { // takes first character, checks if rule exists, if yes, moves on
                currentState = delta.get(currentState).get(a);
            }
            else {
                return false; // there is no transition defined
            }
        }
        return F.contains(currentState); // checking if the state we ended in is the final state
    }
}