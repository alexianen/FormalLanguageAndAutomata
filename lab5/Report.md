# DSL Lab 5 - Converting a Context-Free Grammar to Chomsky Normal Form (CNF)

### Course: Formal Languages & Finite Automata
### Author: Mitrofan Alexia

---

## 1. Theory

Chomsky Normal Form (CNF) is a standard form for context-free grammars (CFGs) that simplifies many theoretical results and algorithms (e.g., the CYK parsing algorithm). A CFG is in CNF if every production is of one of these forms:

- A → BC  (where A, B, C are nonterminal symbols and B, C are not the start symbol)
- A → a   (where a is a terminal symbol)
- S → ε   (only allowed if the start symbol S can derive the empty string)

Important properties and steps related to converting an arbitrary CFG to CNF:

1. New Start Symbol: If the original start symbol appears on the right-hand side (RHS) of any production or the grammar can produce ε, create a new start symbol S0 with S0 → S. This prevents accidental loss of the start symbol during elimination of ε-productions.

2. Eliminate ε-productions (nullable symbols): Compute the set of nullable nonterminals (symbols that can derive ε). For each production, generate variants where nullable symbols in the RHS are optionally removed — but avoid creating ε on RHS unless it is for the start symbol.

3. Eliminate unit (renaming) productions: Remove productions of the form A → B where both sides are nonterminals by computing reachability via unit edges and copying non-unit productions accordingly.

4. Remove useless symbols:
    - Non-productive (cannot derive any terminal string) — identify productive nonterminals and remove productions involving non-productive ones.
    - Inaccessible (unreachable from the start symbol) — remove symbols and productions unreachable from the start.

5. Replace terminals in long RHS: In productions with RHS length ≥ 2, replace terminals with fresh preterminal nonterminals (e.g., T_a → a), so binary productions contain only nonterminals.

6. Binarize long productions: Break RHS of length > 2 into binary productions using fresh nonterminals so each resulting production has length 2.

These transformations preserve the language (modulo the handling of ε) and result in a grammar where every production is either A → BC or A → a (with the possible exception S → ε for the start symbol).

---

## 2. Objectives


The task objectives were:

1. Learn about Chomsky Normal Form (CNF).
2. Get familiar with common normalization approaches for grammars.
3. Implement a method that normalizes an input grammar to CNF, with:
    - The functionality encapsulated in an appropriately-signed method or type.
    - Execution and testing of the implementation.
    - (Bonus) Acceptance of arbitrary grammars (not only a single variant).

Additional practical constraints: use Java and IntelliJ for implementation and testing.

---

## 3. Implementation Description

I implemented a generic CNF converter in Java. The implementation is intentionally simple and readable, suitable for coursework and extension. Below is a description of the main components and the transformation pipeline.

Files delivered
- `CNFConverter.java` — the converter class and supporting data types (Production, Grammar).
- `Main.java` — a small runner that builds the provided "Variant 16" grammar, runs the converter, prints the original and CNF grammar and performs a basic validity check.

Key types and methods
- class CNFConverter
    - inner static class `Production`
        - Represents a production with `lhs` (String) and `rhs` (List<String>).
        - Epsilon is represented as an empty `rhs` list.
    - inner static class `Grammar`
        - Holds `nonTerminals: Set<String>`, `terminals: Set<String>`, `startSymbol: String`, and `productions: Set<Production>`.
    - `Grammar toCNF(Grammar g)`
        - Public entry point which returns a copy of the grammar converted to CNF.
        - Steps performed (in order):
            1. Add a new start symbol S0 → S if needed.
            2. removeEpsilonProductions(grammar) — compute nullable nonterminals and expand productions to remove ε occurrences (allowing ε only for start when necessary).
            3. removeUnitProductions(grammar) — compute unit-reachability and inline non-unit productions.
            4. removeNonProductive(grammar) — remove nonterminals that cannot derive terminal strings and productions using them.
            5. removeInaccessible(grammar) — remove symbols unreachable from start symbol.
            6. replaceTerminalsInLongProductions(grammar) — introduce preterminal nonterminals `T_x` for terminals that occur in RHS with length ≥ 2, adding productions like `T_a -> a`.
            7. binarizeProductions(grammar) — break RHS with length > 2 into binary productions using fresh nonterminals named `X_0`, `X_1`, …
            8. rebuildSymbolSets(grammar) — re-evaluate terminal and nonterminal sets from productions.

Design decisions and notes
- Fresh names:
    - Preterminals: `T_<terminal>` (e.g., `T_a`).
    - Fresh nonterminals from binarization: `X_<n>` (e.g., `X_0`).
    - If the original start must be wrapped, the code uses `<Start>_S0` as the new start.
- Representation:
    - Both terminals and nonterminals are Strings. The converter tracks sets separately.
    - The converter is generic: it takes any grammar represented as a `Grammar` instance.
- Complexity:
    - The ε-elimination step can generate up to 2^k variants of a production if k RHS symbols are nullable — this is a known combinatorial explosion for naive implementations.
- Heuristics:
    - rebuildSymbolSets uses simple heuristics to infer whether a token on RHS is terminal or nonterminal when needed. In practice, since the Grammar object is constructed with explicit terminal/nonterminal sets, the heuristics are rarely needed.

---

## 4, Conclusion

A generic CNF converter in Java has been implemented that follows the standard conversion pipeline:
- add a new start if needed, eliminate ε-productions, remove unit productions, remove useless symbols, replace terminals in long RHSs with preterminals, and binarize long RHSs.
  The implementation is packaged in `CNFConverter` with a clear `toCNF(Grammar g)` method, and a 
- `Main` runner builds the provided Variant 16 grammar, converts it and prints results. 
- The converter accepts arbitrary grammars represented as `Grammar` objects (bonus task).

<a href="https://imgbb.com/"><img src="https://i.ibb.co/K3xdmbY/Screenshot-4551.png" alt="Screenshot-4551" border="0" /></a>
<a href="https://ibb.co/ZzFspThX"><img src="https://i.ibb.co/nMY2NkDm/Screenshot-4552.png" alt="Screenshot-4552" border="0" /></a>
<a href="https://imgbb.com/"><img src="https://i.ibb.co/rRqT579M/Screenshot-4553.png" alt="Screenshot-4553" border="0" /></a>



  