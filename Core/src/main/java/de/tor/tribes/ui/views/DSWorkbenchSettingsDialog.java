/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.InvalidTribe;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.types.test.DummyUserProfile;
import de.tor.tribes.ui.editors.ColorChooserCellEditor;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.panels.MinimapPanel;
import de.tor.tribes.ui.renderer.ColorCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.wiz.red.ResourceDistributorWizard;
import de.tor.tribes.ui.wiz.tap.TacticsPlanerWizard;
import de.tor.tribes.util.*;
import de.tor.tribes.util.html.AttackPlanHTMLExporter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Torridity
 */
public class DSWorkbenchSettingsDialog extends javax.swing.JDialog implements
        DataHolderListener {

    private static Logger logger = Logger.getLogger("SettingsDialog");
    private static DSWorkbenchSettingsDialog SINGLETON = null;
    private boolean updating = false;
    private Proxy webProxy;
    private boolean INITIALIZED = false;
    private boolean isBlocked = false;

    public static synchronized DSWorkbenchSettingsDialog getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSettingsDialog();
        }

        return SINGLETON;
    }

    /**
     * Creates new form TribesPlannerStartFrame
     */
    DSWorkbenchSettingsDialog() {
        initComponents();
        GlobalOptions.addDataHolderListener(DSWorkbenchSettingsDialog.this);

        // <editor-fold defaultstate="collapsed" desc=" General Layout ">
        jTroopDensitySelectionDialog.pack();
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Network Setup">
        boolean useProxy = GlobalOptions.getProperties().getBoolean("proxySet");

        jDirectConnectOption.setSelected(!useProxy);
        jProxyConnectOption.setSelected(useProxy);

        //System.setProperty("proxyHost", GlobalOptions.getProperty("proxyHost"));
        jProxyHost.setText(GlobalOptions.getProperty("proxyHost"));

        //System.setProperty("proxyPort", GlobalOptions.getProperty("proxyPort"));
        jProxyPort.setText(GlobalOptions.getProperty("proxyPort"));

        // System.setProperty("proxyHost", GlobalOptions.getProperty("proxyHost"));
        jProxyTypeChooser.setSelectedIndex(GlobalOptions.getProperties().getInt("proxyType"));

        //System.setProperty("proxyPort", GlobalOptions.getProperty("proxyPort"));
        jProxyUser.setText(GlobalOptions.getProperty("proxyUser"));

        //System.setProperty("proxyPort", GlobalOptions.getProperty("proxyPort"));
        jProxyPassword.setText(GlobalOptions.getProperty("proxyPassword"));

        if (jProxyConnectOption.isSelected()) {
            SocketAddress addr = new InetSocketAddress(jProxyHost.getText(), Integer.parseInt(jProxyPort.getText()));
            switch (jProxyTypeChooser.getSelectedIndex()) {
                case 1: {
                    webProxy = new Proxy(Proxy.Type.SOCKS, addr);
                    break;
                }
                default: {
                    webProxy = new Proxy(Proxy.Type.HTTP, addr);
                    break;
                }
            }
            if ((jProxyUser.getText().length() >= 1) && (jProxyPassword.getPassword().length > 1)) {
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(jProxyUser.getText(), jProxyPassword.getPassword());
                    }
                });
            }
        } else {
            webProxy = Proxy.NO_PROXY;
        }

        //</editor-fold>
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelp(jPlayerServerSettings, "pages.player_server_settings", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelp(jMapSettings, "pages.map_settings", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelp(jAttackSettings, "pages.attack_settings", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelp(jNetworkSettings, "pages.network_settings", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelp(jTemplateSettings, "pages.template_settings", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelp(jMiscSettings, "pages.misc_settings", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.settings", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
    }

    public void restoreProperties() {
        //show popup moral
        jShowPopupMoral.setSelected(GlobalOptions.getProperties().getBoolean("show.popup.moral"));
        jShowPopupConquers.setSelected(GlobalOptions.getProperties().getBoolean("show.popup.conquers"));
        jShowPopupRanks.setSelected(GlobalOptions.getProperties().getBoolean("show.popup.ranks"));
        jShowPopupFarmSpace.setSelected(GlobalOptions.getProperties().getBoolean("show.popup.farm.space"));
        jShowContinents.setSelected(GlobalOptions.getProperties().getBoolean("map.showcontinents"));
        jShowSectorsBox.setSelected(GlobalOptions.getProperties().getBoolean("show.sectors"));
        jShowBarbarianBox.setSelected(GlobalOptions.getProperties().getBoolean("show.barbarian"));
        jMarkerTransparency.setValue(GlobalOptions.getProperties().getInt("map.marker.transparency"));
        jShowAttackMovementBox.setSelected(GlobalOptions.getProperties().getBoolean("attack.movement"));
        jDrawAttacksByDefaultBox.setSelected(GlobalOptions.getProperties().getBoolean("draw.attacks.by.default"));

        jShowLiveCountdown.setSelected(GlobalOptions.getProperties().getBoolean("show.live.countdown"));

        jExtendedAttackLineDrawing.setSelected(GlobalOptions.getProperties().getBoolean("extended.attack.vectors"));
        jHeaderPath.setText(GlobalOptions.getProperty("attack.template.header"));
        jBlockPath.setText(GlobalOptions.getProperty("attack.template.block"));
        jFooterPath.setText(GlobalOptions.getProperty("attack.template.footer"));
        //reload templates
        AttackPlanHTMLExporter.loadCustomTemplate();
        jMarkOwnVillagesOnMinimapBox.setSelected(GlobalOptions.getProperties().getBoolean("mark.villages.on.minimap"));
        jDefaultMarkBox.setSelectedIndex(GlobalOptions.getProperties().getInt("default.mark"));
        jCheckForUpdatesBox.setSelected(GlobalOptions.getProperties().getBoolean("check.updates.on.startup"));
        int villageOrder = GlobalOptions.getProperties().getInt("village.order");
        Village.setOrderType(villageOrder);
        jVillageSortTypeChooser.setSelectedIndex(villageOrder);
        jNotifyDurationBox.setSelectedIndex(GlobalOptions.getProperties().getInt("notify.duration"));
        jInformOnUpdates.setSelected(GlobalOptions.getProperties().getBoolean("inform.on.updates"));
        jMaxTroopDensity.setText(GlobalOptions.getProperty("max.density.troops"));
        jHalfSizeMainMenu.setSelected(GlobalOptions.getProperties().getBoolean("half.ribbon.size"));
        jClipboardSound.setSelected(GlobalOptions.getProperties().getBoolean("clipboard.notification"));
        jLabel24.setEnabled(SystrayHelper.isSystraySupported());
        jEnableSystray.setEnabled(SystrayHelper.isSystraySupported());
        jEnableSystray.setSelected(GlobalOptions.getProperties().getBoolean("systray.enabled"));
        jDeleteFarmReportsOnExit.setSelected(GlobalOptions.getProperties().getBoolean("delete.farm.reports.on.exit"));
        jMaxFarmSpace.setText(GlobalOptions.getProperty("max.farm.space"));
        jBrowserPath.setText(GlobalOptions.getProperty("default.browser"));
        jUseStandardBrowser.setSelected(jBrowserPath.getText().length() < 1);
        setOffense(new TroopAmountFixed(0).loadFromProperty(GlobalOptions.getProperty("standard.off")));
        setDefense(new TroopAmountFixed(0).loadFromProperty(GlobalOptions.getProperty("standard.defense.split")));
        jMaxSimRounds.setText(GlobalOptions.getProperty("max.sim.rounds"));
        jTolerance.setText(GlobalOptions.getProperty("support.tolerance"));
        jMaxLossRatio.setText(GlobalOptions.getProperty("max.loss.ratio"));
        jReportServerPort.setText(GlobalOptions.getProperty("report.server.port"));
        jObstServer.setText(GlobalOptions.getProperty("obst.server"));
    }

    private void setDefense(TroopAmountFixed pDefense) {
        jDefSpear.setText((pDefense.getAmountForUnit("spear") > 0) ? Integer.toString(pDefense.getAmountForUnit("spear")) : "500");
        jDefSword.setText((pDefense.getAmountForUnit("sword") > 0) ? Integer.toString(pDefense.getAmountForUnit("sword")) : "500");
        jDefArcher.setText((pDefense.getAmountForUnit("archer") > 0) ? Integer.toString(pDefense.getAmountForUnit("archer")) : "500");
        jDefSpy.setText((pDefense.getAmountForUnit("spy") > 0) ? Integer.toString(pDefense.getAmountForUnit("spy")) : "50");
        jDefHeavy.setText((pDefense.getAmountForUnit("heavy") > 0) ? Integer.toString(pDefense.getAmountForUnit("heavy")) : "0");
    }

    public TroopAmountFixed getDefense() {
        TroopAmountFixed result = new TroopAmountFixed();
        result.setAmountForUnit("spear", UIHelper.parseIntFromField(jDefSpear, 500));
        result.setAmountForUnit("sword", UIHelper.parseIntFromField(jDefSword, 500));
        result.setAmountForUnit("archer", UIHelper.parseIntFromField(jDefArcher, 500));
        result.setAmountForUnit("spy", UIHelper.parseIntFromField(jDefSpy, 50));
        result.setAmountForUnit("heavy", UIHelper.parseIntFromField(jDefHeavy, 0));
        
        return result;
    }

    private void setOffense(TroopAmountFixed pOffense) {
        jOffAxe.setText((pOffense.getAmountForUnit("axe") > 0) ? Integer.toString(pOffense.getAmountForUnit("axe")) : "7000");
        jOffLight.setText((pOffense.getAmountForUnit("light") > 0) ? Integer.toString(pOffense.getAmountForUnit("light")) : "3000");
        jOffMarcher.setText((pOffense.getAmountForUnit("marcher") > 0) ? Integer.toString(pOffense.getAmountForUnit("marcher")) : "500");
        jOffCata.setText((pOffense.getAmountForUnit("catapult") > 0) ? Integer.toString(pOffense.getAmountForUnit("catapult")) : "50");
        jOffRam.setText((pOffense.getAmountForUnit("ram") > 0) ? Integer.toString(pOffense.getAmountForUnit("ram")) : "300");
    }

    public TroopAmountFixed getOffense() {
        TroopAmountFixed result = new TroopAmountFixed();
        result.setAmountForUnit("axe", UIHelper.parseIntFromField(jOffAxe, 7000));
        result.setAmountForUnit("light", UIHelper.parseIntFromField(jOffLight, 3000));
        result.setAmountForUnit("marcher", UIHelper.parseIntFromField(jOffMarcher, 500));
        result.setAmountForUnit("catapult", UIHelper.parseIntFromField(jOffCata, 50));
        result.setAmountForUnit("ram", UIHelper.parseIntFromField(jOffRam, 300));
        
        return result;
    }

    public Proxy getWebProxy() {
        if (webProxy == null) {
            return Proxy.NO_PROXY;
        }
        return webProxy;
    }

    public void setupAttackColorTable() {
        jAttackColorTable.invalidate();
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
        jAttackColorTable.setDefaultEditor(Color.class, new ColorChooserCellEditor(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //not needed
            }
        }));

        jAttackColorTable.setModel(model);
        jAttackColorTable.getColumnModel().getColumn(1).setMaxWidth(75);
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            String hexColor = GlobalOptions.getProperty(unit.getName() + ".color");
            try {
                Color col = Color.decode(hexColor);
                model.addRow(new Object[]{unit, col});
            } catch (Exception e) {
                logger.warn("Failed to decode color " + hexColor + ". Switch to default");
                hexColor = Integer.toHexString(Color.RED.getRGB());
                hexColor = "#" + hexColor.substring(2, hexColor.length());
                GlobalOptions.addProperty(unit.getName() + ".color", hexColor);
                model.addRow(new Object[]{unit, Color.RED});
            }
        }

        jAttackColorTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jAttackColorTable.revalidate();
    }

    @Override
    public void setVisible(boolean pValue) {
        if (!INITIALIZED) {
            if (!DataHolder.getSingleton().getUnits().isEmpty()) {
                setupAttackColorTable();
                INITIALIZED = true;
            } else {
                //units not loaded yet
            }
        }
        try {
            super.setVisible(pValue);
        } catch (Exception e) {
            logger.debug("IGNORE: Exception while changing visibility", e);
        }
    }

    public void setBlocking(boolean pValue) {
        isBlocked = pValue;

        if (pValue) {
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            jCancelButton.setEnabled(false);
        } else {
            setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            jCancelButton.setEnabled(true);
        }
    }

    public boolean checkSettings() {
        logger.debug("Checking settings");
        checkConnectivity();
        if (!updateServerList()) {
            //remote update failed and no local servers found
            String message = "Serverliste konnte nicht geladen werden.\n"
                    + "Mögliche Ursachen sind fehlerhafte Netzwerkeinstellungen, keine Verbindung zum Internet\n"
                    + "oder die Stämme Server sind nicht verfügbar.\n"
                    + "Da noch kein Datenabgleich mit dem Server stattgefunden hat\n"
                    + "korrigiere bitte deine Netzwerkeinstellungen oder versuche es später noch einmal.";
            JOptionPaneHelper.showWarningBox(this, message, "Warnung");
            return false;
        }

        return checkTribesAccountSettings();
    }

    //check server and player settings
    private boolean checkServerPlayerSettings() {
        boolean result = false;
        if (!GlobalOptions.getProperties().exists("default.player")
                || !GlobalOptions.getProperties().exists("default.server")) {
            UserProfile selection = null;
            try {
                selection = (UserProfile) jProfileBox.getSelectedItem();
                result = true;
            } catch (Exception e) {
                logger.error("Failed to get selected profile", e);
            }
            //check if selection is valid
            if (selection != null) {
                //set default user for server
                GlobalOptions.addProperty("default.player", Long.toString(selection.getProfileId()));
                GlobalOptions.setSelectedServer(selection.getServerId());
                GlobalOptions.addProperty("default.server", selection.getServerId());
                result = true;
            } else {
                //no default user selected
                logger.error("No profile selected");
            }
        } else {
            String defaultUser = GlobalOptions.getProperty("default.player");
            //check if profile is valid
            UserProfile[] profiles = ProfileManager.getSingleton()
                    .getProfiles(GlobalOptions.getProperty("default.server"));
            for (UserProfile profile : profiles) {
                try {
                    if (profile.getProfileId() == Long.parseLong(defaultUser)) {
                        result = true;
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    logger.error("Failed to get profile for id '" + defaultUser + "'");
                }
            }

            if (!result) {
                //profile was probably removed. Get selected entry
                UserProfile selection = null;
                try {
                    selection = (UserProfile) jProfileBox.getSelectedItem();
                    if (selection != null) {
                        //set default user for server
                        GlobalOptions.addProperty("default.player", Long.toString(selection.getProfileId()));
                        result = true;
                    }
                } catch (Exception e) {
                    logger.error("Failed to get selected profile", e);
                }
            }
        }
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        connectionTypeGroup = new javax.swing.ButtonGroup();
        tagMarkerGroup = new javax.swing.ButtonGroup();
        jTroopDensitySelectionDialog = new javax.swing.JDialog();
        jDeffStrengthOKButton = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jSpearAmount = new com.jidesoft.swing.LabeledTextField();
        jSwordAmount = new com.jidesoft.swing.LabeledTextField();
        jArcherAmount = new com.jidesoft.swing.LabeledTextField();
        jHeavyAmount = new com.jidesoft.swing.LabeledTextField();
        jSettingsTabbedPane = new javax.swing.JTabbedPane();
        jPlayerServerSettings = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jDownloadLiveDataButton = new javax.swing.JButton();
        jCheckForUpdatesBox = new javax.swing.JCheckBox();
        jLabelServer = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jProfileBox = new javax.swing.JComboBox();
        jNewProfileButton = new javax.swing.JButton();
        jModifyProfileButton = new javax.swing.JButton();
        jDeleteProfileButton = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jStatusArea = new javax.swing.JTextArea();
        jMapSettings = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jShowContinents = new javax.swing.JCheckBox();
        jShowSectorsBox = new javax.swing.JCheckBox();
        jMarkOwnVillagesOnMinimapBox = new javax.swing.JCheckBox();
        jShowBarbarianBox = new javax.swing.JCheckBox();
        jMarkerTransparency = new javax.swing.JSlider();
        jPanel2 = new javax.swing.JPanel();
        jShowContinentsLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jAttackMovementLabel2 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jDefaultMarkBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jShowPopupRanks = new javax.swing.JCheckBox();
        jShowPopupConquers = new javax.swing.JCheckBox();
        jShowPopupMoral = new javax.swing.JCheckBox();
        jShowPopupFarmSpace = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jMaxFarmSpace = new javax.swing.JTextField();
        jAttackSettings = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jAttackMovementLabel = new javax.swing.JLabel();
        jShowAttackMovementBox = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAttackColorTable = new javax.swing.JTable();
        jAttackMovementLabel3 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jDrawAttacksByDefaultBox = new javax.swing.JCheckBox();
        jLabel21 = new javax.swing.JLabel();
        jShowLiveCountdown = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        jExtendedAttackLineDrawing = new javax.swing.JCheckBox();
        jDefenseSettings = new javax.swing.JPanel();
        jSingleSupportPanel = new javax.swing.JPanel();
        jDefSpear = new com.jidesoft.swing.LabeledTextField();
        jDefSpy = new com.jidesoft.swing.LabeledTextField();
        jDefSword = new com.jidesoft.swing.LabeledTextField();
        jDefHeavy = new com.jidesoft.swing.LabeledTextField();
        jDefArcher = new com.jidesoft.swing.LabeledTextField();
        jXLabel2 = new org.jdesktop.swingx.JXLabel();
        jButton4 = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jMaxSimRounds = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jMaxLossRatio = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jTolerance = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jStandardAttackerPanel = new javax.swing.JPanel();
        jOffAxe = new com.jidesoft.swing.LabeledTextField();
        jOffLight = new com.jidesoft.swing.LabeledTextField();
        jOffMarcher = new com.jidesoft.swing.LabeledTextField();
        jOffCata = new com.jidesoft.swing.LabeledTextField();
        jOffRam = new com.jidesoft.swing.LabeledTextField();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jButton3 = new javax.swing.JButton();
        jNetworkSettings = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jUseStandardBrowser = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jBrowserPath = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jXLabel3 = new org.jdesktop.swingx.JXLabel();
        jPanel8 = new javax.swing.JPanel();
        jDirectConnectOption = new javax.swing.JRadioButton();
        jProxyConnectOption = new javax.swing.JRadioButton();
        jProxyAdressLabel = new javax.swing.JLabel();
        jProxyHost = new javax.swing.JTextField();
        jProxyPortLabel = new javax.swing.JLabel();
        jProxyPort = new javax.swing.JTextField();
        jRefeshNetworkButton = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jProxyTypeChooser = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jProxyUser = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jProxyPassword = new javax.swing.JPasswordField();
        jTemplateSettings = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jHeaderPath = new javax.swing.JTextField();
        jBlockPath = new javax.swing.JTextField();
        jFooterPath = new javax.swing.JTextField();
        jSelectHeaderButton = new javax.swing.JButton();
        jSelectBlockButton = new javax.swing.JButton();
        jSelectFooterButton = new javax.swing.JButton();
        jRestoreHeaderButton = new javax.swing.JButton();
        jRestoreBlockButton = new javax.swing.JButton();
        jRestoreFooterButton = new javax.swing.JButton();
        jMiscSettings = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jVillageSortTypeChooser = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jNotifyDurationBox = new javax.swing.JComboBox();
        jInformOnUpdates = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jMaxTroopDensity = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jHalfSizeMainMenu = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jClipboardSound = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        jDeleteFarmReportsOnExit = new javax.swing.JCheckBox();
        jLabel24 = new javax.swing.JLabel();
        jEnableSystray = new javax.swing.JCheckBox();
        jPanel13 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jReportServerPort = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        jObstServer = new javax.swing.JTextField();
        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();

        jTroopDensitySelectionDialog.setTitle("Deff-Anzahl angeben");
        jTroopDensitySelectionDialog.setModal(true);
        jTroopDensitySelectionDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        jDeffStrengthOKButton.setText("OK");
        jDeffStrengthOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAcceptDeffStrengthEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopDensitySelectionDialog.getContentPane().add(jDeffStrengthOKButton, gridBagConstraints);

        jButton12.setText("Abbrechen");
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAcceptDeffStrengthEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopDensitySelectionDialog.getContentPane().add(jButton12, gridBagConstraints);

        jSpearAmount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/spear.png"))); // NOI18N
        jSpearAmount.setLabelText("");
        jSpearAmount.setPreferredSize(new java.awt.Dimension(215, 24));
        jSpearAmount.setText("8000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopDensitySelectionDialog.getContentPane().add(jSpearAmount, gridBagConstraints);

        jSwordAmount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/sword.png"))); // NOI18N
        jSwordAmount.setLabelText("");
        jSwordAmount.setPreferredSize(new java.awt.Dimension(215, 24));
        jSwordAmount.setText("7000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopDensitySelectionDialog.getContentPane().add(jSwordAmount, gridBagConstraints);

        jArcherAmount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/archer.png"))); // NOI18N
        jArcherAmount.setLabelText("");
        jArcherAmount.setPreferredSize(new java.awt.Dimension(215, 24));
        jArcherAmount.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopDensitySelectionDialog.getContentPane().add(jArcherAmount, gridBagConstraints);

        jHeavyAmount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/heavy.png"))); // NOI18N
        jHeavyAmount.setLabelText("");
        jHeavyAmount.setPreferredSize(new java.awt.Dimension(215, 24));
        jHeavyAmount.setText("1000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopDensitySelectionDialog.getContentPane().add(jHeavyAmount, gridBagConstraints);

        setTitle("Einstellungen");
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fireClosingEvent(evt);
            }
        });

        jSettingsTabbedPane.setBackground(new java.awt.Color(239, 235, 223));
        jSettingsTabbedPane.setPreferredSize(new java.awt.Dimension(620, 400));

        jPlayerServerSettings.setBackground(new java.awt.Color(239, 235, 223));
        jPlayerServerSettings.setPreferredSize(new java.awt.Dimension(620, 400));

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Server"));
        jPanel9.setOpaque(false);

        jDownloadLiveDataButton.setBackground(new java.awt.Color(239, 235, 223));
        jDownloadLiveDataButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/download_tw.png"))); // NOI18N
        jDownloadLiveDataButton.setToolTipText("Daten des markierten Servers direkt von den DS Servern laden");
        jDownloadLiveDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireDownloadLiveDataEvent(evt);
            }
        });

        jCheckForUpdatesBox.setText("Beim Start auf Updates prüfen");
        jCheckForUpdatesBox.setToolTipText("Prüft bei jedem Start von DS Workbench auf aktuelle Weltdaten");
        jCheckForUpdatesBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireCheckForUpdatesEvent(evt);
            }
        });

        jLabelServer.setText("de");
        jLabelServer.setToolTipText("Gewählter Server");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDownloadLiveDataButton)
                            .addComponent(jCheckForUpdatesBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelServer)
                .addGap(18, 18, 18)
                .addComponent(jDownloadLiveDataButton)
                .addGap(18, 18, 18)
                .addComponent(jCheckForUpdatesBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Profil"));
        jPanel10.setOpaque(false);

        jProfileBox.setMinimumSize(new java.awt.Dimension(23, 25));
        jProfileBox.setPreferredSize(new java.awt.Dimension(28, 25));
        jProfileBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireSelectProfile(evt);
            }
        });

        jNewProfileButton.setBackground(new java.awt.Color(239, 235, 223));
        jNewProfileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/id_card_new.png"))); // NOI18N
        jNewProfileButton.setToolTipText("Neues Profil erstellen");
        jNewProfileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireProfileActionEvent(evt);
            }
        });

        jModifyProfileButton.setBackground(new java.awt.Color(239, 235, 223));
        jModifyProfileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/id_card_edit.png"))); // NOI18N
        jModifyProfileButton.setToolTipText("Gewähltes Profil bearbeiten");
        jModifyProfileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireProfileActionEvent(evt);
            }
        });

        jDeleteProfileButton.setBackground(new java.awt.Color(239, 235, 223));
        jDeleteProfileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/id_card_delete.png"))); // NOI18N
        jDeleteProfileButton.setToolTipText("Gewähltes Profil löschen");
        jDeleteProfileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireProfileActionEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProfileBox, 0, 300, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jNewProfileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jModifyProfileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDeleteProfileButton)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jProfileBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jNewProfileButton)
                    .addComponent(jModifyProfileButton)
                    .addComponent(jDeleteProfileButton))
                .addContainerGap(52, Short.MAX_VALUE))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Informationen"));
        jPanel11.setOpaque(false);

        jStatusArea.setEditable(false);
        jStatusArea.setColumns(20);
        jStatusArea.setRows(5);
        jScrollPane1.setViewportView(jStatusArea);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPlayerServerSettingsLayout = new javax.swing.GroupLayout(jPlayerServerSettings);
        jPlayerServerSettings.setLayout(jPlayerServerSettingsLayout);
        jPlayerServerSettingsLayout.setHorizontalGroup(
            jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPlayerServerSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPlayerServerSettingsLayout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPlayerServerSettingsLayout.setVerticalGroup(
            jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPlayerServerSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPlayerServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSettingsTabbedPane.addTab("Spieler/Server", new javax.swing.ImageIcon(getClass().getResource("/res/face.png")), jPlayerServerSettings); // NOI18N

        jMapSettings.setBackground(new java.awt.Color(239, 235, 223));
        jMapSettings.setPreferredSize(new java.awt.Dimension(620, 400));

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jShowContinents.setToolTipText("Anzeiger der Kontinente auf der Minimap");
        jShowContinents.setContentAreaFilled(false);
        jShowContinents.setMaximumSize(new java.awt.Dimension(25, 25));
        jShowContinents.setMinimumSize(new java.awt.Dimension(25, 25));
        jShowContinents.setPreferredSize(new java.awt.Dimension(25, 25));
        jShowContinents.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChangeContinentsOnMinimapEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jShowContinents, gridBagConstraints);

        jShowSectorsBox.setToolTipText("Sektoren in Hauptkarte einzeichnen");
        jShowSectorsBox.setMaximumSize(new java.awt.Dimension(25, 25));
        jShowSectorsBox.setMinimumSize(new java.awt.Dimension(25, 25));
        jShowSectorsBox.setPreferredSize(new java.awt.Dimension(25, 25));
        jShowSectorsBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChangeShowSectorsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jShowSectorsBox, gridBagConstraints);

        jMarkOwnVillagesOnMinimapBox.setToolTipText("Markiert die Dörfer des aktuellen Spielers auf der Minimap");
        jMarkOwnVillagesOnMinimapBox.setMaximumSize(new java.awt.Dimension(25, 25));
        jMarkOwnVillagesOnMinimapBox.setMinimumSize(new java.awt.Dimension(25, 25));
        jMarkOwnVillagesOnMinimapBox.setPreferredSize(new java.awt.Dimension(25, 25));
        jMarkOwnVillagesOnMinimapBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireChangeMarkOwnVillagesOnMinimapEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jMarkOwnVillagesOnMinimapBox, gridBagConstraints);

        jShowBarbarianBox.setToolTipText("Anzeige von Barbarendörfern auf der Karte");
        jShowBarbarianBox.setMaximumSize(new java.awt.Dimension(25, 25));
        jShowBarbarianBox.setMinimumSize(new java.awt.Dimension(25, 25));
        jShowBarbarianBox.setPreferredSize(new java.awt.Dimension(25, 25));
        jShowBarbarianBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowBarbarianChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jShowBarbarianBox, gridBagConstraints);

        jMarkerTransparency.setMajorTickSpacing(10);
        jMarkerTransparency.setMinimum(10);
        jMarkerTransparency.setMinorTickSpacing(1);
        jMarkerTransparency.setPaintLabels(true);
        jMarkerTransparency.setPaintTicks(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jMarkerTransparency, gridBagConstraints);

        jPanel2.setMaximumSize(new java.awt.Dimension(190, 125));
        jPanel2.setMinimumSize(new java.awt.Dimension(190, 125));
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(190, 125));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jShowContinentsLabel.setText("Kontinente anzeigen");
        jShowContinentsLabel.setMaximumSize(new java.awt.Dimension(190, 25));
        jShowContinentsLabel.setMinimumSize(new java.awt.Dimension(190, 25));
        jShowContinentsLabel.setPreferredSize(new java.awt.Dimension(190, 25));
        jPanel2.add(jShowContinentsLabel);

        jLabel7.setText("Sektoren anzeigen");
        jLabel7.setMaximumSize(new java.awt.Dimension(190, 25));
        jLabel7.setMinimumSize(new java.awt.Dimension(190, 25));
        jLabel7.setPreferredSize(new java.awt.Dimension(190, 25));
        jPanel2.add(jLabel7);

        jAttackMovementLabel2.setText("Eigene Dörfer auf Minimap");
        jAttackMovementLabel2.setMaximumSize(new java.awt.Dimension(190, 25));
        jAttackMovementLabel2.setMinimumSize(new java.awt.Dimension(190, 25));
        jAttackMovementLabel2.setPreferredSize(new java.awt.Dimension(190, 25));
        jPanel2.add(jAttackMovementLabel2);

        jLabel17.setText("Barbarendörfer anzeigen");
        jLabel17.setMaximumSize(new java.awt.Dimension(190, 25));
        jLabel17.setMinimumSize(new java.awt.Dimension(190, 25));
        jLabel17.setPreferredSize(new java.awt.Dimension(190, 25));
        jPanel2.add(jLabel17);

        jLabel30.setText("Deckkraft von Markierungen [%]");
        jLabel30.setMaximumSize(new java.awt.Dimension(190, 25));
        jLabel30.setMinimumSize(new java.awt.Dimension(190, 25));
        jLabel30.setPreferredSize(new java.awt.Dimension(190, 25));
        jPanel2.add(jLabel30);

        jLabel4.setText("Standardmarkierung");
        jLabel4.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel4.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 25));

        jDefaultMarkBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DS 6.0", "Rot", "Weiß" }));
        jDefaultMarkBox.setToolTipText("Standardfarbe von Dorfmarkierungen");
        jDefaultMarkBox.setMinimumSize(new java.awt.Dimension(52, 25));
        jDefaultMarkBox.setPreferredSize(new java.awt.Dimension(57, 25));
        jDefaultMarkBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireStandardMarkChangedEvent(evt);
            }
        });

        jLabel3.setText("Popupoptionen");
        jLabel3.setMaximumSize(new java.awt.Dimension(97, 25));
        jLabel3.setMinimumSize(new java.awt.Dimension(97, 25));
        jLabel3.setPreferredSize(new java.awt.Dimension(97, 25));

        jPanel5.setOpaque(false);
        jPanel5.setLayout(new java.awt.GridLayout(5, 0));

        jShowPopupRanks.setText("Erweiterte Informationen anzeigen");
        jShowPopupRanks.setToolTipText("Anzeige von Gesamtpunkten und Platzierungen für Spieler und Stamm");
        jShowPopupRanks.setMaximumSize(new java.awt.Dimension(193, 25));
        jShowPopupRanks.setMinimumSize(new java.awt.Dimension(193, 25));
        jShowPopupRanks.setPreferredSize(new java.awt.Dimension(193, 25));
        jPanel5.add(jShowPopupRanks);

        jShowPopupConquers.setText("Besiegte Gegner anzeigen");
        jShowPopupConquers.setToolTipText("Besiegte Gegner des Spielers im Angriff und der Verteididung anzeigen");
        jShowPopupConquers.setMaximumSize(new java.awt.Dimension(193, 25));
        jShowPopupConquers.setMinimumSize(new java.awt.Dimension(193, 25));
        jShowPopupConquers.setPreferredSize(new java.awt.Dimension(193, 25));
        jPanel5.add(jShowPopupConquers);

        jShowPopupMoral.setText("Moral anzeigen");
        jShowPopupMoral.setToolTipText("Moral anzeigen");
        jShowPopupMoral.setMaximumSize(new java.awt.Dimension(193, 25));
        jShowPopupMoral.setMinimumSize(new java.awt.Dimension(193, 25));
        jShowPopupMoral.setPreferredSize(new java.awt.Dimension(193, 25));
        jShowPopupMoral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowPopupMoralActionPerformed(evt);
            }
        });
        jPanel5.add(jShowPopupMoral);

        jShowPopupFarmSpace.setText("Bauernhof Füllstand anzeigen");
        jShowPopupFarmSpace.setToolTipText("Füllstand des Bauernhofes eines Dorfes anzeigen");
        jShowPopupFarmSpace.setMaximumSize(new java.awt.Dimension(193, 25));
        jShowPopupFarmSpace.setMinimumSize(new java.awt.Dimension(193, 25));
        jShowPopupFarmSpace.setPreferredSize(new java.awt.Dimension(193, 25));
        jPanel5.add(jShowPopupFarmSpace);

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.X_AXIS));

        jLabel22.setText("Max. Bauernhofplätze");
        jLabel22.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel22.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel22.setPreferredSize(new java.awt.Dimension(150, 25));
        jPanel3.add(jLabel22);

        jMaxFarmSpace.setText("20000");
        jMaxFarmSpace.setToolTipText("Anzahl der durch Truppen belegten Bauernhofplätze, bei der ein Dorf zu 100% gefüllt ist");
        jMaxFarmSpace.setMinimumSize(new java.awt.Dimension(6, 25));
        jMaxFarmSpace.setPreferredSize(new java.awt.Dimension(36, 25));
        jPanel3.add(jMaxFarmSpace);

        jPanel5.add(jPanel3);

        javax.swing.GroupLayout jMapSettingsLayout = new javax.swing.GroupLayout(jMapSettings);
        jMapSettings.setLayout(jMapSettingsLayout);
        jMapSettingsLayout.setHorizontalGroup(
            jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMapSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(36, 36, 36)
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addComponent(jDefaultMarkBox, 0, 390, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                .addContainerGap())
        );
        jMapSettingsLayout.setVerticalGroup(
            jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMapSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDefaultMarkBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jMapSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(176, 176, 176))
        );

        jSettingsTabbedPane.addTab("Karten", new javax.swing.ImageIcon(getClass().getResource("/res/ui/map.gif")), jMapSettings); // NOI18N

        jAttackSettings.setBackground(new java.awt.Color(239, 235, 223));
        jAttackSettings.setPreferredSize(new java.awt.Dimension(620, 400));

        jPanel12.setMinimumSize(new java.awt.Dimension(600, 300));
        jPanel12.setOpaque(false);
        jPanel12.setPreferredSize(new java.awt.Dimension(500, 300));
        jPanel12.setLayout(new java.awt.GridBagLayout());

        jAttackMovementLabel.setText("Truppenbewegung anzeigen");
        jAttackMovementLabel.setMaximumSize(new java.awt.Dimension(260, 25));
        jAttackMovementLabel.setMinimumSize(new java.awt.Dimension(260, 25));
        jAttackMovementLabel.setPreferredSize(new java.awt.Dimension(260, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jAttackMovementLabel, gridBagConstraints);

        jShowAttackMovementBox.setToolTipText("Anzeige der Truppenbewegungen von Befehlen auf der Karte");
        jShowAttackMovementBox.setMaximumSize(new java.awt.Dimension(21, 25));
        jShowAttackMovementBox.setMinimumSize(new java.awt.Dimension(21, 25));
        jShowAttackMovementBox.setPreferredSize(new java.awt.Dimension(21, 25));
        jShowAttackMovementBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireChangeShowAttackMovementEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jShowAttackMovementBox, gridBagConstraints);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(300, 200));
        jScrollPane2.setOpaque(false);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(300, 200));

        jAttackColorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jAttackColorTable.setOpaque(false);
        jScrollPane2.setViewportView(jAttackColorTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jScrollPane2, gridBagConstraints);

        jAttackMovementLabel3.setText("Färbung der Befehlsvektoren");
        jAttackMovementLabel3.setMaximumSize(new java.awt.Dimension(260, 25));
        jAttackMovementLabel3.setMinimumSize(new java.awt.Dimension(260, 25));
        jAttackMovementLabel3.setPreferredSize(new java.awt.Dimension(260, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jAttackMovementLabel3, gridBagConstraints);

        jLabel9.setText("Neue Befehle auf der Karte einzeichnen");
        jLabel9.setMaximumSize(new java.awt.Dimension(280, 25));
        jLabel9.setMinimumSize(new java.awt.Dimension(280, 25));
        jLabel9.setPreferredSize(new java.awt.Dimension(280, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jLabel9, gridBagConstraints);

        jDrawAttacksByDefaultBox.setToolTipText("Neue erstellte Befehle sofort auf der Karte einzeichnen");
        jDrawAttacksByDefaultBox.setMaximumSize(new java.awt.Dimension(21, 25));
        jDrawAttacksByDefaultBox.setMinimumSize(new java.awt.Dimension(21, 25));
        jDrawAttacksByDefaultBox.setPreferredSize(new java.awt.Dimension(21, 25));
        jDrawAttacksByDefaultBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireDrawAttacksByDefaultChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jDrawAttacksByDefaultBox, gridBagConstraints);

        jLabel21.setText("Countdown in der Befehlsübersicht anzeigen");
        jLabel21.setMaximumSize(new java.awt.Dimension(260, 25));
        jLabel21.setMinimumSize(new java.awt.Dimension(260, 25));
        jLabel21.setPreferredSize(new java.awt.Dimension(260, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jLabel21, gridBagConstraints);

        jShowLiveCountdown.setToolTipText("<html>Zeigt den Live-Countdown in der Befehlssübersicht an oder blendet ihn aus.<BR/>Deaktiviere diese Option wenn es mit der Performance Probleme gibt</html>");
        jShowLiveCountdown.setMaximumSize(new java.awt.Dimension(21, 25));
        jShowLiveCountdown.setMinimumSize(new java.awt.Dimension(21, 25));
        jShowLiveCountdown.setPreferredSize(new java.awt.Dimension(21, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jShowLiveCountdown, gridBagConstraints);

        jLabel23.setText("Laufrichtung für eingezeichnete Befehle anzeigen");
        jLabel23.setMaximumSize(new java.awt.Dimension(260, 25));
        jLabel23.setMinimumSize(new java.awt.Dimension(260, 25));
        jLabel23.setPreferredSize(new java.awt.Dimension(260, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jLabel23, gridBagConstraints);

        jExtendedAttackLineDrawing.setToolTipText("<html>Zeigt die Laufrichtung der Truppen vom Herkunftsdorf zum Ziel an.<BR/>Diese Option sollte deaktiviert sein wenn du viele Befehle einzeichnen möchtest, da sie starken Einfluss auf die Performance hat</html>");
        jExtendedAttackLineDrawing.setMaximumSize(new java.awt.Dimension(21, 25));
        jExtendedAttackLineDrawing.setMinimumSize(new java.awt.Dimension(21, 25));
        jExtendedAttackLineDrawing.setPreferredSize(new java.awt.Dimension(21, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(jExtendedAttackLineDrawing, gridBagConstraints);

        javax.swing.GroupLayout jAttackSettingsLayout = new javax.swing.GroupLayout(jAttackSettings);
        jAttackSettings.setLayout(jAttackSettingsLayout);
        jAttackSettingsLayout.setHorizontalGroup(
            jAttackSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                .addContainerGap())
        );
        jAttackSettingsLayout.setVerticalGroup(
            jAttackSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSettingsTabbedPane.addTab("Angriffe", new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jAttackSettings); // NOI18N

        jDefenseSettings.setBackground(new java.awt.Color(239, 235, 223));
        jDefenseSettings.setLayout(new java.awt.GridBagLayout());

        jSingleSupportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Einzelunterstützung im Verteidigungsplaner"));
        jSingleSupportPanel.setOpaque(false);
        jSingleSupportPanel.setLayout(new java.awt.GridBagLayout());

        jDefSpear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/spear.png"))); // NOI18N
        jDefSpear.setText("500");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSingleSupportPanel.add(jDefSpear, gridBagConstraints);

        jDefSpy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/spy.png"))); // NOI18N
        jDefSpy.setText("50");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSingleSupportPanel.add(jDefSpy, gridBagConstraints);

        jDefSword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/sword.png"))); // NOI18N
        jDefSword.setText("500");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSingleSupportPanel.add(jDefSword, gridBagConstraints);

        jDefHeavy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/heavy.png"))); // NOI18N
        jDefHeavy.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSingleSupportPanel.add(jDefHeavy, gridBagConstraints);

        jDefArcher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/archer.png"))); // NOI18N
        jDefArcher.setText("500");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSingleSupportPanel.add(jDefArcher, gridBagConstraints);

        jXLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jXLabel2.setText("Trage hier die Truppenstärke einer Einzelverteidigung ein. Beachte dabei, dass von diesen Einstellungen die Analyse von SOS-Anfragen abhängt. Änderst du die Einstellungen, musst du alle Verteidigungen manuell erneut analysieren und berechnen.");
        jXLabel2.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jXLabel2.setLineWrap(true);
        jXLabel2.setPreferredSize(new java.awt.Dimension(400, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSingleSupportPanel.add(jXLabel2, gridBagConstraints);

        jButton4.setText("Übernehmen");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeDefEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSingleSupportPanel.add(jButton4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDefenseSettings.add(jSingleSupportPanel, gridBagConstraints);

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Sonstige Einstellungen"));
        jPanel15.setOpaque(false);
        jPanel15.setLayout(new java.awt.GridBagLayout());

        jLabel25.setText("Max. Simulationsrunden");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel15.add(jLabel25, gridBagConstraints);

        jMaxSimRounds.setText("500");
        jMaxSimRounds.setToolTipText("Die maximale Anzahl an Simulationsrunden, bevor die Simulation abgebrochen wird");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel15.add(jMaxSimRounds, gridBagConstraints);

        jLabel26.setText("Max. Verlustrate");
        jLabel26.setMaximumSize(new java.awt.Dimension(114, 14));
        jLabel26.setMinimumSize(new java.awt.Dimension(114, 14));
        jLabel26.setPreferredSize(new java.awt.Dimension(114, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel15.add(jLabel26, gridBagConstraints);

        jMaxLossRatio.setText("50");
        jMaxLossRatio.setToolTipText("Die maximale Verlustrate der Verteidiger, die man akzeptieren möchte");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel15.add(jMaxLossRatio, gridBagConstraints);

        jLabel27.setText("%");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel15.add(jLabel27, gridBagConstraints);

        jLabel28.setText("Toleranz für Einzelunterstützungen");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel15.add(jLabel28, gridBagConstraints);

        jTolerance.setText("10");
        jTolerance.setToolTipText("Toleranz in Prozent, um welche eine Einzelunterstützung abweichen darf");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel15.add(jTolerance, gridBagConstraints);

        jLabel29.setText("%");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel15.add(jLabel29, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDefenseSettings.add(jPanel15, gridBagConstraints);

        jStandardAttackerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Angreifer im Verteidigungsplaner"));
        jStandardAttackerPanel.setOpaque(false);
        jStandardAttackerPanel.setLayout(new java.awt.GridBagLayout());

        jOffAxe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/axe.png"))); // NOI18N
        jOffAxe.setText("7000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jStandardAttackerPanel.add(jOffAxe, gridBagConstraints);

        jOffLight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/light.png"))); // NOI18N
        jOffLight.setText("3000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jStandardAttackerPanel.add(jOffLight, gridBagConstraints);

        jOffMarcher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/marcher.png"))); // NOI18N
        jOffMarcher.setText("500");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jStandardAttackerPanel.add(jOffMarcher, gridBagConstraints);

        jOffCata.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/catapult.png"))); // NOI18N
        jOffCata.setText("50");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jStandardAttackerPanel.add(jOffCata, gridBagConstraints);

        jOffRam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/ram.png"))); // NOI18N
        jOffRam.setText("300");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jStandardAttackerPanel.add(jOffRam, gridBagConstraints);

        jXLabel1.setForeground(new java.awt.Color(102, 102, 102));
        jXLabel1.setText("Die hier eingestellten Truppen sollten in der Regel beibehalten werden, da sie eine recht aussagekräftige Off darstellen. Minimale Änderungen beeinflussen das Ergebnis bei der Verteidigungsplanung ohnehin nur unwesentlich.");
        jXLabel1.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jXLabel1.setLineWrap(true);
        jXLabel1.setPreferredSize(new java.awt.Dimension(300, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jStandardAttackerPanel.add(jXLabel1, gridBagConstraints);

        jButton3.setText("Übernehmen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeOffEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jStandardAttackerPanel.add(jButton3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDefenseSettings.add(jStandardAttackerPanel, gridBagConstraints);

        jSettingsTabbedPane.addTab("Verteidigung", new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jDefenseSettings); // NOI18N

        jNetworkSettings.setBackground(new java.awt.Color(239, 235, 223));
        jNetworkSettings.setPreferredSize(new java.awt.Dimension(620, 400));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Browser"));
        jPanel4.setMaximumSize(new java.awt.Dimension(400, 126));
        jPanel4.setMinimumSize(new java.awt.Dimension(400, 126));
        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(400, 126));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jUseStandardBrowser.setSelected(true);
        jUseStandardBrowser.setText("Standardbrowser verwenden (Bei Problemen bitte Browser als 'Alternativen Browser' angeben)");
        jUseStandardBrowser.setToolTipText("<html>DS Workbench versucht den Standardbrowser deines Systems zu verwenden.<br/>\nIn manchen F&auml;llen kann es hierbei zu Problemen kommen. Gib dann bitte deinen bevorzugten Browser unter \"Alternativer Browser\" an.</html>");
        jUseStandardBrowser.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeDefaultBrowserEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jUseStandardBrowser, gridBagConstraints);

        jLabel5.setText("Alternativer Browser");
        jLabel5.setEnabled(false);
        jLabel5.setMaximumSize(new java.awt.Dimension(120, 23));
        jLabel5.setMinimumSize(new java.awt.Dimension(120, 23));
        jLabel5.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jLabel5, gridBagConstraints);

        jBrowserPath.setEnabled(false);
        jBrowserPath.setMinimumSize(new java.awt.Dimension(6, 23));
        jBrowserPath.setPreferredSize(new java.awt.Dimension(6, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jBrowserPath, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setText("...");
        jButton1.setEnabled(false);
        jButton1.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectBrowserEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jButton1, gridBagConstraints);

        jXLabel3.setForeground(new java.awt.Color(102, 102, 102));
        jXLabel3.setText("Solltest du einen alternativen Browser verwenden wollen, achte unbedingt darauf, dass der Browser gestartet ist wenn du die Einstellungen testest. Ansonsten kann es dazu kommen, dass DS Workbench nicht mehr reagiert.");
        jXLabel3.setToolTipText("");
        jXLabel3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jXLabel3.setLineWrap(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jXLabel3, gridBagConstraints);

        jPanel8.setOpaque(false);
        jPanel8.setLayout(new java.awt.GridBagLayout());

        connectionTypeGroup.add(jDirectConnectOption);
        jDirectConnectOption.setSelected(true);
        jDirectConnectOption.setText("Ich bin direkt mit dem Internet verbunden");
        jDirectConnectOption.setMaximumSize(new java.awt.Dimension(259, 23));
        jDirectConnectOption.setMinimumSize(new java.awt.Dimension(259, 23));
        jDirectConnectOption.setPreferredSize(new java.awt.Dimension(259, 23));
        jDirectConnectOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeConnectTypeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jDirectConnectOption, gridBagConstraints);

        connectionTypeGroup.add(jProxyConnectOption);
        jProxyConnectOption.setText("Ich benutze einen Proxy für den Internetzugang");
        jProxyConnectOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeConnectTypeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jProxyConnectOption, gridBagConstraints);

        jProxyAdressLabel.setText("Proxy Adresse");
        jProxyAdressLabel.setMaximumSize(new java.awt.Dimension(100, 23));
        jProxyAdressLabel.setMinimumSize(new java.awt.Dimension(100, 23));
        jProxyAdressLabel.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jProxyAdressLabel, gridBagConstraints);

        jProxyHost.setToolTipText("Adresse des Proxy Servers");
        jProxyHost.setEnabled(false);
        jProxyHost.setMinimumSize(new java.awt.Dimension(6, 23));
        jProxyHost.setPreferredSize(new java.awt.Dimension(6, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jProxyHost, gridBagConstraints);

        jProxyPortLabel.setText("Proxy Port");
        jProxyPortLabel.setMaximumSize(new java.awt.Dimension(70, 23));
        jProxyPortLabel.setMinimumSize(new java.awt.Dimension(70, 23));
        jProxyPortLabel.setPreferredSize(new java.awt.Dimension(70, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jProxyPortLabel, gridBagConstraints);

        jProxyPort.setToolTipText("Port des Proxy Servers");
        jProxyPort.setEnabled(false);
        jProxyPort.setMaximumSize(new java.awt.Dimension(40, 23));
        jProxyPort.setMinimumSize(new java.awt.Dimension(40, 23));
        jProxyPort.setPreferredSize(new java.awt.Dimension(40, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jProxyPort, gridBagConstraints);

        jRefeshNetworkButton.setBackground(new java.awt.Color(239, 235, 223));
        jRefeshNetworkButton.setText("Aktualisieren");
        jRefeshNetworkButton.setToolTipText("Netzwerkeinstellungen aktualisieren und prüfen");
        jRefeshNetworkButton.setMaximumSize(new java.awt.Dimension(120, 23));
        jRefeshNetworkButton.setMinimumSize(new java.awt.Dimension(120, 23));
        jRefeshNetworkButton.setPreferredSize(new java.awt.Dimension(120, 23));
        jRefeshNetworkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateProxySettingsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jRefeshNetworkButton, gridBagConstraints);

        jLabel10.setText("Proxy Typ");
        jLabel10.setMaximumSize(new java.awt.Dimension(100, 23));
        jLabel10.setMinimumSize(new java.awt.Dimension(100, 23));
        jLabel10.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jLabel10, gridBagConstraints);

        jProxyTypeChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "HTTP", "SOCKS" }));
        jProxyTypeChooser.setToolTipText("Art des Proxy Servers");
        jProxyTypeChooser.setEnabled(false);
        jProxyTypeChooser.setMinimumSize(new java.awt.Dimension(100, 23));
        jProxyTypeChooser.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jProxyTypeChooser, gridBagConstraints);

        jLabel11.setText("Benutzername");
        jLabel11.setMaximumSize(new java.awt.Dimension(100, 23));
        jLabel11.setMinimumSize(new java.awt.Dimension(100, 23));
        jLabel11.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jLabel11, gridBagConstraints);

        jProxyUser.setToolTipText("Benutzername zur Authentifizierung beim Proxy Server");
        jProxyUser.setEnabled(false);
        jProxyUser.setMaximumSize(new java.awt.Dimension(150, 23));
        jProxyUser.setMinimumSize(new java.awt.Dimension(150, 23));
        jProxyUser.setPreferredSize(new java.awt.Dimension(150, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jProxyUser, gridBagConstraints);

        jLabel12.setText("Passwort");
        jLabel12.setMaximumSize(new java.awt.Dimension(100, 23));
        jLabel12.setMinimumSize(new java.awt.Dimension(100, 23));
        jLabel12.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jLabel12, gridBagConstraints);

        jProxyPassword.setToolTipText("Passwort zur Authentifizierung beim Proxy Server");
        jProxyPassword.setEnabled(false);
        jProxyPassword.setMaximumSize(new java.awt.Dimension(150, 23));
        jProxyPassword.setMinimumSize(new java.awt.Dimension(150, 23));
        jProxyPassword.setPreferredSize(new java.awt.Dimension(150, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jProxyPassword, gridBagConstraints);

        javax.swing.GroupLayout jNetworkSettingsLayout = new javax.swing.GroupLayout(jNetworkSettings);
        jNetworkSettings.setLayout(jNetworkSettingsLayout);
        jNetworkSettingsLayout.setHorizontalGroup(
            jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jNetworkSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE))
                .addContainerGap())
        );
        jNetworkSettingsLayout.setVerticalGroup(
            jNetworkSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jNetworkSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSettingsTabbedPane.addTab("Netzwerk", new javax.swing.ImageIcon(getClass().getResource("/res/proxy.png")), jNetworkSettings); // NOI18N

        jTemplateSettings.setBackground(new java.awt.Color(239, 235, 223));
        jTemplateSettings.setPreferredSize(new java.awt.Dimension(620, 400));

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("HTML Templates (Angriffsexport)"));
        jPanel7.setOpaque(false);
        jPanel7.setLayout(new java.awt.GridBagLayout());

        jLabel6.setText("Header");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jLabel6, gridBagConstraints);

        jLabel18.setText("Angriffsblock");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jLabel18, gridBagConstraints);

        jLabel19.setText("Footer");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jLabel19, gridBagConstraints);

        jHeaderPath.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jHeaderPath, gridBagConstraints);

        jBlockPath.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jBlockPath, gridBagConstraints);

        jFooterPath.setText("<Standard>");
        jFooterPath.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jFooterPath, gridBagConstraints);

        jSelectHeaderButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectHeaderButton.setText("...");
        jSelectHeaderButton.setToolTipText("Template wählen");
        jSelectHeaderButton.setMaximumSize(new java.awt.Dimension(25, 23));
        jSelectHeaderButton.setMinimumSize(new java.awt.Dimension(25, 23));
        jSelectHeaderButton.setPreferredSize(new java.awt.Dimension(25, 23));
        jSelectHeaderButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectTemplateEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jSelectHeaderButton, gridBagConstraints);

        jSelectBlockButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectBlockButton.setText("...");
        jSelectBlockButton.setToolTipText("Template wählen");
        jSelectBlockButton.setMaximumSize(new java.awt.Dimension(25, 23));
        jSelectBlockButton.setMinimumSize(new java.awt.Dimension(25, 23));
        jSelectBlockButton.setPreferredSize(new java.awt.Dimension(25, 23));
        jSelectBlockButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectTemplateEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jSelectBlockButton, gridBagConstraints);

        jSelectFooterButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectFooterButton.setText("...");
        jSelectFooterButton.setToolTipText("Template wählen");
        jSelectFooterButton.setMaximumSize(new java.awt.Dimension(25, 23));
        jSelectFooterButton.setMinimumSize(new java.awt.Dimension(25, 23));
        jSelectFooterButton.setPreferredSize(new java.awt.Dimension(25, 23));
        jSelectFooterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectTemplateEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jSelectFooterButton, gridBagConstraints);

        jRestoreHeaderButton.setBackground(new java.awt.Color(239, 235, 223));
        jRestoreHeaderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jRestoreHeaderButton.setToolTipText("Standard wiederherstellen");
        jRestoreHeaderButton.setAlignmentY(0.0F);
        jRestoreHeaderButton.setMaximumSize(new java.awt.Dimension(25, 23));
        jRestoreHeaderButton.setMinimumSize(new java.awt.Dimension(25, 23));
        jRestoreHeaderButton.setPreferredSize(new java.awt.Dimension(25, 23));
        jRestoreHeaderButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRestoreTemplateEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jRestoreHeaderButton, gridBagConstraints);

        jRestoreBlockButton.setBackground(new java.awt.Color(239, 235, 223));
        jRestoreBlockButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jRestoreBlockButton.setToolTipText("Standard wiederherstellen");
        jRestoreBlockButton.setMaximumSize(new java.awt.Dimension(25, 23));
        jRestoreBlockButton.setMinimumSize(new java.awt.Dimension(25, 23));
        jRestoreBlockButton.setPreferredSize(new java.awt.Dimension(25, 23));
        jRestoreBlockButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRestoreTemplateEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jRestoreBlockButton, gridBagConstraints);

        jRestoreFooterButton.setBackground(new java.awt.Color(239, 235, 223));
        jRestoreFooterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jRestoreFooterButton.setToolTipText("Standard wiederherstellen");
        jRestoreFooterButton.setMaximumSize(new java.awt.Dimension(25, 23));
        jRestoreFooterButton.setMinimumSize(new java.awt.Dimension(25, 23));
        jRestoreFooterButton.setPreferredSize(new java.awt.Dimension(25, 23));
        jRestoreFooterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRestoreTemplateEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel7.add(jRestoreFooterButton, gridBagConstraints);

        javax.swing.GroupLayout jTemplateSettingsLayout = new javax.swing.GroupLayout(jTemplateSettings);
        jTemplateSettings.setLayout(jTemplateSettingsLayout);
        jTemplateSettingsLayout.setHorizontalGroup(
            jTemplateSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTemplateSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                .addContainerGap())
        );
        jTemplateSettingsLayout.setVerticalGroup(
            jTemplateSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTemplateSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(367, Short.MAX_VALUE))
        );

        jSettingsTabbedPane.addTab("Templates", new javax.swing.ImageIcon(getClass().getResource("/res/ui/component.png")), jTemplateSettings); // NOI18N

        jMiscSettings.setBackground(new java.awt.Color(239, 235, 223));
        jMiscSettings.setPreferredSize(new java.awt.Dimension(620, 400));

        jPanel6.setOpaque(false);
        jPanel6.setLayout(new java.awt.GridBagLayout());

        jVillageSortTypeChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alphabetisch", "Nach Koordinaten" }));
        jVillageSortTypeChooser.setToolTipText("Art der Dorfsortierung in DS Workbench");
        jVillageSortTypeChooser.setMaximumSize(new java.awt.Dimension(105, 18));
        jVillageSortTypeChooser.setPreferredSize(new java.awt.Dimension(105, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jVillageSortTypeChooser, gridBagConstraints);

        jLabel13.setText("Dorfsortierung");
        jLabel13.setMaximumSize(new java.awt.Dimension(138, 18));
        jLabel13.setMinimumSize(new java.awt.Dimension(138, 18));
        jLabel13.setPreferredSize(new java.awt.Dimension(138, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jLabel13, gridBagConstraints);

        jLabel14.setText("Anzeigedauer von Hinweisen");
        jLabel14.setMaximumSize(new java.awt.Dimension(138, 18));
        jLabel14.setMinimumSize(new java.awt.Dimension(138, 18));
        jLabel14.setPreferredSize(new java.awt.Dimension(138, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jLabel14, gridBagConstraints);

        jNotifyDurationBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Unbegrenzt", "10 Sekunden", "20 Sekunden", "30 Sekunden" }));
        jNotifyDurationBox.setSelectedIndex(1);
        jNotifyDurationBox.setToolTipText("Zeitdauer nach der Hinweise in der rechten unteren Bildschirmecke automatisch ausgeblendet werden");
        jNotifyDurationBox.setMaximumSize(new java.awt.Dimension(105, 18));
        jNotifyDurationBox.setMinimumSize(new java.awt.Dimension(105, 18));
        jNotifyDurationBox.setPreferredSize(new java.awt.Dimension(105, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jNotifyDurationBox, gridBagConstraints);

        jInformOnUpdates.setSelected(true);
        jInformOnUpdates.setToolTipText("Prüfung auf DS Workbench Updates bei jedem Programmstart\\n");
        jInformOnUpdates.setMaximumSize(new java.awt.Dimension(105, 18));
        jInformOnUpdates.setMinimumSize(new java.awt.Dimension(105, 18));
        jInformOnUpdates.setPreferredSize(new java.awt.Dimension(105, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jInformOnUpdates, gridBagConstraints);

        jLabel15.setText("Über Updates  informieren");
        jLabel15.setMaximumSize(new java.awt.Dimension(138, 18));
        jLabel15.setMinimumSize(new java.awt.Dimension(138, 18));
        jLabel15.setPreferredSize(new java.awt.Dimension(138, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jLabel15, gridBagConstraints);

        jLabel16.setText("<HTML>Max. Deff-Anzahl für die<BR/>Berechnung der Truppendichte</HTML>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jLabel16, gridBagConstraints);

        jMaxTroopDensity.setText("650000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jMaxTroopDensity, gridBagConstraints);

        jButton8.setBackground(new java.awt.Color(239, 235, 223));
        jButton8.setText("Auswählen");
        jButton8.setToolTipText("Setzt die Truppenstärke anhand angegebener Deff-Werte");
        jButton8.setMaximumSize(new java.awt.Dimension(90, 23));
        jButton8.setMinimumSize(new java.awt.Dimension(90, 23));
        jButton8.setPreferredSize(new java.awt.Dimension(90, 23));
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectTroopsDensityEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jButton8, gridBagConstraints);

        jLabel2.setText("Hauptmenü in halber Größe anzeigen");
        jLabel2.setMaximumSize(new java.awt.Dimension(34, 18));
        jLabel2.setMinimumSize(new java.awt.Dimension(34, 18));
        jLabel2.setPreferredSize(new java.awt.Dimension(34, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jLabel2, gridBagConstraints);

        jHalfSizeMainMenu.setToolTipText("<html>Zeigt das Hauptmen&uuml; in halber Gr&ouml;sse an.<br/>Dies Option kann verwendet werden, um z.B. bei Monitoren mit kleiner Aufl&ouml;sung Platz zu sparen.</html>");
        jHalfSizeMainMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowHalfSizeMainMenuEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jHalfSizeMainMenu, gridBagConstraints);

        jLabel8.setText("Hinweis bei gelesenen Clipboard-Daten");
        jLabel8.setMaximumSize(new java.awt.Dimension(34, 18));
        jLabel8.setMinimumSize(new java.awt.Dimension(34, 18));
        jLabel8.setPreferredSize(new java.awt.Dimension(34, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jLabel8, gridBagConstraints);

        jClipboardSound.setSelected(true);
        jClipboardSound.setToolTipText("Spielt einen Ton ab, wenn Spieldaten (Berichte, Übersichten...) aus der Zwischenablage importiert wurden");
        jClipboardSound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireEnableClipboardNotificationEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jClipboardSound, gridBagConstraints);

        jLabel20.setText("Farmberichte beim Beenden löschen");
        jLabel20.setMaximumSize(new java.awt.Dimension(34, 18));
        jLabel20.setMinimumSize(new java.awt.Dimension(34, 18));
        jLabel20.setPreferredSize(new java.awt.Dimension(34, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jLabel20, gridBagConstraints);

        jDeleteFarmReportsOnExit.setSelected(true);
        jDeleteFarmReportsOnExit.setToolTipText("Farmberichte werden beim Beenden von DS Workbench automatisch gelöscht (empfohlen)");
        jDeleteFarmReportsOnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireDeleteFarmReportsOnExitEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jDeleteFarmReportsOnExit, gridBagConstraints);

        jLabel24.setText("Systray Benachrichtigungen aktivieren");
        jLabel24.setMaximumSize(new java.awt.Dimension(34, 18));
        jLabel24.setMinimumSize(new java.awt.Dimension(34, 18));
        jLabel24.setPreferredSize(new java.awt.Dimension(34, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jLabel24, gridBagConstraints);

        jEnableSystray.setSelected(true);
        jEnableSystray.setToolTipText("Öffnet eine Nachricht im Systray (sofern unterstützt) wenn Spieldaten (Berichte, Übersichten...) aus der Zwischenablage gelesen wurden");
        jEnableSystray.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireEnableSystrayEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel6.add(jEnableSystray, gridBagConstraints);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Berichtserver"));
        jPanel13.setOpaque(false);
        jPanel13.setPreferredSize(new java.awt.Dimension(72, 50));
        jPanel13.setLayout(new java.awt.GridBagLayout());

        jLabel31.setText("Port");
        jLabel31.setMaximumSize(new java.awt.Dimension(50, 14));
        jLabel31.setMinimumSize(new java.awt.Dimension(50, 14));
        jLabel31.setPreferredSize(new java.awt.Dimension(50, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel13.add(jLabel31, gridBagConstraints);

        jReportServerPort.setText("8080");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel13.add(jReportServerPort, gridBagConstraints);

        jButton5.setText("Neustart");
        jButton5.setToolTipText("Startet den Berichtserver nach Veränderung der Porteinstellung neu");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireRestartReportServerEvent(evt);
            }
        });
        jPanel13.add(jButton5, new java.awt.GridBagConstraints());

        jLabel32.setText("OBST Server");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel13.add(jLabel32, gridBagConstraints);

        jObstServer.setToolTipText("OBST Server, an den gelesene Berichte ebenfalls weitergeleitet werden");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel13.add(jObstServer, gridBagConstraints);

        javax.swing.GroupLayout jMiscSettingsLayout = new javax.swing.GroupLayout(jMiscSettings);
        jMiscSettings.setLayout(jMiscSettingsLayout);
        jMiscSettingsLayout.setHorizontalGroup(
            jMiscSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jMiscSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMiscSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE))
                .addContainerGap())
        );
        jMiscSettingsLayout.setVerticalGroup(
            jMiscSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMiscSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSettingsTabbedPane.addTab("Sonstiges", new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png")), jMiscSettings); // NOI18N

        jOKButton.setBackground(new java.awt.Color(239, 235, 223));
        jOKButton.setText("OK");
        jOKButton.setToolTipText("Einstellungen übernehmen und speichern");
        jOKButton.setMaximumSize(new java.awt.Dimension(90, 25));
        jOKButton.setMinimumSize(new java.awt.Dimension(90, 25));
        jOKButton.setPreferredSize(new java.awt.Dimension(90, 25));
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireOkEvent(evt);
            }
        });

        jCancelButton.setBackground(new java.awt.Color(239, 235, 223));
        jCancelButton.setText("Abbrechen");
        jCancelButton.setToolTipText("Einstellungen verwerfen");
        jCancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
        jCancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
        jCancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
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
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jCancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOKButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSettingsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSettingsTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 536, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jOKButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void fireUpdateProxySettingsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateProxySettingsEvent

        if (jProxyConnectOption.isSelected()) {
            //store properties
            GlobalOptions.addProperty("proxySet", Boolean.toString(true));
            GlobalOptions.addProperty("proxyHost", jProxyHost.getText());
            GlobalOptions.addProperty("proxyPort", jProxyPort.getText());
            GlobalOptions.addProperty("proxyType", Integer.toBinaryString(jProxyTypeChooser.getSelectedIndex()));
            GlobalOptions.addProperty("proxyUser", jProxyUser.getText());
            GlobalOptions.addProperty("proxyPassword", new String(jProxyPassword.getPassword()));
            //create proxy object
            SocketAddress addr = new InetSocketAddress(jProxyHost.getText(), Integer.parseInt(jProxyPort.getText()));
            switch (jProxyTypeChooser.getSelectedIndex()) {
                case 1: {
                    webProxy = new Proxy(Proxy.Type.SOCKS, addr);
                    break;
                }
                default: {
                    webProxy = new Proxy(Proxy.Type.HTTP, addr);
                    break;
                }
            }
            if ((jProxyUser.getText().length() >= 1) && (jProxyPassword.getPassword().length > 1)) {
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(jProxyUser.getText(), jProxyPassword.getPassword());
                    }
                });
            }
        } else {
            //store properties
            GlobalOptions.addProperty("proxySet", Boolean.toString(false));
            GlobalOptions.removeProperty("proxyHost");
            GlobalOptions.removeProperty("proxyPort");
            GlobalOptions.removeProperty("proxyType");
            GlobalOptions.removeProperty("proxyUser");
            GlobalOptions.removeProperty("proxyPassword");
            //set no proxy and no authentification
            Authenticator.setDefault(null);
            webProxy = Proxy.NO_PROXY;
        }

        GlobalOptions.saveProperties();

        checkConnectivity();

        boolean offlineBefore = GlobalOptions.isOfflineMode();

        if (!updateServerList()) {
            //fully failed --> remote update failed and no local servers found
            String message = "Serverliste konnte nicht geladen werden.\n"
                    + "Mögliche Ursachen sind fehlerhafte Netzwerkeinstellungen, keine Verbindung zum Internet\n"
                    + "oder 'google.com' ist nicht verfügbar.\n"
                    + "Da noch kein Datenabgleich mit dem Server stattgefunden hat "
                    + "korrigiere bitte deine Netzwerkeinstellungen um diesen einmalig durchzuführen.";
            JOptionPaneHelper.showWarningBox(this, message, "Warnung");
        } else {
            String message = null;
            String title = "Fehler";
            int type = JOptionPane.ERROR_MESSAGE;
            if (offlineBefore) {
                //was offline before checking serverlist
                message = "Die Prüfung der Verbindung zum Internet ist fehlgeschlagen.\n"
                        + "Da du bereits Serverdaten besitzt werden diese verwendet. Für ein Update\n"
                        + "prüfe bitte erneut deine Verbindung zum Internet und deine Netzwerkeinstellungen.";
            } else if (GlobalOptions.isOfflineMode()) {
                //get offline while checking serverlist
                message = "Die Prüfung der Verbindung zum Internet war erfolgreich,\n"
                        + "es konnte dennoch keine aktuelle Serverliste heruntergeladen werden.\n"
                        + "Bitte versuch es später noch einmal.";
            } else {
                //success
                message = "Verbindung erfolgreich hergestellt.";
                title = "Information";
                type = JOptionPane.INFORMATION_MESSAGE;
            }

            //show box
            if (type == JOptionPane.INFORMATION_MESSAGE) {
                JOptionPaneHelper.showInformationBox(this, message, title);
            } else {
                JOptionPaneHelper.showErrorBox(this, message, title);
            }

        }

        DSWorkbenchMainFrame.getSingleton().onlineStateChanged();
    }//GEN-LAST:event_fireUpdateProxySettingsEvent

    private void fireChangeConnectTypeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeConnectTypeEvent
        jProxyHost.setEnabled(jProxyConnectOption.isSelected());
        jProxyPort.setEnabled(jProxyConnectOption.isSelected());
        jProxyUser.setEnabled(jProxyConnectOption.isSelected());
        jProxyPassword.setEnabled(jProxyConnectOption.isSelected());
        jProxyTypeChooser.setEnabled(jProxyConnectOption.isSelected());
    }//GEN-LAST:event_fireChangeConnectTypeEvent

    private void fireCloseEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseEvent
        if (!jCancelButton.isEnabled()) {
            return;
        }

        if (!checkTribesAccountSettings()) {
            return;
        }

        DSWorkbenchMainFrame.getSingleton().serverSettingsChangedEvent();
        setVisible(false);
    }//GEN-LAST:event_fireCloseEvent

    private void fireOkEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireOkEvent
        if (!jOKButton.isEnabled()) {
            return;
        }
        try {
            /*
              Validate player settings
             */
            UserProfile selectedProfile = null;
            try {
                selectedProfile = (UserProfile) jProfileBox.getSelectedItem();
            } catch (Exception ignored) {
            }
            if (selectedProfile != null) {
                if (selectedProfile.getTribe() == null) {
                    //probably data is not loaded yet as there was an error during initialization...just return
                    setBlocking(false);
                    setVisible(false);
                    return;
                }
                if (selectedProfile.getTribe().equals(InvalidTribe.getSingleton())) {
                    JOptionPaneHelper.showWarningBox(this, "Der Spieler des gewählten Profils existiert nicht mehr.\nBitte ein anderes Profil wählen. ", "Warnung");
                    return;
                }

                logger.debug("Setting default profile for server '" + GlobalOptions.getSelectedServer() + "' to " + selectedProfile.getTribeName());
                UserProfile formerProfile = GlobalOptions.getSelectedProfile();

                if (formerProfile.getProfileId() != selectedProfile.getProfileId()) {
                    logger.info("Writing user data for former profile");
                    TacticsPlanerWizard.storeProperties();
                    ResourceDistributorWizard.storeProperties();
                    GlobalOptions.saveUserData();
                    GlobalOptions.addProperty("selected.profile", Long.toString(selectedProfile.getProfileId()));
                    formerProfile.updateProperties();
                    formerProfile.storeProfileData();
                    GlobalOptions.setSelectedProfile(selectedProfile);
                    logger.info("Loading user data for selected profile");
                    GlobalOptions.loadUserData();
                } else {
                    GlobalOptions.addProperty("selected.profile", Long.toString(selectedProfile.getProfileId()));
                    GlobalOptions.setSelectedProfile(selectedProfile);
                }
            } else if (GlobalOptions.getSelectedProfile() == null || GlobalOptions.getSelectedProfile().equals(DummyUserProfile.getSingleton())) {
                JOptionPaneHelper.showWarningBox(DSWorkbenchSettingsDialog.this, "Du musst ein Profil auswählen um fortzufahren", "Warnung");
                return;
            }

            /*
              Update attack vector colors
             */
            DefaultTableModel model = ((DefaultTableModel) jAttackColorTable.getModel());
            for (int i = 0; i < model.getRowCount(); i++) {
                String unit = ((UnitHolder) model.getValueAt(i, 0)).getName();
                Color color = (Color) model.getValueAt(i, 1);
                String hexCol = Integer.toHexString(color.getRGB());
                hexCol = "#" + hexCol.substring(2, hexCol.length());
                GlobalOptions.addProperty(unit + ".color", hexCol);
            }

            /*
              Validate misc properties
             */
            int sortType = jVillageSortTypeChooser.getSelectedIndex();
            Village.setOrderType(sortType);
            GlobalOptions.addProperty("village.order", Integer.toString(sortType));
            GlobalOptions.addProperty("notify.duration", Integer.toString(jNotifyDurationBox.getSelectedIndex()));
            GlobalOptions.addProperty("inform.on.updates", Boolean.toString(jInformOnUpdates.isSelected()));
            GlobalOptions.addProperty("show.popup.moral", Boolean.toString(jShowPopupMoral.isSelected()));
            GlobalOptions.addProperty("show.popup.conquers", Boolean.toString(jShowPopupConquers.isSelected()));
            GlobalOptions.addProperty("show.popup.ranks", Boolean.toString(jShowPopupRanks.isSelected()));
            GlobalOptions.addProperty("show.popup.farm.space", Boolean.toString(jShowPopupFarmSpace.isSelected()));
            GlobalOptions.addProperty("max.density.troops", jMaxTroopDensity.getText());
            GlobalOptions.addProperty("max.farm.space", jMaxFarmSpace.getText());
            GlobalOptions.addProperty("show.live.countdown", Boolean.toString(jShowLiveCountdown.isSelected()));
            GlobalOptions.addProperty("extended.attack.vectors", Boolean.toString(jExtendedAttackLineDrawing.isSelected()));
            GlobalOptions.addProperty("max.sim.rounds", jMaxSimRounds.getText());
            GlobalOptions.addProperty("support.tolerance", jTolerance.getText());
            GlobalOptions.addProperty("max.loss.ratio", jMaxLossRatio.getText());
            GlobalOptions.addProperty("map.marker.transparency", Integer.toString(jMarkerTransparency.getValue()));
            GlobalOptions.addProperty("obst.server", jObstServer.getText());
            GlobalOptions.saveProperties();
            if (!checkSettings()) {
                logger.error("Failed to check server settings");
                return;
            }
            setBlocking(false);
            setVisible(false);
            DSWorkbenchMainFrame.getSingleton().serverSettingsChangedEvent();
            MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
            MinimapPanel.getSingleton().redraw();
        } catch (Throwable t) {
            logger.error("Failed to close settings dialog", t);
        }
    }//GEN-LAST:event_fireOkEvent

