package Programming2_CW2;

import com.aspose.cells.ChartType;
import org.json.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


class Main {
    public static void main(String[] args) {
        InventoryManagerApp inventoryManager = new InventoryManagerApp();
        inventoryManager.width = 1920;
        inventoryManager.height = 1080;
        inventoryManager.navBarWidth = 200;
        inventoryManager.run();
    }
}

class LblAsterisk extends JLabel {
    public LblAsterisk() {
        this.setForeground(Color.RED);
        this.setText("*");
        this.setFont(new Font("Tahoma", Font.BOLD, 14));
    }
}


class InventoryManagerApp {
    public int width;
    public int height;
    public JFrame window;
    private String windowTitle;
    public int navBarWidth;

    public HashMap<String, HashMap<String, String>> salesRecords;
    public HashMap<String, HashMap<String, String>> inventoryRecords;

    public DefaultTableModel tableModel;
    public JTable table;

    public JPanel containerTableEdits;
    public JPanel containerNavBar;
    public JPanel containerTable;

    public String[] salesColumnNames;
    public String[] inventoryColumnNames;

    InventoryManagerApp() {

        // app definition and setup
        window = new JFrame(windowTitle);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // get screen width and height
        width = Toolkit.getDefaultToolkit().getScreenSize().width;
        height = Toolkit.getDefaultToolkit().getScreenSize().height;

        window.setSize(width, height - 50);

        // stores easily accessible table records
        salesRecords = new HashMap<>();
        inventoryRecords = new HashMap<>();

        // for swapping between sales and inventory tables
        salesColumnNames = new String[]{"Sale ID", "Product ID", "Customer ID", "Date", "Quantity", "Total Value"};
        inventoryColumnNames = new String[]{"Product ID", "Product Name", "Description", "Cost", "Quantity", "Total Value"};

    }


    public void run() {
        // draws all main components of the app
        // default page is inventory page
        drawNavigation();
        drawTableEdits();
        drawTable();
        drawPageTitle("Stock");
        loadInventoryRecords();
        loadSalesRecords();
        loadInventoryPage();

        window.setVisible(true);
    }

    // replaces current page title with chosen title
    void drawPageTitle(String title) {
        if (!(containerTableEdits.getComponent(0) instanceof JLabel)) {
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Tahoma", Font.BOLD, 40));
            lblTitle.setForeground(Color.BLACK);
            lblTitle.setBounds(50, 25, 200, 50);
            containerTableEdits.add(lblTitle, 0);
        } else {
            ((JLabel) containerTableEdits.getComponent(0)).setText(title);
        }
    }

    // draws all buttons in the vetical navigation bar
    // inventory page button
    // sales page button
    // sales and inventory report buttons
    void drawNavigation() {
        containerNavBar = new JPanel();
        containerNavBar.setPreferredSize(new Dimension(navBarWidth, height - 100));
        containerNavBar.setBackground(Color.LIGHT_GRAY);

        JButton btnInventoryPage = new JButton("Inventory");
        btnInventoryPage.setPreferredSize(new Dimension(new Dimension(navBarWidth, 50)));
        btnInventoryPage.setBackground(Color.WHITE);
        JButton btnSalesPage = new JButton("Sales");
        btnSalesPage.setPreferredSize(new Dimension(new Dimension(navBarWidth, 50)));
        btnSalesPage.setBackground(Color.WHITE);
        JButton btnInventoryReport = new JButton("Inventory Report");
        btnInventoryReport.setPreferredSize(new Dimension(navBarWidth, 50));
        btnInventoryReport.setBackground(Color.WHITE);
        JButton btnSalesReport = new JButton("Sales Report");
        btnSalesReport.setPreferredSize(new Dimension(navBarWidth, 50));
        btnSalesReport.setBackground(Color.WHITE);

        // short-hand lambda definition for the ActionListener interface with the default event method being actionPerformed
        // in which the chosen function gets called.
        btnInventoryPage.addActionListener(e -> loadInventoryPage());
        btnSalesPage.addActionListener(e -> loadSalesPage());
        btnInventoryReport.addActionListener(e -> drawWinInventoryReport());
        btnSalesReport.addActionListener(e -> drawWinSalesReport());

        containerNavBar.add(btnInventoryPage);
        containerNavBar.add(btnSalesPage);
        containerNavBar.add(btnSalesReport);
        containerNavBar.add(btnInventoryReport);

        // positions nav bar on the left side of the screen.
        window.add(containerNavBar, BorderLayout.WEST);
    }

