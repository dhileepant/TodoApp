package com.todo.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
    public static final String URL = "jdbc:mysql://localhost:3306/todo";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "Dhileepan@13";

    static 
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            System.out.println("Driver is missing");
        }
    }

    public Connection getDBConnection() throws SQLException
    {
        return DriverManager.getConnection(URL,USERNAME,PASSWORD);
    }

    // New API used by Main.java
    public Connection getConnection() throws SQLException {
        return getDBConnection();
    }
}