package keith.commands;

public abstract class Command implements IMessageCommand {

    private final String defaultName;
    private final boolean isPrivateMessageCompatible;
    private final boolean isHidden;

    /**
     * Default command constructor with a name and default properties:
     * isPrivateMessageCompatible: true
     * isHidden: true
     * @param name the defaultName of the command
     */
    public Command(String name) {
        defaultName = name;
        this.isPrivateMessageCompatible = true;
        this.isHidden = false;
    }

    /**
     * command constructor to specify properties
     * @param name the default name of the command
     * @param isPrivateMessageCompatible set false if command will not work in private message
     * @param isHidden set true if command shouldn't show up in help
     */
    public Command(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        defaultName = name;
        this.isPrivateMessageCompatible = isPrivateMessageCompatible;
        this.isHidden = isHidden;
    }

    @Override
    public String getShortDescription(String prefix) {
        return "[DEFAULT SHORT DESCRIPTION]";
    }

    @Override
    public String getLongDescription() {
        return "[DEFAULT LONG DESCRIPTION]";
    }

    //Hidden commands won't be displayed to users but can still be accessed by people with sufficient level who know of them
    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public int getTimeOut(){
        return 10;
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return isPrivateMessageCompatible;
    }

    @Override
    public boolean sendTyping() {return true;}

    @Override
    public String getDefaultName() {
        return defaultName;
    }
}
