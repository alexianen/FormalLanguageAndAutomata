# DSL Lab 2 — Determinism in Finite Automata. Conversion from NDFA 2 DFA. Chomsky Hierarchy.

### Course: Formal Languages & Finite Automata  
### Author: Alexia Mitrofan (V16)

---

## Theory

A finite automaton (FA) is a 5-tuple M = (Q, Σ, δ, q0, F) used to recognize regular languages.  
- Deterministic FA (DFA): for every state and input symbol there is at most one next state.  
- Non-deterministic FA (NDFA): a state and symbol may map to multiple next states.  
Every NDFA has an equivalent DFA found by the subset construction algorithm.  
Right-linear (or right-regular) grammars are Type-3 in the Chomsky hierarchy and describe exactly the regular languages recognizable by finite automata.

---

## Objectives

- Implement a Finite Automaton in Java and detect determinism.
- Convert an FA to a right-linear regular grammar and classify it in the Chomsky hierarchy.
- Convert an NDFA to an equivalent DFA using subset construction.

---

## Given variant

- Q = {q0, q1, q2, q3}  
- Σ = {a, b}  
- F = {q3}  
- δ:
  - δ(q0, a) = q1
  - δ(q0, b) = q0
  - δ(q1, b) = q1 and q2  (non-deterministic)
  - δ(q2, a) = q2
  - δ(q2, b) = q3

---

## Implementation description

Main classes implemented:

- `FiniteAutomaton.java`  
  - Models states, alphabet, start state, final states and transitions.  
  - Supports nondeterministic transitions: (state, symbol) -> Set<state>.  
  - Key methods:
    - `isDeterministic()` — checks whether any (state, symbol) maps to >1 next states.
    - `toRegularGrammar()` — converts the FA to a right-linear grammar.

- `RegularGrammar.java`  
  - Stores productions (nonterminal -> RHS alternatives) and the start symbol.  
  - `classify(nonterminals, terminals)` — simple structural classification (Type-3/Type-2/Type-0).

- `NDFAtoDFAConverter.java`  
  - Subset construction (no ε-closure support; assumes no ε-transitions).  
  - Represents DFA states as sorted-set string keys like `{q0,q1}`.

- `Main.java`  
  - Builds the variant FA, prints the FA, checks determinism, converts to grammar, classifies it, converts to DFA and prints DFA.

Key code snippets:

- Determinism check (excerpt):
```java
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
```

- FA -> right-linear grammar (excerpt):
```java
for (String p : states) {
    Map<Character, Set<String>> map = transitions.getOrDefault(p, Collections.emptyMap());
    for (Map.Entry<Character, Set<String>> e : map.entrySet()) {
        char a = e.getKey();
        for (String q : e.getValue()) {
            g.addProduction(p, String.valueOf(a) + q); // p -> a q
            if (finalStates.contains(q)) {
                g.addProduction(p, String.valueOf(a)); // p -> a
            }
        }
    }
}
if (finalStates.contains(startState)) g.addProduction(startState, ""); // epsilon
```

- Subset construction (concept): each DFA state = set of NDFA states; transitions computed by union of δ over members of the set; DFA-final if set contains any NDFA-final.

---

## Program output

```
=== Given Finite Automaton ===
States: [q0, q1, q2, q3]
Alphabet: [a, b]
Start: q0
Finals: [q3]
Transitions:
q0 --a--> [q1]
q0 --b--> [q0]
q1 --b--> [q1, q2]
q2 --a--> [q2]
q2 --b--> [q3]

Deterministic? false

=== Regular Grammar (right-linear) ===
q0 -> aq1 | bq0
q1 -> bq1 | bq2
q2 -> aq2 | bq3 | b

Grammar classification: Type-3 (regular)

=== Converting NDFA -> DFA (subset construction) ===
DFA States: [{q0}, {q1}, {q1,q2}, {q2}, {q1,q2,q3}, {q3}]
DFA Start: {q0}
DFA Finals: [{q1,q2,q3}, {q3}]
DFA Transitions:
{q0} --a--> [{q1}]
{q0} --b--> [{q0}]
{q1} --b--> [{q1,q2}]
{q1,q2} --a--> [{q2}]
{q1,q2} --b--> [{q1,q2,q3}]
{q2} --a--> [{q2}]
{q2} --b--> [{q3}]
{q1,q2,q3} --a--> [{q2}]
{q1,q2,q3} --b--> [{q1,q2,q3}]
```

- The FA is nondeterministic because `q1` has two `b` transitions (`q1` and `q2`).
- The grammar is right-linear (Type-3). The production `q2 -> b` appears because `q2 --b--> q3` and `q3` is final: we add `q2 -> b`.
- The subset construction yields the deterministic automaton with reachable states `{q0}`, `{q1}`, `{q1,q2}`, `{q2}`, `{q1,q2,q3}`, `{q3}`. Any DFA state containing `q3` is accepting.

---


## Conclusions

- The given FA is nondeterministic due to the `q1 --b--> q1` and `q1 --b--> q2` branching.
- The converted grammar is right-linear and thus Type-3 (regular).
- Subset construction produced a correct DFA whose accepting states are any sets containing `q3`.
<a href="https://imgbb.com/"><img src="https://i.ibb.co/8DgnvddZ/Screenshot-4542.png" alt="Screenshot-4542" border="0" /></a>
<a href="https://imgbb.com/"><img src="https://i.ibb.co/FbNYxvVG/Screenshot-4543.png" alt="Screenshot-4543" border="0" /></a>

