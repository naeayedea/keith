package com.naeayedea.keith.common.model.command.input;

import java.util.Map;

public interface TextOptionsCommandInput extends CommandInput {

    Map<String, String> getOptions();

    String getOption(String key);

}
