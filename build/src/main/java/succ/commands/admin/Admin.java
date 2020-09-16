package succ.commands.admin;

import succ.util.Database;
import succ.util.UserManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows any user with administrator permissions to perform additional commands (warning - can be dangerous)
 */
public class Admin extends AdminCommand {

    private Map<String, AdminCommand> admin_commands;
    private UserManager userManager;
    public Admin(Database database){
        userManager = new UserManager(database);
        initialiseCommands();
    }
    @Override
    public String getDescription() {
        return "admin: \"gateway for all admin commands - 'do [prefix]admin [command]'\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        net.dv8tion.jda.api.entities.User user = event.getAuthor();
        AdminCommand command = findCommand(event);
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
        admin_commands = new HashMap<String, AdminCommand>();
        admin_commands.put("sneaky", new Sneaky());
        admin_commands.put("updatelevel", new UpdateLevel(userManager));


        admin_commands.put("help", new succ.commands.admin.Help(admin_commands)); //always initialise help last
    }

    private AdminCommand findCommand(MessageReceivedEvent event){
        String command = event.getMessage().getContentDisplay();        //Get raw message
        String[] commandSplit = command.split("\\s+");            //Split into individual arguments
        command = command.substring(commandSplit[0].length()).trim();          //Remove initial [index]admin
        commandSplit = command.split("\\s+");                     //Split subcommand into individual arguments
        String subCommand = commandSplit[0];
        return admin_commands.get(subCommand);
    }

}
