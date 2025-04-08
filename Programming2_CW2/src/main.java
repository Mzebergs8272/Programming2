//import com.opencsv.CSVReader;
//import com.opencsv.CSVWriter;

import javax.swing.*;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.util.ArrayList;


class Main {
    public static void main(String[] args) {
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

    public ArrayList<Record> records;

    InventoryManagerApp() {
        window = new JFrame(windowTitle);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        width = Toolkit.getDefaultToolkit().getScreenSize().width;
        height = Toolkit.getDefaultToolkit().getScreenSize().height;
        window.setSize(width, height-50);
    }


    public void run() {
        drawNavigation();
        drawTableEditSection();
        drawTable();
        window.setVisible(true);
    }

    void drawNavigation() {
        JPanel navBar = new JPanel();
        navBar.setPreferredSize(new Dimension(navBarWidth, height-100));
        navBar.setBackground(Color.RED);

        JButton btnInventoryPage = new JButton("Inventory");
        btnInventoryPage.setPreferredSize(new Dimension(new Dimension(navBarWidth, 50)));
        JButton btnSalesPage = new JButton("Sales");
        btnSalesPage.setPreferredSize(new Dimension(new Dimension(navBarWidth, 50)));
        JButton btnInventoryReport = new JButton("Inventory Report");
        btnInventoryReport.setPreferredSize(new Dimension(navBarWidth, 50));
        JButton btnSalesReport = new JButton("Sales Report");
        btnSalesReport.setPreferredSize(new Dimension(navBarWidth, 50));

        btnInventoryPage.addActionListener(e -> loadSalesPage());
        btnSalesPage.addActionListener(e -> loadInventoryPage());
        btnInventoryReport.addActionListener(e -> drawInventoryReport());
        btnSalesReport.addActionListener(e -> drawSalesReport());

        navBar.add(btnInventoryPage);
        navBar.add(btnSalesPage);
        navBar.add(btnSalesReport);
        navBar.add(btnInventoryReport);

        window.add(navBar, BorderLayout.WEST);
    }

    void drawTableEditSection() {
        JPanel tempNameContainer = new JPanel();
        tempNameContainer.setPreferredSize(new Dimension(width, 100));
        tempNameContainer.setBackground(Color.BLUE);

        JButton btnAddRecord = new JButton("Add Record");
        btnAddRecord.setPreferredSize(new Dimension(new Dimension(200, 50)));
        JButton btnRemoveRecord = new JButton("Remove Record");
        btnRemoveRecord.setPreferredSize(new Dimension(new Dimension(200, 50)));
        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 50));

        btnAddRecord.addActionListener(e -> addRecord());
        btnRemoveRecord.addActionListener(e -> removeRecord());
        txtSearch.addActionListener(e -> searchTable());

        tempNameContainer.add(btnAddRecord);
        tempNameContainer.add(btnRemoveRecord);
        tempNameContainer.add(txtSearch);
        window.add(tempNameContainer, BorderLayout.NORTH);
    }

    void drawTable() {
        JPanel tableContainer = new JPanel();
        tableContainer.setPreferredSize(new Dimension(width - navBarWidth, height-100));
        tableContainer.setBackground(Color.green);

        // create 2d data array from data in csv
        String[][] data = {
                {"1", "Cheese", "description"},
                {"2", "Cheese", "description"}
        };
        String[] columnNames = {"Item ID", "Item Name", "Description"};
        JTable tblRecords = new JTable(data, columnNames);




        tblRecords.setPreferredSize(new Dimension(width - navBarWidth - 50, height-100));

        tableContainer.add(tblRecords);

        window.add(tableContainer);
    }

    void searchTable() {

    }

    void addRecord() {}
    void removeRecord() {}

    // changes ArrayList records to sales records from csv
    void loadSalesPage() {}
    // changes records list to inventory records from csv
    // changes display of buttons and nav bar
    void loadInventoryPage() {}
    void drawInventoryReport() {}
    void drawSalesReport() {}
}
