package engine;

import ast.*;
import modules.Namespace;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RewriteEngineTest {

    @Test
    void testRewriteNative() {
        RewriteEngine engine = new RewriteEngine(Map.of("Prelude", new Namespace("Prelude", List.of(), List.of(), List.of())));

        Call call = new Call(null, "add", List.of(new IntLiteral(1), new IntLiteral(2)));
        Expr result = engine.rewrite(call, "Prelude");

        assertThat(result).isEqualTo(new IntLiteral(3));
    }
}
