package com.siddhusathu20.jal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    final Evaluator evaluator;
    final Stack<Map<String, Boolean>> scopes = new Stack<>();
    FuncType currentFunc = FuncType.MAIN;
    ClassType currentClass = ClassType.MAIN;

    Resolver(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    enum FuncType {
        MAIN, FUNCTION, METHOD, CONSTRUCTOR
    }

    enum ClassType {
        MAIN, CLASS
    }

    public Void visit(Statement.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    public Void visit(Statement.FuncDef stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunc(stmt, FuncType.FUNCTION);
        return null;
    }

    public Void visit(Statement.Class stmt) {
        ClassType enclosing = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);
        beginScope();
        scopes.peek().put("self", true);
        for (Statement.FuncDef method : stmt.methods) {
            if (method.name.lexeme.equals("constructor"))
                resolveFunc(method, FuncType.CONSTRUCTOR);
            else
                resolveFunc(method, FuncType.METHOD);
        }
        endScope();
        currentClass = enclosing;
        return null;
    }

    public Void visit(Statement.LetStmt stmt) {
        declare(stmt.name);
        if (stmt.value != null) resolve(stmt.value);
        define(stmt.name);
        return null;
    }

    public Void visit(Statement.IfStmt stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    public Void visit(Statement.WhileLoop stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    public Void visit(Statement.TimesLoop stmt) {
        resolve(stmt.count);
        resolve(stmt.body);
        return null;
    }

    public Void visit(Statement.ExprStmt stmt) {
        resolve(stmt.expr);
        return null;
    }

    public Void visit(Statement.Return stmt) {
        if (currentFunc == FuncType.MAIN)
            Main.error(stmt.keyword.line, "Cannot return from top-level");
        if (stmt.value != null) {
            if (currentFunc == FuncType.CONSTRUCTOR)
                Main.error(stmt.keyword.line, "Cannot return a value from a constructor");
            resolve(stmt.value);
        }
        return null;
    }

    public Void visit(Statement.Break stmt) {
        return null;
    }

    public Void visit(Expression.Assignment expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    public Void visit(Expression.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    public Void visit(Expression.Logical expr) {
        resolve(expr.right);
        resolve(expr.left);
        return null;
    }

    public Void visit(Expression.Unary expr) {
        resolve(expr.right);
        return null;
    }

    public Void visit(Expression.FuncCall expr) {
        resolve(expr.func);
        for (Expression arg : expr.args) {
            resolve(arg);
        }
        return null;
    }

    public Void visit(Expression.Group expr) {
        resolve(expr.expr);
        return null;
    }

    public Void visit(Expression.Literal expr) {
        return null;
    }

    public Void visit(Expression.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Main.error(expr.name.line, "Can't read a local variable in its own initialiser");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    public Void visit(Expression.Self expr) {
        if (currentClass == ClassType.MAIN) {
            Main.error(expr.keyword.line, "Can't refer to 'self' outside a class");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    public Void visit(Expression.Getter expr) {
        resolve(expr.object);
        return null;
    }

    public Void visit(Expression.Setter expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    void declare(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, false);
    }

    void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    void resolveLocal(Expression expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                evaluator.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    void resolveFunc(Statement.FuncDef stmt, FuncType type) {
        FuncType enclosingFunc = currentFunc;
        currentFunc = type;
        beginScope();
        for (Token param : stmt.params) {
            declare(param);
            define(param);
        }
        resolve(stmt.body);
        endScope();
        currentFunc = enclosingFunc;
    }

    void resolve(List<Statement> statements) {
        for (Statement stmt : statements) {
            resolve(stmt);
        }
    }

    void resolve(Statement stmt) {
        stmt.accept(this);
    }

    void resolve(Expression expr) {
        expr.accept(this);
    }

    void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    void endScope() {
        scopes.pop();
    }
}
