/*
 * TribesPlannerStartFrame.java
 *
 * Created on 9. Juni 2008, 15:54
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.ServerList;
import de.tor.tribes.util.GlobalOptions;
import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 *
 * @author  Jejkal
 */
public class TribesPlannerStartFrame extends javax.swing.JFrame implements DataHolderListener {

    private boolean updating = false;

    /** Creates new form TribesPlannerStartFrame */
    public TribesPlannerStartFrame() {
        initComponents();
        try {
            GlobalOptions.initialize(false, this);
        } catch (Exception e) {
        }

        //set skin settings
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

        //load network settings and update server/player settings
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
        jNetworkSettings = new javax.swing.JPanel();
        jDirectConnectOption = new javax.swing.JRadioButton();
        jProxyConnectOption = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jProxyHost = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jProxyPort = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Spieler/Server", jPlayerServerSettings);

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

        javax.swing.GroupLayout jGeneralSettingsLayout = new javax.swing.GroupLayout(jGeneralSettings);
        jGeneralSettings.setLayout(jGeneralSettingsLayout);
        jGeneralSettingsLayout.setHorizontalGroup(
            jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jGeneralSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jGraphicPacks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap(142, Short.MAX_VALUE))
        );
        jGeneralSettingsLayout.setVerticalGroup(
            jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jGeneralSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jGeneralSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jGraphicPacks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addContainerGap(219, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Allgemein", jGeneralSettings);

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
                .addContainerGap(111, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Netzwerk", jNetworkSettings);

        jButton1.setText("OK");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireOkEvent(evt);
            }
        });

        jButton3.setText("Abbrechen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseEvent(evt);
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
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
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
                    .addComponent(jButton1)
                    .addComponent(jButton3))
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
                    fireSelectServerEvent(null);
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

        new Thread(new Runnable() {

            public void run() {
                try {
                    GlobalOptions.loadData(false);
                } catch (Exception e) {
                }
            }
            }).start();
    }//GEN-LAST:event_fireSelectServerEvent

    private void fireCloseEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseEvent
        setVisible(false);
    }//GEN-LAST:event_fireCloseEvent

    private void fireOkEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireOkEvent
        GlobalOptions.addProperty("default.player", (String) jTribeNames.getSelectedItem());
        GlobalOptions.saveProperties();
        setVisible(false);        
    }//GEN-LAST:event_fireOkEvent

    private void fireSelectGraphicPackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectGraphicPackEvent
        GlobalOptions.addProperty("default.skin", (String) jGraphicPacks.getSelectedItem());
        try {
            GlobalOptions.loadSkin();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler beim laden des Grafikpaketes.");
            //load default
            GlobalOptions.addProperty("default.skin", "default");
            try {
                GlobalOptions.loadSkin();
            } catch (Exception ie) {
            }
        }
    }//GEN-LAST:event_fireSelectGraphicPackEvent

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TribesPlannerStartFrame().setVisible(true);

            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup connectionTypeGroup;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JRadioButton jDirectConnectOption;
    private javax.swing.JPanel jGeneralSettings;
    private javax.swing.JComboBox jGraphicPacks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jNetworkSettings;
    private javax.swing.JPanel jPlayerServerSettings;
    private javax.swing.JRadioButton jProxyConnectOption;
    private javax.swing.JTextField jProxyHost;
    private javax.swing.JTextField jProxyPort;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jSelectServerButton;
    private javax.swing.JComboBox jServerList;
    private javax.swing.JTextArea jStatusArea;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox jTribeNames;
    // End of variables declaration//GEN-END:variables
    public void fireDataHolderEvent(String pMessage) {
        jStatusArea.insert(pMessage + "\n", jStatusArea.getText().length());
    }

    public void fireDataLoadedEvent() {
        updating = false;
        jSelectServerButton.setEnabled(true);
        String[] tribeNames = new String[GlobalOptions.getDataHolder().getTribes().size()];
        Enumeration<Integer> tribes = GlobalOptions.getDataHolder().getTribes().keys();
        int cnt = 0;
        while (tribes.hasMoreElements()) {
            tribeNames[cnt] = GlobalOptions.getDataHolder().getTribes().get(tribes.nextElement()).getName();
            cnt++;
        }
        Arrays.sort(tribeNames, null);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String tribe : tribeNames) {
            model.addElement(tribe);
        }
        jTribeNames.setModel(model);
        if (GlobalOptions.getProperty("default.player") != null) {
            if (model.getIndexOf(GlobalOptions.getProperty("default.player")) != -1) {
                jTribeNames.setSelectedItem(GlobalOptions.getProperty("default.player"));
            } else {
                jTribeNames.setSelectedIndex(0);
            }
        } else {
            jTribeNames.setSelectedIndex(0);
        }
    }
}