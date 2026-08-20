import java.util.HashMap;
import java.util.Map;

/**
 * Small interpreter to show how the AST can be walked.
 * It evaluates numeric expressions and supports assignment and print.
 */
public class Interpreter {
    private final Map<String, Double> env = new HashMap<>();

    public void run(AST.Program program) {
        for (AST.Statement s : program.statements) {
            execStatement(s);
        }
    }

    private void execStatement(AST.Statement s) {
        if (s instanceof AST.Assignment) {
            AST.Assignment a = (AST.Assignment) s;
            double val = eval(a.expr);
            env.put(a.name, val);
        } else if (s instanceof AST.Print) {
            AST.Print p = (AST.Print) s;
            double val = eval(p.expr);
            System.out.println(val);
        } else if (s instanceof AST.ExprStatement) {
            // evaluate but ignore result
            eval(((AST.ExprStatement) s).expr);
        } else {
            throw new RuntimeException("Unknown statement type: " + s.getClass());
        }
    }

    private double eval(AST.Expression e) {
        if (e instanceof AST.NumberLiteral) {
            return ((AST.NumberLiteral) e).value;
        } else if (e instanceof AST.Var) {
            String name = ((AST.Var) e).name;
            Double v = env.get(name);
            if (v == null) throw new RuntimeException("Undefined variable: " + name);
            return v;
        } else if (e instanceof AST.Binary) {
            AST.Binary b = (AST.Binary) e;
            double l = eval(b.left);
            double r = eval(b.right);
            switch (b.op) {
                case "+":
                    return l + r;
                case "-":
                    return l - r;
                case "*":
                    return l * r;
                case "/":
                    return l / r;
                default:
                    throw new RuntimeException("Unknown operator: " + b.op);
            }
        } else {
            throw new RuntimeException("Unknown expression type: " + e.getClass());
        }
    }

    public Map<String, Double> getEnv() {
        return env;
    }
}