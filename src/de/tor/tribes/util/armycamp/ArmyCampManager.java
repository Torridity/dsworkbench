/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.armycamp;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.ArmyCamp;
import de.tor.tribes.ui.MapPanel;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Torridity
 */
public class ArmyCampManager {

    private static ArmyCampManager SINGLETON = null;
    private List<ArmyCamp> armyCamps = null;
    private DefaultTableModel model = null;
    private List<ArmyCampManagerListener> mManagerListeners = null;

    public static synchronized ArmyCampManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ArmyCampManager();
        }
        return SINGLETON;
    }

    ArmyCampManager() {
        armyCamps = new LinkedList<ArmyCamp>();
        mManagerListeners = new LinkedList<ArmyCampManagerListener>();
        model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Besitzer", "Position"
                }) {

            Class[] types = new Class[]{
                Ally.class, Point.class
            };
            boolean[] canEdit = new boolean[]{
                false, false
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
    }

    public synchronized void addArmyCamphManagerListener(ArmyCampManagerListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mManagerListeners.contains(pListener)) {
            mManagerListeners.add(pListener);
        }
    }

    public synchronized void removeArmyCamphManagerListener(ArmyCampManagerListener pListener) {
        mManagerListeners.remove(pListener);
    }

    public void addArmyCamp(Ally pAlly, short pX, short pY) {
        armyCamps.add(new ArmyCamp(pAlly, pX, pY));
        fireArmyCampsChangedEvents();
    }

    public ArmyCamp getArmyCamp(short pX, short pY) {
        ArmyCamp[] camps = armyCamps.toArray(new ArmyCamp[]{});
        for (ArmyCamp c : camps) {
            if (c.getX() == pX && c.getY() == pY) {
                return c;
            }
        }
        return null;
    }

    public void removeArmyCamp(short pX, short pY) {
        ArmyCamp[] camps = armyCamps.toArray(new ArmyCamp[]{});
        for (ArmyCamp c : camps) {
            if (c.getX() == pX && c.getY() == pY) {
                armyCamps.remove(c);
                break;
            }
        }
        fireArmyCampsChangedEvents();
    }

    public boolean isArmyCamp(short pX, short pY) {
        ArmyCamp[] camps = armyCamps.toArray(new ArmyCamp[]{});
        for (ArmyCamp c : camps) {
            if (c.getX() == pX && c.getY() == pY) {
                return true;
            }
        }
        return false;
    }

    public DefaultTableModel getTableModel() {
        //remove former rows
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        ArmyCamp[] armyCampArray = armyCamps.toArray(new ArmyCamp[]{});
        for (ArmyCamp c : armyCampArray) {
            model.addRow(new Object[]{c.getAlly(), new Point(c.getX(), c.getY())});
        }
        return model;
    }

    public void armyCampsUpdatedExternally() {
        fireArmyCampsChangedEvents();
    }

    private void fireArmyCampsChangedEvents() {
        ArmyCampManagerListener[] listeners = mManagerListeners.toArray(new ArmyCampManagerListener[]{});
        for (ArmyCampManagerListener listener : listeners) {
            listener.fireArmyCampsChangedEvent();
        }
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(0);
    }
}
