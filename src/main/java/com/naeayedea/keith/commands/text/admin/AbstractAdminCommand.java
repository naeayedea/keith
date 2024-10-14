package com.naeayedea.keith.commands.text.admin;

import com.naeayedea.keith.commands.text.AbstractCommand;
import com.naeayedea.keith.commands.text.AccessLevel;

import java.util.List;

public abstract class AbstractAdminCommand extends AbstractCommand {

    public AbstractAdminCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractAdminCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ADMIN;
    }
}
