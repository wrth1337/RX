package ast;

import java.util.List;

public record Pattern(String name, List<PatternArg> arguments) {
    @Override
    public String toString(){
        return name + getArgumentsString();
    }

    private String getArgumentsString(){
        StringBuilder arguments = new StringBuilder();
        arguments.append("(");
        for(PatternArg arg : this.arguments){
            arguments.append(arg.toString());
            arguments.append(", ");
        }
        if(arguments.length() > 1) arguments.delete(arguments.length() - 2, arguments.length());
        arguments.append(")");
        return arguments.toString();
    }
}
