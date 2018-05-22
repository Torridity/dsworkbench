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
package de.tor.tribes.ui.wiz.dep;

import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.TimedAttack;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.models.DefenseToolModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.DefenseStatusTableCellRenderer;
import de.tor.tribes.ui.renderer.LossRatioTableCellRenderer;
import de.tor.tribes.ui.renderer.TendencyTableCellRenderer;
import de.tor.tribes.ui.views.DSWorkbenchSOSRequestAnalyzer;
import de.tor.tribes.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.*;

/**
 *
 * @author Torridity
 */
public class DefenseAnalysePanel extends WizardPage {

    private static final String GENERAL_INFO = "Du befindest dich in der Angriffsanalyse. In diesem Schritt kannst du eingelesene SOS-Anfragen "
            + "aus dem SOS-Analyzer in den Verteidigungsplaner übertragen und bei Bedarf einzelne von der Berechnung ausschließen, indem du sie "
            + "per ENTF löscht.";
    private static DefenseAnalysePanel singleton = null;
    private final NumberFormat numFormat = NumberFormat.getInstance();
    private VillageOverviewMapPanel overviewPanel = null;

    public static synchronized DefenseAnalysePanel getSingleton() {
        if (singleton == null) {
            singleton = new DefenseAnalysePanel();
        }
        return singleton;
    }

    /**
     * Creates new form AttackSourcePanel
     */
    DefenseAnalysePanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        jButton1.setIcon(new ImageIcon("./graphics/big/lifebelt.png"));
        numFormat.setMaximumFractionDigits(0);
        numFormat.setMinimumFractionDigits(0);
        jxAttacksTable.setModel(new DefenseToolModel());
        jxAttacksTable.getColumnExt("Tendenz").setCellRenderer(new TendencyTableCellRenderer());
        jxAttacksTable.getColumnExt("Status").setCellRenderer(new DefenseStatusTableCellRenderer());
        jxAttacksTable.getColumnExt("Verlustrate").setCellRenderer(new LossRatioTableCellRenderer());
        jxAttacksTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jxAttacksTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jxAttacksTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        overviewPanel = new VillageOverviewMapPanel();
        jPanel6.add(overviewPanel, BorderLayout.CENTER);

        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Delete")) {
                    deleteSelection();
                }
            }
        };
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jxAttacksTable.registerKeyboardAction(listener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        capabilityInfoPanel1.addActionListener(listener);
    }

    public static String getDescription() {
        return "Analyse";
    }

    public static String getStep() {
        return "id-defense-analyze";
    }

    public DefenseToolModel getModel() {
        return TableHelper.getTableModel(jxAttacksTable);
    }

    public void setData(List<DefenseInformation> pDefenses) {
        overviewPanel.reset();
        DefenseToolModel model = getModel();
        model.clear();
        for (DefenseInformation defense : pDefenses) {
            if (!defense.isSave()) {
                Village target = defense.getTarget();
                overviewPanel.addVillage(target, Color.RED);
                model.addRow(defense);
                for (TimedAttack a : defense.getTargetInformation().getAttacks()) {
                    overviewPanel.addVillage(a.getSource(), Color.BLACK);
                }
            }
        }
        getModel().fireTableDataChanged();
        overviewPanel.repaint();
    }

    private void deleteSelection() {
        int[] selection = jxAttacksTable.getSelectedRows();
        if (selection.length > 0) {
            List<Integer> rows = new LinkedList<>();
            for (int i : selection) {
                rows.add(jxAttacksTable.convertRowIndexToModel(i));
            }
            Collections.sort(rows);
            for (int i = rows.size() - 1; i >= 0; i--) {
                getModel().removeRow(rows.get(i));
            }
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
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        jTableScrollPane = new javax.swing.JScrollPane();
        jxAttacksTable = new org.jdesktop.swingx.JXTable();
        jButton1 = new javax.swing.JButton();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html");
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

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

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel6.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel6.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 5, 5);
        jPanel2.add(jPanel6, gridBagConstraints);

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/search.png"))); // NOI18N
        jToggleButton1.setToolTipText("Informationskarte vergrößern");
        jToggleButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeViewEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jToggleButton1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jTableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Angegriffene Dörfer"));

        jxAttacksTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableScrollPane.setViewportView(jxAttacksTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jTableScrollPane, gridBagConstraints);

        jButton1.setToolTipText("Angriffsdaten aus dem SOS-Analyzer übertragen");
        jButton1.setMaximumSize(new java.awt.Dimension(73, 60));
        jButton1.setMinimumSize(new java.awt.Dimension(73, 60));
        jButton1.setPreferredSize(new java.awt.Dimension(73, 60));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireLoadAttackInformationEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jButton1, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(capabilityInfoPanel1, gridBagConstraints);

        jXLabel1.setForeground(new java.awt.Color(102, 102, 102));
        jXLabel1.setText("Bitte beachte, dass bereits als \"Sicher\" gekennzeichnete Angriffsziele nicht übertragen werden. Möchtest du bestimmte Ziele ignorieren, so setze sie im SOS-Analyzer auf \"Sicher\". Dieser Status bleibt bestehen, solange keine neuen Angriffe für diese Ziele auftauchen und eingelesen werden.");
        jXLabel1.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jXLabel1.setLineWrap(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel1.add(jXLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
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

    private void fireChangeViewEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeViewEvent
        if (jToggleButton1.isSelected()) {
            overviewPanel.setOptimalSize();
            jTableScrollPane.setViewportView(overviewPanel);
            jPanel6.remove(overviewPanel);
        } else {
            jTableScrollPane.setViewportView(jxAttacksTable);
            jPanel6.add(overviewPanel, BorderLayout.CENTER);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    jPanel6.updateUI();
                }
            });
        }
    }//GEN-LAST:event_fireChangeViewEvent

    private void fireLoadAttackInformationEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireLoadAttackInformationEvent
        if (!DSWorkbenchSOSRequestAnalyzer.getSingleton().sendDataToDefensePlaner()) {
            JOptionPaneHelper.showInformationBox(this, "Der SOS-Analyzer enthält keine Angriffe.\nBitte lies zuerst SOS-Anfragen ein.", "Information");
        }
    }//GEN-LAST:event_fireLoadAttackInformationEvent

    public int[] getDefenseInfo() {
        int targets = 0;
        int offs = 0;
        int fakes = 0;
        int needed = 0;

        for (DefenseInformation element : getModel().getRows()) {
            targets++;
            needed += element.getNeededSupports();
            offs += element.getAttackCount() - element.getFakeCount();
            fakes += element.getFakeCount();
        }
        return new int[]{targets, offs, fakes, needed};
    }

    public DefenseInformation[] getAllElements() {
        return getModel().getRows();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jTableScrollPane;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXTable jxAttacksTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getModel().getRowCount() == 0) {
            setProblem("Keine Angriffe vorhanden. Lies bitte zuerst SOS-Anfragen im SOS-Analyzer ein.");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        DefenseSourcePanel.getSingleton().update();
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
