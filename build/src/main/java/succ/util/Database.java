package succ.util;

import java.sql.*;
import java.util.ArrayList;
import succ.util.logs.ConsoleLogger;

public class Database {

    /**
     * Database class provides methods to interact with a given database via sql commands
     * input is automatically santiized to prevent sql injection via the sanitizeInput method.
     * @param   url     the url of the database you wish to interact with.
     */
    String url;
    ConsoleLogger log;
    public Database(String url){
        this.url=url;
        log = new ConsoleLogger();
    }

    //Attempts a connection to the database, if connection unsuccessful returns null
    private Connection connect(){
        try{
            return DriverManager.getConnection(url);
        }
        catch (SQLException e){
            log.printWarning(e.getMessage());
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
                log.printWarning(e.getMessage());
            }
        }
    }

    //Verifies if a database entry exists
    public boolean exists(String condition, String table){
        Connection connection = connect();
        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM "+table+" WHERE "+condition);
            if(rs.next()){
                closeConnection(connection);
            return true;
            }
            closeConnection(connection);
            return false;
        }
        catch (SQLException e){
            log.printWarning(e.getMessage());
            closeConnection(connection);
            return false;
        }
    }

    //Performs a database select query and returns a single result
    public ArrayList<String> select(String searchTerm){
        Connection connection = connect();
        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT "+searchTerm + " LIMIT 1");
            ArrayList<String> results = new ArrayList<>();
            int columnCount = rs.getMetaData().getColumnCount();
            while(rs.next()){
                //While next row
                for(int i = 1; i<=columnCount; i++){
                    //Fill out each column
                    Object object = rs.getObject(i);
                    results.add(object.toString());
                }
            }
            closeConnection(connection);
            return results;
        }
        catch (SQLException e){
            closeConnection(connection);
            log.printWarning(e.getMessage());
            return null;

        }
    }

    //Updates table entry from query
    public boolean update(String query, String table){
        Connection connection = connect();
        try{
             Statement stmt = connection.createStatement();
             stmt.execute("UPDATE "+table+" SET "+query);
        }
        catch (SQLException e){
            closeConnection(connection);
            log.printWarning(e.getMessage());
            return false;
        }
        closeConnection(connection);
        return true;
    }

    //Inserts new entry into the specified table
    public boolean insert(String query, String table){
        Connection connection = connect();
        try{
            Statement stmt = connection.createStatement();
            stmt.execute("INSERT INTO "+ table + query);
            closeConnection(connection);
            return true;
        }
        catch (SQLException e){
            closeConnection(connection);
            log.printWarning(e.getMessage());
            return false;
        }
    }


    //Warning - never allow the user to input a searchterm directly into this method as it gives unfiltered access
    //to the database.
    public ArrayList<String> query(String searchTerm){
        Connection connection = connect();
        try
        {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(searchTerm);

            int columnCount = rs.getMetaData().getColumnCount();
            ArrayList<String> results = new ArrayList<String>();
            //Loop through results
            while(rs.next()){
                //While next row
                String queryResult="";
                for(int i = 1; i<=columnCount; i++){
                    //Fill out each column
                    Object object = rs.getObject(i);
                    if(object!=null){
                        queryResult+=object.toString()+"    ";
                    }
                }
                results.add(queryResult);
            }
            closeConnection(connection);
            return results;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            closeConnection(connection);
            ArrayList<String> error = new ArrayList<String>();
            error.add(null);
            return error;
        }
    }
}
