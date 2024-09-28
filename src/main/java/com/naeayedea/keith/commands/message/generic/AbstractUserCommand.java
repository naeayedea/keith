package com.naeayedea.keith.commands.message.generic;

import com.naeayedea.keith.commands.message.AbstractCommand;
import com.naeayedea.keith.commands.message.AccessLevel;

import java.util.List;

public abstract class AbstractUserCommand extends AbstractCommand {

    public AbstractUserCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractUserCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

}
