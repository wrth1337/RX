package engine;

import ast.Pattern;
import ast.PatternVar;
import ast.Rule;
import modules.Namespace;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RuleValidatorTest {

    @Test
    void testDuplicateDetection() {
        Rule r1 = new Rule(new Pattern("add", List.of(new PatternVar("x"), new PatternVar("y"))), null);
        Rule r2 = new Rule(new Pattern("add", List.of(new PatternVar("x"), new PatternVar("y"))), null);

        Namespace ns = new Namespace("Test", List.of(r1, r2), List.of());

        assertThatThrownBy(() -> RuleValidator.checkRules(ns.rules(), "Test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Duplicate rule detected");
    }

    @Test
    void testNoDuplicateDetection() {
        Rule r1 = new Rule(new Pattern("add", List.of(new PatternVar("x"), new PatternVar("y"))), null);
        Rule r2 = new Rule(new Pattern("sub", List.of(new PatternVar("x"), new PatternVar("y"))), null);

        Namespace ns = new Namespace("Test", List.of(r1, r2), List.of());

        RuleValidator.checkRules(ns.rules(), "Test");
    }
}
