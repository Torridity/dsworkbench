/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.models.TroopsTableModel.COL_CONTENT;
import de.tor.tribes.ui.tree.IncomingTroopsUserObject;
import de.tor.tribes.ui.tree.OutgoingTroopsUserObject;
import de.tor.tribes.util.troops.SupportVillageTroopsHolder;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 *
 * @author Torridity
 */
public class SupportTroopsTableModel extends AbstractTreeTableModel {

    private List<TroopsTableModel.COL_CONTENT> content = null;
    private HashMap<String, ImageIcon> columnIcons = null;
    private boolean topLevelOnly = false;

    public SupportTroopsTableModel(DefaultMutableTreeNode pRoot) {
        super(pRoot);
        buildStructure();
    }

    public final void buildStructure() {
        content = new LinkedList<COL_CONTENT>();
        content.add(COL_CONTENT.VILLAGE);
        content.add(COL_CONTENT.TRIBE);
        content.add(COL_CONTENT.LAST_CHANGE);
        columnIcons = new HashMap<String, ImageIcon>();
        columnIcons.put("Dorf", null);
        columnIcons.put("Spieler", null);
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
                return String.class;
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
                return Number.class;
            case FARM:
                return Float.class;
        }
        return null;
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
    public int getColumnCount() {
        return content.size();
    }

    /**Return top level elements only --> used within DSWorkbenchTroopFrame for support renderer*/
    public void setTopLevelOnly(boolean pValue) {
        topLevelOnly = pValue;
    }

    @Override
    public Object getValueAt(Object arg0, int arg1) {
        if (arg0 instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) arg0;
            if (dataNode.getUserObject() instanceof SupportVillageTroopsHolder) {
                return getColumnValue((SupportVillageTroopsHolder) dataNode.getUserObject(), arg1);
            } else if (dataNode.getUserObject() instanceof OutgoingTroopsUserObject) {
                if (topLevelOnly) {
                    return null;
                }
                OutgoingTroopsUserObject u = (OutgoingTroopsUserObject) dataNode.getUserObject();
                return getColumnValue(u.getTroopsHolder(), arg1);
            } else if (dataNode.getUserObject() instanceof IncomingTroopsUserObject) {
                if (topLevelOnly) {
                    return null;
                }
                IncomingTroopsUserObject u = (IncomingTroopsUserObject) dataNode.getUserObject();
                return getColumnValue(u.getTroopsHolder(), arg1);
            }
        }
        return null;
    }

    private Object getColumnValue(VillageTroopsHolder pHolder, int col) {
        COL_CONTENT colContent = content.get(col);
        switch (colContent) {
            case TRIBE:
                return pHolder.getVillage().getTribe();
            case VILLAGE:
                return pHolder.getVillage();
            case LAST_CHANGE:
                if (pHolder.getState().getTime() == 0) {
                    return "-";
                }
                return new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(pHolder.getState());
            case SPEAR:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("spear"));
            case SWORD:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("sword"));
            case AXE:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("axe"));
            case ARCHER:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("archer"));
            case SPY:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("spy"));
            case LIGHT:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("light"));
            case MARCHER:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("marcher"));
            case HEAVY:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("heavy"));
            case RAM:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("ram"));
            case CATA:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("catapult"));
            case KNIGHT:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("knight"));
            case MILITIA:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("militia"));
            case SNOB:
                return pHolder.getTroopsOfUnitInVillage(DataHolder.getSingleton().getUnitByPlainName("snob"));
            case OFF:
                return "-";
            case DEF:
                return pHolder.getDefValue();
            case DEF_CAV:
                return pHolder.getDefCavalryValue();
            case DEF_ARCH:
                return pHolder.getDefArcherValue();
            case OUTSIDE:
                if (pHolder instanceof SupportVillageTroopsHolder) {
                    return ((SupportVillageTroopsHolder) pHolder).getOutgoingSupports().size();
                }
                return 0;
            case INSIDE:
                if (pHolder instanceof SupportVillageTroopsHolder) {
                    return ((SupportVillageTroopsHolder) pHolder).getIncomingSupports().size();
                }
                return 0;
            case FARM:
                return pHolder.getFarmSpace();
        }
        return null;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode nodes = (DefaultMutableTreeNode) parent;
            return nodes.getChildAt(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode nodes = (DefaultMutableTreeNode) parent;
            return nodes.getChildCount();
        }
        return 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }
}
