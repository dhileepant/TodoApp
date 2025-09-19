package com.todo;

import com.todo.util.DatabaseConnection;
import com.todo.gui.TodoGUI;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection dbConn = new DatabaseConnection();
        try {
            dbConn.getConnection();
            System.out.println("Database connected successfully.");
        } catch (Exception e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Failed to set look and feel." + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> {
            try {
                TodoGUI gui = new TodoGUI();
                gui.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to create and display the GUI." + e.getMessage());
            }
        });
    }
}