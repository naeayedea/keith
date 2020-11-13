package succ.util;

import java.sql.*;
import java.util.ArrayList;

import org.sqlite.SQLiteException;
import succ.util.logs.ConsoleLogger;

public class Database {

    /**
     * Database class provides methods to interact with a given database via sql commands
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
                    if(object!=null)
                    results.add(object.toString());
                    else
                        results.add("");
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
            String queryResult="";
            for(int i = 1; i<=columnCount; i++){
                queryResult+=rs.getMetaData().getColumnName(i)+"\t";

            }
            results.add(queryResult);
            while(rs.next()){
                queryResult="";
                //While next row
                for(int i = 1; i<=columnCount; i++){
                    //Fill out each column
                    Object object = rs.getObject(i);
                    if(object!=null){
                        queryResult+=object.toString()+"\t";
                    }
                }
                results.add(queryResult);
            }
            closeConnection(connection);
            return results;
        }
        catch (SQLException e)
        {
            closeConnection(connection);
            ArrayList<String> error = new ArrayList<String>();
            error.add(e.getMessage());
            return error;
        }
    }

    public boolean setReminder(String guildid, String channelid, String userid, long date, String text){
        Connection connection = connect();
        String setReminderString = "INSERT INTO reminders (guildid,channelid,userid, date, text) VALUES (?, ?, ?, ?, ?)";
        try{
            PreparedStatement addReminder = connection.prepareStatement(setReminderString);
            connection.setAutoCommit(false);
            addReminder.setString(1, guildid);
            addReminder.setString(2, channelid);
            addReminder.setString(3,userid);
            addReminder.setLong(4, date);
            addReminder.setString(5,text);
            addReminder.executeUpdate();
            connection.commit();
            closeConnection(connection);
            return true;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            closeConnection(connection);
            return false;
        }
    }

    public boolean removeReminder(String guildid, String channelid, String userid, long date, String text){
        Connection connection = connect();
        String setReminderString = "DELETE FROM reminders WHERE (guildid=? AND channelid=? AND userid=? AND date=?)";
        try{
            PreparedStatement addReminder = connection.prepareStatement(setReminderString);
            connection.setAutoCommit(false);
            addReminder.setString(1, guildid);
            addReminder.setString(2, channelid);
            addReminder.setString(3,userid);
            addReminder.setLong(4, date);
            addReminder.executeUpdate();
            connection.commit();
            closeConnection(connection);
            return true;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            closeConnection(connection);
            return false;
        }
    }

    public ArrayList<String> getEmojis(){
        Connection connection = connect();
        String getEmojiString = "SELECT guildid, source_guildid, roleid, emoji, unicode FROM emoji_roles";
        try{
            PreparedStatement getEmoji = connection.prepareStatement(getEmojiString);
            connection.setAutoCommit(false);
            ResultSet rs = getEmoji.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();
            System.out.println("columns: "+columnCount);
            ArrayList<String> results = new ArrayList<String>();
            //Loop through results
            String queryResult="";;
            while(rs.next()){
                queryResult="";
                //While next row
                for(int i = 1; i<=columnCount; i++){
                    //Fill out each column
                    Object object = rs.getObject(i);
                    if(object!=null){
                        queryResult+=object.toString()+"\t";
                    }
                }
                results.add(queryResult);
            }
            connection.commit();
            closeConnection(connection);
            return results;
        } catch(SQLException e){
            closeConnection(connection);
            ArrayList<String> error = new ArrayList<String>();
            error.add(e.getMessage());
            return error;
        }
    }

    public boolean addEmoji(String guildid, String sourceGuildid, String roleid, String emoji, boolean unicode){
        Connection connection = connect();
        String addEmojiString = "INSERT INTO emoji_roles (guildid, source_guildid, roleid, emoji, unicode) VALUES (?, ?, ?, ?, ?)";
        try{
            PreparedStatement addEmoji = connection.prepareStatement(addEmojiString);
            connection.setAutoCommit(false);
            addEmoji.setString(1, guildid);
            addEmoji.setString(2, sourceGuildid);
            addEmoji.setString(3, roleid);
            addEmoji.setString(4, emoji);
            addEmoji.setBoolean(5, unicode);
            addEmoji.executeUpdate();
            connection.commit();
            closeConnection(connection);
            return true;
        } catch(SQLException e){
            System.out.println(e.getMessage());
            closeConnection(connection);
            return false;
        }
    }

    public boolean removeEmoji(String guildid, String emoji){
        Connection connection = connect();
        String removeEmojiString = "DELETE FROM emoji_roles WHERE (guildid=? AND emoji=?)";
        try{
            PreparedStatement removeEmoji = connection.prepareStatement(removeEmojiString);
            connection.setAutoCommit(false);
            removeEmoji.setString(1, guildid);
            removeEmoji.setString(2, emoji);
            removeEmoji.executeUpdate();
            connection.commit();
            closeConnection(connection);
            return true;
        } catch(SQLException e){
            System.out.println(e.getMessage());
            closeConnection(connection);
            return false;
        }
    }
}
