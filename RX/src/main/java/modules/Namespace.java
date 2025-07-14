package modules;

import ast.Import;
import ast.Rule;

import java.util.List;

public record Namespace(String name, List<Rule> rules, List<Import> imports) {
}
