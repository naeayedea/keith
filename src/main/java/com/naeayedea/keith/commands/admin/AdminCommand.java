package com.naeayedea.keith.commands.admin;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.Command;

public abstract class AdminCommand extends Command {

    public AdminCommand(String name) {
        super(name);
    }

    public AdminCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ADMIN;
    }
}
