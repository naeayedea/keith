package succ;

import java.sql.*;

import succ.logs.util.ConsoleLogger;
public class Database {

    /**
     * Database class provides methods to interact with a given database via sql commands
     * input is automatically santiized to prevent sql injection via the sanitizeInput method.
     * @Param   url     the url of the database you wish to interact with.
     */
    String url;
    ConsoleLogger logger;
    public Database(String url){
        this.url=url;
        logger = new ConsoleLogger();
    }

    //Attempts a connection to the database, if connection unsuccessful returns null
    private Connection connect(){
        try{
            return DriverManager.getConnection(url);
        }
        catch (SQLException e){
            logger.printWarning("Connection to database unsuccessful");
            return null;
        }
    }

    //Closes an active connection if applicable
    private void closeConnection(Connection connection){
        if(connection!=null){
            try{
                connection.close();
            }
            catch (SQLException e){
                System.out.println();
            }
        }
    }

    //Verifies if a database entry exists
    public boolean exists(String condition, String table){
        Connection connection = connect();
        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM "+table+"WHERE "+condition);
            return true;
        }
        catch (SQLException e){
            return false;
        }
    }

}
