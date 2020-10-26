package succ.commands.generic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.commands.EditResponse;
import succ.commands.drivers.CalculatorDriver;

public class Calculator extends UserCommand implements EditResponse {

    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "calc: \"edit a calculation containing any number of simple operators (+,-,*,/,!,^)\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        CalculatorDriver calculator = new CalculatorDriver();
        String rawCommand = event.getMessage().getContentRaw().trim();
        String[] args = rawCommand.split("\\s+");
        String response = calculator.calculate(rawCommand.substring(rawCommand.indexOf(args[1])));
        event.getChannel().sendMessage(response).queue();
    }
}
