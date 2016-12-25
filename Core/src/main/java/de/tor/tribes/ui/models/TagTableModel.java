/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.Tag;
import de.tor.tribes.types.TagMapMarker;
import de.tor.tribes.util.tag.TagManager;
import javax.swing.table.AbstractTableModel;

/**
 * @author Torridity
 */
public class TagTableModel extends AbstractTableModel {

    private Class[] types = new Class[]{String.class, Integer.class, TagMapMarker.class, Boolean.class};
    private String[] colNames = new String[]{"Name", "Dörfer", "Kartenmarkierung", "Einzeichnen"};
    private boolean[] editableColumns = new boolean[]{true, false, true, true};

    public TagTableModel() {
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editableColumns[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    @Override
    public int getRowCount() {
        return TagManager.getSingleton().getAllElements().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            Tag t = (Tag) TagManager.getSingleton().getAllElements().get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return t.getName();
                }
                case 1: {
                    return t.getVillageIDs().size();
                }
                case 2: {
                    return t.getMapMarker();
                }
                default: {
                    return t.isShowOnMap();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        boolean completeUpdateNeeded = true;
        try {
            Tag t = (Tag) TagManager.getSingleton().getAllElements().get(rowIndex);

            switch (columnIndex) {
                case 0: {
                    t.setName((String) value);
                    completeUpdateNeeded = false;
                    break;
                }
                case 1: {
                    //do nothing
                    completeUpdateNeeded = false;
                    break;
                }
                case 2: {
                    TagMapMarker m = (TagMapMarker) value;
                    t.setTagColor(m.getTagColor());
                    t.setTagIcon(m.getTagIcon());
                    break;
                }
                default: {
                    t.setShowOnMap((Boolean) value);
                }
            }
        } catch (Exception ignored) {
        }
        //repaint map
        TagManager.getSingleton().revalidate(completeUpdateNeeded);
    }
}
