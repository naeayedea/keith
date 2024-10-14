package com.naeayedea.keith.commands.lib.provider;

import com.naeayedea.keith.commands.lib.MessageContext;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public interface MessageEmbedProvider<T extends Enum<T>> {

    List<MessageEmbed> getEmbeds(MessageContext<T> context) throws KeithPermissionException, KeithExecutionException;

}
