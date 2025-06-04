package ast;

import java.util.List;

public record Call(String function, List<Expr> arguments) implements Expr {
    @Override
    public String toString(){
        return function + arguments.toString();
    }
}
