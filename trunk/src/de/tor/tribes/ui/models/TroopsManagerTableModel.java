/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.TroopsManagerListener;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
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
    private Tag[] visibleTags = null;
    private boolean useANDConnection = true;
    private Village[] filteredVillages = null;
    private Hashtable<UnitHolder, Integer> summedAmount = null;

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
        summedAmount = new Hashtable<UnitHolder, Integer>();
        for (int i = 0; i < DataHolder.getSingleton().getUnits().size(); i++) {
            typesList.add(Integer.class);
            namesList.add("");
            summedAmount.put(DataHolder.getSingleton().getUnits().get(i), 0);
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
        namesList.add("");
        typesList.add(Float.class);

        types = typesList.toArray(new Class[]{});
        colNames = namesList.toArray(new String[]{});
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        filteredVillages = null;
    }

    TroopsManagerTableModel() {
        TroopsManager.getSingleton().addTroopsManagerListener(new TroopsManagerListener() {

            @Override
            public void fireTroopsChangedEvent() {
                setVisibleTags(null, useANDConnection);
                fireTableDataChanged();
            }
        });
    }

    public void setVisibleTags(List<Tag> pTags, boolean pRelation) {
        if (pTags == null || pTags.isEmpty()) {
            visibleTags = null;
        } else {
            visibleTags = pTags.toArray(new Tag[]{});
        }
        useANDConnection = pRelation;
        filteredVillages = TroopsManager.getSingleton().getVillages(visibleTags, useANDConnection);
        updateSum();
    }

    public void updateSum() {
        summedAmount = new Hashtable<UnitHolder, Integer>();
        if (filteredVillages == null) {
            filteredVillages = TroopsManager.getSingleton().getVillages();
        }
        for (Village v : filteredVillages) {
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = summedAmount.get(unit);
                if (amount == null) {
                    amount = 0;
                }
                try {
                    switch (viewType) {
                        case SHOW_OWN_TROOPS:
                            summedAmount.put(unit, amount + TroopsManager.getSingleton().getTroopsForVillage(v).getOwnTroops().get(unit));
                            break;
                        case SHOW_TROOPS_OUTSIDE:
                            summedAmount.put(unit, amount + TroopsManager.getSingleton().getTroopsForVillage(v).getTroopsOutside().get(unit));
                            break;
                        case SHOW_TROOPS_ON_THE_WAY:
                            summedAmount.put(unit, amount + TroopsManager.getSingleton().getTroopsForVillage(v).getTroopsOnTheWay().get(unit));
                            break;
                        case SHOW_FORGEIGN_TROOPS:
                            int own = TroopsManager.getSingleton().getTroopsForVillage(v).getOwnTroops().get(unit);
                            int inVillage = TroopsManager.getSingleton().getTroopsForVillage(v).getTroopsInVillage().get(unit);
                            double res = inVillage - own;
                            summedAmount.put(unit, amount + ((res >= 0) ? (int) res : 0));
                            break;
                        default:
                            summedAmount.put(unit, amount + TroopsManager.getSingleton().getTroopsForVillage(v).getTroopsInVillage().get(unit));
                            break;
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public Hashtable<UnitHolder, Integer> getSummedAmounts() {
        return summedAmount;
    }

    @Override
    public int getRowCount() {
        if (filteredVillages != null) {
            return filteredVillages.length;
        }
        return TroopsManager.getSingleton().getEntryCount();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        try {
            return types[columnIndex];
        } catch (Exception e) {
            return types[types.length - 1];
        }
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
        try {
            return colNames[col];
        } catch (Exception e) {
            return colNames[colNames.length - 1];
        }
    }

    public void addRow(Object[] row) {
        TroopsManager.getSingleton().addTroopsForVillage((Village) row[0], (Date) row[1], (List<Integer>) row[2]);
    }

    public void removeRow(int pRow) {
        Village v = null;
        if (filteredVillages != null) {
            v = filteredVillages[pRow];
        } else {
            v = TroopsManager.getSingleton().getVillages()[pRow];
        }
        if (v != null) {
            TroopsManager.getSingleton().removeTroopsForVillage(v);
        }
        setVisibleTags(null, useANDConnection);
    }

    public void removeRows(Integer[] pRows) {
        List<Village> villages = new LinkedList<Village>();
        for (Integer row : pRows) {
            Village v = null;
            if (filteredVillages != null) {
                v = filteredVillages[row];
            } else {
                v = TroopsManager.getSingleton().getVillages()[row];
            }
            if (v != null && !villages.contains(v)) {
                villages.add(v);
            }
        }
        TroopsManager.getSingleton().removeTroopsForVillages(villages.toArray(new Village[]{}));
        setVisibleTags(null, useANDConnection);
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
        updateSum();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Village[] rows = filteredVillages;
        if (rows == null) {
            rows = TroopsManager.getSingleton().getVillages();
        }
        if (rowIndex > rows.length - 1) {
            return null;
        }

        Village row = rows[rowIndex];

        switch (columnIndex) {
            case 0: {
                return row.getTribe();
            }
            case 1: {
                return row;
            }

            case 2: {
                try {
                    return TroopsManager.getSingleton().getTroopsForVillage(row).getState();
                } catch (Exception e) {
                    return new Date();
                }
            }

            default: {
                try {
                    int unitCount = DataHolder.getSingleton().getUnits().size();
                    if (columnIndex < 3 + unitCount) {
                        try {
                            int troopIndex = columnIndex - 3;
                            System.out.println("Index " + troopIndex);
                            UnitHolder unit = DataHolder.getSingleton().getUnits().get(troopIndex);
                            System.out.println("Unit: " + unit);
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
                        } else if (columnIndex == unitCount + 8) {
                            return TroopsManager.getSingleton().getTroopsForVillage(row).getSupports().size();
                        }//Farm space
                        else {
                            try {
                                return TroopsManager.getSingleton().getTroopsForVillage(row).getFarmSpace();
                            } catch (Exception e) {
                                return 0.0f;
                            }

                        }
                    }
                } catch (Exception e) {
                    return 0;
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
                Village[] rows = filteredVillages;
                if (rows == null) {
                    rows = TroopsManager.getSingleton().getVillages();
                }
                if (pRow > rows.length - 1) {
                    return;
                }

                Village row = rows[pRow];
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
                        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(row);
                        holder.getOwnTroops().put(unit, value);
                        //own changed, so update in-village troops
                        holder.updateSupportValues();
                        break;

                    case SHOW_TROOPS_OUTSIDE:
                        TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsOutside().put(unit, value);
                        break;

                    case SHOW_TROOPS_ON_THE_WAY:
                        TroopsManager.getSingleton().getTroopsForVillage(row).getTroopsOnTheWay().put(unit, value);
                        break;

                    default:
//not allowed due to troops in village are calculated

                }

                //refresh time
                TroopsManager.getSingleton().getTroopsForVillage(row).setState(Calendar.getInstance().getTime());
                fireTableDataChanged();

            }


        }
    }
}
