package Commands.Admin;
import Commands.Command;


//Enhanced commands which only certain users can access
public interface AdminCommand extends Command{
        static final int ADMIN = 2;
}
