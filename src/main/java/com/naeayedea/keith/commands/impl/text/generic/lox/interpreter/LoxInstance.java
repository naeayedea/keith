package com.naeayedea.keith.commands.impl.text.generic.lox.interpreter;

import com.naeayedea.keith.commands.impl.text.generic.lox.lexer.Token;
import com.naeayedea.keith.commands.impl.text.generic.lox.errors.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {

    private final LoxClass klass;
    public final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        if (klass != null) {
            LoxFunction method = klass.findMethod(name.lexeme);
            if (method != null) return method.bind(this);
        } else {
            throw new RuntimeError(name, "Static method '" + name.lexeme + "' does not exist.");
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
