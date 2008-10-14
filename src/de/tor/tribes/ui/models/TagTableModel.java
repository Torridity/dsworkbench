/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.Tag;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.tag.TagManagerListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jejkal
 */
public class TagTableModel extends AbstractTableModel {

    Class[] types = new Class[]{
        Tag.class, Boolean.class
    };
    String[] colNames = new String[]{
        "Tag", "Einzeichnen"
    };
    private static TagTableModel SINGLETON = null;

    public static synchronized TagTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TagTableModel();
        }
        return SINGLETON;
    }

    TagTableModel() {

        TagManager.getSingleton().addTagManagerListener(new TagManagerListener() {

            @Override
            public void fireTagsChangedEvent() {
                fireTableDataChanged();
            }
        });
    }

    @Override
    public int getRowCount() {
        return TagManager.getSingleton().getTags().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    public void addRow(Object[] row) {
        TagManager.getSingleton().addTag((String) row[0]);
    }

    public void removeRow(int pRow) {
        Tag t = TagManager.getSingleton().getTags().get(pRow);
        TagManager.getSingleton().removeTag(t);
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
        return true;
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

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: {
                TagManager.getSingleton().getTags().get(rowIndex).setName((String) value);
            }
            default: {
                TagManager.getSingleton().getTags().get(rowIndex).setShowOnMap((Boolean) value);
            }
        }
    }
}
