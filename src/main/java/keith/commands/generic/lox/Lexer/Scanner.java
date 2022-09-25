package keith.commands.generic.lox.Lexer;

import keith.commands.generic.lox.Lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static keith.commands.generic.lox.Lexer.TokenType.*;

public class Scanner {

    //static fields
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("static", STATIC);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
        keywords.put("break", BREAK);
    }

    //instance fields
    private final String source;
    private final Lox lox;
    private final List<Token> tokens;
    private int start;
    private int current;
    private int line;

    public Scanner(String source, Lox lox) {
        this.source = source;
        this.lox = lox;
        tokens = new ArrayList<>();
        start = 0;
        current = 0;
        line = 1;
    }

    public List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));

        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '`': break;
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '%': addToken(MODULO); break;
            case '!': addToken(match('=') ? BANG_EQUAL: BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL: LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL: GREATER); break;
            case '/':
                if (match('/')) {
                    //double slash is comment which continues until a new line.
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    comment();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ': case '\r': case '\t': break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    lox.error(line, "Unexpected character: "+c);
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        addToken(type == null ? IDENTIFIER : type);
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        //look for a decimal
        if (peek() == '.' && isDigit(peekNext())) {
            //consume the "."
            advance();

            while(isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }


    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }
        if (isAtEnd()) {
            lox.error(line, "Unterminated string.");
            return;
        }
        //the closing ";
        advance();
        //trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void comment() {
        while (!isAtEnd()) {
            if (advance() == '\n') {
                line++;
            }
            if (peek() == '*' && peekNext() == '/') {
                advance();
                advance();
                break;
            }
        }
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if(peek() != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}