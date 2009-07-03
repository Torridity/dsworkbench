/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.TroopsManagerListener;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jejkal
 */
public class TroopsManagerTableModel extends AbstractTableModel {

    public final static int SHOW_TROOPS_IN_VILLAGE = 0;
    public final static int SHOW_OWN_TROOPS = 1;
    public final static int SHOW_TROOPS_OUTSIDE = 2;
    public final static int SHOW_TROOPS_ON_THE_WAY = 3;
    public final static int SHOW_FORGEIGN_TROOPS = 4;
    Class[] types = null;
    String[] colNames = null;
    private static TroopsManagerTableModel SINGLETON = null;
    private NumberFormat nf = null;
    private int viewType = SHOW_TROOPS_IN_VILLAGE;

    public static synchronized TroopsManagerTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TroopsManagerTableModel();
        }
        return SINGLETON;
    }

    /**Setup the table depending on the number of troops of the current server*/
    public void setup() {
        List<Class> typesList = new LinkedList<Class>();
        List<String> namesList = new LinkedList<String>();
        typesList.add(Tribe.class);
        namesList.add("Spieler");
        typesList.add(Village.class);
        namesList.add("Dorf");
        typesList.add(Date.class);
        namesList.add("Stand");
        for (int i = 0; i < DataHolder.getSingleton().getUnits().size(); i++) {
            typesList.add(Integer.class);
            namesList.add("");
        }

        //fight power cols
        namesList.add("");
        typesList.add(Double.class);
        namesList.add("");
        typesList.add(Double.class);
        namesList.add("");
        typesList.add(Double.class);
        namesList.add("");
        typesList.add(Double.class);
        //troops in/out cols
        namesList.add("");
        typesList.add(Integer.class);
        namesList.add("");
        typesList.add(Integer.class);

        types = typesList.toArray(new Class[]{});
        colNames = namesList.toArray(new String[]{});
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
    }

    TroopsManagerTableModel() {
        TroopsManager.getSingleton().addTroopsManagerListener(new TroopsManagerListener() {

            @Override
            public void fireTroopsChangedEvent() {
                fireTableDataChanged();
            }
        });
    }

    @Override
    public int getRowCount() {
        return TroopsManager.getSingleton().getEntryCount();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public int getColumnCount() {
        if (types == null) {
            return 0;
        }
        return types.length;
    }

    @Override
    public String getColumnName(int col) {
        return colNames[col];
    }

    public void addRow(Object[] row) {
        TroopsManager.getSingleton().addTroopsForVillage((Village) row[0], (Date) row[1], (List<Integer>) row[2]);
    }

    public void removeRow(int pRow) {
        Village v = TroopsManager.getSingleton().getVillages()[pRow];
        if (v != null) {
            TroopsManager.getSingleton().removeTroopsForVillage(v);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if ((col > 1) && (col <= 2 + DataHolder.getSingleton().getUnits().size()) && viewType != SHOW_TROOPS_IN_VILLAGE && viewType != SHOW_FORGEIGN_TROOPS) {
            return true;
        }
        return false;
    }

    public void setViewType(int type) {
        viewType = type;
        fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Village row = TroopsManager.getSingleton().getVillages()[rowIndex];
        switch (columnIndex) {
            case 0: {
                return row.getTribe();
            }
            case 1: {
                return row;
            }
            case 2: {
                return TroopsManager.getSingleton().getTroopsForVillage(row).getState();
            }
            default: {
                int unitCount = DataHolder.getSingleton().getUnits().size();
                if (columnIndex < 3 + unitCount) {
                    try {
                        int troopIndex = columnIndex - 3;
                        UnitHolder unit = DataHolder.getSingleton().getUnits().get(troopIndex);
                        switch (viewType) {
                            case SHOW_OWN_TROOPS:
                                return TroopsManager.getSingleton().getTroopsForVillage(row).getOwnTroops().get(unit);
                            case SHOW_TROOPS_OUTSIDE:
                                return TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsOutside().get(unit);
                            case SHOW_TROOPS_ON_THE_WAY:
                                return TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsOnTheWay().get(unit);
                            case SHOW_FORGEIGN_TROOPS:
                                int own = TroopsManager.getSingleton().getTroopsForVillage(row).getOwnTroops().get(unit);
                                int inVillage = TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsInVillage().get(unit);
                                double res = inVillage - own;
                                return (res >= 0) ? res : 0;
                            default:
                                return TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsInVillage().get(unit);
                        }
                    //    return TroopsManager.getSingleton().getTroopsForVillage(row).getTroops().get(troopIndex);
                    } catch (Exception e) {
                        return 0;
                    }
                } else {
                    //troop power columns
                    if (columnIndex == unitCount + 3) {
                        return TroopsManager.getSingleton().getTroopsForVillage(row).getOffValue(viewType);
                    } else if (columnIndex == unitCount + 4) {
                        return TroopsManager.getSingleton().getTroopsForVillage(row).getDefValue(viewType);
                    } else if (columnIndex == unitCount + 5) {
                        return TroopsManager.getSingleton().getTroopsForVillage(row).getDefCavalryValue(viewType);
                    } else if (columnIndex == unitCount + 6) {
                        return TroopsManager.getSingleton().getTroopsForVillage(row).getDefArcherValue(viewType);
                    } //in/out count
                    else if (columnIndex == unitCount + 7) {
                        return TroopsManager.getSingleton().getTroopsForVillage(row).getSupportTargets().size();
                    } else {
                        return TroopsManager.getSingleton().getTroopsForVillage(row).getSupports().size();
                    }
                }
            }
        }
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        switch (pCol) {
            case 0: {
                //not allowed
                break;
            }
            case 1: {
                //not allowed
                break;
            }
            case 2: {
                //not allowed
                break;
            }
            default: {
                int troopIndex = pCol - 3;
                Village row = TroopsManager.getSingleton().getVillages()[pRow];
                UnitHolder unit = DataHolder.getSingleton().getUnits().get(troopIndex);
                Integer value = null;
                try {
                    value = (Integer) pValue;
                } catch (Exception e) {
                    return;
                }
                if (viewType == SHOW_FORGEIGN_TROOPS) {
                    return;
                }
                //set current troops
                switch (viewType) {
                    case SHOW_OWN_TROOPS:
                        TroopsManager.getSingleton().getTroopsForVillage(row).getOwnTroops().put(unit, value);
                        break;
                    case SHOW_TROOPS_OUTSIDE:
                        TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsOutside().put(unit, value);
                        break;
                    case SHOW_TROOPS_ON_THE_WAY:
                        TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsOnTheWay().put(unit, value);
                        break;
                    default:
                    //not allowed due to troops in village are calculated
                    //TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsInVillageWithoutSupport().put(unit, value);
                }

                //refresh time
                TroopsManager.getSingleton().getTroopsForVillage(row).setState(Calendar.getInstance().getTime());
                fireTableDataChanged();
            }
        }
    }
}
