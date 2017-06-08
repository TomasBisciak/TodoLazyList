/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import com.sun.glass.events.KeyEvent;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import todo.hsqldb.DbUtils;
import todo.hsqldb.HSQLDBConfigurator;
import todo.hsqldb.HSQLDBConnector;

/**
 * 
 *
 * @author tomas bisciak
 */
public class Todo extends Application {

    //to-do area here
    //TODO  change ommit cdgDat and use Global configurator instead..
    //TODO finish custom connection methods
    //TODO restart pc to register nativehook and test
    private static StringBuilder todoStringBuilder = new StringBuilder();
    private static StringBuilder unicodeStringBuilder = new StringBuilder();
    private static char[] charBuffer = new char[0x6];
    private static char[] charTemp = {'/', '/', 'T', 'O', 'D', 'O'};

    private static Robot robot;
    private static boolean isScan;
    private static boolean isBuffer;
    private static boolean isRemoving;

    //dirs
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String TODO_DIR = USER_HOME + File.separator + "TODO";
    public static final String TODO_CONFIG_FILE = TODO_DIR + File.separator + "todocfg.cfg";

    private static final int PORT = 17754;
    private static ServerSocket socket;

    public static DataModel datMod;
    public static String[] cfgDat;

    private static Pattern pattern;
    private static Matcher matcher;

    private static char current;

    public static TrayIcon icon;

    public static HSQLDBConfigurator globalConfigurator = HSQLDBConfigurator.getInstance();

    public static HSQLDBConnector connector;

    private static Stage stage;

    public static boolean isCustomConnection;
    public static String[] customConnectionData;

    private Tray tray;
    private boolean isTray = true;

    public static FXMLMainViewController fxc;

