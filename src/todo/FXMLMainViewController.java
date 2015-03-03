/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jfx.messagebox.MessageBox;
import static todo.Todo.TODO_DIR;
import static todo.Todo.datMod;
import todo.hsqldb.DbUtils;
import todo.hsqldb.HSQLDBConfigurator;
import todo.hsqldb.HSQLDBConnector;

/**
 *
 * @author tomas
 */
public class FXMLMainViewController implements Initializable {

    public static final String TODO_CUSTOM_CONNECTION_CONFIG = Todo.TODO_DIR + File.separator + "customconn.cfg";

    @FXML
    private TextArea messTextArea;
    @FXML
    private TextArea descTextArea;
    @FXML
    private Label dlblEntry;
    @FXML
    private Label dlbCategory;
    @FXML
    private Label dlbPriority;

    //db
    @FXML
    private Label dirLabel;
    @FXML
    private TextField dbsUrl;
    @FXML
    private ChoiceBox dbsType;
    @FXML
    private Button chburl;
    @FXML
    private Button chbtype;

    @FXML
    public ListView listView;

    @FXML
    private Button removeBtn;
    @FXML
    private Button removeAllBtn;
    @FXML
    private Button JDBCConnBtn;

    @FXML
    private Hyperlink hsqldbHl;
    @FXML
    private Hyperlink jdbcHl;
    @FXML
    public TableView tableView;
    @FXML
    private ImageView todoImageView;

    private TodoEntry currentSelect;
    @FXML
    private Button donatebtn;

    private ArrayList<TableColumn> tcmns = new ArrayList<>();

    private ArrayList<Integer> mulSelect = new ArrayList<>();

