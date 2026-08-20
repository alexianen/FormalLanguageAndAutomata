import java.util.List;

//import static TokenType.*;

/**
 * Recursive-descent parser that builds the AST for the mini-language.
 *
 * Grammar:
 *   program       -> statement* EOF
 *   statement     -> IDENTIFIER '=' expression ';'
 *                  | 'print' '(' expression ')' ';'
 *                  | expression ';'
 *   expression    -> term (('+'|'-') term)*
 *   term          -> factor (('*'|'/') factor)*
 *   factor        -> NUMBER | IDENTIFIER | '(' expression ')'
 */
public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token consume() {
        return tokens.get(pos++);
    }

    private boolean match(TokenType type) {
        if (peek().type == type) {
            consume();
            return true;
        }
        return false;
    }

    private Token expect(TokenType type, String message) {
        if (peek().type == type) {
            return consume();
        }
        throw new RuntimeException("Parse error at pos " + peek().position + ": " + message + " -- got " + peek());
    }

    public AST.Program parseProgram() {
        AST.Program program = new AST.Program();
        while (peek().type != TokenType.EOF) {
            program.statements.add(parseStatement());
        }
        return program;
    }

    private AST.Statement parseStatement() {
        Token t = peek();
        if (t.type == TokenType.IDENTIFIER) {
            // Could be assignment or expression statement starting with variable
            Token next = tokens.get(pos + 1);
            if (next.type == TokenType.ASSIGN) {
                // assignment
                String name = consume().lexeme;
                consume(); // =
                AST.Expression expr = parseExpression();
                expect(TokenType.SEMICOLON, "expected ';' after assignment");
                return new AST.Assignment(name, expr);
            }
        }

        if (t.type == TokenType.PRINT) {
            consume(); // 'print'
            expect(TokenType.LPAREN, "expected '(' after print");
            AST.Expression expr = parseExpression();
            expect(TokenType.RPAREN, "expected ')' after print expression");
            expect(TokenType.SEMICOLON, "expected ';' after print");
            return new AST.Print(expr);
        }

        // fallback: expression statement
        AST.Expression expr = parseExpression();
        expect(TokenType.SEMICOLON, "expected ';' after expression");
        return new AST.ExprStatement(expr);
    }

    private AST.Expression parseExpression() {
        AST.Expression left = parseTerm();
        while (peek().type == TokenType.PLUS || peek().type == TokenType.MINUS) {
            String op = consume().lexeme;
            AST.Expression right = parseTerm();
            left = new AST.Binary(op, left, right);
        }
        return left;
    }

    private AST.Expression parseTerm() {
        AST.Expression left = parseFactor();
        while (peek().type == TokenType.MUL || peek().type == TokenType.DIV) {
            String op = consume().lexeme;
            AST.Expression right = parseFactor();
            left = new AST.Binary(op, left, right);
        }
        return left;
    }

    private AST.Expression parseFactor() {
        Token t = peek();
        if (t.type == TokenType.NUMBER) {
            consume();
            double val = Double.parseDouble(t.lexeme);
            return new AST.NumberLiteral(val);
        } else if (t.type == TokenType.IDENTIFIER) {
            consume();
            return new AST.Var(t.lexeme);
        } else if (t.type == TokenType.LPAREN) {
            consume();
            AST.Expression e = parseExpression();
            expect(TokenType.RPAREN, "expected ')'");
            return e;
        } else {
            throw new RuntimeException("Unexpected token in factor at pos " + t.position + ": " + t);
        }
    }
}