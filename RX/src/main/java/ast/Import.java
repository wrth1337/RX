package ast;

public record Import(String module) implements TopLevelItem{
    @Override
    public String toString(){
        return "import " + module;
    }
}
