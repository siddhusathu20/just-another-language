package com.siddhusathu20.jal;

import java.util.Arrays;
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
        if (next(TokenType.WHILE)) return parseWhileLoop();
        if (next(TokenType.BREAK)) return parseBreak();
        if (next(TokenType.FOR)) return parseForLoop();
        if (next(TokenType.PRINTLN)) return parsePrintlnStmt();
        if (next(TokenType.PRINT)) return parsePrintStmt();
        if (next(TokenType.INPUT)) return parseInputStmt();
        if (next(TokenType.L_BRACE)) return new Statement.Block(parseBlock());
        if (next(TokenType.EOL)) return parseStatement();
        return parseExprStmt();
    }

    List<Statement> parseBlock() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.R_BRACE) && !atEnd()) {
            statements.add(parseDeclaration());
        }
        if (prev().type != TokenType.R_BRACE) {
            consume(TokenType.R_BRACE, "Expected closing brace }");
        }
        checkEOL();
        return statements;
    }

    Statement parseWhileLoop() {
        Expression condition = parseExpression();
        consume(TokenType.DO, "Expected 'do' after 'while'");
        Statement body = parseStatement();
        return new Statement.WhileLoop(condition, body);
    }

    Statement parseBreak() {
        checkEOL();
        return new Statement.Break();
    }

    Statement parseForLoop() {
        consume(TokenType.L_BRACKET, "Expected '(' after 'for'");
        Statement init;
        if (next(TokenType.SEMICOLON)) init = null;
        else if (next(TokenType.LET)) init = parseVarDeclaration();
        else init = parseExprStmt();
        Expression condition = null;
        if (!next(TokenType.SEMICOLON))
            condition = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after for loop condition");
        Expression update = null;
        if (!next(TokenType.R_BRACKET))
            update = parseExpression();
        consume(TokenType.R_BRACKET, "Expected closing bracket )");
        Statement body = parseStatement();
        if (update != null) {
            body = new Statement.Block(
                Arrays.asList(body, new Statement.ExprStmt(update))
            );
        }
        if (condition == null)
            condition = new Expression.Literal(true);
        body = new Statement.WhileLoop(condition, body);
        if (init != null)
            body = new Statement.Block(
                Arrays.asList(init, body)
            );
        return body;
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

    Statement parsePrintlnStmt() {
        Expression value = parseExpression();
        checkEOL();
        return new Statement.PrintlnStmt(value);
    }

    Statement parseInputStmt() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name");
        checkEOL();
        return new Statement.InputStmt(name);
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
        Expression expr = parseOr();
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

    Expression parseOr() {
        Expression expr = parseAnd();
        while (next(TokenType.OR)) {
            Token operator = prev();
            Expression right = parseAnd();
            expr = new Expression.Logical(expr, operator, right);
        }
        return expr;
    }

    Expression parseAnd() {
        Expression expr = parseEquality();
        while (next(TokenType.AND)) {
            Token operator = prev();
            Expression right = parseEquality();
            expr = new Expression.Logical(expr, operator, right);
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
        if (next(TokenType.EXCL, TokenType.MINUS, TokenType.NUMOF, TokenType.STROF)) {
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
        throw error(advance(), "Expected expression or statement");
    }

    Token consume(TokenType type, String msg) {
        if (check(type)) return advance();
        // System.out.println(peek().toString());
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
        if (check(TokenType.SEMICOLON)) {
            if (check(1, TokenType.EOL)) {
                advance();
                advance();
                return;
            }
            advance();
            return;
        }
        if (check(TokenType.R_BRACE)) {
            if (check(1, TokenType.EOL)) {
                advance();
                advance();
                return;
            }
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

    boolean check(int n, TokenType type) {
        if (atEnd()) {
            if (type != TokenType.EOL) return false;
            else return true;
        }
        return peek(n).type == type;
    }

    boolean check(TokenType type) {
        return check(0, type);
    }

    boolean atEnd() {
        return peek().type == TokenType.EOF;
    }

    Token peek(int n) {
        return tokens.get(current + n);
    }

    Token peek() {
        return peek(0);
    }

    Token prev() {
        return tokens.get(current - 1);
    }

    private class ParseError extends RuntimeException {}
}
