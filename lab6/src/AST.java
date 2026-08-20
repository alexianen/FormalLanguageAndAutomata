import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AST node classes. A compact grouping with pretty-print helpers.
 */
public abstract class AST {
    // Base
    public interface Node {
        String pretty(int indent);
        default String indentStr(int indent) {
            return "  ".repeat(Math.max(0, indent));
        }
    }

    // Program: a list of statements
    public static class Program implements Node {
        public final List<Statement> statements = new ArrayList<>();

        @Override
        public String pretty(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indentStr(indent)).append("Program\n");
            for (Statement s : statements) {
                sb.append(s.pretty(indent + 1));
            }
            return sb.toString();
        }
    }

    // Statements
    public static abstract class Statement implements Node {}

    public static class Assignment extends Statement {
        public final String name;
        public final Expression expr;

        public Assignment(String name, Expression expr) {
            this.name = name;
            this.expr = expr;
        }

        @Override
        public String pretty(int indent) {
            return indentStr(indent) + "Assignment(name=" + name + ")\n" + expr.pretty(indent + 1);
        }
    }

    public static class Print extends Statement {
        public final Expression expr;

        public Print(Expression expr) {
            this.expr = expr;
        }

        @Override
        public String pretty(int indent) {
            return indentStr(indent) + "Print\n" + expr.pretty(indent + 1);
        }
    }

    public static class ExprStatement extends Statement {
        public final Expression expr;

        public ExprStatement(Expression expr) {
            this.expr = expr;
        }

        @Override
        public String pretty(int indent) {
            return indentStr(indent) + "ExprStatement\n" + expr.pretty(indent + 1);
        }
    }

    // Expressions
    public static abstract class Expression implements Node {}

    public static class Binary extends Expression {
        public final String op;
        public final Expression left;
        public final Expression right;

        public Binary(String op, Expression left, Expression right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public String pretty(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indentStr(indent)).append("Binary(op=").append(op).append(")\n");
            sb.append(left.pretty(indent + 1));
            sb.append(right.pretty(indent + 1));
            return sb.toString();
        }
    }

    public static class NumberLiteral extends Expression {
        public final double value;

        public NumberLiteral(double value) {
            this.value = value;
        }

        @Override
        public String pretty(int indent) {
            return indentStr(indent) + "Number(" + value + ")\n";
        }
    }

    public static class Var extends Expression {
        public final String name;

        public Var(String name) {
            this.name = name;
        }

        @Override
        public String pretty(int indent) {
            return indentStr(indent) + "Var(" + name + ")\n";
        }
    }
}