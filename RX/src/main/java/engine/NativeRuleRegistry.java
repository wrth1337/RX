package engine;

import ast.*;

import java.util.List;
import java.util.Optional;

public class NativeRuleRegistry {

    public static Optional<Expr> eval(Call call) {
        String fn = call.function();
        List<Expr> args = call.arguments();

        //String Operations
        if (fn.equals("concat") && args.size() == 2 &&
                args.get(0) instanceof Literal a && args.get(1) instanceof Literal b) {
            return Optional.of(new StringLiteral(a.asRawString() + b.asRawString()));
        }

        if (fn.equals("length") && args.size() == 1 && args.get(0) instanceof StringLiteral s) {
            return Optional.of(new IntLiteral(s.value().length()));
        }

        if (fn.equals("charAt") && args.size() == 2 &&
                args.get(0) instanceof StringLiteral s && args.get(1) instanceof IntLiteral i) {

            String str = s.value();
            int idx = i.value();

            if (idx < 0 || idx >= str.length()) {
                throw new RuntimeException(
                        "Native charAt: index " + idx + " out of bounds (length " + str.length() + ")");
            }

            return Optional.of(new CharLiteral(str.charAt(idx)));
        }

        if (fn.equals("explode") && args.size() == 1 && args.get(0) instanceof StringLiteral s) {
            Expr result = stringToList(s.value());
            return Optional.of(result);
        }

        //Char Operations
        if (fn.equals("toInt") && args.size() == 1 && args.get(0) instanceof CharLiteral c) {
            return Optional.of(new IntLiteral(c.value()));
        }

        // Generic EQ/NQ support
        if ((fn.equals("eq") || fn.equals("nq")) && args.size() == 2) {
            Expr a = args.get(0);
            Expr b = args.get(1);

            if (a instanceof Literal la && b instanceof Literal lb) {
                boolean equal = la.equals(lb);
                return Optional.of(new BoolLiteral(fn.equals("eq") == equal));
            }
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
                case "div" -> {
                    if (l.value() % r.value() != 0) {
                        yield Optional.of(new FloatLiteral((double) l.value() / r.value()));
                    } else {
                        yield Optional.of(new IntLiteral(l.value() / r.value()));
                    }
                }
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

    private static Expr stringToList(String str) {
        Expr result = new Call(null, "Nil", List.of()); // Nil()
        for (int i = str.length() - 1; i >= 0; i--) {
            result = new Call(null, "Cons", List.of(
                    new CharLiteral(str.charAt(i)),
                    result
            ));
        }
        return result;
    }
}
