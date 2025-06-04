package eval;

import ast.*;
import engine.RewriteEngine;

import java.util.List;

public class Evaluator {
    private final RewriteEngine engine;

    public Evaluator(RewriteEngine engine) {
        this.engine = engine;
    }

    public Expr evaluate(Expr expr) {
        if (expr instanceof BinaryOp bin) {
            String fname = switch (bin.op()) {
                case ADD -> "add";
                case SUB -> "sub";
                case MUL -> "mul";
                case DIV -> "div";
                case EQ  -> "eq";
                case LT  -> "lt";
                case NQ -> "nq";
                case GT  -> "gt";
                case LE  -> "le";
                case GE  -> "ge";
            };
            Expr left = evaluate(bin.left());
            Expr right = evaluate(bin.right());
            return evaluate(new Call(fname, List.of(left, right)));
        }

        if (expr instanceof Call call) {
            List<Expr> reducedArgs = call.arguments().stream()
                    .map(this::evaluate)
                    .toList();
            Call reducedCall = new Call(call.function(), reducedArgs);
            Expr rewritten = engine.rewrite(reducedCall);
            if (!rewritten.equals(expr)) {
                return evaluate(rewritten);
            }
            return rewritten;
        }
        return expr;
    }

}
