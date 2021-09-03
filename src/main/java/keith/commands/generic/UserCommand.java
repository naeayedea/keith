package keith.commands.generic;

import keith.commands.AccessLevel;
import keith.commands.Command;

public abstract class UserCommand implements Command {


    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

}
