package com.naeayedea.keith.commands.lib;

import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;

import java.util.List;

public interface MessageComponentProvider<T extends Enum<T>> {

    List<? extends LayoutComponent> getComponents(MessageContext<T> context) throws KeithPermissionException, KeithExecutionException;

}
