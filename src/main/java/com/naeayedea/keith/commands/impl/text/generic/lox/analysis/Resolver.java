package com.naeayedea.keith.commands.impl.text.generic.lox.analysis;

import com.naeayedea.keith.commands.impl.text.generic.lox.interpreter.Interpreter;
import com.naeayedea.keith.commands.impl.text.generic.lox.interpreter.lib.Globals;
import com.naeayedea.keith.commands.impl.text.generic.lox.lexer.Token;
import com.naeayedea.keith.commands.impl.text.generic.lox.Lox;
import com.naeayedea.keith.commands.impl.text.generic.lox.parser.Expr;
import com.naeayedea.keith.commands.impl.text.generic.lox.parser.Stmt;

import java.util.*;

public class Resolver implements Expr.Visitor<String>, Stmt.Visitor<Void> {

    private record Error(Token token, String message) {}

    private enum FunctionType {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    }


    private enum ClassType {
        NONE,
        CLASS
    }

    private final Stack<Map<String, Short>> scopes = new Stack<>();
    private final List<Error> errors = new ArrayList<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;
    private boolean inLoop = false;
    private int numBlocks = 0;
    private final Interpreter interpreter;
    private final Lox lox;


    public Resolver(Interpreter interpreter, Lox lox) {
        this.interpreter = interpreter;
        this.lox = lox;
        beginScope(); //define a base scope for all things e.g. global scope
        for (Token global : Globals.globals.keySet()) {
            declare(global);
            define(global);
        }
    }

    public void reportErrors() {
        errors.sort(Comparator.comparing(e -> e.token.line));
        for (Error error : errors) {
            lox.error(error.token, error.message);
        }
    }

    public boolean notSet(Short bitField, Short mask) {
        return bitField != null && (bitField & mask) != mask;
    }

    public short getShort(Short s) {
        return s != null ? s : 0;
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
        //after all statements have been resolved, do one more sweep and make sure all variables are used.
        for (Stmt statement : statements) {
            if (statement instanceof Stmt.Var var) {
                if (!scopes.isEmpty() && notSet(scopes.peek().get(var.name.lexeme), (short) 0x0010)) {
                    errors.add(new Error(var.name, "unused variable: " + var.name.lexeme));
                }
            }
        }
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                if (currentFunction == FunctionType.FUNCTION && scopes.get(numBlocks).containsKey(name.lexeme)) {
                    interpreter.resolve(expr, numBlocks);
                } else {
                    interpreter.resolve(expr, scopes.size() - 1 - i);
                }
                scopes.get(i).put(name.lexeme, (short) (getShort(scopes.get(i).get(name.lexeme)) | 0x0010));
                return;
            }
        }
        errors.add(new Error(name, "Undefined variable: '" + name.lexeme + "'."));
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        Map<String, Short> scope = scopes.peek();
        if (!scopes.isEmpty()) {
            for (int i = 0; i < numBlocks; i++) {
                if (scopes.get(i).containsKey(name.lexeme)) {
                    errors.add(new Error(name, "Variable: '" + name.lexeme + "' already defined within scope."));

                }
            }
        }
        scope.put(name.lexeme, (short) 0x0000);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, (short) 0x0001);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr arg : expr.arguments) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            errors.add(new Error(expr.keyword, "Can't use 'this' outside of a class."));
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && notSet(scopes.peek().get(expr.name.lexeme), (short) 0x0001)) {
            errors.add(new Error(expr.name, "Can't read local variable in its own initializer."));
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        numBlocks++;
        resolve(stmt.statements);
        numBlocks--;
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);

        beginScope();
        scopes.peek().put("this", (short) 0x0011);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.isStatic) {
                if (method.name.lexeme.equals("init")) {
                    errors.add(new Error(method.name, "Object init function cannot be static."));
                }
                resolveFunction(method, declaration);
            } else {
                if (method.name.lexeme.equals("init")) {
                    declaration = FunctionType.INITIALIZER;
                }
                resolveFunction(method, declaration);
            }
        }

        endScope();
        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            errors.add(new Error(stmt.keyword, "Can't return from top-level code."));
        }
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                errors.add(new Error(stmt.keyword, "Can't return a value from an initializer."));
            }
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        boolean prevLoop = inLoop;
        inLoop = true;
        resolve(stmt.condition);
        resolve(stmt.body);
        inLoop = prevLoop;
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (!inLoop) {
            errors.add(new Error(stmt.keyword, "Cannot break outside of a loop."));
        }
        return null;
    }
}
