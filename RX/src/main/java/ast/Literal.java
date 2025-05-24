package ast;

public sealed interface Literal extends Expr permits BoolLiteral, IntLiteral {
}
