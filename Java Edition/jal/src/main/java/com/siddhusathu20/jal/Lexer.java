package com.siddhusathu20.jal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("let", TokenType.LET);
        keywords.put("print", TokenType.PRINT);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("none", TokenType.NONE);
        keywords.put("def", TokenType.DEF);
        keywords.put("return", TokenType.RETURN);
        keywords.put("then", TokenType.THEN);
        keywords.put("println", TokenType.PRINTLN);
        keywords.put("input", TokenType.INPUT);
        keywords.put("numof", TokenType.NUMOF);
        keywords.put("strof", TokenType.STROF);
        keywords.put("do", TokenType.DO);
    }
    final String src;
    final List<Token> tokens = new ArrayList<>();
    int start = 0;
    int current = 0;
    int line = 1;

    Lexer(String src) {
        this.src = src;
    }

    List<Token> scan() {
        while (!atEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    boolean atEnd() {
        return current >= src.length();
    }

    void scanToken() {
        char c = src.charAt(current++);
        switch (c) {
            case '(':
                addToken(TokenType.L_BRACKET, null);
                break;
            case ')':
                addToken(TokenType.R_BRACKET, null);
                break;
            case '{':
                addToken(TokenType.L_BRACE, null);
                break;
            case '}':
                addToken(TokenType.R_BRACE, null);
                break;
            case ',':
                addToken(TokenType.COMMA, null);
                break;
            case '.':
                addToken(TokenType.DOT, null);
                break;
            case ';':
                addToken(TokenType.SEMICOLON, null);
                break;
            case '-':
                addToken(TokenType.MINUS, null);
                break;
            case '+':
                addToken(TokenType.PLUS, null);
                break;
            case '*':
                addToken(TokenType.ASTERISK, null);
                break;
            case '/':
                addToken(TokenType.SLASH, null);
                break;
            case '#':
                addToken(TokenType.HASHTAG, null);
                break;
            case '!':
                addToken(next('=') ? TokenType.EXCL_EQ : TokenType.EXCL, null);
                break;
            case '=':
                addToken(next('=') ? TokenType.DOUBLE_EQ : TokenType.EQ, null);
                break;
            case '<':
                addToken(next('=') ? TokenType.LT_EQ : TokenType.LT, null);
                break;
            case '>':
                addToken(next('=') ? TokenType.GT_EQ : TokenType.GT, null);
                break;
            case '\n':
                if (tokens.get(tokens.size() - 1).type != TokenType.EOL
                && tokens.get(tokens.size() - 1).type != TokenType.L_BRACE)
                    addToken(TokenType.EOL, null);
                line++;
                break;
            case ' ':
            case '\t':
            case '\r':
                break;
            case '"':
                parseStr();
                break;
            default:
                if (isDigit(c)) {
                    parseNum();
                } else if (isAlphaUc(c)) {
                    identifier();
                } else {
                    Main.error(line, "Syntax error - Unexpected character:");
                    System.err.println(c);
                }
                break;
        }
    }

    char peek() {
        if (atEnd()) return '\0';
        return src.charAt(current);
    }

    char peek2() {
        if (current + 1 >= src.length()) return '\0';
        return src.charAt(current + 1);
    }

    boolean next(char c) {
        if (atEnd()) return false;
        if (src.charAt(current) != c) return false;
        current++;
        return true;
    }

    void addToken(TokenType type, Object value) {
        String lexeme = src.substring(start, current);
        tokens.add(new Token(type, lexeme, value, line));
    }

    boolean isAlphaUc(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '_'));
    }

    boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    boolean isAlNumUc(char c) {
        return (isAlphaUc(c) || isDigit(c));
    }

    void parseStr() {
        while (peek() != '"' && !atEnd()) {
            if (peek() == '\n') line++;
            current++;
        }
        if (atEnd()) {
            Main.error(line, "String not terminated (missing \" - punctuation is important!)");
            current++;
            return;
        }
        current++; // For closing quotes
        String str = src.substring(start + 1, current - 1); // Exclude quotes
        String escaped = str.replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\t", "\t")
                            .replace("\\\\", "\\")
                            .replace("\\r", "\r")
                            .replace("\\b", "\b")
                            .replace("\\f", "\f")
                            .replace("\\'", "\'");
        addToken(TokenType.STR, escaped);
    }

    void parseNum() {
        while (isDigit(peek())) current++;
        if (peek() == '.' && isDigit(peek2())) {
            current++;
            while (isDigit(peek())) current++;
        }
        addToken(TokenType.NUM, Double.parseDouble(src.substring(start, current)));
    }

    void identifier() {
        while (isAlNumUc(peek())) current++;
        String lexeme = src.substring(start, current);
        TokenType type = keywords.get(lexeme);
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type, null);
    }
}
