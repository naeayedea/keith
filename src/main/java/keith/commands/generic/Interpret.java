package keith.commands.generic;

import keith.commands.generic.lox.Lox;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
public class Interpret extends UserCommand {

    public Interpret() {
        super("interpret");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+super.getShortDescription(prefix);
    }

    @Override
    public String getLongDescription() {
        return super.getLongDescription();
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        String raw = event.getMessage().getContentRaw();
        List<String> results = (new Lox()).run(raw.substring(raw.indexOf('`')));
        event.getChannel().sendMessage(String.join("\n", results)).queue();
    }
}
