package com.naeayedea.keith.commands.impl.text.generic.lox.interpreter;

import java.util.List;

public interface LoxCallable {
    Object call(Interpreter interpreter, List<Object> arguments);

    int arity();
}

