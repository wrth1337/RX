package eval;

import ast.*;
import engine.RewriteEngine;
import engine.RewriteResult;
import repl.Highlighter;

import java.util.List;
import java.util.Optional;

public class Evaluator {
    private final RewriteEngine engine;

    public Evaluator(RewriteEngine engine) {
        this.engine = engine;
    }

    public Expr evaluate(Expr expr, String context) {
        if (expr instanceof BinaryOp bin) {
            String fname = switch (bin.op()) {
                case ADD -> "add";
                case SUB -> "sub";
                case MUL -> "mul";
                case DIV -> "div";
                case MOD -> "mod";
                case EQ  -> "eq";
                case LT  -> "lt";
                case NQ -> "nq";
                case GT  -> "gt";
                case LE  -> "le";
                case GE  -> "ge";
            };
            Expr left = evaluate(bin.left(), context);
            Expr right = evaluate(bin.right(), context);
            return evaluate(new Call(null,fname, List.of(left, right)), context);
        }

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

    public Expr evaluateWithTrace(Expr expr, List<String> trace, String context) {
        if (expr instanceof BinaryOp bin) {
            String fname = switch (bin.op()) {
                case ADD -> "add";
                case SUB -> "sub";
                case MUL -> "mul";
                case DIV -> "div";
                case MOD -> "mod";
                case EQ  -> "eq";
                case LT  -> "lt";
                case NQ -> "nq";
                case GT  -> "gt";
                case LE  -> "le";
                case GE  -> "ge";
            };
            Expr left = evaluateWithTrace(bin.left(), trace, context);
            Expr right = evaluateWithTrace(bin.right(), trace, context);
            return evaluateWithTrace(new Call(null,fname, List.of(left, right)), trace, context);
        }

        if (expr instanceof Call call) {
            List<Expr> reducedArgs = call.arguments().stream()
                    .map(arg -> evaluateWithTrace(arg, trace,  context))
                    .toList();
            Call reducedCall = new Call(call.namespace(),call.function(), reducedArgs);
            String namespace = call.namespace() == null ? context : call.namespace();
            Optional<RewriteResult> rewritten = engine.rewriteWithRule(reducedCall, context);
            if (rewritten.isPresent() && !rewritten.get().result().equals(expr)) {
                RewriteResult rr = rewritten.get();
                String highlightedReducedCall = Highlighter.highlight(reducedCall.toString());
                String highlightedRule = Highlighter.highlight(rr.rule().toString());
                String highlightedResult = Highlighter.highlight(rr.result().toString());
                String highlitedContext = Highlighter.highlight(namespace);

                trace.add("[%d] Expression: %s\n     Context: %s\n     Rule: %s\n     Result: %s".formatted(
                        trace.size() + 1,
                        highlightedReducedCall,
                        highlitedContext,
                        highlightedRule,
                        highlightedResult
                ));
                return evaluateWithTrace(rewritten.get().result(), trace, namespace);
            }
        }
        return expr;
    }

}
