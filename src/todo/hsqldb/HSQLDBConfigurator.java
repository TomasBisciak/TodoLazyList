/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo.hsqldb;

import java.io.File;

/**
 * Singleton class for configuration
 *
 * @author tomas
 */
public class HSQLDBConfigurator {

    public static final int SECURITY_LEVEL_LOW = 0;
    public static final int SECURITY_LEVEL_MEDIUM = 1;

    public static final String DBFILE = File.separator + "dbtodo";

    
    private String installationDir;
    private String databaseDir;
    private String password;
    private String username;
    private String type;

    private static HSQLDBConfigurator instance = new HSQLDBConfigurator();
    private boolean ftr;

    private HSQLDBConfigurator() {
    }

    public static HSQLDBConfigurator getInstance() {
        return instance;
    }
    
    
       public void setConfiguration(String[] configuration) {
        databaseDir = configuration[0];
        type = configuration[1];
        username = configuration[2];
        password = configuration[3];
        ftr=Boolean.valueOf(configuration[4]);
    }
    

    //getters setters
    public String getDatabaseDir() {
        return databaseDir;
    }

    public void setDatabaseDir(String databaseDir) {
        this.databaseDir = databaseDir;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isFtr() {
        return ftr;
    }

    public void setFtr(boolean ftr) {
        this.ftr = ftr;
    }

    public String getInstallationDir() {
        return installationDir;
    }

    public void setInstallationDir(String installationDir) {
        this.installationDir = installationDir;
    }

}
