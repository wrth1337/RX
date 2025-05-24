package kernel;

import rules.*;
import ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KernelRules {

    public static List<Rule> defaultRules() {
        return List.of(
                ruleAdd(),
                ruleSub(),
                ruleMul(),
                ruleDiv(),
                ruleEq()
        );
    }

    private static Rule ruleAdd() {
        return new Rule(
                new Pattern("add", List.of("x", "y")),
                new NativeExpr(args -> {
                    Expr x = args.get("x");
                    Expr y = args.get("y");
                    if (x instanceof IntLiteral xi && y instanceof IntLiteral yi) {
                        return new IntLiteral(xi.value() + yi.value());
                    }
                    return new App("add", List.of(x, y));
                })
        );
    }

    private static Rule ruleSub() {
        return new Rule(
                new Pattern("sub", List.of("x", "y")),
                new NativeExpr(args -> {
                    Expr x = args.get("x");
                    Expr y = args.get("y");
                    if (x instanceof IntLiteral xi && y instanceof IntLiteral yi) {
                        return new IntLiteral(xi.value() - yi.value());
                    }
                    return new App("sub", List.of(x, y));
                })
        );
    }

    private static Rule ruleMul() {
        return new Rule(
                new Pattern("mul", List.of("x", "y")),
                new NativeExpr(args -> {
                    Expr x = args.get("x");
                    Expr y = args.get("y");
                    if (x instanceof IntLiteral xi && y instanceof IntLiteral yi) {
                        return new IntLiteral(xi.value() * yi.value());
                    }
                    return new App("mul", List.of(x, y));
                })
        );
    }

    private static Rule ruleDiv() {
        return new Rule(
                new Pattern("div", List.of("x", "y")),
                new NativeExpr(args -> {
                    Expr x = args.get("x");
                    Expr y = args.get("y");
                    if (x instanceof IntLiteral xi && y instanceof IntLiteral yi) {
                        return new IntLiteral(xi.value() / yi.value());
                    }
                    return new App("div", List.of(x, y));
                })
        );
    }

    private static Rule ruleEq() {
        return new Rule(
                new Pattern("eq", List.of("x", "y")),
                new NativeExpr(args -> {
                    Expr x = args.get("x");
                    Expr y = args.get("y");
                    if (x instanceof IntLiteral xi && y instanceof IntLiteral yi) {
                        return new BoolLiteral(xi.value() == yi.value());
                    }
                    return new App("eq", List.of(x, y));
                })
        );
    }

    // Hilfsklasse für native Java-Rewriting-Funktionen
    public interface NativeFunction {
        Expr apply(Map<String, Expr> args);
    }

    public record NativeExpr(NativeFunction impl) implements Expr {}
}
