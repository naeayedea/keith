package keith.commands.generic;

import keith.commands.AccessLevel;
import keith.commands.Command;

public abstract class UserCommand extends Command {

    public UserCommand(String name) {
        super(name);
    }

    public UserCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

}
