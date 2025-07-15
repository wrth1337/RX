package eval;

import ast.*;
import engine.RewriteEngine;
import lexer.Lexer;
import modules.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EvaluatorTest {

    private Evaluator evaluator;

    @BeforeEach
    void setup() {
        String rules = "def fact(0) = 1\n" +
                "def fact(n) = n * fact(n - 1)\n" +
                "\n" +
                "def fib(0) = 0\n" +
                "def fib(1) = 1\n" +
                "def fib(n) = fib(n - 1) + fib(n - 2)" +
                "def if(true, thenBranch, _) = thenBranch\n" +
                "def if(false, _, elseBranch) = elseBranch";
        Lexer lexer = new Lexer(rules);
        Parser parser = new Parser(lexer);
        List<TopLevelItem> parsedTopLevelItems = parser.parse();
        List<Rule> rulesList = parsedTopLevelItems.stream().map(n -> (Rule) n).toList();
        Namespace prelude = new Namespace("Prelude", rulesList, List.of());
        Map<String, Namespace> modules = Map.of("Prelude", prelude);
        RewriteEngine engine = new RewriteEngine(modules);
        this.evaluator = new Evaluator(engine);
    }

    @Test
    void evaluatesAddition() {
        Call add = new Call(null, "add", List.of(
                new IntLiteral(1),
                new IntLiteral(2)
        ));
        Expr result = evaluator.evaluate(add, "Prelude");
        assertThat(result).isEqualTo(new IntLiteral(3));
    }

    @Test
    void evaluatesNestedArithmetic() {
        Call expr = new Call(null, "add", List.of(
                new Call(null, "mul", List.of(new IntLiteral(2), new IntLiteral(3))),
                new IntLiteral(4)
        ));
        Expr result = evaluator.evaluate(expr, "Prelude");
        assertThat(result).isEqualTo(new IntLiteral(10));
    }

    @Test
    void evaluatesComparison() {
        Call cmp = new Call(null, "eq", List.of(
                new IntLiteral(5),
                new IntLiteral(5)
        ));
        Expr result = evaluator.evaluate(cmp, "Prelude");
        assertThat(result).isEqualTo(new BoolLiteral(true));
    }

    @Test
    void evaluatesIfTrueBranch() {
        Call ifExpr = new Call(null, "if", List.of(
                new BoolLiteral(true),
                new IntLiteral(42),
                new IntLiteral(0)
        ));
        Expr result = evaluator.evaluate(ifExpr, "Prelude");
        assertThat(result).isEqualTo(new IntLiteral(42));
    }

    @Test
    void evaluatesIfFalseBranch() {
        Call ifExpr = new Call(null, "if", List.of(
                new BoolLiteral(false),
                new IntLiteral(42),
                new IntLiteral(0)
        ));
        Expr result = evaluator.evaluate(ifExpr, "Prelude");
        assertThat(result).isEqualTo(new IntLiteral(0));
    }

    @Test
    void evaluatesFactorial() {
        Call fact = new Call(null, "fact", List.of(new IntLiteral(5)));
        Expr result = evaluator.evaluate(fact, "Prelude");
        assertThat(result).isEqualTo(new IntLiteral(120));
    }

    @Test
    void evaluatesFibonacci() {
        Call fib = new Call(null, "fib", List.of(new IntLiteral(6)));
        Expr result = evaluator.evaluate(fib, "Prelude");
        assertThat(result).isEqualTo(new IntLiteral(8)); // fib(6) = 8
    }

    @Test
    void evaluatesCharAt() {
        StringLiteral hello = new StringLiteral("hello");
        Call charAt = new Call(null, "charAt", List.of(
                hello,
                new IntLiteral(1)
        ));
        Expr result = evaluator.evaluate(charAt, "Prelude");
        assertThat(result).isInstanceOf(CharLiteral.class);
        assertThat(((CharLiteral) result).value()).isEqualTo('e');
    }

    @Test
    void evaluatesConcat() {
        StringLiteral a = new StringLiteral("foo");
        StringLiteral b = new StringLiteral("bar");
        Call concat = new Call(null, "concat", List.of(a, b));
        Expr result = evaluator.evaluate(concat, "Prelude");
        assertThat(result).isInstanceOf(StringLiteral.class);
        assertThat(((StringLiteral) result).value()).isEqualTo("foobar");
    }

    @Test
    void evaluatesLength() {
        StringLiteral s = new StringLiteral("hello");
        Call length = new Call(null, "length", List.of(s));
        Expr result = evaluator.evaluate(length, "Prelude");
        assertThat(result).isEqualTo(new IntLiteral(5));
    }

    @Test
    void evaluatesWithTrace() {
        Call fib = new Call(null, "fib", List.of(new IntLiteral(5)));
        List<TraceEntry> trace = new ArrayList<>();
        Expr result = evaluator.evaluateWithTrace(fib, trace, "Prelude");

        assertThat(result).isEqualTo(new IntLiteral(5));

        assertThat(trace).isNotEmpty();
        for (TraceEntry entry : trace) {
            assertThat(entry.step()).isGreaterThan(0);
            assertThat(entry.expression()).isNotBlank();
            assertThat(entry.context()).isNotBlank();
            assertThat(entry.rule()).isNotBlank();
            assertThat(entry.result()).isNotBlank();
        }
    }

    @Test
    void evaluatesFizzBuzzLikeExpression() {
        String rules = """
        def if(true, thenBranch, _) = thenBranch
        def if(false, _, elseBranch) = elseBranch
        
        def fizzbuzz(n) = if(eq(mod(n, 15), 0), "FizzBuzz",
                          if(eq(mod(n, 3), 0), "Fizz",
                          if(eq(mod(n, 5), 0), "Buzz",
                          n)))
        """;

        Lexer lexer = new Lexer(rules);
        Parser parser = new Parser(lexer);
        List<TopLevelItem> parsedTopLevelItems = parser.parse();
        List<Rule> rulesList = parsedTopLevelItems.stream().map(n -> (Rule) n).toList();
        Namespace prelude = new Namespace("Prelude", rulesList, List.of());
        Map<String, Namespace> modules = Map.of("Prelude", prelude);

        RewriteEngine localEngine = new RewriteEngine(modules);
        Evaluator localEvaluator = new Evaluator(localEngine);

        Call fizzbuzz15 = new Call(null, "fizzbuzz", List.of(new IntLiteral(15)));
        Expr result15 = localEvaluator.evaluate(fizzbuzz15, "Prelude");
        assertThat(result15).isEqualTo(new StringLiteral("FizzBuzz"));

        Call fizzbuzz9 = new Call(null, "fizzbuzz", List.of(new IntLiteral(9)));
        Expr result9 = localEvaluator.evaluate(fizzbuzz9, "Prelude");
        assertThat(result9).isEqualTo(new StringLiteral("Fizz"));

        Call fizzbuzz10 = new Call(null, "fizzbuzz", List.of(new IntLiteral(10)));
        Expr result10 = localEvaluator.evaluate(fizzbuzz10, "Prelude");
        assertThat(result10).isEqualTo(new StringLiteral("Buzz"));

        Call fizzbuzz7 = new Call(null, "fizzbuzz", List.of(new IntLiteral(7)));
        Expr result7 = localEvaluator.evaluate(fizzbuzz7, "Prelude");
        assertThat(result7).isEqualTo(new IntLiteral(7));
    }

    @Test
    void testSomeLongerCalculations() {
        String expressions = """
            5+3*2-8/4+7*(6-2)
            10*(2+3)-4*(5-2)+7*2
            (15+3*4)%7+10/2
            (5-2)*3-(5-20)
            (5/2)+2.5*3
        """;
        Lexer lexer = new Lexer(expressions);
        Parser parser = new Parser(lexer);
        List<TopLevelItem> parsedTopLevelItems = parser.parse();
        List<Expr> expressionsList = parsedTopLevelItems.stream().map(n -> (Expr) n).toList();

        assertThat(this.evaluator.evaluate(expressionsList.get(0), "Prelude")).isEqualTo(new IntLiteral(37));
        assertThat(this.evaluator.evaluate(expressionsList.get(1), "Prelude")).isEqualTo(new IntLiteral(52));
        assertThat(this.evaluator.evaluate(expressionsList.get(2), "Prelude")).isEqualTo(new IntLiteral(11));
        assertThat(this.evaluator.evaluate(expressionsList.get(3), "Prelude")).isEqualTo(new IntLiteral(24));
        assertThat(this.evaluator.evaluate(expressionsList.get(4), "Prelude")).isEqualTo(new FloatLiteral(10));
    }
}
