/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.commands.drivers.general.help;

import com.naeayedea.keith.core.commands.lib.Command;
import com.naeayedea.keith.core.commands.lib.BaseCommandMessages;
import com.naeayedea.keith.core.commands.lib.strategy.CommandStrategy;

import java.util.Map;

public interface HelpCommandStrategy extends CommandStrategy {

    Map<String, Command> getCommands();

    String getPrefix();

    String getCommandName();

    HelpCommandContext getContext();

    String getCommandDescription(String commandName, String prefix);

    String getCommandExampleUsage(String commandName, String prefix);

    enum HelpCommandContext {
        SINGLE_COMMAND,
        ALL_COMMANDS
    }

    interface HelpCommandErrorType extends BaseCommandMessages {
        String COMMAND_NOT_FOUND = "translation.i18n.commands.help.messages.command-not-found";
    }
}
