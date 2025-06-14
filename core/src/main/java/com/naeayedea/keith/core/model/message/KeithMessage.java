package com.naeayedea.keith.core.model.message;

import com.naeayedea.keith.core.model.channel.KeithMessageChannel;
import com.naeayedea.keith.core.model.user.KeithUser;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface KeithMessage {

    KeithUser getAuthor();

    Optional<KeithMessageChannel> getChannel();

    String getMessageAsContent();

    List<String> getMessageAsTokens();

    List<KeithUser> mentionedUsers();

    Instant getTimestamp();

    Optional<KeithMessage> getRepliedTo();
}
