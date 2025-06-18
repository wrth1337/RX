package ast;

public record Rule(Pattern pattern, Expr replacement, Type returnType) implements TopLevelItem {
    @Override
    public String toString() {
        return pattern + ": " + returnType + " -> " + replacement;
    }
}
