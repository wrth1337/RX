package engine;

import ast.*;
import modules.Namespace;

import java.util.List;
import java.util.Map;

public class RuleValidator {

    public static void checkRules(List<Rule> rules, String namespaceName) {
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                Rule a = rules.get(i);
                Rule b = rules.get(j);
                if (isDuplicate(a, b)) {
                    throw new RuntimeException("Duplicate rule detected in namespace '" + namespaceName + "':\n" + a + "\n" + b);
                }
            }
        }
    }

    public static void checkNamespaces(Map<String, Namespace> namespaces) {
        for (Map.Entry<String, Namespace> entry : namespaces.entrySet()) {
            checkRules(entry.getValue().rules(), entry.getKey());
        }
    }

    private static boolean isDuplicate(Rule a, Rule b) {
        return a.pattern().name().equals(b.pattern().name()) &&
                structurallyEqual(a.pattern().arguments(), b.pattern().arguments());
    }

    private static boolean structurallyEqual(List<PatternArg> args1, List<PatternArg> args2) {
        if (args1.size() != args2.size()) return false;

        for (int i = 0; i < args1.size(); i++) {
            if (!patternArgsEqual(args1.get(i), args2.get(i))) return false;
        }

        return true;
    }

    private static boolean patternArgsEqual(PatternArg a, PatternArg b) {
        //Different classes?
        if (a.getClass() != b.getClass()) {
            return false;
        }

        //Both PatternVar?
        if (a instanceof PatternVar v1 && b instanceof PatternVar v2) {
            return true;
        }

        //Both Literals? Same Literal-Values?
        if (a instanceof PatternLiteral l1 && b instanceof PatternLiteral l2) {
            return l1.value().equals(l2.value());
        }

        //Both Expressions? Same expressions?
        if (a instanceof PatternExpr e1 && b instanceof PatternExpr e2) {
            return callEquals(e1.expr(), e2.expr());
        }

        return false;
    }

    private static boolean callEquals(Expr e1, Expr e2) {
        if (e1 instanceof Call c1 && e2 instanceof Call c2) {
            if (!c1.function().equals(c2.function())) return false;
            if (c1.arguments().size() != c2.arguments().size()) return false;

            for (int i = 0; i < c1.arguments().size(); i++) {
                if (!callEquals(c1.arguments().get(i), c2.arguments().get(i))) return false;
            }
            return true;
        }

        if (e1 instanceof Literal l1 && e2 instanceof Literal l2) {
            return l1.equals(l2);
        }

        if (e1 instanceof Var && e2 instanceof Var) {
            return true;
        }

        return false;
    }
}
