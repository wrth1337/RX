package eval;

import ast.*;
import engine.RewriteEngine;

import java.util.List;

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

//    public Expr evaluateWithTrace(Expr expr, List<String> trace) {
//        if (expr instanceof BinaryOp bin) {
//            String fname = switch (bin.op()) {
//                case ADD -> "add";
//                case SUB -> "sub";
//                case MUL -> "mul";
//                case DIV -> "div";
//                case MOD -> "mod";
//                case EQ  -> "eq";
//                case LT  -> "lt";
//                case NQ -> "nq";
//                case GT  -> "gt";
//                case LE  -> "le";
//                case GE  -> "ge";
//            };
//            Expr left = evaluateWithTrace(bin.left(), trace);
//            Expr right = evaluateWithTrace(bin.right(), trace);
//            return evaluateWithTrace(new Call("tbd",fname, List.of(left, right)), trace);
//        }
//
//        if (expr instanceof Call call) {
//            List<Expr> reducedArgs = call.arguments().stream()
//                    .map(arg -> evaluateWithTrace(arg, trace))
//                    .toList();
//            Call reducedCall = new Call("tbd",call.function(), reducedArgs);
//            Optional<RewriteResult> rewritten = engine.rewriteWithRule(reducedCall);
//
//
//            if (rewritten.isPresent() && !rewritten.get().result().equals(expr)) {
//                RewriteResult rr = rewritten.get();
//                trace.add("[%d] Expression: %s\n     Rule: %s\n     Result: %s".formatted(
//                        trace.size() + 1,
//                        reducedCall,
//                        rr.rule(),
//                        rr.result()
//                ));
//                return evaluateWithTrace(rewritten.get().result(), trace);
//            }
//        }
//        return expr;
//    }

}
