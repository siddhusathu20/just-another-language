package com.siddhusathu20.jal;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object value;
    final int line;

    Token(TokenType type, String lexeme, Object value, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.value = value;
        this.line = line;
    }

    public String toString() {
        if (type == TokenType.EOL || type == TokenType.EOF) return type + " in line " + line;
        if (value == null) return type + " " + lexeme;
        return type + " " + lexeme + ": " + value;
    }
}
