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

import de.tor.tribes.types.Defense;
import de.tor.tribes.types.ext.Village;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 *
 * @author Torridity
 */
public class SupportsModel extends AbstractTableModel {

    private List<Defense> entries = null;
    private Class[] types = new Class[]{Village.class, Village.class, Date.class, Date.class, String.class, Boolean.class};
    private String[] colNames = new String[]{"Herkunft", "Ziel", "Früheste Abschickzeit", "Späteste Abschickzeit", "Countdown", "Übertragen"};

    public SupportsModel() {
        entries = new ArrayList<>();
    }

    public void clear() {
        entries.clear();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    public Defense[] getRows() {
        return entries.toArray(new Defense[entries.size()]);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    public void addRow(Defense pElement) {
        entries.add(pElement);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Defense elem = entries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return elem.getSupporter();
            case 1:
                return elem.getTarget();
            case 2:
                return new Date(elem.getBestSendTime());
            case 3:
                return new Date(elem.getWorstSendTime());
            case 4:

                long sendTime = elem.getBestSendTime();
                if (sendTime < System.currentTimeMillis()) {//if best in past, take worst
                    sendTime = elem.getWorstSendTime();
                }
                long t = sendTime - System.currentTimeMillis();
                t = (t <= 0) ? 0 : t;
                return DurationFormatUtils.formatDuration(t, "HHH:mm:ss.SSS", true);
            default:
                return elem.isTransferredToBrowser();
        }
    }
}
