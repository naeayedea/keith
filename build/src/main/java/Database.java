
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Database {

    //Database url
    String url;
    public Database(String url){
        this.url=url;
    }
    String[] dateFormats = {"d-M-y","d/M/y","d M y"};
    //Connects to database
    private Connection connect(){

        //Attempt connection
        try{
            return DriverManager.getConnection(url);
        }
        catch (SQLException e)
        {
            System.out.println("ERROR: "+e.getMessage());
            return null;
        }

    }

    //Closes connection if open.
    private void closeConnection(Connection connection){
            try
            {
                if(connection!=null)
                {
                    connection.close();
                }
            }
            catch (SQLException e)
            {
                System.out.println("ERROR: "+e.getMessage());
            }
    }

    //Creates a new table with given parameters
    public String createTable(String command){
        Connection connection = connect();
        try{
            Statement stmt = connection.createStatement();
            boolean success = stmt.execute(command);
            if(success){
                closeConnection(connection);
                return "Table created successfully";
            }
            closeConnection(connection);
            return "Table creation unsuccessful";
        }
        catch(SQLException e){
            closeConnection(connection);
            return e.getMessage();
        }
    }

    //Updates a table with given parametes
    public String update(String command){
        Connection connection = connect();
        try{
            Statement stmt = connection.createStatement();
            boolean success = stmt.execute(command);
            if(success){
                closeConnection(connection);
                return "Table updated successfully";
            }
            closeConnection(connection);
            return "Table update unsuccessful";
        } catch (SQLException e){
            closeConnection(connection);
            return e.getMessage();
        }
    }

    public void insert(String command){
        Connection connection = connect();
        try{
            Statement stmt = connection.createStatement();
            stmt.execute(command);
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //Checks if an entry exists in database
    public boolean exists(String command){
      Connection connection = connect();
      try{
          Statement stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery(command);
          if(rs.getInt(1)==1){
              closeConnection(connection);
              return true;
          }
          closeConnection(connection);
          return false;
      } catch (SQLException e)
      {
          closeConnection(connection);
          return true;
      }
    }

    //Performs database query
    public String search(String searchTerm){
        Connection connection = connect();
        try
        {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(searchTerm);

            int columnCount = rs.getMetaData().getColumnCount();
            String queryResult="";

            for(int i = 1; i<=columnCount; i++){
                queryResult+=rs.getMetaData().getColumnName(i)+"\t";
            }
            queryResult+="\n";
            //Loop through results
            while(rs.next()){
                //While next row
                for(int i = 1; i<=columnCount; i++){
                    //Fill out each column
                    Object object = rs.getObject(i);
                    if(object!=null){
                        queryResult+=object.toString()+"\t";
                    }
                }
                queryResult+="\n";
            }
            closeConnection(connection);
            return queryResult;
        }
        catch (SQLException e)
        {
            closeConnection(connection);
            return e.getMessage();
        }
    }

    //Returns a random interesting fact from database
    public ArrayList<ArrayList<String>> onThisDay(java.util.Date input){
        String inputDate;
        //Create Stuff
        if(input==null){
            input = Calendar.getInstance().getTime();
        }

        //Attempt connection to database and return null if invalid.
        Connection connection = connect();
        if(connection==null)return null;


        SimpleDateFormat date = new SimpleDateFormat("%MM-dd");
        inputDate = date.format(input);
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<String> events = new ArrayList<>();
        ArrayList<String> descriptions = new ArrayList<>();
        ArrayList<String> reading = new ArrayList<>();
        ArrayList<ArrayList<String>> results = new ArrayList<>();
        //Do Stuff
        try
        {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select date, event, description, further_reading from on_this_day where date like '"+inputDate+"';");
            int columnCount = rs.getMetaData().getColumnCount();
            while(rs.next()){
                //While next row
                for(int i = 1; i<=columnCount; i++){
                    //Fill out each column
                    Object object = rs.getObject(i);
                    if(object!=null){
                        if(i==1)
                            dates.add(object.toString());
                        if(i==2)
                            events.add(object.toString());
                        if(i==3)
                            descriptions.add(object.toString());
                        if(i==4)
                            reading.add(object.toString());
                    } else if(i==4){
                        reading.add("blank");
                    }
                }
            }
            results.add(dates);
            results.add(events);
            results.add(descriptions);
            results.add(reading);
        }
        catch(SQLException e)
        {
            closeConnection(connection);
            return null;
        }
        //If all is well return random event from this day
        closeConnection(connection);
        if(results.get(0).size()>0){
            return results;
        } else
        {
            return null;
        }
    }

    public java.util.Date parseDate(String dateString){
        for(String format : dateFormats){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            java.util.Date date = dateFormat.parse(dateString);
            return date;
        }
        catch (ParseException e) {}
        }
        System.out.println("Parse Fail");
        return null;
    }
}
