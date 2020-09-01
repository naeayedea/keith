import net.dv8tion.jda.api.entities.MessageChannel;
import java.util.ArrayList;


public class Calculator {
    /*third iteration of calculator. This version uses regex to locate numbers and operators and will
     * attempt to accurately perform calculations involving multiple operations.
     *
     * Please note that this version cannot handle negative numbers or brackets. Future versions will
     * attempt to tackle this.
     */


    // field declarations.
    String operatorRegex = "[+\\-/*^!]";
    String numberRegex = "\\d+(\\.\\d+)?+([E]\\d+)?";
    ArrayList<String> numbers = new ArrayList<String>();
    ArrayList<Character> operators = new ArrayList<Character>();
    ArrayList<String> calculations = new ArrayList<String>();
    String[] negativeHandler = {"+-", "--", "*-", "\\-", "^-", "-+", "-*", "-\\", "-^"};
    String[] operatorsArray = {"+", "-", "*", "/", "^", "!"};
    //Processor is what is used to actually perform each individual calculation.
    Processor processor = new Processor();

    public void calculate(MessageChannel channel, String calculation){

        calculation = calculation.replace("\\s+", "");
        if (calculation.equals("")){
            channel.sendMessage("That is not a valid command. Please type "+MessageHandler.prefix+"help for help.").queue();
            return;
        }
        try{
        initialiseCalculation(channel, calculation);
        }

        catch (NumberFormatException c) {

            channel.sendMessage("That is not a valid calculation. Please try again!").queue();
        }
    }
    public void initialiseCalculation(MessageChannel channel, String testString){

        //Below is a placeholder error handler for calculations using negative numbers as Calculatorv3 cannot handle negatives.
        testString = testString.replaceAll("\\s+", "");
        for (String error : negativeHandler){

            if(testString.substring(1,2).equals(error) || testString.contains(error)){
                channel.sendMessage("There was a syntax error detected! Please note this version of calc cannot handle negatives, sorry").queue();
                 return;
            }
        }
        for (int i =0; i<5; i++){
            if (testString.substring(0,1).equals(operatorsArray[i]) || testString.substring(testString.length()-1).equals(operatorsArray[i])){
                channel.sendMessage("There was a syntax error detected! Please note this version of calc cannot handle negatives, sorry").queue();
                return;
            }
        }
        /* Finds any operators in the input, removes any empty indexes
        and trims off whitespace in order to isolate only operators.*/

        //If a factorial is detected, the following adds a . so the regex will detect it and separate the operator.
//        if (testString.contains("!")){
//            testString = testString.replace("!", "!");
//        }

        String[] rawOperators =testString.split(numberRegex);
        for (String operator : rawOperators) {
            operator = operator.replaceAll("\\s+", "");
            if (!operator.equals("")){
                if(operator.contains("!")){
                    operators.add('!');
                } else
                    operators.add(operator.charAt(0));
            }
        }

        //finds and isolates any numbers and trims whitespace.
        String[] numbersRaw = testString.split(operatorRegex);
        for (String number : numbersRaw){
            number = number.replaceAll("\\s+", "");
            if (!number.equals("")){
                numbers.add(number);
            }
        }
        executeBOMDAS(channel);
    }

    public void executeBOMDAS(MessageChannel channel){

        //Deal with factorials first.
        for (int i = 0; i < operators.size(); i++){

            if (operators.get(i).equals('!'))
            {
                double number = Double.parseDouble(numbers.get(i));
                if (number< 0){
                    channel.sendMessage("undefined").queue();
                }

                numbers.set(numbers.indexOf(numbers.get(i)), processor.calculate(number,operators.get(i),processor.TYPE_FACTORIAL));
                operators.remove(i);
                i--;
            }
        }

        //Next deal with powers.
        for (int i = 0; i < operators.size(); i++){

            if (operators.get(i).equals('^'))
            {
                double numberOne = Double.parseDouble(numbers.get(i));
                double numberTwo = Double.parseDouble(numbers.get(i+1));
                numbers.set(i, processor.calculate(numberOne,operators.get(i),numberTwo));
                numbers.remove(i+1);
                operators.remove(i);
                i--;
            }
        }

        //Next deal with multiplication and then addition from left to right.
        for (int i = 0; i < operators.size(); i++){

            if (operators.get(i).equals('*')||operators.get(i).equals('/'))
            {
                double numberOne = Double.parseDouble(numbers.get(i));
                double numberTwo = Double.parseDouble(numbers.get(i+1));
                numbers.set(i, processor.calculate(numberOne,operators.get(i), numberTwo));
                numbers.remove(i+1);
                operators.remove(i);
                i--;
            }
        }

        //Next deal with adding and subtracting from left to right.
        for (int i = 0; i < operators.size(); i++){

            if (operators.get(i).equals('+')||operators.get(i).equals('-'))
            {
                double numberOne = Double.parseDouble(numbers.get(i));
                double numberTwo = Double.parseDouble(numbers.get(i+1));
                numbers.set(i, processor.calculate(numberOne,operators.get(i),numberTwo));
                numbers.remove(i+1);
                operators.remove(i);
                i--;
            }
        }
        //if all is well the following should be the correct answer.
        System.out.println("Answer returned = "+numbers.get(0));
        channel.sendMessage("The answer is: "+numbers.get(0)).queue();
    }

}
