package com.todo;

import com.todo.util.DatabaseConnection;
public class Main
{
    public static void main(String args[])
    {
        DatabaseConnection db_Connection = new DatabaseConnection();
        try {
            db_Connection.getDBConnection();
            System.out.println("Database connected successfully.");
        } catch (Exception e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }
}