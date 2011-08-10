/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author Torridity
 */
public class TableHelper {

    public static int deleteSelectedRows(JXTable pTable) {
        int[] selrows = pTable.getSelectedRows();

        if (selrows == null || selrows.length == 0) {
            return 0;
        }

        List<Integer> rowsToDelete = new ArrayList<Integer>();

        for (int row : selrows) {
            rowsToDelete.add(pTable.convertRowIndexToModel(row));
        }
        DefaultTableModel theModel = ((DefaultTableModel) pTable.getModel());
        List rowsToKeep = new ArrayList(theModel.getRowCount() - selrows.length);

        for (int i = 0; i < pTable.getRowCount(); i++) {
            int row = pTable.convertRowIndexToModel(i);
            if (!rowsToDelete.contains(row)) {
                //row should not be deleted
                List rowToKeep = new ArrayList(pTable.getColumnCount());
                for (int j = 0; j < pTable.getColumnCount(); j++) {
                    rowToKeep.add(theModel.getValueAt(row, j));
                }
                //add row to keep
                rowsToKeep.add(rowToKeep);
            }
        }
        //remove all rows fast
        theModel.setRowCount(0);

        //restore kept rows
        for (Object keptRow : rowsToKeep) {
            theModel.addRow(((List) keptRow).toArray());
        }
        return selrows.length;
    }
}
