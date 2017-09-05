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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.SupportType;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.troops.SupportVillageTroopsHolder;
import de.tor.tribes.util.troops.TroopsManager;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class SupportTableModel extends AbstractTableModel {

    public enum COL_CONTENT {

        DIRECTION, TRIBE, VILLAGE, SPEAR, SWORD, AXE, ARCHER, SPY, LIGHT, MARCHER, HEAVY, RAM, CATA, KNIGHT, MILITIA, SNOB, OFF, DEF, DEF_CAV, DEF_ARCH, FARM
    }
    private NumberFormat nf = NumberFormat.getInstance();
    private HashMap<String, ImageIcon> columnIcons = null;
    private List<COL_CONTENT> content = null;
    private List<SupportType> data = null;

    public SupportTableModel(List<Village> pSupportVillages) {
        data = new LinkedList<>();
        for (Village v : pSupportVillages) {
            SupportVillageTroopsHolder holder = (SupportVillageTroopsHolder) TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.SUPPORT);
            if (holder != null) {
                //add incs 
                Hashtable<Village, Hashtable<UnitHolder, Integer>> supp = holder.getIncomingSupports();
                Enumeration<Village> keys = supp.keys();
                while (keys.hasMoreElements()) {
                    Village supportSource = keys.nextElement();
                    data.add(new SupportType(supportSource, supp.get(supportSource), SupportType.DIRECTION.INCOMING));
                }
                
                //add outs 
                supp = holder.getOutgoingSupports();
                keys = supp.keys();
                while (keys.hasMoreElements()) {
                    Village supportSource = keys.nextElement();
                    data.add(new SupportType(supportSource, supp.get(supportSource), SupportType.DIRECTION.OUTGOING));
                }
            }

        }
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        fireTableStructureChanged();
    }

    @Override
    public final void fireTableStructureChanged() {
        content = new LinkedList<>();
        content.add(COL_CONTENT.DIRECTION);
        content.add(COL_CONTENT.TRIBE);
        content.add(COL_CONTENT.VILLAGE);
        columnIcons = new HashMap<>();
        columnIcons.put("Richtung", null);
        columnIcons.put("Spieler", null);
        columnIcons.put("Dorf", null);

        for (UnitHolder pUnit : DataHolder.getSingleton().getUnits()) {
            switch (pUnit.getPlainName()) {
                case "spear":
                    content.add(COL_CONTENT.SPEAR);
                    break;
                case "sword":
                    content.add(COL_CONTENT.SWORD);
                    break;
                case "axe":
                    content.add(COL_CONTENT.AXE);
                    break;
                case "archer":
                    content.add(COL_CONTENT.ARCHER);
                    break;
                case "spy":
                    content.add(COL_CONTENT.SPY);
                    break;
                case "light":
                    content.add(COL_CONTENT.LIGHT);
                    break;
                case "marcher":
                    content.add(COL_CONTENT.MARCHER);
                    break;
                case "heavy":
                    content.add(COL_CONTENT.HEAVY);
                    break;
                case "ram":
                    content.add(COL_CONTENT.RAM);
                    break;
                case "catapult":
                    content.add(COL_CONTENT.CATA);
                    break;
                case "snob":
                    content.add(COL_CONTENT.SNOB);
                    break;
                case "knight":
                    content.add(COL_CONTENT.KNIGHT);
                    break;
            }
            columnIcons.put(pUnit.getName(), ImageManager.getUnitIcon(pUnit));
        }
        super.fireTableStructureChanged();
    }

    @Override
    public int getColumnCount() {
        return content.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (content == null || columnIndex < 0) {
            return null;
        }
        COL_CONTENT colContent = content.get(columnIndex);
        switch (colContent) {
            case DIRECTION:
                return SupportType.DIRECTION.class;
            case TRIBE:
                return Tribe.class;
            case VILLAGE:
                return Village.class;
            case SPEAR:
            case SWORD:
            case AXE:
            case ARCHER:
            case SPY:
            case LIGHT:
            case MARCHER:
            case HEAVY:
            case RAM:
            case CATA:
            case KNIGHT:
            case SNOB:
                return Number.class;
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (content == null || columnIndex < 0) {
            return null;
        }

        COL_CONTENT colContent = content.get(columnIndex);
        switch (colContent) {
            case DIRECTION:
                return "Richtung";
            case TRIBE:
                return "Spieler";
            case VILLAGE:
                return "Dorf";
            case SPEAR:
                return "Speerträger";
            case SWORD:
                return "Schwertkämpfer";
            case AXE:
                return "Axtkämpfer";
            case ARCHER:
                return "Bogenschütze";
            case SPY:
                return "Späher";
            case LIGHT:
                return "Leichte Kavallerie";
            case MARCHER:
                return "Berittener Bogenschütze";
            case HEAVY:
                return "Schwere Kavallerie";
            case RAM:
                return "Ramme";
            case CATA:
                return "Katapult";
            case KNIGHT:
                return "Paladin";
            case SNOB:
                return "Adelsgeschlecht";
        }
        return null;
    }

    public ImageIcon getColumnIcon(String pColumnName) {
        if (content == null) {
            return null;
        }
        return columnIcons.get(pColumnName);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (content == null) {
            return null;
        }

        COL_CONTENT colContent = content.get(columnIndex);
        SupportType h = data.get(rowIndex);
        switch (colContent) {
            case DIRECTION:
                return h.getDirection();
            case TRIBE:
                return h.getVillage().getTribe();
            case VILLAGE:
                return h.getVillage();
            case SPEAR:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("spear"));
            case SWORD:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("sword"));
            case AXE:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("axe"));
            case ARCHER:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("archer"));
            case SPY:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("spy"));
            case LIGHT:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("light"));
            case MARCHER:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("marcher"));
            case HEAVY:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("heavy"));
            case RAM:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("ram"));
            case CATA:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("catapult"));
            case KNIGHT:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("knight"));
            case SNOB:
                return h.getTroopsOfUnit(DataHolder.getSingleton().getUnitByPlainName("snob"));
        }
        return null;
    }   
}
