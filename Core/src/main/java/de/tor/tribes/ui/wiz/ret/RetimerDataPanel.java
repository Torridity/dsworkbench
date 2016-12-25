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
package de.tor.tribes.ui.wiz.ret;

import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.*;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.models.RETAttackTableModel;
import de.tor.tribes.ui.renderer.*;
import de.tor.tribes.util.*;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.*;

/**
 *
 * @author Torridity
 */
public class RetimerDataPanel extends WizardPage {

    private static final String GENERAL_INFO = "In diesem Schritt kannst du Angriffe einfügen, die du retimen möchtest.<br>"
            + "Dies kann ein einzelner Angriffsbefehl sein oder du kannst den Angriff manuell eingeben.<br/>"
            + "Die wohl wichtigte Einstellung ist die der erwarteten Einheit. Solltest du die Einheit nicht wissen, kannst<br/>"
            + "du verschiedene Einheiten probieren und DS Workbench wird dich warnen, sofern die Einheit nicht plausibel erscheint.";
    private static RetimerDataPanel singleton = null;

    public static synchronized RetimerDataPanel getSingleton() {
        if (singleton == null) {
            singleton = new RetimerDataPanel();
        }
        return singleton;
    }

    /**
     * Creates new form AttackSourcePanel
     */
    RetimerDataPanel() {
        initComponents();
        jAttacksTable.setModel(new RETAttackTableModel());
        jAttacksTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jAttacksTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);

        jAttacksTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jAttacksTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());

        jideSplitPane1.setOrientation(JideSplitPane.VERTICAL_SPLIT);
        jideSplitPane1.setProportionalLayout(true);
        jideSplitPane1.setDividerSize(5);
        jideSplitPane1.setShowGripper(true);
        jideSplitPane1.setOneTouchExpandable(true);
        jideSplitPane1.setDividerStepSize(10);
        jideSplitPane1.setInitiallyEven(true);
        jideSplitPane1.add(jDataPanel, JideBoxLayout.FLEXIBLE);
        jideSplitPane1.add(jVillageTablePanel, JideBoxLayout.VARY);
        jideSplitPane1.getDividerAt(0).addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jideSplitPane1.setProportions(new double[]{0.5});
                }
            }
        });

        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        ActionListener panelListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Paste")) {
                    pasteFromClipboard();
                } else if (e.getActionCommand().equals("Delete")) {
                    deleteSelection();
                }
            }
        };
        jAttacksTable.registerKeyboardAction(panelListener, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttacksTable.registerKeyboardAction(panelListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        capabilityInfoPanel1.addActionListener(panelListener);

        jAttacksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRows = jAttacksTable.getSelectedRowCount();
                if (selectedRows != 0) {
                    jStatusLabel.setText(selectedRows + " Dorf/Dörfer gewählt");
                }
            }
        });

        ChangeListener cl = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                recalculateArriveTime();
            }
        };

        jSourceCoord.addChangeListener(cl);
        jTargetCoord.addChangeListener(cl);
        jArriveTime.setActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                recalculateArriveTime();
            }
        });

        jInfoTextPane.setText(GENERAL_INFO);
        jUnitBox.setRenderer(new UnitListCellRenderer());
        jWarningLabel.setVisible(false);
    }

    public static String getDescription() {
        return "Angriffe";
    }

    public static String getStep() {
        return "id-ret-attacks";
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
    }

    public void restoreProperties() {
        getModel().clear();
        jUnitBox.setModel(new DefaultComboBoxModel(DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{})));
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
        jDataPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jUnitBox = new javax.swing.JComboBox();
        jArriveTime = new de.tor.tribes.ui.components.DateTimeField();
        jSendTime = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jSourceCoord = new de.tor.tribes.ui.components.CoordinateSpinner();
        jTargetCoord = new de.tor.tribes.ui.components.CoordinateSpinner();
        jWarningLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jAttackBox = new javax.swing.JComboBox();
        jButton4 = new javax.swing.JButton();
        jVillageTablePanel = new javax.swing.JPanel();
        jTableScrollPane = new javax.swing.JScrollPane();
        jAttacksTable = new org.jdesktop.swingx.JXTable();
        jStatusLabel = new javax.swing.JLabel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jideSplitPane1 = new com.jidesoft.swing.JideSplitPane();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html");
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        jDataPanel.setMinimumSize(new java.awt.Dimension(0, 130));
        jDataPanel.setPreferredSize(new java.awt.Dimension(0, 130));
        jDataPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Manuelle Eingabe"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Herkunft");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Ziel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Ankunft");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Vermutete Einheit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel5, gridBagConstraints);

        jLabel6.setText("Errechnete Abschickzeit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel6, gridBagConstraints);

        jUnitBox.setMinimumSize(new java.awt.Dimension(51, 22));
        jUnitBox.setPreferredSize(new java.awt.Dimension(56, 22));
        jUnitBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUnitChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jUnitBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jArriveTime, gridBagConstraints);

        jSendTime.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jSendTime.setText("-unbekannt-");
        jSendTime.setToolTipText("");
        jSendTime.setMaximumSize(new java.awt.Dimension(34, 22));
        jSendTime.setMinimumSize(new java.awt.Dimension(34, 22));
        jSendTime.setPreferredSize(new java.awt.Dimension(34, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jSendTime, gridBagConstraints);

        jButton3.setText("Angriff hinzufügen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireAddAttackEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        jPanel3.add(jButton3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jSourceCoord, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jTargetCoord, gridBagConstraints);

        jWarningLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/warning.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jWarningLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jPanel3, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Sonstige Quellen"));
        jPanel2.setMinimumSize(new java.awt.Dimension(200, 87));
        jPanel2.setPreferredSize(new java.awt.Dimension(200, 87));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_overview.png"))); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(60, 40));
        jButton1.setMinimumSize(new java.awt.Dimension(60, 40));
        jButton1.setPreferredSize(new java.awt.Dimension(60, 40));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireSearchPlainAttackFromClipboardEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jButton1, gridBagConstraints);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/sos_clipboard.png"))); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(60, 40));
        jButton2.setMinimumSize(new java.awt.Dimension(60, 40));
        jButton2.setPreferredSize(new java.awt.Dimension(60, 40));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireReadSOSFromClipboardEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(jButton2, gridBagConstraints);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Unbekannte Einheit"));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jAttackBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Keine" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jAttackBox, gridBagConstraints);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/prev.png"))); // NOI18N
        jButton4.setToolTipText("Einheit manuell bestimmen");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireSetSelectedSOSAttackEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jButton4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDataPanel.add(jPanel1, gridBagConstraints);

        jVillageTablePanel.setLayout(new java.awt.GridBagLayout());

        jTableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Verwendete Dörfer"));
        jTableScrollPane.setMinimumSize(new java.awt.Dimension(23, 100));
        jTableScrollPane.setPreferredSize(new java.awt.Dimension(23, 100));

        jAttacksTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTableScrollPane.setViewportView(jAttacksTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jTableScrollPane, gridBagConstraints);

        jStatusLabel.setMaximumSize(new java.awt.Dimension(0, 16));
        jStatusLabel.setMinimumSize(new java.awt.Dimension(0, 16));
        jStatusLabel.setPreferredSize(new java.awt.Dimension(0, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jStatusLabel, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(capabilityInfoPanel1, gridBagConstraints);

        setLayout(new java.awt.GridBagLayout());

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jXCollapsiblePane1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Informationen einblenden");
        jLabel1.setToolTipText("Blendet Informationen zu dieser Ansicht und zu den Datenquellen ein/aus");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jideSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        if (jXCollapsiblePane1.isCollapsed()) {
            jXCollapsiblePane1.setCollapsed(false);
            jLabel1.setText("Informationen ausblenden");
        } else {
            jXCollapsiblePane1.setCollapsed(true);
            jLabel1.setText("Informationen einblenden");
        }
    }//GEN-LAST:event_fireHideInfoEvent

    private void fireUnitChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireUnitChangedEvent
        recalculateArriveTime();
    }//GEN-LAST:event_fireUnitChangedEvent

    private void fireSearchPlainAttackFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSearchPlainAttackFromClipboardEvent
        readPlainAttackFromClipboard();
    }//GEN-LAST:event_fireSearchPlainAttackFromClipboardEvent

    private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
        Village source = jSourceCoord.getVillage();
        Village target = jTargetCoord.getVillage();
        Date arrive = jArriveTime.getSelectedDate();
        UnitHolder unit = (UnitHolder) jUnitBox.getSelectedItem();
        if (source == null) {
            jStatusLabel.setText("Kein gültiges Herkunftsdorf angegeben");
            return;
        }
        if (target == null) {
            jStatusLabel.setText("Kein gültiges Zieldorf angegeben");
            return;
        }

        if (unit == null) {
            jStatusLabel.setText("Keine gültige Einheit angegeben");
            return;
        }
        Attack a = new Attack();
        a.setSource(source);
        a.setTarget(target);
        a.setArriveTime(arrive);
        a.setUnit(unit);
        getModel().addRow(a);
    }//GEN-LAST:event_fireAddAttackEvent

    private void fireReadSOSFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireReadSOSFromClipboardEvent
        readSOSFromClipboard();
    }//GEN-LAST:event_fireReadSOSFromClipboardEvent

    private void fireSetSelectedSOSAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSetSelectedSOSAttackEvent
        Object element = jAttackBox.getSelectedItem();

        if (element == null || !(element instanceof TimedAttackListEntry)) {
            return;
        }
        TimedAttackListEntry selection = (TimedAttackListEntry) element;
        ((DefaultComboBoxModel) jAttackBox.getModel()).removeElement(selection);
        if (jAttackBox.getModel().getSize() == 0) {
            DefaultComboBoxModel model = new DefaultComboBoxModel(new Object[]{"Keine"});
            jAttackBox.setModel(model);
        }
        jSourceCoord.setValue(selection.getAttack().getSource().getPosition());
        jTargetCoord.setValue(selection.getTarget().getPosition());
        jArriveTime.setDate(new Date(selection.getAttack().getlArriveTime()));
        jUnitBox.setSelectedItem(DataHolder.getSingleton().getUnitByPlainName("ram"));

    }//GEN-LAST:event_fireSetSelectedSOSAttackEvent

    private void readPlainAttackFromClipboard() {
        try {
            String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            readAttackFromString(data);
        } catch (HeadlessException | IOException | UnsupportedFlavorException ignored) {
        }
    }

    private void readSOSFromClipboard() {
        try {
            String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            List<SOSRequest> sos = PluginManager.getSingleton().executeSOSParserParser(data);
            DefaultComboBoxModel model = null;
            if (sos.isEmpty()) {
                model = new DefaultComboBoxModel(new Object[]{"Keine"});
            } else {
                model = new DefaultComboBoxModel();
                for (SOSRequest request : sos) {
                    Enumeration<Village> targets = request.getTargets();
                    while (targets.hasMoreElements()) {
                        Village target = targets.nextElement();
                        TargetInformation info = request.getTargetInformation(target);
                        for (TimedAttack attack : info.getAttacks()) {
                            if (attack.getUnit() == null || attack.getUnit().equals(UnknownUnit.getSingleton())) {
                                model.addElement(new TimedAttackListEntry(target, attack));
                            } else {
                                Attack a = new Attack();
                                a.setSource(attack.getSource());
                                a.setTarget(target);
                                a.setArriveTime(new Date(attack.getlArriveTime()));
                                a.setUnit(attack.getUnit());
                                getModel().addRow(a);
                            }
                        }
                    }
                }
            }
            jAttackBox.setModel(model);
        } catch (HeadlessException | IOException | UnsupportedFlavorException ignored) {
        }
    }

    public boolean readAttackFromString(String pData) {
        boolean result = true;
        List<Village> villages = PluginManager.getSingleton().executeVillageParser(pData);
        if (villages == null || villages.isEmpty() || villages.size() < 2) {
            jWarningLabel.setText("Kein gültiger Angriff gefunden");
            jWarningLabel.setVisible(true);
        } else {
            Village source = villages.get(0);
            Village target = villages.get(1);
            if (pData.contains(PluginManager.getSingleton().getVariableValue("sos.arrive.time"))) {
                //change village order for SOS requests
                source = villages.get(1);
                target = villages.get(0);
            }

            jSourceCoord.setValue(new Point(source.getX(), source.getY()));
            jTargetCoord.setValue(new Point(target.getX(), target.getY()));

            Date arriveDate;
            try {
                String text = pData;
                String arrive;
                String arriveLine;
                if (text.contains(PluginManager.getSingleton().getVariableValue("attack.arrive.time"))) {
                    String searchString = PluginManager.getSingleton().getVariableValue("attack.arrive.time");
                    arriveLine = text.substring(text.indexOf(PluginManager.getSingleton().getVariableValue("attack.arrive.time")) + searchString.length());
                } else {
                    String searchString = PluginManager.getSingleton().getVariableValue("sos.arrive.time");
                    arriveLine = text.substring(text.indexOf(PluginManager.getSingleton().getVariableValue("sos.arrive.time")) + searchString.length());
                }

                StringTokenizer tokenizer = new StringTokenizer(arriveLine, "\n");
                String date = tokenizer.nextToken();
                arrive = date.trim();

                SimpleDateFormat f;
                if (!ServerSettings.getSingleton().isMillisArrival()) {
                    f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
                } else {
                    f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
                }
                arriveDate = f.parse(arrive);
                jArriveTime.setDate(arriveDate);
                if (arriveDate == null) {
                    jWarningLabel.setText("Kann Ankunftzeit nicht lesen");
                    jWarningLabel.setVisible(true);
                    return false;
                }
            } catch (Exception ignored) {
                jWarningLabel.setText("Kein gültiger Angriff gefunden");
                jWarningLabel.setVisible(true);
                return false;
            }
            //calc possible units
            double dist = DSCalculator.calculateDistance(source, target);
            String[] units = new String[]{"axe", "sword", "spy", "light", "heavy", "ram", "knight", "snob"};
            List<UnitHolder> possibleUnits = new LinkedList<>();
            for (String unit : units) {
                UnitHolder unitHolder = DataHolder.getSingleton().getUnitByPlainName(unit);
                if (!unitHolder.equals(UnknownUnit.getSingleton())) {
                    long dur = (long) Math.floor(dist * unitHolder.getSpeed() * 60000.0);
                    if (arriveDate.getTime() - dur <= System.currentTimeMillis()) {
                        possibleUnits.add(unitHolder);
                    }
                }
            }

            if (arriveDate.getTime() < System.currentTimeMillis()) {
                jWarningLabel.setText("Ankunft in der Vergangenheit. Bitte Zeit prüfen.");
                jWarningLabel.setVisible(true);
                result = false;
            } else {
                if (possibleUnits.isEmpty()) {
                    jWarningLabel.setText("Keine mögliche Einheit gefunden");
                    jWarningLabel.setVisible(true);
                    result = false;
                } else {
                    String unitString = "";
                    for (UnitHolder unit : possibleUnits) {
                        unitString += unit.getName() + ", ";
                    }
                    jWarningLabel.setText("Mögliche Einheiten: " + unitString.substring(0, unitString.lastIndexOf(",")));
                    jWarningLabel.setVisible(true);
                    result = true;
                }
            }

            UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
            UnitHolder axe = DataHolder.getSingleton().getUnitByPlainName("axe");
            UnitHolder spy = DataHolder.getSingleton().getUnitByPlainName("spy");
            if (possibleUnits.contains(ram)) {
                jUnitBox.setSelectedItem(ram);
            } else if (possibleUnits.contains(axe)) {
                jUnitBox.setSelectedItem(axe);
            } else {
                jUnitBox.setSelectedItem(spy);
            }
        }
        return result;
    }

    private RETAttackTableModel getModel() {
        return (RETAttackTableModel) jAttacksTable.getModel();
    }

    private void recalculateArriveTime() {
        Village source = jSourceCoord.getVillage();
        String result;
        if (source == null) {
            result = "-unbekannt-";
            jWarningLabel.setVisible(true);
            jWarningLabel.setText("Herkunftsdorf ungültig");
        } else {
            Village target = jTargetCoord.getVillage();
            if (target == null) {
                result = "-unbekannt-";
                jWarningLabel.setVisible(true);
                jWarningLabel.setText("Zieldorf ungültig");
            } else {
                Date arriveTime = jArriveTime.getSelectedDate();
                UnitHolder unit = (UnitHolder) jUnitBox.getSelectedItem();

                long runtime = DSCalculator.calculateMoveTimeInMillis(source, target, unit.getSpeed());
                Date send = new Date(arriveTime.getTime() - runtime);
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                result = f.format(send);
                if (arriveTime.getTime() < System.currentTimeMillis()) {
                    jWarningLabel.setVisible(true);
                    jWarningLabel.setText("Ankunft in der Vergangenheit. Bitte Zeit prüfen.");
                } else if (send.getTime() > System.currentTimeMillis()) {
                    jWarningLabel.setVisible(true);
                    jWarningLabel.setText("Abschickzeit in der Zukunft. Langsamere Einheit wählen?");
                } else {
                    jWarningLabel.setVisible(false);
                }
            }
        }
        jSendTime.setText(result);
    }

    private void pasteFromClipboard() {
        readPlainAttackFromClipboard();
    }

    private void deleteSelection() {
        int[] selection = jAttacksTable.getSelectedRows();
        if (selection.length > 0) {
            List<Integer> rows = new LinkedList<>();
            for (int i : selection) {
                rows.add(jAttacksTable.convertRowIndexToModel(i));
            }
            Collections.sort(rows);
            for (int i = rows.size() - 1; i >= 0; i--) {
                getModel().removeRow(rows.get(i));
            }
            jStatusLabel.setText(selection.length + " Angriff(e) entfernt");
            if (getModel().getRowCount() == 0) {
                setProblem("Keine Angriffe gewählt");
            }
        }
    }

    public Attack[] getAttacks() {
        RETAttackTableModel model = getModel();
        List<Attack> rows = new LinkedList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            rows.add(model.getRow(i));
        }
        return rows.toArray(new Attack[rows.size()]);
    }

    public Attack[] getAllElements() {
        List<Attack> result = new LinkedList<>();
        RETAttackTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            result.add(model.getRow(i));
        }
        return result.toArray(new Attack[result.size()]);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private de.tor.tribes.ui.components.DateTimeField jArriveTime;
    private javax.swing.JComboBox jAttackBox;
    private org.jdesktop.swingx.JXTable jAttacksTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jDataPanel;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jSendTime;
    private de.tor.tribes.ui.components.CoordinateSpinner jSourceCoord;
    private javax.swing.JLabel jStatusLabel;
    private javax.swing.JScrollPane jTableScrollPane;
    private de.tor.tribes.ui.components.CoordinateSpinner jTargetCoord;
    private javax.swing.JComboBox jUnitBox;
    private javax.swing.JPanel jVillageTablePanel;
    private javax.swing.JLabel jWarningLabel;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private com.jidesoft.swing.JideSplitPane jideSplitPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getAllElements().length == 0) {
            setProblem("Keine Angriffe eingelesen");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }

        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;

    }

    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }
}

class TimedAttackListEntry {

    private Village target = null;
    private TimedAttack attack = null;

    public TimedAttackListEntry(Village pTarget, TimedAttack pAttack) {
        target = pTarget;
        attack = pAttack;
    }

    public Village getTarget() {
        return target;
    }

    public TimedAttack getAttack() {
        return attack;
    }

    @Override
    public String toString() {
        return attack.getSource() + " -> " + target;
    }
}
