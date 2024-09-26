package com.naeayedea.keith.commands.admin;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.AbstractCommand;

public abstract class AbstractAdminCommand extends AbstractCommand {

    public AbstractAdminCommand(String name) {
        super(name);
    }

    public AbstractAdminCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ADMIN;
    }
}
