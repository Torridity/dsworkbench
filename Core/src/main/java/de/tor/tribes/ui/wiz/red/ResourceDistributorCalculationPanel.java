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
package de.tor.tribes.ui.wiz.red;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Resource;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.UIHelper;
import de.tor.tribes.util.algo.MerchantDistributor;
import de.tor.tribes.util.algo.types.MerchantDestination;
import de.tor.tribes.util.algo.types.MerchantSource;
import de.tor.tribes.util.algo.types.Order;
import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class ResourceDistributorCalculationPanel extends WizardPage {

    private static final String GENERAL_INFO = "In diesem Schritt kannst du letztendlich die Berechnung durchführen. Abhängig von der Art der Berechnung "
            + "hast du die Möglichkeit, letzte Einstellungen vorzunehmen. Weiterhin stehen 'Erweiterte Einstellungen' zur Verfügung, die du ebenfalls bei "
            + "Bedarf anpassen kannst. Für den Anfang ist aber empfohlen, diese Einstellungen so zu lassen wie sie sind. Sind alle Einstellungen getroffen,"
            + " kannst du die Berechnung über 'Transporte berechnen' starten.";
    private static ResourceDistributorCalculationPanel singleton = null;
    private MerchantDistributor calculator = null;
    private boolean transportsAlreadyTransferred = false;
    private static Logger logger = LogManager.getLogger("ResourceDistributorCalculationPanel");

    public static synchronized ResourceDistributorCalculationPanel getSingleton() {
        if (singleton == null) {
            singleton = new ResourceDistributorCalculationPanel();
        }

        return singleton;
    }

    /**
     * Creates new form ResourceDataReadPanel
     */
    ResourceDistributorCalculationPanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        jXCollapsiblePane3.setLayout(new BorderLayout());
        jXCollapsiblePane3.add(jExpertSettingsPanel, BorderLayout.CENTER);
    }

    public static String getDescription() {
        return "Berechnung";
    }

    public static String getStep() {
        return "id-calculation";
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("red.max.wood.receiver", UIHelper.parseIntFromField(jTargetWood, 380000));
        profile.addProperty("red.max.clay.receiver", UIHelper.parseIntFromField(jTargetClay, 380000));
        profile.addProperty("red.max.iron.receiver", UIHelper.parseIntFromField(jTargetIron, 380000));
        profile.addProperty("red.min.wood.sender", UIHelper.parseIntFromField(jRemainWood, 28000));
        profile.addProperty("red.min.clay.sender", UIHelper.parseIntFromField(jRemainClay, 30000));
        profile.addProperty("red.min.iron.sender", UIHelper.parseIntFromField(jRemainIron, 25000));
        profile.addProperty("red.ignore.small.transports", jIgnoreTransportsButton.isSelected());
        profile.addProperty("red.ignore.amount", UIHelper.parseIntFromField(jMinTransportAmount, 10000));
        profile.addProperty("red.ignore.distance", jIgnoreTransportsByDistanceButton.isSelected());
        profile.addProperty("red.ignore.distance.amount", UIHelper.parseIntFromField(jMaxTransportDistance, 50));
        profile.addProperty("red.max.filling", jFillSlider.getValue());
        profile.addProperty("red.first.order.position", resourceNameToResourceId(jResource1.getToolTipText()));
        profile.addProperty("red.second.order.position", resourceNameToResourceId(jResource2.getToolTipText()));
        profile.addProperty("red.third.order.position", resourceNameToResourceId(jResource3.getToolTipText()));
        profile.addProperty("red.use.resource1", jUseResource1.isSelected());
        profile.addProperty("red.use.resource2", jUseResource1.isSelected());
        profile.addProperty("red.use.resource3", jUseResource1.isSelected());
        profile.addProperty("red.limit.merchant.amount", jLimitMerchantAmount.isSelected());
    }

    public void restoreProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        String val = profile.getProperty("red.max.wood.receiver");
        if (val != null) {
            jTargetWood.setText(val);
        }
        val = profile.getProperty("red.max.clay.receiver");
        if (val != null) {
            jTargetClay.setText(val);
        }
        val = profile.getProperty("red.max.iron.receiver");
        if (val != null) {
            jTargetIron.setText(val);
        }

        val = profile.getProperty("red.min.wood.sender");
        if (val != null) {
            jRemainWood.setText(val);
        }
        val = profile.getProperty("red.min.clay.sender");
        if (val != null) {
            jRemainClay.setText(val);
        }
        val = profile.getProperty("red.min.iron.sender");
        if (val != null) {
            jRemainIron.setText(val);
        }

        int wood = 0;
        int clay = 0;
        int iron = 0;
        val = profile.getProperty("red.first.order.position");
        if (val != null) {
            switch (Integer.parseInt(val)) {
                case 0:
                    wood = 600;
                    break;
                case 1:
                    clay = 600;
                    break;
                default:
                    iron = 600;
                    break;
            }
        }
        val = profile.getProperty("red.second.order.position");
        if (val != null) {
            switch (Integer.parseInt(val)) {
                case 0:
                    wood = 800;
                    break;
                case 1:
                    clay = 800;
                    break;
                default:
                    iron = 800;
                    break;
            }
        }
        val = profile.getProperty("red.third.order.position");
        if (val != null) {
            switch (Integer.parseInt(val)) {
                case 0:
                    wood = 1000;
                    break;
                case 1:
                    clay = 1000;
                    break;
                default:
                    iron = 1000;
                    break;
            }
        }

        if (wood == 0 && clay == 0 && iron == 0) {
            wood = 600;
            clay = 800;
            iron = 1000;
        }

        setAdvisedTransportOrder(wood, clay, iron);

        jIgnoreTransportsButton.setSelected(Boolean.parseBoolean(profile.getProperty("red.ignore.small.transports")));
        val = profile.getProperty("red.ignore.amount");
        if (val != null) {
            jMinTransportAmount.setText(val);
        }

        jIgnoreTransportsByDistanceButton.setSelected(Boolean.parseBoolean(profile.getProperty("red.ignore.distance")));
        val = profile.getProperty("red.ignore.distance.amount");
        if (val != null) {
            jMaxTransportDistance.setText(val);
        }
        val = profile.getProperty("red.max.filling");
        if (val != null) {
            try {
                jFillSlider.setValue(Integer.parseInt(val));
            } catch (Exception e) {
                jFillSlider.setValue(90);
            }
        }

        val = profile.getProperty("red.use.resource1");
        if (val != null) {
            jUseResource1.setSelected(Boolean.parseBoolean(val));
        } else {
            jUseResource1.setSelected(true);
        }
        val = profile.getProperty("red.use.resource2");
        if (val != null) {
            jUseResource2.setSelected(Boolean.parseBoolean(val));
        } else {
            jUseResource2.setSelected(true);
        }
        val = profile.getProperty("red.use.resource3");
        if (val != null) {
            jUseResource3.setSelected(Boolean.parseBoolean(val));
        } else {
            jUseResource3.setSelected(true);
        }
        val = profile.getProperty("red.limit.merchant.amount");
        if (val != null) {
            jLimitMerchantAmount.setSelected(Boolean.parseBoolean(val));
        } else {
            jLimitMerchantAmount.setSelected(true);
        }

        if (!jUseResource1.isSelected() && !jUseResource2.isSelected() && !jUseResource3.isSelected()) {
            jUseResource1.setSelected(true);
            jUseResource2.setSelected(true);
            jUseResource3.setSelected(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jInfoScrollPane = new javax.swing.JScrollPane();
        jInfoTextPane = new javax.swing.JTextPane();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jExpertSettingsPanel = new javax.swing.JPanel();
        jIgnoreTransportsButton = new javax.swing.JCheckBox();
        jMinTransportAmount = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jMaxTransportDistance = new javax.swing.JTextField();
        jIgnoreTransportsByDistanceButton = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jResource1 = new javax.swing.JLabel();
        jResource2 = new javax.swing.JLabel();
        jResource3 = new javax.swing.JLabel();
        jSwitch12Button = new javax.swing.JButton();
        jSwitch23Button = new javax.swing.JButton();
        jSwitch23Button1 = new javax.swing.JButton();
        jUseResource1 = new javax.swing.JCheckBox();
        jUseResource3 = new javax.swing.JCheckBox();
        jUseResource2 = new javax.swing.JCheckBox();
        jFillSlider = new javax.swing.JSlider();
        jLabel13 = new javax.swing.JLabel();
        jLimitMerchantAmount = new javax.swing.JCheckBox();
        jAllowOverflow = new javax.swing.JCheckBox();
        jFillFactor = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jPanel1 = new javax.swing.JPanel();
        jSummaryPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSenders = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jMerchants = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jWood = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jReceivers = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jClay = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jIron = new javax.swing.JLabel();
        jSettingsPanel = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jXCollapsiblePane3 = new org.jdesktop.swingx.JXCollapsiblePane();
        jFillSettingsPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jTargetWood = new com.jidesoft.swing.LabeledTextField();
        jLabel8 = new javax.swing.JLabel();
        jRemainWood = new com.jidesoft.swing.LabeledTextField();
        jTargetClay = new com.jidesoft.swing.LabeledTextField();
        jRemainClay = new com.jidesoft.swing.LabeledTextField();
        jTargetIron = new com.jidesoft.swing.LabeledTextField();
        jRemainIron = new com.jidesoft.swing.LabeledTextField();
        jButton1 = new javax.swing.JButton();
        jNoSettingsLabel = new javax.swing.JLabel();
        jCalculateButton = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html"); // NOI18N
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        jExpertSettingsPanel.setMinimumSize(new java.awt.Dimension(600, 240));
        jExpertSettingsPanel.setPreferredSize(new java.awt.Dimension(600, 240));
        jExpertSettingsPanel.setLayout(new java.awt.GridBagLayout());

        jIgnoreTransportsButton.setText("Transporte mit weniger als");
        jIgnoreTransportsButton.setMaximumSize(new java.awt.Dimension(200, 25));
        jIgnoreTransportsButton.setMinimumSize(new java.awt.Dimension(200, 25));
        jIgnoreTransportsButton.setOpaque(false);
        jIgnoreTransportsButton.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        jExpertSettingsPanel.add(jIgnoreTransportsButton, gridBagConstraints);

        jMinTransportAmount.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jMinTransportAmount.setText("10000");
        jMinTransportAmount.setMaximumSize(new java.awt.Dimension(100, 25));
        jMinTransportAmount.setMinimumSize(new java.awt.Dimension(100, 25));
        jMinTransportAmount.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jExpertSettingsPanel.add(jMinTransportAmount, gridBagConstraints);

        jLabel14.setText("fach füllen. Achtung, kann zu Rohstoffverlust führen!");
        jLabel14.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel14.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel14.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        jExpertSettingsPanel.add(jLabel14, gridBagConstraints);

        jLabel15.setText("Felder ignorieren.");
        jLabel15.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel15.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel15.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jExpertSettingsPanel.add(jLabel15, gridBagConstraints);

        jMaxTransportDistance.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jMaxTransportDistance.setText("50");
        jMaxTransportDistance.setMaximumSize(new java.awt.Dimension(100, 25));
        jMaxTransportDistance.setMinimumSize(new java.awt.Dimension(100, 25));
        jMaxTransportDistance.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jExpertSettingsPanel.add(jMaxTransportDistance, gridBagConstraints);

        jIgnoreTransportsByDistanceButton.setText("Transportentfernung über");
        jIgnoreTransportsByDistanceButton.setMaximumSize(new java.awt.Dimension(200, 25));
        jIgnoreTransportsByDistanceButton.setMinimumSize(new java.awt.Dimension(200, 25));
        jIgnoreTransportsByDistanceButton.setOpaque(false);
        jIgnoreTransportsByDistanceButton.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jExpertSettingsPanel.add(jIgnoreTransportsByDistanceButton, gridBagConstraints);

        jLabel3.setText("Berechnungsreihenfolge");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        jExpertSettingsPanel.add(jLabel3, gridBagConstraints);

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jResource1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jResource1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jResource1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/holz.png"))); // NOI18N
        jResource1.setToolTipText("Holz");
        jResource1.setMaximumSize(new java.awt.Dimension(40, 14));
        jResource1.setMinimumSize(new java.awt.Dimension(40, 14));
        jResource1.setPreferredSize(new java.awt.Dimension(40, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel4.add(jResource1, gridBagConstraints);

        jResource2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jResource2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jResource2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/lehm.png"))); // NOI18N
        jResource2.setToolTipText("Lehm");
        jResource2.setMaximumSize(new java.awt.Dimension(40, 14));
        jResource2.setMinimumSize(new java.awt.Dimension(40, 14));
        jResource2.setPreferredSize(new java.awt.Dimension(40, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel4.add(jResource2, gridBagConstraints);

        jResource3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jResource3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jResource3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/eisen.png"))); // NOI18N
        jResource3.setToolTipText("Eisen");
        jResource3.setMaximumSize(new java.awt.Dimension(40, 14));
        jResource3.setMinimumSize(new java.awt.Dimension(40, 14));
        jResource3.setPreferredSize(new java.awt.Dimension(40, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel4.add(jResource3, gridBagConstraints);

        jSwitch12Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jSwitch12Button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSwitchResourceOrderEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel4.add(jSwitch12Button, gridBagConstraints);

        jSwitch23Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jSwitch23Button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSwitchResourceOrderEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel4.add(jSwitch23Button, gridBagConstraints);

        jSwitch23Button1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_empty.png"))); // NOI18N
        jSwitch23Button1.setToolTipText("Verteilungsreihenfolge empfehlen");
        jSwitch23Button1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAdviceOrderEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel4.add(jSwitch23Button1, gridBagConstraints);

        jUseResource1.setSelected(true);
        jUseResource1.setToolTipText("Transporte für diese Ressource berechnen");
        jUseResource1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jUseResource1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jUseResource1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUsageStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel4.add(jUseResource1, gridBagConstraints);

        jUseResource3.setSelected(true);
        jUseResource3.setToolTipText("Transporte für diese Ressource berechnen");
        jUseResource3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jUseResource3.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jUseResource3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUsageStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        jPanel4.add(jUseResource3, gridBagConstraints);

        jUseResource2.setSelected(true);
        jUseResource2.setToolTipText("Transporte für diese Ressource berechnen");
        jUseResource2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jUseResource2.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jUseResource2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUsageStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel4.add(jUseResource2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        jExpertSettingsPanel.add(jPanel4, gridBagConstraints);

        jFillSlider.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jFillSlider.setMajorTickSpacing(10);
        jFillSlider.setMinimum(50);
        jFillSlider.setMinorTickSpacing(5);
        jFillSlider.setPaintLabels(true);
        jFillSlider.setPaintTicks(true);
        jFillSlider.setSnapToTicks(true);
        jFillSlider.setValue(95);
        jFillSlider.setMaximumSize(new java.awt.Dimension(150, 40));
        jFillSlider.setMinimumSize(new java.awt.Dimension(150, 40));
        jFillSlider.setPreferredSize(new java.awt.Dimension(150, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jExpertSettingsPanel.add(jFillSlider, gridBagConstraints);

        jLabel13.setText("Max. Speicherfüllstand [%]");
        jLabel13.setMaximumSize(new java.awt.Dimension(200, 25));
        jLabel13.setMinimumSize(new java.awt.Dimension(200, 25));
        jLabel13.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jExpertSettingsPanel.add(jLabel13, gridBagConstraints);

        jLimitMerchantAmount.setSelected(true);
        jLimitMerchantAmount.setText("Händlerzahl pro Rohstoff beschränken");
        jLimitMerchantAmount.setToolTipText("<html>Schränkt die Anzahl der Händler pro Rohstoff gleichmäßig ein.<br/>\nWerden alle 3 Rohstoffe gehandelt, wird jeder Rohstoff mit 33% der Händler verschickt.<br/>\nHandelt man nur 2 Rohstoffe, werden für jeden Rohstoff 50% der Händler verwendet.<br/>\nIst diese Option deaktiviert, werden die Händler nach der Reihenfolge des Handels verwendet.</html>");
        jLimitMerchantAmount.setMaximumSize(new java.awt.Dimension(200, 25));
        jLimitMerchantAmount.setMinimumSize(new java.awt.Dimension(200, 25));
        jLimitMerchantAmount.setOpaque(false);
        jLimitMerchantAmount.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jExpertSettingsPanel.add(jLimitMerchantAmount, gridBagConstraints);

        jAllowOverflow.setText("Überfüllung zulassen. Speicher");
        jAllowOverflow.setToolTipText("<html>Hebt das Limit der Speichergröße auf.<br/>  Ist diese Option aktiviert, so werden Rohstoffsendungen bis zur x-fachen Speichergröße an die<br/> Dörfer zugeteilt (oder bis zum Max. Rohstoffe (Empfänger) Limit), je nachdem was kleiner ist. <br/>  Wird ein Speicher beim Beliefern überfüllt, so verfallen die gesendeten Rohstoffe. Ist diese <br/>  Option aktiviert, sollte daher unbedingt darauf geachtet werden empfangene Rohstoffe schnell<br/>  genug zu verarbeiten.<br/> Ist diese Option deaktiviert, so werden Lieferungen anhand des Speichers begrenzt (bis zum Max.<br/> Speicherfüllstand [%] oben). </html>");
        jAllowOverflow.setMaximumSize(new java.awt.Dimension(200, 25));
        jAllowOverflow.setMinimumSize(new java.awt.Dimension(200, 25));
        jAllowOverflow.setOpaque(false);
        jAllowOverflow.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        jExpertSettingsPanel.add(jAllowOverflow, gridBagConstraints);

        jFillFactor.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jFillFactor.setText("2");
        jFillFactor.setMaximumSize(new java.awt.Dimension(100, 25));
        jFillFactor.setMinimumSize(new java.awt.Dimension(100, 25));
        jFillFactor.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jExpertSettingsPanel.add(jFillFactor, gridBagConstraints);

        jLabel16.setText("Rohstoffen ignorieren.");
        jLabel16.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel16.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel16.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        jExpertSettingsPanel.add(jLabel16, gridBagConstraints);

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Informationen einblenden");
        jLabel1.setToolTipText("Blendet Informationen zu dieser Ansicht und zu den Datenquellen ein/aus");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowHideInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel1, gridBagConstraints);

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jXCollapsiblePane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jSummaryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Zusammenfassung"));
        jSummaryPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Lieferanten");
        jLabel2.setMaximumSize(new java.awt.Dimension(200, 16));
        jLabel2.setMinimumSize(new java.awt.Dimension(200, 16));
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jSummaryPanel.add(jLabel2, gridBagConstraints);

        jSenders.setText("10");
        jSenders.setMaximumSize(new java.awt.Dimension(100, 16));
        jSenders.setMinimumSize(new java.awt.Dimension(100, 16));
        jSenders.setPreferredSize(new java.awt.Dimension(100, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jSummaryPanel.add(jSenders, gridBagConstraints);

        jLabel4.setText("Verfügbare Händler");
        jLabel4.setMaximumSize(new java.awt.Dimension(200, 16));
        jLabel4.setMinimumSize(new java.awt.Dimension(200, 16));
        jLabel4.setPreferredSize(new java.awt.Dimension(200, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
        jSummaryPanel.add(jLabel4, gridBagConstraints);

        jMerchants.setText("10");
        jMerchants.setMaximumSize(new java.awt.Dimension(100, 16));
        jMerchants.setMinimumSize(new java.awt.Dimension(100, 16));
        jMerchants.setPreferredSize(new java.awt.Dimension(100, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
        jSummaryPanel.add(jMerchants, gridBagConstraints);

        jLabel6.setText("Verfügbares Holz");
        jLabel6.setMaximumSize(new java.awt.Dimension(200, 16));
        jLabel6.setMinimumSize(new java.awt.Dimension(200, 16));
        jLabel6.setPreferredSize(new java.awt.Dimension(200, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jSummaryPanel.add(jLabel6, gridBagConstraints);

        jWood.setText("10");
        jWood.setMaximumSize(new java.awt.Dimension(100, 16));
        jWood.setMinimumSize(new java.awt.Dimension(100, 16));
        jWood.setPreferredSize(new java.awt.Dimension(100, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jSummaryPanel.add(jWood, gridBagConstraints);

        jLabel10.setText("Empfänger");
        jLabel10.setMaximumSize(new java.awt.Dimension(200, 16));
        jLabel10.setMinimumSize(new java.awt.Dimension(200, 16));
        jLabel10.setPreferredSize(new java.awt.Dimension(200, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
        jSummaryPanel.add(jLabel10, gridBagConstraints);

        jReceivers.setText("10");
        jReceivers.setMaximumSize(new java.awt.Dimension(100, 16));
        jReceivers.setMinimumSize(new java.awt.Dimension(100, 16));
        jReceivers.setPreferredSize(new java.awt.Dimension(100, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
        jSummaryPanel.add(jReceivers, gridBagConstraints);

        jLabel7.setText("Verfügbarer Lehm");
        jLabel7.setMaximumSize(new java.awt.Dimension(200, 16));
        jLabel7.setMinimumSize(new java.awt.Dimension(200, 16));
        jLabel7.setPreferredSize(new java.awt.Dimension(200, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
        jSummaryPanel.add(jLabel7, gridBagConstraints);

        jClay.setText("10");
        jClay.setMaximumSize(new java.awt.Dimension(100, 16));
        jClay.setMinimumSize(new java.awt.Dimension(100, 16));
        jClay.setPreferredSize(new java.awt.Dimension(100, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
        jSummaryPanel.add(jClay, gridBagConstraints);

        jLabel9.setText("Verfügbares Eisen");
        jLabel9.setMaximumSize(new java.awt.Dimension(200, 16));
        jLabel9.setMinimumSize(new java.awt.Dimension(200, 16));
        jLabel9.setPreferredSize(new java.awt.Dimension(200, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
        jSummaryPanel.add(jLabel9, gridBagConstraints);

        jIron.setText("10");
        jIron.setMaximumSize(new java.awt.Dimension(100, 16));
        jIron.setMinimumSize(new java.awt.Dimension(100, 16));
        jIron.setPreferredSize(new java.awt.Dimension(100, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
        jSummaryPanel.add(jIron, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jSummaryPanel, gridBagConstraints);

        jSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));
        jSettingsPanel.setLayout(new java.awt.GridBagLayout());

        jToggleButton1.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jToggleButton1.setText("Erweiterte Einstellungen");
        jToggleButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireShowHideExpertSettingsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSettingsPanel.add(jToggleButton1, gridBagConstraints);

        jXCollapsiblePane3.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 0.9;
        jSettingsPanel.add(jXCollapsiblePane3, gridBagConstraints);

        jFillSettingsPanel.setOpaque(false);
        jFillSettingsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("Max. Rohstoffe (Empfänger)");
        jLabel11.setMaximumSize(new java.awt.Dimension(200, 25));
        jLabel11.setMinimumSize(new java.awt.Dimension(200, 25));
        jLabel11.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFillSettingsPanel.add(jLabel11, gridBagConstraints);

        jTargetWood.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/holz.png"))); // NOI18N
        jTargetWood.setMaximumSize(new java.awt.Dimension(100, 25));
        jTargetWood.setMinimumSize(new java.awt.Dimension(100, 25));
        jTargetWood.setPreferredSize(new java.awt.Dimension(100, 25));
        jTargetWood.setText("380000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFillSettingsPanel.add(jTargetWood, gridBagConstraints);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("Min. Rohstoffe (Lieferanten)");
        jLabel8.setMaximumSize(new java.awt.Dimension(200, 25));
        jLabel8.setMinimumSize(new java.awt.Dimension(200, 25));
        jLabel8.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFillSettingsPanel.add(jLabel8, gridBagConstraints);

        jRemainWood.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/holz.png"))); // NOI18N
        jRemainWood.setMaximumSize(new java.awt.Dimension(100, 25));
        jRemainWood.setMinimumSize(new java.awt.Dimension(100, 25));
        jRemainWood.setPreferredSize(new java.awt.Dimension(100, 25));
        jRemainWood.setText("28000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFillSettingsPanel.add(jRemainWood, gridBagConstraints);

        jTargetClay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/lehm.png"))); // NOI18N
        jTargetClay.setMaximumSize(new java.awt.Dimension(100, 25));
        jTargetClay.setMinimumSize(new java.awt.Dimension(100, 25));
        jTargetClay.setPreferredSize(new java.awt.Dimension(100, 25));
        jTargetClay.setText("380000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFillSettingsPanel.add(jTargetClay, gridBagConstraints);

        jRemainClay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/lehm.png"))); // NOI18N
        jRemainClay.setMaximumSize(new java.awt.Dimension(100, 25));
        jRemainClay.setMinimumSize(new java.awt.Dimension(100, 25));
        jRemainClay.setPreferredSize(new java.awt.Dimension(100, 25));
        jRemainClay.setText("30000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFillSettingsPanel.add(jRemainClay, gridBagConstraints);

        jTargetIron.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/eisen.png"))); // NOI18N
        jTargetIron.setMaximumSize(new java.awt.Dimension(100, 25));
        jTargetIron.setMinimumSize(new java.awt.Dimension(100, 25));
        jTargetIron.setPreferredSize(new java.awt.Dimension(100, 25));
        jTargetIron.setText("380000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFillSettingsPanel.add(jTargetIron, gridBagConstraints);

        jRemainIron.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/eisen.png"))); // NOI18N
        jRemainIron.setMaximumSize(new java.awt.Dimension(100, 25));
        jRemainIron.setMinimumSize(new java.awt.Dimension(100, 25));
        jRemainIron.setPreferredSize(new java.awt.Dimension(100, 25));
        jRemainIron.setText("25000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFillSettingsPanel.add(jRemainIron, gridBagConstraints);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/median.png"))); // NOI18N
        jButton1.setToolTipText("Durchnittliche Ressourcen aller Dörfer verwenden");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireSetMinToMedianEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        jFillSettingsPanel.add(jButton1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jSettingsPanel.add(jFillSettingsPanel, gridBagConstraints);

        jNoSettingsLabel.setBackground(new java.awt.Color(255, 255, 255));
        jNoSettingsLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jNoSettingsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jNoSettingsLabel.setText("Keine Einstellungen notwendig");
        jNoSettingsLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jSettingsPanel.add(jNoSettingsLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jSettingsPanel, gridBagConstraints);

        jCalculateButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jCalculateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/select.png"))); // NOI18N
        jCalculateButton.setText("Transporte berechnen");
        jCalculateButton.setMaximumSize(new java.awt.Dimension(190, 40));
        jCalculateButton.setMinimumSize(new java.awt.Dimension(190, 40));
        jCalculateButton.setPreferredSize(new java.awt.Dimension(190, 40));
        jCalculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateTransportsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jCalculateButton, gridBagConstraints);

        jProgressBar1.setString("Bereit");
        jProgressBar1.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jProgressBar1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireShowHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowHideInfoEvent
        if (jXCollapsiblePane1.isCollapsed()) {
            jXCollapsiblePane1.setCollapsed(false);
            jLabel1.setText("Informationen ausblenden");
        } else {
            jXCollapsiblePane1.setCollapsed(true);
            jLabel1.setText("Informationen einblenden");
        }
    }//GEN-LAST:event_fireShowHideInfoEvent

    private void fireCalculateTransportsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateTransportsEvent
        doCalculate();
    }//GEN-LAST:event_fireCalculateTransportsEvent

    private void fireSwitchResourceOrderEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSwitchResourceOrderEvent
        if (evt.getSource() == jSwitch12Button) {
            String text2 = jResource2.getToolTipText();
            Icon icon2 = jResource2.getIcon();
            jResource2.setToolTipText(jResource1.getToolTipText());
            jResource2.setIcon(jResource1.getIcon());
            jResource1.setToolTipText(text2);
            jResource1.setIcon(icon2);
        } else if (evt.getSource() == jSwitch23Button) {
            String text3 = jResource3.getToolTipText();
            Icon icon3 = jResource3.getIcon();
            jResource3.setToolTipText(jResource2.getToolTipText());
            jResource3.setIcon(jResource2.getIcon());
            jResource2.setToolTipText(text3);
            jResource2.setIcon(icon3);
        }
    }//GEN-LAST:event_fireSwitchResourceOrderEvent

    private void fireShowHideExpertSettingsEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireShowHideExpertSettingsEvent
        jXCollapsiblePane3.setCollapsed(!jToggleButton1.isSelected());
    }//GEN-LAST:event_fireShowHideExpertSettingsEvent

    private void fireSetMinToMedianEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSetMinToMedianEvent

        int elementCount = 0;
        int wood = 0;
        int clay = 0;
        int iron = 0;
        for (VillageMerchantInfo info : ResourceDistributorSettingsPanel.getSingleton().getAllElements()) {
            wood += info.getWoodStock();
            clay += info.getClayStock();
            iron += info.getIronStock();
            elementCount++;
        }

        jRemainWood.setText(Integer.toString(wood / elementCount));
        jRemainClay.setText(Integer.toString(clay / elementCount));
        jRemainIron.setText(Integer.toString(iron / elementCount));
    }//GEN-LAST:event_fireSetMinToMedianEvent

    private void fireAdviceOrderEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAdviceOrderEvent
        int wood = 0;
        int clay = 0;
        int iron = 0;

        for (VillageMerchantInfo newInfo : ResourceDistributorSettingsPanel.getSingleton().getAllElements()) {
            switch (newInfo.getDirection()) {
                case INCOMING:
                    break;
                case OUTGOING:
                    wood += newInfo.getWoodStock() / 1000;
                    clay += newInfo.getClayStock() / 1000;
                    iron += newInfo.getIronStock() / 1000;
                    break;
                case BOTH:
                    wood += newInfo.getWoodStock() / 1000;
                    clay += newInfo.getClayStock() / 1000;
                    iron += newInfo.getIronStock() / 1000;
                    break;
            }
        }

        setAdvisedTransportOrder(wood, clay, iron);
    }//GEN-LAST:event_fireAdviceOrderEvent

    private void fireUsageStateChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireUsageStateChangedEvent
        if (!jUseResource1.isSelected() && !jUseResource2.isSelected() && !jUseResource3.isSelected()) {
            setProblem("Es muss mindestens ein Rohstoff verwendet werden");
        } else {
            setProblem(null);
        }
    }//GEN-LAST:event_fireUsageStateChangedEvent

    private void setAdvisedTransportOrder(int pWood, int pClay, int pIron) {
        Integer[] sums = new Integer[]{pWood, pClay, pIron};
        Arrays.sort(sums);
        int[] priorities = new int[3];
        for (int i = 0; i < 3; i++) {
            if (sums[i] == pWood) {
                priorities[i] = 0;
            } else if (sums[i] == pClay) {
                priorities[i] = 1;
            } else {
                priorities[i] = 2;
            }
        }

        jResource1.setToolTipText(resourceIdToResourceName(priorities[0]));
        jResource1.setIcon(resourceIdToResourceIcon(priorities[0]));
        jResource2.setToolTipText(resourceIdToResourceName(priorities[1]));
        jResource2.setIcon(resourceIdToResourceIcon(priorities[1]));
        jResource3.setToolTipText(resourceIdToResourceName(priorities[2]));
        jResource3.setIcon(resourceIdToResourceIcon(priorities[2]));
    }

    private String resourceIdToResourceName(int id) {
        switch (id) {
            case 0:
                return "Holz";
            case 1:
                return "Lehm";
            default:
                return "Eisen";
        }
    }

    private Icon resourceIdToResourceIcon(int id) {
        switch (id) {
            case 0:
                return new javax.swing.ImageIcon(getClass().getResource("/res/ui/holz.png"));
            case 1:
                return new javax.swing.ImageIcon(getClass().getResource("/res/ui/lehm.png"));
            default:
                return new javax.swing.ImageIcon(getClass().getResource("/res/ui/eisen.png"));
        }
    }

    private int resourceNameToResourceId(String resource) {
        switch (resource) {
            case "Holz":
                return 0;
            case "Lehm":
                return 1;
            default:
                return 2;
        }
    }

    private boolean isFillDistribution() {
        return ResourceDistributorWelcomePanel.FILL_DISTRIBUTION.equals(getWizardDataMap().get(ResourceDistributorWelcomePanel.TYPE));
    }

    protected void setup(boolean pFill) {
        jFillSettingsPanel.setVisible(pFill);
        jNoSettingsLabel.setVisible(!pFill);
        int senders = 0;
        int receivers = 0;
        int merchants = 0;
        int wood = 0;
        int clay = 0;
        int iron = 0;

        for (VillageMerchantInfo newInfo : ResourceDistributorSettingsPanel.getSingleton().getAllElements()) {
            switch (newInfo.getDirection()) {
                case INCOMING:
                    receivers++;
                    break;
                case OUTGOING:
                    senders++;
                    merchants += newInfo.getAvailableMerchants();
                    wood += newInfo.getWoodStock() / 1000;
                    clay += newInfo.getClayStock() / 1000;
                    iron += newInfo.getIronStock() / 1000;
                    break;
                case BOTH:
                    senders++;
                    receivers++;
                    merchants += newInfo.getAvailableMerchants();
                    wood += newInfo.getWoodStock() / 1000;
                    clay += newInfo.getClayStock() / 1000;
                    iron += newInfo.getIronStock() / 1000;
                    break;
            }
        }

        // setAdvisedTransportOrder(wood, clay, iron);

        jSenders.setText(Integer.toString(senders));
        jReceivers.setText(Integer.toString(receivers));
        jMerchants.setText(Integer.toString(merchants));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        jWood.setText(nf.format(wood) + " K");
        jClay.setText(nf.format(clay) + " K");
        jIron.setText(nf.format(iron) + " K");
    }

    private int getFillAmountByResourceId(int pResourceId) {
        /*
        switch (pResourceId) {
            case 0:
                return Math.min(UIHelper.parseIntFromField(jTargetWood, 400000), 400000);
            case 1:
                return Math.min(UIHelper.parseIntFromField(jTargetClay, 400000), 400000);
            default:
                return Math.min(UIHelper.parseIntFromField(jTargetIron, 400000), 400000);
            // That min is not a good idea in my mind (Patrick) for two reasons: i) some villages have higher limits (e.g. 600k bonus) ii) one might want to over fill (e.g. to produce Snobs)
        }*/
        switch (pResourceId) {
        case 0:
            return UIHelper.parseIntFromField(jTargetWood, 400000);
        case 1:
            return UIHelper.parseIntFromField(jTargetClay, 400000);
        default:
            return UIHelper.parseIntFromField(jTargetIron, 400000);
        }
    }

    private int getRemainAmountByResourceId(int pResourceId) {
        /*
        switch (pResourceId) {
        case 0:
            return Math.min(UIHelper.parseIntFromField(jRemainWood, 400000), 400000);
        case 1:
            return Math.min(UIHelper.parseIntFromField(jRemainClay, 400000), 400000);
        default:
            return Math.min(UIHelper.parseIntFromField(jRemainIron, 400000), 400000);
    }*/
         switch (pResourceId) {
            case 0:
                return UIHelper.parseIntFromField(jRemainWood, 400000);
            case 1:
                return UIHelper.parseIntFromField(jRemainClay, 400000);
            default:
                return UIHelper.parseIntFromField(jRemainIron, 400000);
        }
    }

    private int getMeanValueByResourceId(int pResourceId, int pWood, int pClay, int pIron, int pAmount) {
        switch (pResourceId) {
            case 0:
                return (int) Math.rint(pWood / pAmount);
            case 1:
                return (int) Math.rint(pClay / pAmount);
            default:
                return (int) Math.rint(pIron / pAmount);
        }
    }

    private void doCalculate() {
        if (calculator != null) {
            if (calculator.isRunning()) {
                setProblem("Berechnung läuft bereits...");
                return;
            } else if (calculator.hasResult()) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Vorherige Berechnung verwerfen?", "Berechnung verwerfen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    //not recalculate
                    return;
                } else {
                    calculator = null;
                    transportsAlreadyTransferred = false;
                }
            }
        }

        ArrayList<Village> incomingOnly = new ArrayList<>();
        ArrayList<Village> outgoingOnly = new ArrayList<>();
        int dualDirectionVillages = 0;
        VillageMerchantInfo[] allElements = ResourceDistributorSettingsPanel.getSingleton().getAllElements();
        int woodSum = 0;
        int claySum = 0;
        int ironSum = 0;

        for (VillageMerchantInfo info : allElements) {
            if (info.getDirection() == VillageMerchantInfo.Direction.INCOMING) {
                incomingOnly.add(info.getVillage());
            } else if (info.getDirection() == VillageMerchantInfo.Direction.OUTGOING) {
                outgoingOnly.add(info.getVillage());
            } else {
                dualDirectionVillages++;
            }
            woodSum += info.getWoodStock();
            claySum += info.getClayStock();
            ironSum += info.getIronStock();
        }
        int[] priorities = new int[3];
        priorities[0] = resourceNameToResourceId(jResource1.getToolTipText());
        priorities[1] = resourceNameToResourceId(jResource2.getToolTipText());
        priorities[2] = resourceNameToResourceId(jResource3.getToolTipText());

        int[] targetRes;
        int[] remainRes;
        if (isFillDistribution()) {
            targetRes = new int[]{getFillAmountByResourceId(priorities[0]),
                getFillAmountByResourceId(priorities[1]),
                getFillAmountByResourceId(priorities[2])};
            remainRes = new int[]{getRemainAmountByResourceId(priorities[0]),
                getRemainAmountByResourceId(priorities[1]),
                getRemainAmountByResourceId(priorities[2])};
        } else {
            targetRes = new int[]{getMeanValueByResourceId(priorities[0], woodSum, claySum, ironSum, allElements.length),
                getMeanValueByResourceId(priorities[1], woodSum, claySum, ironSum, allElements.length),
                getMeanValueByResourceId(priorities[2], woodSum, claySum, ironSum, allElements.length)};
            jTargetWood.setText(Integer.toString(targetRes[0]));
            jTargetClay.setText(Integer.toString(targetRes[1]));
            jTargetIron.setText(Integer.toString(targetRes[2]));
            jRemainWood.setText(Integer.toString(targetRes[0]));
            jRemainClay.setText(Integer.toString(targetRes[1]));
            jRemainIron.setText(Integer.toString(targetRes[2]));
            remainRes = new int[]{targetRes[0], targetRes[1], targetRes[2]};
        }

        int maxFilling = jFillSlider.getValue();

        
        List<VillageMerchantInfo> copy = new LinkedList<>();
        for (VillageMerchantInfo allElement : allElements) {
            VillageMerchantInfo info = allElement.clone();
            if (jAllowOverflow.isSelected()) {
                try {
                    double fillFactor = Double.parseDouble(jFillFactor.getText());
                    info.adaptStashCapacity((int) (fillFactor * 100), true);
                } catch (NumberFormatException nfex) {
                    info.adaptStashCapacity(maxFilling);
                }
            } else {
                info.adaptStashCapacity(maxFilling);
            }
            copy.add(info);
        }

        calculator = new MerchantDistributor();

        if (!jUseResource1.isSelected()) {
            priorities[0] = -1;
        }
        if (!jUseResource2.isSelected()) {
            priorities[1] = -1;
        }
        if (!jUseResource3.isSelected()) {
            priorities[2] = -1;
        }

        calculator.initialize(copy, incomingOnly, outgoingOnly, targetRes, remainRes, priorities, jLimitMerchantAmount.isSelected());
        calculator.setMerchantDistributorListener(new MerchantDistributor.MerchantDistributorListener() {

            @Override
            public void fireCalculatingResourceEvent(int pResourceId) {
                String resourceName;
                switch (pResourceId) {
                    case 0:
                        resourceName = "Holz";
                        break;
                    case 1:
                        resourceName = "Lehm";
                        break;
                    default:
                        resourceName = "Eisen";
                        break;

                }
                jProgressBar1.setString("Berechne Transporte für '" + resourceName + "'...");
            }

            @Override
            public void fireCalculationFinishedEvent() {
                setBusy(false);
                setProblem(null);
                jProgressBar1.setIndeterminate(false);
                jProgressBar1.setString("Berechnung abgeschlossen.");
            }
        });
        setBusy(true);
        calculator.start();
        jProgressBar1.setIndeterminate(true);
        jProgressBar1.setString("Berechne...");
        try {//let thread start
            Thread.sleep(20);
        } catch (InterruptedException ignored) {
        }
    }

    protected Hashtable<Village, Hashtable<Village, List<Resource>>> getTransports() {
        logger.debug("Getting transports");

        Hashtable<Village, Hashtable<Village, List<Resource>>> transports = new Hashtable<>();
        if (!calculator.getResults().isEmpty()) {
            int minAmount = 1;
            if (jIgnoreTransportsButton.isSelected()) {
                minAmount = UIHelper.parseIntFromField(jMinTransportAmount, 10000);
                logger.debug(" - MinAmount: " + minAmount);
            }

            int maxDistance = Integer.MAX_VALUE;
            if (jIgnoreTransportsByDistanceButton.isSelected()) {
                maxDistance = UIHelper.parseIntFromField(jMaxTransportDistance, 50);
                logger.debug(" - MaxDistance: " + maxDistance);
            }

            int[] priorities = new int[3];
            priorities[0] = resourceNameToResourceId(jResource1.getToolTipText());
            priorities[1] = resourceNameToResourceId(jResource2.getToolTipText());
            priorities[2] = resourceNameToResourceId(jResource3.getToolTipText());

            for (int i = 0; i < 3; i++) {
                Resource.Type current = null;
                switch (priorities[i]) {
                    case 0:
                        current = Resource.Type.WOOD;
                        break;
                    case 1:
                        current = Resource.Type.CLAY;
                        break;
                    case 2:
                        current = Resource.Type.IRON;
                        break;
                }

                List<MerchantSource> resultForResource = calculator.getResults().get(i);

                for (MerchantSource source : resultForResource) {
                    Village sourceVillage = DataHolder.getSingleton().getVillages()[source.getC().getX()][source.getC().getY()];
                    Hashtable<Village, List<Resource>> transportsForSource = transports.get(sourceVillage);

                    if (transportsForSource == null) {
                        transportsForSource = new Hashtable<>();
                        transports.put(sourceVillage, transportsForSource);
                    }

                    for (Order order : source.getOrders()) {
                        MerchantDestination dest = (MerchantDestination) order.getDestination();
                        Village targetVillage = DataHolder.getSingleton().getVillages()[dest.getC().getX()][dest.getC().getY()];
                        if (DSCalculator.calculateDistance(sourceVillage, targetVillage) <= maxDistance) {
                            List<Resource> transportsFromSourceToDest = transportsForSource.get(targetVillage);
                            if (transportsFromSourceToDest == null) {
                                transportsFromSourceToDest = new LinkedList<>();
                                transportsForSource.put(targetVillage, transportsFromSourceToDest);
                            }
                            Resource res = new Resource(order.getAmount() * 1000, current);
                            transportsFromSourceToDest.add(res);
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Ignoring Transport " + sourceVillage + " -> " + targetVillage + " due to distance");
                            }
                        }
                    }
                }
            }

            Set<Entry<Village, Hashtable<Village, List<Resource>>>> entries = transports.entrySet();

            List<Village> destinationsToRemove = new LinkedList<>();
            for (Entry<Village, Hashtable<Village, List<Resource>>> entry : entries) {
                Village transportDestination = entry.getKey();
                Hashtable<Village, List<Resource>> destinationTransports = entry.getValue();
                Set<Entry<Village, List<Resource>>> transportEntries = destinationTransports.entrySet();
                List<Village> toRemove = new LinkedList<>();
                for (Entry<Village, List<Resource>> transportEntry : transportEntries) {
                    Village sourceKey = transportEntry.getKey();
                    List<Resource> sourceTransports = transportEntry.getValue();
                    int amount = 0;
                    for (Resource res : sourceTransports) {
                        amount += res.getAmount();
                    }
                    if (amount < minAmount) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Ignoring transport " + sourceKey + " -> " + transportDestination + " due to resource amount (" + amount + "<" + minAmount + ")");
                        }
                        toRemove.add(sourceKey);
                    }
                }

                logger.debug("Removing " + toRemove.size() + " transports for destination " + transportDestination);
                for (Village rem : toRemove) {
                    destinationTransports.remove(rem);
                }

                if (transportEntries.isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removing destination " + transportDestination + " due to empty transport list");
                    }
                    destinationsToRemove.add(transportDestination);
                }
            }

            logger.debug("Removing " + destinationsToRemove.size() + " destinations");
            for (Village source : destinationsToRemove) {
                transports.remove(source);
            }
        }
        return transports;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jAllowOverflow;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JLabel jClay;
    private javax.swing.JPanel jExpertSettingsPanel;
    private javax.swing.JTextField jFillFactor;
    private javax.swing.JPanel jFillSettingsPanel;
    private javax.swing.JSlider jFillSlider;
    private javax.swing.JCheckBox jIgnoreTransportsButton;
    private javax.swing.JCheckBox jIgnoreTransportsByDistanceButton;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jIron;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JCheckBox jLimitMerchantAmount;
    private javax.swing.JTextField jMaxTransportDistance;
    private javax.swing.JLabel jMerchants;
    private javax.swing.JTextField jMinTransportAmount;
    private javax.swing.JLabel jNoSettingsLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel jReceivers;
    private com.jidesoft.swing.LabeledTextField jRemainClay;
    private com.jidesoft.swing.LabeledTextField jRemainIron;
    private com.jidesoft.swing.LabeledTextField jRemainWood;
    private javax.swing.JLabel jResource1;
    private javax.swing.JLabel jResource2;
    private javax.swing.JLabel jResource3;
    private javax.swing.JLabel jSenders;
    private javax.swing.JPanel jSettingsPanel;
    private javax.swing.JPanel jSummaryPanel;
    private javax.swing.JButton jSwitch12Button;
    private javax.swing.JButton jSwitch23Button;
    private javax.swing.JButton jSwitch23Button1;
    private com.jidesoft.swing.LabeledTextField jTargetClay;
    private com.jidesoft.swing.LabeledTextField jTargetIron;
    private com.jidesoft.swing.LabeledTextField jTargetWood;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JCheckBox jUseResource1;
    private javax.swing.JCheckBox jUseResource2;
    private javax.swing.JCheckBox jUseResource3;
    private javax.swing.JLabel jWood;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane3;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (calculator == null) {
            setProblem("Noch keine Berechnung durchgeführt");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        if (calculator != null && calculator.isRunning()) {
            setProblem("Berechnung läuft...");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }

        if (!transportsAlreadyTransferred) {
            ResourceDistributorFinishPanel.getSingleton().setup();
            transportsAlreadyTransferred = true;
        } else {
            ResourceDistributorFinishPanel.getSingleton().focusSubmit();
        }
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        if (calculator != null && calculator.isRunning()) {
            setProblem("Berechnung läuft...");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        return WizardPanelNavResult.PROCEED;

    }

    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        if (calculator != null && calculator.isRunning()) {
            setProblem("Berechnung läuft...");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }

        return WizardPanelNavResult.PROCEED;
    }
}
