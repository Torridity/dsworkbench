/*
 * TribesPlannerStartFrame.java
 *
 * Created on 9. Juni 2008, 15:54
 */
package de.tor.tribes.ui;

import de.tor.tribes.db.DatabaseAdapter;
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.ServerList;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Color;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author  Jejkal
 */
public class DSWorkbenchSettingsDialog extends javax.swing.JDialog implements DataHolderListener {

    private static Logger logger = Logger.getLogger(DSWorkbenchSettingsDialog.class);
    private static DSWorkbenchSettingsDialog SETTINGS_DIALOG = null;
    boolean updating = false;
    boolean gotServerList = false;
    private DSWorkbenchMainFrame mMainFrame = null;

    public static DSWorkbenchSettingsDialog getGlobalSettingsFrame() {
        if (SETTINGS_DIALOG != null) {
            return SETTINGS_DIALOG;
        }
        SETTINGS_DIALOG = new DSWorkbenchSettingsDialog();
        return SETTINGS_DIALOG;
    }

    /** Creates new form TribesPlannerStartFrame */
    DSWorkbenchSettingsDialog() {
        initComponents();

        jCreateAccountDialog.pack();
        getContentPane().setBackground(GlobalOptions.DS_BACK);
        jCreateAccountDialog.getContentPane().setBackground(GlobalOptions.DS_BACK);
        setAlwaysOnTop(true);
        // jControlPanel.setupPanel(this, true, false);

        String interval = GlobalOptions.getProperty("auto.update.interval");
        if (interval != null) {
            try {
                jUpdateIntervalBox.setSelectedIndex(Integer.parseInt(interval));
            } catch (Exception e) {
                jUpdateIntervalBox.setSelectedIndex(0);
                GlobalOptions.addProperty("auto.update.interval", "7");
            }
        } else {
            jUpdateIntervalBox.setSelectedIndex(0);
            GlobalOptions.addProperty("auto.update.interval", "7");
        }


        // <editor-fold defaultstate="collapsed" desc="Skin Setup">
        DefaultComboBoxModel model = new DefaultComboBoxModel(GlobalOptions.getAvailableSkins());
        jGraphicPacks.setModel(model);
        String skin = GlobalOptions.getProperty("default.skin");
        if (skin != null) {
            if (model.getIndexOf(skin) != -1) {
                jGraphicPacks.setSelectedItem(skin);
            } else {
                jGraphicPacks.setSelectedItem("default");
            }
        } else {
            jGraphicPacks.setSelectedItem("default");
        }
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Network Setup">
        if (GlobalOptions.getProperty("proxySet") != null) {
            System.setProperty("proxySet", GlobalOptions.getProperty("proxySet"));
            String proxySet = GlobalOptions.getProperty("proxySet");
            boolean useProxy = false;
            if (proxySet != null) {
                try {
                    useProxy = Boolean.parseBoolean(proxySet);
                } catch (Exception e) {
                }
            }

            jDirectConnectOption.setSelected(!useProxy);
            jProxyConnectOption.setSelected(useProxy);
        }

        if (GlobalOptions.getProperty("proxyHost") != null) {
            System.setProperty("proxyHost", GlobalOptions.getProperty("proxyHost"));
            jProxyHost.setText(GlobalOptions.getProperty("proxyHost"));
        }
        if (GlobalOptions.getProperty("proxyPort") != null) {
            System.setProperty("proxyPort", GlobalOptions.getProperty("proxyPort"));
            jProxyPort.setText(GlobalOptions.getProperty("proxyPort"));
        }

        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Account Setup">
        String name = GlobalOptions.getProperty("account.name");
        String password = GlobalOptions.getProperty("account.password");

        if ((name != null) && (password != null)) {
            jAccountName.setText(name);
            jAccountPassword.setText(password);
        } else if (name != null) {
            jAccountName.setText(name);
        }
        //</editor-fold>

        try {
            if (Boolean.parseBoolean(GlobalOptions.getProperty("minimap.showcontinents"))) {
                jContinentsOnMinimap.setSelected(true);
            }
        } catch (Exception e) {
        }
        try {
            if (Boolean.parseBoolean(GlobalOptions.getProperty("draw.distance"))) {
                jShowDistanceBox.setSelected(true);
            }
        } catch (Exception e) {
        }
    }

    public void setMainFrame(DSWorkbenchMainFrame pMainFrame) {
        mMainFrame = pMainFrame;
    }

