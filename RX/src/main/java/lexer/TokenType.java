package lexer;

public enum TokenType {
    IDENTIFIER("identifier"),

    INT_LITERAL("intLiteral"),
    BOOL_LITERAL("boolLiteral"),

    PLUS("+"),
    MINUS("-"),
    MULT("*"),
    DIV("/"),

    EQ("=="),

    LPAREN("("),
    RPAREN(")");



    public final String pattern;
    TokenType(String pattern) {
        this.pattern = pattern;
    }
}
