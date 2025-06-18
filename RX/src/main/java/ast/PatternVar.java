package ast;

public record PatternVar(String name, Type type) implements PatternArg {
    @Override
    public String toString(){
        return name + ": " + type;
    }
}
