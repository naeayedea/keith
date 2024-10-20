package com.naeayedea.keith.commands.impl.text;

import java.util.List;

public abstract class AbstractTextCommandPortal extends AbstractTextCommand implements CommandPortal {

    public AbstractTextCommandPortal(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden, int cost) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden, cost);
    }

    public AbstractTextCommandPortal(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractTextCommandPortal(String name, List<String> commandAliases, boolean isPrivateMessageCompatible) {
        super(name, commandAliases, isPrivateMessageCompatible);
    }

    public AbstractTextCommandPortal(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public final boolean isPrivateMessageCompatible() {
        return true;
    }

    @Override
    public int getTimeOut() {
        return 30;
    }

    @Override
    public boolean sendTyping() {
        return true;
    }

    @Override
    public int getCost() {
        return 1;
    }
}
