package com.siddhusathu20.jal;

import java.util.List;

abstract class Expression {
    interface Visitor<R> {
        R visit(Binary expr);
        R visit(Unary expr);
        R visit(Group expr);
        R visit(Literal expr);
        R visit(Variable expr);
        R visit(Assignment expr);
        R visit(Logical expr);
        R visit(FuncCall expr);
    }

    abstract<R> R accept(Visitor<R> visitor);

    static class FuncCall extends Expression {
        final Expression func;
        final Token bracket;
        final List<Expression> args;

        FuncCall(Expression func, Token bracket, List<Expression> args) {
            this.func = func;
            this.bracket = bracket;
            this.args = args;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Assignment extends Expression {
        final Token name;
        final Expression value;

        Assignment(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Variable extends Expression {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Logical extends Expression {
        final Expression left;
        final Token operator;
        final Expression right;

        Logical(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
    
    static class Binary extends Expression {
        final Expression left;
        final Token operator;
        final Expression right;

        Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Unary extends Expression {
        final Token operator;
        final Expression right;
        
        Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Group extends Expression {
        final Expression expr;
        
        Group(Expression expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    static class Literal extends Expression {
        final Object value;
        
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }
}
