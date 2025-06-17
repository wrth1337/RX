package ast;

public sealed interface Literal extends Expr permits BoolLiteral, IntLiteral, FloatLiteral {
}

//TODO: More literals such as strings, float, char, ...