package com.siddhusathu20.jal;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Evaluator implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    final Environment globals = new Environment();
    Environment env = globals;
    int loopCount = 0;
    int breakCount = 0;

    Evaluator() {
        globals.define("print", new Callable() {
            @Override
            public int getArgc() {
                return 1;
            }

            @Override
            public Object call(Evaluator evaluator, List<Object> args) {
                System.out.print(stringCast(args.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<native func print>";
            }
        });

        globals.define("println", new Callable() {
            @Override
            public int getArgc() {
                return 1;
            }

            @Override
            public Object call(Evaluator evaluator, List<Object> args) {
                System.out.println(stringCast(args.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<native func println>";
            }
        });

        globals.define("input", new Callable() {
            @Override
            public int getArgc() {
                return 1;
            }

            @Override
            public Object call(Evaluator evaluator, List<Object> args) {
                Scanner input = new Scanner(System.in);
                System.out.print(args.get(0));
                String value = input.nextLine();
                input = null;
                return value;
            }

            @Override
            public String toString() {
                return "<native func input>";
            }
        });

        globals.define("time", new Callable() {
            @Override
            public int getArgc() {
                return 0;
            }

            @Override
            public Object call(Evaluator evaluator, List<Object> args) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native func time>";
            }
        });
    }
    
    public void interpret(List<Statement> statements) {
        try {
            for (Statement stmt : statements) {
                exec(stmt);
            }
        } catch (RuntimeError e) {
            Main.runtimeError(e);
        }
    }

    public Void visit(Statement.FuncDef stmt) {
        Function func = new Function(stmt);
        env.define(stmt.name.lexeme, func);
        return null;
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

    public Void visit(Statement.WhileLoop stmt) {
        loopCount++;
        int currentLoop = loopCount;
        while (isTrue(eval(stmt.condition)) && loopCount == currentLoop) {
            exec(stmt.body);
        }
        if (breakCount > loopCount) breakCount--;
        else loopCount--;
        return null;
    }

    public Void visit(Statement.Break stmt) {
        breakCount++;
        if (breakCount > loopCount) {
            breakCount--;
            throw new RuntimeError(null, "Unexpected 'break' statement");
        }
        loopCount--;
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

    public Object visit(Expression.FuncCall expr) {
        Object callee = eval(expr.func);
        List<Object> args = new ArrayList<>();
        for (Expression arg : expr.args) {
            args.add(eval(arg));
        }
        if (!(callee instanceof Callable))
            throw new RuntimeError(expr.bracket, "Calls are only valid for functions");
        Callable func = (Callable) callee;
        if (args.size() != func.getArgc())
            throw new RuntimeError(expr.bracket, "Expected " + func.getArgc() + " arguments but got " + args.size());
        return func.call(this, args);
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

    public Object visit(Expression.Logical expr) {
        Object left = eval(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTrue(left)) return left;
        } else {
            if (!isTrue(left)) return left;
        }
        return eval(expr.right);
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
        if (stmt != null) stmt.accept(this);
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
