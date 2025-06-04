package engine;

import ast.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RewriteEngine {
    private final List<Rule> rules;
    private final PatternMatcher matcher = new PatternMatcher();
    private final Substitutor substitutor = new Substitutor();

    public RewriteEngine(List<Rule> rules) {
        this.rules = rules;
    }

    public Expr rewrite(Expr expr) {
        if (expr instanceof Call call) {
            // 1. Try user-defined rules
            for (Rule rule : rules) {
                Optional<Map<String, Expr>> match = matcher.match(call, rule.pattern());
                if (match.isPresent()) {
                    return substitutor.substitute(rule.replacement(), match.get());
                }
            }

            //TODO: Maybe add another "engine" for evaluation in RX... could be very bad performance wise
            //Idea: def add(x,y) = add(x-1,y+1)
            //Idea: def add(0,y) = y .......

            // 2. Try native function
            Optional<Expr> nativeResult = evalNative(call);
            if (nativeResult.isPresent()) return nativeResult.get();

        }
        return expr;
    }

    private Optional<Expr> evalNative(Call call) {
        String fn = call.function();
        List<Expr> args = call.arguments();
        if (args.size() != 2) return Optional.empty();

        Expr left = args.get(0);
        Expr right = args.get(1);

        if (!(left instanceof IntLiteral l) || !(right instanceof IntLiteral r)) return Optional.empty();

        return switch (fn) {
            case "add" -> Optional.of(new IntLiteral(l.value() + r.value()));
            case "sub" -> Optional.of(new IntLiteral(l.value() - r.value()));
            case "mul" -> Optional.of(new IntLiteral(l.value() * r.value()));
            case "div" -> Optional.of(new IntLiteral(l.value() / r.value()));

            case "eq"  -> Optional.of(new BoolLiteral(l.value() == r.value()));
            case "lt"  -> Optional.of(new BoolLiteral(l.value() <  r.value()));
            case "le"  -> Optional.of(new BoolLiteral(l.value() <= r.value()));
            case "gt"  -> Optional.of(new BoolLiteral(l.value() >  r.value()));
            case "ge"  -> Optional.of(new BoolLiteral(l.value() >= r.value()));
            case "nq"  -> Optional.of(new BoolLiteral(l.value() != r.value()));

            default    -> Optional.empty();
        };
    }

}
