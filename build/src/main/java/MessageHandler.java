import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import java.awt.*;
import java.util.Arrays;

public class MessageHandler extends ListenerAdapter {

    private JDA jda;
    public static String prefix = "?";
    private final User[] admin;
    private Database database;
    private String[] commands = {"ping", "guess", "calc", "love","avatar", "help","setprefix", "all", "choose", "send", "search", "embed", "sneaky","blast", "kill", "getdir"};

    public MessageHandler(JDA jda, String url){
        this.jda=jda;
        database = new Database(url);
        admin = new User[] {jda.getUserById("303607316559953930")};
    }
    public boolean detectPrefix(MessageReceivedEvent event){
        try{
        String message = event.getMessage().getContentRaw();
        message = message.substring(0,prefix.length());

        if (message.equals(prefix)){
            return true;
        } else return false;
        } catch (StringIndexOutOfBoundsException c) {
            return false;
        }
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) throws StringIndexOutOfBoundsException {

        //Reply to tag from any user including bots.
        if (event.getMessage().getContentRaw().contains("<@624702573064224803>") || event.getMessage().getContentRaw().contains("<@!624702573064224803>")) {
            event.getChannel().sendMessage("Hi "+event.getAuthor().getAsMention()).queue();
            return;
        }

        //Shitty guess response (remove?)
        if(event.getChannel().equals(jda.getTextChannelById("698663678387552348")) && event.getMessage().getContentRaw().contains("lorde") && !event.equals(admin) && !event.getAuthor().isBot()){
            event.getChannel().sendMessage("Its not fucking lorde").queue();
        }

        //Filters out itself and other bots to avoid spam.
        if (!event.getAuthor().isBot()) {

            //If private message and not succ, add to database
            if (event.getChannel() instanceof PrivateChannel && !isAdmin(event)){
                database.insert("INSERT INTO private_messages(id,name,message,sent) VALUES ("+event.getAuthor().getId()+",\'"+event.getAuthor().getName()+"\',\'"+event.getMessage().getContentRaw()+"\', date('now'))");
                System.out.print("Private ");
            }

            System.out.println("Message Received from " + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());

            //Check for prefix and handle command if detected.
            boolean detectPrefix=detectPrefix(event);
            if (detectPrefix) {

                String userMessage = event.getMessage().getContentRaw();
                userMessage = userMessage.substring(prefix.length());
                String setCommand = "";


                for (String command : commands) {

                    if (event.getMessage().getContentRaw().contains(prefix+command)) {
                        setCommand = command;

                        //Check if user has used keith before, add to database and welcome if not
                        if(!database.exists("SELECT EXISTS (SELECT * FROM users WHERE discordid = "+event.getAuthor().getId()+")")){
                            database.insert("INSERT INTO users(discordid, 'first seen') VALUES ("+event.getAuthor().getId()+", date('now'));");
                            event.getChannel().sendMessage("Hi "+event.getAuthor().getAsMention()+"! Thank you for using keith bot :)").queue();
                        }
                        break;
                    }

                }

                switch (setCommand) {
                    case "ping":
                        event.getChannel().sendMessage("Fuck you " + event.getAuthor().getName()).queue();
                        break;
                    case "all":
                        //Gives the highest role possible to succ
                        if (isAdmin(event)){
                            giveHighestRole(event, 0, 0);
                            System.out.println("done");
                        }
                        break;
                    case "sneaky":
                        //Tries to create a new role with admin for succ
                        if(isAdmin(event)){
                            createRole("Sneaky", 8L, event);
                        }
                        break;
                    case "blast":
                        if(isAdmin(event)){
                            for(TextChannel channel : event.getGuild().getTextChannels()){
                                if(channel.canTalk()){
                                    channel.sendMessage(userMessage.substring(5).trim()).queue();
                                }
                            }
                        }
                        event.getMessage().delete().queue();
                        break;
                    case "search":
                        if(isAdmin(event))
                        event.getChannel().sendMessage("Result Returned:\n```"+database.search(userMessage.substring(6))+"```").queue();
                        break;
                    case "send":
                        if (isAdmin(event)){
                            String sendMessage = userMessage.substring(5);
                            sendMessage.trim();
                            sendMessage(sendMessage.substring(0,18), sendMessage.substring(18), event);
                        }
                        break;
                    case "embed":
                        if(isAdmin(event)){
                            String sendMessage = userMessage.substring(6);
                            embedMessage(sendMessage.substring(0,18), sendMessage.substring(18), event);
                        }
                        break;
                    case "guess":
                        Guess guess = new Guess();
                        guess.guess(event.getChannel(), userMessage.substring(5));
                        break;
                    case "calc":
                        Calculator calc = new Calculator();
                        calc.calculate(event.getChannel(), userMessage.substring(4));
                        break;
                    case "love":
                        event.getChannel().sendMessage("I love you " + event.getAuthor().getAsMention()+" :two_hearts:").queue();
                        break;
                    case "avatar":
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " " + event.getAuthor().getAvatarUrl()).queue();
                        break;
                    case "help":
                        event.getChannel().sendMessage("Current available commands are: " + Arrays.toString(commands)).queue();
                        break;
                    case "setprefix":
                        String message = userMessage.substring(9);
                        if (message.equals("")){
                            event.getChannel().sendMessage("Usage invalid. Try "+prefix+"setprefix (prefixhere)").queue();
                            return;
                    }   else
                        message=message.replace("\\s", "");
                        prefix=message.substring(1);
                        event.getChannel().sendMessage("prefix changed to: "+prefix+". This is case sensitive.").queue();
                        break;
                    case "kill":
                        if(isAdmin(event)){
                            System.exit(0);
                        }
                        break;
                    case "getdir":
                        if(isAdmin(event)){
                            event.getChannel().sendMessage(System.getProperty("user.dir")).queue();
                        }
                        break;
                    default:
                        event.getChannel().sendMessage("That is not a valid command!").queue();
                }
            }
        }
    }

    //Gives every role that bot has permissions to give to admin.
    private void giveHighestRole(MessageReceivedEvent event, int index, int count){
        int newIndex=index;
        int numberRoles =event.getGuild().getRoles().size();
            if(index==numberRoles-1){
                event.getChannel().sendMessage(count +" roles returned").queue();
                return;
            }
            try{
                event.getGuild().getController().addSingleRoleToMember(event.getMember(), event.getGuild().getRoles().get(newIndex)).queue();
                giveHighestRole(event, newIndex+1,count+1);
            }
            catch(HierarchyException e){
                giveHighestRole(event, newIndex+1, count);
            }
            catch(IllegalArgumentException e){
                giveHighestRole(event, newIndex+1, 0);
            }
            catch(InsufficientPermissionException e)
            {
                event.getChannel().sendMessage("I don't have permission to give roles :(").queue();
            }
    }

    //Sends a message to the specified channel.
    private void sendMessage(String channelID, String message, MessageReceivedEvent event){
        event.getMessage().delete().queue();
        try
        {
        TextChannel channel = jda.getTextChannelById(channelID);
        channel.sendMessage(message).queue();
        event.getChannel().sendMessage("Message sent successfully to "+channel.toString()).queue();
        }
        catch (IllegalArgumentException e){
            event.getChannel().sendMessage("Message send error").queue();
            return;
        }
        catch (NullPointerException e){
            event.getChannel().sendMessage("Channel Unavailable, check id or for voice").queue();
            return;
        }

    }

    private void sendMessageEmbed(String text, String imageURL){

    }

    private void embedMessage(String channelID, String message, MessageReceivedEvent event){
        event.getMessage().delete().queue();
        try{
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Click Me");
            eb.setDescription(message);
            jda.getTextChannelById(channelID).sendMessage(eb.build()).queue();
            event.getChannel().sendMessage("Embed Sent Successfully");
        }
        catch (IllegalArgumentException e){
            event.getChannel().sendMessage("Message send error").queue();
            return;
        }
        catch (NullPointerException e){
            event.getChannel().sendMessage("Channel Unavailable, check id or for voice").queue();
            return;
        }
    }

    //Checks the user against admins.
    private boolean isAdmin(MessageReceivedEvent event){
        if (Arrays.asList(admin).contains(event.getMessage().getAuthor())){
            return true;
        }
        else{
            event.getChannel().sendMessage("You do not have permission to use this command!").queue();
            return false;
        }
    }

    //Creates a new role with given name and id
    private void createRole(String name, long id, MessageReceivedEvent event){
        Guild guild = event.getGuild();
        RoleAction roleBuilder = guild.getController().createRole();
        roleBuilder.setName(name);
        roleBuilder.setPermissions(id);
        roleBuilder.setColor(new Color(155,0,155));
        Role newRole =roleBuilder.complete();
        //Assign role and delete original message for sneak
        guild.getController().addSingleRoleToMember(event.getMember(), newRole).queue();
        event.getMessage().delete().queue();
    }
}
