package engine;

import ast.*;

import java.util.ArrayList;
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
        return rewriteWithRule(expr)
                .map(RewriteResult::result)
                .orElse(expr);
    }

    public Optional<RewriteResult> rewriteWithRule(Expr expr) {
        if (expr instanceof Call call) {
            // 1. Try user-defined rules
            for (Rule rule : rules) {
                Optional<Map<String, Expr>> match = matcher.match(call, rule.pattern());
                if (match.isPresent()) {
                    Expr result = substitutor.substitute(rule.replacement(), match.get());
                    return Optional.of(new RewriteResult(result, rule));
                }
            }

            //TODO: Maybe add another "engine" for evaluation in RX... could be very bad performance wise
            // Peano...

            // 2. Try native function
            Optional<Expr> nativeResult = evalNative(call);
            if (nativeResult.isPresent()) {
                Rule nativeRule = makeNativeRule(call, nativeResult.get());
                return Optional.of(new RewriteResult(nativeResult.get(), nativeRule));
            }
        }

        return Optional.empty();
    }


    private Optional<Expr> evalNative(Call call) {
        String fn = call.function();
        List<Expr> args = call.arguments();

        //String Operations
        if (fn.equals("concat") && args.size() == 2 &&
                args.get(0) instanceof Literal a && args.get(1) instanceof Literal b) {
            return Optional.of(new StringLiteral(a + b.toString()));
        }

        if (fn.equals("length") && args.size() == 1 && args.get(0) instanceof StringLiteral s) {
            return Optional.of(new IntLiteral(s.value().length()));
        }

        if (fn.equals("charAt") && args.size() == 2 &&
                args.get(0) instanceof StringLiteral s && args.get(1) instanceof IntLiteral i) {
            return Optional.of(new CharLiteral(s.value().charAt(i.value())));
        }

        //Char Operations
        if (fn.equals("toInt") && args.size() == 1 && args.get(0) instanceof CharLiteral c) {
            return Optional.of(new IntLiteral(c.value()));
        }

        //Numeral operations
        if (args.size() != 2) return Optional.empty();

        Expr left = args.get(0);
        Expr right = args.get(1);

        //Float Promotion
        boolean isFloat = left instanceof FloatLiteral || right instanceof FloatLiteral;

        if (isFloat) {
            double a = (left instanceof FloatLiteral f) ? f.value()
                    : ((IntLiteral) left).value();
            double b = (right instanceof FloatLiteral f ) ? f.value()
                    : ((IntLiteral) right).value();

            return switch (fn) {
                case "add" -> Optional.of(new FloatLiteral(a + b));
                case "sub" -> Optional.of(new FloatLiteral(a - b));
                case "mul" -> Optional.of(new FloatLiteral(a * b));
                case "div" -> Optional.of(new FloatLiteral(a / b));
                case "mod" -> Optional.of(new FloatLiteral(a % b));

                case "eq"  -> Optional.of(new BoolLiteral(a == b));
                case "lt"  -> Optional.of(new BoolLiteral(a <  b));
                case "le"  -> Optional.of(new BoolLiteral(a <= b));
                case "gt"  -> Optional.of(new BoolLiteral(a >  b));
                case "ge"  -> Optional.of(new BoolLiteral(a >= b));
                case "nq"  -> Optional.of(new BoolLiteral(a != b));

                default    -> Optional.empty();
            };
        }

        //Integer Operation
        if (left instanceof IntLiteral l && right instanceof IntLiteral r) {
            return switch (fn) {
                case "add" -> Optional.of(new IntLiteral(l.value() + r.value()));
                case "sub" -> Optional.of(new IntLiteral(l.value() - r.value()));
                case "mul" -> Optional.of(new IntLiteral(l.value() * r.value()));
                case "div" -> Optional.of(new IntLiteral(l.value() / r.value()));
                case "mod" -> Optional.of(new IntLiteral(l.value() % r.value()));

                case "eq"  -> Optional.of(new BoolLiteral(l.value() == r.value()));
                case "lt"  -> Optional.of(new BoolLiteral(l.value() <  r.value()));
                case "le"  -> Optional.of(new BoolLiteral(l.value() <= r.value()));
                case "gt"  -> Optional.of(new BoolLiteral(l.value() >  r.value()));
                case "ge"  -> Optional.of(new BoolLiteral(l.value() >= r.value()));
                case "nq"  -> Optional.of(new BoolLiteral(l.value() != r.value()));

                default    -> Optional.empty();
            };
        }

        return Optional.empty();
    }

    //Is needed for the Trace-Mode -> Native Rules arent "Rewriting-Rules" per definition
    private Rule makeNativeRule(Call call, Expr result) {
        String sb = "[native rule] " + call.function();

        List<PatternArg> patternArgs= new ArrayList<>();
        for (int i = 0; i < call.arguments().size() ; i++) {
            patternArgs.add(new PatternExpr(call.arguments().get(i)));
        }
        Pattern dummypattern = new Pattern(sb, patternArgs);
        return new Rule(dummypattern, result);
    }

}
