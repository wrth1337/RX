package eval;

import ast.*;
import engine.RewriteEngine;
import modules.Namespace;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EvaluatorTest {

    @Test
    void testEvaluateBinaryOp() {
        Namespace prelude = new Namespace("Prelude", List.of(), List.of());
        RewriteEngine engine = new RewriteEngine(Map.of("Prelude", prelude));
        Evaluator evaluator = new Evaluator(engine);

        BinaryOp expr = new BinaryOp(new IntLiteral(2),Operator.ADD, new IntLiteral(3));
        Expr result = evaluator.evaluate(expr, "Prelude");

        assertThat(result).isEqualTo(new IntLiteral(5));
    }

    @Test
    void testEvaluateCallWithoutRewrite() {
        RewriteEngine engine = new RewriteEngine(Map.of("Prelude", new Namespace("Prelude", List.of(), List.of())));
        Evaluator evaluator = new Evaluator(engine);

        Call call = new Call(null, "add", List.of(new IntLiteral(2), new IntLiteral(3)));
        Expr result = evaluator.evaluate(call, "Prelude");

        assertThat(result).isEqualTo(new IntLiteral(5));
    }

    @Test
    void testEvaluateWithTrace() {
        RewriteEngine engine = new RewriteEngine(Map.of("Prelude", new Namespace("Prelude", List.of(), List.of())));
        Evaluator evaluator = new Evaluator(engine);

        BinaryOp expr = new BinaryOp(new IntLiteral(1), Operator.ADD, new IntLiteral(2));
        List<TraceEntry> trace = new java.util.ArrayList<>();
        Expr result = evaluator.evaluateWithTrace(expr, trace, "Prelude");

        assertThat(result).isEqualTo(new IntLiteral(3));
        assertThat(trace).hasSize(1);
    }

}
