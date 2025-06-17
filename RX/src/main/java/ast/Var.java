package ast;

public record Var(String name) implements Expr {
    @Override
    public String toString(){
        return name;
    }
}
