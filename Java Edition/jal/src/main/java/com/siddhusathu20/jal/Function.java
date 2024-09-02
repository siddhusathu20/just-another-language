package com.siddhusathu20.jal;

import java.util.List;

public class Function implements Callable {
    final Statement.FuncDef declaration;
    final Environment closure;
    final boolean isConstructor;

    Function(Statement.FuncDef declaration, Environment closure, boolean isConstructor) {
        this.declaration = declaration;
        this.closure = closure;
        this.isConstructor = isConstructor;
    }

    @Override
    public int getArgc() {
        return declaration.params.size();
    }

    @Override
    public Object call(Evaluator evaluator, List<Object> args) {
        Environment env = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            env.define(declaration.params.get(i).lexeme, args.get(i));
        }
        try {
            evaluator.execBlock(declaration.body, env);
        } catch (Evaluator.Return ret) {
            if (isConstructor) return closure.getAt(0, "self");
            return ret.value;
        }
        if (isConstructor) return closure.getAt(0, "self");
        return null;
    }

    @Override
    public String toString() {
        return "<func " + declaration.name.lexeme + " >";
    }

    Function bind(JALInstance inst) {
        Environment env = new Environment(closure);
        env.define("self", inst);
        return new Function(declaration, env, isConstructor);
    }
}