    public FXMLMainViewController() {
        tcmns.add(new TableColumn("ID"));
        tcmns.add(new TableColumn("Category"));
        tcmns.add(new TableColumn("Message"));
        tcmns.add(new TableColumn("Priority"));

        messTextArea = new TextArea();
        descTextArea = new TextArea();
        dlblEntry = new Label();
        dlbCategory = new Label();
        dlbPriority = new Label();
        tableView = new TableView();
        todoImageView = new ImageView();
        dbsUrl = new TextField();
        dbsType = new ChoiceBox();
        dirLabel = new Label();
        listView = new ListView();
        chburl = new Button();
        chbtype = new Button();
        removeBtn = new Button();
        removeAllBtn = new Button();
        JDBCConnBtn = new Button();

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listView.setItems(DataModel.items);
        removeBtn.setDisable(true);
        
        //SQL injection unprotected
        Callback<TableColumn, TableCell> cellFactory
                = (TableColumn p) -> new EditingCell();

        tcmns.get(0).setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        tcmns.get(1).setCellValueFactory(
                new PropertyValueFactory<>("category")
        );

        tcmns.get(1).setCellFactory(cellFactory);

        tcmns.get(1).setOnEditCommit(
                new EventHandler<CellEditEvent<TodoEntry, String>>() {
                    @Override
                    public void handle(CellEditEvent<TodoEntry, String> t) {
                        ((TodoEntry) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setCategory(t.getNewValue());

                        new HSQLDBConnector(Todo.isCustomConnection) {

                            @Override
                            public void execute() {
                                try {
                                    this.statement.executeUpdate("UPDATE todos SET category='" + t.getNewValue() + "' WHERE todoId='" + t.getRowValue().getId() + "';");
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                                closeConnector();

                            }

                        }.execute();

                    }
                }
        );
        chburl.setOnAction((ActionEvent event) -> {
            dbsUrl.getText();
            datMod.overrideCfg(dbsUrl.getText(), "file", "TODO", "TODODB", "false");
        });
        chbtype.setOnAction((ActionEvent event) -> {
            dbsUrl.getText();
            datMod.overrideCfg(Todo.TODO_DIR, (String) dbsType.getSelectionModel().getSelectedItem(), "TODO", "TODODB", "false");
        });

        tcmns.get(2).setCellValueFactory(
                new PropertyValueFactory<>("message")
        );

        tcmns.get(2).setCellFactory(cellFactory);
        tcmns.get(2).setPrefWidth(450);
        tcmns.get(2).setOnEditCommit(
                new EventHandler<CellEditEvent<TodoEntry, String>>() {
                    @Override
                    public void handle(CellEditEvent<TodoEntry, String> t) {
                        ((TodoEntry) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setMessage(t.getNewValue());

                        new HSQLDBConnector(Todo.isCustomConnection) {

                            @Override
                            public void execute() {
                                try {
                                    this.statement.executeUpdate("UPDATE todos SET message='" + t.getNewValue() + "' WHERE todoId='" + t.getRowValue().getId() + "';");
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                                closeConnector();

                            }

                        }.execute();

                    }
                }
        );

        tcmns.get(3).setCellValueFactory(
                new PropertyValueFactory<>("priority")
        );

        tcmns.get(3).setCellFactory(cellFactory);

        tcmns.get(3).setOnEditCommit(
                new EventHandler<CellEditEvent<TodoEntry, String>>() {
                    @Override
                    public void handle(CellEditEvent<TodoEntry, String> t) {
                        ((TodoEntry) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setPriority(t.getNewValue());

                        new HSQLDBConnector(Todo.isCustomConnection) {

                            @Override
                            public void execute() {
                                try {
                                    this.statement.executeUpdate("UPDATE todos SET priority='" + t.getNewValue() + "' WHERE todoId='" + t.getRowValue().getId() + "';");
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                                closeConnector();

                            }

                        }.execute();

                    }
                }
        );

        tableView.setEditable(true);
        tableView.getColumns().addAll(getTcmns());
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //tableView.getSelectionModel().getSelectedItem()

        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change c) -> {
            mulSelect.removeAll(mulSelect);
            currentSelect = ((TodoEntry) c.getList().get(0));
            if (currentSelect != null) {
                updateDetail(currentSelect);
            }
            //show current selected 
            //Todo.datMod.updateTable(tableView, tcmns);

            c.getList().forEach((Object t) -> {
                mulSelect.add(Integer.parseInt(((TodoEntry) t).getId()));
                if (mulSelect.size() > 0) {
                    removeBtn.setDisable(false);
                } else {
                    removeBtn.setDisable(true);
                }
            });
        });

        JDBCConnBtn.setOnAction((ActionEvent event) -> {

            Stage dialog = new Stage(StageStyle.UTILITY);

            HBox parentRoot = new HBox();
            parentRoot.setSpacing(5);
            parentRoot.setAlignment(Pos.CENTER);
            TextField tf = new TextField();

            Button button = new Button("Confirm");

            button.setOnAction((ActionEvent event1) -> {

                int selected = MessageBox.show(null,
                        "Are you sure?",
                        "Confirmation",
                        MessageBox.ICON_QUESTION | MessageBox.YES | MessageBox.NO | MessageBox.DEFAULT_BUTTON2);
                if (selected == MessageBox.YES) {

                    if (!Files.exists(Paths.get(TODO_CUSTOM_CONNECTION_CONFIG))) {
                        try {
                            Files.createFile(Paths.get(TODO_CUSTOM_CONNECTION_CONFIG));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                    Todo.datMod.overrideCustomConnectionCfg(tf.getText().split(","));

                }//end of if equals yes

            });

            tf.setEditable(true);
            tf.setMinWidth(400);
            tf.setPromptText("url,user,password");
            parentRoot.getChildren().add(tf);
            parentRoot.getChildren().add(button);
            Scene scene = new Scene(parentRoot);
            dialog.setScene(scene);
            dialog.setWidth(500);

            dialog.setTitle("Custom JDBC");
            dialog.setResizable(false);
            dialog.showAndWait();

        });

        // removeBtn.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        removeBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                new HSQLDBConnector(Todo.isCustomConnection) {

                    @Override
                    public void execute() {
                        try {
                            for (Integer sel : mulSelect) {
                                this.statement.executeUpdate("DELETE FROM todos WHERE todoId='" + sel + "';");
                            }
                            Todo.datMod.updateTable(tableView, tcmns);
                            // currentSelect = -1;

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        closeConnector();

                    }

                }.execute();

            }

        });

        removeAllBtn.setOnAction((ActionEvent event) -> {
            int selected = MessageBox.show(null,
                    "Are you sure?",
                    "Confirmation",
                    MessageBox.ICON_QUESTION | MessageBox.YES | MessageBox.NO | MessageBox.DEFAULT_BUTTON2);

            if (selected == MessageBox.YES) {
                new HSQLDBConnector(Todo.isCustomConnection) {

                    @Override
                    public void execute() {
                        try {
                            this.statement.executeUpdate("DELETE FROM todos;");
                            Todo.datMod.updateTable(tableView, tcmns);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        closeConnector();

                    }

                }.execute();

            }
        });

        todoImageView.setImage(new Image(getClass().getResourceAsStream("/todo/res/todolog234.png")));
        descTextArea.setText(FileUtils.readResource(getClass().getResourceAsStream("/todo/res/aboutFile.txt")));
        dbsType.setItems(FXCollections.observableArrayList("file", "mem", "res"));
        dbsType.getSelectionModel().select(0);
        dbsUrl.setText(Todo.TODO_DIR + HSQLDBConfigurator.DBFILE);
        dirLabel.setText(Todo.TODO_DIR + HSQLDBConfigurator.DBFILE);
    }

    @FXML
    private void clickedLogo() {
        openWPage("");
    }

    @FXML
    private void donateLink() {
        openWPage("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=35G2LWFJ9JS56");
    }

    @FXML
    private void oHsqldoc() {
        openWPage("http://hsqldb.org/web/hsqlDocsFrame.html");
    }

    @FXML
    private void oJdbcdoc() {
        openWPage("http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/");
    }

    private void openWPage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException ex) {
            ex.printStackTrace();
        }
    }


    public ArrayList<TableColumn> getTcmns() {
        return tcmns;
    }

    private void updateDetail(TodoEntry todo) {
        dlblEntry.setText("TODO Entry:" + todo.getId());
        dlbCategory.setText("Category:" + todo.getCategory());
        dlbPriority.setText("Priority:" + todo.getPriority());
        messTextArea.setText(todo.getMessage());
    }

    class EditingCell extends TableCell<TodoEntry, String> {

        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) -> {
                if (!arg2) {
                    commitEdit(textField.getText());
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
}
