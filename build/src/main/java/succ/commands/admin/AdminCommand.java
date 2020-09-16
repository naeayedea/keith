package succ.commands.admin;
import succ.commands.Command;


//Enhanced commands which only certain users can access
public abstract class AdminCommand implements Command{

        @Override
        public int getAccessLevel(){
                return 3;
        }
}
