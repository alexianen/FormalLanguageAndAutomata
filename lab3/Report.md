# Laboratory Report — Lexer & Scanner

### Course: Formal Languages & Finite Automata
### Author: Mitrofan Alexia

---

## 1. Theory
Lexical analysis (lexing or scanning) is the process of converting a raw input 
character stream into a sequence of tokens. Tokens are categorized lexemes: each 
token carries a type (for example NUMBER, IDENTIFIER, PLUS) and optionally
metadata (literal value, position). The lexer is the front end for parsers and 
compilers—its responsibilities are recognizing valid tokens, skipping irrelevant
input (whitespace, comments), and reporting malformed input early.

---
## 2. Objectives
- Present a concise implementation of a lexer for a small expression language.
- Support numeric literals (integers, decimals, leading-dot numbers, scientific notation), identifiers, function names (sin, cos), arithmetic operators (+, -, *, /, ^), parentheses, and commas.
- Skip whitespace and single-line comments beginning with `//`.
- Produce tokens with lexeme text, parsed literal for numbers, and character-position information.
- Provide a simple demo and unit tests to validate behavior, and document design choices.

---

## 3. Implementation description
Project layout (standard Maven):
- src/main/java/com/example/lexer
    - TokenType.java — enum of token types (NUMBER, IDENTIFIER, FUNCTION, PLUS, MINUS, STAR, SLASH, CARET, LPAREN, RPAREN, COMMA, EOF, INVALID)
    - Token.java — immutable token object holding type, lexeme, optional literal (Double for numbers), and start position (0-based index).
    - Lexer.java — hand-written, single-pass character scanner that returns a List<Token>.
    - Main.java — small demo printing token streams for sample inputs.
- src/test/java/com/example/lexer
    - LexerTest.java — JUnit tests that check tokenization of typical inputs and edge cases.

Key scanning rules and behavior:
- One-pass scanner: advance char-by-char, using peek/peekNext helpers to decide token boundaries.
- Numbers:
    - Accepts integers (e.g., `42`), decimals (`3.1415`), leading-dot decimals (`.25`), and scientific notation (`2.0e-3`).
    - Parsing approach: consume integer part if present, optionally consume '.' and fractional digits, then optional exponent part `e/E[+/-]?digits`.
    - Parsed numeric lexemes are converted to Double and stored in Token.literal.
- Identifiers and functions:
    - Identifiers begin with a letter or underscore, followed by letters/digits/underscores.
    - `sin` and `cos` are recognized as FUNCTION tokens (easy to extend to more names via a set).
- Operators and punctuation:
    - Single-character tokens (+, -, *, /, ^, (, ), ,) produce corresponding TokenType entries.
- Whitespace and comments:
    - Whitespace characters are skipped.
    - Single-line comments beginning with `//` are skipped until the line end.
- Error handling:
    - Unknown/illegal single characters are emitted as TokenType.INVALID with the lexeme being the offending character(s).
    - The handling of malformed numeric sequences is a design choice. The current implementation tokenizes `1..2` as NUMBER('1'), INVALID('.'), NUMBER('.2'). This choice is intentional and documented; alternatives include emitting DOT tokens or collapsing a malformed numeric run into a single INVALID token.
- Token positions:
    - Tokens record the start position as the 0-based index where the lexeme begins. An EOF token with the input length is appended.

Examples (behavior illustrated by the demo):
- "sin(3.1415) + cos(0) - 2.5 * x ^ 2" → FUNCTION('sin')@0, LPAREN('(')@3, NUMBER('3.1415',3.1415)@4, ...
- "42 + 0.5 - .25 // comment" → NUMBER('42')@0, PLUS('+')@3, NUMBER('0.5')@5, NUMBER('.25')@11, EOF at end-of-input.
- "1..2" → NUMBER('1')@0, INVALID('.')@1, NUMBER('.2')@2 (current design choice; other behaviors are possible).

How to run:
- Build & test: mvn test
- Run demo: mvn -Dexec.mainClass=com.example.lexer.Main exec:java
---

## 4. Conclusion
This lab provides a compact, extendable lexer demonstrating essential principles
of lexical analysis: token recognition, literal capture, skipping irrelevant 
input, and basic error reporting. The implementation is intentionally 
straightforward to serve as a foundation for a parser or evaluator. Future 
improvements could include a richer function table, multi-line comments, 
string literals, more precise error messages (line/column), or changing 
malformed-number handling (e.g., introduce DOT tokens or consolidate malformed
lexemes into single INVALID tokens).

<a href='https://postimages.org/' target='_blank'><img src='https://i.postimg.cc/2SRNsfs7/Screenshot-(4544).png' border='0' alt='Screenshot-(4544)'></a>
<a href='https://postimages.org/' target='_blank'><img src='https://i.postimg.cc/HncFNSBc/Screenshot-(4545).png' border='0' alt='Screenshot-(4545)'></a>