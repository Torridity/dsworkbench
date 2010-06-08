/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.util.GlobalOptions;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public abstract class AbstractDSWorkbenchTableModel extends AbstractTableModel {

    private boolean[] visibleColumns = null;
    private JPopupMenu popup = null;

    public abstract String getPropertyBaseID();

    public abstract Class[] getColumnClasses();

    public abstract String[] getColumnNames();

    public abstract List<String> getInternalColumnNames();

    public abstract boolean[] getEditableColumns();

    public abstract void doNotifyOnColumnChange();

    public AbstractDSWorkbenchTableModel() {
        checkStaticImplementation();
        loadVisibilityState();
        buildPopup();
    }

    private void checkStaticImplementation() throws UnsupportedOperationException {
        if (getColumnClasses() == null || getColumnNames() == null || getInternalColumnNames() == null || getEditableColumns() == null) {
            throw new UnsupportedOperationException("Static initialization not implemented yet");
        }
    }

    /**Fill the array of visible cols*/
    private void loadVisibilityState() {
        String cols = GlobalOptions.getProperty(getPropertyBaseID() + ".visible.cols");
        int cnt = 0;
        if (cols != null) {
            //property stored
            String[] colData = cols.split(",");
            if (colData.length == getInternalColumnNames().size()) {
                //col count has not changed during now and last save
                visibleColumns = new boolean[colData.length];
                for (String col : colData) {
                    visibleColumns[cnt] = Boolean.parseBoolean(col);
                    cnt++;
                }
                return;
            }
        }

        //col count has changed or property was not stored yet
        visibleColumns = new boolean[getInternalColumnNames().size()];
        for (int i = 0; i < visibleColumns.length; i++) {
            visibleColumns[i] = true;
        }
        saveVisibilityState();
    }

    /**Save the property holding the array of visible cols*/
    private void saveVisibilityState() {
        String colData = "";
        for (boolean col : visibleColumns) {
            colData += col + ",";
        }
        colData = colData.substring(0, colData.lastIndexOf(","));
        GlobalOptions.addProperty(getPropertyBaseID() + ".visible.cols", colData);
    }

    /**Build the right-click popup to enable/disable columns*/
    private void buildPopup() {
        int cnt = 0;
        popup = new JPopupMenu("Angezeigte Spalten");
        for (String name : getInternalColumnNames()) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, visibleColumns[cnt]);
            item.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    String text = ((JCheckBoxMenuItem) e.getSource()).getText();
                    int idx = getInternalColumnNames().indexOf(text);
                    visibleColumns[idx] = !visibleColumns[idx];
                    saveVisibilityState();
                    fireTableStructureChanged();
                    doNotifyOnColumnChange();
                }
            });
            popup.add(item);
            cnt++;
        }
    }

    public boolean isColVisible(int pId) {
        try {
            return visibleColumns[pId];
        } catch (Exception e) {
            return false;
        }
    }

    /**Return the right-click popup to register it for a tables ScrollPane and the table itself*/
    public JPopupMenu getPopup() {
        return popup;
    }

    @Override
    public String getColumnName(int col) {
        //return colNames[col];
        return getColumnNames()[getRealColumnId(col)];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return getColumnClasses()[getRealColumnId(columnIndex)];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        col = getRealColumnId(col);
        return getEditableColumns()[col];
    }

    @Override
    public int getColumnCount() {
        int n = 0;
        for (int i = 0; i < visibleColumns.length; i++) {
            if (visibleColumns[i]) {
                n++;
            }
        }
        return n;
    }

    /**Get the real column number in respect to (in-)visible columns*/
    protected int getRealColumnId(int col) {
        int n = col;    // right number to return
        int i = 0;
        do {
            if (!(visibleColumns[i])) {
                n++;
            }
            i++;
        } while (i < n);
        // If we are on an invisible column,
        // we have to go one step further
        while (!(visibleColumns[n])) {
            n++;
        }
        return n;
    }
}
