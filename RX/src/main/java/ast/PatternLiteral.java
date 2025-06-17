package ast;

public record PatternLiteral(Literal value) implements PatternArg {
    @Override
    public String toString(){
        return value.toString();
    }
}
