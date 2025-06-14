package com.naeayedea.keith.core.commands.lib;

import com.naeayedea.keith.core.commands.lib.input.CommandInput;
import com.naeayedea.keith.core.commands.lib.strategy.CommandStrategy;
import com.naeayedea.keith.core.commands.lib.output.CommandOutput;
import com.naeayedea.keith.core.exception.KeithExecutionException;
import com.naeayedea.keith.core.exception.KeithGracefulErrorException;
import com.naeayedea.keith.core.exception.KeithPermissionException;
import org.springframework.lang.NonNull;

public abstract class AbstractChannelCommand<I extends CommandInput, O extends CommandOutput, S extends CommandStrategy> extends AbstractCommand {

    public AbstractChannelCommand(String internalName, String nameKey, String aliasKey) {
        super(internalName, nameKey, aliasKey);
    }

    public AbstractChannelCommand(String internalName, String nameKey, String aliasKey, int cost) {
        super(internalName, aliasKey, nameKey, cost);
    }

    @NonNull
    public abstract O run(@NonNull I input, @NonNull S strategy) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException;

}
