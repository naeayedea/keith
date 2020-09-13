package succ.logs.util;

public class ConsoleLogger {

    /**
     * provides functionality for different system messages to be colour coded
     */

    //Prints a warning message in red
    public void printWarning(String message){
        System.out.println("\u001B[31mWARNING: "+message+"\u001B[37m");
    }

    //prints a success message in green
    public void printSuccess(String message){
        System.out.println("\u001B[32m"+message+"\u001B[37m");
    }

    //Prints a users message from a server in white (default)
    public void printPublicMessage(String message){
        System.out.println("\u001B[37mMessage Received from "+message);   //Text should be white, change just to be sure
    }

    //Prints a private message in Magenta
    public void printPrivateMessage(String message){
        System.out.println("\u001B[35mPrivate Message Received\u001B[37m from "+message);
    }
}
