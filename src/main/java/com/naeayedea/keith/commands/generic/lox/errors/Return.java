package com.naeayedea.keith.commands.generic.lox.errors;

public class Return extends RuntimeException {
    public final Object value;

    public Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
