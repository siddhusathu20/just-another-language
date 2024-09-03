package com.siddhusathu20.jal;

public enum TokenType {
    L_BRACKET, R_BRACKET, L_BRACE, R_BRACE, L_SQ_BR, R_SQ_BR,
    COMMA, DOT, PLUS, MINUS, ASTERISK, SLASH, PERCENT, HASHTAG,

    EQ, DOUBLE_EQ,
    EXCL, EXCL_EQ,
    GT, GT_EQ,
    LT, LT_EQ,

    IDENTIFIER, STR, NUM,

    AND, OR,

    NUMOF, STROF,

    LET, IF, ELSE, THEN, FOR, WHILE, DO, BREAK, TIMES,
    TRUE, FALSE, NONE, DEF, RETURN,

    CLASS, SELF, INHERITS, SUPER,

    SEMICOLON, EOL, EOF
}
