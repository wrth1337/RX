package ast;

import java.util.List;

public record Pattern(String name, List<PatternArg> arguments) {
    @Override
    public String toString(){
        return name + arguments.toString();
    }
}
