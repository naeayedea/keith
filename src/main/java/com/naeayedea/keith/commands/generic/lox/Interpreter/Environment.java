package com.naeayedea.keith.commands.generic.lox.Interpreter;

import com.naeayedea.keith.commands.generic.lox.Lexer.Token;
import com.naeayedea.keith.commands.generic.lox.errors.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public Object get(Token name) {
        Object value;
        if (values.containsKey(name.lexeme)) {
            value = values.get(name.lexeme);
        } else {
            if (enclosing != null) {
                value = enclosing.get(name);
            } else {
                throw new RuntimeError(name, "Undefined variable: '" + name.lexeme + "'.");
            }
        }

        if (value != null) return value;

        throw new RuntimeError(name, "Uninitialized variable: '" + name.lexeme + "'.");
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i <  distance; i++) {
            if (environment.enclosing != null) {
                environment = environment.enclosing;
            }
        }
        return environment;
    }

    public void define(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            throw new RuntimeError(name, "Variable: '" + name.lexeme + "' already defined within scope.");
        } else {
            values.put(name.lexeme, value);
        }
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else if (enclosing != null)  {
            enclosing.assign(name, value);
        } else {
            throw new RuntimeError(name, "Undefined variable: '" + name.lexeme + "'.");
        }
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    public String toString() {
        return values + " " +enclosing;
    }
}
