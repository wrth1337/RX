package engine;

import ast.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PatternMatcherTest {

    @Test
    void testSimpleMatch() {
        Call call = new Call(null, "add", List.of(new IntLiteral(1), new IntLiteral(2)));
        Pattern pattern = new Pattern("add", List.of(
                new PatternVar("x"),
                new PatternVar("y")
        ));

        PatternMatcher matcher = new PatternMatcher();
        Optional<Map<String, Expr>> match = matcher.match(call, pattern);

        assertThat(match).isPresent();
        assertThat(match.get()).containsEntry("x", new IntLiteral(1));
        assertThat(match.get()).containsEntry("y", new IntLiteral(2));
    }

    @Test
    void testNoMatchDifferentName() {
        Call call = new Call(null, "sub", List.of(new IntLiteral(1), new IntLiteral(2)));
        Pattern pattern = new Pattern("add", List.of(
                new PatternVar("x"),
                new PatternVar("y")
        ));

        PatternMatcher matcher = new PatternMatcher();
        Optional<Map<String, Expr>> match = matcher.match(call, pattern);

        assertThat(match).isEmpty();
    }

    @Test
    void testNestedMatch() {
        Call call = new Call(null, "wrap", List.of(
                new Call(null, "add", List.of(new IntLiteral(1), new IntLiteral(2)))
        ));
        Pattern pattern = new Pattern("wrap", List.of(
                new PatternExpr(new Call(null, "add", List.of(new Var("a"), new Var("b"))))
        ));

        PatternMatcher matcher = new PatternMatcher();
        Optional<Map<String, Expr>> match = matcher.match(call, pattern);

        assertThat(match).isPresent();
        assertThat(match.get()).containsEntry("a", new IntLiteral(1));
        assertThat(match.get()).containsEntry("b", new IntLiteral(2));
    }
}
