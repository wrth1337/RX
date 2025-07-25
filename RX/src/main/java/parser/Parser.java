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
            } else if(match(TokenType.IMPORT)) {
                items.add(parseImport());
            } else {
                items.add(parseExpression());
            }
        }
        return items;
    }

    //Parse Imports
    private Import parseImport() {
        String module = parseIdentifier();
        return new Import(module);
    }

    //Parse Rules
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
        expect(TokenType.ASSIGN);
        Expr body = parseExpression();
        return new Rule(new Pattern(name, args), body);
    }

    private String parseIdentifier() {
        Token token = current;
        expect(TokenType.IDENTIFIER);
        return token.lexeme();
    }

    private PatternArg parsePatternArg() {
        switch (current.type()) {
            case TokenType.IDENTIFIER: {
                String name = parseIdentifier();
                String namespace = null;

                if (current.type() == TokenType.DOT) {
                    advance();
                    namespace = name;
                    name = parseIdentifier();
                }

                if (current.type() == TokenType.LPAREN) {
                    advance();
                    List<Expr> args = new ArrayList<>();
                    if (current.type() != TokenType.RPAREN) {
                        do {
                            args.add(parseExpression());
                        } while (match(TokenType.COMMA));
                    }
                    expect(TokenType.RPAREN);
                    return new PatternExpr(new Call(namespace, name, args));
                }

                return new PatternVar(name);
            }
            case TokenType.INT_LITERAL: {
                int value = Integer.parseInt(current.lexeme());
                advance();
                return new PatternLiteral(new IntLiteral(value));
            }
            case TokenType.FLOAT_LITERAL: {
                double value = Double.parseDouble(current.lexeme());
                advance();
                return new PatternLiteral(new FloatLiteral(value));
            }
            case TokenType.STRING_LITERAL: {
                String value = current.lexeme();
                advance();
                return new PatternLiteral(new StringLiteral(value));
            }
            case TokenType.CHAR_LITERAL: {
                char value = current.lexeme().charAt(0);
                advance();
                return new PatternLiteral(new CharLiteral(value));
            }
            case TokenType.TRUE:
            case TokenType.FALSE: {
                boolean value = Boolean.parseBoolean(current.lexeme());
                advance();
                return new PatternLiteral(new BoolLiteral(value));
            }
            case TokenType.WILDCARD: {
                advance();
                return new PatternWildcard();
            }
            default: {
                throw new RuntimeException("Invalid pattern argument: " + current);
            }
        }
    }


    //Parse Expressions
    public Expr parseExpression() {
        return parseLogical();
    }

    private Expr parseLogical() {
        Expr expr = parseComparison();

        while (true) {
            if (match(TokenType.AND)) {
                Expr right = parseComparison();
                expr = new Call(null, "and", List.of(expr, right));
            } else if (match(TokenType.OR)) {
                Expr right = parseComparison();
                expr = new Call(null, "or", List.of(expr, right));
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr parseComparison() {
        Expr expr = parseAddition();

        while (true) {
            if (match(TokenType.EQ)) {
                Expr right = parseAddition();
                expr = new Call(null, "eq", List.of(expr, right));
            } else if (match(TokenType.LT)) {
                Expr right = parseAddition();
                expr = new Call(null, "lt", List.of(expr, right));
            } else if (match(TokenType.LE)) {
                Expr right = parseAddition();
                expr = new Call(null, "le", List.of(expr, right));
            } else if (match(TokenType.GT)) {
                Expr right = parseAddition();
                expr = new Call(null, "gt", List.of(expr, right));
            } else if (match(TokenType.GE)) {
                Expr right = parseAddition();
                expr = new Call(null, "ge", List.of(expr, right));
            } else if (match(TokenType.NQ)) {
                Expr right = parseAddition();
                expr = new Call(null, "nq", List.of(expr, right));
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
                expr = new Call(null, "add", List.of(expr, parseMultiplication()));
            } else if (match(TokenType.MINUS)) {
                expr = new Call(null, "sub", List.of(expr, parseMultiplication()));
            }
            else {
                break;
            }
        }
        return expr;
    }

    private Expr parseMultiplication() {
        Expr expr = parsePrimary();
        while (true) {
            if (match(TokenType.MULT)) {
                expr = new Call(null, "mul", List.of(expr, parsePrimary()));
            } else if (match(TokenType.DIV)) {
                expr = new Call(null, "div", List.of(expr, parsePrimary()));
            } else if (match(TokenType.MOD)) {
                expr = new Call(null, "mod", List.of(expr, parsePrimary()));
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

        if(match(TokenType.STRING_LITERAL)) {
            return new StringLiteral(token.lexeme());
        }

        if(match(TokenType.CHAR_LITERAL)) {
            return new CharLiteral(token.lexeme().charAt(0));
        }

        if (match(TokenType.TRUE)) {
            return new BoolLiteral(true);
        }

        if (match(TokenType.FALSE)) {
            return new BoolLiteral(false);
        }

        if (match(TokenType.IDENTIFIER)) {
            String name = token.lexeme();
            String namespace = null;

            if (current.type() == TokenType.DOT) {
                advance();
                namespace = name;
                name = parseIdentifier();
            }
            if (match(TokenType.LPAREN)) {
                List<Expr> args = new ArrayList<>();
                if (current.type() != TokenType.RPAREN) {
                    do {
                        args.add(parseExpression());
                    } while (match(TokenType.COMMA));
                }
                expect(TokenType.RPAREN);
                return new Call(namespace, name, args);
            } else {
                return new Var(name);
            }
        }

        if (match(TokenType.LPAREN)) {
            Expr expr = parseExpression();
            expect(TokenType.RPAREN);
            return expr;
        }

        // Unary NOT
        if (match(TokenType.BANG)) {
            Expr expr = parsePrimary();
            return new Call(null, "not", List.of(expr));
        }

        // Lists
        if (match(TokenType.LBRACKET)) {
            List<Expr> elements = new ArrayList<>();
            if (current.type() != TokenType.RBRACKET) {
                do {
                    elements.add(parseExpression());
                } while (match(TokenType.COMMA));
            }
            expect(TokenType.RBRACKET);

            // Desugar to Cons/Nil
            Expr list = new Call(null, "Nil", List.of());
            for (int i = elements.size() - 1; i >= 0; i--) {
                List<Expr> args = new ArrayList<>();
                args.add(elements.get(i));
                args.add(list);
                list = new Call(null, "Cons", args);
            }
            return list;
        }

        throw new RuntimeException("Unexpected token in expression: " + current);
    }
}