    static {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        fxc = new FXMLMainViewController();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "FXMLMainView.fxml"
                )
        );
        loader.setController(fxc);
        Parent root = (Parent) loader.load();
        Scene scene = new Scene(root);
        stage = primaryStage;
        stage.getIcons().add(new javafx.scene.image.Image(Todo.class.getResourceAsStream("/todo/res/logobar.png")));
        stage.setWidth(1166);
        stage.setHeight(676);
        stage.setTitle("//TODO LazyList//");
        stage.setScene(scene);
        stage.show();

        if (isTray) {
            tray = new Tray(stage, this);
            isTray = false;
        }

        Platform.setImplicitExit(false);

        datMod.updateTable(fxc.tableView, fxc.getTcmns());

    }

    public static void main(String[] args) {

        try {
            //Bind to localhost adapter with a zero connection queue 
            socket = new ServerSocket(PORT, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
        } catch (BindException e) {
            try {
                Platform.runLater(()->{
                       MessageBox.show(null,
                        "Another instance of this application is already running.",
                        "Already running",
                        MessageBox.ICON_INFORMATION);
                });
                Thread.sleep(3000);
                System.exit(1);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            try {
                  Platform.runLater(()->{
                MessageBox.show(null,
                        "Error has occured",
                        "Exception",
                        MessageBox.ICON_ERROR);
                 });
                Thread.sleep(3000);
                e.printStackTrace();
                System.exit(2);
            } catch (InterruptedException ex) {
               ex.printStackTrace();
            }
        }

        datMod = new DataModel();
        fileManagerCheck();
        globalConfigurator.setConfiguration(datMod.readCfg());

        if (globalConfigurator.isFtr()) {
            connector = new HSQLDBConnector(isCustomConnection);
            try {
                connector.statement.executeUpdate("CREATE TABLE todos (todoId INTEGER IDENTITY,category VARCHAR(255) NOT NULL,"
                        + "message VARCHAR(255) NOT NULL,priority VARCHAR(255) NOT NULL);");

                datMod.overrideCfg(TODO_DIR, "file", "TODO", "TODODB", "false");
                connector.closeConnector();

            } catch (SQLException ex) {
                ex.printStackTrace();
                connector.closeConnector();
            }
            Platform.runLater(() -> {
                MessageBox.show(null,
                        "Read instructions!They help you get started.",
                        "Information",
                        MessageBox.ICON_INFORMATION | MessageBox.OK);

            });
        }

        //check if database uses custom conenction checks also for table if its included in there.
        if (Files.exists(Paths.get(FXMLMainViewController.TODO_CUSTOM_CONNECTION_CONFIG))) {
            try {
                DbUtils.checkCustomConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Thread t = new Thread(() -> {
            clearBuff();
            startListening();
        });
        t.setDaemon(true);
        t.start();

        launch(args);

    }

    private static void fileManagerCheck() {
        if (!Files.exists(Paths.get(TODO_DIR))) {
            try {
                Files.createDirectory(Paths.get(TODO_DIR));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (!Files.exists(Paths.get(TODO_CONFIG_FILE))) {
            try {
                Files.createFile(Paths.get(TODO_CONFIG_FILE));
                //overrride with default 
                datMod.overrideCfg(TODO_DIR, "file", "TODO", "TODODB", "true");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void startListening() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.out.println(ex.getMessage());

            System.exit(1);
        }

        GlobalScreen.getInstance().addNativeKeyListener(new GlobalKeyListener());
        System.out.println("Hook state: " + GlobalScreen.isNativeHookRegistered());

    }

    private static String buffGetString() {
        String s = "";
        for (char c : charBuffer) {
            s += c;
        }
        return s;
    }

    private static boolean isInit() {
        return buffGetString().equals(TodoEntry.PREFIX);
    }

    private static void clearBuff() {
        for (int i = 0; i < charBuffer.length; i++) {
            charBuffer[i] = '\u10BF';
        }
    }

    private static void removeIntext(int a) {
        new Thread(() -> {
            isRemoving = true;
            for (int i = 0; i < a; i++) {
                robot.keyPress(KeyEvent.VK_BACKSPACE);
                robot.keyRelease(KeyEvent.VK_BACKSPACE);
            }
            isRemoving = false;
        }).start();

    }

    private static boolean isFollowing() {
        boolean isFollowing = true;
        for (int i = 0; i < charTemp.length; i++) {
            if (charBuffer[i] == (charTemp[i]) || charBuffer[i] == 4287) {
                isFollowing = true;
            } else {
                return false;
            }
        }
        return isFollowing;
    }

    private static void submitEntry(String entry) {

        TodoEntry todoEntry = processEntry(entry);
        if (todoEntry != null) {// check for validity of message .. length etc
            if (todoEntry.getMessage().length() >= TodoEntry.MAX_SIZE) {
                if (SystemTray.isSupported()) {
                    icon.displayMessage("TODO", "Todo failed-MAX size reached", TrayIcon.MessageType.ERROR);
                }
                return;
            }
            if (todoEntry.getMessage().length() <= TodoEntry.MIN_SIZE) {
                if (SystemTray.isSupported()) {
                    icon.displayMessage("TODO", "Todo failed-MIN size not exceeded", TrayIcon.MessageType.ERROR);
                }
                return;
            }

            DbUtils.addTodoIntoDb(todoEntry);
            datMod.updateTable(fxc.tableView, fxc.getTcmns());
            if (SystemTray.isSupported()) {
                icon.displayMessage("TODO", "Todo submitted", TrayIcon.MessageType.INFO);
            }

        } else {
            if (SystemTray.isSupported()) {
                icon.displayMessage("TODO", "Todo null", TrayIcon.MessageType.ERROR);
            }
        }
    }

    private static TodoEntry processEntry(String entry) {
        String[] parsed = new String[3];

        String[] regexes = {"^(\\s*/\\w+\\s)",
            "(s*/(LOW|MEDIUM|HIGH|low|medium|high)\\s*)$"};

        for (int i = 0; i < regexes.length; i++) {
            pattern = Pattern.compile(regexes[i], Pattern.DOTALL);
            matcher = pattern.matcher(entry);
            if (matcher.find()) {

                parsed[i] = matcher.group().replace("/", " ").trim();
                entry = entry.replaceAll(regexes[i], "");

            }
        }

        parsed[2] = entry;
        return new TodoEntry(parsed[0], parsed[2], parsed[1]);
    }

    private static void closeScan() {
        todoStringBuilder.delete(0, todoStringBuilder.length());
        clearBuff();
        isScan = false;
    }

    private static class GlobalKeyListener implements NativeKeyListener {

        int c = 0;

        public GlobalKeyListener() {

        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent nke) {//Might fuck up diff key locals , shoud be used key typed instead , might patch later on, works on ENG  tho...
            //TODO HAVE TO IGNORE SOME CHARACTERS WHEN SCANNER ENABLED AND SCANNING FOR MESASGE 

            //normal initialization
            current = nke.getKeyCode() == TodoEntry.SLASH ? '/' : (char) nke.getKeyCode();
            if (nke.paramString().contains("NumPad")) {
                current = nke.paramString().charAt(nke.paramString().indexOf("NumPad ") + 7);
            }
            if (isScan) {//scanning phase
                if (current == '\u0008') {
                    todoStringBuilder.deleteCharAt(todoStringBuilder.length() - 1);

                } else {
                    todoStringBuilder.append(current);
                }

                if (todoStringBuilder.toString().endsWith(TodoEntry.POSTFIX)) {//successfull entry
                    submitEntry(todoStringBuilder.toString().substring(0, todoStringBuilder.length() - 2));
                    int backupby = todoStringBuilder.toString().length() + 6;
                    removeIntext(backupby);
                    closeScan();
                }

            } else {

                if (isBuffer) {

                    if (current == '\u0008') {
                        return;
                    }

                    ///fill buffer
                    charBuffer[c] = current;
                    c++;

                    if (isFollowing()) {
                        if (c == charBuffer.length) {//if c is at the end of ... check if shoud start scanning
                            if (isInit()) {//check scanning 
                                isScan = true;
                                isBuffer = false;

                            } else {
                                isBuffer = false;
                            }
                            c = 0;//if shoud not scan get buffer iterator at start
                        }

                    } else {
                        isBuffer = false;
                        clearBuff();
                    }
                } else {

                    if (current == '/') {//check if to start buffer
                        isBuffer = true;
                        charBuffer[0] = '/';
                        c = 1;
                    }

                }
            }

        }

        @Override
        public void nativeKeyPressed(NativeKeyEvent nke) {
        }

        @Override
        public void nativeKeyTyped(NativeKeyEvent nke) {
        }

    }

}
