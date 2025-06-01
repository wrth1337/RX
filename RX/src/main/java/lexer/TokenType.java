package lexer;

public enum TokenType {
    IDENTIFIER("identifier"),
    INT_LITERAL("intLiteral"),

    //Aritmetics
    PLUS("+"),
    MINUS("-"),
    MULT("*"),
    DIV("/"),

    //Bool Stuff
    EQ("=="),

    LPAREN("("),
    RPAREN(")"),
    COMMA(","),

    //Keywords
    // Schlüsselwörter
    DEF("def"),
    TRUE("true"),
    FALSE("false"),

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
