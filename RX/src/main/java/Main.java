import ast.*;
import engine.RuleValidator;
import modules.ModuleLoader;
import engine.RewriteEngine;
import eval.Evaluator;
import lexer.Lexer;
import modules.Namespace;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    static boolean traceMode = false;

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
                repl();
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

    private static void repl() {
        //TODO: put in its own package...
        //TODO: Highlighting

        Scanner scanner = new Scanner(System.in);
        String input;

        ModuleLoader loader = new ModuleLoader(Path.of("modules/"));

        List<Import> rootImports = new ArrayList<>();
        List<Rule> rootRules = new ArrayList<>();
        Map<String, Namespace> namespaces = loader.loadAll(rootRules, rootImports);
        RewriteEngine engine = new RewriteEngine(namespaces);
        Evaluator evaluator = new Evaluator(engine);

        System.out.println("=== Welcome to the RX REPL ===");
        printHelp();

        while (true) {
            System.out.print("> ");
            input = scanner.nextLine().trim();

            switch (input) {
                case "\\q":
                    System.out.println("Exiting RX REPL... bye.");
                    return;

                case "\\h":
                    printHelp();
                    break;

                case "\\c":
                    rootRules.clear();
                    rootImports.clear();
                    namespaces = loader.loadAll(rootRules, rootImports);
                    engine = new RewriteEngine(namespaces);
                    evaluator = new Evaluator(engine);
                    System.out.println("All rules cleared.");
                    break;

                case "\\r":
                    if (namespaces.isEmpty()) {
                        System.out.println("No rules defined.");
                    } else {
                        System.out.println("=== Rules ===");
                        for (Namespace namespace : namespaces.values()) {
                            System.out.println("Namespace: " + namespace.name());
                            for (Rule rule : namespace.rules()) {
                                System.out.println(rule);
                            }
                            System.out.println();
                        }
                    }
                    break;

                case "\\t":
                    traceMode = !traceMode;
                    System.out.printf("Trace mode set to %s%n", traceMode ? "on" : "off");
                    break;

                default:
                    try {
                        Parser parser = new Parser(new Lexer(input));
                        List<TopLevelItem> items = parser.parse();

                        for (TopLevelItem item : items) {
                            if (item instanceof Rule rule) {
                                try {
                                    List<Rule> newRules = new ArrayList<>(namespaces.get("Main").rules());
                                    newRules.add(rule);
                                    RuleValidator.checkRules(newRules, "Main");
                                    namespaces.get("Main").rules().add(rule);
                                    engine = new RewriteEngine(namespaces);
                                    evaluator = new Evaluator(engine);
                                    System.out.println("Rule added: " + rule);
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                    System.err.println("Rule not added.");
                                }

                            } else if (item instanceof Expr expr) {
                                if (traceMode) {
                                    List<String> trace = new ArrayList<>();
                                    Expr result = evaluator.evaluateWithTrace(expr, trace, "Main");
                                    System.out.println();
                                    for (String s : trace) {
                                        System.out.println(s);
                                    }
                                    System.out.printf("\nInitial Expression: %s\nResult: %s\n\n", expr, result);
                                } else {
                                    Expr result = evaluator.evaluate(expr, "Main");
                                    System.out.printf("// %s\n%s\n\n", expr, result);
                                }
                            } else if (item instanceof Import imp) {
                                try {
                                    rootImports.add(imp);
                                    //TODO: New rulevalidation
                                    namespaces = loader.loadAll(rootRules, rootImports);
                                    engine = new RewriteEngine(namespaces);
                                    evaluator = new Evaluator(engine);
                                    System.out.println("Module imported: " + imp.module());
                                } catch (Exception e) {
                                    System.err.println("Failed to load module: " + imp.module() + "\n" + e.getMessage());
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        System.err.println("Parse or evaluation error: " + e.getMessage());
                    }
                    break;
            }
        }
    }


    private static void printHelp() {
        System.out.println("Type '\\q' to quit.");
        System.out.println("Type '\\h' for help.");
        System.out.println("Type '\\c' to clear all rules.");
        System.out.println("Type '\\r' to show all rules.");
        System.out.println("Type '\\t' to toggle trace-mode. Current mode: " + (traceMode ? "on" : "off"));
    }
}
