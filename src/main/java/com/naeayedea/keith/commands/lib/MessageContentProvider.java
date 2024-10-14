package com.naeayedea.keith.commands.lib;

import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;

public interface MessageContentProvider<T extends Enum<T>> {

    String getContent(MessageContext<T> context) throws KeithPermissionException, KeithExecutionException;
}
