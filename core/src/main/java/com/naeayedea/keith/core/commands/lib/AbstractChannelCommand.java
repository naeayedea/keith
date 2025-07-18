package com.naeayedea.keith.core.commands.lib;

import com.naeayedea.keith.common.model.command.input.CommandInput;
import com.naeayedea.keith.core.commands.lib.strategy.CommandStrategy;
import com.naeayedea.keith.common.model.command.output.CommandOutput;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
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
