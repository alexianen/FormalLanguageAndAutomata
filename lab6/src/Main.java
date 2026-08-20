import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String source;
        if (args.length >= 1) {
            source = Files.readString(Path.of(args[0]));
        } else {
            source = """
                    // Sample program
                    x = 2 + 3 * (4 - 1);
                    y = x * 2;
                    print(x);
                    print(y);
                    """;
            System.out.println("No file provided, using sample program:\n" + source);
        }

        // Lexer
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        System.out.println("Tokens:");
        tokens.forEach(System.out::println);

        // Parsing
        Parser parser = new Parser(tokens);
        AST.Program program = parser.parseProgram();

        // Print AST
        System.out.println("\nAST:");
        System.out.println(program.pretty(0));

        // Interpret / run
        System.out.println("Interpreter output:");
        Interpreter interp = new Interpreter();
        interp.run(program);
    }
}