package keith.managers;

import keith.commands.channel_commands.IChannelCommand;

import java.util.HashMap;
import java.util.Map;

public class ChannelCommandManager {

    private static ChannelCommandManager instance;

    private final Map<String, IChannelCommand> gamesInProgress;

    private ChannelCommandManager() {
        gamesInProgress = new HashMap<>();
    }

    public boolean gameInProgress(String channelId) {
        return gamesInProgress.get(channelId) != null;
    }

    public boolean addGame(String channelId, IChannelCommand newGame) {
        if (gameInProgress(channelId)) {
            return false;
        } else {
            gamesInProgress.put(channelId, newGame);
            return true;
        }
    }

    public void removeGame(String channelId) {
        gamesInProgress.remove(channelId);
    }

    public IChannelCommand getGame(String channelId) {
        return gamesInProgress.get(channelId);
    }

    public static ChannelCommandManager getInstance() {
        if (instance == null) {
            instance = new ChannelCommandManager();
        }
        return instance;
    }



}
