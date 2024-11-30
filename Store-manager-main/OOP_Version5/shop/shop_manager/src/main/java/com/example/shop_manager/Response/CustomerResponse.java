package com.example.shop_manager.Response;

import com.example.shop_manager.Entity.Customer;
import com.example.shop_manager.Main.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;

public class CustomerResponse {
    private ArrayList<Customer> customerList = new ArrayList<>();

    public void addCustomer(String id, String name, String address, String phoneNumber, JTable table, JTextField txtId, JTextField txtName, JTextField txtAddress, JTextField txtPhoneNumber) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO customer (id, name, address, phoneNumber) VALUES (?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, address);
            statement.setString(4, phoneNumber);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                Customer customer = new Customer(id, name, address, phoneNumber);
                customerList.add(customer);
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.addRow(new Object[]{id, name, address, phoneNumber});

                JOptionPane.showMessageDialog(table, "Customer added successfully!");
                clearFields(txtId, txtName, txtAddress, txtPhoneNumber);
            }
        } catch (SQLException e1) {
            JOptionPane.showMessageDialog(table, "Customer ID is already in use");
        }
    }

    public void updateCustomer(String id, String name, String address, String phoneNumber, int selectedRow, DefaultTableModel model, JTable table) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Customer SET name = ?, address = ?, phoneNumber = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, address);
            statement.setString(3, phoneNumber);
            statement.setString(4, id);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                model.setValueAt(id, selectedRow, 0);
                model.setValueAt(name, selectedRow, 1);
                model.setValueAt(address, selectedRow, 2);
                model.setValueAt(phoneNumber, selectedRow, 3);
                JOptionPane.showMessageDialog(table, "Customer updated successfully!");
            }
        } catch (SQLException e1) {
            JOptionPane.showMessageDialog(table, "Please enter valid data");
        }
    }

    public void deleteCustomer(int selectedRow, DefaultTableModel model, JTable table) {
        Customer customer = customerList.get(selectedRow);
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Customer WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, customer.getId());
            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                customerList.remove(selectedRow);
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(table, "Customer deleted successfully: " + customer.getId());
            } else {
                JOptionPane.showMessageDialog(table, "Deletion failed! Customer does not exist");
            }
        } catch (SQLException e1) {
            JOptionPane.showMessageDialog(table, "Database connection error: " + e1.getMessage());
        }
    }

    public void loadData(DefaultTableModel model) {
        model.setRowCount(0);
        customerList.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * from customer";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String phoneNumber = rs.getString("phoneNumber");

                Customer customer = new Customer(id, name, address, phoneNumber);
                customerList.add(customer);

                model.addRow(new Object[]{id, name, address, phoneNumber});
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    public void searchCustomerById(String searchId, DefaultTableModel model) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM customer WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, searchId);
            ResultSet resultSet = statement.executeQuery();
            model.setRowCount(0);
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                String phoneNumber = resultSet.getString("phoneNumber");

                model.addRow(new Object[]{id, name, address, phoneNumber});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields(JTextField txtId, JTextField txtName, JTextField txtAddress, JTextField txtPhoneNumber) {
        txtId.setText("");
        txtName.setText("");
        txtAddress.setText("");
        txtPhoneNumber.setText("");
    }
}