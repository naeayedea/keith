package succ.commands.generic;

import succ.commands.Command;

//Commands which everyone except banned users can access
public abstract class UserCommand implements Command {

    public int getAccessLevel(){
        return 1;
    }
}
