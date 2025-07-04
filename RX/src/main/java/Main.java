import ast.*;
import engine.ModuleLoader;
import engine.RewriteEngine;
import engine.RuleValidator;
import eval.Evaluator;
import lexer.Lexer;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

        // Load prelude
        String preludeCode = Files.readString(Path.of("src/main/resources/rx_prelude.rx"));
        String fullCode = preludeCode + "\n" + code;

        // Parse the whole sourcecode
        Parser parser = new Parser(new Lexer(fullCode));
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
        ModuleLoader loader = new ModuleLoader();
        List<Rule> allImportedRules = loader.loadModules(rootImports);

        // Fuse rootRoles and importedRules
        List<Rule> allRules = new ArrayList<>();
        allRules.addAll(allImportedRules);
        allRules.addAll(rootRules);

        // Check rules for duplicates
        RuleValidator.ensureNoDuplicates(allRules);

        // Load Engine
        RewriteEngine engine = new RewriteEngine(allRules);
        Evaluator evaluator = new Evaluator(engine);

        // Evaluation
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

    private static void repl() {
        //TODO: put in its own package...
        //TODO: Highlighting

        Scanner scanner = new Scanner(System.in);
        String input;

        List<Rule> rules = new ArrayList<>();
        RewriteEngine engine = new RewriteEngine(rules);
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
                    rules.clear();
                    engine = new RewriteEngine(rules);
                    evaluator = new Evaluator(engine);
                    System.out.println("All rules cleared.");
                    break;

                case "\\r":
                    if (rules.isEmpty()) {
                        System.out.println("No rules defined.");
                    } else {
                        System.out.println("=== Rules ===");
                        for (Rule rule : rules) {
                            System.out.println(rule);
                        }
                    }
                    break;

                case "\\p":
                    try {
                        String preludeCode = Files.readString(Path.of("src/main/resources/rx_prelude.rx"));
                        Parser preludeParser = new Parser(new Lexer(preludeCode));
                        List<TopLevelItem> preludeItems = preludeParser.parse();

                        for (TopLevelItem item : preludeItems) {
                            if (item instanceof Rule rule) {
                                rules.add(rule);
                            }
                        }

                        engine = new RewriteEngine(rules);
                        evaluator = new Evaluator(engine);
                        System.out.println("Prelude loaded.");
                    } catch (IOException e) {
                        System.err.println("Failed to load prelude: " + e.getMessage());
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
                                    List<Rule> newRules = new ArrayList<>(rules);
                                    newRules.add(rule);
                                    RuleValidator.ensureNoDuplicates(newRules);
                                    rules = newRules;
                                    engine = new RewriteEngine(rules);
                                    evaluator = new Evaluator(engine);
                                    System.out.println("Rule added: " + rule);
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                    System.err.println("Rule not added.");
                                }

                            } else if (item instanceof Expr expr) {
                                if (traceMode) {
                                    List<String> trace = new ArrayList<>();
                                    Expr result = evaluator.evaluateWithTrace(expr, trace);
                                    System.out.println();
                                    for (String s : trace) {
                                        System.out.println(s);
                                    }
                                    System.out.printf("\nInitial Expression: %s\nResult: %s\n\n", expr, result);
                                } else {
                                    Expr result = evaluator.evaluate(expr);
                                    System.out.printf("// %s\n%s\n\n", expr, result);
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
        System.out.println("Type '\\p' to load the prelude.");
        System.out.println("Type '\\t' to toggle trace-mode. Current mode: " + (traceMode ? "on" : "off"));
    }
}
