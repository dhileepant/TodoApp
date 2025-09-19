package com.todo.dao;

import com.todo.model.Todo;
import com.todo.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TodoAppDAO {
    private final DatabaseConnection databaseConnection;

    public TodoAppDAO() {
        this.databaseConnection = new DatabaseConnection();
    }

    public List<Todo> findAll() throws SQLException {
        String query = "SELECT id, title, description, completed, created_at, updated_at FROM todos ORDER BY id DESC";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            List<Todo> todos = new ArrayList<>();
            while (resultSet.next()) {
                todos.add(mapRowToTodo(resultSet));
            }
            return todos;
        }
    }

    public Todo insert(Todo todo) throws SQLException {
        String sql = "INSERT INTO todos (title, description, completed, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        if (todo.getCreated_at() == null) todo.setCreated_at(now);
        todo.setUpdated_at(now);
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, todo.getTitle());
            statement.setString(2, todo.getDescription());
            statement.setBoolean(3, todo.isCompleted());
            statement.setTimestamp(4, Timestamp.valueOf(todo.getCreated_at()));
            statement.setTimestamp(5, Timestamp.valueOf(todo.getUpdated_at()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    todo.setID(keys.getInt(1));
                }
            }
            return todo;
        }
    }

    public boolean update(Todo todo) throws SQLException {
        String sql = "UPDATE todos SET title = ?, description = ?, completed = ?, updated_at = ? WHERE id = ?";
        todo.setUpdated_at(LocalDateTime.now());
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, todo.getTitle());
            statement.setString(2, todo.getDescription());
            statement.setBoolean(3, todo.isCompleted());
            statement.setTimestamp(4, Timestamp.valueOf(todo.getUpdated_at()));
            statement.setInt(5, todo.getID());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM todos WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Todo mapRowToTodo(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        boolean completed = resultSet.getBoolean("completed");
        Timestamp createdTs = resultSet.getTimestamp("created_at");
        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
        LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
        return new Todo(id, title, description, completed, createdAt, updatedAt);
    }
}