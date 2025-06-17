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
        //TODO: Add more args/flags such as verbose mode, debug mode, repl(?) etc...
        //TODO: Metaprogramming -> Rules can have Rules as parameters -> OOP?
        //https://www.researchgate.net/scientific-contributions/Tim-Reichert-69812364

        //TODO: Errorhandling

        if(args.length < 1) {
            System.err.println("Usage: rx [-i <file> | -r]");
            System.exit(1);
        }

        String mode = args[0];

        switch (mode) {
            case "-i":
                if (args.length < 2) {
                    System.err.println("Usage: rx -i <file>");
                    System.exit(1);
                }
                String filename = args[1];
                interpret(filename);
                break;
            case "-r":
                System.out.println("REPL not implemented yet. Exiting...");
                System.exit(0);
                break;
            default:
                System.err.println("Usage: rx [-i <file> | -r]");
                System.exit(1);
        }
    }

    private static void interpret(String filename) throws IOException {
        if (!filename.endsWith(".rx")) {
            System.err.println("Error: input file must have a .rx extension");
            System.exit(1);
        }

        String code = "";

        try {
            code = Files.readString(Path.of(filename));
        } catch (IOException e) {
            System.err.printf("Error reading file: %s%n", filename);
            System.exit(1);
        }


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

        StringBuilder outputBuilder = new StringBuilder();

        for (int i = 0; i < expressions.size(); i++) {
            Expr original = expressions.get(i);
            Expr result = evaluator.evaluate(original);

            outputBuilder
                    .append("// Expression ").append(i + 1).append(" - ").append(original).append(":\n")
                    .append(result.toString()).append("\n\n");
        }

        String outputFilename = filename.replaceAll("\\.rx$", "_output.rx");

        Path outputDir = Path.of("out");
        Files.createDirectories(outputDir);

        Path outFile = outputDir.resolve(Path.of(outputFilename).getFileName());
        Files.writeString(outFile, outputBuilder.toString());

        System.out.println("All expressions written to: " + outFile);
        System.out.println("Program successfully rewritten.");
    }
}
