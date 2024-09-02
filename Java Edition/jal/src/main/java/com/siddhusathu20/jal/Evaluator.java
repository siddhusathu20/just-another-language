package com.siddhusathu20.jal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Evaluator implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    final Environment globals = new Environment();
    Environment env = globals;
    Map<Expression, Integer> locals = new HashMap<>();
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
        Function func = new Function(stmt, env, false);
        env.define(stmt.name.lexeme, func);
        return null;
    }

    public Void visit(Statement.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = eval(stmt.superclass);
            if (!(superclass instanceof JALClass))
                throw new RuntimeError(stmt.superclass.name, "Can only inherit from a class");
        }
        env.define(stmt.name.lexeme, null);
        if (stmt.superclass != null) {
            env = new Environment(env);
            env.define("super", superclass);
        }
        Map<String, Function> methods = new HashMap<>();
        for (Statement.FuncDef method : stmt.methods) {
            Function func = new Function(method, env, method.name.lexeme.equals("constructor"));
            methods.put(method.name.lexeme, func);
        }
        JALClass cls = new JALClass(stmt.name.lexeme, methods, (JALClass) superclass);
        if (stmt.superclass != null) {
            env = env.enclosing;
        }
        env.assign(stmt.name, cls);
        return null;
    }

    public Void visit(Statement.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = eval(stmt.value);
        throw new Return(value);
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

    public Void visit(Statement.TimesLoop stmt) {
        loopCount++;
        int currentLoop = loopCount;
        for (int i = 0; i < (double) eval(stmt.count) && loopCount == currentLoop; i++) {
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
            throw new RuntimeError(stmt.keyword, "Unexpected 'break' statement");
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
        Integer distance = locals.get(expr);
        if (distance != null) {
            env.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    public Object visit(Expression.Variable expr) {
        return lookUpVar(expr.name, expr);
    }

    public Object visit(Expression.Self expr) {
        return lookUpVar(expr.keyword, expr);
    }

    public Object visit(Expression.Super expr) {
        int distance = locals.get(expr);
        JALClass superclass = (JALClass) env.getAt(distance, "super");
        JALInstance instance = (JALInstance) env.getAt(distance - 1, "self");
        Function method = superclass.getMethod(expr.method.lexeme);
        if (method == null)
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'");
        return method.bind(instance);
    }

    public Object visit(Expression.Getter expr) {
        Object object = eval(expr.object);
        if (object instanceof JALInstance)
            return ((JALInstance) object).get(expr.name);
        throw new RuntimeError(expr.name, "Object has no accessible properties");
    }

    public Object visit(Expression.Setter expr) {
        Object object = eval(expr.object);
        if (!(object instanceof JALInstance))
            throw new RuntimeError(expr.name, "Object has no accessible properties");
        Object value = eval(expr.value);
        ((JALInstance) object).set(expr.name, value);
        return value;
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
                throw new RuntimeError(expr.operator, "Invalid typecast");
            case STROF:
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

    Object lookUpVar(Token name, Expression expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return env.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
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

    void resolve(Expression expr, int depth) {
        locals.put(expr, depth);
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

    static class Return extends RuntimeException {
        final Object value;

        Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }

    static class RuntimeError extends RuntimeException {
        final Token token;
        RuntimeError(Token token, String msg) {
            super(msg);
            this.token = token;
        }
    }
}
