package lexer;

public class Lexer {
    private final String code;
    private int position = 0;
    private char currentChar;

    private static final char EOF = '\0';

    public Lexer(String code) {
        this.code = code;
        nextChar();
    }

    public Token nextToken() {
        skipWhitespaceAndComments();

        if (currentChar == EOF) {
            return new Token(TokenType.EOF, "EOF");
        }

        switch (currentChar) {
            case '+': nextChar(); return new Token(TokenType.PLUS, "+");
            case '-': {
                //TODO: maybe useful for later... maybe not...
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
            case '!': {
                nextChar();
                if (currentChar == '=') {
                    nextChar();
                    return new Token(TokenType.NQ, "!=");
                } else {
                    return new Token(TokenType.ERROR, "!");
                }
            }
            case '<': {
                nextChar();
                if (currentChar == '=') {
                    nextChar();
                    return new Token(TokenType.LE, "<=");
                } else {
                    return new Token(TokenType.LT, "<");
                }
            }
            case '>': {
                nextChar();
                if (currentChar == '=') {
                    nextChar();
                    return new Token(TokenType.GE, ">=");
                } else {
                    return new Token(TokenType.GT, ">");
                }
            }

        }

        // Number Literal (Int or Float)
        if (Character.isDigit(currentChar) || (currentChar == '.' && Character.isDigit(peekChar()))) {
            return readNumberLiteral();
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

    private void skipWhitespaceAndComments() {
        boolean skipping = true;

        while (skipping) {
            skipping = false;

            //Whitespace
            while (Character.isWhitespace(currentChar)) {
                nextChar();
                skipping = true;
            }

            //Comments
            if (currentChar == '/' && peekChar() == '/') {
                nextChar();
                nextChar();

                while (currentChar != '\n' && currentChar != EOF) {
                    nextChar();
                }

                skipping = true;
            }
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

    private String readIdentifier() {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(currentChar)) {
            sb.append(currentChar);
            nextChar();
        }
        return sb.toString();
    }

    private Token readNumberLiteral() {
        StringBuilder sb = new StringBuilder();
        boolean seenDot = false;

        // Leading dot: .1337 -> 0.1337
        if (currentChar == '.') {
            seenDot = true;
            sb.append('0');
            sb.append(currentChar);
            nextChar();
        }

        while (Character.isDigit(currentChar)) {
            sb.append(currentChar);
            nextChar();
        }

        if (currentChar == '.') {
            if(seenDot) {
                return new Token(TokenType.ERROR, "Invalid float literal");
            }

            seenDot = true;
            sb.append(currentChar);
            nextChar();

            if (!Character.isDigit(currentChar)) {
                return new Token(TokenType.ERROR, "Expected digits after decimal point");
            }

            while (Character.isDigit(currentChar)) {
                sb.append(currentChar);
                nextChar();
            }
        }

        return new Token(
                seenDot ? TokenType.FLOAT_LITERAL : TokenType.INT_LITERAL,
                sb.toString()
        );
    }
}
