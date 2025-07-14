package engine;

import ast.*;
import modules.Namespace;

import java.util.*;

public class RewriteEngine {
    private final Map<String, Namespace> namespaces;
    private final PatternMatcher matcher = new PatternMatcher();
    private final Substitutor substitutor = new Substitutor();

    public RewriteEngine(Map<String, Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    public Expr rewrite(Expr expr, String context) {
        return rewriteWithRule(expr, context)
                .map(RewriteResult::result)
                .orElse(expr);
    }

    public Optional<RewriteResult> rewriteWithRule(Expr expr, String context) {
        if (expr instanceof Call call) {
            // 1. Try module rules
            //String namespace = call.namespace() == null ? context : call.namespace();
            Optional<Map<String, Expr>> match;
            if (call.namespace() == null) {
                for (Rule rule : namespaces.get(context).rules()) {
                    match = matcher.match(call, rule.pattern());
                    if (match.isPresent()) {
                        Expr result = substitutor.substitute(rule.replacement(), match.get());
                        return Optional.of(new RewriteResult(result, rule));
                    }
                }
            } else {
                List<String> imports = namespaces.get(context).imports().stream().map(Import::module).toList();
                if (imports.contains(call.namespace())) {
                    for (Rule rule : namespaces.get(call.namespace()).rules()) {
                        match = matcher.match(call, rule.pattern());
                        if (match.isPresent()) {
                            Expr result = substitutor.substitute(rule.replacement(), match.get());
                            return Optional.of(new RewriteResult(result, rule));
                        }
                    }
                }
            }

            //2. Try prelude rules
            for (Rule rule : namespaces.get("Prelude").rules()) {
                match = matcher.match(call, rule.pattern());
                if (match.isPresent()) {
                    Expr result = substitutor.substitute(rule.replacement(), match.get());
                    return Optional.of(new RewriteResult(result, rule));
                }
            }

            // 3. Try native function
            Optional<Expr> nativeResult = NativeRuleRegistry.eval(call);
            if (nativeResult.isPresent()) {
                Rule nativeRule = makeNativeRule(call, nativeResult.get());
                return Optional.of(new RewriteResult(nativeResult.get(), nativeRule));
            }

            throw new RuntimeException("No matching rule found for call: " + call);


            //TODO: Maybe add another "engine" for evaluation in RX... could be very bad performance wise
            // Peano...
        }

        return Optional.empty();
    }


    //Is needed for the Trace-Mode -> Native Rules arent "Rewriting-Rules" per definition
    private Rule makeNativeRule(Call call, Expr result) {
        String sb = call.function();

        List<PatternArg> patternArgs= new ArrayList<>();
        for (int i = 0; i < call.arguments().size() ; i++) {
            patternArgs.add(new PatternExpr(call.arguments().get(i)));
        }
        Pattern dummypattern = new Pattern(sb, patternArgs);
        return new Rule(dummypattern, result);
    }

}
