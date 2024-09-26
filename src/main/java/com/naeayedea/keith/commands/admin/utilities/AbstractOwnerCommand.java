package com.naeayedea.keith.commands.admin.utilities;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.AbstractCommand;

public abstract class AbstractOwnerCommand extends AbstractCommand {

    public AbstractOwnerCommand(String name) {
        super(name);
    }

    public AbstractOwnerCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

}
