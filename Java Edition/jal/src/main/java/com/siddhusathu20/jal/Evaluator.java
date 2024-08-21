package com.siddhusathu20.jal;

import java.util.List;
import java.util.Scanner;

public class Evaluator implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    Environment env = new Environment();
    
    public void interpret(List<Statement> statements) {
        try {
            for (Statement stmt : statements) {
                exec(stmt);
            }
        } catch (RuntimeError e) {
            Main.runtimeError(e);
        }
    }

    public Void visit(Statement.Block stmt) {
        execBlock(stmt.statements, new Environment(env));
        return null;
    }

    public Void visit(Statement.IfStmt stmt) {
        if (isTrue(eval(stmt.condition)))
            exec(stmt.thenBranch);
        else
            exec(stmt.elseBranch);
        return null;
    }

    public Void visit(Statement.PrintStmt stmt) {
        Object value = eval(stmt.expr);
        System.out.print(stringCast(value));
        return null;
    }

    public Void visit(Statement.PrintlnStmt stmt) {
        Object value = eval(stmt.expr);
        System.out.println(stringCast(value));
        return null;
    }

    public Void visit(Statement.InputStmt stmt) {
        Scanner input = new Scanner(System.in);
        String value = input.nextLine();
        env.define(stmt.name.lexeme, value);
        input = null;
        return null;
    }

    public Void visit(Statement.LetStmt stmt) {
        Object value = null;
        if (stmt.value != null)
            value = eval(stmt.value);
        env.define(stmt.name.lexeme, value);
        return null;
    }

    public Void visit(Statement.ExprStmt stmt) {
        eval(stmt.expr);
        return null;
    }

    public Object visit(Expression.Assignment expr) {
        Object value = eval(expr.value);
        env.assign(expr.name, value);
        return value;
    }

    public Object visit(Expression.Variable expr) {
        return env.get(expr.name);
    }
    
    public Object visit(Expression.Literal expr) {
        return expr.value;
    }

    public Object visit(Expression.Group expr) {
        return eval(expr.expr);
    }

    public Object visit(Expression.Unary expr) {
        Object right = eval(expr.right);
        switch (expr.operator.type) {
            case EXCL:
                return !isTrue(right);
            case MINUS:
                verifyNumberOperands(expr.operator, right);
                return - (double) right;
            case NUMOF:
                if (right instanceof Double)
                    return (double) right;
                if (right instanceof String) {
                    try {
                        return Double.parseDouble((String) right);
                    } catch (Exception e) {
                        throw new RuntimeError(expr.operator, "Cannot convert \"" + (String) right + "\" to a number");
                    }
                }
            case STROF:
                if (right instanceof String)
                    return (String) right;
                if (right instanceof Double)
                    return stringCast(right);
            default:
                break;
        }
        return null;
    }

    public Object visit(Expression.Binary expr) {
        Object left = eval(expr.left);
        Object right = eval(expr.right);
        switch (expr.operator.type) {
            case ASTERISK:
                verifyNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                verifyNumberOperands(expr.operator, left, right);
                if ((double) right != 0)
                    return (double) left / (double) right;
                throw new RuntimeError(expr.operator, "Division by zero");
            case MINUS:
                verifyNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;
                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;
                throw new RuntimeError(expr.operator, "Invalid operand type(s) - Operands must be numbers or strings.");
            case GT:
                verifyNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case LT:
                verifyNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case GT_EQ:
                verifyNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LT_EQ:
                verifyNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case DOUBLE_EQ:
                return areEqual(left, right);
            default:
                break;
        }
        return null;
    }

    Object eval(Expression expr) {
        return expr.accept(this);
    }

    void exec(Statement stmt) {
        stmt.accept(this);
    }

    void execBlock(List<Statement> statements, Environment env) {
        Environment outer = this.env;
        try {
            this.env = env;
            for (Statement stmt : statements)
                exec(stmt);
        } finally {
            this.env = outer;
        }
    }

    boolean isTrue(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    boolean areEqual(Object x, Object y) {
        if (x == null && y == null) return true;
        if (x == null) return false;
        return x.equals(y);
    }

    void verifyNumberOperands(Token operation, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operation, "Invalid operand type - operand must be a number.");
    }

    void verifyNumberOperands(Token operation, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operation, "Invalid operand type(s) - operands must be numbers.");
    }

    String stringCast(Object value) {
        if (value == null) return "none";
        if (value instanceof Double) {
            String valueStr = value.toString();
            if (valueStr.endsWith(".0"))
                return valueStr.substring(0, valueStr.length() - 2);
        }
        return value.toString();
    }

    static class RuntimeError extends RuntimeException {
        final Token token;
        RuntimeError(Token token, String msg) {
            super(msg);
            this.token = token;
        }
    }
}
