/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

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

        DefaultTableModel theModel = ((DefaultTableModel) pTable.getModel());
        for (int i = selrows.length - 1; i > -1; i--) {
            int rowInModel = pTable.convertRowIndexToModel(selrows[i]);
            theModel.removeRow(rowInModel);
        }
        return selrows.length;
    }
}
