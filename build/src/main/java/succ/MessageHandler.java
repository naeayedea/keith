package succ;

import net.dv8tion.jda.api.entities.PrivateChannel;
import succ.commands.admin.Admin;
import succ.commands.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import succ.logs.util.ConsoleLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Main message handler for keithv2, receives incoming messages and performs tasks depending on message content
 */
public class MessageHandler extends ListenerAdapter {

    JDA jda;
    Map<String, Command> commands;
    String prefix;
    Database database;
    ConsoleLogger log;
    public MessageHandler(JDA jda, String url){
        this.jda = jda;
        database = new Database(url);
        log = new ConsoleLogger();
        initialiseCommands();
        prefix = "^";
    }

    //Populate hashmap with all available commands, when key is entered the relevant command is returned which can be ran.
    private void initialiseCommands(){
        commands = new HashMap<String, Command>();
        commands.put("help", new succ.commands.Help());
        commands.put("admin", new Admin());
    }


    //Search the start of the users message for the prefix, whatever that may be.
    private boolean detectPrefix(MessageReceivedEvent event){
        try{
            String message = event.getMessage().getContentRaw();
            String prefix_search = message.substring(0,prefix.length());

            return prefix_search.equals(prefix) && message.length()>prefix.length();
        } catch (StringIndexOutOfBoundsException c) {
            return false;
        }
    }


    //Handle message event
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(!event.getAuthor().isBot()){     //Filter out bot accounts
            User user = createUser(event);
            if(!user.inDatabase()){
                log.printSuccess("placeholder");
            }
            if(event.getChannel() instanceof TextChannel){
                publicMessageReceived(event);
            }

            if(event.getChannel() instanceof PrivateChannel){
                privateMessageReceived(event);
            }
        }
    }

    //Performs tasks relevant to public messages
    private void publicMessageReceived(MessageReceivedEvent event){
        log.printPublicMessage(event.getAuthor().getName()+": "+event.getMessage().getContentDisplay());
    }

    //Performs tasks relevant to private messages, stores in private message database.
    private void privateMessageReceived(MessageReceivedEvent event){
        log.printPrivateMessage(event.getAuthor().getName()+": "+event.getMessage().getContentDisplay());
    }

    //Executes the specified command in the channel the command was entered.
    private void runCommand(MessageReceivedEvent event, Command command){

    }

    private User createUser(MessageReceivedEvent event){
        return new User(event.getAuthor().getId(), database);
    }

}
