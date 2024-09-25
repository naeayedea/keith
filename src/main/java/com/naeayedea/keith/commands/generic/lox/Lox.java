package com.naeayedea.keith.commands.generic.lox;

import com.naeayedea.keith.commands.generic.lox.Interpreter.Interpreter;
import com.naeayedea.keith.commands.generic.lox.Lexer.Scanner;
import com.naeayedea.keith.commands.generic.lox.Lexer.Token;
import com.naeayedea.keith.commands.generic.lox.Lexer.TokenType;
import com.naeayedea.keith.commands.generic.lox.Parser.Parser;
import com.naeayedea.keith.commands.generic.lox.Parser.Stmt;
import com.naeayedea.keith.commands.generic.lox.analysis.Resolver;
import com.naeayedea.keith.commands.generic.lox.errors.RuntimeError;

import java.util.ArrayList;
import java.util.List;

public class Lox {

    private boolean hadError = false;
    private List<String> errors = new ArrayList<>();

    public List<String> run(String source) {
        Interpreter interpreter = new Interpreter(this);
        errors = new ArrayList<>();
        hadError = false;
        //Scan file and separate into tokens
        Scanner scanner = new Scanner(source, this);
        List<Token> tokens = scanner.scanTokens();

        //Parse tokens into an executable list of statements
        Parser parser = new Parser(tokens, this);
        List<Stmt> statements = parser.parse();

        //stop if there's a syntax error
        if (hadError) return errors;

        //resolve variables
        Resolver resolver = new Resolver(interpreter, this);
        resolver.resolve(statements);
        resolver.reportErrors();

        //stop if there's a resolution error
        if (hadError) return errors;

        return interpreter.interpret(statements);
    }

    public void error(int line, String message) {
        report(line, "", message);
    }

    private void report(int line, String where, String message) {
        errors.add("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    public void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public void runtimeError(RuntimeError error) {
        errors.add("Runtime Error: "+error.getMessage() + "\n[line " + error.token.line + "]");
        hadError=true;
    }

}
