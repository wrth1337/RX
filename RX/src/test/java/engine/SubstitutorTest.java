package engine;

import ast.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SubstitutorTest {

    private final Substitutor substitutor = new Substitutor();

    @Test
    void substitute_VarIsSubstituted() {
        Var x = new Var("x");
        Expr replacement = new Var("y");

        Expr result = substitutor.substitute(x, Map.of("x", replacement));

        assertThat(result).isEqualTo(replacement);
    }

    @Test
    void substitute_VarIsNotSubstitutedIfNotInBindings() {
        Var x = new Var("x");

        Expr result = substitutor.substitute(x, Map.of());

        assertThat(result).isEqualTo(x);
    }

    @Test
    void substitute_LiteralsAreUnchanged() {
        Literal literal = new IntLiteral(42);

        Expr result = substitutor.substitute(literal, Map.of("x", new Var("y")));

        assertThat(result).isSameAs(literal);
    }
}
