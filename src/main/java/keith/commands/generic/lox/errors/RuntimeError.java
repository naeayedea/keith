package keith.commands.generic.lox.errors;

import keith.commands.generic.lox.Lexer.Token;

public class RuntimeError extends RuntimeException {

    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
