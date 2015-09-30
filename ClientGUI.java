import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

import javax.swing.*;

public class ClientGUI {
    
    // Global Variables
    Connection connection = null;
    QueryResultTableModel resultTableModel = new QueryResultTableModel();
    String drivers[] = {"com.mysql.jdbc.Driver", "oracle.jdbc.driver.OracleDriver", "com.ibm.db2.jdbc.netDB2Driver", "com.jdbc.odbc.jdbcOdbcDriver"};
    String url[] = {"jdbc:mysql://localhost:3310/project3"};
    
    // GUI Global Variables
    JFrame frame = new JFrame("SQL Client GUI");
    JComboBox<String> driversCB = new JComboBox<String>(drivers);
    JComboBox<String> urlCB = new JComboBox<String>(url);
    JTextField usernameField = new JTextField(21);
    JTextField passwordField = new JTextField(21);
    JTextArea commandTextArea = new JTextArea(8, 50);    
    JLabel status = new JLabel("No Connection Now");
    JButton connectButton = new JButton("Connect to Database");
    JButton clearButton = new JButton("Clear Command");
    JButton executeButton = new JButton("Execute SQL Command");
    JButton clearResultButton = new JButton("Clear Result Window");
    JTable resultTable = new JTable( resultTableModel );
    
    public ClientGUI(){
        buildGUI();
        ActivateButtons();
    }
    
    private void buildGUI(){
        
        // DB Connection Area
        JPanel dbPanel = new JPanel();
        dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.Y_AXIS));
        
        JPanel title = new JPanel(); 
        title.setLayout(new FlowLayout(FlowLayout.LEFT));
        title.add(new JLabel("Enter Database Information"));
        dbPanel.add(title);
        
        JPanel first = new JPanel(); 
        first.setLayout(new FlowLayout(FlowLayout.LEFT));
        first.add(new JLabel("JDBC Driver    "));
        driversCB.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");
        first.add(driversCB);
        dbPanel.add(first);
        
        JPanel second = new JPanel(); 
        second.setLayout(new FlowLayout(FlowLayout.LEFT));
        second.add(new JLabel("Database URL"));
        urlCB.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");
        second.add(urlCB);
        dbPanel.add(second);
        
        JPanel third = new JPanel(); 
        third.setLayout(new FlowLayout(FlowLayout.LEFT));
        third.add(new JLabel("Username       "));
        third.add(usernameField);
        dbPanel.add(third);
        
        JPanel fourth = new JPanel(); 
        fourth.setLayout(new FlowLayout(FlowLayout.LEFT));
        fourth.add(new JLabel("Password       "));
        fourth.add(passwordField);
        dbPanel.add(fourth);
        
        // SQL Command Window
        JLabel titleSQL = new JLabel("Enter a SQL Command");
        commandTextArea.setWrapStyleWord( true );
        commandTextArea.setLineWrap( true );
        JScrollPane scrollPane = new JScrollPane( commandTextArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
        JPanel sqlPanel = new JPanel();
        sqlPanel.setLayout(new BoxLayout(sqlPanel, BoxLayout.Y_AXIS));
        sqlPanel.add(titleSQL);
        sqlPanel.add(scrollPane);
        
        // Top half panel
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(dbPanel);
        panel.add(sqlPanel);
        
        // Middle Commands
        connectButton.setBackground(Color.BLUE);
        connectButton.setForeground(Color.WHITE);
        executeButton.setBackground(Color.GREEN);
        JPanel middle = new JPanel(); 
        middle.setLayout(new FlowLayout());
        middle.add(status);
        middle.add(connectButton);
        middle.add(clearButton);
        middle.add(executeButton);
        
        // Result    
        clearResultButton.setBackground(Color.YELLOW);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(panel);
        mainPanel.add(middle);
        JLabel sqlResultLabel = new JLabel("SQL Execution Result");
        
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(sqlResultLabel);
        bottom.add(new JScrollPane( resultTable ), BorderLayout.CENTER );
        bottom.add(clearResultButton);
        
        mainPanel.add(bottom);
        
        // Create a frame and add the panel to it
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
        
        // ensure database connection is closed when user quits application
        frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        frame.addWindowListener(new WindowAdapter() { 
              public void windowClosed( WindowEvent event )
              {
                  // disconnect from database and exit when window has closed
                  resultTableModel.DisconnectFromDatabase();
                 System.exit( 0 );
              }
           });
    }
    
    private class ButtonListener implements ActionListener{

     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         
         switch(command){
             case "Connect":
                 ConnectHelper();
                 break;
             case "Clear":
                 ClearHelper();
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
    private void ConnectHelper() {
        
        // Load the JDBC driver
        if(!resultTableModel.LoadDriver(driversCB.getSelectedItem().toString()))
            return;
        
        // Connect to the database & display the status if its a success.
        if(resultTableModel.ConnectToDatabase(urlCB.getSelectedItem().toString(), usernameField.getText(), passwordField.getText())){
            status.setText("Connected to " + urlCB.getSelectedItem().toString());
            status.setForeground(Color.RED);
        }
    }
    
    
    private void ClearHelper() {
        commandTextArea.setText(null);
    }
    
    private void ExecuteHelper() {
        String command = commandTextArea.getText();
        
        // If command is a select, then call Execute Query
        if (command.startsWith("s") || command.startsWith("S"))
            resultTableModel.ExecuteQuery( command );
        // Else command is an update
        else
            resultTableModel.ExecuteUpdate( command );
    }
    private void ClearResultHelper() {
        resultTableModel.EmptyTable();
    }
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        ClientGUI client = new ClientGUI();
    }
}
