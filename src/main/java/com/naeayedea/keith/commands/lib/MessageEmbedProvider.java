package com.naeayedea.keith.commands.lib;

import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public interface MessageEmbedProvider<T extends Enum<T>> {

    List<MessageEmbed> getEmbeds(MessageContext<T> context) throws KeithPermissionException, KeithExecutionException;

}
