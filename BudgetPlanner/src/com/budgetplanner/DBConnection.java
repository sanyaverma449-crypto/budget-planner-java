package com.budgetplanner;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/budget_planner",
                "root",
                "Sanya123_456."
            );
            System.out.println("Database connected successfully");
        } catch (Exception e) {
            System.out.println("Database connection failed");
            e.printStackTrace();
        }
        
        return conn;
    }
}


