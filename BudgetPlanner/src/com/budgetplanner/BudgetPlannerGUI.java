// Budget Planner Project - Developed in Java using MySQL
package com.budgetplanner;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BudgetPlannerGUI extends JFrame {

	JTextField incomeField, budgetField, categoryField, amountField;
    JTextArea displayArea;

    public BudgetPlannerGUI() {

        setTitle("Budget Planner");
        setSize(650, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(new Color(30, 30, 30));

        JLabel heading = new JLabel("Budget Planner", JLabel.CENTER);
        heading.setBounds(0, 35, 650, 40);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(new Color(0, 200, 255));
        add(heading);

        JLabel incomeLabel = createLabel("Total Income:");
        incomeLabel.setBounds(10, 90, 160, 25);
        add(incomeLabel);

        JLabel budgetLabel = createLabel("Set Budget:");
        budgetLabel.setBounds(10, 125, 160, 25);
        add(budgetLabel);

        JLabel categoryLabel = createLabel("Category:");
        categoryLabel.setBounds(10, 160, 160, 25);
        add(categoryLabel);

        JLabel amountLabel = createLabel("Amount:");
        amountLabel.setBounds(10, 195, 160, 25);
        add(amountLabel);

        incomeField = createField();
        incomeField.setBounds(330, 85, 300, 25);
        add(incomeField);

        budgetField = createField();
        budgetField.setBounds(330, 120, 300, 25);
        add(budgetField);

        categoryField = createField();
        categoryField.setBounds(330, 155, 300, 25);
        add(categoryField);

        amountField = createField();
        amountField.setBounds(330, 190, 300, 25);
        add(amountField);

        JButton addBtn = createButton("Add Expense");
        addBtn.setBounds(150, 245, 120, 32);
        add(addBtn);

        JButton calcBtn = createButton("Get Budget");
        calcBtn.setBounds(280, 245, 120, 32);
        add(calcBtn);

        JButton deleteBtn = createButton("Clear Data");
        deleteBtn.setBounds(410, 245, 120, 32);
        add(deleteBtn);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        displayArea.setBackground(new Color(40, 40, 40));
        displayArea.setForeground(Color.WHITE);
        displayArea.setCaretColor(Color.WHITE);

        JScrollPane scroll = new JScrollPane(displayArea);
        scroll.setBounds(10, 290, 620, 220);
        add(scroll);

        addBtn.addActionListener(e -> addExpense());
        calcBtn.addActionListener(e -> generateBudget());
        deleteBtn.addActionListener(e -> clearData());

        setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private JTextField createField() {
        JTextField field = new JTextField();
        field.setBackground(new Color(45, 45, 45));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 150, 255));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        return button;
    }

    public void addExpense() {
        try {
            String category = categoryField.getText().trim();
            double amount = Double.parseDouble(amountField.getText().trim());

            Connection conn = DBConnection.getConnection();

            String query = "INSERT INTO transactions(type, category, amount) VALUES ('Expense', ?, ?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, category);
            pst.setDouble(2, amount);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Expense added");

            categoryField.setText("");
            amountField.setText("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding expense");
            ex.printStackTrace();
        }
    }

    public void generateBudget() {
        try {
            double income = Double.parseDouble(incomeField.getText().trim());
            double budget = Double.parseDouble(budgetField.getText().trim());

            Connection conn = DBConnection.getConnection();

            PreparedStatement pst1 = conn.prepareStatement(
                    "SELECT SUM(amount) FROM transactions WHERE type='Expense'");
            ResultSet rs1 = pst1.executeQuery();

            double totalExpense = 0;
            if (rs1.next()) {
                totalExpense = rs1.getDouble(1);
            }

            double balance = income - totalExpense;

            PreparedStatement pst2 = conn.prepareStatement(
                    "SELECT category, SUM(amount) AS total FROM transactions WHERE type='Expense' GROUP BY category");
            ResultSet rs2 = pst2.executeQuery();

            displayArea.setText("");

            displayArea.append("Total Income: ₹" + income + "\n");
            displayArea.append("Total Expenses: ₹" + totalExpense + "\n");
            displayArea.append("Remaining Balance: ₹" + balance + "\n\n");

            displayArea.append("Breakdown:\n");

            double maxAmount = 0;
            String maxCategory = "";

            while (rs2.next()) {
                String category = rs2.getString("category");
                double amt = rs2.getDouble("total");

                displayArea.append(category + ": ₹" + amt + "\n");

                if (amt > maxAmount) {
                    maxAmount = amt;
                    maxCategory = category;
                }
            }

            displayArea.append("\n");

            if (!maxCategory.isEmpty()) {
                displayArea.append("You are spending the most on: " + maxCategory + " (₹" + maxAmount + ")\n");
            }

            displayArea.append("\n");

            if (totalExpense > budget) {
                displayArea.append("Status: You are OVER your budget.\n");
                displayArea.append("Suggestion: Reduce spending in " + maxCategory + "\n");
            } else if (totalExpense > budget * 0.8) {
                displayArea.append("Status: You are CLOSE to your budget limit.\n");
            } else {
                displayArea.append("Status: You are ON TRACK with your budget.\n");
            }

            displayArea.append("You can save ₹" + balance);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers");
            e.printStackTrace();
        }
    }

    public void clearData() {
        try {
            Connection conn = DBConnection.getConnection();

            PreparedStatement pst = conn.prepareStatement("DELETE FROM transactions");
            pst.executeUpdate();

            displayArea.setText("");
            JOptionPane.showMessageDialog(this, "Data cleared");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error clearing data");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new BudgetPlannerGUI();
    }
}
