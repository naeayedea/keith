package com.naeayedea.keith.commands.generic;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.AbstractCommand;

public abstract class AbstractUserCommand extends AbstractCommand {

    public AbstractUserCommand(String name) {
        super(name);
    }

    public AbstractUserCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

}