private void fireClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireClosingEvent
    fireCloseEvent(null);
}//GEN-LAST:event_fireClosingEvent

    // </editor-fold>
private void fireChangeContinentsOnMinimapEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChangeContinentsOnMinimapEvent
    GlobalOptions.addProperty("map.showcontinents", Boolean.toString(jShowContinents.isSelected()));
    MinimapPanel.getSingleton().resetBuffer();
    MinimapPanel.getSingleton().redraw();
}//GEN-LAST:event_fireChangeContinentsOnMinimapEvent

    // <editor-fold defaultstate="collapsed" desc=" EventListeners for settings ">
private void fireChangeShowAttackMovementEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireChangeShowAttackMovementEvent
    GlobalOptions.addProperty("attack.movement", Boolean.toString(jShowAttackMovementBox.isSelected()));
}//GEN-LAST:event_fireChangeShowAttackMovementEvent

private void fireChangeMarkOwnVillagesOnMinimapEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireChangeMarkOwnVillagesOnMinimapEvent
    GlobalOptions.addProperty("mark.villages.on.minimap", Boolean.toString(jMarkOwnVillagesOnMinimapBox.isSelected()));
    MinimapPanel.getSingleton().resetBuffer();
    MinimapPanel.getSingleton().redraw();
}//GEN-LAST:event_fireChangeMarkOwnVillagesOnMinimapEvent

