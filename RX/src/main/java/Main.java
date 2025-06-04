import ast.*;
import engine.RewriteEngine;
import eval.Evaluator;
import lexer.Lexer;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        //TODO: code should be read from a file -> args...
        //TODO: Add more args/flags such as verbose mode, debug mode, repl(?) etc...
        String code = """
        fact(12)
        """;


        String preludeCode = Files.readString(Path.of("src/main/resources/rx_prelude.rx"));
        String fullCode = preludeCode + "\n" + code;



        Parser parser = new Parser(new Lexer(fullCode));
        List<TopLevelItem> items = parser.parse();



        List<Rule> rules = new ArrayList<>();
        List<Expr> expressions = new ArrayList<>();
        for (TopLevelItem item : items) {
            if (item instanceof Rule rule) {
                rules.add(rule);
            } else if (item instanceof Expr expr) {
                expressions.add(expr);
            }
        }

        RewriteEngine engine = new RewriteEngine(rules);
        Evaluator evaluator = new Evaluator(engine);

        Path outputDir = Path.of("out");
        Files.createDirectories(outputDir);

        //TODO: Output should be written to a single file, not multiple files...
        //Easier for debugging for now... maybe an extra flag later?
        for (int i = 0; i < expressions.size(); i++) {
            Expr original = expressions.get(i);
            Expr result = evaluator.evaluate(original);

            System.out.println("Expression " + (i + 1) + ":\n" + result.toString());

            Path outFile = outputDir.resolve("result_" + (i + 1) + ".rx");
            Files.writeString(outFile, result.toString());
        }

        System.out.println("Program successfully rewritten");
    }
}
