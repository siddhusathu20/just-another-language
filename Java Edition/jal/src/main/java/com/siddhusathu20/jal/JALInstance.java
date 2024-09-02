package com.siddhusathu20.jal;

import java.util.Map;
import java.util.HashMap;

public class JALInstance {
    JALClass cls;
    final Map<String, Object> fields = new HashMap<>();

    JALInstance(JALClass cls) {
        this.cls = cls;
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme))
            return fields.get(name.lexeme);
        Function method = cls.getMethod(name.lexeme);
        if (method != null) return method.bind(this);
        throw new Evaluator.RuntimeError(name, "Undefined property '" + name.lexeme + "'");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return "<instance of " + cls.name + ">";
    }
}
