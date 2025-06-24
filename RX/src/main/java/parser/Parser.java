package parser;

import ast.*;
import lexer.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Token current;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.current = lexer.nextToken();
    }

    private void advance() {
        current = lexer.nextToken();
    }

    private boolean match(TokenType type) {
        if (current.type() == type) {
            advance();
            return true;
        }
        return false;
    }

    private void expect(TokenType type) {
        if (!match(type)) {
            throw new RuntimeException("Expected " + type + ", got " + current);
        }
    }

    public List<TopLevelItem> parse() {
        List<TopLevelItem> items = new ArrayList<>();
        while (current.type() != TokenType.EOF) {
            if (match(TokenType.DEF)) {
                items.add(parseDefinition());
            } else {
                items.add(parseExpression());
            }
        }
        return items;
    }


    //Parse rules
    private Rule parseDefinition() {
        String name = parseIdentifier();
        expect(TokenType.LPAREN);
        List<PatternArg> args = new ArrayList<>();
        if (current.type() != TokenType.RPAREN) {
            do {
                args.add(parsePatternArg());
            } while (match(TokenType.COMMA));
        }
        expect(TokenType.RPAREN);

        Type returnType = null;
        if (match(TokenType.COLON)) {
            returnType = parseType();
        }

        expect(TokenType.ASSIGN);
        Expr body = parseExpression();
        return new Rule(new Pattern(name, args), body, returnType);
    }

    private String parseIdentifier() {
        Token token = current;
        expect(TokenType.IDENTIFIER);
        return token.lexeme();
    }

    private PatternArg parsePatternArg() {
        if (current.type() == TokenType.IDENTIFIER) {
            String name = parseIdentifier();
            Type type = null;

            if (match(TokenType.COLON)) {
                type = parseType();
            }

            return new PatternVar(name, type);
        } else if (current.type() == TokenType.INT_LITERAL) {
            int value = Integer.parseInt(current.lexeme());
            advance();
            return new PatternLiteral(new IntLiteral(value));
        } else if (current.type() == TokenType.FLOAT_LITERAL) {
            double value = Double.parseDouble(current.lexeme());
            advance();
            return new PatternLiteral(new FloatLiteral(value));
        } else if (current.type() == TokenType.TRUE || current.type() == TokenType.FALSE) {
            boolean value = Boolean.parseBoolean(current.lexeme());
            advance();
            return new PatternLiteral(new BoolLiteral(value));
        } else {
            return new PatternExpr(parseExpression());
        }
    }

    private Type parseType() {
        return switch (current.type()) {
            case INT -> { advance(); yield BaseType.INT; }
            case FLOAT -> { advance(); yield BaseType.FLOAT; }
            case BOOL -> { advance(); yield BaseType.BOOL; }
            //TODO: Add better error handling for invalid types
            default -> throw new RuntimeException("Expected a type (int, float, or bool), but found: " + current.lexeme());
        };
    }



    //Parse expressions
    public Expr parseExpression() {
        return parseComparison();
    }

    private Expr parseComparison() {
        Expr expr = parseAddition();

        while (true) {
            if (match(TokenType.EQ)) {
                Expr right = parseAddition();
                expr = new BinaryOp(expr, Operator.EQ, right);
            } else if (match(TokenType.LT)) {
                Expr right = parseAddition();
                expr = new BinaryOp(expr, Operator.LT, right);
            } else if (match(TokenType.LE)) {
                Expr right = parseAddition();
                expr = new BinaryOp(expr, Operator.LE, right);
            } else if (match(TokenType.GT)) {
                Expr right = parseAddition();
                expr = new BinaryOp(expr, Operator.GT, right);
            } else if (match(TokenType.GE)) {
                Expr right = parseAddition();
                expr = new BinaryOp(expr, Operator.GE, right);
            } else if (match(TokenType.NQ)){
                Expr right = parseAddition();
                expr = new BinaryOp(expr, Operator.NQ, right);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr parseAddition() {
        Expr expr = parseMultiplication();
        while (true) {
            if (match(TokenType.PLUS)) {
                expr = new BinaryOp(expr, Operator.ADD, parseMultiplication());
            } else if (match(TokenType.MINUS)) {
                expr = new BinaryOp(expr, Operator.SUB, parseMultiplication());
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr parseMultiplication() {
        Expr expr = parsePrimary();
        while (true) {
            if (match(TokenType.MULT)) {
                expr = new BinaryOp(expr, Operator.MUL, parsePrimary());
            } else if (match(TokenType.DIV)) {
                expr = new BinaryOp(expr, Operator.DIV, parsePrimary());
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr parsePrimary() {
        Token token = current;

        if (match(TokenType.INT_LITERAL)) {
            return new IntLiteral(Integer.parseInt(token.lexeme()));
        }

        if (match(TokenType.FLOAT_LITERAL)) {
            return new FloatLiteral(Double.parseDouble(token.lexeme()));
        }

        if (match(TokenType.TRUE)) {
            return new BoolLiteral(true);
        }

        if (match(TokenType.FALSE)) {
            return new BoolLiteral(false);
        }

        if (match(TokenType.IDENTIFIER)) {
            String name = token.lexeme();
            if (match(TokenType.LPAREN)) {
                List<Expr> args = new ArrayList<>();
                if (current.type() != TokenType.RPAREN) {
                    do {
                        args.add(parseExpression());
                    } while (match(TokenType.COMMA));
                }
                expect(TokenType.RPAREN);
                return new Call(name, args);
            } else {
                return new Var(name);
            }
        }

        if (match(TokenType.LPAREN)) {
            Expr expr = parseExpression();
            expect(TokenType.RPAREN);
            return expr;
        }

        throw new RuntimeException("Unexpected token in expression: " + current);
    }
}
