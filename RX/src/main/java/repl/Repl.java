package repl;

import ast.Expr;
import ast.Import;
import ast.Rule;
import ast.TopLevelItem;
import eval.TraceEntry;
import modules.ModuleLoader;
import engine.RewriteEngine;
import engine.RuleValidator;
import eval.Evaluator;
import lexer.Lexer;
import modules.ModuleTester;
import modules.Namespace;
import parser.Parser;

import java.nio.file.Path;
import java.util.*;

public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    //TODO: Testing in REPL
    ModuleLoader loader = new ModuleLoader(Path.of("modules/"), false);
    List<Import> rootImports = new ArrayList<>();
    List<Rule> rootRules = new ArrayList<>();
    Map<String, Namespace> namespaces = loader.loadAll(rootRules, rootImports);
    RewriteEngine engine = new RewriteEngine(namespaces);
    Evaluator evaluator = new Evaluator(engine);
    private boolean traceMode = false;
    private boolean highlighting = true;
    private boolean testMode = false;

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
            System.out.println(highlight(line));
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
                highlighting = !highlighting;
                System.out.println("Highlighting set to " + (highlighting ? "on" : "off") + "\n");
                break;
            case "\\?":
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
                System.out.println("Trace mode set to " + (traceMode ? "on" : "off") + "\n");
                break;
            case "\\test":
                testMode = !testMode;
                loader = new ModuleLoader(Path.of("modules/"), testMode);
                System.out.println("Test mode set to " + (testMode ? "on" : "off") + "\n");
                break;
            default:
                System.out.println("Unknown command: " + input);
                printHelp();
        }
    }

    private void printHelp() {
        System.out.println("Type '\\q' to quit.");
        System.out.println("Type '\\h' to toggle highlighting. Current mode: " + (highlighting ? "on" : "off"));
        System.out.println("Type '\\?' for help.");
        System.out.println("Type '\\c' to clear all rules.");
        System.out.println("Type '\\r' to show all available rules.");
        System.out.println("Type '\\t' to toggle trace-mode. Current mode: " + (traceMode ? "on" : "off"));
        System.out.println("Type '\\test' to toggle test-mode. Current mode is: " + (testMode ? "on" : "off"));
        System.out.println();
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
                List<String> highlightedRules = namespace.rules().stream().map(n -> highlight(n.toString())).toList();
                for (String rule : highlightedRules) {
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
            String message = "\u001B[0;31m" + "Parse or evaluation error: " + "\u001B[0m";
            System.out.println(message + e.getMessage() + "\n");
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
            String highlightedRule = highlight(rule.toString());
            System.out.println("Rule added: " + highlightedRule);
        } catch (Exception e) {
            String message = "\u001B[0;31m" + "Rule error: " + "\u001B[0m";
            System.out.println(message + e.getMessage());
        }
    }

    private void evaluateExpression(Expr expr) {
        if (traceMode) {
            List<TraceEntry> traceEntries = new ArrayList<>();
            Expr result = evaluator.evaluateWithTrace(expr, traceEntries, "Main");
            System.out.println();
            for (TraceEntry trace : traceEntries) {
                String highlightedReducedCall = highlight(trace.expression());
                String highlightedContext = highlight(trace.context());
                String highlightedRule = highlight(trace.rule());
                String highlightedResult = highlight(trace.result());
                System.out.printf(
                        "[%d] Expression: %s\n     Context: %s\n     Rule: %s\n     Result: %s%n", trace.step(),
                        highlightedReducedCall,
                        highlightedContext,
                        highlightedRule,
                        highlightedResult
                );
            }
            String highlightedExpr = highlight(expr.toString());
            String highlightedResult = highlight(result.toString());
            System.out.printf("\nInitial Expression: %s\nResult: %s\n\n", highlightedExpr, highlightedResult);
        } else {
            Expr result = evaluator.evaluate(expr, "Main");
            String highlightedExpr = highlight(expr.toString());
            String highlightedResult = highlight(result.toString());
            System.out.printf("Expression: %s\nResult: %s\n\n", highlightedExpr, highlightedResult);
        }
    }

    private void loadImport(Import imp) {
        if (namespaces.get("Main").imports().stream().map(Import::module).toList().contains(imp.module())) {
            System.out.println("Module already imported: " + highlight(imp.toString()));
            return;
        }
        try {
            rootImports.add(imp);
            namespaces = loader.loadAll(rootRules, rootImports);

            RuleValidator.checkNamespaces(namespaces);

            if (testMode) {
                Map<String, List<List<Expr>>> testresult = ModuleTester.testNamespaces(filterNamespace(namespaces, imp));
                for (Map.Entry<String, List<List<Expr>>> entry : testresult.entrySet()) {
                    String moduleName = entry.getKey();
                    List<Expr> passes = entry.getValue().get(0);
                    List<Expr> fails = entry.getValue().get(1);
                    if (!passes.isEmpty()) {
                        System.out.println(moduleName + " - Passes [" + passes.size() + "/" + (passes.size() + fails.size()) + "]:");
                        for (Expr pass : passes) {
                            System.out.println("  "+pass.toString());
                        }
                    }
                    if (!fails.isEmpty()) {
                        System.out.println(moduleName + " - Fails [" + fails.size() + "/" + (passes.size() + fails.size()) + "]:");
                        for (Expr fail : fails) {
                            System.out.println("  "+fail.toString());
                        }
                    }
                }
            }

            engine = new RewriteEngine(namespaces);
            evaluator = new Evaluator(engine);
            String highlightedImport = highlight(imp.toString());
            System.out.println("Module imported: " + highlightedImport);
        } catch (Exception e) {
            rootImports.remove(imp);
            namespaces = loader.loadAll(rootRules, rootImports);
            engine = new RewriteEngine(namespaces);
            evaluator = new Evaluator(engine);
            String highlightedImport = highlight(imp.toString());
            String message = "\u001B[0;31m" + "Failed to load module: " + "\u001B[0m";
            System.out.println(message + highlightedImport + "\n" + e.getMessage());
        }
    }

    private String highlight(String input) {
        if (highlighting) {
            return Highlighter.highlight(input);
        } else {
            return input;
        }
    }

    private static Map<String, Namespace> filterNamespace(Map<String, Namespace> namespaces, Import namespaceImport) {
        Namespace namespace = namespaces.get(namespaceImport.module());

        Map<String, Namespace> filteredMap = new HashMap<>();
        filteredMap.put(namespace.name(), namespace);
        filteredMap.put("Prelude", namespaces.get("Prelude"));

        for (Import importDecl : namespace.imports()) {
            String importName = importDecl.module();
            if (namespaces.containsKey(importName)) {
                filteredMap.put(importName, namespaces.get(importName));
            }
        }

        return filteredMap;
    }
}