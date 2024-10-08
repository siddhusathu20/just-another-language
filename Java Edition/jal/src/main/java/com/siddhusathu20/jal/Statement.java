package com.siddhusathu20.jal;

import java.util.List;

abstract class Statement {
    interface Visitor<R> {
        R visit(ExprStmt stmt);
        R visit(LetStmt stmt);
        R visit(Block stmt);
        R visit(IfStmt stmt);
        R visit(WhileLoop stmt);
        R visit(Break stmt);
        R visit(FuncDef stmt);
        R visit(Return stmt);
        R visit(TimesLoop stmt);
        R visit(Class stmt);
    }

    abstract<R> R accept(Visitor<R> visitor);

    static class Class extends Statement {
        final Token name;
        final List<Statement.FuncDef> methods;
        final Expression.Variable superclass;

        Class(Token name, List<Statement.FuncDef> methods, Expression.Variable superclass) {
            this.name = name;
            this.methods = methods;
            this.superclass = superclass;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class FuncDef extends Statement {
        final Token name;
        final List<Token> params;
        final List<Statement> body;

        FuncDef(Token name, List<Token> params, List<Statement> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Return extends Statement {
        final Token keyword;
        final Expression value;

        Return(Token keyword, Expression value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

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

    static class TimesLoop extends Statement {
        final Expression count;
        final Statement body;

        TimesLoop(Expression count, Statement body) {
            this.count = count;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Break extends Statement {
        final Token keyword;

        Break(Token keyword) {
            this.keyword = keyword;
        }

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
}
