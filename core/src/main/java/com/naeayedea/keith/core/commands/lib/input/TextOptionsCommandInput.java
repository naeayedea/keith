package com.naeayedea.keith.core.commands.lib.input;

import com.naeayedea.keith.core.model.channel.KeithMessageChannel;
import com.naeayedea.keith.core.model.message.KeithMessage;
import com.naeayedea.keith.core.model.user.KeithUser;

import java.util.Map;

public interface TextOptionsCommandInput extends CommandInput {

    Map<String, String> getOptions();

    String getOption(String key);

}
