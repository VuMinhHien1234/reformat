package com.example.shop_manager.Response;

import com.example.shop_manager.Main.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.HashMap;

public class OrderResponse {
    private static HashMap<String, Double> productMap = new HashMap<>();
    private static HashMap<String, Integer> product_quantity = new HashMap<>();

    public static void addOrder(DefaultTableModel tableModel, String orderId, String customerId, String productId, String quantityText) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int quantity = Integer.parseInt(quantityText.trim());

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(null, "Quantity must be greater than 0.");
                return;
            }

            boolean isCustomerValid = checkCustomerExist(conn, customerId);
            boolean isProductValid = checkProductExist(conn, productId);

            if (!isCustomerValid || !isProductValid) {
                return;
            }

            if (!checkOrderExist(conn, orderId)) {
                insertOrder(conn, orderId);
            }

            insertOrderCustomer(conn, orderId, customerId);
            insertOrderProduct(conn, orderId, productId, quantity);
            updateProductQuantity(conn, productId, quantity);
            updateOrderPrice(conn, orderId, productId, quantity);

            JOptionPane.showMessageDialog(null, "Order added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadData(tableModel);
    }

    // Xóa đơn hàng
    public static void deleteOrder(String orderId, JTable orderTable, DefaultTableModel tableModel) {

        if (orderId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Order ID cannot be empty.");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            String deleteOrderProduct = "DELETE FROM Order_Product WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderProduct)) {
                stmt.setString(1, orderId);
                stmt.executeUpdate();
            }

            String deleteOrderCustomer = "DELETE FROM Order_Customer WHERE id_order = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderCustomer)) {
                stmt.setString(1, orderId);
                stmt.executeUpdate();
            }

            String deleteOrder = "DELETE FROM Report_Order WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrder)) {
                stmt.setString(1, orderId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Order has been successfully deleted.");
                    // Xóa dòng trong bảng JTable
                    int selectedRow = orderTable.getSelectedRow();
                    tableModel.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(null, "Order ID not found.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
        }
    }

    // Cập nhật đơn hàng
    public static void updateOrder(String orderId, String customerId, String productId, String quantityText, JTable orderTable, DefaultTableModel tableModel) {

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            Double price = productMap.get(productId);
            int quantity = Integer.parseInt(quantityText.trim());

            if (price == null) {
                String query = "SELECT price,quantity FROM Product WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, productId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    price = rs.getDouble("price");
                    quantity = rs.getInt("quantity");
                    productMap.put(productId, price);
                    product_quantity.put(productId,quantity);
                } else {
                    JOptionPane.showMessageDialog(null, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            double total = price * Integer.parseInt(quantityText);
            int quantity_productleft = product_quantity.get(productId);
            int quantity_input =Integer.parseInt(quantityText);
            String sqlUpdateOrder = "UPDATE `Order` SET totalprice = ? WHERE id = ?";
            try (PreparedStatement statementOrder = conn.prepareStatement(sqlUpdateOrder)) {
                statementOrder.setDouble(1, total);
                statementOrder.setString(2, orderId);

                int rowsUpdatedOrder = statementOrder.executeUpdate();
                if (rowsUpdatedOrder <= 0) {
                    JOptionPane.showMessageDialog(null, "Order not found or update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int old_quantity_order;
            String query = "SELECT quantity FROM Order_product WHERE order_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                old_quantity_order=rs.getInt("quantity");
            } else {
                JOptionPane.showMessageDialog(null, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sqlUpdateProduct_Order = "UPDATE Order_Product SET quantity = ? WHERE order_id= ?";
            try (PreparedStatement statementProduct = conn.prepareStatement(sqlUpdateProduct_Order)) {
                statementProduct.setInt(1, quantity_input);
                statementProduct.setString(2, orderId);

                int rowsUpdatedProduct = statementProduct.executeUpdate();
                if (rowsUpdatedProduct <= 0) {
                    JOptionPane.showMessageDialog(null, "Product update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            String sqlUpdateProduct = "UPDATE Product SET quantity = ? WHERE id= ?";
            try (PreparedStatement statementProduct = conn.prepareStatement(sqlUpdateProduct)) {
                statementProduct.setInt(1,quantity_productleft-(quantity_input-old_quantity_order));
                statementProduct.setString(2, productId);

                int rowsUpdatedProduct = statementProduct.executeUpdate();
                if (rowsUpdatedProduct <= 0) {
                    JOptionPane.showMessageDialog(null, "Product update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            conn.commit();
            JOptionPane.showMessageDialog(null, "Order and product quantity updated successfully!");
            loadData(tableModel);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter valid numbers for quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void loadData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String sql = """
                SELECT
                    o.id AS order_id,
                    o.status,
                    c.id AS customer_id,
                    c.name AS customer_name,
                    p.id AS product_id,
                    p.price,
                    op.quantity,
                    (p.price * op.quantity) AS total
                FROM
                    `Order` o
                JOIN
                    Order_Customer oc ON o.id = oc.id_order
                JOIN
                    Customer c ON oc.id_customer = c.id
                JOIN
                    Order_Product op ON o.id = op.order_id
                JOIN
                    Product p ON op.product_id = p.id
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] rowData = {
                        rs.getString("order_id"),
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("product_id"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getDouble("total"),
                        rs.getString("status")
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            System.out.println("Database error: " + ex.getMessage());
        }
    }

    // Kiểm tra xem customer có tồn tại trong cơ sở dữ liệu không
    private static boolean checkCustomerExist(Connection conn, String customerId) {
        String sql = "SELECT COUNT(*) FROM Customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Kiểm tra xem product có tồn tại trong cơ sở dữ liệu không
    private static boolean checkProductExist(Connection conn, String productId) {
        String sql = "SELECT COUNT(*) FROM Product WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Kiểm tra xem order có tồn tại trong cơ sở dữ liệu không
    private static boolean checkOrderExist(Connection conn, String orderId) {
        String sql = "SELECT COUNT(*) FROM `Order` WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Thêm đơn hàng vào bảng Order
    private static void insertOrder(Connection conn, String orderId) throws SQLException {
        String insertOrder = "INSERT INTO `Order` (id) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertOrder)) {
            stmt.setString(1, orderId);
            stmt.executeUpdate();
        }
    }

    // Thêm Order_Customer vào cơ sở dữ liệu
    private static void insertOrderCustomer(Connection conn, String orderId, String customerId) throws SQLException {
        String insertOrderCustomer = "INSERT INTO Order_Customer (id_order, id_customer) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertOrderCustomer)) {
            stmt.setString(1, orderId);
            stmt.setString(2, customerId);
            stmt.executeUpdate();
        }
    }

    // Thêm Order_Product vào cơ sở dữ liệu
    private static void insertOrderProduct(Connection conn, String orderId, String productId, int quantity) throws SQLException {
        String insertOrderProduct = "INSERT INTO Order_Product (order_id, product_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertOrderProduct)) {
            stmt.setString(1, orderId);
            stmt.setString(2, productId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        }
    }

    // Cập nhật số lượng sản phẩm trong bảng Product
    private static void updateProductQuantity(Connection conn, String productId, int quantity) throws SQLException {
        String updateProduct = "UPDATE Product SET quantity = quantity - ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateProduct)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, productId);
            stmt.executeUpdate();
        }
    }

    // Cập nhật giá trị tổng của đơn hàng trong bảng Order
    private static void updateOrderPrice(Connection conn, String orderId, String productId, int quantity) throws SQLException {
        String sql = """
                UPDATE `Order`
                SET totalPrice = (
                    SELECT SUM(p.price * op.quantity)
                    FROM Order_Product op
                    JOIN Product p ON op.product_id = p.id
                    WHERE op.order_id = ?
                )
                WHERE id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            stmt.setString(2, orderId);
            stmt.executeUpdate();
        }
    }
}




