package parser;

import ast.*;
import lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ParserTest {

    private List<TopLevelItem> parse(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        return parser.parse();
    }

    @Test
    void parsesIntegerLiteral() {
        List<TopLevelItem> result = parse("42");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst())
                .isInstanceOf(IntLiteral.class);
        assertThat(((IntLiteral) result.getFirst()).value()).isEqualTo(42);
    }

    @Test
    void parsesBinaryExpression() {
        List<TopLevelItem> result = parse("1 + 2 * 3");
        assertThat(result).hasSize(1);

        Expr expr = (Expr) result.getFirst();
        assertThat(expr).isInstanceOf(BinaryOp.class);

        BinaryOp add = (BinaryOp) expr;
        assertThat(add.op()).isEqualTo(Operator.ADD);
        assertThat(add.left()).isInstanceOf(IntLiteral.class);
        assertThat(((IntLiteral) add.left()).value()).isEqualTo(1);

        assertThat(add.right()).isInstanceOf(BinaryOp.class);
        BinaryOp mul = (BinaryOp) add.right();
        assertThat(mul.op()).isEqualTo(Operator.MUL);
        assertThat(((IntLiteral) mul.left()).value()).isEqualTo(2);
        assertThat(((IntLiteral) mul.right()).value()).isEqualTo(3);
    }

    @Test
    void parsesParentheses() {
        List<TopLevelItem> result = parse("(1 + 2) * 3");
        Expr expr = (Expr) result.getFirst();

        assertThat(expr).isInstanceOf(BinaryOp.class);
        BinaryOp mul = (BinaryOp) expr;
        assertThat(mul.op()).isEqualTo(Operator.MUL);

        assertThat(mul.left()).isInstanceOf(BinaryOp.class);
        BinaryOp add = (BinaryOp) mul.left();
        assertThat(add.op()).isEqualTo(Operator.ADD);
        assertThat(((IntLiteral) add.left()).value()).isEqualTo(1);
        assertThat(((IntLiteral) add.right()).value()).isEqualTo(2);

        assertThat(((IntLiteral) mul.right()).value()).isEqualTo(3);
    }

    @Test
    void parsesSimpleCall() {
        List<TopLevelItem> result = parse("foo(1, 2)");
        Expr expr = (Expr) result.getFirst();

        assertThat(expr).isInstanceOf(Call.class);
        Call call = (Call) expr;
        assertThat(call.namespace()).isNull();
        assertThat(call.function()).isEqualTo("foo");
        assertThat(call.arguments()).hasSize(2);
    }

    @Test
    void parsesNamespacedCall() {
        List<TopLevelItem> result = parse("math.add(1, 2)");
        Expr expr = (Expr) result.getFirst();

        assertThat(expr).isInstanceOf(Call.class);
        Call call = (Call) expr;
        assertThat(call.namespace()).isEqualTo("math");
        assertThat(call.function()).isEqualTo("add");
        assertThat(call.arguments()).hasSize(2);
    }

    @Test
    void parsesVariable() {
        List<TopLevelItem> result = parse("x");
        Expr expr = (Expr) result.getFirst();

        assertThat(expr).isInstanceOf(Var.class);
        Var var = (Var) expr;
        assertThat(var.name()).isEqualTo("x");
    }

    @Test
    void parsesBooleanLiteral() {
        List<TopLevelItem> result = parse("true");
        Expr expr = (Expr) result.getFirst();
        assertThat(expr).isInstanceOf(BoolLiteral.class);
        assertThat(((BoolLiteral) expr).value()).isTrue();
    }

    @Test
    void parsesStringLiteral() {
        List<TopLevelItem> result = parse("\"hello\"");
        Expr expr = (Expr) result.getFirst();
        assertThat(expr).isInstanceOf(StringLiteral.class);
        assertThat(((StringLiteral) expr).value()).isEqualTo("hello");
    }

    @Test
    void parsesCharLiteral() {
        List<TopLevelItem> result = parse("'c'");
        Expr expr = (Expr) result.getFirst();
        assertThat(expr).isInstanceOf(CharLiteral.class);
        assertThat(((CharLiteral) expr).value()).isEqualTo('c');
    }

    @Test
    void parsesRuleDefinition() {
        List<TopLevelItem> result = parse("def f(x) = x + 1");
        assertThat(result).hasSize(1);

        TopLevelItem item = result.getFirst();
        assertThat(item).isInstanceOf(Rule.class);

        Rule rule = (Rule) item;
        assertThat(rule.pattern().name()).isEqualTo("f");
        assertThat(rule.pattern().arguments()).hasSize(1);
        assertThat(rule.pattern()).isInstanceOf(Pattern.class);
    }

    @Test
    void parsesImport() {
        List<TopLevelItem> result = parse("import math");
        assertThat(result).hasSize(1);

        TopLevelItem item = result.getFirst();
        assertThat(item).isInstanceOf(Import.class);
        assertThat(((Import) item).module()).isEqualTo("math");
    }

    @Test
    void parsesPatternArguments() {
        List<TopLevelItem> result = parse("def f(x) = 1");
        Rule rule = (Rule) result.getFirst();
        PatternArg arg = rule.pattern().arguments().getFirst();
        assertThat(arg).isInstanceOf(PatternVar.class);
        assertThat(((PatternVar) arg).name()).isEqualTo("x");

        result = parse("def f(42) = 1");
        rule = (Rule) result.getFirst();
        arg = rule.pattern().arguments().getFirst();
        assertThat(arg).isInstanceOf(PatternLiteral.class);
        assertThat(((PatternLiteral) arg).value()).isInstanceOf(IntLiteral.class);
        assertThat(((IntLiteral) ((PatternLiteral) arg).value()).value()).isEqualTo(42);

        result = parse("def f(_) = 1");
        rule = (Rule) result.getFirst();
        arg = rule.pattern().arguments().getFirst();
        assertThat(arg).isInstanceOf(PatternWildcard.class);
    }

    @Test
    void parsesAllComparisonOperators() {
        List<TopLevelItem> eqResult = parse("1 == 2");
        BinaryOp eq = (BinaryOp) eqResult.getFirst();
        assertThat(eq.op()).isEqualTo(Operator.EQ);
        assertThat(((IntLiteral) eq.left()).value()).isEqualTo(1);
        assertThat(((IntLiteral) eq.right()).value()).isEqualTo(2);

        List<TopLevelItem> ltResult = parse("1 < 2");
        BinaryOp lt = (BinaryOp) ltResult.getFirst();
        assertThat(lt.op()).isEqualTo(Operator.LT);
        assertThat(((IntLiteral) lt.left()).value()).isEqualTo(1);
        assertThat(((IntLiteral) lt.right()).value()).isEqualTo(2);

        List<TopLevelItem> leResult = parse("1 <= 2");
        BinaryOp le = (BinaryOp) leResult.getFirst();
        assertThat(le.op()).isEqualTo(Operator.LE);
        assertThat(((IntLiteral) le.left()).value()).isEqualTo(1);
        assertThat(((IntLiteral) le.right()).value()).isEqualTo(2);

        List<TopLevelItem> gtResult = parse("1 > 2");
        BinaryOp gt = (BinaryOp) gtResult.getFirst();
        assertThat(gt.op()).isEqualTo(Operator.GT);
        assertThat(((IntLiteral) gt.left()).value()).isEqualTo(1);
        assertThat(((IntLiteral) gt.right()).value()).isEqualTo(2);

        List<TopLevelItem> geResult = parse("1 >= 2");
        BinaryOp ge = (BinaryOp) geResult.getFirst();
        assertThat(ge.op()).isEqualTo(Operator.GE);
        assertThat(((IntLiteral) ge.left()).value()).isEqualTo(1);
        assertThat(((IntLiteral) ge.right()).value()).isEqualTo(2);

        List<TopLevelItem> nqResult = parse("1 != 2");
        BinaryOp nq = (BinaryOp) nqResult.getFirst();
        assertThat(nq.op()).isEqualTo(Operator.NQ);
        assertThat(((IntLiteral) nq.left()).value()).isEqualTo(1);
        assertThat(((IntLiteral) nq.right()).value()).isEqualTo(2);
    }

    @Test
    void parsesComparisonExpression() {
        List<TopLevelItem> result = parse("1 + 2 == 3");
        assertThat(result).hasSize(1);

        Expr expr = (Expr) result.getFirst();
        assertThat(expr).isInstanceOf(BinaryOp.class);
        BinaryOp eq = (BinaryOp) expr;
        assertThat(eq.op()).isEqualTo(Operator.EQ);

        assertThat(eq.left()).isInstanceOf(BinaryOp.class);
        BinaryOp add = (BinaryOp) eq.left();
        assertThat(add.op()).isEqualTo(Operator.ADD);
        assertThat(((IntLiteral) add.left()).value()).isEqualTo(1);
        assertThat(((IntLiteral) add.right()).value()).isEqualTo(2);

        assertThat(eq.right()).isInstanceOf(IntLiteral.class);
        assertThat(((IntLiteral) eq.right()).value()).isEqualTo(3);
    }
}