private void fireStandardMarkChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireStandardMarkChangedEvent
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        int idx = jDefaultMarkBox.getSelectedIndex();
        if (idx < 0) {
            idx = 0;
        }
        GlobalOptions.addProperty("default.mark", Integer.toString(idx));
    }
}//GEN-LAST:event_fireStandardMarkChangedEvent

private void fireDrawAttacksByDefaultChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireDrawAttacksByDefaultChangedEvent
    GlobalOptions.addProperty("draw.attacks.by.default", Boolean.toString(jDrawAttacksByDefaultBox.isSelected()));
}//GEN-LAST:event_fireDrawAttacksByDefaultChangedEvent

private void fireCheckForUpdatesEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireCheckForUpdatesEvent
    GlobalOptions.addProperty("check.updates.on.startup", Boolean.toString(jCheckForUpdatesBox.isSelected()));
}//GEN-LAST:event_fireCheckForUpdatesEvent

private void fireChangeShowSectorsEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChangeShowSectorsEvent
    GlobalOptions.addProperty("show.sectors", Boolean.toString(jShowSectorsBox.isSelected()));
}//GEN-LAST:event_fireChangeShowSectorsEvent

private void fireShowBarbarianChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowBarbarianChangedEvent
    GlobalOptions.addProperty("show.barbarian", Boolean.toString(jShowBarbarianBox.isSelected()));
}//GEN-LAST:event_fireShowBarbarianChangedEvent

private void fireAcceptDeffStrengthEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAcceptDeffStrengthEvent
    if (evt.getSource() == jDeffStrengthOKButton) {
        try {
            double def = 0;
            UnitHolder h = DataHolder.getSingleton().getUnitByPlainName("spear");
            int spear = Integer.parseInt(jSpearAmount.getText());
            def += h.getDefense() * spear;
            int sword = Integer.parseInt(jSwordAmount.getText());
            h = DataHolder.getSingleton().getUnitByPlainName("sword");
            def += h.getDefense() * sword;
            int archer = Integer.parseInt(jArcherAmount.getText());
            h = DataHolder.getSingleton().getUnitByPlainName("archer");
            if (h != null) {
                def += h.getDefense() * archer;
            }
            int heavy = Integer.parseInt(jHeavyAmount.getText());
            h = DataHolder.getSingleton().getUnitByPlainName("heavy");
            def += h.getDefense() * heavy;
            String result = Integer.toString((int) def);
            GlobalOptions.addProperty("max.density.troops", result);
            jMaxTroopDensity.setText(result);
        } catch (Exception e) {
            JOptionPaneHelper.showErrorBox(jTroopDensitySelectionDialog, "Bitte überprüfe deine Eingaben.", "Fehler");
            return;
        }
    }

    jTroopDensitySelectionDialog.setVisible(false);
}//GEN-LAST:event_fireAcceptDeffStrengthEvent

private void fireSelectTroopsDensityEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectTroopsDensityEvent
    jTroopDensitySelectionDialog.setLocationRelativeTo(this);
    jTroopDensitySelectionDialog.setVisible(true);
}//GEN-LAST:event_fireSelectTroopsDensityEvent

private void fireSelectBrowserEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectBrowserEvent
    if (!jButton1.isEnabled()) {
        return;
    }
    String dir = GlobalOptions.getProperty("screen.dir");

    JFileChooser chooser = null;
    try {
        chooser = new JFileChooser(dir);
    } catch (Exception e) {
        JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n"
                + "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
        return;
    }
    chooser.setDialogTitle("Browser auswählen...");

    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

        @Override
        public boolean accept(File f) {
            return true;
        }

        @Override
        public String getDescription() {
            return "*.*";
        }
    });
    int ret = chooser.showSaveDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
        File f = chooser.getSelectedFile();

        if (f != null && f.canExecute()) {
            try {
                jBrowserPath.setText(f.getCanonicalPath());
            } catch (Exception e) {
                jBrowserPath.setText(f.getPath());
            }
            GlobalOptions.addProperty("default.browser", jBrowserPath.getText());
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Aktiver Browser geändert.\nWillst du die Einstellungen jetzt testen?", "Erfolg", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                if (!BrowserCommandSender.openTestPage("http://www.google.com")) {
                    JOptionPaneHelper.showErrorBox(this, "Browser konnte nicht geöffnet werden. Bitte überprüfe deine Einstellungen.", "Fehler");
                }
            }
        } else {
            JOptionPaneHelper.showErrorBox(this, "Die ausgewählte Datei scheint kein gültiges Programm zu sein.", "Fehler");
        }
    }

}//GEN-LAST:event_fireSelectBrowserEvent

private void fireChangeDefaultBrowserEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeDefaultBrowserEvent
    boolean value = jUseStandardBrowser.isSelected();
    GlobalOptions.addProperty("default.browser", (value) ? "" : jBrowserPath.getText());
    jLabel5.setEnabled(!value);
    jBrowserPath.setEnabled(!value);
    jButton1.setEnabled(!value);
}//GEN-LAST:event_fireChangeDefaultBrowserEvent

private void fireSelectTemplateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectTemplateEvent
    String dir = null;
    int templateID = -1;
    if (evt.getSource() == jSelectHeaderButton) {
        dir = GlobalOptions.getProperty("attack.template.header");
        templateID = 0;
    } else if (evt.getSource() == jSelectBlockButton) {
        dir = GlobalOptions.getProperty("attack.template.block");
        templateID = 1;
    } else if (evt.getSource() == jSelectFooterButton) {
        dir = GlobalOptions.getProperty("attack.template.footer");
        templateID = 2;
    }
    if (templateID < 0) {
        //unknown event source
        return;
    }
    if (dir == null) {
        dir = ".";
    }

    JFileChooser chooser = null;
    try {
        chooser = new JFileChooser(dir);
    } catch (Exception e) {
        JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n"
                + "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
        return;
    }
    chooser.setDialogTitle("Template auswählen...");

    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

        @Override
        public boolean accept(File f) {
            return true;
        }

        @Override
        public String getDescription() {
            return "*.tmpl";
        }
    });
    int ret = chooser.showOpenDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
        File f = chooser.getSelectedFile();
        if (f != null && f.isFile() && f.exists()) {
            switch (templateID) {
                case 0: {
                    GlobalOptions.addProperty("attack.template.header", f.getPath());
                    jHeaderPath.setText(f.getPath());
                    break;
                }
                case 1: {
                    GlobalOptions.addProperty("attack.template.block", f.getPath());
                    jBlockPath.setText(f.getPath());
                    break;
                }
                default: {
                    GlobalOptions.addProperty("attack.template.footer", f.getPath());
                    jFooterPath.setText(f.getPath());
                    break;
                }
            }
        } else {
            JOptionPaneHelper.showErrorBox(this, "Die ausgewählte Datei scheint keine gültige Datei zu sein.", "Fehler");
        }
        AttackPlanHTMLExporter.loadCustomTemplate();
    }
}//GEN-LAST:event_fireSelectTemplateEvent

