package succ.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import succ.util.logs.ConsoleLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows bot to manager discord servers as well
 * as accessing relevant information from the database
 * for mesage verification and custom prefixes.
 */
public class ServerManager {

    private Database database;
    private ConsoleLogger log;
    private Server currentServer;

    public ServerManager(Database database){
        this.database=database;
        log = new ConsoleLogger();
    }

    /**
     * returns a user object from the database
     * @param  guildID   a discord guild id.
     * @returns a user object
     */
    public Server getServer(String guildID){
        if(!retrieveServer(guildID))    //If returns false, server not in database => create
            createServer(guildID);
        retrieveServer(guildID);
        return currentServer;
    }

    /*
     * retrieves the users info from the database.
     * returns true if successful.
     */
    private boolean retrieveServer(String guildID){
        ArrayList<String> rs = database.select("* FROM servers WHERE ServerID = "+guildID);
        try{
            String firstSeen = rs.get(1);
            String prefix = rs.get(2);
            boolean banned = Boolean.parseBoolean(rs.get(3));
            currentServer = new Server(guildID, firstSeen, prefix, banned);
            return true;
        }
        catch (IndexOutOfBoundsException e){
            log.printWarning("server query returning insufficient columns - database issue?");
        }
        return false;
    }

    /**
     * Creates a database entry for a new bot user
     * @param guildID a discord servers id
     * @return true if successful
     */
    public boolean createServer(String guildID){
        log.printSuccess("Inserting server into database");
        return database.insert("(ServerID) VALUES ("+guildID+")", "servers");
    }

    /**
     * bans a server with the given id, bot will ignore all messages in this server
     * only reversable with direct database access to avoid exploitation.
     */
    public void banServer(String guildID){
        database.update("banned = true WHERE ServerID = "+guildID, "servers");
    }

    /**
     * Updates the given servers prefix to the specified string
     */

    public boolean setPrefix(String guildID, String prefix){
        return database.update("prefix = '"+prefix+"' WHERE ServerID = "+guildID, "servers");
    }

    public Message getRoleMessage(Guild guild){
        ArrayList<String> rs = database.select("emoji_message_id, emoji_message_channel FROM servers WHERE ServerID ="+guild.getId());
        if(rs==null)
            return null;
        try{
            String messageId = rs.get(0);
            String channelId = rs.get(1);
            if(!messageId.equals("") && !channelId.equals("")){
                final Message[] roleMessage = new Message[1];
                MessageChannel channel = guild.getTextChannelById(channelId);
                channel.retrieveMessageById(messageId).queue((message)->{
                    roleMessage[0] =message;
                }, (failure) -> {});
                Thread.sleep(500);
                return roleMessage[0];
            }
            return null;
        } catch(IndexOutOfBoundsException e){
            log.printWarning("role channel query returning insuffient columns, database issue?");
            return null;
        } catch (InterruptedException e) {
            return null;
        }
    }

    public boolean setRoleMessage(Guild guild, Message roleMessage){
        database.update("emoji_message_id = '"+roleMessage.getId()+"' WHERE ServerID = "+guild.getId(), "servers");
        return database.update("emoji_message_channel = '"+roleMessage.getChannel().getId()+"' WHERE ServerID = "+guild.getId(), "servers");
    }
}
