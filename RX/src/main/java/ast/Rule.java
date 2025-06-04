package ast;

public record Rule(Pattern pattern, Expr replacement) implements TopLevelItem {
}
