// Budget Planner Project - Developed in Java using MySQL
package com.budgetplanner;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BudgetPlannerGUI extends JFrame {
	
	double totalExpense = 0;

    JTextField incomeField, budgetField, categoryField, amountField;
    JTextArea displayArea;

    public BudgetPlannerGUI() {
    	System.out.println("GUI file updated");
    	JOptionPane.showMessageDialog(null, "Welcome to Budget Planner!");

        setTitle("Budget Planner");
        setSize(650, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== HEADING =====
        JLabel heading = new JLabel("Budget Planner", JLabel.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(new Color(0, 200, 255));
        panel.add(heading, BorderLayout.NORTH);

        // ===== INPUT PANEL =====
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBackground(new Color(30, 30, 30));

        JLabel incomeLabel = new JLabel("Enter Total Income:");
        JLabel budgetLabel = new JLabel("Enter Monthly Budget:");
        JLabel categoryLabel = new JLabel("Category:");
        JLabel amountLabel = new JLabel("Amount:");

        incomeField = new JTextField();
        budgetField = new JTextField();
        categoryField = new JTextField();
        amountField = new JTextField();
        
        JLabel[] labels = {incomeLabel, budgetLabel, categoryLabel, amountLabel};
        for (JLabel lbl : labels) {
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        JTextField[] fields = {incomeField, budgetField, categoryField, amountField};
        for (JTextField f : fields) {
            f.setBackground(new Color(50, 50, 50));
            f.setForeground(Color.WHITE);
            f.setCaretColor(Color.WHITE);
        }

        inputPanel.add(incomeLabel);
        inputPanel.add(incomeField);
        inputPanel.add(budgetLabel);
        inputPanel.add(budgetField);
        inputPanel.add(categoryLabel);
        inputPanel.add(categoryField);
        inputPanel.add(amountLabel);
        inputPanel.add(amountField);

        // ===== BUTTONS =====
        JButton addBtn = new JButton("Save Expense");
        JButton calcBtn = new JButton("Get Budget");
        JButton deleteBtn = new JButton("Clear Data");

        JButton[] buttons = {addBtn, calcBtn, deleteBtn};
        for (JButton b : buttons) {
            b.setBackground(new Color(0, 150, 255));
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            b.setFocusPainted(false);
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(30, 30, 30));
        buttonPanel.add(addBtn);
        buttonPanel.add(calcBtn);
        buttonPanel.add(deleteBtn);

        // ===== OUTPUT =====
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        displayArea.setBackground(new Color(40, 40, 40));
        displayArea.setForeground(Color.WHITE);
        displayArea.setLineWrap(true);

        JScrollPane scroll = new JScrollPane(displayArea);
        scroll.setPreferredSize(new Dimension(550, 280));

        // ===== CENTER PANEL =====
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(new Color(30, 30, 30));

        center.add(inputPanel, BorderLayout.NORTH);
        center.add(buttonPanel, BorderLayout.CENTER);
        center.add(scroll, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        add(panel);

        // ===== ADD EXPENSE =====
        addBtn.addActionListener(e -> {
            try {
                Connection conn = DBConnection.getConnection();

                String category = categoryField.getText();
                double amount = Double.parseDouble(amountField.getText());
                totalExpense += amount;
                System.out.println("Total Expense: " + totalExpense);
                JOptionPane.showMessageDialog(null, "Total Expense: " + totalExpense);

                String query = "INSERT INTO transactions(type, category, amount) VALUES ('Expense', ?, ?)";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, category);
                pst.setDouble(2, amount);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Expense Added!");

                categoryField.setText("");
                amountField.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter valid data!");
            }
        });

        // ===== CLEAR DATA =====
        deleteBtn.addActionListener(e -> {
            try {
                Connection conn = DBConnection.getConnection();

                String query = "DELETE FROM transactions";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.executeUpdate();

                displayArea.setText("");
                JOptionPane.showMessageDialog(this, "All data cleared!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error clearing data!");
            }
        });

        // ===== GET BUDGET =====
        calcBtn.addActionListener(e -> generateBudget());

        setVisible(true);
    }

    // ===== MAIN LOGIC =====
    public void generateBudget() {
        try {
            Connection conn = DBConnection.getConnection();

            double income = Double.parseDouble(incomeField.getText());
            double budget = Double.parseDouble(budgetField.getText());

            String totalQuery = "SELECT SUM(amount) FROM transactions WHERE type='Expense'";
            PreparedStatement pst1 = conn.prepareStatement(totalQuery);
            ResultSet rs1 = pst1.executeQuery();

            double totalExpense = 0;
            if (rs1.next()) totalExpense = rs1.getDouble(1);

            double balance = income - totalExpense;

            String breakdownQuery = "SELECT category, SUM(amount) as total FROM transactions WHERE type='Expense' GROUP BY category";
            PreparedStatement pst2 = conn.prepareStatement(breakdownQuery);
            ResultSet rs2 = pst2.executeQuery();

            displayArea.setText("");

            displayArea.append("Total Income: ₹" + income + "\n");
            displayArea.append("Total Expenses: ₹" + totalExpense + "\n");
            displayArea.append("Remaining Balance: ₹" + balance + "\n\n");

            displayArea.append("\nExpense Breakdown:\n");

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
            JOptionPane.showMessageDialog(this, "Enter valid numbers!");
        }
    }

    public static void main(String[] args) {
        new BudgetPlannerGUI();
    }
}





