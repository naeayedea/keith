package succ.commands.enhanced;
import succ.commands.Command;

public abstract class EnhancedCommand implements Command {

    @Override
    public int getAccessLevel(){
        return 2;
    }
}
