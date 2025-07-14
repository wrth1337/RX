package repl;

import ast.Expr;
import ast.Import;
import ast.Rule;
import ast.TopLevelItem;
import modules.ModuleLoader;
import engine.RewriteEngine;
import engine.RuleValidator;
import eval.Evaluator;
import lexer.Lexer;
import modules.Namespace;
import parser.Parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    ModuleLoader loader = new ModuleLoader(Path.of("modules/"));
    List<Import> rootImports = new ArrayList<>();
    List<Rule> rootRules = new ArrayList<>();
    Map<String, Namespace> namespaces = loader.loadAll(rootRules, rootImports);
    RewriteEngine engine = new RewriteEngine(namespaces);
    Evaluator evaluator = new Evaluator(engine);
    private boolean traceMode = false;

    public void start() {
        printWelcome();
        printHelp();
        while (true) {
            String line = readLine();
            if (isCommand(line)) {
                processCommand(line);
            } else {
                processInput(line);
            }
        }
    }

    private void printWelcome() {
        System.out.println("=== Welcome to the RX REPL ===");
    }


    private String readLine() {
        System.out.print("> ");
        String line = scanner.nextLine().trim();
        if (!isCommand(line)) {
            System.out.print("Input: ");
            System.out.println(Highlighter.highlight(line));
        }
        return line;
    }

    private boolean isCommand(String line) {
        return line.startsWith("\\");
    }

    private void processCommand(String input) {
        switch (input) {
            case "\\q":
                System.out.println("Exiting RX REPL... bye.");
                System.exit(0);
                break;
            case "\\h":
                printHelp();
                break;
            case "\\c":
                clearRules();
                break;
            case "\\r":
                showRules();
                break;
            case "\\t":
                traceMode = !traceMode;
                System.out.println("Trace mode set to " + (traceMode ? "on" : "off"));
                break;
            default:
                System.out.println("Unknown command: " + input);
                printHelp();
        }
    }

    private void printHelp() {
        System.out.println("Type '\\q' to quit.");
        System.out.println("Type '\\h' for help.");
        System.out.println("Type '\\c' to clear all rules.");
        System.out.println("Type '\\r' to show all available rules.");
        System.out.println("Type '\\t' to toggle trace-mode. Current mode: " + (traceMode ? "on" : "off"));
    }

    private void clearRules() {
        rootRules.clear();
        rootImports.clear();
        namespaces = loader.loadAll(rootRules, rootImports);
        engine = new RewriteEngine(namespaces);
        evaluator = new Evaluator(engine);
        System.out.println("All rules cleared.");
    }

    private void showRules() {
        List<String> importedModules = new ArrayList<>(namespaces.get("Main").imports().stream().map(Import::module).toList());
        importedModules.add("Prelude");
        importedModules.add("Main");
        List<Namespace> availableNamespaces = new ArrayList<>(namespaces.values().stream().filter(ns -> importedModules.contains(ns.name())).toList());
        if (availableNamespaces.isEmpty()) {
            System.out.println("No rules defined.");
        } else {
            System.out.println("=== Rules ===");
            for (Namespace namespace : availableNamespaces) {
                System.out.println("Namespace: " + namespace.name());
                for (Rule rule : namespace.rules()) {
                    System.out.println(rule);
                }
                System.out.println();
            }
        }
    }

    private void processInput(String input) {
        try {
            Parser parser = new Parser(new Lexer(input));
            List<TopLevelItem> items = parser.parse();

            for (TopLevelItem item : items) {
                if (item instanceof Rule rule) {
                    addRule(rule);
                } else if (item instanceof Expr expr) {
                    evaluateExpression(expr);
                } else if (item instanceof Import imp) {
                    loadImport(imp);
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Parse or evaluation error: " + e.getMessage());
        }
    }

    private void addRule(Rule rule) {
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
    }

    private void evaluateExpression(Expr expr) {
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
    }

    private void loadImport(Import imp) {
        try {
            rootImports.add(imp);
            namespaces = loader.loadAll(rootRules, rootImports);
            engine = new RewriteEngine(namespaces);
            evaluator = new Evaluator(engine);
            System.out.println("Module imported: " + imp.module());
        } catch (Exception e) {
            rootImports.remove(imp);
            namespaces = loader.loadAll(rootRules, rootImports);
            engine = new RewriteEngine(namespaces);
            evaluator = new Evaluator(engine);
            System.err.println("Failed to load module: " + imp.module() + "\n" + e.getMessage());
        }
    }
}