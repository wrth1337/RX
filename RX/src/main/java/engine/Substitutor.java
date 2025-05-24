package engine;

import ast.*;

import java.util.List;
import java.util.Map;

public class Substitutor {
    public Expr substitute(Expr expr, Map<String, Expr> bindings) {
        if (expr instanceof Var var) {
            return bindings.getOrDefault(var.name(), var);
        } else if (expr instanceof App app) {
            List<Expr> newArgs = app.arguments().stream()
                    .map(arg -> substitute(arg, bindings))
                    .toList();
            return new App(app.function(), newArgs);
        } else {
            return expr;
        }
    }
}
