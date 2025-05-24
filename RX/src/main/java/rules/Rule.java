package rules;

import ast.Expr;

public record Rule(Pattern pattern, Expr replacement) {
}
