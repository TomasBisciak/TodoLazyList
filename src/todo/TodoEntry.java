/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.beans.property.SimpleStringProperty;
import todo.hsqldb.HSQLDBConnector;

/**
 *
 * @author tomas bisciak
 */
public class TodoEntry {

    private SimpleStringProperty  id;
    private SimpleStringProperty  category;
    private SimpleStringProperty  message;
    private SimpleStringProperty  priority;

    private SimpleStringProperty[] data;

    public static final short MAX_SIZE = 0xff;
    public static final short MIN_SIZE = 0x1;
    public static final short SLASH = 0x81;
    public static final String PREFIX = "//TODO";
    public static final String POSTFIX = "//";

    public static final String insertTodoEntry = "INSERT INTO todos (category,message,priority)"
            + "VALUES (?,?,?);";

    //creation for tableview entry (visualization)
    public TodoEntry(String id, String category, String message, String priority) {
        this.id = new SimpleStringProperty(id);
        this.category = category == null ? new SimpleStringProperty("GENERAL") : new SimpleStringProperty(category);
        this.priority = priority == null ? new SimpleStringProperty("MEDIUM") : new SimpleStringProperty(priority);
        this.message = new SimpleStringProperty(message);
        data = new SimpleStringProperty[]{this.category, this.message, this.priority};
    }

    //adding into database creation .. automaticly fills up column for id
    public TodoEntry(String category, String message, String priority) {
          this.category = category == null ? new SimpleStringProperty("GENERAL") : new SimpleStringProperty(category);
        this.priority = priority == null ? new SimpleStringProperty("MEDIUM") : new SimpleStringProperty(priority);
        this.message = new SimpleStringProperty(message);

       data = new SimpleStringProperty[]{this.category, this.message, this.priority};
    }

    public PreparedStatement prepareQuery(HSQLDBConnector connector) {
        try {

            for (int i = 0, a = 1; i < data.length; i++, a++) {
                connector.todoEntryPreparedStatement.setString(a, data[i].get());

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return connector.todoEntryPreparedStatement;
    }

    @Override
    public String toString() {
        return String.format("Todo Entry- category:%s ----message:%s ---priority:%s", getCategory(), getMessage(), getPriority());
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public String getMessage() {
        return message.get();
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public String getPriority() {
        return priority.get();
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }


}
