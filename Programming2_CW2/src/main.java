import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;


class Main {
    public static void main(String[] args) throws IOException {
        InventoryManagerApp inventoryManager = new InventoryManagerApp();
        inventoryManager.width = 1920;
        inventoryManager.height = 1080;
        inventoryManager.navBarWidth = 200;
        inventoryManager.run();
    }
}



abstract class Record {
    public String id;
    public String productName;
    public int quantity;
    public float totalValue;



}



class InventoryRecord extends Record {
    public String description;
    public float cost;


}



class SalesRecord extends Record {
    public String customerName;
    public String date;

}


class InventoryManagerApp {
    public int width;
    public int height;
    public JFrame window;
    private String windowTitle;
    public int navBarWidth;

    public ArrayList<ArrayList<String>> salesRecords;
    public ArrayList<ArrayList<String>> inventoryRecords;

    public DefaultTableModel tableModel;

    public JPanel containerTableEdits;
    public JPanel containerNavBar;
    public JPanel containerTable;

    InventoryManagerApp() {
        window = new JFrame(windowTitle);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        width = Toolkit.getDefaultToolkit().getScreenSize().width;
        height = Toolkit.getDefaultToolkit().getScreenSize().height;
        window.setSize(width, height-50);
        salesRecords = new ArrayList<>();
        inventoryRecords = new ArrayList<>();
    }


    public void run() throws IOException {
        drawNavigation();
        drawTableEdits();
        drawTable();
        drawPageTitle("Stock");

        loadSalesRecords();
        loadInventoryRecords();

        window.setVisible(true);
    }

    void drawPageTitle(String title) {
        if (!(containerTableEdits.getComponent(0) instanceof JLabel)) {
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Tahoma", Font.BOLD, 40));
            lblTitle.setForeground(Color.WHITE);
            containerTableEdits.add(lblTitle, 0);
        }
        else {
            ((JLabel) containerTableEdits.getComponent(0)).setText(title);
        }
    }

    void drawNavigation() {
        containerNavBar = new JPanel();
        containerNavBar.setPreferredSize(new Dimension(navBarWidth, height-100));
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

        btnInventoryPage.addActionListener(e -> {
            try {
                loadInventoryPage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        btnSalesPage.addActionListener(e -> {
            try {
                loadSalesPage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
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

        btnAddRecord.addActionListener(e -> addRecord());
        btnRemoveRecord.addActionListener(e -> drawWinRemoveRecord());
        txtSearch.addActionListener(e -> searchTable());

        containerTableEdits.add(btnAddRecord);
        containerTableEdits.add(btnRemoveRecord);
        containerTableEdits.add(txtSearch);
        window.add(containerTableEdits, BorderLayout.NORTH);
    }

    void drawTable() {
        containerTable = new JPanel();
        containerTable.setPreferredSize(new Dimension(width - navBarWidth, height-225));
        containerTable.setBackground(Color.green);

        tableModel = new DefaultTableModel();
        JTable tblRecords = new JTable(tableModel);
        tblRecords.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(tblRecords);
        scrollPane.setPreferredSize(new Dimension(1425, height-225));
        containerTable.add(scrollPane);

        window.add(containerTable);
    }

    void searchTable() {

    }

    void addRecord() {
        // creates modal window
        // text fields to specify ID, Name, Description, Cost, Quantity, Total Value
        // access tableModel to add row
        tableModel.addRow(new String[]{String.valueOf(tableModel.getRowCount()), "", "", "", "", ""});
    }

    void drawWinRemoveRecord() {
        JFrame winRemoveRecord = new JFrame("Remove Record");
        winRemoveRecord.setLayout(new FlowLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                     i++)
                {
                    recordIDs.add(String.valueOf(i));
                }
            }
            System.out.println(recordIDs);
            // iterates through all records IDs specified and removes corresponding records from tableModel
            for (String recordID : recordIDs) {
                Vector<Vector> dataVector = tableModel.getDataVector();
                for (int i=0; i<dataVector.size(); i++) {
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
        JFrame window = new JFrame("Add Sale");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new FlowLayout());
        window.setSize(new Dimension(400, 200));

        JComboBox<ArrayList<String>> dropdown = new JComboBox<ArrayList<String>>();

        for (ArrayList<String> record : inventoryRecords) {
            dropdown.addItem(record);
        }

        window.add(dropdown);
        window.setVisible(true);

    }

    void loadSalesRecords() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("Programming2_CW2/src/SalesRecords.json")));
        JSONArray json = new JSONArray(content);

        String[][] data = new String[json.length()][6];

        for (int i=0; i<json.length(); i++) {
            JSONObject record = json.getJSONObject(i);
            int j = 0;
            System.out.println(json.getJSONObject(i));
            System.out.println(record.keySet());
            for (Object col : record.keySet()) {
                data[i][j] = record.get(col.toString()).toString();
                salesRecords.add(new ArrayList<String>());
                salesRecords.get(i).add(data[i][j]);
                j++;
            }
        }

        tableModel.setDataVector(data, new String[]{"Sale ID", "Product ID", "Customer Name", "Date", "Quantity", "Total Value"});

    }

    void loadInventoryRecords() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("Programming2_CW2/src/StockRecords.json")));
        JSONArray json = new JSONArray(content);

        String[][] data = new String[json.length()][6];

        for (int i=0; i<json.length(); i++) {
            JSONObject record = json.getJSONObject(i);
            int j = 0;
            for (Object col : record.keySet()) {
                data[i][j] = record.get(col.toString()).toString();
                inventoryRecords.add(new ArrayList<String>());
                inventoryRecords.get(i).add(data[i][j]);
                j++;
            }
        }

        tableModel.setDataVector(data, new String[]{"Item ID", "Item Name", "Description", "Cost", "Quantity", "Total Value"});
    }

    void removeSelectedRecords() {

    }

    // changes ArrayList records to sales records from csv
    // changes text on buttons for corresponding page
    void loadSalesPage() throws IOException {
        drawPageTitle("Sales");
        containerNavBar.getComponent(1).setBackground(Color.GREEN);
        containerNavBar.getComponent(0).setBackground(Color.WHITE);

        try {
            loadSalesRecords();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        JButton btnAddSale = new JButton("Add Sale");
        btnAddSale.addActionListener(e -> drawWinAddSale());
        btnAddSale.setPreferredSize(new Dimension(200, 50));
        containerTableEdits.remove(1);
        containerTableEdits.add(btnAddSale, 1);

    }
    // changes records list to inventory records from csv
    // changes display of buttons and nav bar
    void loadInventoryPage() throws IOException {
        drawPageTitle("Stock");
        containerNavBar.getComponent(1).setBackground(Color.WHITE);
        containerNavBar.getComponent(0).setBackground(Color.GREEN);
        try {
            loadInventoryRecords();
        }
        catch (IOException e) {
            throw e;
        }

        JButton btnAddRecord = new JButton("Add Record");
        btnAddRecord.setPreferredSize(new Dimension(200, 50));
        btnAddRecord.addActionListener(e -> addRecord());
        containerTableEdits.remove(1);
        containerTableEdits.add(btnAddRecord, 1);
    }

    // draws window displaying graphs for inventory
    void drawWinInventoryReport() {}
    // draws window displaying graphs for sales
    void drawWinSalesReport() {}
}
