
public class Processor {

    //field declarations.

    int endLoop = 0;
    int testLocation = 0;
    String testString;
    int startSecondNumber;
    int startFirstNumber;
    double numberOne;
    double numberTwo;
    char operation;
    char[] operators = {'+', '-', '*', '/', '!', '^'};

    public String calculate(String calculation){

        initialiseCalculation(calculation);
        String answer = performCalculation();
        return answer;
    }

    public void initialiseCalculation(String calculation){

        testString = calculation;
        testLocation = 0;
        getNumbers();
    }

    public void getNumbers(){

        findNumberOne();
        getNumberOne();
        getOperator();

        if (operation != '!'){

            findNumberTwo();
            getNumberTwo();
        }

    }

    public String performCalculation(){

        switch (operation) {

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
                System.out.println(numberOne);
                System.out.println(numberTwo);
                answer = numberOne / numberTwo;
                return Double.toString(answer);
            case '!':
                double factorial = 0;

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

    public void findNumberOne(){

        /*locates the first number within the string, when an exception is thrown it knows the number isn't there and
        moves on to the next index. Process ends when an exception isn't thrown aka the number has been located.*/

        try {

            while (endLoop ==0) {

                Double.parseDouble(String.valueOf(testString.charAt(testLocation)));
                startFirstNumber = testLocation;
                endLoop = 1;

            }
        }

        catch (NumberFormatException c) {

            testLocation++;
            findNumberOne();

        }

        catch (StringIndexOutOfBoundsException c) {
            return;
        }
    }

    public void getNumberOne(){


        /* Using the location gathered by findNumberOne(), the function finds the range of the number withing the string
        and parses it once located. */

        endLoop = 0;
        try {

            while (endLoop == 0) {

                Double.parseDouble(String.valueOf(testString.charAt(testLocation)));
                testLocation++;
            }
        }

        catch (NumberFormatException c) {

            /* this exception is thrown when there is no number to be parsed, which means either a decimal point or the
            end of the first number which is handled below. */

            if (testString.charAt(testLocation) == '.'){

                testLocation++;

                try {

                    while (endLoop == 0) {

                        Double.parseDouble(String.valueOf(testString.charAt(testLocation)));
                        testLocation++;

                    }
                }

                catch (NumberFormatException e) {
                    if (testString.charAt(testLocation) == 'E' || testString.charAt(testLocation) == 'e'){
                        testLocation++;
                        try {

                            while (endLoop == 0) {

                                Double.parseDouble(String.valueOf(testString.charAt(testLocation)));
                                testLocation++;

                            }
                        }

                        catch (NumberFormatException q) {

                            numberOne = Double.parseDouble(testString.substring(startFirstNumber,testLocation));

                        }
                    }
                    numberOne = Double.parseDouble(testString.substring(startFirstNumber,testLocation));

                }
            }

            else {

                numberOne = Double.parseDouble(testString.substring(startFirstNumber,testLocation));

            }

        }

        catch (StringIndexOutOfBoundsException c) {
            return;
        }
    }

    public void getOperator(){

        for (char operator : operators) {

            if (testString.indexOf(operator) != -1){

                testLocation++;
                operation = operator;
            }
        }
    }

    public void findNumberTwo(){

        try {

            while (endLoop ==0) {

                Double.parseDouble(String.valueOf(testString.charAt(testLocation)));
                startSecondNumber = testLocation;
                endLoop = 1;

            }
        }

        catch (NumberFormatException c) {

            testLocation++;
            findNumberTwo();
        }

        catch (StringIndexOutOfBoundsException c){
            return;
        }

    }

    public void getNumberTwo(){

        endLoop = 0;

        try {

            while (endLoop ==0) {

                Double.parseDouble(String.valueOf(testString.charAt(testLocation)));
                testLocation++;

            }

        }

        catch (NumberFormatException c) {

            if (testString.charAt(testLocation) == '.'){
                testLocation++;

                try {

                    while (endLoop == 0) {

                        Double.parseDouble(String.valueOf(testString.charAt(testLocation)));
                        testLocation++;

                    }
                }

                catch (StringIndexOutOfBoundsException e){
                    testLocation--;
                    numberTwo = Double.parseDouble(testString.substring(startSecondNumber, testLocation));

                }

                catch (NumberFormatException e) {

                    if (testString.charAt(testLocation) == 'E' || testString.charAt(testLocation) == 'e'){
                        testLocation++;
                        try {

                            while (endLoop == 0) {

                                Double.parseDouble(String.valueOf(testString.charAt(testLocation)));
                                testLocation++;

                            }
                        }

                        catch (NumberFormatException g) {
                            System.out.println(testLocation);
                            numberTwo = Double.parseDouble(testString.substring(startSecondNumber,testLocation));

                        }

                        catch (StringIndexOutOfBoundsException r){
                            numberTwo = Double.parseDouble(testString.substring(startSecondNumber,testLocation));

                        }
                    }
                }
            }

            else {
                numberTwo = Double.parseDouble(testString.substring(startSecondNumber,testLocation));


            }
        }

        catch (StringIndexOutOfBoundsException e){
            numberTwo = Double.parseDouble(testString.substring(startSecondNumber, testLocation));

        }

    }

}
