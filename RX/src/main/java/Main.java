import ast.*;
import engine.RewriteEngine;
import eval.Evaluator;
import kernel.KernelRules;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

import java.util.List;

public class Main {
    public static void main(String[] args) {
//        RewriteEngine engine = new RewriteEngine(KernelRules.defaultRules());
//        Evaluator evaluator = new Evaluator(engine);
//
//        Expr expr = new App("add", List.of(new IntLiteral(2), new IntLiteral(3)));
//        Expr result = evaluator.evaluate(expr);
//        System.out.println("Original:" + expr);
//        System.out.println("Result:" + result + "\n");
//
//        Expr expr2 = new App("add", List.of(new App("add", List.of(new IntLiteral(2), new IntLiteral(3))), new IntLiteral(10)));
//        Expr result2 = evaluator.evaluate(expr2);
//        System.out.println("Original:" + expr2);
//        System.out.println("Result:" + result2 + "\n");
//
//        Expr expr3 = new App("eq", List.of(new IntLiteral(10), new App("mul", List.of(new IntLiteral(2), new IntLiteral(3)))));
//        Expr result3 = evaluator.evaluate(expr3);
//        System.out.println("Original:" + expr3);
//        System.out.println("Result:" + result3 + "\n");
//
//        Expr expr4 = new App("eq", List.of(new IntLiteral(10), new App("div", List.of(new IntLiteral(1000), new IntLiteral(100)))));
//        Expr result4 = evaluator.evaluate(expr4);
//        System.out.println("Original:" + expr4);
//        System.out.println("Result:" + result4 + "\n");

        String code = "def double(x) = x + x";
        Lexer lexer = new Lexer(code);
        while(true){
            Token token = lexer.nextToken();
            System.out.println(token);
            if(token.type().equals(TokenType.EOF)) break;
        }
    }
}
