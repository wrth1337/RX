package interpreter;

import ast.*;
import engine.RewriteEngine;
import engine.RuleValidator;
import eval.Evaluator;
import lexer.Lexer;
import modules.ModuleLoader;
import modules.ModuleTester;
import modules.Namespace;
import parser.Parser;
import repl.Highlighter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Interpreter {

    private final boolean debug;
    private final boolean highlighting;
    private final boolean testModules;

    public Interpreter(boolean debug, boolean highlighting, boolean testModules) {
        this.debug = debug;
        this.highlighting = highlighting;
        this.testModules = testModules;
    }

    public void interpret(Path filename) throws IOException {
        if (!filename.toString().endsWith(".rx")) {
            throw new IllegalArgumentException("Input file must have a .rx extension");
        }

        log("[1] Reading file: " + filename);

        String code = Files.readString(filename);

        log("[2] Parsing source code");
        Parser parser = new Parser(new Lexer(code));
        List<TopLevelItem> items = parser.parse();

        List<Import> imports = new ArrayList<>();
        List<Rule> rules = new ArrayList<>();
        List<Expr> expressions = new ArrayList<>();

        for (TopLevelItem item : items) {
            if (item instanceof Rule r) {
                rules.add(r);
                log("  Found rule: " + highlight(r.toString()));
            }
            else if (item instanceof Expr e) {
                expressions.add(e);
                log("  Found expression: " + highlight(e.toString()));
            }
            else if (item instanceof Import i) {
                imports.add(i);
                log("  Found import: " + highlight(i.toString()));
            }
        }

        log("[3] Loading modules");
        ModuleLoader loader = new ModuleLoader(Path.of("modules/"), testModules);
        Map<String, Namespace> namespaces = loader.loadAll(rules, imports);
        log("  Modules loaded: " + namespaces.keySet());

        log("[4] Validating namespaces");
        log("[4.1] Validating rules");
        RuleValidator.checkNamespaces(namespaces);
        if (testModules) {
            log("[4.2] Test namespaces");
            Map<String, List<List<Expr>>> testresult = ModuleTester.testNamespaces(namespaces);
            for (Map.Entry<String, List<List<Expr>>> entry : testresult.entrySet()) {
                String moduleName = entry.getKey();
                List<Expr> passes = entry.getValue().get(0);
                List<Expr> fails = entry.getValue().get(1);
                if (debug) {
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

                } else {
                    if (!fails.isEmpty()) {
                        System.out.println("Module " + moduleName + " has " + fails.size() + " failed Tests.");
                    }
                }
            }
        }

        log("[5] Starting evaluation");
        RewriteEngine engine = new RewriteEngine(namespaces);
        Evaluator evaluator = new Evaluator(engine);

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < expressions.size(); i++) {
            Expr expr = expressions.get(i);
            log("  Evaluating expression [" + (i + 1) + "]: " + highlight(expr.toString()) );

            Expr result = evaluator.evaluate(expr, "Main");

            log("  Result: " + highlight(result.toString()));

            output.append("// Expression ").append(i + 1).append(": ").append(expr).append("\n");
            output.append(result).append("\n\n");
        }

        log("[6] Create directory: out");
        Path outputDir = Path.of("out");
        Files.createDirectories(outputDir);

        log("[7] Create output: out/" + filename.getFileName().toString().replaceAll("\\.rx$", "_output.rx"));
        Path outFile = outputDir.resolve(filename.getFileName().toString().replaceAll("\\.rx$", "_output.rx"));
        Files.writeString(outFile, output.toString());

        log("[8] Output written to: " + outFile);
    }

    private void log(String message) {
        if (!debug) return;
        System.out.println("[DEBUG] " + message);
    }

    private String highlight(String input) {
        if(highlighting) return Highlighter.highlight(input);
        else return input;
    }
}
