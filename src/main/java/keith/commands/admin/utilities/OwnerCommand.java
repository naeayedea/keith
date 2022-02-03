package keith.commands.admin.utilities;

import keith.commands.AccessLevel;
import keith.commands.Command;

public abstract class OwnerCommand implements Command {

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

}
