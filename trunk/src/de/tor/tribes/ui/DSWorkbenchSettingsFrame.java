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
import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author  Jejkal
 */
public class DSWorkbenchSettingsFrame extends javax.swing.JFrame implements DataHolderListener {

    private static Logger logger = Logger.getLogger(DSWorkbenchSettingsFrame.class);
    private DSWorkbenchMainFrame mainFrame;

    /** Creates new form TribesPlannerStartFrame */
    public DSWorkbenchSettingsFrame() {
        initComponents();
        // jControlPanel.setupPanel(this, true, false);

        try {
            GlobalOptions.initialize(this);
        } catch (Exception e) {
            logger.error("Failed to initialize global options", e);
            JOptionPane.showMessageDialog(this, "Fehler bei der Initialisierung.\nMöglicherweise ist deine DSWorkBench Installation defekt.", "Fehler", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
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

        fireUpdateProxySettingsEvent(null);
//</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Account Setup">
        String name = GlobalOptions.getProperty("account.name");
        String password = GlobalOptions.getProperty("account.password");

        if ((name != null) && (password != null)) {
            jSavePassword.setEnabled(true);
            jAccountName.setText(name);
            jAccountPassword.setText(password);
        } else if (name != null) {
            jAccountName.setText(name);
        }
    //</editor-fold>
    }

    protected boolean checkSettings() {
        logger.debug("Checking settings");
        /*************************
         ***Check Account
         *************************/
        String name = GlobalOptions.getProperty("account.name");
        String password = GlobalOptions.getProperty("account.password");
        if (DatabaseAdapter.checkUser(name, password) != DatabaseAdapter.ID_SUCCESS) {
            logger.info("Account check failed (network or account error)");
            return false;
        }

        /*************************
         ***Check server and DS user
         *************************/
        String defaultServer = GlobalOptions.getProperty("default.server");
        String serverUser = GlobalOptions.getProperty("player." + defaultServer);

        if ((defaultServer == null) || (serverUser == null)) {
            logger.info("Either default server or default server player is not set");
            return false;
        }

        //all settings are OK
        return true;
    }

    public void setVisible(boolean v) {
        if (!checkSettings()) {
            super.setVisible(v);
        } else {
            runMainApplication();
        }
    }

    private void runMainApplication() {
        if (mainFrame == null) {
            java.awt.EventQueue.invokeLater(new  

                  Runnable() {

                       
                     
                        @Override
                public void run() {
                    mainFrame = new DSWorkbenchMainFrame();
                    try {
                        GlobalOptions.loadData(false);
                    } catch (Exception e) {
                        logger.error("Failed to load server data", e);
                        JOptionPane.showMessageDialog(null, "Fehler beim laden der Serverdaten");
                        System.exit(1);
                    }
                    GlobalOptions.loadUserData();
                    mainFrame.init();
                    mainFrame.setVisible(true);
                }
            });
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionTypeGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jLoginPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jAccountPassword = new javax.swing.JPasswordField();
        jAccountName = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jSavePassword = new javax.swing.JCheckBox();
        jPlayerServerSettings = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jServerList = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jTribeNames = new javax.swing.JComboBox();
        jSelectServerButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jStatusArea = new javax.swing.JTextArea();
        jGeneralSettings = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jGraphicPacks = new javax.swing.JComboBox();
        jButton4 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
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
        jButton6 = new javax.swing.JButton();

        setTitle("Einstellungen");
        setAlwaysOnTop(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fireClosingEvent(evt);
            }
        });

        jLabel6.setText("Name");

        jLabel7.setText("Passwort");

        jAccountPassword.setMaximumSize(new java.awt.Dimension(200, 20));
        jAccountPassword.setMinimumSize(new java.awt.Dimension(200, 20));
        jAccountPassword.setPreferredSize(new java.awt.Dimension(200, 20));

        jAccountName.setMaximumSize(new java.awt.Dimension(200, 20));
        jAccountName.setMinimumSize(new java.awt.Dimension(200, 20));
        jAccountName.setPreferredSize(new java.awt.Dimension(200, 20));

        jButton5.setText("Einloggen");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireLoginIntoAccountEvent(evt);
            }
        });

        jSavePassword.setText("Passwort speichern");
        jSavePassword.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChangeSavePasswordEvent(evt);
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
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jAccountName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAccountPassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jLoginPanelLayout.createSequentialGroup()
                        .addComponent(jSavePassword)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton5)))
                .addContainerGap(147, Short.MAX_VALUE))
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
                .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jSavePassword))
                .addContainerGap(163, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Login", new javax.swing.ImageIcon(getClass().getResource("/res/login.png")), jLoginPanel); // NOI18N

        jLabel1.setText("Server");

        jLabel2.setText("Spieler");

        jSelectServerButton.setText("Auswählen");
        jSelectServerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectServerEvent(evt);
            }
        });

        jStatusArea.setColumns(20);
        jStatusArea.setRows(5);
        jScrollPane1.setViewportView(jStatusArea);

        javax.swing.GroupLayout jPlayerServerSettingsLayout = new javax.swing.GroupLayout(jPlayerServerSettings);
        jPlayerServerSettings.setLayout(jPlayerServerSettingsLayout);
        jPlayerServerSettingsLayout.setHorizontalGroup(
            jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPlayerServerSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addGroup(jPlayerServerSettingsLayout.createSequentialGroup()
                        .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTribeNames, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jServerList, 0, 262, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSelectServerButton, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)))
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
                    .addComponent(jTribeNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Spieler/Server", new javax.swing.ImageIcon(getClass().getResource("/res/face.png")), jPlayerServerSettings); // NOI18N

        jLabel5.setText("Grafikpaket");

        jGraphicPacks.setMaximumSize(new java.awt.Dimension(120, 18));
        jGraphicPacks.setMinimumSize(new java.awt.Dimension(120, 18));
        jGraphicPacks.setPreferredSize(new java.awt.Dimension(120, 18));

        jButton4.setText("Auswählen");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectGraphicPackEvent(evt);
            }
        });

        jLabel8.setText("Automatischer Datenabgleich");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nie", "Bei Programmstart", "Stündlich", "Alle 2 Stunden", "Alle 4 Stunden", "Alle 12 Stunden", "Täglich" }));

        javax.swing.GroupLayout jGeneralSettingsLayout = new javax.swing.GroupLayout(jGeneralSettings);
        jGeneralSettings.setLayout(jGeneralSettingsLayout);
        jGeneralSettingsLayout.setHorizontalGroup(
            jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jGeneralSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, 0, 167, Short.MAX_VALUE)
                    .addComponent(jGraphicPacks, 0, 167, Short.MAX_VALUE))
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
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(189, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Allgemein", new javax.swing.ImageIcon(getClass().getResource("/res/settings.png")), jGeneralSettings); // NOI18N

        connectionTypeGroup.add(jDirectConnectOption);
        jDirectConnectOption.setText("Ich bin direkt mit dem Internet verbunden");
        jDirectConnectOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeConnectTypeEvent(evt);
            }
        });

        connectionTypeGroup.add(jProxyConnectOption);
        jProxyConnectOption.setSelected(true);
        jProxyConnectOption.setText("Ich benutze einen Proxy für den Internetzugang");
        jProxyConnectOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeConnectTypeEvent(evt);
            }
        });

        jLabel3.setText("Proxy Adresse");

        jProxyHost.setText("proxy.fzk.de");

        jLabel4.setText("Proxy Port");

        jProxyPort.setText("8000");
        jProxyPort.setMaximumSize(new java.awt.Dimension(40, 20));
        jProxyPort.setMinimumSize(new java.awt.Dimension(40, 20));
        jProxyPort.setPreferredSize(new java.awt.Dimension(40, 20));

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
                            .addComponent(jProxyHost, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
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
                .addContainerGap(107, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Netzwerk", new javax.swing.ImageIcon(getClass().getResource("/res/proxy.png")), jNetworkSettings); // NOI18N

        jOKButton.setText("OK");
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireOkEvent(evt);
            }
        });

        jCancelButton.setText("Abbrechen");
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseEvent(evt);
            }
        });

        jButton6.setText("Neuen Account erstellen");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
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
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 140, Short.MAX_VALUE)
                        .addComponent(jCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOKButton))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE))
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
                    .addComponent(jButton6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void fireUpdateProxySettingsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateProxySettingsEvent
        if (jDirectConnectOption.isSelected()) {
            System.getProperties().put("proxySet", "false");
        } else {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", jProxyHost.getText());
            System.getProperties().put("proxyPort", jProxyPort.getText());
        }

        GlobalOptions.addProperty("proxySet", Boolean.toString(jDirectConnectOption.isSelected()));
        GlobalOptions.addProperty("proxyHost", jProxyHost.getText());
        GlobalOptions.addProperty("proxyPort", jProxyPort.getText());
        GlobalOptions.saveProperties();
        try {
            ServerList.loadServerList();
            String[] list = ServerList.getServerIDs();
            Arrays.sort(list, null);
            DefaultComboBoxModel model = new DefaultComboBoxModel();

            for (String serverID : list) {
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
                        model.addElement("Bitte Server auswählen");
                        jTribeNames.setModel(model);
                        jTribeNames.setSelectedIndex(0);
                    }
                } else {
                    jServerList.setSelectedIndex(0);
                }
            } else {
                jServerList.setSelectedIndex(0);
            }



        } catch (Exception e) {
            String message = "Serverliste konnte nicht heruntergeladen werden.\nBitte überprüfe deine Netzwerkeinstellungen und klicke anschließend auf 'Aktualisieren'.";
            JOptionPane.showMessageDialog(this, message, "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_fireUpdateProxySettingsEvent

    private void fireChangeConnectTypeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeConnectTypeEvent
        jProxyHost.setEnabled(jProxyConnectOption.isSelected());
        jProxyPort.setEnabled(jProxyConnectOption.isSelected());
    }//GEN-LAST:event_fireChangeConnectTypeEvent

    private void fireSelectServerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectServerEvent
        if (!jSelectServerButton.isEnabled()) {
            return;
        }
        if (jServerList.getSelectedItem() == null) {
            return;
        }

        GlobalOptions.addProperty("default.server", (String) jServerList.getSelectedItem());
        GlobalOptions.saveProperties();
        GlobalOptions.setSelectedServer((String) jServerList.getSelectedItem());
        jSelectServerButton.setEnabled(false);
        updating = true;
        jStatusArea.setText("");
        jOKButton.setEnabled(false);
        jCancelButton.setEnabled(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jTribeNames.setModel(new DefaultComboBoxModel());
        new Thread(new  

              Runnable() {

                 
                    @Override
            public void run() {
                try {
                    GlobalOptions.loadData(false);
                } catch (Exception e) {
                }
            }
        }).start();
    }//GEN-LAST:event_fireSelectServerEvent

    private void fireCloseEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseEvent
        if (!jCancelButton.isEnabled()) {
            return;
        }
        if ((!checkSettings()) && (mainFrame == null)) {
            if (JOptionPane.showConfirmDialog(this, "Die Einstellungen sind fehlerhaft.\nDSWorkBench wirklich beenden?", "Fehler", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
        setVisible(false);
    }//GEN-LAST:event_fireCloseEvent

    private void fireOkEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireOkEvent
        if (!jOKButton.isEnabled()) {
            return;
        }
        String selection = (String) jTribeNames.getSelectedItem();
        if ((selection != null) && (!selection.equals("Bitte wählen"))) {
            logger.debug("Setting default player for server '" + GlobalOptions.getSelectedServer() + "' to " + jTribeNames.getSelectedItem());
            GlobalOptions.addProperty("player." + GlobalOptions.getSelectedServer(), (String) jTribeNames.getSelectedItem());
            GlobalOptions.saveProperties();
        }
        setVisible(false);
        runMainApplication();
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
// TODO add your handling code here:
}//GEN-LAST:event_fireCreateAccountEvent

private void fireLoginIntoAccountEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireLoginIntoAccountEvent
    String name = jAccountName.getText();
    String password = new String(jAccountPassword.getPassword());
    if ((name != null) && (password != null)) {
        int ret = DatabaseAdapter.checkUser(name, password);
        if (ret == DatabaseAdapter.ID_SUCCESS) {
            GlobalOptions.setLoggedInAs(name);
            GlobalOptions.addProperty("account.name", jAccountName.getText());
        } else if (ret == DatabaseAdapter.ID_CONNECTION_FAILED) {
            JOptionPane.showMessageDialog(this, "Keine Verbindung zur Datenbank.\nBitte überprüfe die Netzwerkeinstellungen.", "Fehler", JOptionPane.ERROR_MESSAGE);
        } else if (ret == DatabaseAdapter.ID_USER_NOT_EXIST) {
            JOptionPane.showMessageDialog(this, "Der Benutzer '" + name + "' existiert nicht.\nBitte erstelle einen neuen Account.", "Information", JOptionPane.INFORMATION_MESSAGE);
        } else if (ret == DatabaseAdapter.ID_UNKNOWN_ERROR) {
            JOptionPane.showMessageDialog(this, "Ein unbekannter Fehler ist aufgetreten.", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
        if (jSavePassword.isSelected()) {
            GlobalOptions.addProperty("account.password", new String(jAccountPassword.getPassword()));
            GlobalOptions.saveProperties();
        }
    }
}//GEN-LAST:event_fireLoginIntoAccountEvent

private void fireChangeSavePasswordEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChangeSavePasswordEvent
    if (jSavePassword.isSelected()) {
        GlobalOptions.addProperty("account.name", jAccountName.getText());
        GlobalOptions.addProperty("account.password", new String(jAccountPassword.getPassword()));
        GlobalOptions.saveProperties();
    }
}//GEN-LAST:event_fireChangeSavePasswordEvent

private void fireClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireClosingEvent
    fireCloseEvent(null);
}//GEN-LAST:event_fireClosingEvent

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        DOMConfigurator.configure("log4j.xml");

        java.awt.EventQueue.invokeLater(new  

              Runnable() {

                 public void run() {
                new DSWorkbenchSettingsFrame().setVisible(true);

            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup connectionTypeGroup;
    private javax.swing.JTextField jAccountName;
    private javax.swing.JPasswordField jAccountPassword;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JRadioButton jDirectConnectOption;
    private javax.swing.JPanel jGeneralSettings;
    private javax.swing.JComboBox jGraphicPacks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jLoginPanel;
    private javax.swing.JPanel jNetworkSettings;
    private javax.swing.JButton jOKButton;
    private javax.swing.JPanel jPlayerServerSettings;
    private javax.swing.JRadioButton jProxyConnectOption;
    private javax.swing.JTextField jProxyHost;
    private javax.swing.JTextField jProxyPort;
    private javax.swing.JCheckBox jSavePassword;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jSelectServerButton;
    private javax.swing.JComboBox jServerList;
    private javax.swing.JTextArea jStatusArea;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox jTribeNames;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireDataHolderEvent(String pMessage) {
        jStatusArea.insert(pMessage + "\n", jStatusArea.getText().length());
    }

    @Override
    public void fireDataLoadedEvent() {
        updating = false;
        jSelectServerButton.setEnabled(true);
        jOKButton.setEnabled(true);
        jCancelButton.setEnabled(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        String[] tribeNames = new String[GlobalOptions.getDataHolder().getTribes().size()];
        Enumeration<Integer> tribes = GlobalOptions.getDataHolder().getTribes().keys();
        int cnt = 0;
        while (tribes.hasMoreElements()) {
            tribeNames[cnt] = GlobalOptions.getDataHolder().getTribes().get(tribes.nextElement()).getName();
            cnt++;
        }
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
    }
}
