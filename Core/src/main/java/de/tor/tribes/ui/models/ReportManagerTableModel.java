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

import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.report.ReportManager;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Torridity
 */
public class ReportManagerTableModel extends AbstractTableModel {

    private String sSet = null;
    private Class[] types = new Class[]{FightReport.status.class, Date.class, Tribe.class, Village.class, Tribe.class, Village.class, Integer.class, Byte.class};
    private String[] colNames = new String[]{"Status", "Gesendet", "Angreifer", "Herkunft", "Verteidiger", "Ziel", "Typ", "Sonstiges"};
    private static Logger logger = LogManager.getLogger("ReportTableModel");

    public ReportManagerTableModel(String pSet) {
        sSet = pSet;
    }

    public void setReportSet(String pSet) {
        sSet = pSet;
        fireTableDataChanged();
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
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    @Override
    public int getRowCount() {
        if (sSet == null) {
            return 0;
        }
        return ReportManager.getSingleton().getAllElements(sSet).size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (sSet == null) {
            return null;
        }
        try {
            FightReport r = (FightReport) ReportManager.getSingleton().getAllElements(sSet).get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.getStatus();
                case 1:
                    return new Date(r.getTimestamp());//new SimpleDateFormat("dd.MM.yy HH:mm").format(new Date(r.getTimestamp()));
                case 2:
                    return r.getAttacker();
                case 3:
                    return r.getSourceVillage();
                case 4:
                    return r.getDefender();
                case 5:
                    return r.getTargetVillage();
                case 6:
                    return r.guessType();
                default:
                    return r;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
