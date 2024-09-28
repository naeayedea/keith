package com.naeayedea.keith.commands.message.generic.lox.interpreter;

import com.naeayedea.keith.commands.message.generic.lox.lexer.Token;
import com.naeayedea.keith.commands.message.generic.lox.lexer.TokenType;
import com.naeayedea.keith.commands.message.generic.lox.parser.Stmt;
import com.naeayedea.keith.commands.message.generic.lox.errors.Return;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define(new Token(TokenType.IDENTIFIER, "this", null, -1), instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        //create a new environment for the function, e.g. function should only be able to see and access global variables, functions
        Environment environment = new Environment(closure);
        //input the functions parameters into its environment
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i), arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }
        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
