package succ;

import net.dv8tion.jda.api.entities.PrivateChannel;
import succ.commands.admin.Admin;
import succ.commands.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import succ.commands.Help;
import succ.util.Database;
import succ.util.UserManager;
import succ.util.logs.ConsoleLogger;
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
    UserManager userManager;
    public MessageHandler(JDA jda, String url){
        this.jda = jda;
        database = new Database(url);
        log = new ConsoleLogger();
        userManager = new UserManager(database);
        initialiseCommands();
        prefix = "^";
    }

    //Populate hashmap with all available commands, when key is entered the relevant command is returned which can be ran.
    private void initialiseCommands(){
        commands = new HashMap<String, Command>();
        commands.put("admin", new Admin(database));
        commands.put("help", new Help(commands)); //Always initialise help last
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

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getChannel().getId().equals("622761218532179968") || event.getChannel().getId().equals("699019273615704067")){
        net.dv8tion.jda.api.entities.User user = event.getAuthor();
            if(!user.isBot()){     //Filter out bot accounts
                if(!(userManager.getUser(user.getId()).getAccessLevel()==0)){
                    if(event.getChannel() instanceof TextChannel){
                        publicMessageReceived(event);                   //Log operations
                    }
                    if(event.getChannel() instanceof PrivateChannel){
                        privateMessageReceived(event);                  //Log operations
                    }
                    if(detectPrefix(event)){                            //Search beginning of message for server prefix
                        Command command = findCommand(event);
                        if(command!=null && userManager.getUser(user.getId()).getAccessLevel()>=command.getAccessLevel()){                              //If command found, perform.
                            if(!(userManager.getUser(user.getId()).getCommandCount() > 0)){      //check if user has used bot before, if no send welcome message
                                event.getChannel().sendMessage("Hi "+user.getAsMention()+", thank you for using keith! Type \"" +prefix+"help\" to see a list of commands!").queue();
                                System.out.println("fat cunt");
                            }
                            command.run(event);
                            userManager.incrementCommandCount(user.getId());
                        }
                        else if(command==null){
                            event.getChannel().sendMessage("That is not a valid command!").queue();
                        }
                        else{
                            event.getChannel().sendMessage("You do not have permission to use this command!").queue();
                            System.out.println("pussy");
                        }
                    }
                }
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

    private Command findCommand(MessageReceivedEvent event){
        String command = event.getMessage().getContentDisplay();
        command = command.substring(prefix.length());
        String[] commandSplit = command.split("\\s+");
        return commands.get(commandSplit[0]);
    }

//    //adds user to database
//    private void addUserToDatabase(net.dv8tion.jda.api.entities.User user, MessageReceivedEvent event){
//        userManager.createUser(user.getId());
//        event.getChannel().sendMessage("Hi "+user.getAsMention()+", thank you for using keith! Type \"" +prefix+"help\" to see a list of commands!").queue();
//    }
}
