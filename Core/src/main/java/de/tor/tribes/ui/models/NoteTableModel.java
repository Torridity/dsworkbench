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

import de.tor.tribes.types.Note;
import de.tor.tribes.util.note.NoteManager;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Charon
 */
public class NoteTableModel extends AbstractTableModel {

    private String sNoteSet = null;
    private final Class[] types = new Class[]{Integer.class, String.class, Integer.class, Integer.class, Date.class};
    private final String colNames[] = new String[]{"Icon", "Notiz", "Dörfer", "Kartensymbol", "Letzte Änderung"};
    private boolean[] editableColumns = new boolean[]{true, true, false, true, false};

    public NoteTableModel(String pMarkerSet) {
        sNoteSet = pMarkerSet;
    }

    public void setNoteSet(String pMarkerSet) {
        sNoteSet = pMarkerSet;
        fireTableDataChanged();
    }

    public String getNoteSet() {
        return sNoteSet;
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public String getColumnName(int columnIndex) {
        return colNames[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editableColumns[columnIndex];
    }

    @Override
    public int getRowCount() {
        if (sNoteSet == null) {
            return 0;
        }
        return NoteManager.getSingleton().getAllElements(sNoteSet).size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (sNoteSet == null) {
            return null;
        }

        Note n = ((Note) NoteManager.getSingleton().getAllElements(sNoteSet).get(rowIndex));
        switch (columnIndex) {
            case 0: {
                return n.getNoteSymbol();
            }
            case 1: {
                return n.getNoteText();
            }
            case 2: {
                return n.getVillageIds().size();
            }
            case 3: {
                return n.getMapMarker();
            }
            default: {
                return new Date(n.getTimestamp());// new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(n.getTimestamp());
            }
        }
    }

    @Override
    public void setValueAt(Object pValue, int rowIndex, int columnIndex) {
        Note n = ((Note) NoteManager.getSingleton().getAllElements(sNoteSet).get(rowIndex));
        switch (columnIndex) {
            case 0: {
                n.setNoteSymbol((Integer) pValue);
                break;
            }
            case 1: {
                n.setNoteText((String) pValue);
                // view can't be set
                break;
            }
            case 3: {
                n.setMapMarker((Integer) pValue);
                break;
            }
        }
        NoteManager.getSingleton().revalidate(sNoteSet, true);
    }
}
