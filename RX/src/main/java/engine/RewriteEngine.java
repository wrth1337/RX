package engine;

import ast.*;
import kernel.KernelRules;
import rules.Rule;

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
        //System.out.println("Rewriting: " + expr);
        if (expr instanceof App app) {
            for (Rule rule : rules) {
                Optional<Map<String, Expr>> match = matcher.match(app, rule.pattern());
                if (match.isPresent()) {
                    Expr replacement = rule.replacement();
                    if (replacement instanceof KernelRules.NativeExpr(KernelRules.NativeFunction impl)) {
                        return impl.apply(match.get());
                    } else {
                        return substitutor.substitute(replacement, match.get());
                    }
                }
            }
        }
        return expr;
    }
}
