package ast;

public record PatternExpr(Expr expr) implements PatternArg {
    @Override
    public String toString(){
        return expr.toString();
    }
}