/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.Defense;
import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.ext.Village;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang.time.DurationFormatUtils;

/**
 *
 * @author Torridity
 */
public class SupportsModel extends AbstractTableModel {

    private List<Defense> entries = null;
    private Class[] types = new Class[]{Village.class, Village.class, Date.class, Date.class, String.class, Boolean.class};
    private String[] colNames = new String[]{"Herkunft", "Ziel", "Früheste Abschickzeit", "Späteste Abschickzeit", "Countdown", "Übertragen"};

    public SupportsModel() {
        entries = new ArrayList<Defense>();
    }

    public void clear() {
        entries.clear();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    public Defense[] getRows() {
        return entries.toArray(new Defense[entries.size()]);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    public void addRow(Defense pElement) {
        entries.add(pElement);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Defense elem = entries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return elem.getSupporter();
            case 1:
                return elem.getTarget();
            case 2:
                return new Date(elem.getBestSendTime());
            case 3:
                return new Date(elem.getWorstSendTime());
            case 4:

                long sendTime = elem.getBestSendTime();
                if (sendTime < System.currentTimeMillis()) {//if best in past, take worst
                    sendTime = elem.getWorstSendTime();
                }
                long t = sendTime - System.currentTimeMillis();
                t = (t <= 0) ? 0 : t;
                return DurationFormatUtils.formatDuration(t, "HHH:mm:ss.SSS", true);
            default:
                return elem.isTransferredToBrowser();
        }
    }
}
