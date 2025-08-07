
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class InventorySystemGUI extends JFrame {
    private final String URL = "jdbc:mysql://localhost:3306/";
    private final String USER = "root";
    private final String PASSWORD = "Bh@tti1466";
    private final String DB_NAME = "ims_db";
    private Connection connection;

    private Connection getConnection(String url) throws SQLException {
        return DriverManager.getConnection(url, USER, PASSWORD);
    }

    public InventorySystemGUI() {
        try {
            this.connection = getConnection(URL);
            String databaseUrl = URL + DB_NAME;
            this.connection = getConnection(databaseUrl);
            showRoleSelection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Connection Failed!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void showItemTable(JFrame parentFrame, boolean isAdminView) {
    try {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT item_name, price, quantity FROM items_details");

        // Choose columns based on viewer type
        DefaultTableModel model;
        if (isAdminView) {
            model = new DefaultTableModel(new Object[]{"Item Name", "Price (Rs.)", "Quantity Available"}, 0);
        } else {
            model = new DefaultTableModel(new Object[]{"Item Name", "Price (Rs.)"}, 0);
        }

        while (rs.next()) {
            if (isAdminView) {
                model.addRow(new Object[]{
                    rs.getString("item_name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                });
            } else {
                model.addRow(new Object[]{
                    rs.getString("item_name"),
                    rs.getDouble("price")
                });
            }
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(parentFrame, scrollPane, "📦 Available Items", JOptionPane.PLAIN_MESSAGE);

    } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "Error fetching items: " + ex.getMessage());
        }
    }


    private void showRoleSelection() {
        JFrame roleFrame = new JFrame("Select Role");
        roleFrame.setSize(400, 200);
        roleFrame.setLayout(new BorderLayout());
        roleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("DO YOU WANT TO CONTINUE AS:", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel buttonPanel = new JPanel();
        JButton adminButton = new JButton("ADMIN");
        JButton customerButton = new JButton("CUSTOMER");
        JButton exitButton = new JButton("EXIT");  // Exit button added

        buttonPanel.add(adminButton);
        buttonPanel.add(customerButton);
        buttonPanel.add(exitButton); // Add exit button to the panel

        roleFrame.add(label, BorderLayout.NORTH);
        roleFrame.add(buttonPanel, BorderLayout.CENTER);
        roleFrame.setLocationRelativeTo(null);
        roleFrame.setVisible(true);

        adminButton.addActionListener(e -> {
            roleFrame.dispose();
            showAdminLogin();
        });

        customerButton.addActionListener(e -> {
            roleFrame.dispose();
            showCustomerLogin();
        });

        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(roleFrame, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }


    private void showAdminLogin() {
        String password = JOptionPane.showInputDialog(this, "Enter Admin Password:", "Admin Login", JOptionPane.PLAIN_MESSAGE);
        if ("SAR IMS".equals(password)) {
            showAdminPanel();
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect Password!", "Error", JOptionPane.ERROR_MESSAGE);
            showRoleSelection();
        }
    }

    private void showAdminPanel() {
        JFrame adminFrame = new JFrame("Admin Dashboard");
        adminFrame.setSize(400, 300);
        adminFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        adminFrame.setLayout(new GridLayout(6, 1));

        JButton addItem = new JButton("Add Item");
        JButton updateItem = new JButton("Update Item");
        JButton deleteItem = new JButton("Delete Item");
        JButton viewRevenue = new JButton("View Revenue");
        JButton backButton = new JButton("Back to Role Selection");
        JButton exitButton = new JButton("Exit");

        adminFrame.add(addItem);
        adminFrame.add(updateItem);
        adminFrame.add(deleteItem);
        adminFrame.add(viewRevenue);
        adminFrame.add(backButton);
        adminFrame.add(exitButton);

        adminFrame.setLocationRelativeTo(null);
        adminFrame.setVisible(true);
        // Show item list when admin panel opens
        showItemTable(adminFrame, true);  // Shows quantity

        addItem.addActionListener(e -> {
            JTextField itemName = new JTextField();
            JTextField price = new JTextField();
            JTextField quantity = new JTextField();
            Object[] fields = {
                "Item Name:", itemName,
                "Price:", price,
                "Quantity:", quantity
            };
            int option = JOptionPane.showConfirmDialog(adminFrame, fields, "Add New Item", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO items_details (item_name, price, quantity) VALUES (?, ?, ?)");
                    ps.setString(1, itemName.getText().trim());
                    ps.setDouble(2, Double.parseDouble(price.getText().trim()));
                    ps.setInt(3, Integer.parseInt(quantity.getText().trim()));
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(adminFrame, "Item added successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(adminFrame, "Error: " + ex.getMessage());
                }
            }
        });

        updateItem.addActionListener(e -> {
            JTextField itemName = new JTextField();
            JTextField newPrice = new JTextField();
            JTextField newQuantity = new JTextField();
            Object[] fields = {
                "Item Name to Update:", itemName,
                "New Price:", newPrice,
                "New Quantity:", newQuantity
            };
            int option = JOptionPane.showConfirmDialog(adminFrame, fields, "Update Item", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    PreparedStatement ps = connection.prepareStatement("UPDATE items_details SET price=?, quantity=? WHERE item_name=?");
                    ps.setDouble(1, Double.parseDouble(newPrice.getText().trim()));
                    ps.setInt(2, Integer.parseInt(newQuantity.getText().trim()));
                    ps.setString(3, itemName.getText().trim());
                    int updated = ps.executeUpdate();
                    if (updated > 0) {
                        JOptionPane.showMessageDialog(adminFrame, "Item updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(adminFrame, "Item not found!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(adminFrame, "Error: " + ex.getMessage());
                }
            }
        });

        deleteItem.addActionListener(e -> {
            String itemName = JOptionPane.showInputDialog(adminFrame, "Enter item name to delete:");
            if (itemName != null && !itemName.trim().isEmpty()) {
                try {
                    PreparedStatement ps = connection.prepareStatement("DELETE FROM items_details WHERE item_name=?");
                    ps.setString(1, itemName.trim());
                    int deleted = ps.executeUpdate();
                    if (deleted > 0) {
                        JOptionPane.showMessageDialog(adminFrame, "Item deleted successfully!");
                    } else {
                        JOptionPane.showMessageDialog(adminFrame, "Item not found!");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(adminFrame, "Error: " + ex.getMessage());
                }
            }
        });

        viewRevenue.addActionListener(e -> {
            try {
                String query = "SELECT item_name, price, quantity FROM revenue";
                PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                // Table model
                DefaultTableModel model = new DefaultTableModel(new Object[]{"Item Name", "Price", "Quantity", "Subtotal"}, 0);
                double totalEarnings = 0.0;

                while (rs.next()) {
                    String itemName = rs.getString("item_name");
                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");
                    double subtotal = price * quantity;

                    model.addRow(new Object[]{itemName, price, quantity, subtotal});
                    totalEarnings += subtotal;
                }

                JTable table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setPreferredSize(new Dimension(500, 200));

                // Panel to show table and total
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(scrollPane, BorderLayout.CENTER);

                JLabel totalLabel = new JLabel("💰 Total Earnings: Rs. " + totalEarnings);
                totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
                totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
                panel.add(totalLabel, BorderLayout.SOUTH);

                JOptionPane.showMessageDialog(null, panel, "Revenue Report", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error fetching revenue: " + ex.getMessage());
            }
        });


        backButton.addActionListener(e -> {
            adminFrame.dispose();
            showRoleSelection();
        });

        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(adminFrame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    // ---- Customer code (unchanged) ----

    private void showCustomerLogin() {
        JTextField customer_NameField = new JTextField();
        JTextField Phone_NumberField = new JTextField();

        Object[] message = {
            "Customer Name:", customer_NameField,
            "Phone Number:", Phone_NumberField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Customer Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String customer_Name = customer_NameField.getText().trim();
            String Phone_Number = Phone_NumberField.getText().trim();
            if (!customer_Name.isEmpty() && !Phone_Number.isEmpty()) {
                try {
                    PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM customers_data WHERE customer_Name=? AND Phone_Number=?");
                    checkStmt.setString(1, customer_Name);
                    checkStmt.setString(2, Phone_Number);
                    ResultSet rs = checkStmt.executeQuery();

                    if (!rs.next()) {
                        PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO customers_Data (customer_Name, Phone_Number) VALUES (?, ?)");
                        insertStmt.setString(1, customer_Name);
                        insertStmt.setString(2, Phone_Number);
                        insertStmt.executeUpdate();
                    }
                    showCustomerPanel(customer_Name,Phone_Number);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
                    showRoleSelection();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
                showRoleSelection();
            }
        } else {
            showRoleSelection();
        }
    }

    private void showCustomerPanel(String customer_Name, String Phone_Number) {
        JFrame customerFrame = new JFrame("Customer Dashboard - " + customer_Name);
        customerFrame.setSize(400, 300);
        customerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        customerFrame.setLayout(new GridLayout(4, 1));

        JButton buyItem = new JButton("Buy Item");
        JButton viewItems = new JButton("View Items");
        JButton exit = new JButton("Exit");
        JButton backButton = new JButton("Back to Main");

        customerFrame.add(buyItem);
        customerFrame.add(viewItems);
        customerFrame.add(exit);
        customerFrame.add(backButton);

        customerFrame.setLocationRelativeTo(null);
        customerFrame.setVisible(true);
        showItemTable(customerFrame, false);  // Hides quantity


        viewItems.addActionListener(e -> {
            try {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT item_name, price FROM items_details");

                // Table model with only columns you want customers to see
                DefaultTableModel model = new DefaultTableModel(new Object[]{"Item Name", "Price (Rs.)"}, 0);

                while (rs.next()) {
                    String itemName = rs.getString("item_name");
                    double price = rs.getDouble("price");
                    model.addRow(new Object[]{itemName, price});
                }

                JTable table = new JTable(model);
                table.setFont(new Font("Arial", Font.PLAIN, 14));
                table.setRowHeight(25);
                table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setPreferredSize(new Dimension(400, 200));

                JPanel panel = new JPanel(new BorderLayout());
                JLabel title = new JLabel("🛒 Available Items", JLabel.CENTER);
                title.setFont(new Font("Arial", Font.BOLD, 16));
                panel.add(title, BorderLayout.NORTH);
                panel.add(scrollPane, BorderLayout.CENTER);

                JOptionPane.showMessageDialog(null, panel, "Items List", JOptionPane.PLAIN_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        });

        buyItem.addActionListener(e -> {
            JTextField item = new JTextField();
            JTextField qty = new JTextField();
            Object[] fields = {"Item Name:", item, "Quantity:", qty};
            int opt = JOptionPane.showConfirmDialog(null, fields, "Buy Item", JOptionPane.OK_CANCEL_OPTION);

            if (opt == JOptionPane.OK_OPTION) {
                try {
                    String checkQuery = "SELECT item_id, price, quantity FROM items_details WHERE item_name=?";
                    PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                    checkStmt.setString(1, item.getText());
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        int itemId = rs.getInt("item_id");
                        int stock = rs.getInt("quantity");
                        double price = rs.getDouble("price");
                        int buyQty = Integer.parseInt(qty.getText());

                        if (buyQty <= stock) {
                            double total = price * buyQty;

                            PreparedStatement checkRevenueStmt = connection.prepareStatement(
                                "SELECT quantity, price FROM revenue WHERE item_name=?");
                            checkRevenueStmt.setString(1, item.getText());
                            ResultSet revenueRs = checkRevenueStmt.executeQuery();

                            if (revenueRs.next()) {
                                int existingQty = revenueRs.getInt("quantity");
                                double existingPrice = revenueRs.getDouble("price");

                                int newQty = existingQty + buyQty;
                                double newPrice = (existingPrice * existingQty + price * buyQty) / newQty;

                                PreparedStatement updateRevenueStmt = connection.prepareStatement(
                                    "UPDATE revenue SET quantity=?, price=? WHERE item_name=?");
                                updateRevenueStmt.setInt(1, newQty);
                                updateRevenueStmt.setDouble(2, newPrice);
                                updateRevenueStmt.setString(3, item.getText());
                                updateRevenueStmt.executeUpdate();
                            } else {
                                PreparedStatement insertRevenueStmt = connection.prepareStatement(
                                    "INSERT INTO revenue (item_id, item_name, price, quantity) VALUES (?, ?, ?, ?)");
                                insertRevenueStmt.setInt(1, itemId);
                                insertRevenueStmt.setString(2, item.getText());
                                insertRevenueStmt.setDouble(3, price);
                                insertRevenueStmt.setInt(4, buyQty);
                                insertRevenueStmt.executeUpdate();
                            }

                            PreparedStatement updateStock = connection.prepareStatement(
                                "UPDATE items_details SET quantity = quantity - ? WHERE item_id = ?");
                            updateStock.setInt(1, buyQty);
                            updateStock.setInt(2, itemId);
                            updateStock.executeUpdate();

                            // ✅ Update customer's quantity_bought using both customer_Name and Phone_Number
                            PreparedStatement updateCustomerQtyStmt = connection.prepareStatement(
                                "UPDATE customers_Data SET quantity_bought = quantity_bought + ? WHERE customer_Name = ? AND Phone_Number = ?");
                            updateCustomerQtyStmt.setInt(1, buyQty);
                            updateCustomerQtyStmt.setString(2, customer_Name);
                            updateCustomerQtyStmt.setString(3, Phone_Number);
                            updateCustomerQtyStmt.executeUpdate();

                            //JOptionPane.showMessageDialog(null, "Purchase successful! Total: Rs. " + total);
                            StringBuilder bill = new StringBuilder();
                            bill.append("🧾 Purchase Bill\n")
                                .append("-------------------------\n")
                                .append("Customer Name: ").append(customer_Name).append("\n")
                                .append("Item Name: ").append(item.getText()).append("\n")
                                .append("Quantity Bought: ").append(buyQty).append("\n")
                                .append("Total Price: Rs. ").append(total).append("\n")
                                .append("-------------------------\n")
                                .append("Thank you for your purchase!");

                            JOptionPane.showMessageDialog(null, bill.toString(), "Bill", JOptionPane.INFORMATION_MESSAGE);

                        } else {
                            JOptionPane.showMessageDialog(null, "Not enough stock available.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Item not found.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        });

        exit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(customerFrame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventorySystemGUI::new);
    }
}
