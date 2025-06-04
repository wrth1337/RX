package ast;

public record IntLiteral(int value) implements Literal {
    @Override
    public String toString(){
        return String.valueOf(value);
    }
}
