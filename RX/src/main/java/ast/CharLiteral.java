package ast;

public record CharLiteral(char value) implements Literal {
    @Override
    public String toString(){
        return "'" + value + "'";
    }
}
