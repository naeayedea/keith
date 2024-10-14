package com.naeayedea.keith.commands.lib;

import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

public interface MessageEntitySelectionMenuProvider<T extends Enum<T>> {

    EntitySelectMenu getEntitySelectMenu(MessageContext<T> context);
}
