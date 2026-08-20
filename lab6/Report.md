# DSL Lab 6 - Parser & Building an Abstract Syntax Tree

### Course: Formal Languages & Finite Automata
### Author: Mitrofan Alexia

---

## 1. Theory

Parsing is the process of analysing a sequence of tokens (produced by lexical analysis) to determine its grammatical structure with respect to a formal grammar. A parse tree (concrete syntax tree) represents the exact syntactic structure including all tokens; an Abstract Syntax Tree (AST) is a simplified, hierarchical representation that preserves the syntactic structure needed for semantic analysis or subsequent compilation phases while omitting punctuation and syntactic sugar.

Lexical analysis (lexing/tokenization) groups characters into tokens such as identifiers, numbers, operators, and punctuation. Regular expressions are a common and practical technique to describe token patterns for a lexer.

A recursive-descent parser is a top-down parser made of mutually recursive procedures where each procedure implements one production of the grammar. Operator precedence is typically handled by separating grammar levels (e.g., expression → term ((+|-) term)*, term → factor ((*|/) factor)*).

ASTs are useful because they make later analyses (type checking, optimization, code generation, interpretation) simpler: nodes represent meaningful constructs (assignments, binary operators, literals, variables, function calls, etc.)

---

## 2. Objectives

1. Provide a TokenType enumeration to categorize tokens; use regular expressions to identify token types.
2. Implement AST data structures suitable for the processed language.
3. Implement a simple parser that builds an AST from the token stream.

---

## 3. Implementation

Language supported:
- Assignments: `identifier = expression;`
- Print statements: `print(expression);`
- Expressions: `+`, `-`, `*`, `/`, numbers, variables, and parentheses
- Statement termination: `;`
- Comments: `//` single-line
- Whitespace is ignored

Project organization (place all files in package `parser`):
- TokenType.java — enum of token types with regex patterns
- Token.java — token container (type, lexeme, position)
- Lexer.java — regex-based lexer that scans the input and emits Token objects
- AST.java — AST node classes (Program, Statement subclasses, Expression subclasses)
- Parser.java — recursive-descent parser that builds the AST
- Interpreter.java — walks the AST and evaluates statements and expressions
- Main.java — separate main class to run lexing, parsing, print AST and interpret

Key details

- TokenType
    - Enum constants include PRINT, IDENTIFIER, NUMBER, PLUS, MINUS, MUL, DIV, LPAREN, RPAREN, ASSIGN, SEMICOLON, WHITESPACE, COMMENT, EOF.
    - Each (non-EOF) token type holds a regex Pattern used by the lexer. Example:
        - PRINT: `^print\b`
        - IDENTIFIER: `^[A-Za-z_][A-Za-z0-9_]*`
        - NUMBER: `^\d+(?:\.\d+)?`
        - Operators and punctuation: `^\+`, `^\-`, `^\*`, `^/`, `^\(`, `^\)`, `^=`, `^;`
    - WHITESPACE and COMMENT patterns are matched and skipped (not emitted).

- Lexer
    - Scans the input from left to right. At each position it tries token patterns in a fixed order so keywords (PRINT) are matched before IDENTIFIER.
    - When a match is found at the current position the matched lexeme is consumed and (unless whitespace/comment) a Token is appended to the token list.
    - When no token matches at the current position the lexer throws a runtime error indicating an unexpected character.
    - After input is exhausted an EOF token is appended.

- Parser (recursive-descent)
    - Grammar implemented:
        - program -> statement* EOF
        - statement -> IDENTIFIER '=' expression ';'
          | 'print' '(' expression ')' ';'
          | expression ';'
        - expression -> term (('+'|'-') term)*
        - term -> factor (('*'|'/') factor)*
        - factor -> NUMBER | IDENTIFIER | '(' expression ')'
    - Methods:
        - parseProgram, parseStatement, parseExpression, parseTerm, parseFactor
    - Operator precedence is achieved by expression → term ((+|-) term)* and term → factor ((*|/) factor)*.
    - Error handling uses RuntimeException with position info; can be enhanced to a dedicated ParseException.

- AST
    - Program (list of statements)
    - Statements: Assignment(name, expr), Print(expr), ExprStatement(expr)
    - Expressions: Binary(op, left, right), NumberLiteral(value), Var(name)
    - Each node implements a simple pretty( int indent ) that prints a readable tree.

- Interpreter
    - Evaluates numeric expressions, supports assignment and print.
    - Environment is a Map<String, Double>.
    - Undefined variables cause runtime errors.

- Main (usage)
    - If run with a filename argument: reads the file as source.
    - Otherwise uses a built-in sample program:
      ```text
      // Sample program
      x = 2 + 3 * (4 - 1);
      y = x * 2;
      print(x);
      print(y);
      ```
    - Workflow: Lexing → print tokens, Parsing → print AST, Interpretation → print outputs from print statements.

---

## 4. Conclusion

This lab successfully implemented a small end-to-end front-end for a mini-language: a regex-based lexer, a recursive-descent parser that constructs an Abstract Syntax Tree (AST), and an interpreter that evaluates the AST. The solution demonstrates how lexical patterns map to tokens, how grammar productions are implemented as parser routines, and how the AST provides a compact, semantically useful representation of program structure. The parser correctly enforces operator precedence and grouping through the expression/term/factor breakdown, and the interpreter shows how a symbol environment can execute assignments and print statements.

Key takeaways:
- Regular expressions are effective for token classification in small languages; ordering (keywords before identifiers) matters.
- Recursive-descent parsing is easy to implement and understand for LL(1)-style grammars and gives straightforward control over precedence.
- An AST decouples syntactic details from semantics, making later analyses and transformations simpler.
- Robust tooling improvements (better error messages, line/column info, unit tests) significantly increase usability and maintainability.
