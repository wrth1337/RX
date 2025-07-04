package engine;

import ast.*;
import lexer.Lexer;
import parser.Parser;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ModuleLoader {

    private final Path userModulesBase = Paths.get("modules");
    private final Set<String> loadedModules = new HashSet<>();
    private final List<Rule> allRules = new ArrayList<>();

    public List<Rule> loadModules(List<Import> rootImports) {
        for (Import imp : rootImports) {
            loadModule(imp.module());
        }
        return allRules;
    }

    public List<Rule> loadModuleForREPL(String moduleName) {
        loadModule(moduleName);
        return allRules;
    }

    private void loadModule(String moduleName) {
        if (loadedModules.contains(moduleName)) {
            return;
        }

        boolean loadedInternal = false;
        boolean loadedExternal = false;

        // Search module in internal modules
        InputStream in = getClass().getResourceAsStream("/modules/" + moduleName + ".rx");
        if (in != null) {
            parseModuleStream(moduleName, in);
            loadedInternal = true;
        }

        // Search module in user modules
        Path userPath = userModulesBase.resolve(moduleName + ".rx");
        if (Files.exists(userPath)) {
            try {
                InputStream userIn = Files.newInputStream(userPath);
                parseModuleStream(moduleName, userIn);
                loadedExternal = true;
            } catch (IOException e) {
                throw new RuntimeException("Error reading user module: " + userPath, e);
            }
        }

        if (!loadedInternal && !loadedExternal) {
            throw new RuntimeException("Module not found: " + moduleName);
        }
        if (loadedInternal && loadedExternal) {
            throw new RuntimeException("Duplicate module name (internal & user): " + moduleName);
        }

        loadedModules.add(moduleName);
    }

    private void parseModuleStream(String moduleName, InputStream in) {
        try (in) {
            String source = new String(in.readAllBytes());
            Parser parser = new Parser(new Lexer(source));
            List<TopLevelItem> items = parser.parse();

            List<Import> nestedImports = new ArrayList<>();
            for (TopLevelItem item : items) {
                if (item instanceof Import imp) {
                    nestedImports.add(imp);
                } else if (item instanceof Rule rule) {
                    allRules.add(rule);
                } else {
                    throw new RuntimeException(
                            "Invalid item in module '" + moduleName + "': Only imports and rules allowed!");
                }
            }

            for (Import nested : nestedImports) {
                loadModule(nested.module());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse module: " + moduleName, e);
        }
    }
}
