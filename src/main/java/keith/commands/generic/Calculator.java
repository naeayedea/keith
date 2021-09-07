package keith.commands.generic;

import keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Calculator extends UserCommand {

    String defaultName;
    public Calculator() {
        defaultName = "calculate";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"evaluates the expression passed using "+prefix+defaultName+" [expression]\"";
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
    public String getDefaultName() {
        return defaultName;
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

    //can create own calculator later, right now thank you: https://stackoverflow.com/a/26227947
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
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(parseFactor());
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(parseFactor()));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(parseFactor()));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(parseFactor()));
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

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}
