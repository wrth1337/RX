package ast;

public record PatternWildcard() implements PatternArg{
    @Override
    public String toString(){
        return "_";
    }
}
