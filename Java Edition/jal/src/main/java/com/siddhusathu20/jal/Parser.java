package com.siddhusathu20.jal;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    final List<Token> tokens;
    int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!atEnd())
            statements.add(parseDeclaration());
        return statements;
    }

    Statement parseDeclaration() {
        try {
            if (next(TokenType.LET))
                return parseVarDeclaration();
            return parseStatement();
        } catch (ParseError e) {
            return null;
        }
    }

    Statement parseVarDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name");
        Expression value = null;
        if (next(TokenType.EQ))
            value = parseExpression();
        consume(TokenType.EOL, "Expected end of line after variable declaration");
        return new Statement.LetStmt(name, value);
    }

    Statement parseStatement() {
        if (next(TokenType.PRINT)) return parsePrintStmt();
        return parseExprStmt();
    }

    Statement parsePrintStmt() {
        Expression value = parseExpression();
        consume(TokenType.EOL, "Expected end of line after value");
        return new Statement.PrintStmt(value);
    }

    Statement parseExprStmt() {
        Expression value = parseExpression();
        consume(TokenType.EOL, "Expected end of line after expression");
        return new Statement.ExprStmt(value);
    }

    Expression parseExpression() {
        return parseEquality();
    }

    Expression parseEquality() {
        Expression expr = parseComparison();
        while (next(TokenType.EXCL_EQ, TokenType.DOUBLE_EQ)) {
            Token operator = prev();
            Expression right = parseComparison();
            expr = new Expression.Binary(expr, operator, right);
        }
        return expr;
    }

    Expression parseComparison() {
        Expression expr = parseTerm();
        while (next(TokenType.GT, TokenType.GT_EQ, TokenType.LT, TokenType.LT_EQ)) {
            Token operator = prev();
            Expression right = parseTerm();
            expr = new Expression.Binary(expr, operator, right);
        }
        return expr;
    }

    Expression parseTerm() {
        Expression expr = parseFactor();
        while (next(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = prev();
            Expression right = parseFactor();
            expr = new Expression.Binary(expr, operator, right);
        }
        return expr;
    }

    Expression parseFactor() {
        Expression expr = parseUnary();
        while (next(TokenType.ASTERISK, TokenType.SLASH)) {
            Token operator = prev();
            Expression right = parseUnary();
            expr = new Expression.Binary(expr, operator, right);
        }
        return expr;
    }

    Expression parseUnary() {
        if (next(TokenType.EXCL, TokenType.MINUS)) {
            Token operator = prev();
            Expression right = parseUnary();
            return new Expression.Unary(operator, right);
        }
        return parsePrimary();
    }

    Expression parsePrimary() {
        if (next(TokenType.FALSE))
            return new Expression.Literal(false);
        if (next(TokenType.TRUE))
            return new Expression.Literal(true);
        if (next(TokenType.NONE))
            return new Expression.Literal(null);
        if (next(TokenType.NUM, TokenType.STR))
            return new Expression.Literal(prev().value);
        if (next(TokenType.IDENTIFIER))
            return new Expression.Variable(prev());
        if (next(TokenType.L_BRACKET)) {
            Expression expr = parseExpression();
            consume(TokenType.R_BRACKET, "Expected closing bracket )");
            return new Expression.Group(expr);
        }
        throw error(peek(), "Expected expression");
    }

    Token consume(TokenType type, String msg) {
        if (check(type)) return advance();
        throw error(peek(), msg);
    }

    ParseError error(Token token, String msg) {
        Main.error(token.line, msg);
        return new ParseError();
    }

    boolean next(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    Token advance() {
        if (!atEnd()) current++;
        return prev();
    }

    boolean check(TokenType type) {
        if (atEnd()) return false;
        return peek().type == type;
    }

    boolean atEnd() {
        return peek().type == TokenType.EOF;
    }

    Token peek() {
        return tokens.get(current);
    }

    Token prev() {
        return tokens.get(current - 1);
    }

    private class ParseError extends RuntimeException {}
}
