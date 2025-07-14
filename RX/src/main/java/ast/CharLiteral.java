package ast;

public record CharLiteral(char value) implements Literal {
    @Override
    public String toString(){
        return "'" + value + "'";
    }

    @Override
    public String asRawString() {
        return String.valueOf(value);
    }
}
