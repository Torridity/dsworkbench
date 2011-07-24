/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.util.GlobalOptions;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Torridity
 */
public abstract class AbstractDSWorkbenchTableModel extends AbstractTableModel {

    private boolean[] visibleColumns = null;
    private JPopupMenu popup = null;
    private TableRowSorter<TableModel> mRowSorter = null;
    private TableColumnModel mColumnModel = null;
    private JTable mAssociatedTable = null;
    private PropertyChangeListener mColumnListener = null;

    public abstract String getPropertyBaseID();

    public abstract Class[] getColumnClasses();

    public abstract String[] getColumnNames();

    public abstract List<String> getInternalColumnNames();

    public abstract boolean[] getEditableColumns();

    public abstract void doNotifyOnColumnChange();

    public AbstractDSWorkbenchTableModel() {
        checkStaticImplementation();
        mColumnListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String prop = evt.getPropertyName();
                if (prop != null && prop.equals("preferredWidth")) {
                    saveColumnState();
                }
            }
        };
        loadVisibilityState();
        loadColumnState();
        buildPopup();
    }

    private void checkStaticImplementation() throws UnsupportedOperationException {
        if (getColumnClasses() == null || getColumnNames() == null || getInternalColumnNames() == null || getEditableColumns() == null) {
            throw new UnsupportedOperationException("Static initialization not implemented yet");
        }
    }

    public TableRowSorter<TableModel> getRowSorter() {
        return mRowSorter;
    }

    public void resetRowSorter(JTable pTable) {
        if (mAssociatedTable == null) {
            mAssociatedTable = pTable;
        }

        if (mRowSorter != null) {
            //store current state
            List<RowSorter.SortKey> keys = (List<RowSorter.SortKey>) mRowSorter.getSortKeys();
            if (!keys.isEmpty()) {

                int col = keys.get(0).getColumn();
                col = convertViewColumnToModel(col);
                if (isColVisible(col)) {
                    String sortValue = col + "," + ((keys.get(0).getSortOrder() == SortOrder.ASCENDING) ? "0" : "1");
                    GlobalOptions.addProperty(getPropertyBaseID() + ".sort.key", sortValue);
                }
            }
        }
        pTable.setModel(this);

        //create new row sorter and reset state
        mRowSorter = new TableRowSorter<TableModel>();
        mRowSorter.setModel(this);
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        String sortKey = GlobalOptions.getProperty(getPropertyBaseID() + ".sort.key");
        if (sortKey != null) {
            String[] keySplit = sortKey.split(",");
            int col = Integer.parseInt(keySplit[0]);

            if (isColVisible(col)) {
                col = convertModelColumnToView(col);
                if (Integer.parseInt(keySplit[1]) == 0) {
                    sortKeys.add(new RowSorter.SortKey(col, SortOrder.ASCENDING));
                } else {
                    sortKeys.add(new RowSorter.SortKey(col, SortOrder.DESCENDING));
                }
            }
            mRowSorter.setSortKeys(sortKeys);
        }
        pTable.setRowSorter(mRowSorter);
    }

    /**Fill the array of visible cols*/
    private void loadVisibilityState() {
        //load visible columns
        String cols = GlobalOptions.getProperty(getPropertyBaseID() + ".visible.cols");
        int cnt = 0;
        boolean done = false;
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
                done = true;
            }
        }

        if (!done) {
            //col count has changed or property was not stored yet
            visibleColumns = new boolean[getInternalColumnNames().size()];
            for (int i = 0; i < visibleColumns.length; i++) {
                visibleColumns[i] = true;
            }
        }
    }

    public void loadColumnState() {
        //restore column widths
        if (mAssociatedTable != null) {
            String columnSizes = GlobalOptions.getProperty(getPropertyBaseID() + ".columns.size");
            if (columnSizes != null) {
                String[] sizeSplit = columnSizes.split(",");
                int col = 0;
                for (int i = 0; i < visibleColumns.length; i++) {
                    if (isColVisible(i)) {
                        int size = Integer.parseInt(sizeSplit[i]);
                        if (size > 0) {
                            // mAssociatedTable.getColumnModel().getColumn(col).setWidth(size);
                            mAssociatedTable.getColumnModel().getColumn(col).setPreferredWidth(size);
                            mAssociatedTable.getColumnModel().getColumn(col).addPropertyChangeListener(mColumnListener);
                        }
                        col++;
                    }
                }
            } else {
                int col = 0;
                for (int i = 0; i < visibleColumns.length; i++) {
                    if (isColVisible(i)) {
                        mAssociatedTable.getColumnModel().getColumn(col).addPropertyChangeListener(mColumnListener);
                        col++;
                    }
                }
            }
        }
    }

    /**Save the property holding the array of visible cols*/
    private void saveVisibilityState() {
        //store visible columns
        String colData = "";
        for (boolean col : visibleColumns) {
            colData += col + ",";
        }
        colData = colData.substring(0, colData.lastIndexOf(","));
        GlobalOptions.addProperty(getPropertyBaseID() + ".visible.cols", colData);

    }

    private void saveColumnState() {
        //store column widths
        if (mAssociatedTable != null) {
            //store column state because table is initialized
            String colSizes = "";
            int col = 0;
            for (int i = 0; i < visibleColumns.length; i++) {
                if (isColVisible(i)) {
                    colSizes += mAssociatedTable.getColumnModel().getColumn(col).getWidth() + ",";
                    col++;
                } else {
                    colSizes += "-1,";
                }
            }
            colSizes = colSizes.substring(0, colSizes.lastIndexOf(","));
            GlobalOptions.addProperty(getPropertyBaseID() + ".columns.size", colSizes);
        }
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
                    saveColumnState();
                    String text = ((JCheckBoxMenuItem) e.getSource()).getText();
                    int idx = getInternalColumnNames().indexOf(text);
                    visibleColumns[idx] = !visibleColumns[idx];
                    saveVisibilityState();
                    fireTableStructureChanged();
                    doNotifyOnColumnChange();
                    loadColumnState();
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
        return getColumnNames()[convertViewColumnToModel(col)];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return getColumnClasses()[convertViewColumnToModel(columnIndex)];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        col = convertViewColumnToModel(col);
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

    /**Translate a view column id to a model column id in respect to (in-)visible columns<BR/>
     * e.g. Visibility is as follows: |1|1|0|0|0|0|1|<BR/>
     * If we request the id of col 3 we get value 6 because all columns in between (and col 3 itself) are invisible<BR/>
     * If we request the id of col 1 we get value 1 because column 1 is visible in the view
     * If we request the id of col 4 an exception is thrown because the view has only 4 columns
     */
    protected int convertViewColumnToModel(int col) {
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

    protected int convertModelColumnToView(int col) {
        int realCol = col;
        for (int i = 0; i < col; i++) {
            if (!visibleColumns[i]) {
                realCol--;
            }
        }
        return realCol;
    }
}
