package ast;

public sealed interface Expr extends TopLevelItem permits Call, BinaryOp, Literal, Var {
}
