package ast;

public sealed interface PatternArg permits PatternVar, PatternLiteral, PatternExpr, PatternWildcard {
}