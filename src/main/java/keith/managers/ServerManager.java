package keith.managers;

import keith.util.Database;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.PreparedStatement;

public class ServerManager {

    private static ServerManager instance;

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    private PreparedStatement createServerStatement() {
        return Database.prepareStatement("INSERT INTO servers (ServerID) VALUES (?)");
    }

    private PreparedStatement prefixStatement() {
        return Database.prepareStatement("SELECT prefix FROM servers WHERE ServerID = ?");
    }

    public void addServer(Guild guild) {
        Database.executeUpdate(createServerStatement(), guild.getId());
    }

    public String getPrefix(Guild guild) {
        return Database.getStringResult(prefixStatement(), guild.getId()).get(0);
    }

    public void setPrefix(Guild guild, String newPrefix) {

    }
}
