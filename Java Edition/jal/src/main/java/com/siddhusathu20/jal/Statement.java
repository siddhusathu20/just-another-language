package com.siddhusathu20.jal;

import java.util.List;

abstract class Statement {
    interface Visitor<R> {
        R visit(ExprStmt stmt);
        R visit(LetStmt stmt);
        R visit(PrintStmt stmt);
        R visit(PrintlnStmt stmt);
        R visit(InputStmt stmt);
        R visit(Block stmt);
        R visit(IfStmt stmt);
        R visit(WhileLoop stmt);
        R visit(Break stmt);
    }

    abstract<R> R accept(Visitor<R> visitor);

    static class IfStmt extends Statement {
        final Expression condition;
        final Statement thenBranch;
        final Statement elseBranch;

        IfStmt(Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class WhileLoop extends Statement {
        final Expression condition;
        final Statement body;

        WhileLoop(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Break extends Statement {
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }


    static class Block extends Statement {
        final List<Statement> statements;

        Block(List<Statement> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

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

    static class PrintlnStmt extends Statement {
        final Expression expr;

        PrintlnStmt(Expression expr) {
            this.expr = expr;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class InputStmt extends Statement {
        final Token name;

        InputStmt(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
}
