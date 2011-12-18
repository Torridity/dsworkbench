/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchDefenseTool.java
 *
 * Created on Nov 6, 2011, 1:34:08 PM
 */
package de.tor.tribes.ui.views;

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
import de.tor.tribes.ui.models.DefenseToolModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefenseStatusTableCellRenderer;
import de.tor.tribes.ui.renderer.LossRatioTableCellRenderer;
import de.tor.tribes.ui.renderer.TendencyTableCellRenderer;
import de.tor.tribes.ui.renderer.WallLevellCellRenderer;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.ProfileManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.math.LongRange;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.painter.MattePainter;

/**
 *
 * @author Torridity
 * @TODO Store current defense to allow remembering of already found snobs/fakes...make entries removeable if village is full
 * @TODO Classify fakes also by same source to different targets (already included?) --> If X targets with 1 source found, X-1/X attacks might be fakes
 * @TODO Include delta-view to allow to descide, to which villages the attack count raises over time...e.g. to get "real" targets
 * @TODO Include attack planer seemlessly? --> allows to skip the step going through attack planer
 */
public class DSWorkbenchDefenseTool extends javax.swing.JFrame {

    private static DSWorkbenchDefenseTool SINGLETON = null;
    private Hashtable<Village, SOSRequest.TargetInformation> infos = new Hashtable<Village, SOSRequest.TargetInformation>();
    private final NumberFormat numFormat = NumberFormat.getInstance();
    private boolean aborted = false;

