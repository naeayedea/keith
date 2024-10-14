package com.naeayedea.keith.commands.lib.provider;

import com.naeayedea.keith.commands.lib.MessageContext;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;

public interface MessageContentProvider<T extends Enum<T>> {

    String getContent(MessageContext<T> context) throws KeithPermissionException, KeithExecutionException;
}
