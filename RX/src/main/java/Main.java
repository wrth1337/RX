import ast.*;
import engine.RuleValidator;
import modules.ModuleLoader;
import engine.RewriteEngine;
import eval.Evaluator;
import lexer.Lexer;
import modules.Namespace;
import parser.Parser;
import repl.Repl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
                if (args.length > 1) {
                    System.err.println("Usage: rx -r");
                    System.exit(1);
                }
                Repl repl = new Repl();
                repl.start();
                break;
            default:
                System.err.println("Usage: rx [-i <file> | -r]");
                System.exit(1);
        }
    }

    private static void interpret(String filename) throws IOException {
        //TODO: put in its own package...

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


        // Parse sourcecode
        Parser parser = new Parser(new Lexer(code));
        List<TopLevelItem> items = parser.parse();

        List<Import> rootImports = new ArrayList<>();
        List<Rule> rootRules = new ArrayList<>();
        List<Expr> expressions = new ArrayList<>();
        for (TopLevelItem item : items) {
            if (item instanceof Rule rule) {
                rootRules.add(rule);
            } else if (item instanceof Expr expr) {
                expressions.add(expr);
            } else if (item instanceof Import imp) {
                rootImports.add(imp);
            }
        }

        // Load all imports
        ModuleLoader loader = new ModuleLoader(Path.of("modules/"));

        Map<String, Namespace> namespaces = loader.loadAll(rootRules, rootImports);


        RuleValidator.checkNamespaces(namespaces);

        // Load Engine
        RewriteEngine engine = new RewriteEngine(namespaces);
        Evaluator evaluator = new Evaluator(engine);

        // Evaluation
        StringBuilder outputBuilder = new StringBuilder();

        for (int i = 0; i < expressions.size(); i++) {
            Expr original = expressions.get(i);
            Expr result = evaluator.evaluate(original, "Main");

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
