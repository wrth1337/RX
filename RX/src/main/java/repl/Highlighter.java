package repl;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

public class Highlighter {
    private static final String RESET      = "\u001B[0m";
    private static final String RED = "\u001B[0;31m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String WHITE = "\u001B[37m";

    private static final String[] BRACKET_COLORS = {
            "\u001B[38;2;189;147;249m", // Purple
            "\u001B[38;2;139;233;253m", // Cyan
            "\u001B[38;2;255;184;108m", // Orange
            "\u001B[38;2;80;250;123m",  // Green
            "\u001B[38;2;241;250;140m", // Yellow
            "\u001B[38;2;255;121;198m"  // Pink
    };

    public static String highlight(String input) {
        Lexer lexer = new Lexer(input);
        StringBuilder sb = new StringBuilder();
        int parenDepth = 0;

        while (true) {
            Token token = lexer.nextToken();
            if (token.type() == TokenType.EOF) break;

            String lexeme = token.lexeme();

            switch(token.type()) {
                case STRING_LITERAL:
                    sb.append(GREEN)
                            .append("\"").append(lexeme).append("\"")
                            .append(RESET);
                    break;
                case CHAR_LITERAL:
                    sb.append(GREEN)
                            .append("'").append(lexeme).append("'")
                            .append(RESET);
                    break;
                case LPAREN:
                    sb.append(BRACKET_COLORS[parenDepth % BRACKET_COLORS.length])
                            .append(lexeme).append(RESET);
                    parenDepth++;
                    break;
                case RPAREN:
                    parenDepth--;
                    sb.append(BRACKET_COLORS[Math.floorMod(parenDepth, BRACKET_COLORS.length)])
                            .append(lexeme).append(RESET);
                    break;
                case IDENTIFIER:
                    sb.append(WHITE).append(lexeme).append(RESET);
                    break;
                case INT_LITERAL:
                    sb.append(GREEN).append(lexeme).append(RESET);
                    break;
                case FLOAT_LITERAL:
                    sb.append(GREEN).append(lexeme).append(RESET);
                    break;
                case TRUE: case FALSE:
                    sb.append(GREEN).append(lexeme).append(RESET);
                    break;
                case PLUS: case MINUS: case MULT: case DIV: case MOD:
                    sb.append(GREEN).append(lexeme).append(RESET);
                    break;
                case EQ: case GE: case LE: case GT: case LT: case NQ:
                    sb.append(BLUE).append(lexeme).append(RESET);
                    break;
                case IMPORT: case DEF:
                    sb.append(BLUE).append(lexeme).append(RESET);
                    break;
                case COMMA:
                    sb.append(WHITE).append(lexeme).append(RESET);
                    break;
                case WILDCARD:
                    sb.append(GREEN).append(lexeme).append(RESET);
                    break;
                case ARROW: case ASSIGN:
                    sb.append(BLUE).append(lexeme).append(RESET);
                    break;
                case ERROR:
                    sb.append(RED).append(lexeme).append(RESET);
                    break;
                default:
                    sb.append(WHITE).append(lexeme).append(RESET);
            }
        }
        return sb.toString();
    }
}