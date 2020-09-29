package succ;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import succ.commands.admin.Admin;
import succ.commands.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import succ.commands.Help;
import succ.commands.generic.Avatar;
import succ.commands.generic.Feedback;
import succ.commands.generic.SetPrefix;
import succ.util.Database;
import succ.util.ServerManager;
import succ.util.UserManager;
import succ.util.logs.ConsoleLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Main message handler for keithv2, receives incoming messages and performs tasks depending on message content
 */
public class MessageHandler extends ListenerAdapter {

    JDA jda;
    Map<String, Command> commands;
    Database database;
    ConsoleLogger log;
    UserManager userManager;
    ServerManager serverManager;
    ExecutorService commandExecutor = Executors.newCachedThreadPool();
    public MessageHandler(JDA jda, String url){
        this.jda = jda;
        database = new Database(url);
        log = new ConsoleLogger();
        userManager = new UserManager(database);
        serverManager = new ServerManager(database);
        initialiseCommands();
        commandExecutor = Executors.newCachedThreadPool();
    }

    //Populate hashmap with all available commands, when key is entered the relevant command is returned which can be ran.
    private void initialiseCommands(){
        commands = new HashMap<String, Command>();
        commands.put("admin", new Admin(database, jda, serverManager));
        commands.put("avatar", new Avatar());
        commands.put("setprefix", new SetPrefix(serverManager));
        commands.put("feedback", new Feedback(userManager, jda));
        commands.put("help", new Help(commands, serverManager)); //Always initialise help last
    }


    //Search the start of the users message for the prefix, whatever that may be.
    private boolean detectPrefix(MessageReceivedEvent event, String prefix){
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
        if(event.getAuthor().isBot() ||  userManager.getUser(event.getAuthor().getId()).getAccessLevel()==0)      //Filter out bots, banned users and servers, waste no time on it.
            return;
        if(!(event.getChannel() instanceof PrivateChannel) && serverManager.getServer(event.getGuild().getId()).isBanned()){
            return;
        }
        new Thread( () -> {
            String prefix;
            if(!(event.getChannel() instanceof PrivateChannel))
                prefix = serverManager.getServer(event.getGuild().getId()).getPrefix();
            else
                prefix = "?";

            if(event.getMessage().getMentionedUsers().contains(jda.getSelfUser())){
                event.getChannel().sendMessage("The current prefix is: "+prefix).queue();
                return;
            }

            User user = event.getAuthor();
            if(!user.isBot()){     //Filter out bot accounts
                if(!(userManager.getUser(user.getId()).getAccessLevel()==0)){
                    if(event.getChannel() instanceof TextChannel){
                        publicMessageReceived(event);                   //Log operations
                    }
                    if(event.getChannel() instanceof PrivateChannel){
                        privateMessageReceived(event);                  //Log operations
                    }
                    if(detectPrefix(event, prefix)){                            //Search beginning of message for server prefix
                        Command command = findCommand(event, prefix);
                        if(command!=null && userManager.getUser(user.getId()).getAccessLevel()>=command.getAccessLevel()){                              //If command found, perform.
                            if(!(userManager.getUser(user.getId()).getCommandCount() > 0)){      //check if user has used bot before, if no send welcome message
                                event.getChannel().sendMessage("Hi "+user.getAsMention()+", thank you for using keith! Type \"" +prefix+"help\" to see a list of commands!").queue();
                            }
                            try {
                                Runnable execution = () -> {command.run(event); userManager.incrementCommandCount(user.getId());};
                                commandExecutor.submit(execution).get(10, TimeUnit.SECONDS);
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                event.getChannel().sendMessage("Something went wrong :(").queue();
                            } catch (TimeoutException e){
                                event.getChannel().sendMessage("Command took to long to execute!").queue();
                            }
                        }
                        else if(command==null){
                        }
                        else{
                            event.getChannel().sendMessage("You do not have permission to use this command!").queue();
                        }
                    }
                }
            }
        }).start();
    }

    //Performs tasks relevant to public messages
    private void publicMessageReceived(MessageReceivedEvent event){
        log.printPublicMessage(event.getAuthor().getName()+": "+event.getMessage().getContentDisplay());
    }

    //Performs tasks relevant to private messages, stores in private message database.
    private void privateMessageReceived(MessageReceivedEvent event){
        log.printPrivateMessage(event.getAuthor().getName()+": "+event.getMessage().getContentDisplay());
    }

    private Command findCommand(MessageReceivedEvent event, String prefix){
        String command = event.getMessage().getContentDisplay();
        command = command.substring(prefix.length());
        String[] commandSplit = command.split("\\s+");
        return commands.get(commandSplit[0]);
    }
}
