# Lexer Lab (Java)

This lab implements a simple lexer (tokenizer) in Java that recognizes:
- integers and floating-point numbers (including leading-dot `.5` and scientific notation),
- identifiers and function names (sin, cos),
- arithmetic operators: + - * / ^,
- parentheses and commas,
- line comments starting with `//`.

Build & test:
- Requirements: Java 17+, Maven
- Compile & test: mvn test
- Run the demo Main: mvn -Dexec.mainClass=com.example.lexer.Main exec:java