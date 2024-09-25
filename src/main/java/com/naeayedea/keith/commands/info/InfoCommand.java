package com.naeayedea.keith.commands.info;
import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.Command;

public abstract class InfoCommand extends Command {

    public InfoCommand(String name) {
        super(name);
    }

    public InfoCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }


    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ALL;
    }

}
