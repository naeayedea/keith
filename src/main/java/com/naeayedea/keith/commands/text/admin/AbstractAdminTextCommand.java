package com.naeayedea.keith.commands.text.admin;

import com.naeayedea.keith.commands.text.AbstractTextCommand;
import com.naeayedea.keith.commands.lib.command.AccessLevel;

import java.util.List;

public abstract class AbstractAdminTextCommand extends AbstractTextCommand {

    public AbstractAdminTextCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractAdminTextCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ADMIN;
    }
}
