package engine;

import ast.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NativeRuleRegistryTest {

    @Test
    void testEvalAddInt() {
        Call call = new Call(null, "add",
                java.util.List.of(new IntLiteral(2), new IntLiteral(5)));
        Optional<Expr> result = NativeRuleRegistry.eval(call);
        assertThat(result).contains(new IntLiteral(7));
    }

    @Test
    void testEvalConcat() {
        Call call = new Call(null, "concat",
                java.util.List.of(new StringLiteral("Hello "), new StringLiteral("World")));
        Optional<Expr> result = NativeRuleRegistry.eval(call);
        assertThat(result).contains(new StringLiteral("Hello World"));
    }

    @Test
    void testEvalCharAt() {
        Call call = new Call(null, "charAt",
                java.util.List.of(new StringLiteral("ChatGPT"), new IntLiteral(4)));
        Optional<Expr> result = NativeRuleRegistry.eval(call);
        assertThat(result).contains(new CharLiteral('G'));
    }

    @Test
    void testEvalFloatPromotion() {
        Call call = new Call(null, "mul",
                java.util.List.of(new IntLiteral(2), new FloatLiteral(3.5)));
        Optional<Expr> result = NativeRuleRegistry.eval(call);
        assertThat(result).contains(new FloatLiteral(7.0));
    }

    @Test
    void testEvalToInt() {
        Call call = new Call(null, "toInt",
                java.util.List.of(new CharLiteral('A')));
        Optional<Expr> result = NativeRuleRegistry.eval(call);
        assertThat(result).contains(new IntLiteral(65));
    }

    @Test
    void explodeStringToList() {
        Call explodeCall = new Call(null, "explode", List.of(new StringLiteral("hi")));
        Optional<Expr> resultOpt = NativeRuleRegistry.eval(explodeCall);
        Expr expected = new Call(null, "Cons", List.of(
                new CharLiteral('h'),
                new Call(null, "Cons", List.of(
                        new CharLiteral('i'),
                        new Call(null, "Nil", List.of())
                ))
        ));
        assertThat(resultOpt).contains(expected);
    }
}
