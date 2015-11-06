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

import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.BarbarianAlly;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.conquer.ConquerManager;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class ConquerTableModel extends AbstractTableModel {

    private Class[] types = new Class[]{Village.class, String.class, String.class, String.class, Tribe.class, Ally.class, Tribe.class, Ally.class, Integer.class, Double.class};
    private String[] colNames = new String[]{"Dorf", "Dorfpunkte", "Kontinent", "Geadelt am", "Verlierer", "Stamm (Verlierer)", "Gewinner", "Stamm (Gewinner)", "Zustimmung", "Entfernung"};
    private boolean[] editableColumns = new boolean[]{false, false, false, false, false, false, false, false, false, false};

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
        return ConquerManager.getSingleton().getConquerCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Conquer c = ConquerManager.getSingleton().getConquer(rowIndex);

        switch (columnIndex) {
            case 0:
                return c.getVillage();
            case 1: {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(0);
                nf.setMaximumFractionDigits(0);
                Village v = c.getVillage();
                if (v != null) {
                    int points = v.getPoints();
                    return nf.format(points);
                } else {
                    return nf.format(0);
                }
            }
            case 2: {
                return c.getVillage().getContinentString();
            }
            case 3: {
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return f.format(new Date(c.getTimestamp() * 1000));//new Date( c.getTimestamp() * 1000);//f.format(new Date((long) c.getTimestamp() * 1000));
            }
            case 4: {
                Tribe t = c.getLoser();
                if (t == null) {
                    return Barbarians.getSingleton();
                } else {
                    return t;
                }
            }
            case 5: {
                Tribe t = c.getLoser();
                if (t == null) {
                    return BarbarianAlly.getSingleton();
                } else if (t.getAlly() == null) {
                    return NoAlly.getSingleton();
                } else {
                    return t.getAlly();
                }
            }
            case 6: {
                Tribe t = c.getWinner();
                if (t == null) {
                    return Barbarians.getSingleton();
                } else {
                    return t;
                }
            }
            case 7: {
                Tribe t = c.getWinner();
                if (t == null) {
                    return BarbarianAlly.getSingleton();
                } else if (t.getAlly() == null) {
                    return NoAlly.getSingleton();
                } else {
                    return t.getAlly();
                }
            }
            case 8: {
                return c.getCurrentAcceptance();
            }
            default: {
                Village v = c.getVillage();
                Village vUser = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                if (vUser != null) {
                    return DSCalculator.calculateDistance(v, vUser);
                } else {
                    return 0;
                }
            }
        }
    }
}
