package com.naeayedea.keith.platform.discord.command.lib;

import com.naeayedea.keith.core.exception.KeithExecutionException;
import com.naeayedea.keith.core.exception.KeithGracefulErrorException;
import com.naeayedea.keith.core.exception.KeithPermissionException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.lang.NonNull;

import java.util.List;

public interface TextCommandHandler extends CommandHandler {

    /**
     * Determine if a command is usable in private messages
     *
     * @return true if compatible, false otherwise.
     */
    boolean isPrivateMessageCompatible();

    /**
     * Runs the given command
     *
     * @param event  the event which triggered the command
     * @param tokens a list of tokens of the message from the user
     */
    void run(@NonNull MessageReceivedEvent event, @NonNull List<String> tokens) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException;

    /**
     * Determine if a command is hidden
     *
     * @return true if hidden, false otherwise
     */
    boolean isHidden();

}
