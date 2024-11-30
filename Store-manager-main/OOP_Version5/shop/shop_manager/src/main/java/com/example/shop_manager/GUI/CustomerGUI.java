package com.example.shop_manager.GUI;

import com.example.shop_manager.Entity.Customer;
import com.example.shop_manager.Main.DatabaseConnection;
import com.example.shop_manager.Response.CustomerResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class CustomerGUI extends JPanel {

    private JTextField txtId, txtName, txtAddress, txtPhoneNumber;
    private JTable table;
    private DefaultTableModel model;
    private CustomerResponse logic;

    public CustomerGUI() {
        setLayout(new BorderLayout());
        String[] columnNames = {"ID", "Name", "Address", "Phone Number"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2));

        panel.add(new JLabel("ID:"));
        txtId = new JTextField();
        panel.add(txtId);

        panel.add(new JLabel("Name:"));
        txtName = new JTextField();
        panel.add(txtName);

        panel.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        panel.add(txtAddress);

        panel.add(new JLabel("Phone Number:"));
        txtPhoneNumber = new JTextField();
        panel.add(txtPhoneNumber);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        searchPanel.add(new JLabel("Search by ID:"));
        JTextField txtSearchId = new JTextField(10);
        searchPanel.add(txtSearchId);

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchId = txtSearchId.getText();
                logic.searchCustomerById(searchId, model);
            }
        });
        searchPanel.add(btnSearch);

        add(searchPanel, BorderLayout.NORTH);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String id = model.getValueAt(selectedRow, 0).toString();
                    String name = model.getValueAt(selectedRow, 1).toString();
                    String address = model.getValueAt(selectedRow, 2).toString();
                    String phoneNumber = model.getValueAt(selectedRow, 3).toString();
                    txtId.setText(id);
                    txtName.setText(name);
                    txtAddress.setText(address);
                    txtPhoneNumber.setText(phoneNumber);
                }
            }
        });

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String id = txtId.getText();
                String name = txtName.getText();
                String address = txtAddress.getText();
                String phoneNumber = txtPhoneNumber.getText();
                logic.addCustomer(id, name, address, phoneNumber, table, txtId, txtName, txtAddress, txtPhoneNumber);
            }
        });
        panel.add(btnAdd);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(table, "Please select a product first!", "Selection Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String id = txtId.getText();
                String name = txtName.getText();
                String address = txtAddress.getText();
                String phoneNumber = txtPhoneNumber.getText();

                logic.updateCustomer(id, name, address, phoneNumber, selectedRow, model, table);
            }
        });
        panel.add(btnUpdate);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    logic.deleteCustomer(selectedRow, model, table);
                } else {
                    JOptionPane.showMessageDialog(table, "Please select a row to delete!");
                }
            }
        });
        panel.add(btnDelete);

        JButton btnLoadData = new JButton("Load Data");
        btnLoadData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logic.loadData(model);
            }
        });
        panel.add(btnLoadData);
        add(panel, BorderLayout.EAST);
        logic = new CustomerResponse();
    }

}
