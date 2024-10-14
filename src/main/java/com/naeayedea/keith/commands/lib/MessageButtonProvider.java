package com.naeayedea.keith.commands.lib;

import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public interface MessageButtonProvider<T extends Enum<T>> {

    List<Button> getComponents(MessageContext<T> context) throws KeithPermissionException, KeithExecutionException;

}
