package engine;

import ast.*;
import rules.Pattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PatternMatcher {
    public Optional<Map<String, Expr>> match(App expr, Pattern pattern) {
        if (!expr.function().equals(pattern.name())) return Optional.empty();
        if (expr.arguments().size() != pattern.parameters().size()) return Optional.empty();

        Map<String, Expr> bindings = new HashMap<>();
        for (int i = 0; i < pattern.parameters().size(); i++) {
            bindings.put(pattern.parameters().get(i), expr.arguments().get(i));
        }
//        System.out.println("Matched: " + expr + " with " + pattern);
//        System.out.println("Bindings: " + bindings);
        return Optional.of(bindings);
    }
}