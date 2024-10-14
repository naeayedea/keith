package com.naeayedea.keith.commands.text.generic.lox.interpreter.lib;

import com.naeayedea.keith.commands.text.generic.lox.interpreter.Interpreter;
import com.naeayedea.keith.commands.text.generic.lox.interpreter.LoxCallable;
import com.naeayedea.keith.commands.text.generic.lox.lexer.Token;
import com.naeayedea.keith.commands.text.generic.lox.lexer.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Globals {
    public static final Map<Token, LoxCallable> globals = new HashMap<>();

    static {
        globals.put(new Token(TokenType.IDENTIFIER, "clock", null, -1), new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }
}
