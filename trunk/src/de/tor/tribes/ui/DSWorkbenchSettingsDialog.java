/*
 * TribesPlannerStartFrame.java
 *
 * Created on 9. Juni 2008, 15:54
 */
package de.tor.tribes.ui;

import de.tor.tribes.db.DatabaseAdapter;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.ServerList;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Color;
import java.awt.Point;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import de.tor.tribes.ui.renderer.ColorCellRenderer;
import de.tor.tribes.io.UnitHolder;

/**
 *
 * @author  Jejkal
 */
public class DSWorkbenchSettingsDialog extends javax.swing.JDialog implements DataHolderListener {

    private static Logger logger = Logger.getLogger(DSWorkbenchSettingsDialog.class);
    private static DSWorkbenchSettingsDialog SINGLETON = null;
    boolean updating = false;
    boolean gotServerList = false;

    public static DSWorkbenchSettingsDialog getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSettingsDialog();
        }

        return SINGLETON;
    }

    /** Creates new form TribesPlannerStartFrame */
    DSWorkbenchSettingsDialog() {
        initComponents();

        jCreateAccountDialog.pack();
        getContentPane().setBackground(Constants.DS_BACK);
        jCreateAccountDialog.getContentPane().setBackground(Constants.DS_BACK);
        setAlwaysOnTop(true);
        // jControlPanel.setupPanel(this, true, false);

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

        setupAttackColorTable();

    }

    private void setupAttackColorTable() {
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Einheit", "Farbe"
                }) {

            Class[] types = new Class[]{
                String.class, Color.class
            };
            boolean[] canEdit = new boolean[]{
                false, true
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
        jAttackColorTable.setDefaultRenderer(Color.class, new ColorCellRenderer());
        jAttackColorTable.setModel(model);
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            model.addRow(new Object[]{unit, Color.RED});
        }

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
        /***************************
         ***Check server and DS user
         ***************************/
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
        jRegisterNameLabel = new javax.swing.JLabel();
        jRegistrationAccountName = new javax.swing.JTextField();
        jRegisterPasswordLabel = new javax.swing.JLabel();
        jRegistrationPassword = new javax.swing.JPasswordField();
        jRegisterButton = new javax.swing.JButton();
        jCancelRegistrationButton = new javax.swing.JButton();
        jRepeatPasswordLabel = new javax.swing.JLabel();
        jRegistrationPassword2 = new javax.swing.JPasswordField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jLoginPanel = new javax.swing.JPanel();
        jAccountNameLabel = new javax.swing.JLabel();
        jAccountPasswordLabel = new javax.swing.JLabel();
        jAccountPassword = new javax.swing.JPasswordField();
        jAccountName = new javax.swing.JTextField();
        jCheckAccountButton = new javax.swing.JButton();
        jPlayerServerSettings = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jServerList = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jTribeNames = new javax.swing.JComboBox();
        jSelectServerButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jStatusArea = new javax.swing.JTextArea();
        jDownloadDataButton = new javax.swing.JButton();
        jMapSettings = new javax.swing.JPanel();
        jSkinPackLabel = new javax.swing.JLabel();
        jGraphicPacks = new javax.swing.JComboBox();
        jSelectSkinButton = new javax.swing.JButton();
        jPreviewSkinButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jContinentsOnMinimap = new javax.swing.JCheckBox();
        jShowDistanceBox = new javax.swing.JCheckBox();
        jMarkOwnVillagesOnMinimapBox = new javax.swing.JCheckBox();
        jMarkActiveVillageBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jShowContinentsLabel = new javax.swing.JLabel();
        jShowDistanceLabel = new javax.swing.JLabel();
        jAttackMovementLabel2 = new javax.swing.JLabel();
        jAttackMovementLabel1 = new javax.swing.JLabel();
        jNetworkSettings = new javax.swing.JPanel();
        jDirectConnectOption = new javax.swing.JRadioButton();
        jProxyConnectOption = new javax.swing.JRadioButton();
        jProxyAdressLabel = new javax.swing.JLabel();
        jProxyHost = new javax.swing.JTextField();
        jProxyPortLabel = new javax.swing.JLabel();
        jProxyPort = new javax.swing.JTextField();
        jRefeshNetworkButton = new javax.swing.JButton();
        jAttackSettings = new javax.swing.JPanel();
        jAttackMovementLabel = new javax.swing.JLabel();
        jShowAttackMovementBox = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAttackColorTable = new javax.swing.JTable();
        jAttackMovementLabel3 = new javax.swing.JLabel();
        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jCreateAccountButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jCreateAccountDialog.setTitle(bundle.getString("DSWorkbenchSettingsDialog.jCreateAccountDialog.title")); // NOI18N
        jCreateAccountDialog.setAlwaysOnTop(true);
        jCreateAccountDialog.setBackground(new java.awt.Color(239, 235, 223));
        jCreateAccountDialog.setModal(true);

        jRegisterNameLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jRegisterNameLabel.text")); // NOI18N

        jRegistrationAccountName.setMaximumSize(new java.awt.Dimension(200, 20));
        jRegistrationAccountName.setMinimumSize(new java.awt.Dimension(200, 20));
        jRegistrationAccountName.setPreferredSize(new java.awt.Dimension(200, 20));

        jRegisterPasswordLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jRegisterPasswordLabel.text")); // NOI18N

        jRegistrationPassword.setMaximumSize(new java.awt.Dimension(200, 20));
        jRegistrationPassword.setMinimumSize(new java.awt.Dimension(200, 20));
        jRegistrationPassword.setPreferredSize(new java.awt.Dimension(200, 20));

        jRegisterButton.setBackground(new java.awt.Color(239, 235, 223));
        jRegisterButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jRegisterButton.text")); // NOI18N
        jRegisterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRegisterEvent(evt);
            }
        });

        jCancelRegistrationButton.setBackground(new java.awt.Color(239, 235, 223));
        jCancelRegistrationButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jCancelRegistrationButton.text")); // NOI18N
        jCancelRegistrationButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelRegistrationEvent(evt);
            }
        });

        jRepeatPasswordLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jRepeatPasswordLabel.text")); // NOI18N

        javax.swing.GroupLayout jCreateAccountDialogLayout = new javax.swing.GroupLayout(jCreateAccountDialog.getContentPane());
        jCreateAccountDialog.getContentPane().setLayout(jCreateAccountDialogLayout);
        jCreateAccountDialogLayout.setHorizontalGroup(
            jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCreateAccountDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRegisterPasswordLabel)
                    .addComponent(jRegisterNameLabel)
                    .addComponent(jRepeatPasswordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRegistrationPassword2, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addGroup(jCreateAccountDialogLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCancelRegistrationButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRegisterButton, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                    .addComponent(jRegistrationAccountName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRegistrationPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jCreateAccountDialogLayout.setVerticalGroup(
            jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCreateAccountDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRegisterNameLabel)
                    .addComponent(jRegistrationAccountName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRegisterPasswordLabel)
                    .addComponent(jRegistrationPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRepeatPasswordLabel)
                    .addComponent(jRegistrationPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jCreateAccountDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCancelRegistrationButton)
                    .addComponent(jRegisterButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle(bundle.getString("DSWorkbenchSettingsDialog.title")); // NOI18N
        setAlwaysOnTop(true);
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fireClosingEvent(evt);
            }
        });

        jTabbedPane1.setBackground(new java.awt.Color(239, 235, 223));

        jLoginPanel.setBackground(new java.awt.Color(239, 235, 223));

        jAccountNameLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jAccountNameLabel.text")); // NOI18N

        jAccountPasswordLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jAccountPasswordLabel.text")); // NOI18N

        jAccountPassword.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jAccountPassword.toolTipText")); // NOI18N
        jAccountPassword.setMaximumSize(new java.awt.Dimension(200, 20));
        jAccountPassword.setMinimumSize(new java.awt.Dimension(200, 20));
        jAccountPassword.setPreferredSize(new java.awt.Dimension(200, 20));

        jAccountName.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jAccountName.toolTipText")); // NOI18N
        jAccountName.setMaximumSize(new java.awt.Dimension(200, 20));
        jAccountName.setMinimumSize(new java.awt.Dimension(200, 20));
        jAccountName.setPreferredSize(new java.awt.Dimension(200, 20));

        jCheckAccountButton.setBackground(new java.awt.Color(239, 235, 223));
        jCheckAccountButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jCheckAccountButton.text")); // NOI18N
        jCheckAccountButton.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jCheckAccountButton.toolTipText")); // NOI18N
        jCheckAccountButton.addMouseListener(new java.awt.event.MouseAdapter() {
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
                    .addComponent(jAccountPasswordLabel)
                    .addComponent(jAccountNameLabel))
                .addGap(21, 21, 21)
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jAccountName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addComponent(jAccountPassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addComponent(jCheckAccountButton))
                .addContainerGap(268, Short.MAX_VALUE))
        );
        jLoginPanelLayout.setVerticalGroup(
            jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLoginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAccountNameLabel)
                    .addComponent(jAccountName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAccountPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAccountPasswordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckAccountButton)
                .addContainerGap(163, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("DSWorkbenchSettingsDialog.jLoginPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/login.png")), jLoginPanel); // NOI18N

        jPlayerServerSettings.setBackground(new java.awt.Color(239, 235, 223));

        jLabel1.setText(bundle.getString("DSWorkbenchSettingsDialog.jLabel1.text")); // NOI18N

        jServerList.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jServerList.toolTipText")); // NOI18N

        jLabel2.setText(bundle.getString("DSWorkbenchSettingsDialog.jLabel2.text")); // NOI18N

        jTribeNames.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jTribeNames.toolTipText")); // NOI18N

        jSelectServerButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectServerButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jSelectServerButton.text")); // NOI18N
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
        jDownloadDataButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jDownloadDataButton.text")); // NOI18N
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
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
                            .addComponent(jDownloadDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .addComponent(jSelectServerButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))))
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("DSWorkbenchSettingsDialog.jPlayerServerSettings.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/face.png")), jPlayerServerSettings); // NOI18N

        jMapSettings.setBackground(new java.awt.Color(239, 235, 223));

        jSkinPackLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jSkinPackLabel.text")); // NOI18N

        jGraphicPacks.setMaximumSize(new java.awt.Dimension(114, 22));
        jGraphicPacks.setMinimumSize(new java.awt.Dimension(114, 22));
        jGraphicPacks.setPreferredSize(new java.awt.Dimension(114, 22));

        jSelectSkinButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectSkinButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jSelectSkinButton.text")); // NOI18N
        jSelectSkinButton.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jSelectSkinButton.toolTipText")); // NOI18N
        jSelectSkinButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectGraphicPackEvent(evt);
            }
        });

        jPreviewSkinButton.setBackground(new java.awt.Color(239, 235, 223));
        jPreviewSkinButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jPreviewSkinButton.text")); // NOI18N
        jPreviewSkinButton.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jPreviewSkinButton.toolTipText")); // NOI18N
        jPreviewSkinButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowSkinPreviewEvent(evt);
            }
        });

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jContinentsOnMinimap.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jContinentsOnMinimap.toolTipText")); // NOI18N
        jContinentsOnMinimap.setContentAreaFilled(false);
        jContinentsOnMinimap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChangeContinentsOnMinimapEvent(evt);
            }
        });
        jPanel1.add(jContinentsOnMinimap);

        jShowDistanceBox.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jShowDistanceBox.toolTipText")); // NOI18N
        jShowDistanceBox.setOpaque(false);
        jShowDistanceBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireChangeDrawDistanceEvent(evt);
            }
        });
        jPanel1.add(jShowDistanceBox);

        jMarkOwnVillagesOnMinimapBox.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jMarkOwnVillagesOnMinimapBox.toolTipText")); // NOI18N
        jMarkOwnVillagesOnMinimapBox.setOpaque(false);
        jMarkOwnVillagesOnMinimapBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireChangeMarkOwnVillagesOnMinimapEvent(evt);
            }
        });
        jPanel1.add(jMarkOwnVillagesOnMinimapBox);

        jMarkActiveVillageBox.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jMarkActiveVillageBox.toolTipText")); // NOI18N
        jMarkActiveVillageBox.setOpaque(false);
        jMarkActiveVillageBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireChangeMarkActiveVillageEvent(evt);
            }
        });
        jPanel1.add(jMarkActiveVillageBox);

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jShowContinentsLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jShowContinentsLabel.text")); // NOI18N
        jShowContinentsLabel.setMaximumSize(new java.awt.Dimension(150, 21));
        jShowContinentsLabel.setMinimumSize(new java.awt.Dimension(150, 21));
        jShowContinentsLabel.setPreferredSize(new java.awt.Dimension(150, 21));
        jPanel2.add(jShowContinentsLabel);

        jShowDistanceLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jShowDistanceLabel.text")); // NOI18N
        jShowDistanceLabel.setMaximumSize(new java.awt.Dimension(150, 21));
        jShowDistanceLabel.setMinimumSize(new java.awt.Dimension(150, 21));
        jShowDistanceLabel.setPreferredSize(new java.awt.Dimension(150, 21));
        jPanel2.add(jShowDistanceLabel);

        jAttackMovementLabel2.setText(bundle.getString("DSWorkbenchSettingsDialog.jAttackMovementLabel2.text")); // NOI18N
        jAttackMovementLabel2.setMaximumSize(new java.awt.Dimension(150, 21));
        jAttackMovementLabel2.setMinimumSize(new java.awt.Dimension(150, 21));
        jAttackMovementLabel2.setPreferredSize(new java.awt.Dimension(150, 21));
        jPanel2.add(jAttackMovementLabel2);

        jAttackMovementLabel1.setText(bundle.getString("DSWorkbenchSettingsDialog.jAttackMovementLabel1.text")); // NOI18N
        jAttackMovementLabel1.setMaximumSize(new java.awt.Dimension(150, 21));
        jAttackMovementLabel1.setMinimumSize(new java.awt.Dimension(150, 21));
        jAttackMovementLabel1.setPreferredSize(new java.awt.Dimension(150, 21));
        jPanel2.add(jAttackMovementLabel1);

        javax.swing.GroupLayout jMapSettingsLayout = new javax.swing.GroupLayout(jMapSettings);
        jMapSettings.setLayout(jMapSettingsLayout);
        jMapSettingsLayout.setHorizontalGroup(
            jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMapSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSkinPackLabel)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                    .addComponent(jGraphicPacks, javax.swing.GroupLayout.Alignment.TRAILING, 0, 242, Short.MAX_VALUE))
                .addGap(25, 25, 25)
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPreviewSkinButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSelectSkinButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(41, 41, 41))
        );
        jMapSettingsLayout.setVerticalGroup(
            jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMapSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSelectSkinButton)
                    .addComponent(jSkinPackLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jGraphicPacks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPreviewSkinButton)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(125, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("DSWorkbenchSettingsDialog.jMapSettings.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/ui/map.gif")), jMapSettings); // NOI18N

        jNetworkSettings.setBackground(new java.awt.Color(239, 235, 223));

        connectionTypeGroup.add(jDirectConnectOption);
        jDirectConnectOption.setSelected(true);
        jDirectConnectOption.setText(bundle.getString("DSWorkbenchSettingsDialog.jDirectConnectOption.text")); // NOI18N
        jDirectConnectOption.setOpaque(false);
        jDirectConnectOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeConnectTypeEvent(evt);
            }
        });

        connectionTypeGroup.add(jProxyConnectOption);
        jProxyConnectOption.setText(bundle.getString("DSWorkbenchSettingsDialog.jProxyConnectOption.text")); // NOI18N
        jProxyConnectOption.setOpaque(false);
        jProxyConnectOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeConnectTypeEvent(evt);
            }
        });

        jProxyAdressLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jProxyAdressLabel.text")); // NOI18N

        jProxyHost.setBackground(new java.awt.Color(239, 235, 223));
        jProxyHost.setEnabled(false);

        jProxyPortLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jProxyPortLabel.text")); // NOI18N

        jProxyPort.setBackground(new java.awt.Color(239, 235, 223));
        jProxyPort.setEnabled(false);
        jProxyPort.setMaximumSize(new java.awt.Dimension(40, 20));
        jProxyPort.setMinimumSize(new java.awt.Dimension(40, 20));
        jProxyPort.setPreferredSize(new java.awt.Dimension(40, 20));

        jRefeshNetworkButton.setBackground(new java.awt.Color(239, 235, 223));
        jRefeshNetworkButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jRefeshNetworkButton.text")); // NOI18N
        jRefeshNetworkButton.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jRefeshNetworkButton.toolTipText")); // NOI18N
        jRefeshNetworkButton.addMouseListener(new java.awt.event.MouseAdapter() {
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
                            .addComponent(jProxyAdressLabel)
                            .addComponent(jProxyPortLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jProxyHost, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                            .addComponent(jProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jRefeshNetworkButton, javax.swing.GroupLayout.Alignment.TRAILING)
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
                    .addComponent(jProxyAdressLabel)
                    .addComponent(jProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jProxyPortLabel)
                    .addComponent(jProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRefeshNetworkButton)
                .addContainerGap(107, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("DSWorkbenchSettingsDialog.jNetworkSettings.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/proxy.png")), jNetworkSettings); // NOI18N

        jAttackMovementLabel.setText(bundle.getString("DSWorkbenchSettingsDialog.jAttackMovementLabel.text")); // NOI18N
        jAttackMovementLabel.setMaximumSize(new java.awt.Dimension(150, 21));
        jAttackMovementLabel.setMinimumSize(new java.awt.Dimension(150, 21));
        jAttackMovementLabel.setPreferredSize(new java.awt.Dimension(150, 21));

        jShowAttackMovementBox.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jShowAttackMovementBox.toolTipText")); // NOI18N
        jShowAttackMovementBox.setOpaque(false);
        jShowAttackMovementBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireChangeShowAttackMovementEvent(evt);
            }
        });

        jScrollPane2.setOpaque(false);

        jAttackColorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(jAttackColorTable);

        jAttackMovementLabel3.setText(bundle.getString("DSWorkbenchSettingsDialog.jAttackMovementLabel3.text")); // NOI18N
        jAttackMovementLabel3.setMaximumSize(new java.awt.Dimension(150, 21));
        jAttackMovementLabel3.setMinimumSize(new java.awt.Dimension(150, 21));
        jAttackMovementLabel3.setPreferredSize(new java.awt.Dimension(150, 21));

        javax.swing.GroupLayout jAttackSettingsLayout = new javax.swing.GroupLayout(jAttackSettings);
        jAttackSettings.setLayout(jAttackSettingsLayout);
        jAttackSettingsLayout.setHorizontalGroup(
            jAttackSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAttackMovementLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAttackMovementLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jAttackSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                    .addComponent(jShowAttackMovementBox))
                .addContainerGap())
        );
        jAttackSettingsLayout.setVerticalGroup(
            jAttackSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jShowAttackMovementBox)
                    .addComponent(jAttackMovementLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAttackSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAttackMovementLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(114, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("DSWorkbenchSettingsDialog.jAttackSettings.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jAttackSettings); // NOI18N

        jOKButton.setBackground(new java.awt.Color(239, 235, 223));
        jOKButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jOKButton.text")); // NOI18N
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireOkEvent(evt);
            }
        });

        jCancelButton.setBackground(new java.awt.Color(239, 235, 223));
        jCancelButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jCancelButton.text")); // NOI18N
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseEvent(evt);
            }
        });

        jCreateAccountButton.setBackground(new java.awt.Color(239, 235, 223));
        jCreateAccountButton.setText(bundle.getString("DSWorkbenchSettingsDialog.jCreateAccountButton.text")); // NOI18N
        jCreateAccountButton.setToolTipText(bundle.getString("DSWorkbenchSettingsDialog.jCreateAccountButton.toolTipText")); // NOI18N
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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCreateAccountButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 279, Short.MAX_VALUE)
                        .addComponent(jCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOKButton))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE))
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
        DSWorkbenchMainFrame.getSingleton().onlineStateChanged();

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
        //store current managers
        TagManager.getSingleton().saveTagsToFile(DataHolder.getSingleton().getDataDirectory() + "/tags.xml");
        MarkerManager.getSingleton().saveMarkersToFile(DataHolder.getSingleton().getDataDirectory() + "/markers.xml");
        AttackManager.getSingleton().saveAttacksToFile(DataHolder.getSingleton().getDataDirectory() + "/attacks.xml");

        String selectedServer = (String) jServerList.getSelectedItem();
        GlobalOptions.addProperty("default.server", selectedServer);
        GlobalOptions.saveProperties();

        GlobalOptions.setSelectedServer(selectedServer);
        jSelectServerButton.setEnabled(false);
        jDownloadDataButton.setEnabled(false);
        updating = true;
        jStatusArea.setText("");
        jOKButton.setEnabled(false);
        jCreateAccountButton.setEnabled(false);
        jCancelButton.setEnabled(false);
        jTribeNames.setModel(new DefaultComboBoxModel());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        DataHolder.getSingleton().initialize();
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    DataHolder.getSingleton().loadData(false);
                } catch (Exception e) {
                    logger.error("Failed loading data", e);
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
        DSWorkbenchMainFrame.getSingleton().serverSettingsChangedEvent();
        setVisible(false);
    }//GEN-LAST:event_fireCloseEvent

    private void fireOkEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireOkEvent
        if (!jOKButton.isEnabled()) {
            return;
        }

        String selection = (String) jTribeNames.getSelectedItem();
        if ((selection != null) && (!selection.equals("Bitte auswählen"))) {
            logger.debug("Setting default player for server '" + GlobalOptions.getSelectedServer() + "' to " + jTribeNames.getSelectedItem());
            GlobalOptions.addProperty("player." + GlobalOptions.getSelectedServer(), (String) jTribeNames.getSelectedItem());
        }
        GlobalOptions.saveProperties();

        if (!checkSettings()) {
            return;
        }

        DSWorkbenchMainFrame.getSingleton().serverSettingsChangedEvent();
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
            JOptionPane.showMessageDialog(this, "Keine Verbindung zur Datenbank.\nBitte überprüfe deine Netzwerkeinstellungen.", "Fehler", JOptionPane.ERROR_MESSAGE);
        } else if (ret == DatabaseAdapter.ID_USER_NOT_EXIST) {
            JOptionPane.showMessageDialog(this, "Der Benutzer '" + name + "' existiert nicht.\nBitte erstelle zuerst einen Account.", "Information", JOptionPane.INFORMATION_MESSAGE);
        } else if (ret == DatabaseAdapter.ID_WRONG_PASSWORD) {
            JOptionPane.showMessageDialog(this, "Das eingegebene Passwort ist falsch.\nBitte überprüfe die Eingaben.", "Fehler", JOptionPane.ERROR_MESSAGE);
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

    if (user.length() > 20) {
        JOptionPane.showMessageDialog(jCreateAccountDialog, "Der Accountname darf höchstens 20 Zeichen lang sein.", "Fehler", JOptionPane.INFORMATION_MESSAGE);
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
    MinimapPanel.getSingleton().resetBuffer();
    MinimapPanel.getSingleton().redraw();
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
        int serverDataVersion = DatabaseAdapter.getDataVersion(selectedServer);
        int userDataVersion = DatabaseAdapter.getUserDataVersion(name, selectedServer);
        if (userDataVersion == serverDataVersion) {
            JOptionPane.showMessageDialog(this, "Du besitzt bereits die aktuellsten Daten.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    GlobalOptions.setSelectedServer((String) jServerList.getSelectedItem());
    GlobalOptions.addProperty("default.server", GlobalOptions.getSelectedServer());
    GlobalOptions.saveProperties();
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
                DataHolder.getSingleton().loadData(true);
            } catch (Exception e) {
                logger.error("Failed to loading data", e);
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

private void fireChangeShowAttackMovementEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireChangeShowAttackMovementEvent
    GlobalOptions.addProperty("attack.movement", Boolean.toString(jShowAttackMovementBox.isSelected()));
    GlobalOptions.saveProperties();
}//GEN-LAST:event_fireChangeShowAttackMovementEvent

private void fireShowSkinPreviewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowSkinPreviewEvent
    String selection = (String) jGraphicPacks.getSelectedItem();
    try {
        if (selection != null) {
            Skin.showPreview(selection, evt.getLocationOnScreen());
        }
    } catch (Exception e) {
        logger.error("No preview available for selected skin '" + selection + "'");
    }
}//GEN-LAST:event_fireShowSkinPreviewEvent

private void fireChangeMarkOwnVillagesOnMinimapEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireChangeMarkOwnVillagesOnMinimapEvent
    GlobalOptions.addProperty("mark.villages.on.minimap", Boolean.toString(jMarkOwnVillagesOnMinimapBox.isSelected()));
    GlobalOptions.saveProperties();
    MinimapPanel.getSingleton().resetBuffer();
    MinimapPanel.getSingleton().redraw();
}//GEN-LAST:event_fireChangeMarkOwnVillagesOnMinimapEvent

private void fireChangeMarkActiveVillageEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireChangeMarkActiveVillageEvent
    GlobalOptions.addProperty("mark.active.village", Boolean.toString(jMarkActiveVillageBox.isSelected()));
    GlobalOptions.saveProperties();
}//GEN-LAST:event_fireChangeMarkActiveVillageEvent

    private boolean updateServerList(boolean pLocal) {
        String[] servers = null;
        if (!pLocal) {

            logger.debug("Checking general connectivity");
            try {
                URLConnection c = new URL("http://www.heise.de").openConnection();
                c.setConnectTimeout(10000);
                String header = c.getHeaderField(0);
                if (header != null) {
                    logger.debug("Connection established");
                    GlobalOptions.setOfflineMode(false);
                } else {
                    logger.warn("Could not establish connection");
                    GlobalOptions.setOfflineMode(true);
                }
            } catch (Exception in) {
                logger.error("Exception while opening connection", in);
                GlobalOptions.setOfflineMode(true);
            }

            if (!GlobalOptions.isOfflineMode()) {
                try {
                    ServerList.loadServerList();
                    servers = ServerList.getServerIDs();
                } catch (Exception e) {
                    logger.error("Failed to load server list", e);
                    GlobalOptions.setOfflineMode(true);
                }
            }
        }

        if (servers == null) {
            GlobalOptions.setOfflineMode(true);
            servers = DataHolder.getSingleton().getLocalServers();
        }

        if (servers.length < 1) {
            logger.error("No locally stored server found");
            jServerList.setModel(new DefaultComboBoxModel());
            jTribeNames.setModel(new DefaultComboBoxModel());
            return false;
        }

        Arrays.sort(servers);
        DefaultComboBoxModel model = new DefaultComboBoxModel(servers);

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
    private javax.swing.JLabel jAccountNameLabel;
    private javax.swing.JPasswordField jAccountPassword;
    private javax.swing.JLabel jAccountPasswordLabel;
    private javax.swing.JTable jAttackColorTable;
    private javax.swing.JLabel jAttackMovementLabel;
    private javax.swing.JLabel jAttackMovementLabel1;
    private javax.swing.JLabel jAttackMovementLabel2;
    private javax.swing.JLabel jAttackMovementLabel3;
    private javax.swing.JPanel jAttackSettings;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JButton jCancelRegistrationButton;
    private javax.swing.JButton jCheckAccountButton;
    private javax.swing.JCheckBox jContinentsOnMinimap;
    private javax.swing.JButton jCreateAccountButton;
    private javax.swing.JDialog jCreateAccountDialog;
    private javax.swing.JRadioButton jDirectConnectOption;
    private javax.swing.JButton jDownloadDataButton;
    private javax.swing.JComboBox jGraphicPacks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jLoginPanel;
    private javax.swing.JPanel jMapSettings;
    private javax.swing.JCheckBox jMarkActiveVillageBox;
    private javax.swing.JCheckBox jMarkOwnVillagesOnMinimapBox;
    private javax.swing.JPanel jNetworkSettings;
    private javax.swing.JButton jOKButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPlayerServerSettings;
    private javax.swing.JButton jPreviewSkinButton;
    private javax.swing.JLabel jProxyAdressLabel;
    private javax.swing.JRadioButton jProxyConnectOption;
    private javax.swing.JTextField jProxyHost;
    private javax.swing.JTextField jProxyPort;
    private javax.swing.JLabel jProxyPortLabel;
    private javax.swing.JButton jRefeshNetworkButton;
    private javax.swing.JButton jRegisterButton;
    private javax.swing.JLabel jRegisterNameLabel;
    private javax.swing.JLabel jRegisterPasswordLabel;
    private javax.swing.JTextField jRegistrationAccountName;
    private javax.swing.JPasswordField jRegistrationPassword;
    private javax.swing.JPasswordField jRegistrationPassword2;
    private javax.swing.JLabel jRepeatPasswordLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton jSelectServerButton;
    private javax.swing.JButton jSelectSkinButton;
    private javax.swing.JComboBox jServerList;
    private javax.swing.JCheckBox jShowAttackMovementBox;
    private javax.swing.JLabel jShowContinentsLabel;
    private javax.swing.JCheckBox jShowDistanceBox;
    private javax.swing.JLabel jShowDistanceLabel;
    private javax.swing.JLabel jSkinPackLabel;
    private javax.swing.JTextArea jStatusArea;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox jTribeNames;
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
    public void fireDataLoadedEvent(boolean pSuccess) {
        if (pSuccess) {
            fireDataHolderEvent("Erstelle Spielerliste");
            Collection<Tribe> tribes = DataHolder.getSingleton().getTribes().values();
            Tribe[] ta = tribes.toArray(new Tribe[]{});
            Arrays.sort(ta, Tribe.CASE_INSENSITIVE_ORDER);
            DefaultComboBoxModel model = new DefaultComboBoxModel();

            model.addElement("Bitte wählen");

            for (Tribe tribe : ta) {
                model.addElement(tribe.toString());
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
            if (DSWorkbenchMainFrame.getSingleton().isInitialized()) {
                DSWorkbenchMainFrame.getSingleton().serverSettingsChangedEvent();
            }
            fireDataHolderEvent("Lade Benutzerdaten");
            GlobalOptions.loadUserData();

            fireDataHolderEvent("Fertig");
        }
        updating = false;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        jSelectServerButton.setEnabled(true);
        jDownloadDataButton.setEnabled(true);
        jCreateAccountButton.setEnabled(true);
        jOKButton.setEnabled(true);
        jCancelButton.setEnabled(true);
    }
}


