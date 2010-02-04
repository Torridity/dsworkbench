/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.Tag;
import de.tor.tribes.types.TagMapMarker;
import de.tor.tribes.ui.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.DSWorkbenchTagFrame;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.tag.TagManagerListener;
import java.io.PushbackReader;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jejkal
 */
public class TagTableModel extends AbstractDSWorkbenchTableModel {

    private final String PROPERTY_BASE_ID = "tag.table.model";
    protected static Class[] types;
    protected static String[] colNames;
    protected static List<String> internalNames;

    static {
        types = new Class[]{String.class, Integer.class, TagMapMarker.class, Boolean.class};
        colNames = new String[]{"Name", "Dörfer", "Kartenmarkierung", "Einzeichnen"};
        internalNames = Arrays.asList(new String[]{"Name", "Dörfer", "Kartenmarkierung", "Einzeichnen"});
    }
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

    public void addRow(Object[] row) {
        TagManager.getSingleton().addTag((String) row[0]);
    }

    public void removeRow(int pRow) {
        Tag t = TagManager.getSingleton().getTags().get(pRow);
        TagManager.getSingleton().removeTag(t);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        col = getRealColumnId(col);
        if (col != 1) {
            return true;
        }
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        columnIndex = getRealColumnId(columnIndex);
        switch (columnIndex) {
            case 0: {
                return TagManager.getSingleton().getTags().get(rowIndex).getName();
            }
            case 1: {
                return TagManager.getSingleton().getTags().get(rowIndex).getVillageIDs().size();
            }
            case 2: {
                return TagManager.getSingleton().getTags().get(rowIndex).getMapMarker();
            }
            default: {
                return TagManager.getSingleton().getTags().get(rowIndex).isShowOnMap();
            }
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        columnIndex = getRealColumnId(columnIndex);
        switch (columnIndex) {
            case 0: {
                TagManager.getSingleton().getTags().get(rowIndex).setName((String) value);
                break;
            }
            case 1: {
                //do nothing
                break;
            }
            case 2: {
                TagMapMarker m = (TagMapMarker) value;
                Tag t = TagManager.getSingleton().getTags().get(rowIndex);
                t.setTagColor(m.getTagColor());
                t.setTagIcon(m.getTagIcon());
                break;
            }
            default: {
                TagManager.getSingleton().getTags().get(rowIndex).setShowOnMap((Boolean) value);
            }
        }
        //repaint map
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(0);
    }

    @Override
    public String getPropertyBaseID() {
        return PROPERTY_BASE_ID;
    }

    @Override
    public Class[] getColumnClasses() {
        return types;
    }

    @Override
    public String[] getColumnNames() {
        return colNames;
    }

    @Override
    public List<String> getInternalColumnNames() {
        return internalNames;
    }

    @Override
    public void doNotifyOnColumnChange() {
        DSWorkbenchTagFrame.getSingleton();
    }
}
