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
        checkEOL();
        return new Statement.LetStmt(name, value);
    }

    Statement parseStatement() {
        if (next(TokenType.IF)) return parseIfStmt();
        if (next(TokenType.ELSE))
            throw error(peek(), "Expected valid 'if' before 'else'");
        if (next(TokenType.PRINT)) return parsePrintStmt();
        if (next(TokenType.L_BRACE)) return new Statement.Block(parseBlock());
        return parseExprStmt();
    }

    List<Statement> parseBlock() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.R_BRACE) && !atEnd()) {
            statements.add(parseDeclaration());
        }
        if (prev().type != TokenType.R_BRACE)
            consume(TokenType.R_BRACE, "Expected closing brace }");
        checkEOL();
        return statements;
    }

    Statement parseIfStmt() {
        Expression condition = parseExpression();
        consume(TokenType.THEN, "Expected 'then' after 'if'");
        Statement thenBranch = parseStatement();
        Statement elseBranch = null;
        if (next(TokenType.ELSE))
            elseBranch = parseStatement();
        return new Statement.IfStmt(condition, thenBranch, elseBranch);
    }

    Statement parsePrintStmt() {
        Expression value = parseExpression();
        checkEOL();
        return new Statement.PrintStmt(value);
    }

    Statement parseExprStmt() {
        Expression value = parseExpression();
        checkEOL();
        return new Statement.ExprStmt(value);
    }

    Expression parseExpression() {
        return parseAssignment();
    }

    Expression parseAssignment() {
        Expression expr = parseEquality();
        if (next(TokenType.EQ)) {
            Token eq = prev();
            Expression value = parseAssignment();
            if (expr instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expr).name;
                return new Expression.Assignment(name, value);
            }
            error(eq, "Invalid assignment target");
        }
        return expr;
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

    void checkEOL() {
        if (check(TokenType.EOL)) {
            advance();
            return;
        }
        if (check(TokenType.R_BRACE)) {
            advance();
            return;
        }
        if (check(TokenType.EOF)) {
            advance();
            return;
        }
        if (prev().type == TokenType.R_BRACE) return;
        throw error(peek(), "Expected end of line or closing brace");
    }

    Token advance() {
        if (!atEnd()) current++;
        return prev();
    }

    boolean check(TokenType type) {
        if (atEnd()) {
            if (type != TokenType.EOL) return false;
            else return true;
        }
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
