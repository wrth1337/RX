package ast;

public record PatternVar(String name) implements PatternArg {
    @Override
    public String toString(){
        return name;
    }
}