    public static synchronized DSWorkbenchDefenseTool getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchDefenseTool();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchDefenseTool */
    DSWorkbenchDefenseTool() {
        initComponents();
        numFormat.setMaximumFractionDigits(0);
        numFormat.setMinimumFractionDigits(0);
        jPanel1.add(jMainPanel, BorderLayout.CENTER);
        jXTable1.setModel(new DefenseToolModel());
        BufferedImage back = ImageUtils.createCompatibleBufferedImage(3, 3, BufferedImage.TRANSLUCENT);
        Graphics g = back.getGraphics();
        g.setColor(new Color(120, 120, 120, 120));
        g.fillRect(0, 0, back.getWidth(), back.getHeight());
        g.setColor(new Color(120, 120, 120));
        g.drawLine(0, 0, 3, 3);
        g.dispose();
        TexturePaint paint = new TexturePaint(back, new Rectangle2D.Double(0, 0, back.getWidth(), back.getHeight()));
        jxWaitPane.setBackgroundPainter(new MattePainter(paint));
        setGlassPane(jxWaitPane);
        jXTable1.getColumnExt("Tendenz").setCellRenderer(new TendencyTableCellRenderer());
        jXTable1.getColumnExt("Status").setCellRenderer(new DefenseStatusTableCellRenderer());
        jXTable1.getColumnExt("Wall").setCellRenderer(new WallLevellCellRenderer());
        jXTable1.getColumnExt("Verlustrate").setCellRenderer(new LossRatioTableCellRenderer());
        jXTable1.setDefaultRenderer(Date.class, new DateCellRenderer());
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
        jLabel8.setText("Aktualisiere Verteidigungsstatus");
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

            SimulatorResult result = sim.calculate(off, def, KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM), Arrays.asList(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM)), false, 0, 100.0, jWallLevel.getValue(), 0, 30, true, true, false, false, false);
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
                    jSecuredStatus.setBackground(Color.GREEN);
                    jSecuredStatus.setForeground(Color.BLACK);
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
        for (DefenseElement element : getModel().getRows()) {
            if (aborted) {
                break;
            }
            jLabel8.setText("Berechne Angriffe " + cnt + "/" + getModel().getRowCount());
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
                Hashtable<UnitHolder, AbstractUnitElement> def = getStandardDef(factor);

                double troops = UnitManager.getSingleton().getUnitByPlainName("spear").getPop() * def.get(UnitManager.getSingleton().getUnitByPlainName("spear")).getCount();
                troops += UnitManager.getSingleton().getUnitByPlainName("sword").getPop() * def.get(UnitManager.getSingleton().getUnitByPlainName("sword")).getCount();
                troops += UnitManager.getSingleton().getUnitByPlainName("heavy").getPop() * def.get(UnitManager.getSingleton().getUnitByPlainName("heavy")).getCount();
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
                if (!result.isWin() && lossesPercent < 30) {
                    element.setNeededSupports(factor);
                    break;
                } else {
                    factor++;
                }
                if (factor > 500) {
                    if (lossesPercent < 100) {
                        element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.FINE);
                        element.setLossRation(lossesPercent);
                    } else {
                        element.setDefenseStatus(DefenseElement.DEFENSE_STATUS.DANGEROUS);
                        element.setLossRation(100.0);
                    }
                }
            }
        }
        getModel().fireTableDataChanged();
    }

    private void calculatePossibilities() {
        Enumeration<Village> targets = infos.keys();
        HashMap<Village, LongRange> arrivals = new HashMap<Village, LongRange>();
        List<Village> targetList = new ArrayList<Village>();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            targetList.add(target);
            SOSRequest.TargetInformation info = infos.get(target);
            arrivals.put(target, new LongRange(info.getFirstAttack(), info.getLastAttack()));
        }

        Village[] villageList = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
        int[][] data = new int[targetList.size()][villageList.length];

        for (int i = 0; i < targetList.size(); i++) {
            for (int j = 0; j < villageList.length; j++) {
                if (DSCalculator.calculateDistance(targetList.get(i), villageList[j]) < 50) {
                    data[i][j] += 1;
                }
            }
        }

        for (int i = 0; i < targetList.size(); i++) {
            StringBuilder b = new StringBuilder();
            for (int j = 0; j < villageList.length; j++) {
                b.append(data[i][j]).append(",");
            }
            System.out.println(b.toString());
        }
    }

    private Hashtable<UnitHolder, AbstractUnitElement> getStandardOff() {
        Hashtable<UnitHolder, AbstractUnitElement> off = new Hashtable<UnitHolder, AbstractUnitElement>();
        for (de.tor.tribes.io.UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            off.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
        }
        off.put(UnitManager.getSingleton().getUnitByPlainName("axe"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("axe"), 7000, 10));
        off.put(UnitManager.getSingleton().getUnitByPlainName("light"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("light"), 2300, 10));
        off.put(UnitManager.getSingleton().getUnitByPlainName("ram"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("ram"), 270, 10));
        return off;
    }

    private Hashtable<UnitHolder, AbstractUnitElement> getStandardDef(int pFactor) {
        Hashtable<UnitHolder, AbstractUnitElement> def = new Hashtable<UnitHolder, AbstractUnitElement>();
        for (de.tor.tribes.io.UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            def.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
        }
        def.put(UnitManager.getSingleton().getUnitByPlainName("spear"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("spear"), 500 * pFactor, 10));
        def.put(UnitManager.getSingleton().getUnitByPlainName("sword"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("sword"), 500 * pFactor, 10));
        def.put(UnitManager.getSingleton().getUnitByPlainName("heavy"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("heavy"), 100 * pFactor, 10));
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

        jMainPanel = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();
        jButton5 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jWallLevel = new javax.swing.JProgressBar();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jSecuredStatus = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jAttackCount = new javax.swing.JLabel();
        jFakeCount = new javax.swing.JLabel();
        jFirstAttack = new javax.swing.JLabel();
        jLastAttack = new javax.swing.JLabel();
        jDefenseValue = new javax.swing.JLabel();
        jNeededSupports = new javax.swing.JLabel();
        jxWaitPane = new org.jdesktop.swingx.JXPanel();
        jLabel8 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jButton3.setText("Dummy");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCalc(evt);
            }
        });

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
        jScrollPane2.setViewportView(jXTable1);

        jButton5.setText("Update");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateDef(evt);
            }
        });

        javax.swing.GroupLayout jMainPanelLayout = new javax.swing.GroupLayout(jMainPanel);
        jMainPanel.setLayout(jMainPanelLayout);
        jMainPanelLayout.setHorizontalGroup(
            jMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
                    .addGroup(jMainPanelLayout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 555, Short.MAX_VALUE)
                        .addComponent(jButton5)))
                .addContainerGap())
        );
        jMainPanelLayout.setVerticalGroup(
            jMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 227, Short.MAX_VALUE)
                .addGroup(jMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton5))
                .addContainerGap())
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Ziele"));

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jList1);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Verteidungsstatus"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Angriffe");
        jLabel1.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel1.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel1.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel6.setText("Fakes");
        jLabel6.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel6.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel6, gridBagConstraints);

        jLabel2.setText("Wall");
        jLabel2.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel2, gridBagConstraints);

        jWallLevel.setMaximum(20);
        jWallLevel.setMinimumSize(new java.awt.Dimension(100, 22));
        jWallLevel.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jWallLevel, gridBagConstraints);

        jLabel3.setText("Verteidigung");
        jLabel3.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel3.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel3.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Erster Angriff");
        jLabel4.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel4.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel4.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Letzter Angriff");
        jLabel5.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel5.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel5.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel5, gridBagConstraints);

        jLabel7.setText("Status");
        jLabel7.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel7.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel7, gridBagConstraints);

        jSecuredStatus.setBackground(new java.awt.Color(255, 0, 0));
        jSecuredStatus.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jSecuredStatus.setMaximumSize(new java.awt.Dimension(100, 22));
        jSecuredStatus.setMinimumSize(new java.awt.Dimension(100, 22));
        jSecuredStatus.setOpaque(true);
        jSecuredStatus.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jSecuredStatus, gridBagConstraints);

        jLabel9.setText("Notwendige Unterstützungen");
        jLabel9.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel9.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel9.setPreferredSize(new java.awt.Dimension(170, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel9, gridBagConstraints);

        jButton1.setText("jButton1");
        jButton1.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(23, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        jPanel2.add(jButton1, gridBagConstraints);

        jButton2.setText("jButton1");
        jButton2.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton2.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton2.setPreferredSize(new java.awt.Dimension(23, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        jPanel2.add(jButton2, gridBagConstraints);

        jAttackCount.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jAttackCount.setMaximumSize(new java.awt.Dimension(34, 22));
        jAttackCount.setMinimumSize(new java.awt.Dimension(34, 22));
        jAttackCount.setPreferredSize(new java.awt.Dimension(34, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jAttackCount, gridBagConstraints);

        jFakeCount.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jFakeCount.setMaximumSize(new java.awt.Dimension(34, 22));
        jFakeCount.setMinimumSize(new java.awt.Dimension(34, 22));
        jFakeCount.setPreferredSize(new java.awt.Dimension(34, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jFakeCount, gridBagConstraints);

        jFirstAttack.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jFirstAttack.setMaximumSize(new java.awt.Dimension(34, 22));
        jFirstAttack.setMinimumSize(new java.awt.Dimension(34, 22));
        jFirstAttack.setPreferredSize(new java.awt.Dimension(34, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jFirstAttack, gridBagConstraints);

        jLastAttack.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLastAttack.setMaximumSize(new java.awt.Dimension(34, 22));
        jLastAttack.setMinimumSize(new java.awt.Dimension(34, 22));
        jLastAttack.setPreferredSize(new java.awt.Dimension(34, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLastAttack, gridBagConstraints);

        jDefenseValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jDefenseValue.setMaximumSize(new java.awt.Dimension(34, 22));
        jDefenseValue.setMinimumSize(new java.awt.Dimension(34, 22));
        jDefenseValue.setPreferredSize(new java.awt.Dimension(34, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jDefenseValue, gridBagConstraints);

        jNeededSupports.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jNeededSupports.setMaximumSize(new java.awt.Dimension(34, 22));
        jNeededSupports.setMinimumSize(new java.awt.Dimension(34, 22));
        jNeededSupports.setPreferredSize(new java.awt.Dimension(34, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jNeededSupports, gridBagConstraints);

        jxWaitPane.setOpaque(false);
        jxWaitPane.setLayout(new java.awt.BorderLayout());

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jxWaitPane.add(jLabel8, java.awt.BorderLayout.CENTER);

        jButton6.setText("Abbrechen");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireAbortAnalysisEvent(evt);
            }
        });
        jxWaitPane.add(jButton6, java.awt.BorderLayout.PAGE_END);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));
        jPanel1.setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        getContentPane().add(capabilityInfoPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireCalc(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalc
        calculatePossibilities();
    }//GEN-LAST:event_fireCalc

    private void updateDef(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateDef
        aborted = false;
        getGlassPane().setVisible(true);
        new Thread(new Runnable() {

            @Override
            public void run() {
                updateStatus();
                calculateNeededSupports();
                getGlassPane().setVisible(false);
            }
        }).start();

    }//GEN-LAST:event_updateDef

    private void fireAbortAnalysisEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAbortAnalysisEvent
        aborted = true;
    }//GEN-LAST:event_fireAbortAnalysisEvent

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;


                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchDefenseTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchDefenseTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchDefenseTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchDefenseTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>


        /* Create and display the form */
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de47");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de47")[0]);
        DataHolder.getSingleton().loadData(false);
        //  GlobalOptions.loadUserData();
        DSWorkbenchDefenseTool.getSingleton().setData(createSampleRequests());
        DSWorkbenchDefenseTool.getSingleton().setData(createSampleRequests());
        DSWorkbenchDefenseTool.getSingleton().pack();
        DSWorkbenchDefenseTool.getSingleton().setVisible(true);
    }

    private static List<SOSRequest> createSampleRequests() {
        int wallLevel = 20;
        int supportCount = 50;
        int maxAttackCount = 10;
        int maxFakeCount = 0;

        List<SOSRequest> result = new LinkedList<SOSRequest>();
        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();

        for (int i = 0; i < supportCount; i++) {
            int id = (int) Math.rint(Math.random() * (villages.length - 1));
            Village target = villages[id];
            SOSRequest r = new SOSRequest(target.getTribe());
            r.addTarget(target);
            SOSRequest.TargetInformation info = r.getTargetInformation(target);
            info.setWallLevel(wallLevel);

            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("spear"), (int) Math.rint(Math.random() * 14000));
            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("sword"), (int) Math.rint(Math.random() * 14000));
            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("heavy"), (int) Math.rint(Math.random() * 5000));

            int cnt = (int) Math.rint(maxAttackCount * Math.random());
            for (int j = 0; j < cnt; j++) {
                info.addAttack(DataHolder.getSingleton().getRandomVillageWithOwner(), new Date(System.currentTimeMillis() + Math.round(3600 * Math.random())));
                for (int k = 0; k < (int) Math.rint(maxFakeCount * Math.random()); k++) {
                    info.addAttack(DataHolder.getSingleton().getRandomVillageWithOwner(), new Date(System.currentTimeMillis() + Math.round(3600 * Math.random())));
                }
            }
            result.add(r);
        }

        return result;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JLabel jAttackCount;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jDefenseValue;
    private javax.swing.JLabel jFakeCount;
    private javax.swing.JLabel jFirstAttack;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLastAttack;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jMainPanel;
    private javax.swing.JLabel jNeededSupports;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel jSecuredStatus;
    private javax.swing.JProgressBar jWallLevel;
    private org.jdesktop.swingx.JXTable jXTable1;
    private org.jdesktop.swingx.JXPanel jxWaitPane;
    // End of variables declaration//GEN-END:variables
}
