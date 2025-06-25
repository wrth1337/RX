package lexer;

public enum TokenType {
    IDENTIFIER("identifier"),

    //Literals
    INT_LITERAL("intLiteral"),
    FLOAT_LITERAL("floatLiteral"),
    STRING_LITERAL("stringLiteral"),
    CHAR_LITERAL("charLiteral"),

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
    WILDCARD("_"),

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
