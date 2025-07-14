package modules;

import ast.Expr;
import ast.Import;
import ast.Rule;
import ast.TopLevelItem;
import lexer.Lexer;
import parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleLoader {

    private final Map<String, Namespace> loadedModules = new HashMap<>();

    private final Path userModulesPath;

    public ModuleLoader(Path userModulesPath) {
        this.userModulesPath = userModulesPath;
    }

    public Map<String, Namespace> loadAll(List<Rule> mainRules, List<Import> mainImports) {
        loadedModules.clear();
        loadPrelude();
        Namespace mainNamespace = registerMain(mainRules, mainImports);

        for (Import importModule : mainNamespace.imports()) {
            if (loadedModules.containsKey(importModule.module())) {
                continue;
            }
            Namespace imported = parseModuleFile(importModule.module());
            loadedModules.put(importModule.module(), imported);
        }

        return loadedModules;
    }

    private void loadPrelude() {
        Namespace prelude = parseModuleFile("Prelude");
        loadedModules.put("Prelude", prelude);
    }

    private Namespace registerMain(List<Rule> mainRules, List<Import> mainImports) {
        Namespace main = new Namespace("Main", mainRules, mainImports);
        loadedModules.put("Main", main);
        return main;
    }

    private Namespace parseModuleFile(String moduleName) {
        String sourceCode = readModuleSource(moduleName);
        Parser parser = new Parser(new Lexer(sourceCode));
        List<TopLevelItem> items = parser.parse();

        List<Import> imports = new ArrayList<>();
        List<Rule> rules = new ArrayList<>();
        for (TopLevelItem item : items) {
            if (item instanceof Rule rule) {
                rules.add(rule);
            } else if (item instanceof Expr) {
                throw new RuntimeException("Invalid item in module '" + moduleName + "': Only imports and rules allowed!");
            } else if (item instanceof Import imp) {
                if (!loadedModules.containsKey(imp.module())) {
                    Namespace imported = parseModuleFile(imp.module());
                    loadedModules.put(imp.module(), imported);
                }
                imports.add(imp);
            }
        }
        return new Namespace(moduleName, rules, imports);
    }

    private String readModuleSource(String moduleName) {
        String resourcePath;

        if (moduleName.equals("Prelude")) {
            resourcePath = "/prelude/Prelude.rx";
        } else {
            // 1. Try internal modules first: /modules/ModuleName.rx
            resourcePath = "/modules/" + moduleName + ".rx";
            InputStream in = getClass().getResourceAsStream(resourcePath);
            if (in != null) {
                return readInputStream(in);
            }

            // 2. Otherwise look in user modules folder
            Path userModulePath = userModulesPath.resolve(moduleName + ".rx");
            if (Files.exists(userModulePath)) {
                try {
                    return Files.readString(userModulePath);
                } catch (IOException e) {
                    throw new RuntimeException("Could not read user module: " + moduleName, e);
                }
            }

            throw new RuntimeException("Module not found: " + moduleName);
        }

        // Load Prelude (always internal)
        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new RuntimeException("Prelude not found at " + resourcePath);
        }
        return readInputStream(in);
    }

    private String readInputStream(InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not read resource input stream", e);
        }
    }
}
