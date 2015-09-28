import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import javax.swing.*;

public class ClientGUI {
    
    // Global Variables
    Connection connection = null;
    String drivers[] = {"com.mysql.jdbc.Driver", "oracle.jdbc.driver.OracleDriver", "com.ibm.db2.jdbc.netDB2Driver", "com.jdbc.odbc.jdbcOdbcDriver"};
    String url[] = {"jdbc:mysql://localhost:3310/project3"};
    
    // GUI Global Variables
    JComboBox<String> driversCB = new JComboBox<String>(drivers);
    JComboBox<String> urlCB = new JComboBox<String>(url);
    JTextField usernameField = new JTextField(25);
    JTextField passwordField = new JTextField(25);
    JTextArea commandTextArea = new JTextArea(10, 50);
    JLabel status = new JLabel("No Connection Now");
    JButton connectButton = new JButton("Connect to Database");
    JButton clearButton = new JButton("Clear Command");
    JButton executeButton = new JButton("Execute SQL Command");
    JTextArea resultTextArea = new JTextArea(10, 10);
    JButton clearResultButton = new JButton("Clear Result Window");
    
    public ClientGUI(){
        buildGUI();
    }
    
    private void buildGUI(){
        

        
        // DB Connection Area
        JPanel dbPanel = new JPanel();
        dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.Y_AXIS));
        dbPanel.add(new JLabel("Enter Database Information"));
        
        JPanel first = new JPanel(); 
        first.setLayout(new FlowLayout());
        first.add(new JLabel("JDBC Driver"));
        first.add(driversCB);
        dbPanel.add(first);
        
        JPanel second = new JPanel(); 
        second.setLayout(new FlowLayout());
        second.add(new JLabel("Database URL"));
        second.add(urlCB);
        dbPanel.add(second);
        
        JPanel third = new JPanel(); 
        third.setLayout(new FlowLayout());
        third.add(new JLabel("Username"));
        third.add(usernameField);
        dbPanel.add(third);
        
        JPanel fourth = new JPanel(); 
        fourth.setLayout(new FlowLayout());
        fourth.add(new JLabel("Password"));
        fourth.add(passwordField);
        dbPanel.add(fourth);
        
        // SQL Command Window
        JLabel titleSQL = new JLabel("Enter a SQL Command");
        JPanel sqlPanel = new JPanel();
        sqlPanel.setLayout(new BoxLayout(sqlPanel, BoxLayout.Y_AXIS));
        sqlPanel.add(titleSQL);
        sqlPanel.add(commandTextArea);
        
        // Top half panel
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(dbPanel);
        panel.add(sqlPanel);
        
        // Middle Commands
        JPanel middle = new JPanel(); 
        middle.setLayout(new FlowLayout());
        middle.add(status);
        middle.add(connectButton);
        middle.add(clearButton);
        middle.add(executeButton);
        
        // Result
        JLabel resultTitle = new JLabel("SQL Execution Result");
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(panel);
        mainPanel.add(middle);
        mainPanel.add(resultTitle);
        mainPanel.add(resultTextArea);
        mainPanel.add(clearResultButton);
        
        // Create a frame and add the panel to it
        JFrame frame = new JFrame("SQL Client GUI");
        frame.add(mainPanel);
        
        // Display the frame on the screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack(); // size frame to fit its components
        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);
        frame.setSize(950, 500);
       
    }

    private void ActivateButtons() {
        connectButton.setActionCommand("Connect");
        clearButton.setActionCommand("Clear");
        executeButton.setActionCommand("Execute");
        clearResultButton.setActionCommand("ClearResult");
        
        connectButton.addActionListener(new ButtonListener());
        clearButton.addActionListener(new ButtonListener());
        executeButton.addActionListener(new ButtonListener());
        clearResultButton.addActionListener(new ButtonListener());
    }
    
    private class ButtonListener implements ActionListener{

     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         
         switch(command){
             case "Connect":
                try {
                    ConnectHelper();
                } catch (ClassNotFoundException | SQLException e1) {
                    e1.printStackTrace();
                }
                 break;
             case "Clear":
                try {
                    ClearHelper();
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                 break;
             case "Execute":
                 ExecuteHelper();
                 break;
             case "ClearResult":
                 ClearResultHelper();
                 break;
             default:
                 throw new IllegalArgumentException("Invalid Command given");
         }
     }

    }
    private void ConnectHelper() throws SQLException, ClassNotFoundException{
        // Load the JDBC driver
        Class.forName(driversCB.getSelectedItem().toString());
        System.out.println("Driver loaded");
        
        System.out.println(usernameField.getText());

        // Establish a connection
        connection = DriverManager.getConnection
                (urlCB.getSelectedItem().toString(), usernameField.getText(), passwordField.getText());
        System.out.println("Database connected");
        status.setText("Connected to " + urlCB.getSelectedItem().toString());
        status.setForeground(Color.GREEN);
        
        DatabaseMetaData dbMetaData = connection.getMetaData();
        System.out.println("JDBC Driver name " + dbMetaData.getDriverName() );
        System.out.println("JDBC Driver version " + dbMetaData.getDriverVersion());
        System.out.println("Driver Major version " +dbMetaData.getDriverMajorVersion());
        System.out.println("Driver Minor version " +dbMetaData.getDriverMinorVersion() );
    }
    
    
    private void ClearHelper() throws SQLException {
        // Create a statement
        Statement statement = connection.createStatement();

        // Execute a statement
        ResultSet resultSet = statement.executeQuery
          (commandTextArea.getText());
        
        ResultSetMetaData meta = resultSet.getMetaData();
        for (int j = 0; j< meta.getColumnCount(); j++){
            
        }

        // Iterate through the result set and print the returned results
        while (resultSet.next())
          System.out.println(resultSet.getString("bikename") + "         \t" +
            resultSet.getString("cost") + "         \t" + resultSet.getString("mileage"));
            //the following print statement works exactly the same  
          //System.out.println(resultSet.getString(1) + "         \t" +
          //  resultSet.getString(2) + "         \t" + resultSet.getString(3));

        // Close the connection
        connection.close();
        
    }
    
    private void ExecuteHelper() {
        // TODO Auto-generated method stub
        
    }
    private void ClearResultHelper() {
        // TODO Auto-generated method stub
        
    }
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        ClientGUI client = new ClientGUI();
        client.ActivateButtons();
    }
}