    // draws the top bar for editing the displayed table
    // includes page title, add record, remove record, remove selected records, save all, and search table components
    void drawTableEdits() {
        containerTableEdits = new JPanel();
        // absolute layout, all components positions are absolute.
        containerTableEdits.setLayout(null);
        containerTableEdits.setPreferredSize(new Dimension(window.getWidth(), 100));
        containerTableEdits.setBackground(Color.LIGHT_GRAY);

        JButton btnAddRecord = new JButton("Add Record");
        btnAddRecord.setBackground(Color.decode("#09548d"));
        btnAddRecord.setBounds(350, 25, 200, 50);
        JButton btnRemoveRecord = new JButton("Remove Record");
        btnRemoveRecord.setBackground(Color.decode("#09548d"));
        btnRemoveRecord.setBounds(570, 25, 200, 50);
        btnRemoveRecord.setForeground(Color.WHITE);
        JLabel lblSearch = new JLabel("Search");
        lblSearch.setBounds(1015, 25, 50, 25);
        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(1015, 50, 200, 25);

        JButton btnRemoveSelectedRecords = new JButton("Remove Selected Rows");
        btnRemoveSelectedRecords.setBounds(1235, 25, 200, 50);
        btnRemoveSelectedRecords.setBackground(Color.decode("#09548d"));
        btnRemoveSelectedRecords.setForeground(Color.WHITE);

        JButton btnSave = new JButton("Save All");
        btnSave.setBounds(1455, 25, 200, 50);
        btnSave.setBackground(Color.decode("#09548d"));
        btnSave.setForeground(Color.WHITE);

        btnAddRecord.addActionListener(e -> drawWinaddRecord());
        btnRemoveRecord.addActionListener(e -> drawWinRemoveRecord());
        btnRemoveSelectedRecords.addActionListener(e -> removeSelectedRecords());
        btnSave.addActionListener(e -> saveRecords());

        // JTextfield doesnt seemingly support shorthand definitions for event listeners related to text input
        // so this is the long defintion for the text input listener for table search component
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchTable(txtSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchTable(txtSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

        });

        containerTableEdits.add(btnAddRecord);
        containerTableEdits.add(btnRemoveRecord);
        containerTableEdits.add(txtSearch);
        containerTableEdits.add(lblSearch);
        containerTableEdits.add(btnRemoveSelectedRecords);
        containerTableEdits.add(btnSave);

        // positions the table edit section at the top of the window.
        window.add(containerTableEdits, BorderLayout.NORTH);
    }