    protected boolean checkSettings() {
        logger.debug("Checking settings");

        /*************************
         ***Check Network
         *************************/
        if (!updateServerList(false)) {
            //remote update failed and no local servers found
            String message = "Serverliste konnte nicht geladen werden.\n";
            message += "Mögliche Ursachen sind fehlerhafte Netzwerkeinstellungen oder keine Verbindung zum Internet.\n";
            message += "Da noch kein Datenabgleich mit dem Server stattgefunden hat, korrigiere bitte deine Netzwerkeinstellungen um diesen einmalig durchzuführen.";
            JOptionPane.showMessageDialog(this, message, "Warnung", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (GlobalOptions.isOfflineMode()) {
            //remote update failed but local servers found
            String message = "Serverliste konnte nicht geladen werden.\n";
            message += "Mögliche Ursachen sind fehlerhafte Netzwerkeinstellungen oder keine Verbindung zum Internet.\n";
            message += "Da bereits Serverdaten auf deiner Festplatte existieren, wechselt DS Workbench in den Offline-Modus.\n";
            message += "Um Online-Funktionen zu nutzen korrigieren bitte später deine Netzwerkeinstellungen oder verbinde dich mit dem Internet.";
            JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
            GlobalOptions.setOfflineMode(false);
        }

        // <editor-fold defaultstate="collapsed" desc="Check Account (only if online-mode)">
        if (!GlobalOptions.isOfflineMode()) {
            String name = GlobalOptions.getProperty("account.name");
            String password = GlobalOptions.getProperty("account.password");
            int result = DatabaseAdapter.checkUser(name, password);
            UIManager.put("OptionPane.noButtonText", "Fortfahren");
            UIManager.put("OptionPane.yesButtonText", "Einstellungen überprüfen");
            if ((result == DatabaseAdapter.ID_USER_NOT_EXIST) || (result == DatabaseAdapter.ID_WRONG_PASSWORD)) {
                logger.info("Account check failed (account error)");
                String message = "Die Accountvalidierung ist fehlgeschlagen.\n";
                message += "Wenn du noch nicht registriert bist tu dies bitte über den entsprechenden Button.\n";
                message += "Falls du bereits registriert bist, überprüfe bitte deinen Benutzernamen und dein Passwort.\n";
                message += "Solange die Accountvalidierung nicht durchgeführt wurde, ist es dir nicht möglich sein, Serverdaten zu aktualisieren.";
                if (JOptionPane.showConfirmDialog(this, message, "Warnung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                    //NO_OPTION selected, so check settings again
                    UIManager.put("OptionPane.noButtonText", "No");
                    UIManager.put("OptionPane.yesButtonText", "Yes");
                    return false;
                }
            } else if (result == DatabaseAdapter.ID_CONNECTION_FAILED) {
                logger.info("Account check failed (connection error)");
                String message = "Die Accountvalidierung ist fehlgeschlagen.\n";
                message += "Bitte überprüfe deine Netzwerkeinstellungen und ob du mit dem Internet verbunden bist.\n";
                message += "Solange die Accountvalidierung nicht erfolgreich war wird es dir nicht möglich sein, Serverdaten zu aktualisieren.";
                if (JOptionPane.showConfirmDialog(this, message, "Warnung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                    //NO_OPTION selected, so check settings again
                    UIManager.put("OptionPane.noButtonText", "No");
                    UIManager.put("OptionPane.yesButtonText", "Yes");
                    return false;
                }
            }
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.yesButtonText", "Yes");
        }
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Check Server/Player settings">
        if (!checkPlayerSettings()) {
            String message = "Bitte überprüfe die Spieler-/Servereinstellungen und schließe die Einstellungen mit OK.\n";
            message += "Möglicherweise wurde noch kein Server oder kein Spieler ausgewählt.\n";
            message += "Diese Einstellungen sind für einen korrekten Ablauf zwingend notwendig.";
            UIManager.put("OptionPane.noButtonText", "Beenden");
            UIManager.put("OptionPane.yesButtonText", "Korrigieren");
            if (JOptionPane.showConfirmDialog(this, message, "Warnung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                logger.error("Player/Server settings incorrect. User requested application to terminate");
                System.exit(1);
            }
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.yesButtonText", "Yes");
            return false;
        } else {
            return true;
        }
    //</editor-fold>
    }

    private boolean checkPlayerSettings() {
        /*************************
         ***Check server and DS user
         *************************/
        String defaultServer = GlobalOptions.getProperty("default.server");
        String serverUser = GlobalOptions.getProperty("player." + defaultServer);

        if (defaultServer == null) {
            logger.warn("Default server is not set");
            String selection = (String) jServerList.getSelectedItem();
            if ((selection != null) && (selection.length() > 1)) {
                GlobalOptions.setSelectedServer(selection);
                GlobalOptions.addProperty("default.server", (String) selection);
            } else {
                return false;
            }
        }
        if (serverUser == null) {
            logger.warn("Default user is not set");
            String selection = (String) jTribeNames.getSelectedItem();

            if ((selection != null) && (!selection.equals("Bitte auswählen")) && (selection.length() > 1)) {
                GlobalOptions.getProperty("player." + defaultServer);
            } else {
                return false;
            }
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionTypeGroup = new javax.swing.ButtonGroup();
        jCreateAccountDialog = new javax.swing.JDialog();
        jLabel9 = new javax.swing.JLabel();
        jRegistrationAccountName = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jRegistrationPassword = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jRegistrationPassword2 = new javax.swing.JPasswordField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jLoginPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jAccountPassword = new javax.swing.JPasswordField();
        jAccountName = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jPlayerServerSettings = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jServerList = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jTribeNames = new javax.swing.JComboBox();
        jSelectServerButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jStatusArea = new javax.swing.JTextArea();
        jDownloadDataButton = new javax.swing.JButton();
        jGeneralSettings = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jGraphicPacks = new javax.swing.JComboBox();
        jButton4 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jUpdateIntervalBox = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jContinentsOnMinimap = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jShowDistanceBox = new javax.swing.JCheckBox();
        jNetworkSettings = new javax.swing.JPanel();
        jDirectConnectOption = new javax.swing.JRadioButton();
        jProxyConnectOption = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jProxyHost = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jProxyPort = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jCreateAccountButton = new javax.swing.JButton();

        jCreateAccountDialog.setTitle("Registrierung");
        jCreateAccountDialog.setAlwaysOnTop(true);
        jCreateAccountDialog.setBackground(new java.awt.Color(239, 235, 223));
        jCreateAccountDialog.setModal(true);

        jLabel9.setText("Name");

        jRegistrationAccountName.setMaximumSize(new java.awt.Dimension(200, 20));
        jRegistrationAccountName.setMinimumSize(new java.awt.Dimension(200, 20));
        jRegistrationAccountName.setPreferredSize(new java.awt.Dimension(200, 20));

        jLabel10.setText("Passwort");

        jRegistrationPassword.setMaximumSize(new java.awt.Dimension(200, 20));
        jRegistrationPassword.setMinimumSize(new java.awt.Dimension(200, 20));
        jRegistrationPassword.setPreferredSize(new java.awt.Dimension(200, 20));

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setText("Registrieren");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRegisterEvent(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setText("Abbrechen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelRegistrationEvent(evt);
            }
        });

        jLabel12.setText("Passwort wiederholen");

        javax.swing.GroupLayout jCreateAccountDialogLayout = new javax.swing.GroupLayout(jCreateAccountDialog.getContentPane());
        jCreateAccountDialog.getContentPane().setLayout(jCreateAccountDialogLayout);
        jCreateAccountDialogLayout.setHorizontalGroup(
            jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCreateAccountDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRegistrationPassword2, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addGroup(jCreateAccountDialogLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))
                    .addComponent(jRegistrationAccountName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRegistrationPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jCreateAccountDialogLayout.setVerticalGroup(
            jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCreateAccountDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jRegistrationAccountName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jRegistrationPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jRegistrationPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle("Einstellungen");
        setAlwaysOnTop(true);
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fireClosingEvent(evt);
            }
        });

        jTabbedPane1.setBackground(new java.awt.Color(239, 235, 223));

        jLoginPanel.setBackground(new java.awt.Color(239, 235, 223));

        jLabel6.setText("Name");

        jLabel7.setText("Passwort");

        jAccountPassword.setMaximumSize(new java.awt.Dimension(200, 20));
        jAccountPassword.setMinimumSize(new java.awt.Dimension(200, 20));
        jAccountPassword.setPreferredSize(new java.awt.Dimension(200, 20));

        jAccountName.setMaximumSize(new java.awt.Dimension(200, 20));
        jAccountName.setMinimumSize(new java.awt.Dimension(200, 20));
        jAccountName.setPreferredSize(new java.awt.Dimension(200, 20));

        jButton5.setBackground(new java.awt.Color(239, 235, 223));
        jButton5.setText("Prüfen");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireLoginIntoAccountEvent(evt);
            }
        });

        javax.swing.GroupLayout jLoginPanelLayout = new javax.swing.GroupLayout(jLoginPanel);
        jLoginPanel.setLayout(jLoginPanelLayout);
        jLoginPanelLayout.setHorizontalGroup(
            jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLoginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6))
                .addGap(21, 21, 21)
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jAccountName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                    .addComponent(jAccountPassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                    .addComponent(jButton5))
                .addContainerGap(186, Short.MAX_VALUE))
        );
        jLoginPanelLayout.setVerticalGroup(
            jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLoginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jAccountName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAccountPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addContainerGap(166, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Login", new javax.swing.ImageIcon(getClass().getResource("/res/login.png")), jLoginPanel); // NOI18N

        jPlayerServerSettings.setBackground(new java.awt.Color(239, 235, 223));

        jLabel1.setText("Server");

        jLabel2.setText("Spieler");

        jSelectServerButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectServerButton.setText("Server auswählen");
        jSelectServerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateDataEvent(evt);
            }
        });

        jStatusArea.setColumns(20);
        jStatusArea.setEditable(false);
        jStatusArea.setRows(5);
        jScrollPane1.setViewportView(jStatusArea);

        jDownloadDataButton.setBackground(new java.awt.Color(239, 235, 223));
        jDownloadDataButton.setText("Daten downloaden");
        jDownloadDataButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDownloadDataEvent(evt);
            }
        });

        javax.swing.GroupLayout jPlayerServerSettingsLayout = new javax.swing.GroupLayout(jPlayerServerSettings);
        jPlayerServerSettings.setLayout(jPlayerServerSettingsLayout);
        jPlayerServerSettingsLayout.setHorizontalGroup(
            jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPlayerServerSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .addGroup(jPlayerServerSettingsLayout.createSequentialGroup()
                        .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTribeNames, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jServerList, 0, 262, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jDownloadDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                            .addComponent(jSelectServerButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPlayerServerSettingsLayout.setVerticalGroup(
            jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPlayerServerSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jServerList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSelectServerButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTribeNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDownloadDataButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Spieler/Server", new javax.swing.ImageIcon(getClass().getResource("/res/face.png")), jPlayerServerSettings); // NOI18N

        jGeneralSettings.setBackground(new java.awt.Color(239, 235, 223));

        jLabel5.setText("Grafikpaket");

        jGraphicPacks.setMaximumSize(new java.awt.Dimension(114, 22));
        jGraphicPacks.setMinimumSize(new java.awt.Dimension(114, 22));
        jGraphicPacks.setPreferredSize(new java.awt.Dimension(114, 22));

        jButton4.setBackground(new java.awt.Color(239, 235, 223));
        jButton4.setText("Auswählen");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectGraphicPackEvent(evt);
            }
        });

        jLabel8.setText("Automatischer Datenabgleich");

        jUpdateIntervalBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nie", "Bei Programmstart", "Stündlich", "Alle 2 Stunden", "Alle 4 Stunden", "Alle 12 Stunden", "Täglich" }));
        jUpdateIntervalBox.setSelectedIndex(6);
        jUpdateIntervalBox.setEnabled(false);

        jLabel11.setText("Kontinente anzeigen");

        jContinentsOnMinimap.setContentAreaFilled(false);
        jContinentsOnMinimap.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jContinentsOnMinimap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChangeContinentsOnMinimapEvent(evt);
            }
        });

        jLabel13.setText("Entfernung anzeigen");

        jShowDistanceBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jShowDistanceBox.setOpaque(false);
        jShowDistanceBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireChangeDrawDistanceEvent(evt);
            }
        });

        javax.swing.GroupLayout jGeneralSettingsLayout = new javax.swing.GroupLayout(jGeneralSettings);
        jGeneralSettings.setLayout(jGeneralSettingsLayout);
        jGeneralSettingsLayout.setHorizontalGroup(
            jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jGeneralSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel5)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jContinentsOnMinimap, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(jShowDistanceBox, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(jGraphicPacks, 0, 216, Short.MAX_VALUE)
                    .addComponent(jUpdateIntervalBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 216, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap())
        );
        jGeneralSettingsLayout.setVerticalGroup(
            jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jGeneralSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jButton4)
                    .addComponent(jGraphicPacks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jUpdateIntervalBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jContinentsOnMinimap))
                .addGap(12, 12, 12)
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jShowDistanceBox))
                .addContainerGap(133, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Allgemein", new javax.swing.ImageIcon(getClass().getResource("/res/settings.png")), jGeneralSettings); // NOI18N

        jNetworkSettings.setBackground(new java.awt.Color(239, 235, 223));

        connectionTypeGroup.add(jDirectConnectOption);
        jDirectConnectOption.setSelected(true);
        jDirectConnectOption.setText("Ich bin direkt mit dem Internet verbunden");
        jDirectConnectOption.setOpaque(false);
        jDirectConnectOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeConnectTypeEvent(evt);
            }
        });

        connectionTypeGroup.add(jProxyConnectOption);
        jProxyConnectOption.setText("Ich benutze einen Proxy für den Internetzugang");
        jProxyConnectOption.setOpaque(false);
        jProxyConnectOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeConnectTypeEvent(evt);
            }
        });

        jLabel3.setText("Proxy Adresse");

        jProxyHost.setBackground(new java.awt.Color(239, 235, 223));
        jProxyHost.setEnabled(false);

        jLabel4.setText("Proxy Port");

        jProxyPort.setBackground(new java.awt.Color(239, 235, 223));
        jProxyPort.setEnabled(false);
        jProxyPort.setMaximumSize(new java.awt.Dimension(40, 20));
        jProxyPort.setMinimumSize(new java.awt.Dimension(40, 20));
        jProxyPort.setPreferredSize(new java.awt.Dimension(40, 20));

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setText("Aktualisieren");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateProxySettingsEvent(evt);
            }
        });

        javax.swing.GroupLayout jNetworkSettingsLayout = new javax.swing.GroupLayout(jNetworkSettings);
        jNetworkSettings.setLayout(jNetworkSettingsLayout);
        jNetworkSettingsLayout.setHorizontalGroup(
            jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jNetworkSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jNetworkSettingsLayout.createSequentialGroup()
                        .addGroup(jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jProxyHost, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                            .addComponent(jProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jDirectConnectOption)
                    .addComponent(jProxyConnectOption))
                .addContainerGap())
        );
        jNetworkSettingsLayout.setVerticalGroup(
            jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jNetworkSettingsLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jDirectConnectOption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProxyConnectOption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(110, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Netzwerk", new javax.swing.ImageIcon(getClass().getResource("/res/proxy.png")), jNetworkSettings); // NOI18N

        jOKButton.setBackground(new java.awt.Color(239, 235, 223));
        jOKButton.setText("OK");
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireOkEvent(evt);
            }
        });

        jCancelButton.setBackground(new java.awt.Color(239, 235, 223));
        jCancelButton.setText("Abbrechen");
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseEvent(evt);
            }
        });

        jCreateAccountButton.setBackground(new java.awt.Color(239, 235, 223));
        jCreateAccountButton.setText("Neuen Account erstellen");
        jCreateAccountButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCreateAccountEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jCreateAccountButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 185, Short.MAX_VALUE)
                        .addComponent(jCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOKButton))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jOKButton)
                    .addComponent(jCancelButton)
                    .addComponent(jCreateAccountButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void fireUpdateProxySettingsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateProxySettingsEvent
        if (jDirectConnectOption.isSelected()) {
            System.getProperties().put("proxySet", "false");
            System.getProperties().put("proxyHost", "");
            System.getProperties().put("proxyPort", "");
            GlobalOptions.addProperty("proxySet", Boolean.toString(false));
            GlobalOptions.addProperty("proxyHost", "");
            GlobalOptions.addProperty("proxyPort", "");
        } else {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", jProxyHost.getText());
            System.getProperties().put("proxyPort", jProxyPort.getText());
            GlobalOptions.addProperty("proxySet", Boolean.toString(true));
            GlobalOptions.addProperty("proxyHost", jProxyHost.getText());
            GlobalOptions.addProperty("proxyPort", jProxyPort.getText());
        }

        GlobalOptions.saveProperties();
        if (!updateServerList(false)) {
            //remote update failed and no local servers found
            String message = "Serverliste konnte nicht geladen werden.\n";
            message = "Mögliche Ursachen sind fehlerhafte Netzwerkeinstellungen oder keine Verbindung zum Internet.\n";
            message = "Da noch kein Datenabgleich mit dem Server stattgefunden hat, korrigiere bitte deine Netzwerkeinstellungen um diesen einmalig durchzuführen.";
            JOptionPane.showMessageDialog(this, message, "Warnung", JOptionPane.WARNING_MESSAGE);
        } else if (GlobalOptions.isOfflineMode()) {
            //remote update failed but local servers found
            String message = "Serverliste konnte nicht geladen werden.\n";
            message += "Mögliche Ursachen sind fehlerhafte Netzwerkeinstellungen oder keine Verbindung zum Internet.\n";
            message += "Da bereits Serverdaten auf deiner Festplatte existieren, wechselt DS Workbench in den Offline-Modus.\n";
            message += "Um Online-Funktionen zu nutzen korrigieren bitte später deine Netzwerkeinstellungen oder verbinde dich mit dem Internet.";
            JOptionPane.showMessageDialog(this, message, "Warnung", JOptionPane.WARNING_MESSAGE);
        }
        if (mMainFrame != null) {
            mMainFrame.onlineStateChanged();
        }
    }//GEN-LAST:event_fireUpdateProxySettingsEvent

    private void fireChangeConnectTypeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeConnectTypeEvent
        jProxyHost.setEnabled(jProxyConnectOption.isSelected());
        jProxyPort.setEnabled(jProxyConnectOption.isSelected());
    }//GEN-LAST:event_fireChangeConnectTypeEvent

    private void fireUpdateDataEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateDataEvent
        if (!jSelectServerButton.isEnabled()) {
            return;
        }

        if (jServerList.getSelectedItem() == null) {
            return;
        }

        GlobalOptions.setSelectedServer((String) jServerList.getSelectedItem());
        GlobalOptions.addProperty("default.server", (String) jServerList.getSelectedItem());
        GlobalOptions.saveProperties();
        GlobalOptions.setSelectedServer((String) jServerList.getSelectedItem());
        jSelectServerButton.setEnabled(false);
        jDownloadDataButton.setEnabled(false);
        updating = true;
        jStatusArea.setText("");
        jOKButton.setEnabled(false);
        jCreateAccountButton.setEnabled(false);
        jCancelButton.setEnabled(false);
        jTribeNames.setModel(new DefaultComboBoxModel());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    GlobalOptions.loadData(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.setDaemon(true);
        t.start();
}//GEN-LAST:event_fireUpdateDataEvent

    private void fireCloseEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseEvent
        if (!jCancelButton.isEnabled()) {
            return;
        }
        if (!checkPlayerSettings()) {
            String message = "Bitte überprüfe die Spieler-/Servereinstellungen und schließe die Einstellungen mit OK.\n";
            message += "Möglicherweise wurde noch kein Server oder kein Spieler ausgewählt.\n";
            message += "Diese Einstellungen sind für einen korrekten Ablauf zwingend notwendig.";
            UIManager.put("OptionPane.noButtonText", "Beenden");
            UIManager.put("OptionPane.yesButtonText", "Korrigieren");
            if (JOptionPane.showConfirmDialog(this, message, "Warnung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                logger.error("Player/Server settings incorrect. User requested application to terminate");
                System.exit(1);
            } else {
                UIManager.put("OptionPane.noButtonText", "No");
                UIManager.put("OptionPane.yesButtonText", "Yes");
                return;
            }
        }
        if (mMainFrame != null) {
            mMainFrame.serverSettingsChangedEvent();
        }
        setVisible(false);
    }//GEN-LAST:event_fireCloseEvent

    private void fireOkEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireOkEvent
        if (!jOKButton.isEnabled()) {
            return;
        }

        GlobalOptions.addProperty("auto.update.interval", Integer.toString(jUpdateIntervalBox.getSelectedIndex()));
        String selection = (String) jTribeNames.getSelectedItem();
        if ((selection != null) && (!selection.equals("Bitte auswählen"))) {
            logger.debug("Setting default player for server '" + GlobalOptions.getSelectedServer() + "' to " + jTribeNames.getSelectedItem());
            GlobalOptions.addProperty("player." + GlobalOptions.getSelectedServer(), (String) jTribeNames.getSelectedItem());
        }
        GlobalOptions.saveProperties();

        if (!checkSettings()) {
            return;
        }
        if (mMainFrame != null) {
            mMainFrame.serverSettingsChangedEvent();
        }
        setVisible(false);
    }//GEN-LAST:event_fireOkEvent

    private void fireSelectGraphicPackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectGraphicPackEvent
        GlobalOptions.addProperty("default.skin", (String) jGraphicPacks.getSelectedItem());
        try {
            GlobalOptions.loadSkin();
        } catch (Exception e) {
            logger.error("Failed to load skin '" + jGraphicPacks.getSelectedItem() + "'", e);
            JOptionPane.showMessageDialog(this, "Fehler beim laden des Grafikpaketes.");
            //load default
            GlobalOptions.addProperty("default.skin", "default");
            try {
                GlobalOptions.loadSkin();
            } catch (Exception ie) {
                logger.error("Failed to load default skin", ie);
            }

        }
    }//GEN-LAST:event_fireSelectGraphicPackEvent

