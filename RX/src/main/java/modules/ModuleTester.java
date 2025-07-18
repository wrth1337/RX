package modules;

import ast.Expr;
import ast.StringLiteral;
import engine.RewriteEngine;
import eval.Evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleTester {
    public static Map<String, List<List<Expr>>> testNamespaces(Map<String, Namespace> namespaces){
        RewriteEngine engine = new RewriteEngine(namespaces);
        Evaluator evaluator = new Evaluator(engine);

        Map<String, List<List<Expr>>> resultMap = new HashMap<>();

        for (Namespace ns : namespaces.values()) {
            List<Expr> fails = new ArrayList<>();
            List<Expr> passes = new ArrayList<>();
            for (Expr expr : ns.unitTests()) {
                Expr result = evaluator.evaluate(expr, ns.name());
                if (!(result instanceof StringLiteral(String value))) {
                    throw new RuntimeException("Invalid Unit-Test: " + expr + " -> " + result);
                }
                if(!value.startsWith("[Failed]") && !value.startsWith("[Success]")){
                    throw new RuntimeException("Invalid Unit-Test: " + expr + " -> " + result);
                }

                if(value.startsWith("[Failed]")){
                    fails.add(result);
                } else {
                    passes.add(result);
                }
            }
            if (passes.isEmpty() && fails.isEmpty() ) {
                continue;
            }
            resultMap.put(ns.name(), new ArrayList<>(List.of(passes, fails)));
        }
        return resultMap;
    }
}