    // draws a blank JTable with fixed size.
    void drawTable() {
        containerTable = new JPanel(new FlowLayout());
        containerTable.setPreferredSize(new Dimension(width - navBarWidth, height - 225));
        containerTable.setBackground(Color.WHITE);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);

        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);

        // enables table vertical and horizontal scrolling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1425, height - 225));

        containerTable.add(scrollPane);

        // adds table in the remaining default slot of the window
        window.add(containerTable);
    }

    // searches currently displayed table and displays any matching records
    void searchTable(String query) {

        // checks what table is currently open and updates it to ensure a proper search.
        if (tableModel.getColumnName(0).equals("Product ID")) {
            drawInventoryRecords();
            updateInventoryTable();

        } else {
            drawSalesRecords();
            updateSalesTable();
        }

        // if search is empty, draw the entire table
        if (query == null || query.isEmpty()) {
            if (tableModel.getColumnName(0).equals("Product ID")) {
                drawInventoryRecords();
            } else drawSalesRecords();

            return;
        }

        // iterates through displayed table and appends any matching records to a result ArrayList
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (Vector<Object> record : tableModel.getDataVector()) {
            boolean match = false;
            ArrayList<String> resultRecord = new ArrayList<>();
            for (Object column : record) {
                resultRecord.add(column.toString());
                if (column.toString() != null && !column.toString().isEmpty() && column.toString().equalsIgnoreCase(query)) {
                    match = true;
                }
            }
            if (match) result.add(resultRecord);
        }

        // if any matches were found, replace the current table records with new records
        // otherwise, display no records
        if (!result.isEmpty()) {

            String[][] arrResult = new String[result.size()][result.getFirst().size()];
            for (int i = 0; i < result.size(); i++) {
                for (int j = 0; j < result.getFirst().size(); j++) {
                    arrResult[i][j] = result.get(i).get(j);
                }
            }
            tableModel.setDataVector(arrResult, tableModel.getColumnName(0).equals("Product ID") ? inventoryColumnNames : salesColumnNames);
        } else {
            tableModel.setDataVector(new Object[][]{}, tableModel.getColumnName(0).equals("Product ID") ? inventoryColumnNames : salesColumnNames);
        }
    }

    // draws a popup for adding an inventory record, with a grid bag layout
    void drawWinaddRecord() {
        JFrame winAddRecord = new JFrame("Add Record");

        winAddRecord.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        winAddRecord.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                window.setEnabled(true);
                window.toFront();
            }
        });

        window.setEnabled(false);
        winAddRecord.setSize(new Dimension(450, 300));
        winAddRecord.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // 5 by 5 grid
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblProdName = new JLabel("Product Name");
        JLabel lblDescription = new JLabel("Product Description");
        JLabel lblCost = new JLabel("Cost");
        JLabel lblQuantity = new JLabel("Quantity");

        JTextField txtProdName = new JTextField();
        txtProdName.setPreferredSize(new Dimension(175, 25));
        JTextField txtDescription = new JTextField();
        txtDescription.setPreferredSize(new Dimension(175, 25));
        JTextField txtCost = new JTextField();
        txtCost.setPreferredSize(new Dimension(175, 25));
        JTextField txtQuantity = new JTextField();
        txtQuantity.setPreferredSize(new Dimension(175, 25));

        JButton btnAddRecord = new JButton("Add Record");
        btnAddRecord.setPreferredSize(new Dimension(175, 25));

        JLabel lblAsterisk = new JLabel("*");
        lblAsterisk.setForeground(Color.RED);

        // grid bag layout setup
        gbc.gridx = 0;
        gbc.gridy = 0;
        winAddRecord.add(lblProdName, gbc);
        gbc.gridx = 1;
        winAddRecord.add(txtProdName, gbc);
        gbc.gridx = 2;
        winAddRecord.add(new LblAsterisk(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        winAddRecord.add(lblDescription, gbc);
        gbc.gridx = 1;
        winAddRecord.add(txtDescription, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        winAddRecord.add(lblCost, gbc);
        gbc.gridx = 1;
        winAddRecord.add(txtCost, gbc);
        gbc.gridx = 2;
        winAddRecord.add(new LblAsterisk(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        winAddRecord.add(lblQuantity, gbc);
        gbc.gridx = 1;
        winAddRecord.add(txtQuantity, gbc);
        gbc.gridx = 2;
        winAddRecord.add(new LblAsterisk(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        winAddRecord.add(btnAddRecord, gbc);

        winAddRecord.setVisible(true);

        // validates that all required fields are filled and they're all in the correct format
        btnAddRecord.addActionListener(e -> {
            if (txtProdName.getText().isEmpty() || txtCost.getText().isEmpty() || txtQuantity.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all the required fields");
            } else {
                try {
                    tableModel.addRow(new String[]{
                        String.valueOf(tableModel.getRowCount() + 1),
                        txtProdName.getText(),
                        txtDescription.getText(),
                        txtCost.getText(),
                        txtQuantity.getText(),
                        String.valueOf(Integer.parseInt(txtQuantity.getText()) * Float.parseFloat(txtCost.getText()))
                    });
                    // confirmation message
                    JOptionPane.showMessageDialog(null, "Record added", "Success!", JOptionPane.PLAIN_MESSAGE);
                } catch (NumberFormatException formatException) {
                    JOptionPane.showMessageDialog(null, "Quantity and cost must be numbers.", "Format Error!", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

    }

    // draws popup for removing records for any table, with a grid bag layout
    void drawWinRemoveRecord() {
        JFrame winRemoveRecord = new JFrame("Remove Record");
        winRemoveRecord.setLayout(new GridBagLayout());
        winRemoveRecord.setSize(new Dimension(600, 300));
        winRemoveRecord.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setEnabled(false);

        winRemoveRecord.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                window.setEnabled(true);
                window.toFront();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblRangeRemove = new JLabel("Range of records to remove. Example: 1-5");
        JTextField txtRangeRemove = new JTextField(15);
        JLabel lblListRemove = new JLabel("List of records to remove. Example: 1 2 3 4 5");
        JTextField txtListRemove = new JTextField(15);

        JButton btnRemoveRecord = new JButton("Remove Records");
        btnRemoveRecord.addActionListener(e -> {
            ArrayList<String> recordIDs = new ArrayList<>(Arrays.asList(txtListRemove.getText().split("[, ]")));

            // parses the range remove field and creates a list of all records with IDs within specified range
            if (txtRangeRemove.getText().matches("[0-9]-[0-9]")) {
                String[] range = txtRangeRemove.getText().split("-");
                for (int i = Math.min(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
                     i <= Math.max(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
                     i++) {
                    recordIDs.add(String.valueOf(i));
                }
            }
            else {
                if (!txtRangeRemove.getText().isEmpty()) {
                    // draws popup to indicate the format for the specified range is wrong
                    JOptionPane.showMessageDialog(null, "Specified range of records can't be parsed, use recommended format e.g., '1-5'", "Format error!", JOptionPane.WARNING_MESSAGE);
                }
            }
            // iterates through all records IDs specified and removes corresponding records from tableModel
            for (String recordID : recordIDs) {
                Vector<Vector> dataVector = tableModel.getDataVector();
                for (int i = 0; i < dataVector.size(); i++) {
                    if (dataVector.get(i).getFirst().toString().equals(recordID)) {
                        if (tableModel.getColumnName(0).equals("Sale ID")) {
                            updateSalesTable();

                            int quantity = Integer.parseInt(dataVector.get(i).get(4).toString());
                            float totalValue = Float.parseFloat(dataVector.get(i).get(5).toString());

                            // replaces quantity and total value of sale onto product
                            inventoryRecords.get(dataVector.get(i).getFirst().toString()).put(
                                    "Quantity",
                                    String.valueOf(Integer.parseInt(inventoryRecords.get(dataVector.get(i).getFirst().toString()).get("Quantity")) + quantity)
                            );

                            inventoryRecords.get(dataVector.get(i).getFirst().toString()).put(
                                    "Total Value",
                                    String.valueOf(Float.parseFloat(inventoryRecords.get(dataVector.get(i).getFirst().toString()).get("Total Value")) + totalValue)
                            );

                        }

                        tableModel.removeRow(i);
                        i -= 1;
                    }
                }
            }

            JOptionPane.showMessageDialog(null, "Specified records deleted.", "Success!", JOptionPane.PLAIN_MESSAGE);
        });

        // grid bag layout setup
        gbc.gridx = 0;
        gbc.gridy = 0;
        winRemoveRecord.add(lblRangeRemove, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        winRemoveRecord.add(txtRangeRemove, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        winRemoveRecord.add(lblListRemove, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        winRemoveRecord.add(txtListRemove, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        winRemoveRecord.add(btnRemoveRecord, gbc);

        winRemoveRecord.setVisible(true);

    }

    // draws popup for adding a sale record, with a grid bag layout
    void drawWinAddSale() {

        JFrame winAddSale = new JFrame("Add Sale");
        winAddSale.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // re-enables the main window when this popup closes, to prevent event-based problems and errors
        winAddSale.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
            window.setEnabled(true);
            window.toFront();
            }
        });

        // disabled input for the main window
        window.setEnabled(false);
        winAddSale.setSize(new Dimension(450, 300));
        winAddSale.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> dropdown = new JComboBox<>();
        dropdown.setPreferredSize(new Dimension(250, 25));
        // loads the dropdown menu, showing all inventory records
        for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
            String recordString =
                    "ID: " + record.getKey() +
                    " | Name: " + record.getValue().get("Product Name") +
                    " | Quantity: " + record.getValue().get("Quantity");
            dropdown.addItem(recordString);
        }

        JLabel lblDropdown = new JLabel("Select product");
        JLabel lblQuantity = new JLabel("Quantity");
        JLabel lblCustomerID = new JLabel("Customer ID");
        JLabel lblDate = new JLabel("Date");

        JTextField txtQuantity = new JTextField();
        txtQuantity.setPreferredSize(new Dimension(325, 25));
        JTextField txtCustomerID = new JTextField();
        txtCustomerID.setPreferredSize(new Dimension(225, 25));
        JTextField txtDate = new JTextField();
        txtDate.setPreferredSize(new Dimension(225, 25));

        JButton btnAddSale = new JButton("Add Sale");

        // grid bag layout setup

        gbc.gridx = 0;
        gbc.gridy = 0;
        winAddSale.add(lblDropdown, gbc);
        gbc.gridx = 1;
        winAddSale.add(dropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        winAddSale.add(lblQuantity, gbc);
        gbc.gridx = 1;
        winAddSale.add(txtQuantity, gbc);
        gbc.gridx = 2;
        winAddSale.add(new LblAsterisk(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        winAddSale.add(lblCustomerID, gbc);
        gbc.gridx = 1;
        winAddSale.add(txtCustomerID, gbc);
        gbc.gridx = 2;
        winAddSale.add(new LblAsterisk(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        winAddSale.add(lblDate, gbc);
        gbc.gridx = 1;
        winAddSale.add(txtDate, gbc);
        gbc.gridx = 2;
        winAddSale.add(new LblAsterisk(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        winAddSale.add(btnAddSale, gbc);

        // validation to ensure all fields are the correct type, to prevent the NumberFormatException when calculating the fields together
        btnAddSale.addActionListener(e -> {
            String selectedProductID = dropdown.getSelectedItem().toString().split(" ")[1];

            try {
                // takes the cost and quantity of the selected inventory record and parses them into numbers
                float prodCost = Float.parseFloat(inventoryRecords.get(selectedProductID).get("Cost"));
                int prodQuantity = Integer.parseInt(inventoryRecords.get(selectedProductID).get("Quantity"));

                try {
                    int selectedQuantity = Integer.parseInt(txtQuantity.getText());

                    // ensures the selected stock record has enough quantity for chosen quantity
                    if (selectedQuantity <= prodQuantity && selectedQuantity > 0) {
                        if (txtDate.getText().matches("^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$")) {
                            tableModel.insertRow(tableModel.getRowCount(), new String[]{
                                String.valueOf(tableModel.getRowCount() + 1),
                                selectedProductID, txtCustomerID.getText(),
                                txtDate.getText(), txtQuantity.getText(),
                                String.valueOf(prodCost * selectedQuantity)});

                            // updates the inventory record by subtracting specified quantity and total value,
                            // ensuring the sales influence the stock
                            inventoryRecords.get(selectedProductID).put("Quantity", String.valueOf(prodQuantity - selectedQuantity));
                            inventoryRecords.get(selectedProductID).put("Total Value", String.valueOf((prodQuantity - selectedQuantity) * prodCost));

                            // refreshes the dropdown menu
                            dropdown.removeAllItems();
                            for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
                                String recordString =
                                    "ID: " + record.getKey() +
                                    " | Name: " + record.getValue().get("Product Name") +
                                    " | Quantity: " + record.getValue().get("Quantity");
                                dropdown.addItem(recordString);
                            }
                            // updates sales records
                            updateSalesTable();

                            // confirmation message
                            JOptionPane.showMessageDialog(null, "Successfully added sale.", "Success!", JOptionPane.PLAIN_MESSAGE);

                        } else {
                            JOptionPane.showMessageDialog(null, "Format for date is incorrect. Use an accepted format: dd/mm/yyy.", "Format error!", JOptionPane.WARNING_MESSAGE);

                        }
                    } else if (selectedQuantity <= 0) {
                        // selected quantity cannot be less than 1
                        JOptionPane.showMessageDialog(null, "Specified product quantity cannot be zero.", "Product quantity error!", JOptionPane.WARNING_MESSAGE);

                    } else {
                        JOptionPane.showMessageDialog(null, "Insufficient quantity of chosen product for specified quantity.", "Product quantity error!", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (NumberFormatException numFormatException) {
                    JOptionPane.showMessageDialog(null, "Quantity must be an integer.", "Format error!", JOptionPane.WARNING_MESSAGE);
                }

            } catch (NumberFormatException emptyFieldException) {
                JOptionPane.showMessageDialog(null, "Specified product must have a cost and quantity.", "Format error!", JOptionPane.WARNING_MESSAGE);
            }
        });

        winAddSale.setLocationRelativeTo(null);
        winAddSale.setVisible(true);
    }

    // updates the inventoryRecords hashmap based on the records of currently displayed table
    // inventory table must be displayed, to prevent overwriting records incorrectly
    void updateInventoryTable() {
        for (Vector<Object> record : tableModel.getDataVector()) {
            HashMap<String, String> details = new HashMap<>();
            details.put("Product Name", record.get(1).toString());
            details.put("Description", record.get(2).toString());
            details.put("Cost", record.get(3).toString());
            details.put("Quantity", record.get(4).toString());
            details.put("Total Value", record.get(5).toString());
            inventoryRecords.put(String.valueOf(record.getFirst()), details);
        }

    }

    // updates the inventoryRecords hashmap based on the records of currently displayed table
    // sales table must be displayed, to prevent overwriting records incorrectly
    void updateSalesTable() {
        for (Vector<Object> record : tableModel.getDataVector()) {

            HashMap<String, String> details = new HashMap<>();
            details.put("Product ID", record.get(1).toString());
            details.put("Customer ID", record.get(2).toString());
            details.put("Date", record.get(3).toString());
            details.put("Quantity", record.get(4).toString());
            details.put("Total Value", record.get(5).toString());

            salesRecords.put(String.valueOf(record.getFirst()), details);

        }
    }

    // loads the salesRecords hashmap up with sales records from json
    void loadSalesRecords() {
        try {
            // turns all content of JSON file into a string
            String content = new String(Files.readAllBytes(Paths.get("src/Programming2_CW2/SalesRecords.json")));

            // parses the json string into a json object
            JSONObject json = new JSONObject(content);

            // iterates through the keys of each record in json and put them into the salesRecords hashmap
            for (Object recordKey : json.keySet()) {
                JSONObject record = (JSONObject) json.get((String) recordKey);

                salesRecords.put((String) recordKey, new HashMap<String, String>());

                salesRecords.get(recordKey).put("Product ID", record.get("Product ID").toString());
                salesRecords.get(recordKey).put("Customer ID", record.get("Customer ID").toString());
                salesRecords.get(recordKey).put("Date", record.get("Date").toString());
                salesRecords.get(recordKey).put("Quantity", record.get("Quantity").toString());
                salesRecords.get(recordKey).put("Total Value", record.get("Total Value").toString());


            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // sames as loadSalesRecords but with inventoryRecords hashmap and json file
    void loadInventoryRecords() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/Programming2_CW2/StockRecords.json")));
            JSONObject json = new JSONObject(content);


            for (Object recordKey : json.keySet()) {
                JSONObject record = (JSONObject) json.get((String) recordKey);


                inventoryRecords.put((String) recordKey, new HashMap<String, String>());

                inventoryRecords.get(recordKey).put("Product Name", record.get("Product Name").toString());
                inventoryRecords.get(recordKey).put("Description", record.get("Description").toString());
                inventoryRecords.get(recordKey).put("Cost", record.get("Cost").toString());
                inventoryRecords.get(recordKey).put("Quantity", record.get("Quantity").toString());
                inventoryRecords.get(recordKey).put("Total Value", record.get("Total Value").toString());

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // draws sales records from salesRecords hashmap into current displayed table
    void drawSalesRecords() {

        Object[][] resultTable = new Object[salesRecords.size()][6];

        int i = 0;

        for (Map.Entry<String, HashMap<String, String>> record : salesRecords.entrySet()) {
            resultTable[i][0] = Integer.parseInt(record.getKey());
            resultTable[i][1] = record.getValue().get("Product ID");
            resultTable[i][2] = record.getValue().get("Customer ID");
            resultTable[i][3] = record.getValue().get("Date");
            resultTable[i][4] = record.getValue().get("Quantity");
            resultTable[i][5] = record.getValue().get("Total Value");

            i++;
        }

        tableModel.setDataVector(resultTable, salesColumnNames);
    }

    // draws inventory records from inventoryRecords hashmap into current displayed table
    void drawInventoryRecords() {
        Object[][] resultTable = new Object[inventoryRecords.size()][6];

        int i = 0;

        for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
            resultTable[i][0] = Integer.parseInt(record.getKey());
            resultTable[i][1] = record.getValue().get("Product Name");
            resultTable[i][2] = record.getValue().get("Description");
            resultTable[i][3] = record.getValue().get("Cost");
            resultTable[i][4] = record.getValue().get("Quantity");
            resultTable[i][5] = record.getValue().get("Total Value");

            i++;
        }

        tableModel.setDataVector(resultTable, inventoryColumnNames);
    }

    // removes currently selected/highlighted records in the table
    void removeSelectedRecords() {

        while (table.getSelectedRows().length > 0) {
            Vector<Vector> dataVector = tableModel.getDataVector();
            int firstSelectedRecord = table.getSelectedRows()[0];

            if (tableModel.getColumnName(0).equals("Sale ID")) {

                int quantity = Integer.parseInt(dataVector.get(firstSelectedRecord).get(4).toString());
                float totalValue = Float.parseFloat(dataVector.get(firstSelectedRecord).get(5).toString());

                // replaces quantity and total value of sale onto product
                inventoryRecords.get(dataVector.get(firstSelectedRecord).get(1).toString()).put(
                        "Quantity",
                        String.valueOf(Integer.parseInt(inventoryRecords.get(dataVector.get(firstSelectedRecord).get(1).toString()).get("Quantity")) + quantity)
                );

                inventoryRecords.get(dataVector.get(firstSelectedRecord).get(1).toString()).put(
                        "Total Value",
                        String.valueOf(Float.parseFloat(inventoryRecords.get(dataVector.get(firstSelectedRecord).get(1).toString()).get("Total Value")) + totalValue)
                );

                salesRecords.remove(String.valueOf(dataVector.get(firstSelectedRecord).getFirst()));
            } else {
                inventoryRecords.remove(String.valueOf(dataVector.get(firstSelectedRecord).getFirst()));
            }

            tableModel.removeRow(firstSelectedRecord);

        }

    }

    // changes ArrayList records to sales records from csv
    // changes text on buttons for corresponding page
    void loadSalesPage() {
        if (tableModel.getColumnName(0).equals("Sale ID")) {
            return;
        }
        drawPageTitle("Sales");
        containerNavBar.getComponent(1).setBackground(Color.decode("#47D45A"));
        containerNavBar.getComponent(0).setBackground(Color.WHITE);

        updateInventoryTable();
        drawSalesRecords();

        JButton btnAddSale = new JButton("Add Sale");
        btnAddSale.addActionListener(e -> drawWinAddSale());
        btnAddSale.setBackground(Color.decode("#09548d"));
        btnAddSale.setForeground(Color.WHITE);
        btnAddSale.setBounds(350, 25, 200, 50);

        containerTableEdits.remove(1);
        containerTableEdits.add(btnAddSale, 1);
        containerTableEdits.repaint();

    }

    // changes records list to inventory records from csv
    // changes display of buttons and nav bar
    void loadInventoryPage() {
        if (tableModel.getColumnName(0).equals("Product ID")) {
            return;
        }

        drawPageTitle("Stock");
        containerNavBar.getComponent(1).setBackground(Color.WHITE);
        containerNavBar.getComponent(0).setBackground(Color.decode("#47D45A"));

        updateSalesTable();
        drawInventoryRecords();

        JButton btnAddRecord = new JButton("Add Record");
        btnAddRecord.setBackground(Color.decode("#09548d"));
        btnAddRecord.setForeground(Color.WHITE);
        btnAddRecord.setBounds(350, 25, 200, 50);

        btnAddRecord.addActionListener(e -> drawWinaddRecord());
        containerTableEdits.remove(1);
        containerTableEdits.add(btnAddRecord, 1);
        containerTableEdits.repaint();
    }

    // correctly saves and opens both inventory and sales tables and saves them to json
    // these records overwrite the json files.
    void saveRecords() {
        // if current displayed table is inventory table:

        if (tableModel.getColumnName(0).equals("Product ID")) {
            try (FileWriter writer = new FileWriter("src/Programming2_CW2/StockRecords.json", false)) {
                // update table records into inventoryRecords hashmap
                updateInventoryTable();

                // iterate through inventory records hashmap and append records to json object
                JSONObject newInventoryRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
                    JSONObject details = new JSONObject(record.getValue());
                    newInventoryRecords.put(record.getKey(), details);
                }

                // overwrite any contents of json file with new inventory records
                writer.write(newInventoryRecords.toString());


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // the same process as above, but if the sales page is open instead
            try (FileWriter writer = new FileWriter("src/Programming2_CW2/SalesRecords.json", false)) {

                JSONObject newSalesRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : salesRecords.entrySet()) {
                    JSONObject details = new JSONObject(record.getValue());
                    newSalesRecords.put(record.getKey(), details);
                }
                writer.write(newSalesRecords.toString());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // redraws the inventory records
            drawInventoryRecords();

        } else {
            // same process as above but starting with the sales page instead of the inventory page
            try (FileWriter writer = new FileWriter("src/Programming2_CW2/salesRecords.json", false)) {
                updateSalesTable();

                JSONObject newSalesRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : salesRecords.entrySet()) {
                    JSONObject details = new JSONObject(record.getValue());
                    newSalesRecords.put(record.getKey(), details);
                }
                writer.write(newSalesRecords.toString());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (FileWriter writer = new FileWriter("src/Programming2_CW2/StockRecords.json", false)) {
                JSONObject newInventoryRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
                    JSONObject details = new JSONObject(record.getValue());
                    newInventoryRecords.put(record.getKey(), details);
                }
                writer.write(newInventoryRecords.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            drawSalesRecords();
        }
    }

    // draws window displaying graphs for inventory
    void drawWinInventoryReport() {
        if (tableModel.getColumnName(0).equals("Product ID")) {
            updateInventoryTable();
        } else {
            updateSalesTable();
            drawInventoryRecords();
        }

        try {
            // create workbook and select first sheet
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook();
            com.aspose.cells.Worksheet sheet = workbook.getWorksheets().get(0);
            com.aspose.cells.Cells cells = sheet.getCells();

            // create headers for each bit of data
            String[] headers = {"Product ID", "Product Name", "Cost", "Quantity"};
            for (int i = 0; i < headers.length; i++) {
                cells.get(0, i).putValue(headers[i]);
            }

            // insert data from the StockRecords
            int row = 1;
            for (Map.Entry<String, HashMap<String, String>> entry : inventoryRecords.entrySet()) {
                String quantity = entry.getValue().get("Quantity");

                int quantityInt = (quantity != null && !quantity.isEmpty()) ? Integer.parseInt(quantity) : 0;

                cells.get(row, 0).putValue(entry.getKey());
                cells.get(row, 1).putValue(entry.getValue().get("Product Name"));
                cells.get(row, 2).putValue(quantityInt);
                row++;
            }

            // create bar chart along with all the details
            int chartIndex = sheet.getCharts().add(com.aspose.cells.ChartType.COLUMN, row + 2, 0, row + 30, 10);
            com.aspose.cells.Chart chart = sheet.getCharts().get(chartIndex);
            chart.getCategoryAxis().getTitle().setText("Product");
            chart.getValueAxis().getTitle().setText("Quantity");
            chart.getTitle().setText("Inventory Report");
            chart.getNSeries().add("C2:C" + row, true);
            chart.getNSeries().setCategoryData("B2:B" + row);
            chart.setShowLegend(false);

            // save the chart to an image
            String chartPath = "src/Programming2_CW2/charts/inventoryChart.png";
            chart.toImage(chartPath);

            // set the default sheet to be the one with the chart
            workbook.getWorksheets().setActiveSheetIndex(0);

            // save the workbook
            workbook.save("Inventory_Report.xlsx");

            // load the window for chart
            loadChartWin("Inventory Report", chartPath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Error generating inventory report. Could it be already running?");
        }
    }

    // draws window displaying graphs for sales
    void drawWinSalesReport() {
        if (tableModel.getColumnName(0).equals("Sale ID")) {
            updateSalesTable();
        } else {
            updateInventoryTable();
            saveRecords();
            loadSalesPage();
        }

        try {
            // create workbook and select first sheet
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook();
            com.aspose.cells.Worksheet sheet = workbook.getWorksheets().get(0);
            com.aspose.cells.Cells cells = sheet.getCells();

            // create headers for all of the data
            String[] headers = {"Sale ID", "Product ID", "Customer ID", "Date", "Quantity", "Total Value"};
            for (int i = 0; i < headers.length; i++) {
                cells.get(0, i).putValue(headers[i]);
            }

            // insert data from SalesRecords
            int row = 1;
            for (Map.Entry<String, HashMap<String, String>> entry : salesRecords.entrySet()) {
                cells.get(row, 0).putValue(entry.getKey());
                cells.get(row, 1).putValue(entry.getValue().get("Product ID"));
                cells.get(row, 2).putValue(entry.getValue().get("Customer ID"));
                cells.get(row, 3).putValue(entry.getValue().get("Date"));
                cells.get(row, 4).putValue(Integer.parseInt(entry.getValue().get("Quantity")));
                cells.get(row, 5).putValue(Double.parseDouble(entry.getValue().get("Total Value")));
                row++;
            }

            // create line graph with all details
            int chartIndex = sheet.getCharts().add(ChartType.COLUMN, row + 2, 0, row + 30, 10);
            com.aspose.cells.Chart chart = sheet.getCharts().get(chartIndex);
            chart.getCategoryAxis().getTitle().setText("Sale ID");
            chart.getValueAxis().getTitle().setText("Sale Value");
            chart.getNSeries().add("F2:F" + row, true);
            chart.getNSeries().setCategoryData("A2:A" + row);
            chart.getTitle().setText("Sales Report");
            chart.setShowLegend(false);

            // save the chart to an image
            String chartPath = "src/Programming2_CW2/charts/salesChart.png";
            chart.toImage(chartPath);

            // set the default sheet to be the one with the chart
            workbook.getWorksheets().setActiveSheetIndex(0);

            // save the workbook
            workbook.save("Sales_Report.xlsx");

            // load the window for chart
            loadChartWin("Sales Report", chartPath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Error generating sales report. Could it be already running?");
        }


    }

    // creates a window with the image of the chart
    void loadChartWin(String title, String imagePath) {
        // create a new jframe window with size
        JFrame chartWindow = new JFrame(title);
        chartWindow.setSize(800, 600);
        chartWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon chartImage;

        try {
            BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
            chartImage = new ImageIcon(bufferedImage);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(window, "Error loading sales report chart image.");
            return;
        }
        // load the image from the file path and a label to hold the image

        JLabel imageLabel = new JLabel(chartImage);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        // add the image to the window and make the window visible
        chartWindow.add(new JScrollPane(imageLabel));
        chartWindow.setVisible(true);
        chartWindow.setLocationRelativeTo(null);
    }
}
