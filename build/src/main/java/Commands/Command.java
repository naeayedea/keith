package Commands;


//Interface for bot interaction with commands, lays out the generic methods needed to run commands.
public interface Command {

        void run();
        String getDescription();
        int getAccessLevel();

}
