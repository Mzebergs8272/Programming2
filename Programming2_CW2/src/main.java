//import com.opencsv.CSVReader;
//import com.opencsv.CSVWriter;

import javax.swing.*;
import java.awt.*;



class Main {
    public static void main(String[] args) {
        InventoryManager inventoryManager = new InventoryManager();
        inventoryManager.width = 1920;
        inventoryManager.height = 1080;
        inventoryManager.navBarWidth = 200;
        inventoryManager.run();
    }
}



class Record {
    public String id;
    public String productName;
    public int quantity;
    public float totalValue;

    Record() {

    }
}



class InventoryRecord extends Record {
    public String description;
    public float cost;
}



class SalesRecord extends Record {
    public String customerName;
    public String date;

}


class InventoryManager {
    public int width;
    public int height;
    public JFrame window;
    private String windowTitle;

    public int navBarWidth;

    InventoryManager() {
        window = new JFrame(windowTitle);
        window.setLayout(new FlowLayout());


        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        width = Toolkit.getDefaultToolkit().getScreenSize().width;
        height = Toolkit.getDefaultToolkit().getScreenSize().height;
        window.setSize(width, height-50);
    }


    public void run() {
        drawNavigation();
        window.setVisible(true);
    }

    void drawNavigation() {

        JPanel navBar = new JPanel();
        navBar.setPreferredSize(new Dimension(navBarWidth, height));
        navBar.setBackground(Color.RED);

        this.window.add(navBar);
    }
}
