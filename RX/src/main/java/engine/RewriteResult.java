package engine;

import ast.Expr;
import ast.Rule;

public record RewriteResult(Expr result, Rule rule) {
}
