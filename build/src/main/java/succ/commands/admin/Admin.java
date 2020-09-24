package succ.commands.admin;

import net.dv8tion.jda.api.JDA;
import succ.commands.Command;
import succ.commands.enhanced.Ban;
import succ.util.Database;
import succ.util.ServerManager;
import succ.util.UserManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows any user with administrator permissions to perform additional commands (warning - can be dangerous)
 */
public class Admin extends AdminCommand {

    private Map<String, Command> admin_commands;
    private UserManager userManager;
    private ServerManager serverManager;
    private JDA jda;
    public Admin(Database database, JDA jda, ServerManager serverManager){
        userManager = new UserManager(database);
        this.serverManager = serverManager;
        this.jda = jda;
        initialiseCommands();
    }
    @Override
    public String getDescription() {
        return "admin: \"gateway for all admin commands - do '[prefix]admin [command]'\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        net.dv8tion.jda.api.entities.User user = event.getAuthor();
        Command command = findCommand(event);
        if(command!=null && userManager.getUser(user.getId()).getAccessLevel()>=command.getAccessLevel()){                              //If command found, perform. && userManager.getUser(user.getId()).getAccessLevel()>=command.getAccessLevel()
            command.run(event);
        }
        else if(command==null){
            event.getChannel().sendMessage("That is not a valid command!").queue();
        }
        else{
            event.getChannel().sendMessage("You do not have permission to use this command!").queue();
        }
    }

    private void initialiseCommands(){
        admin_commands = new HashMap<String, Command>();
        admin_commands.put("sneaky", new Sneaky());
        admin_commands.put("updatelevel", new UpdateLevel(userManager));
        admin_commands.put("sleep", new Sleep());
        admin_commands.put("send", new SendMessage(jda));
        admin_commands.put("ban", new Ban(userManager, serverManager));
        admin_commands.put("help", new succ.commands.admin.Help(admin_commands, serverManager)); //always initialise help last
    }

    private Command findCommand(MessageReceivedEvent event){
        String command = event.getMessage().getContentDisplay();        //Get raw message
        String[] commandSplit = command.split("\\s+");            //Split into individual arguments
        command = command.substring(commandSplit[0].length()).trim();          //Remove initial [index]admin
        commandSplit = command.split("\\s+");                     //Split subcommand into individual arguments
        String subCommand = commandSplit[0];
        return admin_commands.get(subCommand);
    }

}
