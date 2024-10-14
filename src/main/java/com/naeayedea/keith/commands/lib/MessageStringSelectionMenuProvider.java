package com.naeayedea.keith.commands.lib;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public interface MessageStringSelectionMenuProvider<T extends Enum<T>> {

    StringSelectMenu getStringSelectMenu(MessageContext<T> context);

}
