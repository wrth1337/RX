package parser;

import ast.*;
import lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
        assertThat(expr).isInstanceOf(Call.class);

        Call add = (Call) expr;
        assertThat(add.function()).isEqualTo("add");
        assertThat(add.arguments().getFirst()).isInstanceOf(IntLiteral.class);
        assertThat(((IntLiteral) add.arguments().get(0)).value()).isEqualTo(1);

        assertThat(add.arguments().get(1)).isInstanceOf(Call.class);
        Call mul = (Call) add.arguments().get(1);
        assertThat(mul.function()).isEqualTo("mul");
        assertThat(((IntLiteral) mul.arguments().get(0)).value()).isEqualTo(2);
        assertThat(((IntLiteral) mul.arguments().get(1)).value()).isEqualTo(3);


    }

    @Test
    void parsesParentheses() {
        List<TopLevelItem> result = parse("(1 + 2) * 3");
        Expr expr = (Expr) result.getFirst();

        assertThat(expr).isInstanceOf(Call.class);
        Call mul = (Call) expr;
        assertThat(mul.function()).isEqualTo("mul");

        assertThat(mul.arguments().getFirst()).isInstanceOf(Call.class);
        Call add = (Call) mul.arguments().getFirst();
        assertThat(add.function()).isEqualTo("add");
        assertThat(((IntLiteral) add.arguments().get(0)).value()).isEqualTo(1);
        assertThat(((IntLiteral) add.arguments().get(1)).value()).isEqualTo(2);

        assertThat(((IntLiteral) mul.arguments().get(1)).value()).isEqualTo(3);
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
        Call eq = (Call) eqResult.getFirst();
        assertThat(eq.function()).isEqualTo("eq");
        assertThat(eq.arguments()).hasSize(2);
        assertThat(((IntLiteral) eq.arguments().get(0)).value()).isEqualTo(1);
        assertThat(((IntLiteral) eq.arguments().get(1)).value()).isEqualTo(2);

        List<TopLevelItem> ltResult = parse("1 < 2");
        Call lt = (Call) ltResult.getFirst();
        assertThat(lt.function()).isEqualTo("lt");
        assertThat(((IntLiteral) lt.arguments().get(0)).value()).isEqualTo(1);
        assertThat(((IntLiteral) lt.arguments().get(1)).value()).isEqualTo(2);

        List<TopLevelItem> leResult = parse("1 <= 2");
        Call le = (Call) leResult.getFirst();
        assertThat(le.function()).isEqualTo("le");
        assertThat(((IntLiteral) le.arguments().get(0)).value()).isEqualTo(1);
        assertThat(((IntLiteral) le.arguments().get(1)).value()).isEqualTo(2);

        List<TopLevelItem> gtResult = parse("1 > 2");
        Call gt = (Call) gtResult.getFirst();
        assertThat(gt.function()).isEqualTo("gt");
        assertThat(((IntLiteral) gt.arguments().get(0)).value()).isEqualTo(1);
        assertThat(((IntLiteral) gt.arguments().get(1)).value()).isEqualTo(2);

        List<TopLevelItem> geResult = parse("1 >= 2");
        Call ge = (Call) geResult.getFirst();
        assertThat(ge.function()).isEqualTo("ge");
        assertThat(((IntLiteral) ge.arguments().get(0)).value()).isEqualTo(1);
        assertThat(((IntLiteral) ge.arguments().get(1)).value()).isEqualTo(2);

        List<TopLevelItem> nqResult = parse("1 != 2");
        Call nq = (Call) nqResult.getFirst();
        assertThat(nq.function()).isEqualTo("nq");
        assertThat(((IntLiteral) nq.arguments().get(0)).value()).isEqualTo(1);
        assertThat(((IntLiteral) nq.arguments().get(1)).value()).isEqualTo(2);
    }

    @Test
    void parsesComparisonExpression() {
        List<TopLevelItem> result = parse("1 + 2 == 3");
        assertThat(result).hasSize(1);

        Expr expr = (Expr) result.getFirst();
        assertThat(expr).isInstanceOf(Call.class);
        Call eq = (Call) expr;
        assertThat(eq.function()).isEqualTo("eq");

        assertThat(eq).isInstanceOf(Call.class);
        Call add = (Call) eq.arguments().getFirst();
        assertThat(add.function()).isEqualTo("add");
        assertThat(((IntLiteral) add.arguments().get(0)).value()).isEqualTo(1);
        assertThat(((IntLiteral) add.arguments().get(1)).value()).isEqualTo(2);
    }

    @Test
    void parsesListLiteral() {
        List<TopLevelItem> result = parse("[1, 2, 3]");
        assertThat(result).hasSize(1);

        Expr expr = (Expr) result.getFirst();
        assertThat(expr).isInstanceOf(Call.class);

        Call cons1 = (Call) expr;
        assertThat(cons1.function()).isEqualTo("Cons");
        assertThat(((IntLiteral) cons1.arguments().get(0)).value()).isEqualTo(1);

        Call cons2 = (Call) cons1.arguments().get(1);
        assertThat(cons2.function()).isEqualTo("Cons");
        assertThat(((IntLiteral) cons2.arguments().get(0)).value()).isEqualTo(2);

        Call cons3 = (Call) cons2.arguments().get(1);
        assertThat(cons3.function()).isEqualTo("Cons");
        assertThat(((IntLiteral) cons3.arguments().get(0)).value()).isEqualTo(3);

        Call nil = (Call) cons3.arguments().get(1);
        assertThat(nil.function()).isEqualTo("Nil");
        assertThat(nil.arguments()).isEmpty();
    }

    @Test
    void parsesMixedListLiteralWithExpressions() {
        List<TopLevelItem> result = parse("[420, 13.37, 1337, 1, 2, 3, 5 + 1, 5 + 2.5, 2.5 * 3]");
        assertThat(result).hasSize(1);

        Expr list = (Expr) result.getFirst();
        assertThat(list).isInstanceOf(Call.class);

        List<Expr> elements = new ArrayList<>();
        while (list instanceof Call cons && cons.function().equals("Cons")) {
            elements.add(cons.arguments().get(0));
            list = cons.arguments().get(1);
        }

        assertThat(list).isInstanceOf(Call.class);
        assert list instanceof Call;
        assertThat(((Call) list).function()).isEqualTo("Nil");

        assertThat(elements).hasSize(9);

        assertThat(elements.get(0)).isInstanceOf(IntLiteral.class);
        assertThat(((IntLiteral) elements.get(0)).value()).isEqualTo(420);

        assertThat(elements.get(1)).isInstanceOf(FloatLiteral.class);
        assertThat(((FloatLiteral) elements.get(1)).value()).isEqualTo(13.37);

        assertThat(elements.get(2)).isInstanceOf(IntLiteral.class);
        assertThat(((IntLiteral) elements.get(2)).value()).isEqualTo(1337);

        assertThat(((IntLiteral) elements.get(3)).value()).isEqualTo(1);
        assertThat(((IntLiteral) elements.get(4)).value()).isEqualTo(2);
        assertThat(((IntLiteral) elements.get(5)).value()).isEqualTo(3);

        assertThat(elements.get(6)).isInstanceOf(Call.class);
        Call add1 = (Call) elements.get(6);
        assertThat(add1.function()).isEqualTo("add");
        assertThat(((IntLiteral) add1.arguments().get(0)).value()).isEqualTo(5);
        assertThat(((IntLiteral) add1.arguments().get(1)).value()).isEqualTo(1);

        Call add2 = (Call) elements.get(7);
        assertThat(add2.function()).isEqualTo("add");
        assertThat(((IntLiteral) add2.arguments().get(0)).value()).isEqualTo(5);
        assertThat(((FloatLiteral) add2.arguments().get(1)).value()).isEqualTo(2.5);

        Call mul = (Call) elements.get(8);
        assertThat(mul.function()).isEqualTo("mul");
        assertThat(((FloatLiteral) mul.arguments().get(0)).value()).isEqualTo(2.5);
        assertThat(((IntLiteral) mul.arguments().get(1)).value()).isEqualTo(3);
    }

}
