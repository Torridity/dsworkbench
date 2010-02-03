/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.util.GlobalOptions;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.NotSerializableException;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public abstract class AbstractDSWorkbenchTableModel extends AbstractTableModel {

    protected static Class[] types;
    protected static String[] colNames;
    protected static List<String> internalNames;
    private boolean[] visibleColumns = null;
    private JPopupMenu popup = null;

    public abstract List<String> getInternalColumnNames();

    public abstract String getPropertyBaseID();

    public abstract String[] getColumnNames();

    public abstract void doNotifyOnColumnChange();

    public AbstractDSWorkbenchTableModel() {
        checkStaticImplementation();
        loadVisibilityState();
        buildPopup();
    }

    private void checkStaticImplementation() throws UnsupportedOperationException {
        if (types == null || colNames == null || internalNames == null) {
            throw new UnsupportedOperationException("Static initialization not implemented yet");
        }
    }

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

    private void saveVisibilityState() {
        String colData = "";
        for (boolean col : visibleColumns) {
            colData += col + ",";
        }
        colData = colData.substring(0, colData.lastIndexOf(","));
        GlobalOptions.addProperty(getPropertyBaseID() + ".visible.cols", colData);
    }

    private void buildPopup() {
        int cnt = 0;
        popup = new JPopupMenu("Angezeigte Spalten");
        for (String name : getInternalColumnNames()) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, visibleColumns[cnt]);
            item.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    String text = ((JCheckBoxMenuItem) e.getSource()).getText();
                    System.out.println(getInternalColumnNames());
                    System.out.println("Getting");
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

    public JPopupMenu getPopup() {
        return popup;
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

    protected int getNumber(int col) {
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
