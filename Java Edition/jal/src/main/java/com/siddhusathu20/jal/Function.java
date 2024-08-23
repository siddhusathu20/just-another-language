package com.siddhusathu20.jal;

import java.util.List;

public class Function implements Callable {
    final Statement.FuncDef declaration;
    final Environment closure;

    Function(Statement.FuncDef declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
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
            return ret.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<func " + declaration.name.lexeme + " >";
    }
}
