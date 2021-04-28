package com.company;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
       // checkAST();

        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
    }


    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parseX();

        // Stop if there was a syntax error.
        if (hadError) return;
        if (hadRuntimeError) System.exit(70);
       // System.out.println(new AstPrinter().print(expression));  // For now, just print the tokens.
        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

     static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            Lox.report(token.line, " at end", message);
        } else {
            Lox.report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void report(int line, String where,
                               String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    private static void report(Token token, String where,
                               String message) {
        System.err.println(
                "[token " + token.literal + "] Error" + where + ": " + message);
        hadError = true;
    }

    private static void checkAST(){
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));

    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
