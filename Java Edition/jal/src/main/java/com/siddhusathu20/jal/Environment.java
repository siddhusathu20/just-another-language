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

    Object get(Token name) {
        if (vars.containsKey(name.lexeme))
            return vars.get(name.lexeme);
        if (enclosing != null) return enclosing.get(name);
        throw new Evaluator.RuntimeError(name, "Variable " + name.lexeme + " not defined");
    }
}
