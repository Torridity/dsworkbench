/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.StandardAttackElement;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.views.DSWorkbenchAttackFrame;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.attack.StandardAttackManager;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class StandardAttackTableModel extends AbstractTableModel {

    private static Logger logger = Logger.getLogger("StandardAttackTable");
    Class[] types = new Class[]{
        Village.class, Village.class, UnitHolder.class, Date.class, Date.class, Boolean.class, Integer.class
    };
    String[] colNames = new String[]{
        "Herkunft", "Ziel", "Einheit", "Abschickzeit", "Ankunftzeit", "Einzeichnen", "Typ"
    };
    private static StandardAttackTableModel SINGLETON = null;

    public static synchronized StandardAttackTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new StandardAttackTableModel();
        }
        return SINGLETON;
    }

    StandardAttackTableModel() {
    }

    public void setup() {
        List<String> names = new LinkedList<String>();
        List<Class> classes = new LinkedList<Class>();
        names.add("Angriffstyp");
        classes.add(String.class);
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            names.add(unit.getPlainName());
            classes.add(StandardAttackElement.class);
        }
        types = classes.toArray(new Class[]{});
        colNames = names.toArray(new String[]{});
    }

    @Override
    public int getColumnCount() {
        return types.length;
    }

    @Override
    public String getColumnName(int col) {
        return colNames[col];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Object getValueAt(int pRow, int pCol) {
        int typeId = 0;
        if (pRow == 0) {
            typeId = StandardAttackManager.NO_TYPE_ROW;
        } else if (pRow == 1) {
            typeId = StandardAttackManager.OFF_TYPE_ROW;
        } else if (pRow == 2) {
            typeId = StandardAttackManager.SNOB_TYPE_ROW;
        } else if (pRow == 3) {
            typeId = StandardAttackManager.SUPPORT_TYPE_ROW;
        } else if (pRow == 4) {
            typeId = StandardAttackManager.FAKE_TYPE_ROW;
        } else if (pRow == 5) {
            typeId = StandardAttackManager.FAKE_DEFF_TYPE_ROW;
        }

        if (pCol == 0) {
            switch (typeId) {
                case StandardAttackManager.NO_TYPE_ROW:
                    return "Keiner";
                case StandardAttackManager.FAKE_TYPE_ROW:
                    return "Fake";
                case StandardAttackManager.OFF_TYPE_ROW:
                    return "Off";
                case StandardAttackManager.SNOB_TYPE_ROW:
                    return "AG";
                case StandardAttackManager.FAKE_DEFF_TYPE_ROW:
                    return "Fake (Deff)";
                default:
                    return "Unterstützung";
            }
        } else {
            String col = getColumnName(pCol);
            UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(col);
            return StandardAttackManager.getSingleton().getElementForUnit(typeId, unit);
        }
    }

    @Override
    public int getRowCount() {
        return 6;
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        int typeId = 0;
        if (pRow == 0) {
            typeId = StandardAttackManager.NO_TYPE_ROW;
        } else if (pRow == 1) {
            typeId = StandardAttackManager.OFF_TYPE_ROW;
        } else if (pRow == 2) {
            typeId = StandardAttackManager.SNOB_TYPE_ROW;
        } else if (pRow == 3) {
            typeId = StandardAttackManager.SUPPORT_TYPE_ROW;
        } else if (pRow == 4) {
            typeId = StandardAttackManager.FAKE_TYPE_ROW;
        } else if (pRow == 5) {
            typeId = StandardAttackManager.FAKE_DEFF_TYPE_ROW;
        }

        String col = getColumnName(pCol);
        UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(col);
        StandardAttackElement elem = StandardAttackManager.getSingleton().getElementForUnit(typeId, unit);
        if (!elem.trySettingAmount((String) pValue)) {
            String message = "Ungültiger Wert. Zulässige Werte sind:\n";
            message += "- Alle\n";
            message += "- Anzahl (z.B. 100)\n";
            message += "- Alle - Anzahl (z.B. Alle - 100)\n";
            message += "- Anzahl% (z.B. 10%)\n";

            JOptionPaneHelper.showInformationBox(DSWorkbenchAttackFrame.getSingleton().getStandardAttackDialog(), message, "Information");
        }
    }
}
