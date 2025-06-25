package engine;

import ast.*;
import ast.Pattern;
import java.util.*;

public class PatternMatcher {

    public Optional<Map<String, Expr>> match(Call expr, Pattern pattern) {
        if (!expr.function().equals(pattern.name())) return Optional.empty();
        if (expr.arguments().size() != pattern.arguments().size()) return Optional.empty();

        Map<String, Expr> bindings = new HashMap<>();
        for (int i = 0; i < pattern.arguments().size(); i++) {
            PatternArg patArg = pattern.arguments().get(i);
            Expr exprArg = expr.arguments().get(i);

            if (patArg instanceof PatternWildcard) break;

            if (!matchArg(patArg, exprArg, bindings)) {
                return Optional.empty();
            }
        }

        return Optional.of(bindings);
    }

    private boolean matchArg(PatternArg patArg, Expr expr, Map<String, Expr> bindings) {
        if (patArg instanceof PatternVar var) {
            String name = var.name();
            if (bindings.containsKey(name)) {
                return bindings.get(name).equals(expr);
            } else {
                bindings.put(name, expr);
                return true;
            }
        } else if (patArg instanceof PatternLiteral lit) {
            return lit.value().equals(expr);
        } else if (patArg instanceof PatternExpr patExpr) {
            return patExpr.expr().equals(expr);
        }

        return false;
    }
}
