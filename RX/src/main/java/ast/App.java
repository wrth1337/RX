package ast;

import java.util.List;

public record App(String function, List<Expr> arguments) implements Expr {
}
