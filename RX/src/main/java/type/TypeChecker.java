package type;

import ast.Rule;

import java.util.*;

public class TypeChecker {

    private final Map<String, List<FunctionSignature>> env = new HashMap<>();

    public void checkAll(List<Rule> rules) {
        for (Rule rule : rules) {
            FunctionSignature sig = FunctionSignature.from(rule);
            env.computeIfAbsent(sig.name(), k -> new ArrayList<>());

            boolean duplicate = env.get(sig.name()).stream()
                    .anyMatch(existing -> existing.argTypes().equals(sig.argTypes()));

            if (duplicate) {
                throw new RuntimeException("Duplicate function definition: " + sig.name() +
                        " with same argument types: " + sig.argTypes());
            }

            env.get(sig.name()).add(sig);
        }
    }
}
