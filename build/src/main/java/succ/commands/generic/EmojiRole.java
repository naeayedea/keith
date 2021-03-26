package succ.commands.generic;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.Database;
import succ.util.ServerManager;
import succ.util.Emoji;
import succ.util.logs.ConsoleLogger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiRole extends UserCommand {

    Map<String, Boolean> activeEmojis;
    ArrayList<Emoji> emojiRoles;
    private Database database;
    private ServerManager serverManager;
    private JDA jda;
    private String emojiRegex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
    private ConsoleLogger log;
    public EmojiRole(Database database, ServerManager serverManager, JDA jda){
        this.database = database;
        this.serverManager = serverManager;
        this.jda = jda;
        activeEmojis = new HashMap<>();
        emojiRoles = new ArrayList<>();
        log = new ConsoleLogger();
        getEmojiRoles();
    }

    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "emoji: \"links emoji and roll together do emoji [link/remove] [role] [emoji]\"\n\"use emoji set [messageid] to set the active role message\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        if(event.getChannel() instanceof PrivateChannel) {
            event.getChannel().sendMessage("Command not supported in private channel!").queue();
            return;
        }
        Message message = event.getMessage();
        String messageRaw = message.getContentDisplay().trim();
        messageRaw = messageRaw.substring(6).trim();
        String[] args = messageRaw.split("\\s+");
        List<Role> roles = message.getMentionedRoles();
        List<Emote> emotes = message.getEmotes();
        ArrayList<String> unicode = new ArrayList<>();
        Pattern unicodePattern = Pattern.compile(emojiRegex);
        Matcher matcher = unicodePattern.matcher(messageRaw);

        while(matcher.find()){
            unicode.add(matcher.group());
        }

        ArrayList<Emoji> emojis = new ArrayList<>();
        //wrap unicode emojis and custom emotes within emoji class
        for(Emote emote : emotes){
            Emoji emoji = new Emoji(emote.getGuild().getIdLong(), emote.getId(), false);
            emojis.add(emoji);
        }
        for(String string : unicode){
            System.out.println("match "+string);
            Emoji emoji = new Emoji(string, true);
            emojis.add(emoji);
        }

        if(args.length<2 && !(args[0].equals("set"))|| (args[0].equals("add") && roles.size()<=0) || args[0].equals("remove") && emojis.size()<=0){
            event.getChannel().sendMessage("Insufficient Arguments!").queue();
            return;
        }
        if(args[0].equals("link")){
            Emoji emoji = emojis.get(0);
            activeEmojis.put(emoji.getEmoji(), true);
            Guild guild = event.getGuild();
            Role role = roles.get(0);
            if(database.addEmoji(guild.getId(),""+emoji.getSourceId(),role.getId(), emoji.getEmoji(), emoji.isUnicode())){
                emojiRoles.add(new Emoji(guild.getIdLong(),emoji.getSourceId(), role.getId(), emoji.getEmoji(), emoji.isUnicode()));
                Message roleMessage = serverManager.getRoleMessage(guild);
                if(roleMessage!=null){
                    if(emoji.isUnicode())
                        roleMessage.addReaction(emoji.getEmoji()).queue();
                    else{
                        Emote emote = guild.getEmoteById(emoji.getEmoji());
                        if(emote!=null){
                            roleMessage.addReaction(emote).queue();
                        }
                    }
                }
                event.getChannel().sendMessage("added link").queue();
            }
        }
        else if(args[0].equals("remove")){
            Emoji emoji = emojis.get(0);
            Guild guild = event.getGuild();
            activeEmojis.remove(emoji.getEmoji());
            database.removeEmoji(event.getGuild().getId(), emojis.get(0).getEmoji());
            Message roleMessage = serverManager.getRoleMessage(guild);
            if(roleMessage!=null){
                if(emoji.isUnicode())
                    roleMessage.clearReactions(emoji.getEmoji()).queue();
                else{
                    Emote emote = guild.getEmoteById(emoji.getEmoji());
                    if(emote!=null){
                        roleMessage.clearReactions(emote).queue();
                    }
                }
                event.getChannel().sendMessage("removed link").queue();
            }
        }
        else if(args[0].equals("set")){
            MessageChannel channel = event.getChannel();
            channel.getHistoryAround(event.getMessage().getId(),50).queue((history)->{
                List<Message> list = history.getRetrievedHistory();
                for(int j=1; j<list.size();j++){
                    Message oldMessage = list.get(j);
                    if(oldMessage.getAuthor().equals(event.getAuthor())){
                        Guild guild = event.getGuild();
                        Message previousRoleMessage = serverManager.getRoleMessage(guild);
                        if(!(previousRoleMessage==null))
                            previousRoleMessage.clearReactions().queue();
                        serverManager.setRoleMessage(guild, oldMessage);
                        channel.addReactionById(oldMessage.getId(),"U+2757").queue();
                        for(Emoji emoji : emojiRoles){
                            if(emoji.getServerId()==guild.getIdLong()){
                                if(!emoji.isUnicode()){
                                    Guild emojiSource = jda.getGuildById(emoji.getSourceId());
                                    if(emojiSource==null)
                                        continue;
                                    Emote emote =  emojiSource.getEmoteById(emoji.getEmoji());
                                    if(emote==null)
                                        continue;
                                    System.out.println("adding custom to new role");
                                    channel.addReactionById(oldMessage.getId(), emote).queue();
                                }else
                                    channel.addReactionById(oldMessage.getId(),emoji.getEmoji()).queue();
                            }
                        }
                        message.delete().queue();
                        return;
                    }
                }
                event.getChannel().sendMessage("couldn't find a message to set").queue();
            }, (failure)->{
                event.getChannel().sendMessage("couldn't find a message to set").queue();
            });
        }
    }

    //get emoji roles from database
    private void getEmojiRoles(){
        ArrayList<String> emojis = database.getEmojis();
        int n = 0;
        for(String result : emojis){
            try{
            String[] args = result.split("\\s+");
            Emoji emoji;
            if(args[4].equals("1"))
                args[4]="true";
                emoji = new Emoji(Long.parseLong(args[0]), Long.parseLong(args[1]), args[2], args[3], Boolean.parseBoolean(args[4]));

            Guild server = jda.getGuildById(emoji.getServerId());
            Role role = server.getRoleById(emoji.getRoleId());
            if(server==null || role==null)
                continue;
            emojiRoles.add(emoji);
            activeEmojis.put(emoji.getEmoji(), true);
            n++;
            }
            catch(Exception e){
                continue;
            }
        }
        log.printSuccess("Reloaded "+n+" emojis");
    }

    public Map<String, Boolean> getActiveEmojis(){
        return activeEmojis;
    }

    public ArrayList<Emoji> getEmojis(){
        return emojiRoles;
    }
}
