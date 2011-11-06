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
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.SOSRequest.TimedAttack;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Torridity
 */
public class DSWorkbenchDefenseTool extends javax.swing.JFrame {

    private static DSWorkbenchDefenseTool SINGLETON = null;
    private Hashtable<Village, SOSRequest.TargetInformation> infos = new Hashtable<Village, SOSRequest.TargetInformation>();

    public static synchronized DSWorkbenchDefenseTool getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchDefenseTool();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchDefenseTool */
    DSWorkbenchDefenseTool() {
        initComponents();
        jList1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireUpdateTargetSelectionEvent();
                }
            }
        });

        jPanel1.add(jMainPanel, BorderLayout.CENTER);

    }

    public void setData(List<SOSRequest> pRequests) {
        DefaultListModel model = new DefaultListModel();
        infos.clear();
        for (SOSRequest request : pRequests) {
            Enumeration<Village> targets = request.getTargets();
            while (targets.hasMoreElements()) {
                Village target = targets.nextElement();
                model.addElement(target);
                infos.put(target, request.getTargetInformation(target));
            }
        }
        jList1.setModel(model);
    }

    private void fireUpdateTargetSelectionEvent() {
        Village selection = (Village) jList1.getSelectedValue();
        SOSRequest.TargetInformation info = infos.get(selection);
        try {
            UnitManager.getSingleton().parseUnits(GlobalOptions.getSelectedServer());
        } catch (Exception e) {
        }
        //sim off and def
        Hashtable<UnitHolder, AbstractUnitElement> def = new Hashtable<UnitHolder, AbstractUnitElement>();
        Hashtable<UnitHolder, AbstractUnitElement> off = new Hashtable<UnitHolder, AbstractUnitElement>();
        //pre-fill tables
        for (de.tor.tribes.io.UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            off.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
            def.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        int attCount = 0;
        int fakeCount = 0;
        long first = Long.MAX_VALUE;
        long last = Long.MIN_VALUE;

        for (TimedAttack a : info.getAttacks()) {
            if (a.isPossibleFake()) {
                fakeCount++;
            } else {
                attCount++;
            }
            if (a.getlArriveTime() < first) {
                first = a.getlArriveTime();
            }

            if (a.getlArriveTime() > last) {
                last = a.getlArriveTime();
            }
        }
        Hashtable<de.tor.tribes.io.UnitHolder, Integer> troops = info.getTroops();
        Enumeration<de.tor.tribes.io.UnitHolder> units = troops.keys();

        long force = 0;
        while (units.hasMoreElements()) {
            de.tor.tribes.io.UnitHolder unit = units.nextElement();
            int amount = troops.get(unit);
            def.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), amount, 10));
            force += unit.getDefense() * amount;
        }

        jDefenseValue.setText(nf.format(force));
        jAttackCount.setText(nf.format(attCount));
        jFakeCount.setText(nf.format(fakeCount));
        if (ServerSettings.getSingleton().isMillisArrival()) {
            jFirstAttack.setText(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(new Date(first)));
            jLastAttack.setText(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(new Date(last)));
        } else {
            jFirstAttack.setText(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(new Date(first)));
            jLastAttack.setText(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(new Date(last)));
        }
        jWallLevel.setValue(info.getWallLevel());

        NewSimulator sim = new NewSimulator();

        off.put(UnitManager.getSingleton().getUnitByPlainName("axe"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("axe"), 7000, 10));
        off.put(UnitManager.getSingleton().getUnitByPlainName("light"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("light"), 2300, 10));
        off.put(UnitManager.getSingleton().getUnitByPlainName("ram"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("ram"), 270, 10));

        SimulatorResult result = sim.calculate(off, def, KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM), Arrays.asList(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM)), false, 0, 100.0, jWallLevel.getValue(), 0, 30, true, true, false, false, false);
        int cleanAfter = 0;
        for (int i = 1; i < attCount; i++) {
            if (result.isWin()) {
                cleanAfter = i + 1;
                break;
            }
            result = sim.calculate(off, result.getSurvivingDef(), KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM), Arrays.asList(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM)), false, 0, 100.0, result.getWallLevel(), 0, 30, true, true, false, false, false);
        }
        if (!result.isWin()) {
            jSecuredStatus.setBackground(Color.GREEN);
            jSecuredStatus.setForeground(Color.BLACK);
            double survive = 0;
            Enumeration<UnitHolder> keys = result.getSurvivingDef().keys();
            while (keys.hasMoreElements()) {
                UnitHolder key = keys.nextElement();
                int amount = result.getSurvivingDef().get(key).getCount();
                survive += (double) amount * key.getDefense();
            }
            double lossPercent = 100 - (100.0 * survive / (double) force);

            jSecuredStatus.setText("Sicher (Verluste etwa: " + nf.format(lossPercent) + "%)");
        } else {
            jSecuredStatus.setBackground(Color.RED);
            jSecuredStatus.setForeground(Color.WHITE);
            jSecuredStatus.setText("Dorf clean nach " + cleanAfter + "/" + attCount + " Angriffen");
        }

        calculateNeededSupports();
    }

    private void calculateNeededSupports() {
        Village selection = (Village) jList1.getSelectedValue();
        SOSRequest.TargetInformation info = infos.get(selection);
        try {
            UnitManager.getSingleton().parseUnits(GlobalOptions.getSelectedServer());
        } catch (Exception e) {
        }
        //sim off and def
        Hashtable<UnitHolder, AbstractUnitElement> def = new Hashtable<UnitHolder, AbstractUnitElement>();
        Hashtable<UnitHolder, AbstractUnitElement> off = new Hashtable<UnitHolder, AbstractUnitElement>();
        //pre-fill tables
        for (de.tor.tribes.io.UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            off.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
            def.put(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName(unit.getPlainName()), 0, 10));
        }

        NewSimulator sim = new NewSimulator();
        int attCount = 0;
        for (TimedAttack a : info.getAttacks()) {
            if (!a.isPossibleFake()) {
                attCount++;
            }
        }
        int factor = 1;
        SimulatorResult result = null;
        while (true) {
            off.put(UnitManager.getSingleton().getUnitByPlainName("axe"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("axe"), 7000, 10));
            off.put(UnitManager.getSingleton().getUnitByPlainName("light"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("light"), 2300, 10));
            off.put(UnitManager.getSingleton().getUnitByPlainName("ram"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("ram"), 270, 10));

            def.put(UnitManager.getSingleton().getUnitByPlainName("spear"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("spear"), factor * 50, 10));
            def.put(UnitManager.getSingleton().getUnitByPlainName("sword"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("sword"), factor * 50, 10));
            def.put(UnitManager.getSingleton().getUnitByPlainName("heavy"), new AbstractUnitElement(UnitManager.getSingleton().getUnitByPlainName("heavy"), factor * 10, 10));

            double force = UnitManager.getSingleton().getUnitByPlainName("spear").getDefense() * factor * 50;
            force += UnitManager.getSingleton().getUnitByPlainName("sword").getDefense() * factor * 50;
            force += UnitManager.getSingleton().getUnitByPlainName("heavy").getDefense() * factor * 10;
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
                survive += (double) amount * key.getDefense();
            }
            double lossesPercent = 100 - (100.0 * survive / force);

            if (!result.isWin() && lossesPercent < 30) {
                jNeededSupports.setText(Integer.toString(factor) + "(" + lossesPercent + ")");
                break;
            } else {
                factor++;
            }
        }
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jAttackCount = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jFakeCount = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jWallLevel = new javax.swing.JProgressBar();
        jLabel3 = new javax.swing.JLabel();
        jDefenseValue = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jFirstAttack = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLastAttack = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jSecuredStatus = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jNeededSupports = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

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

        jAttackCount.setEditable(false);
        jAttackCount.setMinimumSize(new java.awt.Dimension(100, 20));
        jAttackCount.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jAttackCount, gridBagConstraints);

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

        jFakeCount.setEditable(false);
        jFakeCount.setMinimumSize(new java.awt.Dimension(100, 20));
        jFakeCount.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jFakeCount, gridBagConstraints);

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
        jWallLevel.setMinimumSize(new java.awt.Dimension(100, 20));
        jWallLevel.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
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

        jDefenseValue.setEditable(false);
        jDefenseValue.setMinimumSize(new java.awt.Dimension(100, 20));
        jDefenseValue.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jDefenseValue, gridBagConstraints);

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

        jFirstAttack.setEditable(false);
        jFirstAttack.setMinimumSize(new java.awt.Dimension(100, 20));
        jFirstAttack.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jFirstAttack, gridBagConstraints);

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

        jLastAttack.setEditable(false);
        jLastAttack.setMinimumSize(new java.awt.Dimension(100, 20));
        jLastAttack.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLastAttack, gridBagConstraints);

        jLabel7.setText("Gesichert");
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
        jSecuredStatus.setMaximumSize(new java.awt.Dimension(100, 20));
        jSecuredStatus.setMinimumSize(new java.awt.Dimension(100, 20));
        jSecuredStatus.setOpaque(true);
        jSecuredStatus.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
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

        jNeededSupports.setEditable(false);
        jNeededSupports.setMinimumSize(new java.awt.Dimension(100, 20));
        jNeededSupports.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jNeededSupports, gridBagConstraints);

        javax.swing.GroupLayout jMainPanelLayout = new javax.swing.GroupLayout(jMainPanel);
        jMainPanel.setLayout(jMainPanelLayout);
        jMainPanelLayout.setHorizontalGroup(
            jMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                .addContainerGap())
        );
        jMainPanelLayout.setVerticalGroup(
            jMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                .addContainerGap(166, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));
        jPanel1.setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        getContentPane().add(capabilityInfoPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchDefenseTool().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JTextField jAttackCount;
    private javax.swing.JTextField jDefenseValue;
    private javax.swing.JTextField jFakeCount;
    private javax.swing.JTextField jFirstAttack;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jLastAttack;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jMainPanel;
    private javax.swing.JTextField jNeededSupports;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel jSecuredStatus;
    private javax.swing.JProgressBar jWallLevel;
    // End of variables declaration//GEN-END:variables
}
