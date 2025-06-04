package ast;

public sealed interface Literal extends Expr permits BoolLiteral, IntLiteral {
}

//TODO: More literals such as strings, float, char, ...