package com.naeayedea.keith.commands.message.generic.lox.errors;

import com.naeayedea.keith.commands.message.generic.lox.lexer.Token;

public class RuntimeError extends RuntimeException {

    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
