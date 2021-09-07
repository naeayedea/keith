package keith.commands.admin;

import keith.commands.AccessLevel;
import keith.commands.Command;

public abstract class AdminCommand implements Command {

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ADMIN;
    }
}
