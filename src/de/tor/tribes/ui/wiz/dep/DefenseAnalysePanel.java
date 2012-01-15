/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AttackSourcePanel.java
 *
 * Created on Oct 15, 2011, 9:54:36 AM
 */
package de.tor.tribes.ui.wiz.dep;

import de.tor.tribes.dssim.algo.NewSimulator;
import de.tor.tribes.dssim.types.AbstractUnitElement;
import de.tor.tribes.dssim.types.KnightItem;
import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.dssim.types.UnitHolder;
import de.tor.tribes.dssim.util.UnitManager;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.TargetInformation;
import de.tor.tribes.types.TimedAttack;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.TroopSelectionPanel;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.models.DefenseToolModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.DefenseStatusTableCellRenderer;
import de.tor.tribes.ui.renderer.LossRatioTableCellRenderer;
import de.tor.tribes.ui.renderer.TendencyTableCellRenderer;
import de.tor.tribes.ui.renderer.WallLevellCellRenderer;
import de.tor.tribes.ui.util.ColorGradientHelper;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.TableHelper;
import de.tor.tribes.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanel;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class DefenseAnalysePanel extends javax.swing.JPanel implements WizardPanel {

    private static final String GENERAL_INFO = "Du befindest dich in der Angriffsanalyse. Hier kannst du pr&uuml;fen bzw. festlegen, bei welchen Angriffen eine Verteidung sinnvoll bzw. notwendig ist. "
            + "In der Tabelle sind alle aktuellen Angriffe aufgelistet, anfangs sind lediglich die Informationen enhalten, die aus den SOS-Anfragen gewonnen wurden. "
            + "Um die notwendigen Unterst&uuml;tzungen zu berechnen, trage zuerst die vermuteten Truppen des Angreifers ein. In den meisten F&auml;llen gen&uuml;gen die Voreinstellungen um ein ausreichend genaues Ergebnis zu erzielen. "
            + "Anschlie&szlig;end werden die Truppen einer Einzelverteidigung ben&ouml;tigt. Diese Werte entscheiden, wieviele Unterst&uuml;tzungen auf ein Dorf laufen m&uuml;ssen, bis es sicher ist. "
            + "Kleine Werte bewirken, dass bei Verlusten in einzelnen D&ouml;rfern nur wenige Truppen nachgebaut werden m&uuml;ssen. Bei gro&szlig;en Werten m&uuml;ssen weniger Unterst&uuml;tzungen geschickt werden. "
            + "Zuletzt kann man angeben, wieviele Einzelunterst&uuml;tzungen maximal geschickt werden sollen und wieviele Verluste man in einem verteidigten Dorf maximal zulassen m&ouml;chte. "
            + "Hier k&ouml;nnen meist die Standardeinstellungen verwendet werden. Um die notwendigen Unterst&uuml;tzungen zu berechnen, muss man nun noch auf 'Aktualisieren' klicken."
            + "</html>";
    private static DefenseAnalysePanel singleton = null;
    private WizardController controller = null;
    private final NumberFormat numFormat = NumberFormat.getInstance();
    private VillageOverviewMapPanel overviewPanel = null;

    public static synchronized DefenseAnalysePanel getSingleton() {
        if (singleton == null) {
            singleton = new DefenseAnalysePanel();
        }
        return singleton;
    }

    /** Creates new form AttackSourcePanel */
    DefenseAnalysePanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
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
    }

    public void setController(WizardController pWizCtrl) {
        controller = pWizCtrl;
    }

    public DefenseToolModel getModel() {
        return TableHelper.getTableModel(jxAttacksTable);
    }

    public void setData(List<DefenseInformation> pDefenses) {
        overviewPanel.reset();
        DefenseToolModel model = getModel();
        model.clear();
        for (DefenseInformation defense : pDefenses) {
            Village target = defense.getTarget();
            overviewPanel.addVillage(target, Color.RED);
            model.addRow(defense);
            for (TimedAttack a : defense.getTargetInformation().getAttacks()) {
                overviewPanel.addVillage(a.getSource(), Color.BLACK);
            }
        }
        getModel().fireTableDataChanged();
        overviewPanel.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
        jTableScrollPane = new javax.swing.JScrollPane();
        jxAttacksTable = new org.jdesktop.swingx.JXTable();
        jPanel6 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();

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
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jTableScrollPane, gridBagConstraints);

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel6.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel6.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
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
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jToggleButton1, gridBagConstraints);

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

                public void run() {
                    jPanel6.updateUI();
                }
            });
        }
    }//GEN-LAST:event_fireChangeViewEvent

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
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jTableScrollPane;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private org.jdesktop.swingx.JXTable jxAttacksTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
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
