package succ.commands.drivers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorDriver {

    String operatorRegex = "[+\\-/*^!]";
    String numberRegex = "((?<=[+\\-/*!])?(-))?+\\d+(\\.\\d+)?+([E]\\d+)?";

    public String calculate(String calc){
        ArrayList<Character> operators = new ArrayList<>();
        ArrayList<Double> numbers = new ArrayList<>();
        String calculation=calc;
        //Remove whitespace
        calculation=calculation.replaceAll("\\s+","");
        /*
         * Handle combined negative/positives
         */

        //Combine multiple negatives
        Pattern pattern = Pattern.compile("-{2,}");
        Matcher matcher = pattern.matcher(calculation);
        while(matcher.find()){
            String match = matcher.group();
            String replacement = (match.length()%2==0) ? "+" : "-";
            calculation = calculation.replace(match, replacement);
        }

        //Combine multiple positives
        pattern = Pattern.compile("\\+{2,}");
        matcher = pattern.matcher(calculation);
        while(matcher.find()){
            String match = matcher.group();
            calculation=calculation.replace(match, "+");
        }
        //Combine +- into -
        pattern = Pattern.compile("-\\+|\\+-");
        matcher = pattern.matcher(calculation);
        while(matcher.find()){
            String match = matcher.group();
            calculation = calculation.replace(match, "-");
        }
        /*
         * Split string into numbers/operators
         */

        pattern = Pattern.compile(numberRegex);
        matcher = pattern.matcher(calculation);
        while(matcher.find()){
            String match = matcher.group();
            double number = Double.parseDouble(match);
            numbers.add(number);
//            System.out.println("calculation: "+calculation);
//            System.out.println("match: "+match);
//            System.out.println("number: "+number);
//            System.out.println("index: "+calculation.indexOf(match));
            if(number>=0 || calculation.indexOf(match)==0)
                calculation = calculation.replaceFirst(match, "");
            else {
                calculation = calculation.replaceFirst(match,"+");
            }
        }


        pattern = Pattern.compile(operatorRegex);
        matcher = pattern.matcher(calculation);
        while(matcher.find()){
            String match = matcher.group();
            operators.add(match.charAt(0));
        }

        /*
         * Apply BOMDAS
         */

        //Deal with factorials first.
        for (int i = 0; i < operators.size(); i++){

            if (operators.get(i).equals('!'))
            {
                double number = numbers.get(i);
                if (number< 0){
                    return "factorial undefined";
                }
                numbers.set(numbers.indexOf(numbers.get(i)), subCalculation(number,0,operators.get(i)));
                operators.remove(i);
                i--;
            }
        }

        //Next deal with powers.
        for (int i = 0; i < operators.size(); i++){

            if (operators.get(i).equals('^'))
            {
                double numberOne = numbers.get(i);
                double numberTwo = numbers.get(i+1);
                numbers.set(i, subCalculation(numberOne,numberTwo, operators.get(i)));
                numbers.remove(i+1);
                operators.remove(i);
                i--;
            }
        }

        //Next deal with multiplication and then addition from left to right.
        for (int i = 0; i < operators.size(); i++){

            if (operators.get(i).equals('*')||operators.get(i).equals('/'))
            {
                double numberOne = numbers.get(i);
                double numberTwo = numbers.get(i+1);
                numbers.set(i, subCalculation(numberOne,numberTwo,operators.get(i)));
                numbers.remove(i+1);
                operators.remove(i);
                i--;
            }
        }

        //Next deal with adding and subtracting from left to right.
        for (int i = 0; i < operators.size(); i++){

            if (operators.get(i).equals('+')||operators.get(i).equals('-'))
            {
                double numberOne = numbers.get(i);
                double numberTwo = numbers.get(i+1);
                numbers.set(i, subCalculation(numberOne,numberTwo, operators.get(i)));
                numbers.remove(i+1);
                operators.remove(i);
                i--;
            }
        }

        return (numbers.get(0)==Double.MAX_VALUE) ? "Infinity" :"The answer is: "+numbers.get(0);
    }

    private double subCalculation(double numberOne, double numberTwo, char operator){
        switch (operator) {
            case '+':
                return numberOne + numberTwo;
            case '-':
                return numberOne - numberTwo;
            case '*':
                return numberOne * numberTwo;
            case '/':
                return numberOne / numberTwo;
            case '!':
                double factorial = 0;
                if(numberOne>170) return Double.MAX_VALUE;
                //0 special case
                if (numberOne == 0) {
                    return 1;
                }
                else {
                    for (double i = numberOne; i != 1; i--) {
                        factorial = numberOne *= i - 1;
                    }
                    return factorial;
                }
            case '^':
                return Math.pow(numberOne, numberTwo);
            default:
                return 0;
        }
    }
}
