package lexer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LexerTest {

    @Test
    void shouldRecognizeSingleCharacterTokens() {
        Lexer lexer = new Lexer("+-*/%(),_");

        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.PLUS, "+"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.MINUS, "-"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.MULT, "*"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.DIV, "/"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.MOD, "%"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.LPAREN, "("));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.RPAREN, ")"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.COMMA, ","));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.WILDCARD, "_"));
        assertThat(lexer.nextToken().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    void shouldRecognizeOperatorsAndComparison() {
        Lexer lexer = new Lexer("== != <= >= < > = ->");

        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.EQ, "=="));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.NQ, "!="));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.LE, "<="));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.GE, ">="));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.LT, "<"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.GT, ">"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.ASSIGN, "="));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.ARROW, "->"));
        assertThat(lexer.nextToken().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    void shouldRecognizeIdentifiersAndKeywords() {
        Lexer lexer = new Lexer("import def true false someVar");

        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.IMPORT, "import"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.DEF, "def"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.TRUE, "true"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.FALSE, "false"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.IDENTIFIER, "someVar"));
        assertThat(lexer.nextToken().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    void shouldRecognizeIntAndFloatLiterals() {
        Lexer lexer = new Lexer("42 3.14 .1337 99. ..1");

        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.INT_LITERAL, "42"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.FLOAT_LITERAL, "3.14"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.FLOAT_LITERAL, "0.1337"));
        Token errorToken = lexer.nextToken();
        assertThat(errorToken.type()).isEqualTo(TokenType.ERROR);
        assertThat(errorToken.lexeme()).contains("Expected digits after decimal point");
    }

    @Test
    void shouldRecognizeStringLiteral() {
        Lexer lexer = new Lexer("\"Hello, World!\"");

        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.STRING_LITERAL, "Hello, World!"));
        assertThat(lexer.nextToken().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    void shouldReturnErrorForUnterminatedString() {
        Lexer lexer = new Lexer("\"Unclosed string");

        Token token = lexer.nextToken();
        assertThat(token.type()).isEqualTo(TokenType.ERROR);
        assertThat(token.lexeme()).contains("Unterminated string literal");
    }

    @Test
    void shouldRecognizeCharLiteral() {
        Lexer lexer = new Lexer("'a'");

        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.CHAR_LITERAL, "a"));
        assertThat(lexer.nextToken().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    void shouldReturnErrorForUnterminatedCharLiteral() {
        Lexer lexer = new Lexer("'b");

        Token token = lexer.nextToken();
        assertThat(token.type()).isEqualTo(TokenType.ERROR);
        assertThat(token.lexeme()).contains("Unterminated character literal");
    }

    @Test
    void shouldSkipWhitespaceAndComments() {
        Lexer lexer = new Lexer("   42   // this is a comment\n  +");

        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.INT_LITERAL, "42"));
        assertThat(lexer.nextToken()).isEqualTo(new Token(TokenType.PLUS, "+"));
        assertThat(lexer.nextToken().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    void shouldReturnErrorForUnknownCharacter() {
        Lexer lexer = new Lexer("@");

        Token token = lexer.nextToken();
        assertThat(token.type()).isEqualTo(TokenType.ERROR);
        assertThat(token.lexeme()).contains("@");
    }
}
