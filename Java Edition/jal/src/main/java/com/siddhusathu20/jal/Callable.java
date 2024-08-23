package com.siddhusathu20.jal;

import java.util.List;

public interface Callable {
    int getArgc();
    Object call(Evaluator evaluator, List<Object> args);
}