private void fireRestoreTemplateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRestoreTemplateEvent

    if (evt.getSource() == jRestoreHeaderButton) {
        jHeaderPath.setText("<Standard>");
        GlobalOptions.removeProperty("attack.template.header");
    } else if (evt.getSource() == jRestoreBlockButton) {
        jBlockPath.setText("<Standard>");
        GlobalOptions.removeProperty("attack.template.block");
    } else if (evt.getSource() == jRestoreFooterButton) {
        jFooterPath.setText("<Standard>");
        GlobalOptions.removeProperty("attack.template.footer");
    }
    AttackPlanHTMLExporter.loadCustomTemplate();
}//GEN-LAST:event_fireRestoreTemplateEvent

private void fireDownloadLiveDataEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireDownloadLiveDataEvent
    if (jProfileBox.getSelectedItem() == null) {
        return;
    }
    // <editor-fold defaultstate="collapsed" desc=" Offline Mode ? ">

    if (GlobalOptions.isOfflineMode()) {
        JOptionPaneHelper.showWarningBox(this, "Du befindest dich im Offline-Modus."
                + "\nBitte korrigiere deine Netzwerkeinstellungen um den Download durchzuführen.",
                "Warnung");
        return;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Account valid, data outdated ? ">
    String selectedServer = ((UserProfile) jProfileBox.getSelectedItem()).getServerId();
    // </editor-fold>

    //save current user data for current server
    GlobalOptions.saveUserData();
    GlobalOptions.setSelectedServer(selectedServer);
    GlobalOptions.addProperty("default.server", selectedServer);
    GlobalOptions.saveProperties();

    updating = true;
    jOKButton.setEnabled(false);
    jCancelButton.setEnabled(false);
    jDownloadLiveDataButton.setEnabled(false);
    jNewProfileButton.setEnabled(false);
    jModifyProfileButton.setEnabled(false);
    jDeleteProfileButton.setEnabled(false);
    jStatusArea.setText("");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    Thread t = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                logger.debug("Start downloading data from tribal wars servers");
                boolean ret = DataHolder.getSingleton().loadLiveData();
                logger.debug("Update finished " + ((ret) ? "successfully" : "with errors"));
            } catch (Exception e) {
                logger.error("Failed to load data", e);
            }
        }
    });

    logger.debug("Starting update thread");
    t.setDaemon(true);
    t.start();
}//GEN-LAST:event_fireDownloadLiveDataEvent

