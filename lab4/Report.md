# Laboratory Report — Regular expressions.

### Course: Formal Languages & Finite Automata
### Author: Mitrofan Alexia

---

## 1. Theory

Lexical and syntactic descriptions of languages often use regular expressions (regex). Regular expressions describe regular languages and can be compiled into automata (NFA/DFA) that accept exactly the strings the regex describes. Beyond recognition, a useful practical task is the inverse: given a regex, generate valid strings that belong to the language it describes. Generation is helpful for testing, examples, fuzzing, and teaching.

To do this reliably we parse the regular expression into an abstract syntax tree (AST) that represents concatenation, alternation, repetition, and grouping; then we traverse the AST to produce strings satisfying the grammar. Care is required for unbounded operators (`*`, `+`) to avoid infinite/very large output; therefore we bound repetitions during generation.

---

## 2. Objectives

- Implement a small regex dialect parser (recursive-descent) that builds an AST at runtime rather than hardcoding specific regexes.
- Implement a generator that walks the AST and produces valid strings for a given regex.
- Support the dialect features required by the assignment:
    - literals (single-character tokens)
    - concatenation (implicit)
    - alternation: `|`
    - grouping: `( )`
    - postfix operators: `*` (Kleene star), `+` (one-or-more), `?` (optional)
    - exact repetition: `^n` (e.g., `(3|4)^5` -> repeat chosen group exactly 5 times)
- Bound `*` and `+` repetitions to a configurable maximum (default 5) to avoid unbounded generation.
- Provide a main program (entry point) suitable for IntelliJ that demonstrates generation for the three Variant 1 regexes.
- Bonus: provide tracing/log output for both parsing (showing parse actions and AST construction) and generation (step-by-step string construction).

---

## 3. Implementation description

### Project layout (single-file or small IntelliJ project)

- `src/main/java/com/example/regex/RegexGenerator.java` — primary file with:
    - `Parser` (recursive-descent) that turns a regex string into an AST and produces a parse trace.
    - AST node classes: `LiteralNode`, `ConcatNode`, `AltNode`, `StarNode`, `PlusNode`, `QuestionNode`, `ExactRepeatNode`, `EmptyNode`.
    - Generator methods that walk the AST and produce strings, with a configurable `maxRepeat` limit and generation trace.
    - Utility methods:
        - `public static Node parseRegexWithTrace(String regex, List<String> parseTrace)`
        - `public static List<String> generateSamples(Node ast, int samples, int maxRepeat, long seed, List<String> outTraceForFirst)`
    - `main(String[] args)` — demonstration entrypoint (optional separate `Main.java` can call the public helpers).

- `src/main/java/com/example/regex/Main.java` (optional) — small launcher that calls `RegexGenerator` helpers and prints parse/generation traces and example outputs.

### Dialect and key behaviors

- Single-character literals only (e.g., `'U'` and `'V'` are separate characters; `"UV"` is parsed as concatenation).
- Alternation `|` and implicit concatenation.
- Grouping with `(` and `)`.
- Postfix operators:
    - `*` → 0..`maxRepeat` repetitions
    - `+` → 1..`maxRepeat` repetitions
    - `?` → 0 or 1
- Exact repetition: `^n` immediately following a primary, e.g. `(3|4)^5`.
- Parser:
    - Recursive-descent with methods for union (alternation), concatenation, repetition and primary.
    - Produces a parse trace (list of human-readable actions) useful for debugging and step-by-step explanation.
- Generator:
    - Randomized sampling of the language (seedable RNG for reproducible runs).
    - Alternation choices and repetition counts are randomly chosen (within bounds).
    - Duplicate results suppressed when generating a sample set.
    - Detailed generation trace for the first sample produced.
- Error handling:
    - Parser throws `RuntimeException` for malformed syntax (unclosed parentheses, missing number after `^`, unexpected character).
    - Generator assumes AST is valid.

---

## 4. Conclusion

This project implements a compact, extendable runtime regex parser and 
generator in Java suitable for IntelliJ. It dynamically parses regex strings
into an AST and generates strings honoring alternation, concatenation, grouping,
repetition, and exact repetition (caret-notation `^n`). Repetitions for `*` and `+` 
are bounded (default 5) to meet the assignment rule and keep outputs manageable.
The implementation includes parsing and generation tracing to illustrate the 
processing steps — useful both for debugging and pedagogy.
<a href="https://imgbb.com/"><img src="https://i.ibb.co/7tcXyB6n/Screenshot-4548.png" alt="Screenshot-4548" border="0" /></a>
<a href="https://imgbb.com/"><img src="https://i.ibb.co/4wH6HRFB/Screenshot-4549.png" alt="Screenshot-4549" border="0" /></a>
<a href="https://imgbb.com/"><img src="https://i.ibb.co/21TnYKLF/Screenshot-4550.png" alt="Screenshot-4550" border="0" /></a>

