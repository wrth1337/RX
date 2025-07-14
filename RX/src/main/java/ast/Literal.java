package ast;

public sealed interface Literal extends Expr permits BoolLiteral, IntLiteral, FloatLiteral, StringLiteral, CharLiteral {
    String asRawString();
}