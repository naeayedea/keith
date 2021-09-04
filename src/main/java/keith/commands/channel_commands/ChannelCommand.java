package keith.commands.channel_commands;

import keith.managers.UserManager.User;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface ChannelCommand {

    void evaluate(Message channel, List<String> message, User user);
}
