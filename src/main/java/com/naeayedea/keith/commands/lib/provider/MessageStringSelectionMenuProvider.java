package com.naeayedea.keith.commands.lib.provider;

import com.naeayedea.keith.commands.lib.MessageContext;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public interface MessageStringSelectionMenuProvider<T extends Enum<T>> {

    StringSelectMenu getStringSelectMenu(MessageContext<T> context);

}
