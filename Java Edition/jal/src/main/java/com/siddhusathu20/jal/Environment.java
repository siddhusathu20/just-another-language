package com.siddhusathu20.jal;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Map<String, Object> vars = new HashMap<>();

    void define(String name, Object value) {
        vars.put(name, value);
    }

    Object get(Token name) {
        if (vars.containsKey(name.lexeme))
            return vars.get(name.lexeme);
        throw new Evaluator.RuntimeError(name, "Variable " + name.lexeme + " not defined");
    }
}