private void fireCreateAccountEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateAccountEvent
    if (!jCreateAccountButton.isEnabled()) {
        return;
    }
    jCreateAccountDialog.setVisible(true);
}//GEN-LAST:event_fireCreateAccountEvent

private void fireLoginIntoAccountEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireLoginIntoAccountEvent
    String name = jAccountName.getText();
    String password = new String(jAccountPassword.getPassword());

    if ((name != null) && (password != null)) {
        int ret = DatabaseAdapter.checkUser(name, password);
        if (ret == DatabaseAdapter.ID_SUCCESS) {
            GlobalOptions.addProperty("account.name", jAccountName.getText());
            GlobalOptions.addProperty("account.password", new String(jAccountPassword.getPassword()));
            GlobalOptions.saveProperties();
        } else if (ret == DatabaseAdapter.ID_CONNECTION_FAILED) {
            JOptionPane.showMessageDialog(this, "Keine Verbindung zur Datenbank.\nBitte überprüfe die Netzwerkeinstellungen.", "Fehler", JOptionPane.ERROR_MESSAGE);
        } else if (ret == DatabaseAdapter.ID_USER_NOT_EXIST) {
            JOptionPane.showMessageDialog(this, "Der Benutzer '" + name + "' existiert nicht.\nBitte erstelle einen neuen Account.", "Information", JOptionPane.INFORMATION_MESSAGE);
        } else if (ret == DatabaseAdapter.ID_WRONG_PASSWORD) {
            JOptionPane.showMessageDialog(this, "Das eingegebene Passwort ist falsch.\nBitte überprüfe den Benutzernamen oder erstellen einen Account.", "Fehler", JOptionPane.ERROR_MESSAGE);
        } else if (ret == DatabaseAdapter.ID_UNKNOWN_ERROR) {
            JOptionPane.showMessageDialog(this, "Ein unbekannter Fehler ist aufgetreten.", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }
}//GEN-LAST:event_fireLoginIntoAccountEvent

private void fireClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireClosingEvent
    fireCloseEvent(null);
}//GEN-LAST:event_fireClosingEvent

