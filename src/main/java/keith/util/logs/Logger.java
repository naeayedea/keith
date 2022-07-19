package keith.util.logs;

public class Logger {


    //Prints a warning message in red
    public static void printWarning(String message){
        System.out.println("\u001B[31mWARNING: "+message+"\u001B[37m");
    }

    //prints a success message in green
    public static void printSuccess(String message){
        System.out.println("\u001B[32m"+message+"\u001B[37m");
    }

    //Prints a users message from a server in white (default)
    public static void printPublicMessage(String message){
        System.out.println("\u001B[37mMessage Received from "+message);   //Text should be white, change just to be sure
    }

    //Prints a private message in Magenta
    public static void printPrivateMessage(String message){
        System.out.println("\u001B[35mPrivate Message Received\u001B[37m from "+message);
    }
}
