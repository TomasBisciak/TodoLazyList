/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo.hsqldb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import todo.Todo;
import todo.TodoEntry;

/**
 *
 * @author tomas
 */
public class DbUtils {

    public static ObservableList<TodoEntry> readTodosFromDb() {

        ArrayList<String> rows = new ArrayList<>();
        ObservableList<TodoEntry> data = FXCollections.observableArrayList();
        new HSQLDBConnector(Todo.isCustomConnection) {

            @Override
            public void execute() {

                try {
                    resultSet = statement.executeQuery("SELECT * FROM todos;");
                    resultSetMd = resultSet.getMetaData();
                    while (resultSet.next()) {
//                        if (resultSet.isLast()) {
//                            Todo.datMod.totalTodos = resultSet.getRow();
//                        }
                        for (int i = 1; i < this.resultSetMd.getColumnCount() + 1; i++) {
                            rows.add(resultSet.getString(i));
                        }

                        data.add(new TodoEntry(rows.get(0), rows.get(1), rows.get(2), rows.get(3)));
                        rows.removeAll(rows);
                    }
                    closeConnector();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    closeConnector();
                }
            }

        }.execute();

        return data;
    }

    public static void checkCustomConnection() {
        while (true) {
            try {
                StringBuilder sbc = new StringBuilder();
                Todo.connector = new HSQLDBConnector(Todo.isCustomConnection);
                Todo.connector.statement.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES");
                int count = 0;

                while (Todo.connector.resultSet.next()) {
                    sbc.append(Todo.connector.resultSet.getString(count));
                    if (sbc.toString().equals("TODOS") || sbc.toString().equals("todos")) {
                        Todo.isCustomConnection = false;
                    }
                    sbc.delete(0, sbc.length());
                    count++;
                }

                Todo.connector.closeConnector();
                break;
            } catch (SQLException e) {
                Todo.isCustomConnection = false;
                e.printStackTrace();
            }

        }
    }

    public static void addTodoIntoDb(TodoEntry entry) {

        new HSQLDBConnector(Todo.isCustomConnection) {

            @Override
            public void execute() {

                try {
                    entry.prepareQuery(this).executeUpdate();
                    closeConnector();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    closeConnector();
                }

            }

        }.execute();

    }

}
