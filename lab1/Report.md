# DSL Lab 1 - Intro to formal languages.

### Course: Formal Languages & Finite Automata
### Author: Alexia Mitrofan (V16)

----

## Theory
In this laboratory, I have implemented a Right-Linear Grammar, which is a Regular Grammar,
and it corresponds to Type 3 in the Chomsky Hierarchy. It is also the only type of grammar
which can be represented by a Finite Automaton, since we only move forward, thus 
there is no need for a memory.

## Objectives

* Creating and understanding what is a formal language and grammar;
* Generating random strings which belong to our language;
* Demonstrating that Finite Automaton can be used for Type 3 Grammar.

## Implementation description

* Grammar Class (Grammar.java) - it is a Right-Linear Grammar. It consists of the following sets:

Non-Terminals (Vn): {S, A, B};

Terminals (Vt): {a, b, c, d};

Production Rules (P): {S→bS∣dA, A→aA∣dB∣b, B→cB∣a}.

String Generation Logic: The generateString() method starts with the startSymbol and
iteratively replaces the right-most non-terminal by randomly selecting a production rule until 
the string consists only of terminals.
```
 public String generateString() {
        String currentString = startSymbol;
        Random random = new Random();

        while(containsNonTerminal(currentString)) { 
            String lastChar = String.valueOf(currentString.charAt(currentString.length()-1)); 
            List<String> rules = ruleProductions.get(lastChar); 
            String option = rules.get(random.nextInt(rules.size()));
            currentString = currentString.substring(0, currentString.length()-1) + option; 
        }
        return currentString;
    }
```

* Finite Automaton Class (FiniteAutomaton.java) -  abstract machine used to recognize patterns
in input sequences, forming the basis for understanding regular languages in computer science.

The FA is represented as a 5-tuple M=(Q,Σ,δ,q0,F).

States (Q): Derived from the Grammar's non-terminals plus a terminal state q_f.

Transitions (δ): Implemented using a nested Map<String, Map<Character, String>> for O(1) state lookups.
```
    public FiniteAutomaton(Set<String> Q, Set<String> sigma,
                           Map<String, Map<Character, String>> delta,
                           String q0, Set<String> F) {
        this.Q = Q;
        this.sigma = sigma;
        this.delta = delta;
        this.q0 = q0;
        this.F = F;
    }
```
Acceptance: A string is accepted only if the path through the transition table 
ends in a state included in the Final States set (F).

```
public boolean stringBelongToLanguage(String input) {
        String currentState = q0;

        for (char a : input.toCharArray()) {
            if (delta.containsKey(currentState) && delta.get(currentState).containsKey(a)) { 
                currentState = delta.get(currentState).get(a);
            }
            else {
                return false; // there is no transition defined
            }
        }
        return F.contains(currentState);
    }
```


## Conclusions / Screenshots / Results

<a href="https://ibb.co/676yNwfK"><img src="https://i.ibb.co/676yNwfK/Screenshot-3728.png" alt="Screenshot-3728" border="0" /></a>