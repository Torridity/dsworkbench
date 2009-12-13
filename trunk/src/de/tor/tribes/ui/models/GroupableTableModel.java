/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class GroupableTableModel extends AbstractTableModel {

    private List<String> groups = new LinkedList<String>();
    private Hashtable<String, List<String>> mValues = null;
    private List<String> collapsed = new LinkedList<String>();

    public GroupableTableModel() {
        mValues = new Hashtable<String, List<String>>();
        for (int i = 0; i < 100; i++) {
            String group = "Group" + i;
            List<String> values = new LinkedList<String>();
            groups.add(group);
            if (i % 2 == 0) {
                collapsed.add(group);
            }
            for (int j = 0; j < 100; j++) {
                String key = "SubGroup" + i + "." + j;
                values.add(key);
            }
            mValues.put(group, values);
        }
    }

    @Override
    public int getRowCount() {
        Enumeration<String> keys = mValues.keys();
        int rows = 0;
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            rows++;
            if (!collapsed.contains(key)) {
                rows += mValues.get(key).size();
            }
        }
        return rows;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        int value = 0;
        for (String group : groups) {

            if (value == rowIndex) {
                if (columnIndex == 0) {
                    return group;
                } else {
                    return "";
                }
            } else {
                if (!collapsed.contains(group)) {
                    //group is not collapsed
                    int subvalue = 0;
                    for (String v : mValues.get(group)) {
                        value++;
                        if (value == rowIndex) {
                            if (columnIndex == 0) {
                                return group;
                            } else if (columnIndex == 1) {
                                return v;
                            } else {
                                return "";
                            }
                        } else {
                            subvalue++;
                        }
                    }
                }

            }
            value++;
        }
        return null;
    }
}

