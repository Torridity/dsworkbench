/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.util.tag.TagManager;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jejkal
 */
public class TagTableModel extends AbstractTableModel {

    @Override
    public int getRowCount() {
        return TagManager.getSingleton().getTags().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: {
                return TagManager.getSingleton().getTags().get(rowIndex).getName();
            }
            default: {
                return TagManager.getSingleton().getTags().get(rowIndex).isShowOnMap();
            }
        }
    }
}
