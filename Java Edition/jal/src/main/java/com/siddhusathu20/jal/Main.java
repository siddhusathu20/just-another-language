package com.siddhusathu20.jal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.siddhusathu20.jal.Evaluator.RuntimeError;

public class Main {

    static boolean errored = false;
    static boolean runtimeErrored = false;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Incorrect usage!");
            System.exit(64);
        } else {
            run(args[0]);
        }
    }

    static void run(String path) throws IOException {
        String src = Files.readString(Paths.get(path));
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.scan();
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();
        if (errored) System.exit(65);
        Evaluator evaluator = new Evaluator();
        evaluator.interpret(statements);
        if (runtimeErrored) System.exit(70);
    }

    static void error(int line, String msg) {
        if (msg != null)
            System.err.println("Error at line " + line + ": " + msg);
        errored = true;
    }

    static void runtimeError(RuntimeError err) {
        if (err.token != null)
            System.err.println("Error at line " + err.token.line + ": " + err.getMessage());
        else
            System.err.println("Error: " + err.getMessage());
        runtimeErrored = true;
    }
}