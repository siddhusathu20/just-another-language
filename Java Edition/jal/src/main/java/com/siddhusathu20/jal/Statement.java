package com.siddhusathu20.jal;

abstract class Statement {
    interface Visitor<R> {
        R visit(ExprStmt stmt);
        R visit(LetStmt stmt);
        R visit(PrintStmt stmt);
    }

    abstract<R> R accept(Visitor<R> visitor);

    static class ExprStmt extends Statement {
        final Expression expr;

        ExprStmt(Expression expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class LetStmt extends Statement {
        final Token name;
        final Expression value;

        LetStmt(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
    
    static class PrintStmt extends Statement {
        final Expression expr;

        PrintStmt(Expression expr) {
            this.expr = expr;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
}
