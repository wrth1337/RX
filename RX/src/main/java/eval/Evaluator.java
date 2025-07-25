package eval;

import ast.*;
import engine.RewriteEngine;
import engine.RewriteResult;

import java.util.List;
import java.util.Optional;

public class Evaluator {
    private final RewriteEngine engine;

    public Evaluator(RewriteEngine engine) {
        this.engine = engine;
    }

    public Expr evaluate(Expr expr, String context) {
        if (expr instanceof Call call) {
            List<Expr> reducedArgs = call.arguments().stream()
                    .map(arg -> evaluate(arg, context))
                    .toList();
            Call reducedCall = new Call(call.namespace(),call.function(), reducedArgs);
            String namespace = call.namespace() == null ? context : call.namespace();
            Expr rewritten = engine.rewrite(reducedCall, context);
            if (!rewritten.equals(expr)) {
                return evaluate(rewritten, namespace);
            }
            return rewritten;
        }
        return expr;
    }

    public Expr evaluateWithTrace(Expr expr, List<TraceEntry> trace, String context) {
        if (expr instanceof Call call) {
            List<Expr> reducedArgs = call.arguments().stream()
                    .map(arg -> evaluateWithTrace(arg, trace,  context))
                    .toList();
            Call reducedCall = new Call(call.namespace(),call.function(), reducedArgs);
            String namespace = call.namespace() == null ? context : call.namespace();
            Optional<RewriteResult> rewritten = engine.rewriteWithRule(reducedCall, context);
            if (rewritten.isPresent() && !rewritten.get().result().equals(expr)) {
                RewriteResult rr = rewritten.get();
                trace.add(new TraceEntry(trace.size()+1, reducedCall.toString(), context, rr.rule().toString(), rr.result().toString()));
                return evaluateWithTrace(rewritten.get().result(), trace, namespace);
            }
        }
        return expr;
    }

}
