package com.naeayedea.keith.model.chat;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public record ChatCandidate(MessageChannel channel, Guild guild, long connectionTime) {
}
