package lexer;

public class Lexer {
    private final String code;
    private int position = 0;
    private char currentChar;

    private static final char EOF = '\0';

    public Lexer(String code) {
        this.code = code;
        this.currentChar = readChar();
    }

    public Token nextToken() {
        skipWhitespace();

        // EOF
        if (currentChar == EOF) {
            return new Token(TokenType.EOF, "EOF");
        }

        switch (currentChar) {
            case '+': nextChar(); return new Token(TokenType.PLUS, "+");
            case '-': {
                if (peekChar() == '>') {
                    nextChar(); nextChar();
                    return new Token(TokenType.ARROW, "->");
                } else {
                    nextChar();
                    return new Token(TokenType.MINUS, "-");
                }
            }
            case '*': nextChar(); return new Token(TokenType.MULT, "*");
            case '/': nextChar(); return new Token(TokenType.DIV, "/");
            case '(': nextChar(); return new Token(TokenType.LPAREN, "(");
            case ')': nextChar(); return new Token(TokenType.RPAREN, ")");
            case ',': nextChar(); return new Token(TokenType.COMMA, ",");
            case '=': {
                nextChar();
                if (currentChar == '=') {
                    nextChar();
                    return new Token(TokenType.EQ, "==");
                } else {
                    return new Token(TokenType.ASSIGN, "=");
                }
            }
        }

        // Int Literal
        if (Character.isDigit(currentChar)) {
            return new Token(TokenType.INT_LITERAL, readIntLiteral());
        }

        // Identifier
        if (Character.isLetter(currentChar)) {
            String ident = readIdentifier();
            return switch (ident) {
                case "def" -> new Token(TokenType.DEF, "def");
                case "true" -> new Token(TokenType.TRUE, "true");
                case "false" -> new Token(TokenType.FALSE, "false");
                default -> new Token(TokenType.IDENTIFIER, ident);
            };
        }

        // Unknown symbol
        char unknown = currentChar;
        nextChar();
        return new Token(TokenType.ERROR, String.valueOf(unknown));
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(currentChar)) {
            nextChar();
        }
    }

    private void nextChar() {
        if (position < code.length()) {
            currentChar = code.charAt(position++);
        } else {
            currentChar = EOF;
        }
    }

    private char peekChar() {
        if (position < code.length()) {
            return code.charAt(position);
        }
        return EOF;
    }

    private char readChar() {
        if (position < code.length()) {
            return code.charAt(position++);
        } else {
            return EOF;
        }
    }

    private String readIntLiteral() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(currentChar)) {
            sb.append(currentChar);
            nextChar();
        }
        return sb.toString();
    }

    private String readIdentifier() {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(currentChar)) {
            sb.append(currentChar);
            nextChar();
        }
        return sb.toString();
    }
}
