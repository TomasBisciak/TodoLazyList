/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import todo.Executable;
import todo.Todo;
import todo.TodoEntry;

/**
 * 
 * @author tomas
 */
public class HSQLDBConnector {

    public Connection connection;
    public Statement statement;
    public ResultSet resultSet;

    public PreparedStatement todoEntryPreparedStatement = null;
    public ResultSetMetaData resultSetMd;

    public static final String MEM_TYPEDB = "file";
    public static final String FILE_TYPEDB = "mem";
    public static final String RES_TYPEDB = "res";

    private HSQLDBConfigurator configurator;

    public HSQLDBConnector(Executable e, boolean custom) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            openConnection(Todo.globalConfigurator,custom);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        e.execute();
    }

    public HSQLDBConnector(boolean custom) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            openConnection(Todo.globalConfigurator,custom);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    //to override methods
    public void execute() {
    }

    public void execute(String query) {
    }

    public <T extends Iterable> T executeRetrieve() {
        return null;
    }

    public void closeConnector() {

        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (resultSet != null) {
                statement.close();
            }
            if (resultSet != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public final void openConnection(HSQLDBConfigurator configurator,boolean custom) {

        try {

            if (custom) {
                
                //
                
            } else {

                connection = DriverManager.getConnection(
                        "jdbc:hsqldb:" + configurator.getType()+ ":" + configurator.getDatabaseDir() + HSQLDBConfigurator.DBFILE + ";ifxeists=true",
                        configurator.getUsername(), configurator.getPassword());
            }
            statement = connection.createStatement();
            todoEntryPreparedStatement = connection.prepareStatement(TodoEntry.insertTodoEntry);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public HSQLDBConfigurator getConfigurator() {
        return configurator;
    }

    public void setConfigurator(HSQLDBConfigurator configurator) {
        this.configurator = configurator;
    }

}
