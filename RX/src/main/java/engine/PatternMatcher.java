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
            if (patExpr.expr() instanceof Call patCall && expr instanceof Call exprCall) {
                Optional<Map<String, Expr>> nestedMatch = match(exprCall, new Pattern(patCall.function(), toPatternArgs(patCall.arguments())));
                if (nestedMatch.isEmpty()) return false;

                for (var entry : nestedMatch.get().entrySet()) {
                    if (bindings.containsKey(entry.getKey()) &&
                            !bindings.get(entry.getKey()).equals(entry.getValue())) {
                        return false;
                    }
                    bindings.put(entry.getKey(), entry.getValue());
                }
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private List<PatternArg> toPatternArgs(List<Expr> exprs) {
        List<PatternArg> result = new ArrayList<>();
        for (Expr expr : exprs) {
            switch (expr) {
                case Var var -> result.add(new PatternVar(var.name()));
                case Literal lit -> result.add(new PatternLiteral(lit));
                case Call call -> result.add(new PatternExpr(call));
                case null, default -> throw new RuntimeException("Unsupported pattern expression: " + expr);
            }
        }
        return result;
    }
}
