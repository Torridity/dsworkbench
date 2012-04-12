/*
 * DSWorkbenchSplashScreen.java
 *
 * Created on 30. Juni 2008, 14:12
 */
package de.tor.tribes.ui.windows;

import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.util.GlobalOptions;
import org.apache.log4j.Logger;
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.php.DatabaseInterface;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.ui.renderer.ProfileTreeNodeRenderer;
import de.tor.tribes.ui.wiz.FirstStartWizard;
import de.tor.tribes.util.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 * @author Jejkal
 */
public class DSWorkbenchSplashScreen extends javax.swing.JFrame implements DataHolderListener {

    private static Logger logger = Logger.getLogger("Launcher");
    private final DSWorkbenchSplashScreen self = this;
    private final SplashRepaintThread t;
    private static DSWorkbenchSplashScreen SINGLETON = null;

    public static synchronized DSWorkbenchSplashScreen getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSplashScreen();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchSplashScreen
     */
    DSWorkbenchSplashScreen() {
        initComponents();
        if (GlobalOptions.isMinimal()) {
            jLabel1.setIcon(new ImageIcon("./graphics/splash_mini.gif"));
        } else {
            jLabel1.setIcon(new ImageIcon("./graphics/splash.gif"));
        }

        setTitle("DS Workbench " + Constants.VERSION + Constants.VERSION_ADDITION);
        new Timer("StartupTimer", true).schedule(new HideSplashTask(), 1000);
        jProfileDialog.getContentPane().setBackground(Constants.DS_BACK_LIGHT);
        jProfileDialog.pack();
        jProfileDialog.setLocationRelativeTo(this);
        t = new SplashRepaintThread();
        t.start();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProfileDialog = new javax.swing.JDialog();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jStatusOutput = new javax.swing.JProgressBar();

        jProfileDialog.setTitle("Profile");
        jProfileDialog.setModal(true);
        jProfileDialog.setUndecorated(true);

        jScrollPane2.setViewportView(jTree1);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setText("Profil auswählen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectAccountEvent(evt);
            }
        });

        javax.swing.GroupLayout jProfileDialogLayout = new javax.swing.GroupLayout(jProfileDialog.getContentPane());
        jProfileDialog.getContentPane().setLayout(jProfileDialogLayout);
        jProfileDialogLayout.setHorizontalGroup(
            jProfileDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jProfileDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jProfileDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jProfileDialogLayout.setVerticalGroup(
            jProfileDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jProfileDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        jLabel1.setMaximumSize(new java.awt.Dimension(516, 250));
        jLabel1.setMinimumSize(new java.awt.Dimension(516, 250));
        jLabel1.setPreferredSize(new java.awt.Dimension(516, 250));
        getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);

        jStatusOutput.setIndeterminate(true);
        jStatusOutput.setMinimumSize(new java.awt.Dimension(10, 20));
        jStatusOutput.setPreferredSize(new java.awt.Dimension(146, 20));
        jStatusOutput.setString("Lade Einstellungen...");
        jStatusOutput.setStringPainted(true);
        getContentPane().add(jStatusOutput, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireSelectAccountEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectAccountEvent
        Object[] path = jTree1.getSelectionPath().getPath();
        UserProfile profile = null;
        try {
            profile = (UserProfile) ((DefaultMutableTreeNode) path[2]).getUserObject();
        } catch (Exception e) {
        }
        if (profile == null) {
            JOptionPaneHelper.showWarningBox(jProfileDialog, "Bitte eine Profil auswählen.", "Bitte wählen");
        } else {
            String server = profile.getServerId();
            GlobalOptions.setSelectedServer(server);
            GlobalOptions.setSelectedProfile(profile);
            GlobalOptions.addProperty("default.server", server);
            GlobalOptions.addProperty("selected.profile", Long.toString(profile.getProfileId()));
            GlobalOptions.addProperty("player." + server, Long.toString(profile.getProfileId()));
            jProfileDialog.setVisible(false);
        }
    }//GEN-LAST:event_fireSelectAccountEvent

    protected boolean hideSplash() {
        try {
            if (!new File(".").canWrite()) {
                JOptionPaneHelper.showErrorBox(self, "Fehler bei der Initialisierung.\nDas DS Workbench Verzeichnis ist für deinen Systembenutzer nicht beschreibbar.\nBitte installiere DS Workbench z.B. in dein Benutzerverzeichnis.", "Fehler");
                return false;
            }
            File f = new File("./servers");
            if (!f.exists() && !f.mkdir()) {
                JOptionPaneHelper.showErrorBox(self, "Fehler bei der Initialisierung.\nDas Serververzeichnis konnte nicht erstellt werden.", "Fehler");
                return false;
            }

            ProfileManager.getSingleton().loadProfiles();
            if (ProfileManager.getSingleton().getProfiles().length == 0) {
                logger.debug("Starting first start wizard");

                //first start wizard
                if (!new File("./hfsw").exists()) {
                    logger.debug(" - Initializing first start wizard");
                    Map result = new HashMap<String, String>();

                    try {
                        WizardPanelProvider provider = new FirstStartWizard();
                        Wizard wizard = provider.createWizard();
                        logger.debug(" - Showing wizard");
                        result = (Map) WizardDisplayer.showWizard(wizard);
                        logger.debug("Wizard finished with result " + result);
                    } catch (Throwable t) {
                        logger.error("Wizard exception", t);
                        result = null;
                    }
                    logger.debug(" - Wizard has finished");
                    if (result == null) {
                        logger.warn(" - Wizard returned no result. Startup will fail.");
                        JOptionPaneHelper.showWarningBox(self, "Du musst die grundlegenden Einstellungen zumindest einmalig durchführen,\n"
                                + "um DS Workbench verwenden zu können. Bitte starte DS Workbench neu.", "Abbruch");
                        return false;
                    } else {
                        logger.debug("Wizard result: " + result);
                    }
                    logger.debug("- First start wizard finished");
                    GlobalOptions.addProperty("proxySet", (String) result.get("proxySet"));
                    GlobalOptions.addProperty("proxyHost", (String) result.get("proxyHost"));
                    GlobalOptions.addProperty("proxyPort", (String) result.get("proxyPort"));
                    GlobalOptions.addProperty("proxyType", (String) result.get("proxyType"));
                    GlobalOptions.addProperty("proxyUser", (String) result.get("proxyUser"));
                    GlobalOptions.addProperty("proxyPassword", (String) result.get("proxyPassword"));
                    GlobalOptions.addProperty("account.name", (String) result.get("account.name"));
                    GlobalOptions.addProperty("account.password", (String) result.get("account.password"));
                    GlobalOptions.addProperty("default.server", (String) result.get("server"));
                    GlobalOptions.addProperty("player." + (String) result.get("server"), (String) result.get("tribe"));
                    logger.debug("Creating initial profile");
                    UserProfile p = UserProfile.create(GlobalOptions.getProperty("default.server"), GlobalOptions.getProperty("player." + GlobalOptions.getProperty("default.server")));
                    GlobalOptions.setSelectedProfile(p);
                    GlobalOptions.addProperty("selected.profile", Long.toString(p.getProfileId()));
                    logger.debug(" - Disabling first start wizard");
                    FileUtils.touch(new File("./hfsw"));
                    GlobalOptions.saveProperties();
                }
            }

            jStatusOutput.setString("Prüfe auf Updates");
            DSWorkbenchUpdateDialog.UPDATE_RESULT updateResult;
            DSWorkbenchUpdateDialog updateDialog = new DSWorkbenchUpdateDialog(this, true);
            if (updateDialog.getResult() == DSWorkbenchUpdateDialog.UPDATE_RESULT.READY) {
                updateDialog.setLocationRelativeTo(this);
                updateDialog.setVisible(true);
            }
            updateResult = updateDialog.getResult();
            switch (updateResult) {
                case CANCELED:
                    jStatusOutput.setString("Update abgebrochen");
                    break;
                case ERROR:
                    jStatusOutput.setString("Updates momentan nicht möglich");
                    break;
                case SUCCESS:
                    jStatusOutput.setString("Update erfolgreich. Neustart notwendig!");
                    JOptionPaneHelper.showInformationBox(this, "DS Workbench wurde erfolgreich aktualisiert.\n"
                            + "Bitte starte DS Workbench nun neu.", "Neustart notwendig");
                    return true;
                case NOT_NEEDED:
                    jStatusOutput.setString("Kein Update notwendig");
                    break;
                default:
                    jStatusOutput.setString("Unbekannter Fehler beim Update");
            }

            //load properties, cursors, skins, world decoration
            logger.debug("Adding startup listeners");
            DataHolder.getSingleton().addDataHolderListener(this);
            DataHolder.getSingleton().addDataHolderListener(DSWorkbenchSettingsDialog.getSingleton());
            GlobalOptions.addDataHolderListener(this);
        } catch (Exception e) {
            logger.error("Failed to initialize global options", e);
            JOptionPaneHelper.showErrorBox(self, "Fehler bei der Initialisierung.\nMöglicherweise ist deine DS Workbench Installation defekt.", "Fehler");
            return false;
        }

        logger.debug("Starting profile selection");
        boolean settingsRestored = false;
        try {
            //open profile selection
            if (ProfileManager.getSingleton().getProfiles().length == 0) {
                logger.debug("No profile exists, SettingsDialog will handle this");
                //no profile found...this is handles by the settings validation
            } else if (ProfileManager.getSingleton().getProfiles().length == 1) {
                logger.debug("One profile exists. Using it...");
                //only one single profile was found, use it
                UserProfile profile = ProfileManager.getSingleton().getProfiles()[0];
                String server = profile.getServerId();
                GlobalOptions.setSelectedServer(server);
                GlobalOptions.setSelectedProfile(profile);
                GlobalOptions.addProperty("default.server", server);
                GlobalOptions.addProperty("selected.profile", Long.toString(profile.getProfileId()));
            } else {
                logger.debug("More than one profiles exist. Showing selection dialog");
                File f = new File("./servers");
                List<String> servers = new LinkedList<String>();
                for (File server : f.listFiles()) {
                    servers.add(server.getName());
                }
                //sort server names
                Collections.sort(servers, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        if (o1.length() < o2.length()) {
                            return -1;
                        } else if (o1.length() > o2.length()) {
                            return 1;
                        }
                        return o1.compareTo(o2);
                    }
                });
                List<Object> path = new LinkedList<Object>();
                DefaultMutableTreeNode root = new DefaultMutableTreeNode("Profile");
                long selectedProfile = -1;
                try {
                    selectedProfile = Long.parseLong(GlobalOptions.getProperty("selected.profile"));
                } catch (Exception e) {
                }
                path.add(root);
                for (String server : servers) {
                    DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(server);
                    boolean profileAdded = false;
                    for (UserProfile profile : ProfileManager.getSingleton().getProfiles(server)) {
                        DefaultMutableTreeNode profileNode = new DefaultMutableTreeNode(profile);
                        if (profile.getProfileId() == selectedProfile) {
                            path.add(serverNode);
                            path.add(profileNode);
                        }
                        serverNode.add(profileNode);
                        profileAdded = true;
                    }
                    if (profileAdded) {
                        root.add(serverNode);
                    }
                }

                jTree1.setModel(new DefaultTreeModel(root));
                jTree1.setSelectionPath(new TreePath(path.toArray()));
                jTree1.scrollPathToVisible(new TreePath(path.toArray()));
                jTree1.setCellRenderer(new ProfileTreeNodeRenderer());
                jProfileDialog.setVisible(true);
            }
            logger.debug("Profile selection finished");
            //check settings
            DSWorkbenchSettingsDialog.getSingleton().restoreProperties();
            settingsRestored = true;
            if (!DSWorkbenchSettingsDialog.getSingleton().checkSettings()) {
                logger.debug("Settings check in settings dialog failed");
                logger.info("Reading user settings returned error(s)");
                DSWorkbenchSettingsDialog.getSingleton().setBlocking(true);
                DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
            }
        } catch (Exception e) {
            logger.warn("Failed to open profile manager", e);
        }

        if (!settingsRestored) {
            DSWorkbenchSettingsDialog.getSingleton().restoreProperties();
        }

        // <editor-fold defaultstate="collapsed" desc=" Check for data updates ">
        logger.debug("Checking for application updates");
        boolean checkForUpdates = Boolean.parseBoolean(GlobalOptions.getProperty("check.updates.on.startup"));

        if (checkForUpdates && !GlobalOptions.isOfflineMode()) {
            String selectedServer = GlobalOptions.getProperty("default.server");
            String name = GlobalOptions.getProperty("account.name");
            String password = GlobalOptions.getProperty("account.password");
            if (DatabaseInterface.checkUser(name, password) != DatabaseInterface.ID_SUCCESS) {
                JOptionPaneHelper.showErrorBox(this, "Die Accountvalidierung ist fehlgeschlagen.\n"
                        + "Bitte überprüfe deine Account- und Netzwerkeinstellungen und versuches es erneut.",
                        "Fehler");
                checkForUpdates = false;
            } else {
                long serverDataVersion = DatabaseInterface.getServerDataVersion(selectedServer);
                long userDataVersion = DatabaseInterface.getUserDataVersion(name, selectedServer);
                logger.debug("User data version is " + userDataVersion);
                logger.debug("Server data version is " + serverDataVersion);
                if (userDataVersion == serverDataVersion) {
                    logger.debug("Skip downloading updates");
                    checkForUpdates = false;
                }
            }
        }

        try {
            if (!DataHolder.getSingleton().loadData(checkForUpdates)) {
                throw new Exception("loadData() returned 'false'. See log for more details.");
            }
        } catch (Exception e) {
            logger.error("Failed to load server data", e);
            return false;
        }
        // </editor-fold>
        try {
            logger.debug("Checking for plugin updates");
            PluginManager.getSingleton().checkForUpdates();

            logger.debug("Initializing application window");
            DSWorkbenchMainFrame.getSingleton().init();
            logger.info("Showing application window");


            DSWorkbenchMainFrame.getSingleton().setVisible(true);
            try {
                ReportServer.getSingleton().start(GlobalOptions.getProperties().getInt("report.server.port", 8080));
            } catch (IOException ioe) {
                logger.error("Failed to start report server", ioe);
            }
            t.stopRunning();
            setVisible(false);
            GlobalOptions.removeDataHolderListener(this);
            boolean informOnUpdate = true;

            try {
                String val = GlobalOptions.getProperty("inform.on.updates");
                if (val != null) {
                    informOnUpdate = Boolean.parseBoolean(val);
                }
            } catch (Exception e) {
                //value not found, inform by default
            }
            if (informOnUpdate) {
                //check version
                double version = DatabaseInterface.getCurrentVersion();

                if (version > 0 && version > Constants.VERSION) {
                    NotifierFrame.doNotification("Eine neue Version (" + version + ") von DS Workbench ist verfügbar.\n" + "Klicke auf das Update Icon um \'http://www.dsworkbench.de\' im Browser zu öffnen.", NotifierFrame.NOTIFY_UPDATE);
                }
            }
            return true;
        } catch (Throwable th) {
            logger.fatal("Fatal error while running DS Workbench", th);
            JOptionPaneHelper.showErrorBox(self, "Ein schwerwiegender Fehler ist aufgetreten.\nMöglicherweise ist deine DS Workbench Installation defekt. Bitte kontaktiere den Entwickler.", "Fehler");
            return false;
        }
    }

    public static class ExceptionHandler
            implements Thread.UncaughtExceptionHandler {

        public void handle(Throwable thrown) {
            // for EDT exceptions
            handleException(Thread.currentThread().getName(), thrown);
        }

        public void uncaughtException(Thread thread, Throwable thrown) {
            // for other uncaught exceptions
            handleException(thread.getName(), thrown);
        }

        protected void handleException(String tname, Throwable thrown) {
            logger.warn("Unhandled exception in thread '" + tname + "'", thrown);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Locale.setDefault(Locale.GERMAN);
        int mode = -1;
        int minimal = 0;
        if (args != null) {
            for (String arg : args) {
                if (arg.equals("-d") || arg.equals("--debug")) {
                    //debug mode
                    mode = 1;
                    SystrayHelper.showInfoMessage("Running in debug mode");
                } else if (arg.equals("-i") || arg.equals("--info")) {
                    //info mode
                    mode = 0;
                    SystrayHelper.showInfoMessage("Running in info mode");
                } else if (arg.equals("-m")) {
                    minimal = 1;
                }
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
        /*
         * new Thread.UncaughtExceptionHandler() { @Override public void uncaughtException(Thread t, Throwable e) { logger.error("Uncaught
         * exception in thread " + t, e); } });
         */
        Appender a = null;

        if (!Constants.DEBUG) {
            a = new org.apache.log4j.RollingFileAppender();
            ((org.apache.log4j.RollingFileAppender) a).setMaxFileSize("1MB");
        } else {
            SystrayHelper.installSystrayIcon();
            SystrayHelper.showInfoMessage("Running in developer mode");
            a = new org.apache.log4j.ConsoleAppender();
            ((org.apache.log4j.ConsoleAppender) a).setWriter(new PrintWriter(System.out));
        }
        a.setLayout(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n"));
        try {
            if (!Constants.DEBUG) {
                ((org.apache.log4j.RollingFileAppender) a).setFile("./log/dsworkbench.log", true, true, 1024);
            }
            switch (mode) {
                case 0: {
                    Logger.getRootLogger().setLevel(Level.INFO);
                    break;
                }
                case 1: {
                    Logger.getRootLogger().setLevel(Level.DEBUG);
                    break;
                }
                default: {
                    Logger.getRootLogger().setLevel(Level.ERROR);
                    break;
                }
            }

            Logger.getRootLogger().addAppender(a);
            Logger.getLogger("de.tor").addAppender(a);
            Logger.getLogger("dswb").addAppender(a);
            GlobalOptions.setMinimalVersion(minimal == 1);
        } catch (IOException ioe) {
            logger.error("Failed to initialize logging", ioe);
        }

        try {
            GlobalOptions.initialize();
            String lnf = GlobalOptions.getProperty("look.and.feel");

            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
                //no nimbus for mac users
                lnf = UIManager.getSystemLookAndFeelClassName();
            }
            if (lnf == null) {
                //lnf = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } else {
                UIManager.setLookAndFeel(lnf);
            }
        } catch (Exception e) {
            logger.error("Failed to setup LnF", e);
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    DSWorkbenchSplashScreen.getSingleton().setLocationRelativeTo(null);
                    DSWorkbenchSplashScreen.getSingleton().setVisible(true);
                } catch (Exception e) {
                    logger.error("Fatal application error", e);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JDialog jProfileDialog;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JProgressBar jStatusOutput;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireDataHolderEvent(String pText) {
        jStatusOutput.setString(pText);
    }

    public void updateStatus() {
        jStatusOutput.repaint();
    }

    @Override
    public void fireDataLoadedEvent(boolean pSuccess) {
        if (pSuccess) {
            jStatusOutput.setString("Daten geladen");
        } else {
            jStatusOutput.setString("Download fehlgeschlagen");
        }
    }
}

class HideSplashTask extends TimerTask {

    public HideSplashTask() {
    }

    public void run() {
        try {
            if (!DSWorkbenchSplashScreen.getSingleton().hideSplash()) {
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}

class SplashRepaintThread extends Thread {

    private boolean running = true;

    public SplashRepaintThread() {
        setName("SplashHideThread");
        setDaemon(true);
    }

    public void run() {
        while (running) {
            DSWorkbenchSplashScreen.getSingleton().updateStatus();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void stopRunning() {
        running = false;
    }
}
