/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.util.List;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import org.apache.commons.configuration.Configuration;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;

/**
 *
 * @author Torridity
 */
public class PropertyHelper {

    public static void storeTableProperties(JXTable pTable, Configuration pConfig, String pPrefix) {
        List<TableColumn> cols = ((TableColumnModelExt) pTable.getColumnModel()).getColumns(true);

        for (TableColumn c : cols) {
            TableColumnExt col = (TableColumnExt) c;

            String title = col.getTitle();
            pConfig.setProperty(pPrefix + ".table.col." + title + ".width", col.getWidth());
            pConfig.setProperty(pPrefix + ".table.col." + title + ".visible", col.isVisible());
        }
        int sortedCol = pTable.getSortedColumnIndex();
        if (sortedCol < 0) {
            return;
        }
        pConfig.setProperty(pPrefix + ".table.sort.col", sortedCol);
        int sortOrder = 0;
        switch (pTable.getSortOrder(sortedCol)) {
            case ASCENDING:
                sortOrder = 1;
                break;
            case DESCENDING:
                sortOrder = -1;
                break;
            default:
                sortOrder = 0;
        }
        pConfig.setProperty(pPrefix + ".table.sort.order", sortOrder);
        pConfig.setProperty(pPrefix + ".table.horizontal.scroll", pTable.isHorizontalScrollEnabled());
    }

    public static void restoreTableProperties(JXTable pTable, Configuration pConfig, String pPrefix) {
        for (int i = 0; i < pTable.getColumnCount(); i++) {
            TableColumnExt col = pTable.getColumnExt(i);
            String title = col.getTitle();
            col.setPreferredWidth(pConfig.getInteger(pPrefix + ".table.col." + title + ".width", col.getWidth()));
            col.setVisible(pConfig.getBoolean(pPrefix + ".table.col." + title + ".visible", true));
        }

        SortOrder sortOrder = SortOrder.UNSORTED;
        switch (pConfig.getInteger(pPrefix + ".table.sort.order", 0)) {
            case 1:
                sortOrder = SortOrder.ASCENDING;
                break;
            case -1:
                sortOrder = SortOrder.DESCENDING;
                break;
            default:
                sortOrder = SortOrder.UNSORTED;
        }


        Boolean scroll = pConfig.getBoolean(pPrefix + ".table.horizontal.scroll", false);
        pTable.setHorizontalScrollEnabled(scroll);
        Integer orderCol = pConfig.getInteger(pPrefix + ".table.sort.col", 0);
        try {
            pTable.setSortOrder(orderCol.intValue(), sortOrder);
        } catch (IndexOutOfBoundsException e) {
        }
    }
}
