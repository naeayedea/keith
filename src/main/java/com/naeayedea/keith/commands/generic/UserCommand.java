package com.naeayedea.keith.commands.generic;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.Command;

public abstract class UserCommand extends Command {

    public UserCommand(String name) {
        super(name);
    }

    public UserCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

}
