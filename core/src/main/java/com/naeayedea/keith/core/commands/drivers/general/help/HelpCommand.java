/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.commands.drivers.general.help;

import com.naeayedea.keith.core.commands.drivers.general.AbstractUserCommand;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.core.commands.lib.Command;
import com.naeayedea.keith.common.model.command.input.TextCommandInput;
import com.naeayedea.keith.common.model.command.output.tiles.TileCommandOutput;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import org.springframework.lang.NonNull;

import java.util.Map;

import static com.naeayedea.keith.core.commands.drivers.general.help.HelpCommandStrategy.HelpCommandErrorType.*;

public class HelpCommand extends AbstractUserCommand<TextCommandInput, TileCommandOutput, HelpCommandStrategy> {

    public HelpCommand(String internalName, String nameKey, String aliasKey) {
        super(internalName, nameKey, aliasKey);
    }

    @Override
    @NonNull
    public TileCommandOutput run(@NonNull TextCommandInput commandInput, @NonNull HelpCommandStrategy strategy) throws KeithGracefulErrorException {
        Map<String, Command> commands = strategy.getCommands();

        if (strategy.getContext() == HelpCommandStrategy.HelpCommandContext.SINGLE_COMMAND) {
            //get the details of the specific command
            Command command = commands.get(strategy.getCommandName());

            if (command == null) {
                throw new KeithGracefulErrorException(COMMAND_NOT_FOUND);
            }

        } else {

        }

        return null;
    }

    @Override
    @NonNull
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return true;
    }
}
