/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TroopSelectionPanel.java
 *
 * Created on Dec 18, 2011, 2:22:28 PM
 */
package de.tor.tribes.ui.panels;

import com.jidesoft.swing.LabeledTextField;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.UIHelper;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class TroopSelectionPanel extends javax.swing.JPanel {
    
    private Hashtable<UnitHolder, Point> unitCoordinates = new Hashtable<UnitHolder, Point>();
    private LabeledTextField[][] unitFields = new LabeledTextField[20][20];

    /** Creates new form TroopSelectionPanel */
    public TroopSelectionPanel() {
        initComponents();
        setup(DataHolder.getSingleton().getUnits());
    }
    
    public final void setup(List<UnitHolder> pUnits) {
        setup(DataHolder.getSingleton().getUnits(), true);
    }
    
    public final void setup(List<UnitHolder> pUnits, boolean pTypeSeparation) {
        removeAll();
        unitCoordinates.clear();
        unitFields = new LabeledTextField[20][20];
        int infantryX = 0;
        int cavallryX = 0;
        int otherX = 0;
        int unitCount = 0;
        for (UnitHolder unit : pUnits) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            if (unit.isInfantry()) {
                gridBagConstraints.gridx = (pTypeSeparation) ? infantryX : unitCount;
                gridBagConstraints.gridy = 0;
                unitCoordinates.put(unit, new Point(gridBagConstraints.gridx, gridBagConstraints.gridy));
                infantryX++;
            } else if (unit.isCavalry()) {
                gridBagConstraints.gridx = (pTypeSeparation) ? cavallryX : unitCount;
                gridBagConstraints.gridy = (pTypeSeparation) ? 1 : 0;
                unitCoordinates.put(unit, new Point(gridBagConstraints.gridx, gridBagConstraints.gridy));
                cavallryX++;
            } else if (unit.isOther()) {
                gridBagConstraints.gridx = (pTypeSeparation) ? otherX : unitCount;
                gridBagConstraints.gridy = (pTypeSeparation) ? 2 : 0;
                unitCoordinates.put(unit, new Point(gridBagConstraints.gridx, gridBagConstraints.gridy));
                otherX++;
            }
            LabeledTextField unitField = new LabeledTextField();
            unitField.setIcon(ImageManager.getUnitIcon(unit));
            unitFields[gridBagConstraints.gridx][gridBagConstraints.gridy] = unitField;
            unitField.setMinimumSize(new Dimension(80, 21));
            unitField.setPreferredSize(new Dimension(80, 21));
            unitField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 21));
            unitField.setText("0");
            add(unitField, gridBagConstraints);
            unitCount++;
        }
    }
    
    public final void setupDefense(boolean pTypeSeparation) {
        List<UnitHolder> units = new LinkedList<UnitHolder>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (unit.isDefense()) {
                units.add(unit);
            }
        }
        setup(units, pTypeSeparation);
    }
    
    public final void setupOffense(boolean pTypeSeparation) {
        List<UnitHolder> units = new LinkedList<UnitHolder>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (unit.isOffense()) {
                units.add(unit);
            }
        }
        setup(units, pTypeSeparation);
    }
    
    public Hashtable<UnitHolder, Integer> getAmounts() {
        Hashtable<UnitHolder, Integer> values = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            values.put(unit, getAmountForUnit(unit));
        }
        return values;
    }
    
    public void setAmounts(Hashtable<UnitHolder, Integer> pAmounts) {
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Integer amount = pAmounts.get(unit);
            if (amount != null) {
                setAmountForUnit(unit, amount);
            } else {
                setAmountForUnit(unit, 0);
            }
        }
    }
    
    public int getAmountForUnit(UnitHolder pUnit) {
        Point location = unitCoordinates.get(pUnit);
        if (location != null) {
            LabeledTextField field = unitFields[location.x][location.y];
            return UIHelper.parseIntFromField(field, 0);
        }
        return 0;
    }
    
    public void setAmountForUnit(UnitHolder pUnit, int pValue) {
        Point location = unitCoordinates.get(pUnit);
        if (location != null) {
            LabeledTextField field = unitFields[location.x][location.y];
            field.setText(Integer.toString(pValue));
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (unitFields[i][j] != null) {
                    unitFields[i][j].setEnabled(enabled);
                }
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

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de68");
        ProfileManager.getSingleton().loadProfiles();
        DataHolder.getSingleton().loadData(false);
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(300, 300);
        TroopSelectionPanel panel = new TroopSelectionPanel();
        panel.setupDefense(false);
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
    }
}
