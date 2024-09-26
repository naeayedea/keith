package com.naeayedea.keith.commands.info;
import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.AbstractCommand;

public abstract class AbstractInfoCommand extends AbstractCommand {

    public AbstractInfoCommand(String name) {
        super(name);
    }

    public AbstractInfoCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ALL;
    }

}
