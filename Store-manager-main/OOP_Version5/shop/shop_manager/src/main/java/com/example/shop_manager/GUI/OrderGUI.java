package com.example.shop_manager.GUI;

import com.example.shop_manager.Response.OrderResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderGUI extends JPanel {
    private JTextField txtCustomerId, txtProductId, txtQuantity, txtOrderId;
    private JTable orderTable;
    private DefaultTableModel tableModel;

    public OrderGUI() {
        setLayout(new BorderLayout());
        // Table columns
        String[] columnNames = {"Order ID", "Customer ID", "Customer Name", "Product ID", "Price", "Quantity", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0);
        orderTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2));

        inputPanel.add(new JLabel("Order ID:"));
        txtOrderId = new JTextField();
        inputPanel.add(txtOrderId);

        inputPanel.add(new JLabel("Customer ID:"));
        txtCustomerId = new JTextField();
        inputPanel.add(txtCustomerId);
        inputPanel.add(new JLabel("Product ID:"));
        txtProductId = new JTextField();
        inputPanel.add(txtProductId);

        inputPanel.add(new JLabel("Quantity:"));
        txtQuantity = new JTextField();
        inputPanel.add(txtQuantity);

        // Button setup
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> addOrder());
        inputPanel.add(btnAdd);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> deleteOrder());
        inputPanel.add(btnDelete);

        JButton btnEdit = new JButton("Update");
        btnEdit.addActionListener(e -> updateOrder());
        inputPanel.add(btnEdit);

        JButton btnLoadData = new JButton("LoadData");
        btnLoadData.addActionListener(e -> loadData());
        inputPanel.add(btnLoadData);

        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = orderTable.getSelectedRow();
                if (selectedRow != -1) {
                    String id = orderTable.getValueAt(selectedRow, 0).toString();
                    String customer_id = orderTable.getValueAt(selectedRow, 1).toString();
                    String product_id = orderTable.getValueAt(selectedRow, 3).toString();
                    String quantity = orderTable.getValueAt(selectedRow, 5).toString();
                    txtOrderId.setText(id);
                    txtCustomerId.setText(customer_id);
                    txtProductId.setText(product_id);
                    txtQuantity.setText(quantity);
                }
            }
        });
        add(inputPanel, BorderLayout.EAST);
    }

    private void addOrder() {
        OrderResponse.addOrder(tableModel,txtOrderId.getText(), txtCustomerId.getText(), txtProductId.getText(), txtQuantity.getText());
    }

    private void deleteOrder() {
        OrderResponse.deleteOrder(txtOrderId.getText(), orderTable, tableModel);
    }

    private void updateOrder() {
        OrderResponse.updateOrder(txtOrderId.getText(), txtCustomerId.getText(), txtProductId.getText(), txtQuantity.getText(), orderTable, tableModel);
    }

    private void loadData() {
        OrderResponse.loadData(tableModel);
    }

    private void clearFields() {
        txtCustomerId.setText("");
        txtProductId.setText("");
        txtQuantity.setText("");
    }
}
