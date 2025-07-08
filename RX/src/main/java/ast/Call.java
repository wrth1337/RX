package ast;

import java.util.List;

public record Call(String namespace, String function, List<Expr> arguments) implements Expr {
    @Override
    public String toString(){
        return (namespace != null ? namespace + "." : "") + function + getArgumentsString();
    }

    private String getArgumentsString(){
        StringBuilder arguments = new StringBuilder();
        arguments.append("(");
        for(Expr arg : this.arguments){
            arguments.append(arg.toString());
            arguments.append(", ");
        }
        if(arguments.length() > 1) arguments.delete(arguments.length() - 2, arguments.length());
        arguments.append(")");
        return arguments.toString();
    }
}
