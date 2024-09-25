package com.naeayedea.keith.commands.admin.utilities;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.Command;

public abstract class OwnerCommand extends Command {

    public OwnerCommand(String name) {
        super(name);
    }

    public OwnerCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

}
