package ast;

public enum BaseType implements Type{
    INT("Int"),
    FLOAT("Float"),
    BOOL("Bool");
    
    private final String name;

    BaseType(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
