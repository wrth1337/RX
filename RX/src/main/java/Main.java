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
import java.util.Scanner;

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
                repl();
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

    private static void repl() {
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

                default:
                    try {
                        Parser parser = new Parser(new Lexer(input));
                        List<TopLevelItem> items = parser.parse();

                        for (TopLevelItem item : items) {
                            if (item instanceof Rule rule) {
                                rules.add(rule);
                                engine = new RewriteEngine(rules);
                                evaluator = new Evaluator(engine);
                                System.out.println("Rule added: " + rule);
                            } else if (item instanceof Expr expr) {
                                Expr result = evaluator.evaluate(expr);
                                System.out.printf("// %s\n%s\n\n", expr, result);
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
    }
}
