package com.naeayedea.keith.core.service;

import org.jspecify.annotations.NonNull;

/**
 * Static metadata for one registered command: its internal name plus the i18n keys used to
 * resolve its display name/aliases/description.
 *
 * @param internalName the stable, non-localized identifier used to look the command up
 * @param nameKey i18n key resolving to the command's display name
 * @param aliasKey i18n key resolving to a comma-separated list of aliases
 * @param descriptionKey i18n key resolving to a one-line description, formatted with
 *                        {0}=command name, {1}=prefix
 * @author naeayedea
 */
public record CommandDescriptor(
    @NonNull String internalName,
    @NonNull String nameKey,
    @NonNull String aliasKey,
    @NonNull String descriptionKey
) {
}
