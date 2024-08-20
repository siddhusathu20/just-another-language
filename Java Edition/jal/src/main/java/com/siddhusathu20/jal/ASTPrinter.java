package com.siddhusathu20.jal;

public class ASTPrinter implements Expression.Visitor<String> {
    String print(Expression expr) {
        return expr.accept(this);
    }
    
    String repr(String type, Expression... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(type);
        for (Expression expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visit(Expression.Variable expr) {
        return repr("let", expr);
    }

    @Override
    public String visit(Expression.Binary expr) {
        return repr(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visit(Expression.Unary expr) {
        return repr(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visit(Expression.Group expr) {
        return repr("group", expr.expr);
    }

    @Override
    public String visit(Expression.Literal expr) {
        return expr.value.toString();
    }
}
