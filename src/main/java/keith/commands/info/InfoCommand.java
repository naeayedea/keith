package keith.commands.info;
import keith.commands.AccessLevel;
import keith.commands.Command;

public abstract class InfoCommand implements Command {

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ALL;
    }

}
