/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import todo.hsqldb.DbUtils;

/**
 *
 * @author tomas
 */
public class DataModel {

    public int totalTodos;
    private static StringBuilder sb;

    private ArrayList<Object> datList;

    public static ObservableList<String> items = FXCollections.observableArrayList(
            "Total todos:", "High priority:", "Medium priority", "Low priority", "Total categories:",
            "Biggest category:", "Todos since:", "Custom connection:");

    public DataModel() {
        sb = new StringBuilder();
    }

    public void overrideCfg(String... args) {
        String s = String.format("%s==:%s==:%s==:%s==:%s", args[0], args[1], args[2], args[3], args[4]);
        FileUtils.writeToFile(Todo.TODO_CONFIG_FILE, s, false);
    }

    public void overrideCustomConnectionCfg(String[] args) {
        String s = String.format("%s==:%s==:%s", args[0], args[1], args[2]);
        FileUtils.writeToFile(FXMLMainViewController.TODO_CUSTOM_CONNECTION_CONFIG, s, false);
    }

    public String[] readCfg() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(Todo.TODO_CONFIG_FILE))) {
            return bufferedReader.readLine().split("==:");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateTable(TableView tableView, ArrayList<TableColumn> arr) {
        ObservableList<TodoEntry> ob = DbUtils.readTodosFromDb();
        calculateStats();
        tableView.setItems(ob);
        //maybe columns after thiws
    }

    private void calculateStats() {

        Platform.runLater(() -> {

            datList = checkData();
            int[] priorities = (int[]) datList.get(0);

            Todo.fxc.listView.getItems().set(0, "Total todos:" + getTotalTodos());
            Todo.fxc.listView.getItems().set(1, "High priority:" + priorities[0]);
            Todo.fxc.listView.getItems().set(2, "Medium priority:" + priorities[1]);
            Todo.fxc.listView.getItems().set(3, "Low priority:" + priorities[2]);
            Todo.fxc.listView.getItems().set(4, "Total categories:" + datList.get(1));
            Todo.fxc.listView.getItems().set(5, "Biggest category:" + datList.get(2));
            Todo.fxc.listView.getItems().set(6, "Todos since:" + (int) datList.get(3));
            Todo.fxc.listView.getItems().set(7, "Custom connection:" + Todo.isCustomConnection);

        });

    }

    public int getTotalTodos() {
        return Todo.fxc.tableView.getItems().size();
    }

    public ArrayList<Object> checkData() {
        ArrayList<Object> arr = new ArrayList<>();
        ArrayList<String> cat = new ArrayList<>();
        HashMap<String, Integer> ck = new HashMap<>();

        int[] prio = new int[3];
        int[] since = new int[1];
        since[0] = -1;

        Todo.fxc.tableView.getItems().forEach((Object i) -> {
            TodoEntry t = ((TodoEntry) i);
            sb.append(t.getPriority());

            if (sb.toString().equals("HIGH")) {
                prio[0]++;
            }
            if (sb.toString().equals("MEDIUM")) {
                prio[1]++;
            }
            if (sb.toString().equals("LOW")) {
                prio[2]++;
            }

            sb.delete(0, sb.length());

            //get highest
            if (Integer.parseInt(t.getId()) > since[0]) {
                since[0] = Integer.parseInt(t.getId());
            }
            
            if (!cat.contains(t.getCategory())) {
                cat.add(t.getCategory());
                ck.put(t.getCategory(), 0);
            } else {
               
                Integer c = ck.get(t.getCategory());
                ck.put(t.getCategory(), c++);
                    System.out.println("incrementing contained "+t.getCategory()+"  val "+c);
            }

        });

        Integer[] val = Arrays.copyOf(ck.values().toArray(), ck.values().size(), Integer[].class);
        String[] keys = Arrays.copyOf(ck.keySet().toArray(), ck.keySet().size(), String[].class);
        int bv=-1;
        String bc="";
        for (int i = 0,cv=-1; i < val.length; i++) {
            cv=val[i];
            System.out.println("debug-key:"+keys[i]+ "    val:"+val[i]);//include correctly
            if(cv>=bv){
                bv=cv;
                bc=keys[i];
            }
                
        }
        
  
        arr.add(prio);
        arr.add(cat.size());//number of categories
        arr.add(bc);
        arr.add(since[0]);//highest(since)

        return arr;
    }

}
