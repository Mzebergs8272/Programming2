package Programming2_CW2;

import com.aspose.cells.ChartType;
import com.aspose.cells.ImageOrPrintOptions;
import com.aspose.cells.SaveFormat;
import org.json.JSONObject;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.Map;
import java.util.concurrent.Flow;



class Main {
    public static void main(String[] args) throws IOException {
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
        window = new JFrame(windowTitle);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        width = Toolkit.getDefaultToolkit().getScreenSize().width;
        height = Toolkit.getDefaultToolkit().getScreenSize().height;
        window.setSize(width, height - 50);
        salesRecords = new HashMap<>();
        inventoryRecords = new HashMap<>();
        salesColumnNames = new String[]{"Sale ID", "Product ID", "Customer ID", "Date", "Quantity", "Total Value"};
        inventoryColumnNames = new String[]{"Product ID", "Product Name", "Description", "Cost", "Quantity", "Total Value"};
    }


    public void run() {
        drawNavigation();
        drawTableEdits();
        drawTable();
        drawPageTitle("Stock");

        loadInventoryRecords();
        loadSalesRecords();

        loadInventoryPage();

        window.setVisible(true);
    }

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

        btnInventoryPage.addActionListener(e -> loadInventoryPage());
        btnSalesPage.addActionListener(e -> loadSalesPage());
        btnInventoryReport.addActionListener(e -> drawWinInventoryReport());
        btnSalesReport.addActionListener(e -> drawWinSalesReport());

        containerNavBar.add(btnInventoryPage);
        containerNavBar.add(btnSalesPage);
        containerNavBar.add(btnSalesReport);
        containerNavBar.add(btnInventoryReport);

