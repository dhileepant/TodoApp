package com.todo.gui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.todo.dao.TodoAppDAO;
import java.util.List;
import com.todo.model.Todo;
import java.sql.SQLException;

public class TodoGUI extends JFrame {
    private TodoAppDAO todoAppDAO;
    private JTable todoTable;
    private DefaultTableModel tableModel;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JCheckBox completedCheckBox;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JComboBox<String> filterComboBox;

    public TodoGUI() {
        this.todoAppDAO = new TodoAppDAO();
        initializeComponents();
        setupLayout();
        wireActions();
        refreshTable();
    }

    public void initializeComponents() {
        setTitle("Todo Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        String[] columnNames = {"ID", "Title", "Description", "Completed", "Created At", "Updated At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        todoTable = new JTable(tableModel);
        todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoTable.getSelectionModel().addListSelectionListener(
            (e) -> {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedTodo();
                }
            }
        );
        titleField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        completedCheckBox = new JCheckBox("Completed");
        addButton = new JButton("Add Todo");
        updateButton = new JButton("Update Todo");
        deleteButton = new JButton("Delete Todo");
        refreshButton = new JButton("Refresh Todo");
        String[] filterOptions = {"All", "Completed", "Pending"};
        filterComboBox = new JComboBox<>(filterOptions);
        filterComboBox.addActionListener(
            (e) -> {
                filterTodos();
            }
        );
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(titleField, gbc);
        gbc.gridx=0;
        gbc.gridy =1;
        inputPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx =1;
        inputPanel.add(descriptionArea, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        inputPanel.add(completedCheckBox, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(addButton, gbc);
        gbc.gridx = 1;
        inputPanel.add(updateButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(deleteButton, gbc);
        gbc.gridx = 1;
        inputPanel.add(refreshButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        inputPanel.add(new JLabel("Filter:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(filterComboBox, gbc);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(todoTable), BorderLayout.CENTER);
    }

    private void wireActions() {
        addButton.addActionListener((e) -> onAdd());
        updateButton.addActionListener((e) -> onUpdate());
        deleteButton.addActionListener((e) -> onDelete());
        refreshButton.addActionListener((e) -> refreshTable());
    }

    private void refreshTable() {
        try {
            List<Todo> todos = todoAppDAO.findAll();
            tableModel.setRowCount(0);
            for (Todo t : todos) {
                tableModel.addRow(new Object[]{
                    t.getID(), t.getTitle(), t.getDescription(), t.isCompleted(), t.getCreated_at(), t.getUpdated_at()
                });
            }
        } catch (SQLException ex) {
            showError("Failed to load todos: " + ex.getMessage());
        }
    }

    private void onAdd() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckBox.isSelected();
        if (title.isEmpty()) {
            showError("Title is required.");
            return;
        }
        try {
            Todo todo = new Todo(title, description);
            todo.setCompleted(completed);
            todoAppDAO.insert(todo);
            clearInputs();
            refreshTable();
        } catch (SQLException ex) {
            showError("Failed to add todo: " + ex.getMessage());
        }
    }

    private void onUpdate() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Select a row to update.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckBox.isSelected();
        if (title.isEmpty()) {
            showError("Title is required.");
            return;
        }
        try {
            Todo todo = new Todo();
            todo.setID(id);
            todo.setTitle(title);
            todo.setDescription(description);
            todo.setCompleted(completed);
            if (!todoAppDAO.update(todo)) {
                showError("Update failed or no rows affected.");
            }
            refreshTable();
        } catch (SQLException ex) {
            showError("Failed to update todo: " + ex.getMessage());
        }
    }

    private void onDelete() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Select a row to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            if (!todoAppDAO.delete(id)) {
                showError("Delete failed or no rows affected.");
            }
            refreshTable();
        } catch (SQLException ex) {
            showError("Failed to delete todo: " + ex.getMessage());
        }
    }

    private void loadSelectedTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow >= 0) {
            titleField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 1)));
            descriptionArea.setText(String.valueOf(tableModel.getValueAt(selectedRow, 2)));
            Object completedObj = tableModel.getValueAt(selectedRow, 3);
            boolean completed = false;
            if (completedObj instanceof Boolean) {
                completed = (Boolean) completedObj;
            } else if (completedObj != null) {
                completed = Boolean.parseBoolean(completedObj.toString());
            }
            completedCheckBox.setSelected(completed);
        }
    }

    private void filterTodos() {
        String filter = (String) filterComboBox.getSelectedItem();
        if (filter == null || filter.equals("All")) {
            refreshTable();
            return;
        }
        boolean showCompleted = filter.equals("Completed");
        int rowCount = tableModel.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            Object completedObj = tableModel.getValueAt(i, 3);
            boolean completed = false;
            if (completedObj instanceof Boolean) {
                completed = (Boolean) completedObj;
            } else if (completedObj != null) {
                completed = Boolean.parseBoolean(completedObj.toString());
            }
            if (completed != showCompleted) {
                tableModel.removeRow(i);
            }
        }
    }

    private void clearInputs() {
        titleField.setText("");
        descriptionArea.setText("");
        completedCheckBox.setSelected(false);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}