import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;


public class QueryResultTableModel extends AbstractTableModel {
    
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private ResultSetMetaData metaData;
    private int numberOfRows;
    private int numberOfColumns;

    // keep track of database connection status
    private boolean connectedToDatabase = false;
    
    public Boolean LoadDriver(String name){
        try {
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( null, "MySQL driver not found", "Driver not found", JOptionPane.ERROR_MESSAGE );
            return false;
        }
        System.out.println("Driver loaded");
        return true;
    }
    
    public Boolean ConnectToDatabase(String database, String username, String password){
        
        if (connectedToDatabase){
            System.out.printf("Already Connected...Disconnecting....");
            DisconnectFromDatabase();
        }
        
        if (username.isEmpty() || password.isEmpty()){
            JOptionPane.showMessageDialog( null, "Username/Password Combination Incorrect", "Authentication Error", JOptionPane.ERROR_MESSAGE );
            return false;
        }
        
        // Establish a connection
        try {
            connection = DriverManager.getConnection(database, username, password);
            connectedToDatabase = true;
            
            // Add the statement
            statement = connection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
            
            DatabaseMetaData dbMetaData = connection.getMetaData();
            System.out.println("Database connected");
            System.out.println("JDBC Driver name " + dbMetaData.getDriverName() );
            System.out.println("JDBC Driver version " + dbMetaData.getDriverVersion());
            System.out.println("Driver Major version " +dbMetaData.getDriverMajorVersion());
            System.out.println("Driver Minor version " +dbMetaData.getDriverMinorVersion() );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog( null, "Unable to Connect.\nMost likey due to incorrect Username/Password combination", "Database Error", JOptionPane.ERROR_MESSAGE );
            return false;
        }
        
        return true;
    }

    public void ExecuteQuery(String query) {
        
        // ensure database connection is available
        if ( !connectedToDatabase ){
            JOptionPane.showMessageDialog( null, "You must be connected to a database first", "Database Error", JOptionPane.ERROR_MESSAGE );
            return;
        }

        try {
            
            // Specify query and execute it
            System.out.println("Queryy: " + query);
            resultSet = statement.executeQuery(query);
            
            // obtain meta data for ResultSet
            metaData = resultSet.getMetaData();
            
            // Set number of rows & columns
            resultSet.last();                   // move to last row
            numberOfRows = resultSet.getRow();  // The last row is the number of rows
            numberOfColumns = metaData.getColumnCount(); // get column count
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE );
        }

        // notify JTable that model has changed
        fireTableStructureChanged();
     }
    
    public void ExecuteUpdate(String query) {
        
        // ensure database connection is available
        if ( !connectedToDatabase ){
            JOptionPane.showMessageDialog( null, "You must be connected to a database first", "Database Error", JOptionPane.ERROR_MESSAGE );
            return;
        }

        try {
            
            // Specify query and execute it
            System.out.printf("Queryy: " + query);
            statement.executeUpdate(query);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog( null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE );
        }

        // notify JTable that model has changed
        fireTableStructureChanged();
     }
    
    // close Statement and Connection               
    public void DisconnectFromDatabase()            
    {              
       if ( !connectedToDatabase )                  
          return;

       // close Statement and Connection            
       try                                          
       {                                            
          statement.close();                        
          connection.close();                       
       } // end try                                 
       catch ( SQLException sqlException )          
       {                                            
          sqlException.printStackTrace();           
       } // end catch                               
       finally  // update database connection status
       {                                            
          connectedToDatabase = false;              
       } // end finally                             
    } // end method disconnectFromDatabase  
    
    public void EmptyTable(){
        if (resultSet == null)
            return;
        try {
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        numberOfRows = 0;
        numberOfColumns = 0;
        fireTableStructureChanged();
        System.out.printf("We did all this");
    }
    
    @Override
    // Used by Abstract Table Model
    public Class getColumnClass( int column ) throws IllegalStateException
    {
       // ensure database connection is available
       if ( !connectedToDatabase ) 
          throw new IllegalStateException( "Not Connected to Database" );

       // determine Java class of column
       try 
       {
          String className = metaData.getColumnClassName( column + 1 );
          
          // return Class object that represents className
          return Class.forName( className );
       } // end try
       catch ( Exception exception ) 
       {
          exception.printStackTrace();
       } // end catch
       
       return Object.class; // if problems occur above, assume type Object
    } // end method getColumnClass
    
    @Override
    // Used by Abstract Table Model
    public String getColumnName( int column ) throws IllegalStateException
    {    
       // ensure database connection is available
       if ( !connectedToDatabase ) 
          throw new IllegalStateException( "Not Connected to Database" );

       // determine column name
       try 
       {
          return metaData.getColumnName( column + 1 );  
       } // end try
       catch ( SQLException sqlException ) 
       {
          sqlException.printStackTrace();
       } // end catch
       
       return ""; // if problems, return empty string for column name
    } // end method getColumnName
    
    @Override
    // Needed by Abstract Table Model
    public int getColumnCount() {
        return numberOfColumns;
    }

    @Override
    // Needed by Abstract Table Model
    public int getRowCount() {
        return numberOfRows;
    }

    @Override
    // Needed by Abstract Table Model
    public Object getValueAt(int row, int column) {
        try 
        {
             resultSet.next();  /* fixes a bug in MySQL/Java with date format */
           resultSet.absolute( row + 1 );
           return resultSet.getObject( column + 1 );
        } // end try
        catch ( SQLException sqlException ) 
        {
           sqlException.printStackTrace();
        } // end catch
        
        return "";
    }


}
