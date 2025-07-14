package ast;

public record StringLiteral(String value) implements Literal {
    @Override
    public String toString(){
        return "\""+value+"\"";
    }

    @Override
    public String asRawString() {
        return value;
    }
}