        window.add(containerNavBar, BorderLayout.WEST);
    }

    void drawTableEdits() {
        containerTableEdits = new JPanel();
        containerTableEdits.setLayout(null);
        containerTableEdits.setPreferredSize(new Dimension(window.getWidth(), 100));
        containerTableEdits.setBackground(Color.LIGHT_GRAY);

        JButton btnAddRecord = new JButton("Add Record");
        btnAddRecord.setBackground(Color.GREEN);
        btnAddRecord.setBounds(350, 25, 200, 50);
        JButton btnRemoveRecord = new JButton("Remove Record");
        btnRemoveRecord.setBackground(Color.GREEN);
        btnRemoveRecord.setBounds(570, 25, 200, 50);
        JLabel lblSearch = new JLabel("Search");
        lblSearch.setBounds(1015, 25, 50, 25);
        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(1015, 50, 200, 25);

        JButton btnRemoveSelectedRecords = new JButton("Remove Selected Rows");
        btnRemoveSelectedRecords.setBounds(1235, 25, 200, 50);
        btnRemoveSelectedRecords.setBackground(Color.GREEN);

        JButton btnSave = new JButton("Save All");
        btnSave.setBounds(1455, 25, 200, 50);

        btnSave.setBackground(Color.GREEN);

        btnAddRecord.addActionListener(e -> drawWinaddRecord());
        btnRemoveRecord.addActionListener(e -> drawWinRemoveRecord());
        btnRemoveSelectedRecords.addActionListener(e -> removeSelectedRecords());
        btnSave.addActionListener(e -> saveRecords());

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
            public void changedUpdate(DocumentEvent e) {}

        });

        containerTableEdits.add(btnAddRecord);
        containerTableEdits.add(btnRemoveRecord);
        containerTableEdits.add(txtSearch);
        containerTableEdits.add(lblSearch);
        containerTableEdits.add(btnRemoveSelectedRecords);
        containerTableEdits.add(btnSave);
        window.add(containerTableEdits, BorderLayout.NORTH);
    }

    void drawTable() {
        containerTable = new JPanel(new FlowLayout());
        containerTable.setPreferredSize(new Dimension(width - navBarWidth, height - 225));
        containerTable.setBackground(Color.WHITE);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);

        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1425, height - 225));

        containerTable.add(scrollPane);

        window.add(containerTable);
    }

    void searchTable(String query) {
        System.out.println("ewrgerg");
        if (tableModel.getColumnName(0).equals("Product ID")) {
            drawInventoryRecords();
            updateInventoryTable();

        }
        else {
            drawSalesRecords();
            updateSalesTable();
        }

        if (query == null || query.isEmpty()) {
            if (tableModel.getColumnName(0).equals("Product ID")) {
                drawInventoryRecords();
            }
            else drawSalesRecords();

            return;
        }

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (Vector<Object> record : tableModel.getDataVector()) {
            boolean match = false;
            ArrayList<String> resultRecord = new ArrayList<>();
            for (Object column : record) {
                resultRecord.add(column.toString());
                System.out.println(column + " " + query);
                if (column.toString() != null && !column.toString().isEmpty() && column.toString().equalsIgnoreCase(query)) {
                    match = true;
                }
            }
            if (match) result.add(resultRecord);
        }

        if (!result.isEmpty()) {

            String[][] arrResult = new String[result.size()][result.getFirst().size()];
            for (int i = 0; i < result.size(); i++) {
                for (int j = 0; j < result.getFirst().size(); j++) {
                    arrResult[i][j] = result.get(i).get(j);
                }
            }
            tableModel.setDataVector(arrResult, tableModel.getColumnName(0).equals("Product ID") ? inventoryColumnNames : salesColumnNames);
        }
        else {
            tableModel.setDataVector(new Object[][]{}, tableModel.getColumnName(0).equals("Product ID") ? inventoryColumnNames : salesColumnNames);
        }
    }

    void drawWinaddRecord() {
        // creates modal window
        // text fields to specify ID, Name, Description, Cost, Quantity, Total Value
        // access tableModel to add row

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

        btnAddRecord.addActionListener(e -> {
            if (txtProdName.getText().isEmpty() || txtCost.getText().isEmpty() || txtQuantity.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all the fields");
            }
            else {
                try {
                    tableModel.addRow(new String[]{
                            String.valueOf(tableModel.getRowCount() + 1),
                            txtProdName.getText(),
                            txtDescription.getText(),
                            txtCost.getText(),
                            txtQuantity.getText(),
                            String.valueOf(Integer.parseInt(txtQuantity.getText()) * Float.parseFloat(txtCost.getText()))
                    });

                    JOptionPane.showMessageDialog(null, "Record added", "Success!", JOptionPane.PLAIN_MESSAGE);
                }
                catch (NumberFormatException formatException) {
                    JOptionPane.showMessageDialog(null, "Quantity and cost must be numbers.", "Format Error!", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

    }

    void drawWinRemoveRecord() {
        JFrame winRemoveRecord = new JFrame("Remove Record");
        winRemoveRecord.setLayout(new FlowLayout());

        winRemoveRecord.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
            window.setEnabled(true);
            window.toFront();
            }
        });

        winRemoveRecord.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setEnabled(false);
        winRemoveRecord.setSize(new Dimension(400, 200));

        JLabel lblRangeRemove = new JLabel("Specify range of records to remove. Example: 1-5");
        JLabel lblListRemove = new JLabel("Specify list of records to remove. Example: 1 2 3 4 5");

        JTextField txtRangeRemove = new JTextField();
        txtRangeRemove.setPreferredSize(new Dimension(200, 25));
        JTextField txtListRemove = new JTextField();
        txtListRemove.setPreferredSize(new Dimension(200, 25));

        JButton btnRemoveRecord = new JButton("Remove specified records");
        btnRemoveRecord.addActionListener(e -> {

            ArrayList<String> recordIDs = new ArrayList<String>();

            recordIDs.addAll(Arrays.asList(txtListRemove.getText().split("[, ]")));

            if (txtRangeRemove.getText().matches("[0-9]-[0-9]")) {
                System.out.println(txtRangeRemove.getText());
                String[] range = txtRangeRemove.getText().split("-");
                for (int i = Math.min(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
                     i <= Math.max(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
                     i++) {
                    recordIDs.add(String.valueOf(i));
                }
            }
            // iterates through all records IDs specified and removes corresponding records from tableModel
            for (String recordID : recordIDs) {
                Vector<Vector> dataVector = tableModel.getDataVector();
                for (int i = 0; i < dataVector.size(); i++) {
                    if (dataVector.get(i).elementAt(0).equals(recordID)) {
                        tableModel.removeRow(i);
                        i -= 1;
                    }
                }
            }
        });

        winRemoveRecord.add(lblRangeRemove);
        winRemoveRecord.add(txtRangeRemove);
        winRemoveRecord.add(lblListRemove);
        winRemoveRecord.add(txtListRemove);
        winRemoveRecord.add(btnRemoveRecord);

        winRemoveRecord.setVisible(true);

    }



    void drawWinAddSale() {

        JFrame winAddSale = new JFrame("Add Sale");
        winAddSale.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        winAddSale.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                window.setEnabled(true);
                window.toFront();
            }
        });

        window.setEnabled(false);
        winAddSale.setSize(new Dimension(450, 300));
        winAddSale.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> dropdown = new JComboBox<>();
        dropdown.setPreferredSize(new Dimension(250, 25));
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

        //Button logic
        btnAddSale.addActionListener(e -> {
            String selectedProductID = dropdown.getSelectedItem().toString().split(" ")[1];

            try {
                float prodCost = Float.parseFloat(inventoryRecords.get(selectedProductID).get("Cost"));
                int prodQuantity = Integer.parseInt(inventoryRecords.get(selectedProductID).get("Quantity"));

                try {
                    int selectedQuantity = Integer.parseInt(txtQuantity.getText());

                    if (selectedQuantity <= prodQuantity && selectedQuantity > 0) {
                        if (txtDate.getText().matches("^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$")) {
                            tableModel.insertRow(tableModel.getRowCount(), new String[]{
                                    String.valueOf(tableModel.getRowCount() + 1),
                                    selectedProductID, txtCustomerID.getText(),
                                    txtDate.getText(), txtQuantity.getText(),
                                    String.valueOf(prodCost * selectedQuantity)});

                            inventoryRecords.get(selectedProductID).put("Quantity", String.valueOf(prodQuantity - selectedQuantity));
                            inventoryRecords.get(selectedProductID).put("Total Value", String.valueOf((prodQuantity - selectedQuantity) * prodCost));

                            dropdown.removeAllItems();
                            for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
                                String recordString =
                                        "ID: " + record.getKey() +
                                                " | Name: " + record.getValue().get("Product Name") +
                                                " | Quantity: " + record.getValue().get("Quantity");
                                dropdown.addItem(recordString);
                            }
                            updateSalesTable();

                            JOptionPane.showMessageDialog(null, "Successfully added sale.", "Success!", JOptionPane.PLAIN_MESSAGE);

                        } else {
                            JOptionPane.showMessageDialog(null, "Format for date is incorrect. Use an accepted format: dd/mm/yyy.", "Format error!", JOptionPane.WARNING_MESSAGE);

                        }
                    } else if (selectedQuantity <= 0) {
                        JOptionPane.showMessageDialog(null, "Specified product quantity cannot be zero.", "Product quantity error!", JOptionPane.WARNING_MESSAGE);

                    } else {
                        JOptionPane.showMessageDialog(null, "Insufficient quantity of chosen product for specified quantity.", "Product quantity error!", JOptionPane.WARNING_MESSAGE);
                    }
                }
                catch (NumberFormatException numFormatException) {
                        JOptionPane.showMessageDialog(null, "Quantity must be an integer.", "Format error!", JOptionPane.WARNING_MESSAGE);
                        System.out.println(numFormatException.getMessage());
                    }

            } catch (NumberFormatException emptyFieldException) {
                JOptionPane.showMessageDialog(null, "Specified product must have a cost and quantity.", "Format error!", JOptionPane.WARNING_MESSAGE);
            }
        });

        winAddSale.setLocationRelativeTo(null);
        winAddSale.setVisible(true);
    }


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

    void loadSalesRecords() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/Programming2_CW2/SalesRecords.json")));
            JSONObject json = new JSONObject(content);

            String[][] data = new String[json.length()][6];
            int i = 0;
            for (Object recordKey : json.keySet()) {
                JSONObject record = (JSONObject) json.get((String) recordKey);

                data[i][0] = (String) recordKey;
                data[i][1] = record.get("Product ID").toString();
                data[i][2] = record.get("Customer ID").toString();
                data[i][3] = record.get("Date").toString();
                data[i][4] = record.get("Quantity").toString();
                data[i][5] = record.get("Total Value").toString();

                salesRecords.put((String) recordKey, new HashMap<String, String>());

                salesRecords.get(data[i][0]).put("Product ID", data[i][1]);
                salesRecords.get(data[i][0]).put("Customer ID", data[i][2]);
                salesRecords.get(data[i][0]).put("Date", data[i][3]);
                salesRecords.get(data[i][0]).put("Quantity", data[i][4]);
                salesRecords.get(data[i][0]).put("Total Value", data[i][5]);

                i++;
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void loadInventoryRecords() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/Programming2_CW2/StockRecords.json")));
            JSONObject json = new JSONObject(content);

            String[][] data = new String[json.length()][6];
            int i = 0;
            for (Object recordKey : json.keySet()) {
                JSONObject record = (JSONObject) json.get((String) recordKey);

                data[i][0] = (String) recordKey;
                data[i][1] = record.get("Product Name").toString();
                data[i][2] = record.get("Description").toString();
                data[i][3] = record.get("Cost").toString();
                data[i][4] = record.get("Quantity").toString();
                data[i][5] = record.get("Total Value").toString();

                inventoryRecords.put((String) recordKey, new HashMap<String, String>());

                inventoryRecords.get(data[i][0]).put("Product Name", data[i][1]);
                inventoryRecords.get(data[i][0]).put("Description", data[i][2]);
                inventoryRecords.get(data[i][0]).put("Cost", data[i][3]);
                inventoryRecords.get(data[i][0]).put("Quantity", data[i][4]);
                inventoryRecords.get(data[i][0]).put("Total Value", data[i][5]);

                i++;

            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void drawSalesRecords() {

        Object[][] resultTable = new Object[salesRecords.size()][6];

        int i = 0;

        for (Map.Entry<String, HashMap<String, String>> record : salesRecords.entrySet()) {
            resultTable[i][0] = Integer.parseInt(record.getKey());
            resultTable[i][1] = record.getValue().get("Product ID").toString();
            resultTable[i][2] = record.getValue().get("Customer ID").toString();
            resultTable[i][3] = record.getValue().get("Date").toString();
            resultTable[i][4] = record.getValue().get("Quantity").toString();
            resultTable[i][5] = record.getValue().get("Total Value").toString();

            i++;
        }

        tableModel.setDataVector(resultTable, salesColumnNames);
    }

    void drawInventoryRecords() {
        Object[][] resultTable = new Object[inventoryRecords.size()][6];

        int i = 0;

        for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
            resultTable[i][0] = Integer.parseInt(record.getKey());
            resultTable[i][1] = record.getValue().get("Product Name").toString();
            resultTable[i][2] = record.getValue().get("Description").toString();
            resultTable[i][3] = record.getValue().get("Cost").toString();
            resultTable[i][4] = record.getValue().get("Quantity").toString();
            resultTable[i][5] = record.getValue().get("Total Value").toString();

            i++;
        }

        tableModel.setDataVector(resultTable, inventoryColumnNames);
    }

    void removeSelectedRecords() {
        for (int row : table.getSelectedRows()) {

            if (tableModel.getColumnName(0).equals("Sale ID")) {
                salesRecords.remove(String.valueOf(tableModel.getDataVector().get(row).getFirst()));
            }
            else {
                inventoryRecords.remove(String.valueOf(tableModel.getDataVector().get(row).getFirst()));
            }

            tableModel.removeRow(row);

        }

    }

    // changes ArrayList records to sales records from csv
    // changes text on buttons for corresponding page
    void loadSalesPage() {
        drawPageTitle("Sales");
        containerNavBar.getComponent(1).setBackground(Color.GREEN);
        containerNavBar.getComponent(0).setBackground(Color.WHITE);

        updateInventoryTable();
        drawSalesRecords();

        JButton btnAddSale = new JButton("Add Sale");
        btnAddSale.addActionListener(e -> drawWinAddSale());
        btnAddSale.setBackground(Color.GREEN);
        btnAddSale.setBounds(350, 25, 200, 50);

        containerTableEdits.remove(1);
        containerTableEdits.add(btnAddSale, 1);
        containerTableEdits.repaint();

    }
    // changes records list to inventory records from csv
    // changes display of buttons and nav bar
    void loadInventoryPage() {
        drawPageTitle("Stock");
        containerNavBar.getComponent(1).setBackground(Color.WHITE);
        containerNavBar.getComponent(0).setBackground(Color.GREEN);

        updateSalesTable();
        drawInventoryRecords();

        JButton btnAddRecord = new JButton("Add Record");
        btnAddRecord.setBackground(Color.GREEN);
        btnAddRecord.setBounds(350, 25, 200, 50);

        btnAddRecord.addActionListener(e -> drawWinaddRecord());
        containerTableEdits.remove(1);
        containerTableEdits.add(btnAddRecord, 1);
        containerTableEdits.repaint();
    }

    void saveRecords() {
        if (tableModel.getColumnName(0).equals("Product ID")) {
            try (FileWriter writer = new FileWriter("src/Programming2_CW2/StockRecords.json", false)) {

                updateInventoryTable();

                JSONObject newInventoryRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
                    JSONObject details = new JSONObject(record.getValue());
                    newInventoryRecords.put(record.getKey(), details);
                }
                writer.write(newInventoryRecords.toString());


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
            drawInventoryRecords();
        }
        else {
            try (FileWriter writer = new FileWriter("src/Programming2_CW2/salesRecords.json", false)) {
                updateSalesTable();

                JSONObject newSalesRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : salesRecords.entrySet()) {
                    JSONObject details = new JSONObject(record.getValue());
                    newSalesRecords.put(record.getKey(), details);
                }
                writer.write(newSalesRecords.toString());

            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (FileWriter writer = new FileWriter("src/Programming2_CW2/StockRecords.json", false)) {
                JSONObject newInventoryRecords = new JSONObject();
//                System.out.println(inventoryRecords.get("2").get("Quantity").toString());
                for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
                    System.out.println(record.getKey() + " " + record.getValue().get("Quantity").toString());
                    JSONObject details = new JSONObject(record.getValue());
                    newInventoryRecords.put(record.getKey(), details);
                }
                writer.write(newInventoryRecords.toString());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            drawSalesRecords();
        }
    }

    // draws window displaying graphs for inventory
    void drawWinInventoryReport() {
        updateInventoryTable();
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
            e.printStackTrace();
            System.out.println("already running?");
            JOptionPane.showMessageDialog(window, "Error generating inventory report. Could it be already running?");
        }
    }
    // draws window displaying graphs for sales
    void drawWinSalesReport() {
        updateSalesTable();
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
            e.printStackTrace();
            System.out.println("already running?");
            JOptionPane.showMessageDialog(window, "Error generating sales report. Could it be already running?");
        }
    }

    // creates a window with the image of the chart
    void loadChartWin(String title, String imagePath) {
        // create a new jframe window with size
        JFrame chartWindow = new JFrame(title);
        chartWindow.setSize(800,600);
        chartWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // load the image from the file path and a label to hold the image
        ImageIcon chartImage = new ImageIcon(imagePath);
        JLabel imageLabel = new JLabel(chartImage);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        // add the image to the window and make the window visible
        chartWindow.add(new JScrollPane(imageLabel));
        chartWindow.setVisible(true);
        chartWindow.setLocationRelativeTo(null);
    }
}
