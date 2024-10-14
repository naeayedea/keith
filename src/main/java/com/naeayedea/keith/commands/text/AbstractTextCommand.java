package com.naeayedea.keith.commands.text;

import com.naeayedea.keith.commands.common.AbstractCommand;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTextCommand extends AbstractCommand implements TextCommand {

    private final boolean isPrivateMessageCompatible;

    private final boolean isHidden;


    private final List<String> commandAliases;

    /**
     * command constructor to specify all values of a command
     *
     * @param name                       the default name of the command
     * @param isPrivateMessageCompatible set false if command will not work in private message
     * @param isHidden                   set true if command shouldn't show up in help
     * @param cost                       the cost of a command with respect to rate limiting
     */
    public AbstractTextCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden, int cost) {
        super(name, cost);

        this.isPrivateMessageCompatible = isPrivateMessageCompatible;
        this.isHidden = isHidden;

        this.commandAliases = new ArrayList<>(commandAliases);

        this.commandAliases.add(name);
    }

    /**
     * Default command constructor that only requires name
     *
     * @param name the defaultName of the command
     *             default properties:
     *             isPrivateMessageCompatible: true
     *             isHidden: true
     *             cost: 1
     */
    public AbstractTextCommand(String name, List<String> commandAliases) {
        this(name, commandAliases, true, false, 1);
    }

    /**
     * command constructor to specify the name and private message compatibility
     *
     * @param name                       the defaultName of the command
     * @param isPrivateMessageCompatible set false if command will not work in private message
     *                                   default values:
     *                                   isHidden: true
     *                                   cost: 1
     */
    public AbstractTextCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible) {
        this(name, commandAliases, isPrivateMessageCompatible, false, 1);
    }

    /**
     * command constructor to specify the name, private message compatibility and the hidden property
     *
     * @param name                       the default name of the command
     * @param isPrivateMessageCompatible set false if command will not work in private message
     * @param isHidden                   set true if command shouldn't show up in help
     *                                   default properties:
     *                                   cost: 1
     */
    public AbstractTextCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        this(name, commandAliases, isPrivateMessageCompatible, isHidden, 1);
    }

    @Override
    public final List<String> getAliases() {
        return commandAliases;
    }

    @Override
    public String getExampleUsage(String prefix) {
        return "[DEFAULT EXAMPLE USAGE]";
    }

    @Override
    public String getDescription() {
        return "[DEFAULT DESCRIPTION]";
    }

    //Hidden commands won't be displayed to users but can still be accessed by people with sufficient level who know of them
    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return isPrivateMessageCompatible;
    }

    @Override
    public boolean sendTyping() {
        return true;
    }

}
