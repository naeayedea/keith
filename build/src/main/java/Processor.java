public class Processor {

    /**
     * Updated calculation processor to be more efficient.
     * Version 1 was simply calculatorv2. version 2 is
     * made to function specifically as a processor.
     *
     * Processor should be passed a number and an operator as
     * Calculator now handles the filtering of numbers from the string.
     *
     * v1.0
     */

    public static final double TYPE_FACTORIAL = 0;
    public String calculate(double numberOne, char operator, double numberTwo){

        switch (operator) {

            case '+':
                double answer = numberOne + numberTwo;
                return Double.toString(answer);
            case '-':
                answer = numberOne - numberTwo;
                return Double.toString(answer);
            case '*':
                answer = numberOne * numberTwo;
                return Double.toString(answer);
            case '/':
                answer = numberOne / numberTwo;
                return Double.toString(answer);
            case '!':
                double factorial = 0;

                //0 special case
                if (numberOne == 0) {

                    return "1";
                }

                else {

                    for (double i = numberOne; i != 1; i--) {

                        factorial = numberOne *= i - 1;

                    }

                    return String.format("%.0f", factorial);

                }
            case '^':
                answer = Math.pow(numberOne, numberTwo);
                return Double.toString(answer);
            default:
                return null;
        }

    }
}
