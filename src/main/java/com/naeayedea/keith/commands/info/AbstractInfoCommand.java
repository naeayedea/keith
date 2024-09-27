package com.naeayedea.keith.commands.info;
import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.AbstractCommand;

import java.util.List;

public abstract class AbstractInfoCommand extends AbstractCommand {

    public AbstractInfoCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractInfoCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ALL;
    }

}
