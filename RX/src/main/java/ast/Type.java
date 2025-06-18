package ast;

public sealed interface Type permits BaseType {
    String name();
}
