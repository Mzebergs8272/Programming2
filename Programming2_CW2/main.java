package Programming2_CW2;

import org.json.JSONObject;

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
            lblTitle.setForeground(Color.WHITE);
            containerTableEdits.add(lblTitle, 0);
        } else {
            ((JLabel) containerTableEdits.getComponent(0)).setText(title);
        }
    }

    void drawNavigation() {
        containerNavBar = new JPanel();
        containerNavBar.setPreferredSize(new Dimension(navBarWidth, height - 100));
        containerNavBar.setBackground(Color.RED);

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
        containerTableEdits.setPreferredSize(new Dimension(width, 100));
        containerTableEdits.setBackground(Color.BLUE);

        JButton btnAddRecord = new JButton("Add Record");
        btnAddRecord.setPreferredSize(new Dimension(200, 50));
        JButton btnRemoveRecord = new JButton("Remove Record");
        btnRemoveRecord.setPreferredSize(new Dimension(200, 50));
        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 50));

        JButton btnRemoveSelectedRecords = new JButton("Remove Selected Rows");
        JButton btnSave = new JButton("Save All");

        btnAddRecord.addActionListener(e -> addRecord());
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
        containerTableEdits.add(btnRemoveSelectedRecords);
        containerTableEdits.add(btnSave);
        window.add(containerTableEdits, BorderLayout.NORTH);
    }

    void drawTable() {
        containerTable = new JPanel(new FlowLayout());
        containerTable.setPreferredSize(new Dimension(width - navBarWidth, height - 225));
        containerTable.setBackground(Color.green);

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
        if (tableModel.getColumnName(0).equals("Product ID")) {
            updateInventoryTable();

        }
        else updateSalesTable();

        if (query == null || query.isEmpty()) {
            if (tableModel.getColumnName(0).equals("Product ID")) {
                drawInventoryRecords();
            }
            else drawSalesRecords();

            return;
        }

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (Vector<String> record : tableModel.getDataVector()) {
            boolean match = false;
            ArrayList<String> resultRecord = new ArrayList<>();
            for (String column : record) {
                resultRecord.add(column);
                System.out.println(column + " " + query);
                if (column != null && column.equalsIgnoreCase(query)) {
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
            tableModel.setDataVector(new String[][]{}, tableModel.getColumnName(0).equals("Product ID") ? inventoryColumnNames : salesColumnNames);
        }
    }

    void addRecord() {
        // creates modal window
        // text fields to specify ID, Name, Description, Cost, Quantity, Total Value
        // access tableModel to add row
        tableModel.addRow(new String[]{String.valueOf(tableModel.getRowCount()+1), "", "", "", "", ""});

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

        gbc.gridx = 0;
        gbc.gridy = 2;
        winAddSale.add(lblCustomerID, gbc);
        gbc.gridx = 1;
        winAddSale.add(txtCustomerID, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        winAddSale.add(lblDate, gbc);
        gbc.gridx = 1;
        winAddSale.add(txtDate, gbc);

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
                            tableModel.addRow(new String[]{
                                String.valueOf(tableModel.getRowCount() + 1),
                                selectedProductID, txtCustomerID.getText(),
                                txtDate.getText(), txtQuantity.getText(),
                                String.valueOf(prodCost * selectedQuantity)
                            });

                            inventoryRecords.get(selectedProductID).put("Quantity", String.valueOf(prodQuantity - selectedQuantity));
                            inventoryRecords.get(selectedProductID).put("Total Value", String.valueOf((prodQuantity - selectedQuantity) * prodCost));

                            HashMap<String, String> newInventoryRecord = getJsonInventoryRecord(selectedProductID);
                            newInventoryRecord.put("Quantity", inventoryRecords.get(selectedProductID).get("Quantity"));
                            newInventoryRecord.put("Total Value", inventoryRecords.get(selectedProductID).get("Total Value"));

                            updateJsonInventoryRecord(selectedProductID, newInventoryRecord);

                            HashMap<String, String> newSaleRecord = new HashMap<>();
                            newSaleRecord.put("Product ID", selectedProductID);
                            newSaleRecord.put("Customer ID", txtCustomerID.getText());
                            newSaleRecord.put("Date", txtDate.getText());
                            newSaleRecord.put("Quantity", txtQuantity.getText());
                            newSaleRecord.put("Total Value", String.valueOf(selectedQuantity * prodCost));

                            updateJsonSalesRecord(String.valueOf(tableModel.getRowCount()), newSaleRecord);


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
        for (Vector<String> record : tableModel.getDataVector()) {

            HashMap<String, String> details = new HashMap<>();
            details.put("Product Name", record.get(1));
            details.put("Description", record.get(2));
            details.put("Cost", record.get(3));
            details.put("Quantity", record.get(4));
            details.put("Total Value", record.get(5));
            inventoryRecords.put(record.getFirst(), details);

        }

    }

    void updateSalesTable() {
        for (Vector<String> record : tableModel.getDataVector()) {

            HashMap<String, String> details = new HashMap<>();
            details.put("Product ID", record.get(1));
            details.put("Customer ID", record.get(2));
            details.put("Date", record.get(3));
            details.put("Quantity", record.get(4));
            details.put("Total Value", record.get(5));

            salesRecords.put(record.getFirst(), details);

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

        String[][] resultTable = new String[salesRecords.size()][6];

        int i = 0;

        for (Map.Entry<String, HashMap<String, String>> record : salesRecords.entrySet()) {
            resultTable[i][0] = record.getKey();
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
        String[][] resultTable = new String[inventoryRecords.size()][6];

        int i = 0;

        for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
            resultTable[i][0] = record.getKey();
            resultTable[i][1] = record.getValue().get("Product Name").toString();
            resultTable[i][2] = record.getValue().get("Description").toString();
            resultTable[i][3] = record.getValue().get("Cost").toString();
            resultTable[i][4] = record.getValue().get("Quantity").toString();
            resultTable[i][5] = record.getValue().get("Total Value").toString();

            i++;
        }

        tableModel.setDataVector(resultTable, inventoryColumnNames);
    }

    HashMap<String, String> getJsonSalesRecord(String saleId) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/Programming2_CW2/SalesRecords.json")));
            JSONObject json = new JSONObject(content);

            HashMap<String, String> salesRecord = new HashMap<>();

            JSONObject record = (JSONObject) json.get(saleId);

            salesRecord.put("Product ID", record.get("Product ID").toString());
            salesRecord.put("Customer ID", record.get("Customer ID").toString());
            salesRecord.put("Date", record.get("Date").toString());
            salesRecord.put("Quantity", record.get("Quantity").toString());
            salesRecord.put("Total Value", record.get("Total Value").toString());

            return salesRecord;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    void updateJsonSalesRecord(String saleId, HashMap<String, String> newRecord) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/Programming2_CW2/SalesRecords.json")));
            JSONObject json = new JSONObject(content);

            JSONObject record = new JSONObject();

            record.put("Product ID", newRecord.get("Product ID"));
            record.put("Customer ID", newRecord.get("Customer ID"));
            record.put("Date", newRecord.get("Date"));
            record.put("Quantity", newRecord.get("Quantity"));
            record.put("Total Value", newRecord.get("Total Value"));

            json.put(saleId, record);

            Files.write(Paths.get("src/Programming2_CW2/SalesRecords.json"), json.toString().getBytes()); // 4 is indentation
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    HashMap<String, String> getJsonInventoryRecord(String recordId) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/Programming2_CW2/StockRecords.json")));
            JSONObject json = new JSONObject(content);

            HashMap<String, String> inventoryRecord = new HashMap<>();

            JSONObject record = (JSONObject) json.get(recordId);

            inventoryRecord.put("Product Name", record.get("Product Name").toString());
            inventoryRecord.put("Description", record.get("Description").toString());
            inventoryRecord.put("Cost", record.get("Cost").toString());
            inventoryRecord.put("Quantity", record.get("Quantity").toString());
            inventoryRecord.put("Total Value", record.get("Total Value").toString());

            return inventoryRecord;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void updateJsonInventoryRecord(String recordId, HashMap<String, String> newRecord) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/Programming2_CW2/StockRecords.json")));
            JSONObject json = new JSONObject(content);

            JSONObject record = new JSONObject();

            record.put("Product Name", newRecord.get("Product Name"));
            record.put("Description", newRecord.get("Description"));
            record.put("Cost", newRecord.get("Cost"));
            record.put("Quantity", newRecord.get("Quantity"));
            record.put("Total Value", newRecord.get("Total Value"));

            json.put(recordId, record);

            Files.write(Paths.get("src/Programming2_CW2/StockRecords.json"), json.toString().getBytes());
            System.out.println("fwejfewfe");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void removeSelectedRecords() {
        for (int row : table.getSelectedRows()) {

            if (tableModel.getColumnName(0).equals("Sale ID")) {
                System.out.println(salesRecords.get(tableModel.getDataVector().get(row).getFirst()));

                salesRecords.remove(tableModel.getDataVector().get(row).getFirst());
            }
            else {
                System.out.println(inventoryRecords.get(tableModel.getDataVector().get(row).getFirst()));
                inventoryRecords.remove(tableModel.getDataVector().get(row).getFirst());
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
        btnAddSale.setPreferredSize(new Dimension(200, 50));
        containerTableEdits.remove(1);
        containerTableEdits.add(btnAddSale, 1);

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
        btnAddRecord.setPreferredSize(new Dimension(200, 50));
        btnAddRecord.addActionListener(e -> addRecord());
        containerTableEdits.remove(1);
        containerTableEdits.add(btnAddRecord, 1);
    }

    void saveRecords() {
        if (tableModel.getColumnName(0).equals("Product ID")) {
            try (FileWriter writer = new FileWriter("src/Programming2_CW2/StockRecords.json")) {

                updateInventoryTable();

                JSONObject newInventoryRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
                    JSONObject details = new JSONObject(record.getValue());
                    newInventoryRecords.put(record.getKey(), details);
                }
                writer.write(newInventoryRecords.toString());

                drawSalesRecords();

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
            try (FileWriter writer = new FileWriter("src/Programming2_CW2/salesRecords.json")) {
                updateSalesTable();

                JSONObject newSalesRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : salesRecords.entrySet()) {
                    JSONObject details = new JSONObject(record.getValue());
                    newSalesRecords.put(record.getKey(), details);
                }
                writer.write(newSalesRecords.toString());

                drawInventoryRecords();

                JSONObject newInventoryRecords = new JSONObject();
                for (Map.Entry<String, HashMap<String, String>> record : inventoryRecords.entrySet()) {
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
    void drawWinInventoryReport() {}
    // draws window displaying graphs for sales
    void drawWinSalesReport() {}
}
