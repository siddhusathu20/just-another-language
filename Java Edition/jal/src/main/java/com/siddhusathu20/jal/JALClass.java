package com.siddhusathu20.jal;

import java.util.List;
import java.util.Map;

public class JALClass implements Callable {
    final String name;
    final Map<String, Function> methods;
    final JALClass superclass;

    JALClass(String name, Map<String, Function> methods, JALClass superclass) {
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
    }

    @Override
    public Object call(Evaluator evaluator, List<Object> args) {
        JALInstance instance = new JALInstance(this);
        Function init = getMethod("constructor");
        if (init != null)
            init.bind(instance).call(evaluator, args);
        return instance;
    }

    @Override
    public int getArgc() {
        Function init = getMethod("constructor");
        if (init == null) return 0;
        return init.getArgc();
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }

    Function getMethod(String name) {
        if (methods.containsKey(name)) return methods.get(name);
        if (superclass != null) return superclass.getMethod(name);
        return null;
    }
}