private void fireRegisterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRegisterEvent
    String user = jRegistrationAccountName.getText();
    String password = new String(jRegistrationPassword.getPassword());
    String password2 = new String(jRegistrationPassword2.getPassword());

    if ((user.length() < 3) || (password.length() < 3)) {
        JOptionPane.showMessageDialog(jCreateAccountDialog, "Accountname und Passwort müssen mindestens 3 Zeichen lang sein.", "Fehler", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    if (!password.equals(password2)) {
        JOptionPane.showMessageDialog(jCreateAccountDialog, "Die eingegebenen Passwörter unterscheiden sich.\nBitte überprüfe deine Eingabe.", "Warnung", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int ret = DatabaseAdapter.addUser(user, password);
    switch (ret) {
        case DatabaseAdapter.ID_CONNECTION_FAILED: {
            JOptionPane.showMessageDialog(jCreateAccountDialog, "Fehler beim Verbinden mit der Datenbank.\nBitte überprüfe die Netzwerkeinstellungen.", "Fehler", JOptionPane.ERROR);
            break;

        }
        case DatabaseAdapter.ID_DUAL_ACCOUNT: {
            JOptionPane.showMessageDialog(jCreateAccountDialog, "Die Überprüfung hat ergeben, dass du bereits einen Account erstellt hast.\nDas Anlegen von mehreren Accounts ist nicht erlaubt!", "Fehler", JOptionPane.ERROR_MESSAGE);
            break;
        }
        case DatabaseAdapter.ID_USER_ALREADY_EXIST: {
            JOptionPane.showMessageDialog(jCreateAccountDialog, "Es existiert bereits ein Benutzer mit dem angegebenen Namen.\nBitte wähle einen anderen Namen.", "Fehler", JOptionPane.ERROR_MESSAGE);
            break;
        }
        case DatabaseAdapter.ID_UNKNOWN_ERROR: {
            JOptionPane.showMessageDialog(jCreateAccountDialog, "Ein unbekannter Fehler ist aufgetreten.\nBitte wende dich an den Entwickler.", "Fehler", JOptionPane.ERROR);
            break;
        }
        default: {
            JOptionPane.showMessageDialog(jCreateAccountDialog, "Dein Account wurde erfolgreich angelegt.\nDu kannst nun DS-Serverdaten herunterladen.", "Account angelegt", JOptionPane.INFORMATION_MESSAGE);
            jAccountName.setText(jRegistrationAccountName.getText());
            jAccountPassword.setText(new String(jRegistrationPassword.getPassword()));
            jCreateAccountDialog.setVisible(false);
        }
    }
}//GEN-LAST:event_fireRegisterEvent

private void fireCancelRegistrationEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelRegistrationEvent
    jCreateAccountDialog.setVisible(false);
}//GEN-LAST:event_fireCancelRegistrationEvent

private void fireChangeContinentsOnMinimapEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChangeContinentsOnMinimapEvent
    GlobalOptions.addProperty("minimap.showcontinents", Boolean.toString(jContinentsOnMinimap.isSelected()));
    GlobalOptions.saveProperties();
    MinimapPanel.getGlobalMinimap().resetBuffer();
    MinimapPanel.getGlobalMinimap().redraw();
}//GEN-LAST:event_fireChangeContinentsOnMinimapEvent

private void fireDownloadDataEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDownloadDataEvent
    if (!jSelectServerButton.isEnabled()) {
        return;
    }

    if (jServerList.getSelectedItem() == null) {
        return;
    }

    if (GlobalOptions.isOfflineMode()) {
        JOptionPane.showMessageDialog(this, "Du befindest dich im Offline-Modus.\nBitte korrigiere deine Netzwerkeinstellungen um den Download durchzuführen.", "Warnung", JOptionPane.WARNING_MESSAGE);
        return;
    }
    String selectedServer = (String) jServerList.getSelectedItem();
    String name = GlobalOptions.getProperty("account.name");
    String password = GlobalOptions.getProperty("account.password");
    if (DatabaseAdapter.checkUser(name, password) != DatabaseAdapter.ID_SUCCESS) {
        JOptionPane.showMessageDialog(this, "Die Accountvalidierung ist fehlgeschlagen.\nBitte überprüfe deine Account- und Netzwerkeinstellungen und versuches es erneut.", "Fehler", JOptionPane.ERROR_MESSAGE);
        return;
    } else {
        if (!DatabaseAdapter.isUpdatePossible(name, selectedServer)) {
            long delta = DatabaseAdapter.getTimeSinceLastUpdate(name, selectedServer);
            long minDelta = DatabaseAdapter.getMinUpdateInterval();
            if ((delta > 0) && (minDelta > 0)) {
                long next = minDelta - delta;
                String nextUpdate = new SimpleDateFormat("hh 'h' mm 'min' ss 's'").format(new Date(next));
                JOptionPane.showMessageDialog(this, "Ein Datenabgleich ist momentan nur einmal täglich erlaubt.\nNächstmögliches Update in: " + nextUpdate, "Fehler", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Ein Datenabgleich ist momentan nur einmal täglich erlaubt.\nÜberprüfung auf nächstmögliches Update fehlgeschlagen.\nBitte korrigiere deine Account Einstellungen.", "Fehler", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
    }

    GlobalOptions.addProperty("default.server", selectedServer);
    GlobalOptions.saveProperties();
    GlobalOptions.setSelectedServer((String) jServerList.getSelectedItem());
    jSelectServerButton.setEnabled(false);
    jDownloadDataButton.setEnabled(false);
    jCreateAccountButton.setEnabled(false);
    updating = true;
    jStatusArea.setText("");
    jOKButton.setEnabled(false);
    jCancelButton.setEnabled(false);
    jTribeNames.setModel(new DefaultComboBoxModel());

    Thread t = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                GlobalOptions.loadData(true);
            } catch (Exception e) {
                logger.error("Failed to update data", e);
            }

        }
    });
    t.setPriority(Thread.MIN_PRIORITY);
    t.setDaemon(true);
    t.start();
}//GEN-LAST:event_fireDownloadDataEvent

private void fireChangeDrawDistanceEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireChangeDrawDistanceEvent
    GlobalOptions.addProperty("draw.distance", Boolean.toString(jShowDistanceBox.isSelected()));
    GlobalOptions.saveProperties();
}//GEN-LAST:event_fireChangeDrawDistanceEvent

    private boolean updateServerList(boolean pLocal) {
        String[] servers = null;
        if (!pLocal) {
            try {
                ServerList.loadServerList();
                servers = ServerList.getServerIDs();
                GlobalOptions.setOfflineMode(false);
            } catch (Exception e) {
                logger.error("Failed to load server list", e);
                GlobalOptions.setOfflineMode(true);
            }
        }
        if (servers == null) {
            GlobalOptions.setOfflineMode(true);
            servers = GlobalOptions.getDataHolder().getLocalServers();
        }

        if (servers.length < 1) {
            logger.error("No locally stored server found");
            jServerList.setModel(new DefaultComboBoxModel());
            jTribeNames.setModel(new DefaultComboBoxModel());
            return false;
        }

        Arrays.sort(servers, null);
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        for (String serverID : servers) {
            model.addElement(serverID);
        }
        jServerList.setModel(model);

        if (GlobalOptions.getProperty("default.server") != null) {
            if (model.getIndexOf(GlobalOptions.getProperty("default.server")) != -1) {
                jServerList.setSelectedItem(GlobalOptions.getProperty("default.server"));
                model = new DefaultComboBoxModel();
                if (GlobalOptions.getProperty("player." + GlobalOptions.getProperty("default.server")) != null) {
                    model.addElement(GlobalOptions.getProperty("player." + GlobalOptions.getProperty("default.server")));
                    jTribeNames.setModel(model);
                    jTribeNames.setSelectedIndex(0);
                } else {
                    model.addElement("Bitte auswählen");
                    jTribeNames.setModel(model);
                    jTribeNames.setSelectedIndex(0);
                }
            } else {
                jServerList.setSelectedIndex(0);
            }
        } else {
            jServerList.setSelectedIndex(0);
        }
        return true;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        DOMConfigurator.configure("log4j.xml");

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchSettingsDialog().setVisible(true);

            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup connectionTypeGroup;
    private javax.swing.JTextField jAccountName;
    private javax.swing.JPasswordField jAccountPassword;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JCheckBox jContinentsOnMinimap;
    private javax.swing.JButton jCreateAccountButton;
    private javax.swing.JDialog jCreateAccountDialog;
    private javax.swing.JRadioButton jDirectConnectOption;
    private javax.swing.JButton jDownloadDataButton;
    private javax.swing.JPanel jGeneralSettings;
    private javax.swing.JComboBox jGraphicPacks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jLoginPanel;
    private javax.swing.JPanel jNetworkSettings;
    private javax.swing.JButton jOKButton;
    private javax.swing.JPanel jPlayerServerSettings;
    private javax.swing.JRadioButton jProxyConnectOption;
    private javax.swing.JTextField jProxyHost;
    private javax.swing.JTextField jProxyPort;
    private javax.swing.JTextField jRegistrationAccountName;
    private javax.swing.JPasswordField jRegistrationPassword;
    private javax.swing.JPasswordField jRegistrationPassword2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jSelectServerButton;
    private javax.swing.JComboBox jServerList;
    private javax.swing.JCheckBox jShowDistanceBox;
    private javax.swing.JTextArea jStatusArea;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox jTribeNames;
    private javax.swing.JComboBox jUpdateIntervalBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireDataHolderEvent(String pMessage) {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        jStatusArea.insert("(" + f.format(new Date(System.currentTimeMillis())) + ") " + pMessage + "\n", jStatusArea.getText().length());
        try {
            Point point = new Point(0, (int) (jStatusArea.getSize().getHeight()));
            JViewport vp = jScrollPane1.getViewport();
            if ((vp == null) || (point == null)) {
                return;
            }
            vp.setViewPosition(point);
        } catch (Throwable t) {
        }
    }

    @Override
    public void fireDataLoadedEvent() {
        fireDataHolderEvent("Erstelle Spielerliste");
        String[] tribeNames = new String[GlobalOptions.getDataHolder().getTribes().size()];
        Enumeration<Integer> tribes = GlobalOptions.getDataHolder().getTribes().keys();
        int cnt = 0;
        while (tribes.hasMoreElements()) {
            tribeNames[cnt] = GlobalOptions.getDataHolder().getTribes().get(tribes.nextElement()).getName();
            cnt++;
        }
        long s = System.currentTimeMillis();
        Arrays.sort(tribeNames, null);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("Bitte wählen");

        for (String tribe : tribeNames) {
            model.addElement(tribe);
        }
        jTribeNames.setModel(model);
        if (GlobalOptions.getProperty("player." + GlobalOptions.getSelectedServer()) != null) {
            if (model.getIndexOf(GlobalOptions.getProperty("player." + GlobalOptions.getSelectedServer())) != -1) {
                jTribeNames.setSelectedItem(GlobalOptions.getProperty("player." + GlobalOptions.getSelectedServer()));
            } else {
                jTribeNames.setSelectedIndex(0);
            }
        } else {
            jTribeNames.setSelectedIndex(0);
        }
        if (mMainFrame != null) {
            mMainFrame.serverSettingsChangedEvent();
        }
        fireDataHolderEvent("Lade Benutzerdaten");
        GlobalOptions.loadUserData();

        fireDataHolderEvent("Fertig");
        updating = false;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        jSelectServerButton.setEnabled(true);
        jDownloadDataButton.setEnabled(true);
        jCreateAccountButton.setEnabled(true);
        jOKButton.setEnabled(true);
        jCancelButton.setEnabled(true);
    }
}


