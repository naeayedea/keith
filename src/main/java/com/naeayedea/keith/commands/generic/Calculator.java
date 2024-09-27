package com.naeayedea.keith.commands.generic;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class Calculator extends AbstractUserCommand {

    public Calculator(@Value("${keith.commands.calculator.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.calculator.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);

    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"evaluates the expression passed using "+prefix+getDefaultName()+" [expression]\"";
    }

    @Override
    public String getLongDescription() {
        return "basic calculator functionality supports common operators such as:\n" +
                "+, -, / (divide), * (multiply), ^ (exponential or power)\n\n" +
                "and some functions such as:\n" +
                "sqrt(num), sin(num), cos(num), tan(num)\n" +
                "please note that there is a limit to calculations and very large numbers will be returned as infinity";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        try {
            double answer = eval(Utilities.stringListToString(tokens));
            event.getChannel().sendMessage(Double.isInfinite(answer) ? "infinity" : ""+BigDecimal.valueOf(answer).setScale(3, RoundingMode.HALF_UP).doubleValue()).queue();
        } catch (RuntimeException e) {
            Utilities.Messages.sendError(event.getChannel(), "Calculator Error",  e.getMessage());
        }
    }

    /*adapted calculator code from: https://stackoverflow.com/a/26227947, fixed bug in original and adapted to work well
     * with the bot and users and made use of brackets with functions make more sense
     */
    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch, prev;

            void nextChar() {
                prev = pos;
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                double x = parseBracket();
                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
                return x;
            }

            double parseBracket() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) {
                        throw new RuntimeException ("No closing bracket after character "+prev+": '"+str.charAt(prev)+"'");
                    }
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                    return x;
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    switch (func) {
                        case "sqrt":
                            double y = parseBracket();
                            if (y < 0) throw new RuntimeException("Cannot take the square root of a negative number!");
                            x = Math.sqrt(y);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(parseBracket()));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(parseBracket()));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(parseBracket()));
                            break;
                        default:
                            throw new RuntimeException("Unknown Function: " + func);
                    }
                } else {
                    if (ch == -1) {
                        throw new RuntimeException("Incomplete Calculation! Expected expression after character "+prev+": '"+str.charAt(prev)+"'");
                    }
                    throw new RuntimeException("Unexpected Character:" + (char) ch);
                }
                return x;
            }
        }.parse();
    }
}
