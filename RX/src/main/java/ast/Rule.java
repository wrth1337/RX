package ast;

public record Rule(Pattern pattern, Expr replacement) implements TopLevelItem {
    @Override
    public String toString() {
        return pattern + " -> " + replacement;
    }
}
