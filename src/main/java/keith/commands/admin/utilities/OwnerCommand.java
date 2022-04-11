package keith.commands.admin.utilities;

import keith.commands.AccessLevel;
import keith.commands.Command;

public abstract class OwnerCommand extends Command {

    public OwnerCommand(String name) {
        super(name);
    }

    public OwnerCommand(String name, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

}
