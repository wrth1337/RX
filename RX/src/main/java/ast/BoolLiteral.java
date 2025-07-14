package ast;

public record BoolLiteral(boolean value) implements Literal {
    @Override
    public String toString(){
        return String.valueOf(value);
    }

    @Override
    public String asRawString() {
        return String.valueOf(value);
    }
}
