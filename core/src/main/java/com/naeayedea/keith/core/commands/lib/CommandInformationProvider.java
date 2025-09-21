/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.commands.lib;

import com.naeayedea.keith.common.model.user.AccessLevel;
import org.springframework.lang.NonNull;

public interface CommandInformationProvider {

    /**
     * Retrieve the {@link AccessLevel AccessLevel} of the command
     *
     * @return an {@link AccessLevel AccessLevel} object representing the permissions required for a command
     * @see AccessLevel
     */
    @NonNull
    AccessLevel getAccessLevel();

    /**
     * Get the message timeout of a command
     *
     * @return the timeout period of a command in seconds
     */
    int getTimeOut();

    /**
     * If the command can be used in a private channel
     */
    boolean isPrivateMessageCompatible();

    /**
     * Returns the cost of running this command with respect to rate limiting
     */
    int getCost();

    /**
     * Retrieve the internal name for the command
     *
     * @return the internal name of the command
     */
    @NonNull String getInternalName();

    /**
     * Returns the key used to retrieve the command alias translations
     *
     * @return the key used by i18n to retrieve the command aliases
     */
    @NonNull
    String getAliasTranslationKey();

    /**
     * Returns the key used to retrieve the command name translation
     *
     * @return the key used by i18n to retrieve the command name
     */
    @NonNull
    String getNameTranslationKey();

    /**
     * Determine if a command is hidden
     *
     * @return true if hidden, false otherwise
     */
    boolean isHidden();
}
