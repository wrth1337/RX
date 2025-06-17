package ast;

public record BinaryOp(Expr left, Operator op, Expr right) implements Expr {
    @Override
    public String toString(){
        return op + "(" + left + ", " + right + ")";
    }
}
