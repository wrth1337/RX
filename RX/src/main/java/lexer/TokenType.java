package lexer;

public enum TokenType {
    IDENTIFIER("identifier"),

    //Literals
    INT_LITERAL("intLiteral"),
    FLOAT_LITERAL("floatLiteral"),

    //Aritmetics
    PLUS("+"),
    MINUS("-"),
    MULT("*"),
    DIV("/"),

    //Bool Stuff
    EQ("=="),
    GE (">="),
    LE ("<="),
    GT (">"),
    LT ("<"),
    NQ ("!="),

    LPAREN("("),
    RPAREN(")"),
    COMMA(","),

    //Keywords
    DEF("def"),
    TRUE("true"), //BoolLiteral
    FALSE("false"), //BoolLiteral

    //Rules
    ARROW("->"),
    ASSIGN("="),

    EOF("EOF"),
    ERROR("ERROR");



    public final String pattern;
    TokenType(String pattern) {
        this.pattern = pattern;
    }
}
