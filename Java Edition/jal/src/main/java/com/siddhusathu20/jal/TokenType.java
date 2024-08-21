package com.siddhusathu20.jal;

public enum TokenType {
    L_BRACKET, R_BRACKET, L_BRACE, R_BRACE,
    COMMA, DOT, PLUS, MINUS, ASTERISK, SLASH, HASHTAG,

    EQ, DOUBLE_EQ,
    EXCL, EXCL_EQ,
    GT, GT_EQ,
    LT, LT_EQ,

    IDENTIFIER, STR, NUM,

    AND, OR,

    NUMOF, STROF,

    LET, PRINT, PRINTLN, INPUT,
    IF, ELSE, THEN, FOR, WHILE, DO,
    TRUE, FALSE, NONE, DEF, RETURN,

    SEMICOLON, EOL, EOF
}
