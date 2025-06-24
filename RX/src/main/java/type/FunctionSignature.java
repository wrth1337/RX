package type;

import ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record FunctionSignature(String name, List<Type> argTypes, Type returnType) {

    public static FunctionSignature from(Rule rule) {
        List<Type> argTypes = new ArrayList<>();

        for (PatternArg arg : rule.pattern().arguments()) {
            if (arg instanceof PatternVar var) {
                argTypes.add(var.type());
            } else if (arg instanceof PatternLiteral lit) {
                argTypes.add(inferLiteralType(lit.value()));
            } else {
                throw new RuntimeException("Unsupported pattern argument: " + arg);
            }
        }

        return new FunctionSignature(rule.pattern().name(), argTypes, rule.returnType());
    }

    private static BaseType inferLiteralType(Literal lit) {
        return switch (lit) {
            case IntLiteral ignored -> BaseType.INT;
            case FloatLiteral ignored -> BaseType.FLOAT;
            case BoolLiteral ignored -> BaseType.BOOL;
        };
    }

    //Dont check Return-Type
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionSignature that)) return false;
        return name.equals(that.name) &&
                argTypes.equals(that.argTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, argTypes);
    }
}
