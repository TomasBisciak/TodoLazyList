/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.jnativehook.GlobalScreen;

/**
 *
 * @author tomas
 */
public class Tray {

    private Stage stage;
    private Application application;

    public Tray(Stage stage, Application application) {
        this.stage = stage;
        this.application = application;
        hookTray();

    }

    private void hookTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
        } else {
            try {
                Todo.icon = new TrayIcon(ImageIO.read(Todo.class.getResourceAsStream("/todo/res/logoTray.png")), "TODO application",
                        createPopupMenu());
                Todo.icon.addActionListener((ActionEvent e) -> {
                    Platform.runLater(() -> {
                        stage.setIconified(false);
                    });
                    //some popup test
                });
                SystemTray.getSystemTray().add(Todo.icon);
                //Thread.sleep(3000);
                Todo.icon.displayMessage("TODO", "Listener enabled",
                        TrayIcon.MessageType.INFO);

            } catch (AWTException | IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    public PopupMenu createPopupMenu() throws HeadlessException {
        PopupMenu menu = new PopupMenu();

        MenuItem exit = new MenuItem("Exit");
        MenuItem showWindow = new MenuItem("Show window");
        showWindow.addActionListener((ActionEvent e) -> {
            Platform.runLater(() -> {
                try {
                    application.start(stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        });
        exit.addActionListener((ActionEvent e) -> {
            GlobalScreen.unregisterNativeHook();
            System.exit(0);
        });
        menu.add(showWindow);
        menu.add(exit);

        return menu;
    }

}
