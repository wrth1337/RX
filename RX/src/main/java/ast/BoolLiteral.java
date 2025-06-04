package ast;

public record BoolLiteral(boolean value) implements Literal {
    @Override
    public String toString(){
        return String.valueOf(value);
    }
}
