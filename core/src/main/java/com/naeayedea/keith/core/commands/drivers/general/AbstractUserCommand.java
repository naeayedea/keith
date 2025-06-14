package com.naeayedea.keith.core.commands.drivers.general;

import com.naeayedea.keith.core.commands.lib.AbstractChannelCommand;
import com.naeayedea.keith.core.commands.lib.AccessLevel;
import com.naeayedea.keith.core.commands.lib.input.CommandInput;
import com.naeayedea.keith.core.commands.lib.output.CommandOutput;
import com.naeayedea.keith.core.commands.lib.strategy.CommandStrategy;
import org.springframework.lang.NonNull;

public abstract class AbstractUserCommand<I extends CommandInput, O extends CommandOutput, S extends CommandStrategy> extends AbstractChannelCommand<I, O, S> {

    public AbstractUserCommand(String internalName, String nameKey, String aliasKey) {
        super(internalName, nameKey, aliasKey);
    }

    public AbstractUserCommand(String internalName, String nameKey, String aliasKey, int cost) {
        super(internalName, aliasKey, nameKey, cost);
    }

    @Override
    @NonNull
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }
}
