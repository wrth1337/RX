package ast;

public record FloatLiteral(double value) implements Literal {
    @Override
    public String toString(){
        return String.valueOf(value);
    }
}
