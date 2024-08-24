package com.siddhusathu20.jal;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    final Map<String, Object> vars = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        vars.put(name, value);
    }

    void assign(Token name, Object value) {
        if (vars.containsKey(name.lexeme)) {
            vars.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new Evaluator.RuntimeError(name, "Variable " + name.lexeme + " not defined");
    }

    void assignAt(int distance, Token name, Object value) {
        envAt(distance).vars.put(name.lexeme, value);
    }

    Object get(Token name) {
        if (vars.containsKey(name.lexeme))
            return vars.get(name.lexeme);
        if (enclosing != null) return enclosing.get(name);
        throw new Evaluator.RuntimeError(name, "Variable " + name.lexeme + " not defined");
    }

    Object getAt(int distance, String name) {
        return envAt(distance).vars.get(name);
    }

    Environment envAt(int distance) {
        Environment env = this;
        for (int i = 0; i < distance; i++) {
            env = env.enclosing;
        }
        return env;
    }
}