private void fireProfileActionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireProfileActionEvent
    if (evt.getSource() == jNewProfileButton) {
        DSWorkbenchProfileDialog.getSingleton().setLocationRelativeTo(this);
        DSWorkbenchProfileDialog.getSingleton().showAddProfileDialog();
    } else if (evt.getSource() == jModifyProfileButton) {
        DSWorkbenchProfileDialog.getSingleton().setLocationRelativeTo(this);
        UserProfile profile = (UserProfile) jProfileBox.getSelectedItem();
        if (profile == null) {
            return;
        }
        DSWorkbenchProfileDialog.getSingleton().showModifyDialog(profile);
    } else if (evt.getSource() == jDeleteProfileButton) {
        UserProfile profile = (UserProfile) jProfileBox.getSelectedItem();
        boolean success = false;
        if (JOptionPaneHelper.showWarningConfirmBox(this, "Mit dem Profil werden alle Angriffe, Markierungen usw. gelöscht.\nSoll das Profil " + profile + " wirklich gelöscht werden?", "Warnung", "Nein", "Ja") == JOptionPane.OK_OPTION) {
            success = profile.delete();
            if (!success) {
                JOptionPaneHelper.showWarningBox(this, "Das Profil konnte nicht gelöscht werden.\nVersuch es bitte später oder nach einem Neustart von DS Workbench noch einmal.", "Löschen fehlgeschlagen");
            }
        } else {
            //delete canceled
            return;
        }
    }
    updateProfileList();
}//GEN-LAST:event_fireProfileActionEvent

    private void fireShowHalfSizeMainMenuEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowHalfSizeMainMenuEvent
        GlobalOptions.addProperty("half.ribbon.size", Boolean.toString(jHalfSizeMainMenu.isSelected()));
        JOptionPaneHelper.showInformationBox(DSWorkbenchSettingsDialog.this, "Für die Größenänderung des Hauptmenüs ist ein Neustart von DS Workbench erforderlich.", "Neustart erforderlich");
    }//GEN-LAST:event_fireShowHalfSizeMainMenuEvent

    private void fireEnableClipboardNotificationEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireEnableClipboardNotificationEvent
        GlobalOptions.addProperty("clipboard.notification", Boolean.toString(jClipboardSound.isSelected()));
    }//GEN-LAST:event_fireEnableClipboardNotificationEvent

    private void fireEnableSystrayEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireEnableSystrayEvent
        GlobalOptions.addProperty("systray.enabled", Boolean.toString(jEnableSystray.isSelected()));
    }//GEN-LAST:event_fireEnableSystrayEvent

    private void fireDeleteFarmReportsOnExitEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireDeleteFarmReportsOnExitEvent
        GlobalOptions.addProperty("delete.farm.reports.on.exit", Boolean.toString(jDeleteFarmReportsOnExit.isSelected()));
    }//GEN-LAST:event_fireDeleteFarmReportsOnExitEvent

    private void fireChangeOffEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeOffEvent
        GlobalOptions.addProperty("standard.off", getOffense().toProperty());
    }//GEN-LAST:event_fireChangeOffEvent

    private void fireChangeDefEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeDefEvent
        GlobalOptions.addProperty("standard.defense.split", getDefense().toProperty());
    }//GEN-LAST:event_fireChangeDefEvent

    private void fireRestartReportServerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRestartReportServerEvent
        int portBefore = GlobalOptions.getProperties().getInt("report.server.port");
        int port = UIHelper.parseIntFromField(jReportServerPort, 8080);

        if (port != portBefore) {
            GlobalOptions.addProperty("report.server.port", Integer.toString(port));
            ReportServer.getSingleton().stop();
            try {
                ReportServer.getSingleton().start(port);
                JOptionPaneHelper.showInformationBox(this, "Der Berichtserver läuft nun auf Port " + port, "Information");
            } catch (BindException be) {
                JOptionPaneHelper.showErrorBox(this, "Port " + port + " wird bereits verwendet.\nFalls der Berichtsserver vorher auf diesem Port lief,\n"
                        + "starte DS Workbench neu um den Port wiederverwenden zu können.", "Fehler");
            } catch (Exception e) {
                JOptionPaneHelper.showErrorBox(this, "Fehler beim Starten des Berichtservers auf Port " + port, "Fehler");
            }
        } else {
            JOptionPaneHelper.showInformationBox(this, "Der Berichtserver läuft bereits auf Port " + port, "Information");
        }
    }//GEN-LAST:event_fireRestartReportServerEvent

    private void jShowPopupMoralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShowPopupMoralActionPerformed
        //TODO remove this when moral is working correctly
        if (jShowPopupMoral.isSelected()
                && (ServerSettings.getSingleton().getMoralType() == ServerSettings.TIMEBASED_MORAL
                || ServerSettings.getSingleton().getMoralType() == ServerSettings.TIME_LIMITED_POINTBASED_MORAL)) {
            String warning = "Achtung: Auf Welten mit ";
            if (ServerSettings.getSingleton().getMoralType() == ServerSettings.TIMEBASED_MORAL) {
                warning += "Zeitbasierter";
            } else if (ServerSettings.getSingleton().getMoralType() == ServerSettings.TIME_LIMITED_POINTBASED_MORAL) {
                warning += "zeitlich begrenzter punktebasierter Moral";
            }
            warning += " wird die Moral mit der falschen Formel berechnet\n"
                    + "Trotzdem die Moralanzeige einschalten?";
            if (JOptionPaneHelper.showWarningConfirmBox(this, warning, "Warnung", "Nein",
                    "Ja") != JOptionPane.OK_OPTION) {
                jShowPopupMoral.setSelected(false);
            }
        }
    }//GEN-LAST:event_jShowPopupMoralActionPerformed

    private void fireSelectProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireSelectProfile
        if (jProfileBox.getSelectedItem() == null) {
            return;
        }
        String selectedServer = ((UserProfile) jProfileBox.getSelectedItem()).getServerId();
        GlobalOptions.addProperty("default.player", Long.toString(((UserProfile) jProfileBox.getSelectedItem()).getProfileId()));

        if (!GlobalOptions.getProperty("default.server").equals(selectedServer)) {
            //save user data for current server
            GlobalOptions.saveUserData();
            GlobalOptions.addProperty("default.server", selectedServer);
            GlobalOptions.saveProperties();

            GlobalOptions.setSelectedServer(selectedServer);
            jOKButton.setEnabled(false);
            jCancelButton.setEnabled(false);
            jDownloadLiveDataButton.setEnabled(false);
            jNewProfileButton.setEnabled(false);
            jModifyProfileButton.setEnabled(false);
            jDeleteProfileButton.setEnabled(false);

            jStatusArea.setText("");
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            try {
                logger.debug("Start loading from hard disk");
                boolean ret = DataHolder.getSingleton().loadData(false);
                jLabelServer.setText(selectedServer);
                logger.debug("Data loaded " + ((ret) ? "successfully" : "with errors"));
            } catch (Exception e) {
                logger.error("Failed loading data", e);
            }
        }
    }//GEN-LAST:event_fireSelectProfile

    // </editor-fold>
    /**
     * Update the server list
     */
    private boolean updateServerList() {
        String[] servers;
        //if connection not failed before, get server list
        if (!GlobalOptions.isOfflineMode()) {
            try {
                ServerManager.loadServerList(getWebProxy());
                servers = ServerManager.getServerIDs();
                if (servers == null) {
                    throw new Exception("No server received");
                }
            } catch (Exception e) {
                logger.error("Failed to load server list", e);
                GlobalOptions.setOfflineMode(true);
                servers = ServerManager.getLocalServers();
            }
        } else {
            //get local list in offline mode
            servers = ServerManager.getLocalServers();
        }

        if (servers.length < 1) {
            logger.error("Failed to get server list and no locally stored server found");
            jProfileBox.setModel(new DefaultComboBoxModel(new Object[]{"Keine Server gefunden"}));
            jLabelServer.setText("kein Server");
            return false;
        }
        updateProfileList();

        return true;
    }

    private void updateProfileList() {
        DefaultComboBoxModel model;
        UserProfile[] profiles = ProfileManager.getSingleton().getProfiles();
        if (profiles != null && profiles.length > 0) {
            model = new DefaultComboBoxModel(profiles);
        } else {
            model = new DefaultComboBoxModel(new Object[]{"Kein Profil vorhanden"});
        }

        long profileId = -1;
        if (GlobalOptions.getSelectedProfile() != null) {
            profileId = GlobalOptions.getSelectedProfile().getProfileId();
        } else {
            try {
                profileId = GlobalOptions.getProperties().getLong("default.player");
            } catch (Exception ignored) {
            }
        }
        jProfileBox.setModel(model);

        if (profileId != -1) {

            for (UserProfile profile : profiles) {
                if (profile.getProfileId() == profileId) {
                    jProfileBox.setSelectedItem(profile);
                    break;
                }
            }
        }
        jLabelServer.setText(((UserProfile) jProfileBox.getSelectedItem()).getServerId());

        fireSelectProfile(null);
    }

    /**
     * Check the connectivity to google.com
     */
    private void checkConnectivity() {
        logger.debug("Checking general connectivity");
        try {
            URLConnection c = new URL("http://www.google.com").openConnection(getWebProxy());
            //   c.setConnectTimeout(10000);
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
    }

    /**
     * Check the tribes server and account
     */
    private boolean checkTribesAccountSettings() {
        if (!checkServerPlayerSettings()) {
            String message = "Bitte überprüfe die Spieler-/Servereinstellungen und schließe die Einstellungen mit OK.\n";
            message += "Möglicherweise wurde noch kein Server oder kein Spieler ausgewählt.\n";
            message += "Diese Einstellungen sind für einen korrekten Ablauf zwingend notwendig.";

            if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Warnung", "Beenden", "Korrigieren") == JOptionPane.NO_OPTION) {
                logger.error("Player/Server settings incorrect. User requested application to terminate");
                System.exit(1);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void fireDataHolderEvent(String pMessage) {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        jStatusArea.insert("(" + f.format(new Date(System.currentTimeMillis())) + ") " + pMessage + "\n", jStatusArea.getText().length());
        UIHelper.applyCorrectViewPosition(jStatusArea, jScrollPane1);
    }

    @Override
    public void fireDataLoadedEvent(boolean pSuccess) {
        if (pSuccess) {
            try {
                Collection<Tribe> tribes = DataHolder.getSingleton().getTribes().values();
                Tribe[] ta = tribes.toArray(new Tribe[]{});
                Arrays.sort(ta, Tribe.CASE_INSENSITIVE_ORDER);
                DefaultComboBoxModel model;
                UserProfile[] profiles = ProfileManager.getSingleton().getProfiles();
                UserProfile active = null;
                if (profiles != null && profiles.length > 0) {
                    model = new DefaultComboBoxModel(profiles);

                    jProfileBox.setModel(model);
                    long profileId = -1;
                    try {
                        profileId = GlobalOptions.getProperties().getLong("default.player");
                    } catch (Exception ignored) {
                    }
                    if (profileId != -1) {
                        for (UserProfile profile : profiles) {
                            if (profile.getProfileId() == profileId) {
                                jProfileBox.setSelectedItem(profile);
                                active = profile;
                                break;
                            }
                        }
                    } else {
                        jProfileBox.setSelectedIndex(0);
                        GlobalOptions.addProperty("default.player", Long.toString(profiles[0].getProfileId()));

                        String server = ((UserProfile) jProfileBox.getSelectedItem()).getServerId();
                        jLabelServer.setText(server);
                        GlobalOptions.addProperty("default.server", server);
                    }
                    if (active != null) {
                        GlobalOptions.setSelectedProfile(active);
                    } else {
                        GlobalOptions.setSelectedProfile(profiles[0]);
                    }
                } else {
                    model = new DefaultComboBoxModel(new Object[]{"Kein Profil vorhanden"});
                    jProfileBox.setModel(model);
                    GlobalOptions.setSelectedProfile(DummyUserProfile.getSingleton());
                    jLabelServer.setText("kein Server");
                }
                //if (DSWorkbenchMainFrame.getSingleton().isInitialized()) {
                if (GlobalOptions.isStarted()) {
                    DSWorkbenchMainFrame.getSingleton().serverSettingsChangedEvent();
                }
            } catch (Exception e) {
                logger.error("Failed to setup tribe list", e);
            }
            logger.info("Loading user data");
            GlobalOptions.loadUserData();
        }

        updating = false;

        jOKButton.setEnabled(true);
        if (!isBlocked) {
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            jCancelButton.setEnabled(true);
        }
        jDownloadLiveDataButton.setEnabled(true);
        jNewProfileButton.setEnabled(true);
        jModifyProfileButton.setEnabled(true);
        jDeleteProfileButton.setEnabled(true);
    }

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //  UIManager.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup connectionTypeGroup;
    private com.jidesoft.swing.LabeledTextField jArcherAmount;
    private javax.swing.JTable jAttackColorTable;
    private javax.swing.JLabel jAttackMovementLabel;
    private javax.swing.JLabel jAttackMovementLabel2;
    private javax.swing.JLabel jAttackMovementLabel3;
    private javax.swing.JPanel jAttackSettings;
    private javax.swing.JTextField jBlockPath;
    private javax.swing.JTextField jBrowserPath;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JCheckBox jCheckForUpdatesBox;
    private javax.swing.JCheckBox jClipboardSound;
    private com.jidesoft.swing.LabeledTextField jDefArcher;
    private com.jidesoft.swing.LabeledTextField jDefHeavy;
    private com.jidesoft.swing.LabeledTextField jDefSpear;
    private com.jidesoft.swing.LabeledTextField jDefSpy;
    private com.jidesoft.swing.LabeledTextField jDefSword;
    private javax.swing.JComboBox jDefaultMarkBox;
    private javax.swing.JPanel jDefenseSettings;
    private javax.swing.JButton jDeffStrengthOKButton;
    private javax.swing.JCheckBox jDeleteFarmReportsOnExit;
    private javax.swing.JButton jDeleteProfileButton;
    private javax.swing.JRadioButton jDirectConnectOption;
    private javax.swing.JButton jDownloadLiveDataButton;
    private javax.swing.JCheckBox jDrawAttacksByDefaultBox;
    private javax.swing.JCheckBox jEnableSystray;
    private javax.swing.JCheckBox jExtendedAttackLineDrawing;
    private javax.swing.JTextField jFooterPath;
    private javax.swing.JCheckBox jHalfSizeMainMenu;
    private javax.swing.JTextField jHeaderPath;
    private com.jidesoft.swing.LabeledTextField jHeavyAmount;
    private javax.swing.JCheckBox jInformOnUpdates;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JPanel jMapSettings;
    private javax.swing.JCheckBox jMarkOwnVillagesOnMinimapBox;
    private javax.swing.JSlider jMarkerTransparency;
    private javax.swing.JTextField jMaxFarmSpace;
    private javax.swing.JTextField jMaxLossRatio;
    private javax.swing.JTextField jMaxSimRounds;
    private javax.swing.JTextField jMaxTroopDensity;
    private javax.swing.JPanel jMiscSettings;
    private javax.swing.JButton jModifyProfileButton;
    private javax.swing.JPanel jNetworkSettings;
    private javax.swing.JButton jNewProfileButton;
    private javax.swing.JComboBox jNotifyDurationBox;
    private javax.swing.JButton jOKButton;
    private javax.swing.JTextField jObstServer;
    private com.jidesoft.swing.LabeledTextField jOffAxe;
    private com.jidesoft.swing.LabeledTextField jOffCata;
    private com.jidesoft.swing.LabeledTextField jOffLight;
    private com.jidesoft.swing.LabeledTextField jOffMarcher;
    private com.jidesoft.swing.LabeledTextField jOffRam;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPlayerServerSettings;
    private javax.swing.JComboBox jProfileBox;
    private javax.swing.JLabel jProxyAdressLabel;
    private javax.swing.JRadioButton jProxyConnectOption;
    private javax.swing.JTextField jProxyHost;
    private javax.swing.JPasswordField jProxyPassword;
    private javax.swing.JTextField jProxyPort;
    private javax.swing.JLabel jProxyPortLabel;
    private javax.swing.JComboBox jProxyTypeChooser;
    private javax.swing.JTextField jProxyUser;
    private javax.swing.JButton jRefeshNetworkButton;
    private javax.swing.JTextField jReportServerPort;
    private javax.swing.JButton jRestoreBlockButton;
    private javax.swing.JButton jRestoreFooterButton;
    private javax.swing.JButton jRestoreHeaderButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton jSelectBlockButton;
    private javax.swing.JButton jSelectFooterButton;
    private javax.swing.JButton jSelectHeaderButton;
    private javax.swing.JTabbedPane jSettingsTabbedPane;
    private javax.swing.JCheckBox jShowAttackMovementBox;
    private javax.swing.JCheckBox jShowBarbarianBox;
    private javax.swing.JCheckBox jShowContinents;
    private javax.swing.JLabel jShowContinentsLabel;
    private javax.swing.JCheckBox jShowLiveCountdown;
    private javax.swing.JCheckBox jShowPopupConquers;
    private javax.swing.JCheckBox jShowPopupFarmSpace;
    private javax.swing.JCheckBox jShowPopupMoral;
    private javax.swing.JCheckBox jShowPopupRanks;
    private javax.swing.JCheckBox jShowSectorsBox;
    private javax.swing.JPanel jSingleSupportPanel;
    private com.jidesoft.swing.LabeledTextField jSpearAmount;
    private javax.swing.JPanel jStandardAttackerPanel;
    private javax.swing.JTextArea jStatusArea;
    private com.jidesoft.swing.LabeledTextField jSwordAmount;
    private javax.swing.JPanel jTemplateSettings;
    private javax.swing.JTextField jTolerance;
    private javax.swing.JDialog jTroopDensitySelectionDialog;
    private javax.swing.JCheckBox jUseStandardBrowser;
    private javax.swing.JComboBox jVillageSortTypeChooser;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXLabel jXLabel2;
    private org.jdesktop.swingx.JXLabel jXLabel3;
    private javax.swing.ButtonGroup tagMarkerGroup;
    // End of variables declaration//GEN-END:variables
}
