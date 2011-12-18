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
import de.tor.tribes.types.DefenseElement;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.TroopSelectionPanel;
import de.tor.tribes.ui.models.DefenseToolModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefenseStatusTableCellRenderer;
import de.tor.tribes.ui.renderer.LossRatioTableCellRenderer;
import de.tor.tribes.ui.renderer.TendencyTableCellRenderer;
import de.tor.tribes.ui.renderer.WallLevellCellRenderer;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.UIHelper;
import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanel;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class AnalysePanel extends javax.swing.JPanel implements WizardPanel {

    private static final String GENERAL_INFO = "Du befindest dich in der Angriffsanalyse. Hier kannst du pr&uuml;fen bzw. festlegen, bei welchen Angriffen eine Verteidung sinnvoll bzw. notwendig ist."
            + "In der Tabelle sind alle aktuellen Angriffe aufgelistet, anfangs sind lediglich die Informationen enhalten, die aus den SOS-Anfragen gewonnen wurden."
            + "Um die notwendigen Unterst&uuml;tzungen zu berechnen, trage zuerst die vermuteten Truppen des Angreifers ein. In den meisten F&auml;llen gen&uuml;gen die Voreinstellungen um ein ausreichend genaues Ergebnis zu erzielen."
            + "Anschlie&szlig;end werden die Truppen einer Einzelverteidigung ben&ouml;tigt. Diese Werte entscheiden, wieviele Unterst&uuml;tzungen auf ein Dorf laufen m&uuml;ssen, bis es sicher ist."
            + "Kleine Werte bewirken, dass bei Verlusten in einzelnen D&ouml;rfern nur wenige Truppen nachgebaut werden m&uuml;ssen. Bei gro&szlig;en Werten m&uuml;ssen weniger Unterst&uuml;tzungen geschickt werden. "
            + "Zuletzt kann man angeben, wieviele Einzelunterst&uuml;tzungen maximal geschickt werden sollen und wieviele Verluste man in einem verteidigten Dorf maximal zulassen m&ouml;chte."
            + "Hier k&ouml;nnen meist die Standardeinstellungen verwendet werden. Um die notwendigen Unterst&uuml;tzungen zu berechnen, muss man nun noch auf 'Aktualisieren' klicken."
            + "</html>";
    private static AnalysePanel singleton = null;
    private WizardController controller = null;
    private final NumberFormat numFormat = NumberFormat.getInstance();
    private boolean aborted = false;
    private boolean calculating = false;
    private TroopSelectionPanel defensePanel = null;
    private TroopSelectionPanel offensePanel = null;

    public static synchronized AnalysePanel getSingleton() {
        if (singleton == null) {
            singleton = new AnalysePanel();
        }
        return singleton;
    }

    /** Creates new form AttackSourcePanel */
    AnalysePanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        numFormat.setMaximumFractionDigits(0);
        numFormat.setMinimumFractionDigits(0);
        jXTable1.setModel(new DefenseToolModel());
        jXTable1.getColumnExt("Tendenz").setCellRenderer(new TendencyTableCellRenderer());
        jXTable1.getColumnExt("Status").setCellRenderer(new DefenseStatusTableCellRenderer());
        jXTable1.getColumnExt("Wall").setCellRenderer(new WallLevellCellRenderer());
        jXTable1.getColumnExt("Verlustrate").setCellRenderer(new LossRatioTableCellRenderer());
        jXTable1.setDefaultRenderer(Date.class, new DateCellRenderer());
        offensePanel = new TroopSelectionPanel();
        offensePanel.setupOffense(true);
        Hashtable<de.tor.tribes.io.UnitHolder, Integer> offAmounts = new Hashtable<de.tor.tribes.io.UnitHolder, Integer>();
        offAmounts.put(DataHolder.getSingleton().getUnitByPlainName("axe"), 7000);
        offAmounts.put(DataHolder.getSingleton().getUnitByPlainName("light"), 2300);
        offAmounts.put(DataHolder.getSingleton().getUnitByPlainName("ram"), 250);
        offensePanel.setAmounts(offAmounts);
        jPanel4.add(offensePanel, BorderLayout.CENTER);
        defensePanel = new TroopSelectionPanel();
        defensePanel.setupDefense(true);
        jPanel5.add(defensePanel, BorderLayout.CENTER);
    }

    public void setController(WizardController pWizCtrl) {
        controller = pWizCtrl;
    }

    public DefenseToolModel getModel() {
        return (DefenseToolModel) jXTable1.getModel();
    }

    public void setData(List<SOSRequest> pRequests) {

        for (SOSRequest request : pRequests) {
            Enumeration<Village> targets = request.getTargets();
            while (targets.hasMoreElements()) {
                Village target = targets.nextElement();
                DefenseElement elem = getModel().findElement(target);
                if (elem != null) {
                    SOSRequest.TargetInformation newInfo = elem.getTargetInformation().merge(request.getTargetInformation(target), null);
                    elem.setTargetInformation(newInfo);
                } else {
                    elem = new DefenseElement();
                    elem.setTarget(target);
                    elem.setTargetInformation(request.getTargetInformation(target));
                    getModel().addRow(elem);
                }
            }
        }
        getModel().fireTableDataChanged();
    }

    private void updateStatus() {
        jLabel2.setText("Aktualisiere Verteidigungsstatus");
        for (DefenseElement element : getModel().getRows()) {
            if (aborted) {
                break;
            }
            SOSRequest.TargetInformation info = element.getTargetInformation();
            try {
                UnitManager.getSingleton().parseUnits(GlobalOptions.getSelectedServer());
            } catch (Exception e) {
            }
            //sim off and def
            Hashtable<UnitHolder, AbstractUnitElement> def = new Hashtable<UnitHolder, AbstractUnitElement>();
            Hashtable<UnitHolder, AbstractUnitElement> off = getStandardOff();
            //pre-fill tables
            for (de.tor.tribes.io.UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                def.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
            }

            int attCount = info.getOffs();

            Hashtable<de.tor.tribes.io.UnitHolder, Integer> troops = info.getTroops();
            Enumeration<de.tor.tribes.io.UnitHolder> units = troops.keys();

            int pop = 0;
            while (units.hasMoreElements()) {
                de.tor.tribes.io.UnitHolder unit = units.nextElement();
                int amount = troops.get(unit);
                def.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), amount, 10));
                pop += unit.getPop() * amount;
            }

            NewSimulator sim = new NewSimulator();
            boolean noAttack = (attCount == 0);

            SimulatorResult result = sim.calculate(off, def, KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM), Arrays.asList(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM)), false, 0, 100.0, 20, 0, 30, true, true, false, false, false);
            int cleanAfter = 0;
            for (int i = 1; i < attCount; i++) {
                if (result.isWin()) {
                    cleanAfter = i + 1;
                    break;
                }
                result = sim.calculate(off, result.getSurvivingDef(), KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM), Arrays.asList(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM)), false, 0, 100.0, result.getWallLevel(), 0, 30, true, true, false, false, false);
            }
            double lossPercent = 0;
            if (!noAttack) {
                if (!result.isWin()) {
                    double survive = 0;
                    Enumeration<UnitHolder> keys = result.getSurvivingDef().keys();
                    while (keys.hasMoreElements()) {
                        UnitHolder key = keys.nextElement();
                        int amount = result.getSurvivingDef().get(key).getCount();
                        survive += (double) amount * key.getPop();
                    }
                    lossPercent = 100 - (100.0 * survive / (double) pop);
                    if (Math.max(75.0, lossPercent) == lossPercent) {
                        element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.DANGEROUS);
                        element.setLossRation(lossPercent);
                    } else if (Math.max(25.0, lossPercent) == 25.0) {
                        element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.SAVE);
                        element.setLossRation(lossPercent);
                    } else {
                        element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.FINE);
                        element.setLossRation(lossPercent);
                    }
                } else {
                    element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.DANGEROUS);
                    element.setLossRation(100.0);
                    element.setCleanAfter(cleanAfter);
                }
            } else {
                element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.SAVE);
                element.setLossRation(0.0);
            }
        }
        getModel().fireTableDataChanged();
    }

    private void calculateNeededSupports() {
        int cnt = 1;
        int maxRuns = UIHelper.parseIntFromField(jMaxRuns, 500);
        int maxLossRatio = UIHelper.parseIntFromField(jMaxLossRatio, 30);
        jProgressBar1.setMaximum(getModel().getRowCount());
        for (DefenseElement element : getModel().getRows()) {
            if (aborted) {
                break;
            }
            jLabel2.setText("Analysiere Angriff " + cnt + "/" + getModel().getRowCount());
            jProgressBar1.setValue(cnt);
            cnt++;
            SOSRequest.TargetInformation info = element.getTargetInformation();
            try {
                UnitManager.getSingleton().parseUnits(GlobalOptions.getSelectedServer());
            } catch (Exception e) {
            }

            NewSimulator sim = new NewSimulator();
            int attCount = info.getOffs();

            //no atts for this target...don't know why...
            if (attCount == 0) {
                continue;
            }

            int factor = 1;
            SimulatorResult result = null;
            while (true) {
                Hashtable<UnitHolder, AbstractUnitElement> off = getStandardOff();
                Hashtable<UnitHolder, AbstractUnitElement> def = getDefenseAmount(factor);
                double troops = 0;
                Set<Entry<UnitHolder, AbstractUnitElement>> entries = def.entrySet();
                for (Entry<UnitHolder, AbstractUnitElement> entry : entries) {
                    UnitHolder unit = entry.getKey();
                    troops += unit.getPop() * entry.getValue().getCount();
                }

                result = sim.calculate(off, def, KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM), Arrays.asList(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM)), false, 0, 100.0, info.getWallLevel(), 0, 30, true, true, false, false, false);
                for (int i = 1; i < attCount; i++) {
                    if (result.isWin()) {
                        break;
                    }
                    result = sim.calculate(off, result.getSurvivingDef(), KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM), Arrays.asList(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM)), false, 0, 100.0, result.getWallLevel(), 0, 30, true, true, false, false, false);
                }

                double survive = 0;
                Enumeration<UnitHolder> keys = result.getSurvivingDef().keys();
                while (keys.hasMoreElements()) {
                    UnitHolder key = keys.nextElement();
                    int amount = result.getSurvivingDef().get(key).getCount();
                    survive += (double) amount * key.getPop();
                }
                double lossesPercent = 100 - (100.0 * survive / troops);
                if (!result.isWin() && lossesPercent < maxLossRatio) {
                    element.setNeededSupports(factor);
                    element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.SAVE);
                    element.setLossRation(lossesPercent);
                    break;
                } else {
                    factor++;
                }
                if (factor > maxRuns) {
                    if (lossesPercent < 100) {
                        element.setNeededSupports(factor);
                        element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.FINE);
                        element.setLossRation(lossesPercent);
                    } else {
                        element.setNeededSupports(factor);
                        element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.DANGEROUS);
                        element.setLossRation(100.0);
                    }
                    //break due to max iterations
                    break;
                }
            }
        }
        getModel().fireTableDataChanged();
    }

    private Hashtable<UnitHolder, AbstractUnitElement> getStandardOff() {
        Hashtable<UnitHolder, AbstractUnitElement> off = new Hashtable<UnitHolder, AbstractUnitElement>();
        for (de.tor.tribes.io.UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            off.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
        }
        Hashtable<de.tor.tribes.io.UnitHolder, Integer> amounts = offensePanel.getAmounts();
        Set<Entry<de.tor.tribes.io.UnitHolder, Integer>> entries = amounts.entrySet();
        for (Entry<de.tor.tribes.io.UnitHolder, Integer> entry : entries) {
            de.tor.tribes.io.UnitHolder unit = entry.getKey();
            off.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), entry.getValue(), 10));
        }
        return off;
    }

    public Hashtable<de.tor.tribes.io.UnitHolder, Integer> getDefenseAmount() {
        Hashtable<de.tor.tribes.io.UnitHolder, Integer> def = new Hashtable<de.tor.tribes.io.UnitHolder, Integer>();
        for (de.tor.tribes.io.UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            def.put(unit, defensePanel.getAmountForUnit(unit));
        }
        return def;
    }

    private Hashtable<UnitHolder, AbstractUnitElement> getDefenseAmount(int pFactor) {
        Hashtable<UnitHolder, AbstractUnitElement> def = new Hashtable<UnitHolder, AbstractUnitElement>();
        for (de.tor.tribes.io.UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            def.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
        }
        Hashtable<de.tor.tribes.io.UnitHolder, Integer> amounts = defensePanel.getAmounts();
        Set<Entry<de.tor.tribes.io.UnitHolder, Integer>> entries = amounts.entrySet();
        for (Entry<de.tor.tribes.io.UnitHolder, Integer> entry : entries) {
            de.tor.tribes.io.UnitHolder unit = entry.getKey();
            def.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), entry.getValue() * pFactor, 10));
        }

        return def;
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();
        jPanel1 = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jRefreshAbortButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jMaxRuns = new com.jidesoft.swing.LabeledTextField();
        jMaxLossRatio = new com.jidesoft.swing.LabeledTextField();
        jLabel3 = new javax.swing.JLabel();

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

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Informationen ausblenden");
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

        jXTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jXTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jScrollPane1, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Informationen"));
        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jProgressBar1, gridBagConstraints);

        jRefreshAbortButton.setText("Aktualisieren");
        jRefreshAbortButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireRefreshOrCancelEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jRefreshAbortButton, gridBagConstraints);

        jLabel2.setText("Bereit");
        jLabel2.setMaximumSize(new java.awt.Dimension(150, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(150, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel1, gridBagConstraints);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Truppen pro Angriff"));
        jPanel4.setMinimumSize(new java.awt.Dimension(100, 27));
        jPanel4.setPreferredSize(new java.awt.Dimension(100, 27));
        jPanel4.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel4, gridBagConstraints);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Truppen pro Einzelverteidigung"));
        jPanel5.setMinimumSize(new java.awt.Dimension(100, 27));
        jPanel5.setPreferredSize(new java.awt.Dimension(100, 27));
        jPanel5.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel5, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jMaxRuns.setToolTipText("Maximale Anzahl von Durchläufen");
        jMaxRuns.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/clock.png"))); // NOI18N
        jMaxRuns.setPreferredSize(new java.awt.Dimension(100, 22));
        jMaxRuns.setText("500");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jMaxRuns, gridBagConstraints);

        jMaxLossRatio.setToolTipText("Maximale Verlustrate der Verteidiger");
        jMaxLossRatio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/def_fake.png"))); // NOI18N
        jMaxLossRatio.setPreferredSize(new java.awt.Dimension(100, 20));
        jMaxLossRatio.setText("30");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanel3.add(jMaxLossRatio, gridBagConstraints);

        jLabel3.setText("%");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel3.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel3, gridBagConstraints);

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

    private void fireRefreshOrCancelEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRefreshOrCancelEvent
        if (calculating) {
            calculating = false;
            aborted = true;
            jRefreshAbortButton.setText("Aktualisieren");
        } else {
            calculating = true;
            aborted = false;
            jRefreshAbortButton.setText("Abbrechen");
            controller.setProblem("Berechnung läuft...");
            startCalculation();
        }
    }//GEN-LAST:event_fireRefreshOrCancelEvent

    private void startCalculation() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                updateStatus();
                calculateNeededSupports();
                jRefreshAbortButton.setText("Aktualisieren");
                jLabel2.setText("Bereit");
                jProgressBar1.setValue(0);
                controller.setProblem(null);
                calculating = false;
            }
        }).start();
    }

    public int[] getDefenseInfo() {
        int targets = 0;
        int offs = 0;
        int fakes = 0;
        int needed = 0;

        for (DefenseElement element : getModel().getRows()) {
            targets++;
            needed += element.getNeededSupports();
            offs += element.getAttackCount() - element.getFakeCount();
            fakes += element.getFakeCount();
        }
        return new int[]{targets, offs, fakes, needed};
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private com.jidesoft.swing.LabeledTextField jMaxLossRatio;
    private com.jidesoft.swing.LabeledTextField jMaxRuns;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JButton jRefreshAbortButton;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private org.jdesktop.swingx.JXTable jXTable1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        if (calculating) {
            controller.setProblem("Berechnung läuft...");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        controller.setProblem(null);
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        if (calculating) {
            controller.setProblem("Berechnung läuft...");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        controller.setProblem(null);
        return WizardPanelNavResult.PROCEED;
    }
}
