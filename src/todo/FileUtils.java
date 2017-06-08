/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Scanner;



//TODO modify to NIO
//TODO make thread safe
public final class FileUtils {

    //write to any file //cannot be resource have to be located in the pc
    public static void writeToFile(String file, String s, boolean append) {
        try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file, append)))) {
            printWriter.print(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
          
    }

    //reads from any file
    public static String readFromFile(String file) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            int current;
            while ((current = bufferedReader.read()) != -1) {
                sb.append((char) current);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    //readingResources
    public static String readResource(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try (Scanner scan = new Scanner(stream)) {

            while (scan.hasNextLine()) {
                sb.append(scan.nextLine()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    
    
    public static void openWebpage(URI uri){
        //if desktop mode is supported get desktop
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if ((desktop != null) && (desktop.isSupported(Desktop.Action.BROWSE))) {
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

  //furthermore use NIO FILES. class to do additional operations
}
