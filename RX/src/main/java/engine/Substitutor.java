package engine;

import ast.*;

import java.util.List;
import java.util.Map;

public class Substitutor {

    public Expr substitute(Expr expr, Map<String, Expr> bindings) {
        if (expr instanceof Var var) {
            return bindings.getOrDefault(var.name(), var);
        }

        if (expr instanceof Call call) {
            List<Expr> newArgs = call.arguments().stream()
                    .map(arg -> substitute(arg, bindings))
                    .toList();
            return new Call(call.namespace(), call.function(), newArgs);
        }

        if (expr instanceof BinaryOp bin) {
            Expr left = substitute(bin.left(), bindings);
            Expr right = substitute(bin.right(), bindings);
            return new BinaryOp(left, bin.op(), right);
        }

        return expr;
    }
}
