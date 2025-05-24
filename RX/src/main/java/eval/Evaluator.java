package eval;

import ast.*;
import engine.RewriteEngine;

import java.util.List;

public class Evaluator {
    private final RewriteEngine engine;

    public Evaluator(RewriteEngine engine) {
        this.engine = engine;
    }

    //TODO: Temporary evaluate-method
    public Expr evaluate(Expr expr) {
        if (expr instanceof App app) {
            List<Expr> reducedArgs = app.arguments().stream()
                    .map(this::evaluate)
                    .toList();
            App reducedApp = new App(app.function(), reducedArgs);
            Expr rewritten = engine.rewrite(reducedApp);
            if (!rewritten.equals(expr)) {
                return evaluate(rewritten);
            }
            return rewritten;
        }
        return expr;
    }

}
