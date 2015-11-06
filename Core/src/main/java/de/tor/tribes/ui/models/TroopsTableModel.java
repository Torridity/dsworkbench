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
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class TroopsTableModel extends AbstractTableModel {

    private static Logger logger = Logger.getLogger("TroopTable");

    public enum COL_CONTENT {

        TRIBE, VILLAGE, LAST_CHANGE, SPEAR, SWORD, AXE, ARCHER, SPY, LIGHT, MARCHER, HEAVY, RAM, CATA, KNIGHT, MILITIA, SNOB, OFF, DEF, DEF_CAV, DEF_ARCH, OUTSIDE, INSIDE, FARM
    }
    private String sSet = null;
    private NumberFormat nf = NumberFormat.getInstance();
    private HashMap<String, ImageIcon> columnIcons = null;
    private List<COL_CONTENT> content = null;

    public TroopsTableModel(String pSet) {
        sSet = pSet;
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        fireTableStructureChanged();
    }

    public void setTroopSet(String pSet) {
        sSet = pSet;
        fireTableDataChanged();
    }

    public String getTroopSet() {
        return sSet;
    }

    @Override
    public final void fireTableStructureChanged() {
        content = new ArrayList<COL_CONTENT>();
        content.add(COL_CONTENT.TRIBE);
        content.add(COL_CONTENT.VILLAGE);
        content.add(COL_CONTENT.LAST_CHANGE);
        columnIcons = new HashMap<String, ImageIcon>();
        columnIcons.put("Spieler", null);
        columnIcons.put("Dorf", null);
        columnIcons.put("Stand", null);

        for (UnitHolder pUnit : DataHolder.getSingleton().getUnits()) {
            if (pUnit.getPlainName().equals("spear")) {
                content.add(COL_CONTENT.SPEAR);
            } else if (pUnit.getPlainName().equals("sword")) {
                content.add(COL_CONTENT.SWORD);
            } else if (pUnit.getPlainName().equals("axe")) {
                content.add(COL_CONTENT.AXE);
            } else if (pUnit.getPlainName().equals("archer")) {
                content.add(COL_CONTENT.ARCHER);
            } else if (pUnit.getPlainName().equals("spy")) {
                content.add(COL_CONTENT.SPY);
            } else if (pUnit.getPlainName().equals("light")) {
                content.add(COL_CONTENT.LIGHT);
            } else if (pUnit.getPlainName().equals("marcher")) {
                content.add(COL_CONTENT.MARCHER);
            } else if (pUnit.getPlainName().equals("heavy")) {
                content.add(COL_CONTENT.HEAVY);
            } else if (pUnit.getPlainName().equals("ram")) {
                content.add(COL_CONTENT.RAM);
            } else if (pUnit.getPlainName().equals("catapult")) {
                content.add(COL_CONTENT.CATA);
            } else if (pUnit.getPlainName().equals("snob")) {
                content.add(COL_CONTENT.SNOB);
            } else if (pUnit.getPlainName().equals("knight")) {
                content.add(COL_CONTENT.KNIGHT);
            } else if (pUnit.getPlainName().equals("militia")) {
                content.add(COL_CONTENT.MILITIA);
            }
            columnIcons.put(pUnit.getName(), ImageManager.getUnitIcon(pUnit));
        }
        content.add(COL_CONTENT.OFF);
        content.add(COL_CONTENT.DEF);
        content.add(COL_CONTENT.DEF_CAV);
        content.add(COL_CONTENT.DEF_ARCH);
        content.add(COL_CONTENT.OUTSIDE);
        content.add(COL_CONTENT.INSIDE);
        content.add(COL_CONTENT.FARM);
        columnIcons.put("Angriff", new ImageIcon("graphics/icons/att.png"));
        columnIcons.put("Verteidigung", new ImageIcon("graphics/icons/def.png"));
        columnIcons.put("Verteidigung (Kavallerie)", new ImageIcon("graphics/icons/def_cav.png"));
        columnIcons.put("Verteidigung (Bogen)", new ImageIcon("graphics/icons/def_archer.png"));
        columnIcons.put("Unterstützungen außerhalb", new ImageIcon("graphics/icons/move_out.png"));
        columnIcons.put("Unterstützungen innerhalb", new ImageIcon("graphics/icons/move_in.png"));
        columnIcons.put("Bauernhofbedarf", new ImageIcon("graphics/icons/farm.png"));
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
            case TRIBE:
                return Tribe.class;
            case VILLAGE:
                return Village.class;
            case LAST_CHANGE:
                return Date.class;
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
            case MILITIA:
            case SNOB:
            case OFF:
            case DEF:
            case DEF_CAV:
            case DEF_ARCH:
            case OUTSIDE:
            case INSIDE:
                return Integer.class;
            case FARM:
                return Float.class;
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (content == null || columnIndex < 0 || sSet.equals(TroopsManager.SUPPORT_GROUP)) {
            return false;
        }
        COL_CONTENT colContent = content.get(columnIndex);
        switch (colContent) {
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
            case MILITIA:
            case SNOB:
                return true;
        }
        return false;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (content == null || columnIndex < 0) {
            return null;
        }

        COL_CONTENT colContent = content.get(columnIndex);
        switch (colContent) {
            case TRIBE:
                return "Spieler";
            case VILLAGE:
                return "Dorf";
            case LAST_CHANGE:
                return "Stand";
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
            case MILITIA:
                return "Miliz";
            case SNOB:
                return "Adelsgeschlecht";
            case OFF:
                return "Angriff";
            case DEF:
                return "Verteidigung";
            case DEF_CAV:
                return "Verteidigung (Kavallerie)";
            case DEF_ARCH:
                return "Verteidigung (Bogen)";
            case OUTSIDE:
                return "Unterstützungen außerhalb";
            case INSIDE:
                return "Unterstützungen innerhalb";
            case FARM:
                return "Bauernhofbedarf";
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
        if (sSet == null) {
            return 0;
        }
        try {
            return TroopsManager.getSingleton().getAllElements(sSet).size();
        } catch (NullPointerException npe) {
            return 0;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (content == null) {
            return null;
        }

        COL_CONTENT colContent = content.get(columnIndex);
        VillageTroopsHolder h = TroopsManager.getSingleton().getManagedElement(sSet, rowIndex);
        switch (colContent) {
            case TRIBE:
                return h.getVillage().getTribe();
            case VILLAGE:
                return h.getVillage();
            case LAST_CHANGE:
                return h.getState();//new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(h.getState());
            case SPEAR:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("spear"));
            case SWORD:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("sword"));
            case AXE:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("axe"));
            case ARCHER:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("archer"));
            case SPY:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("spy"));
            case LIGHT:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("light"));
            case MARCHER:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("marcher"));
            case HEAVY:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("heavy"));
            case RAM:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("ram"));
            case CATA:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("catapult"));
            case KNIGHT:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("knight"));
            case MILITIA:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("militia"));
            case SNOB:
                return h.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("snob"));
            case OFF:
                return h.getOffValue();
            case DEF:
                return h.getDefValue();
            case DEF_CAV:
                return h.getDefCavalryValue();
            case DEF_ARCH:
                return h.getDefArcherValue();
            case OUTSIDE:
                return 0;
            case INSIDE:
                return 0;
            case FARM:
                return h.getFarmSpace();
        }
        return null;
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        if (content == null) {
            return;
        }
        COL_CONTENT colContent = content.get(pCol);
        VillageTroopsHolder h = TroopsManager.getSingleton().getManagedElement(sSet, pRow);
        Integer val = null;
        try {
            if (pValue == null) {
                //value was deleted
                val = 0;
            } else {
                val = (Integer) pValue;
            }
        } catch (Exception e) {
            val = null;
        }

        if (val == null) {
            logger.error("Failed to set troop amount (Value class is '" + pValue.getClass() + "')");
            return;
        }
        switch (colContent) {
            case SPEAR:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("spear"), val.intValue());
                break;
            case SWORD:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("sword"), val.intValue());
                break;
            case AXE:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("axe"), val.intValue());
                break;
            case ARCHER:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("archer"), val.intValue());
                break;
            case SPY:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("spy"), val.intValue());
                break;
            case LIGHT:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("light"), val.intValue());
                break;
            case MARCHER:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("marcher"), val.intValue());
                break;
            case HEAVY:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("heavy"), val.intValue());
                break;
            case RAM:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("ram"), val.intValue());
                break;
            case CATA:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("catapult"), val.intValue());
                break;
            case KNIGHT:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("knight"), val.intValue());
                break;
            case MILITIA:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("militia"), val.intValue());
                break;
            case SNOB:
                h.getTroops().put(DataHolder.getSingleton().getUnitByPlainName("snob"), val.intValue());
                break;
        }
        //update troops layer
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TROOP_LAYER);
    }
}
