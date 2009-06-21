/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @TODO (DIFF) Fix to new troop structure
 * @TODO (DIFF) If troops exist only losses are substracted
 * @author Jejkal
 */
public class ReportParser {
    /*
    Angreifer:	Ken Follett
    Dorf:	092 Werl (442|458) K44
    
    Anzahl:	0	0	0	0	200	0	0	0	0	0	0	0
    Verluste:	0	0	0	0	10	0	0	0	0	0	0	0
    
    
    Verteidiger:	king bushido 95
    Dorf:	KönigsPalast [001] (440|450) K44
    
    Anzahl:	0	555	18887	0	79	0	0	0	0	61	0	1
    Verluste:	0	0	10000	0	0	0	0	0	0	0	0	0
     */

    public static boolean parse(String pTroopsString) {
        StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r");
        int villageLines = -1;
        boolean retValue = false;
        //boolean haveVillage = false;
        Village v = null;
        List<Integer> troops = new LinkedList<Integer>();
        List<Integer> troopsLost = new LinkedList<Integer>();
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            //tokenize line by tab and space
            StringTokenizer elemTok = new StringTokenizer(line, " \t");
            if (line.trim().startsWith("Dorf:")) {
                while (elemTok.hasMoreElements()) {
                    String currentToken = elemTok.nextToken();
                    //search village
                    if (currentToken.startsWith("(") && currentToken.endsWith(")")) {
                        //check if we got a village

                        String coord = currentToken.trim().substring(currentToken.lastIndexOf("(") + 1, currentToken.lastIndexOf(")"));
                        if (ServerSettings.getSingleton().getCoordType() != 2) {
                            String[] split = coord.trim().split("[(\\:)]");
                            int[] xy = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                            v = DataHolder.getSingleton().getVillages()[xy[0]][xy[1]];
                            villageLines = 2;
                            break;
                        } else {
                            String[] split = coord.trim().split("[(\\|)]");
                            v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
                            villageLines = 2;
                            break;
                        }


                    /*   if (currentToken.matches("\\([0-9]+\\|[0-9]+\\)")) {
                    //extract village coordinates
                    String[] split = currentToken.trim().split("[(\\|)]");
                    v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[1])][Integer.parseInt(split[2])];
                    //next 4 lines are village
                    villageLines = 2;
                    break;
                    }*/
                    }
                }
            } else {
                //parse single line for village
                if (v != null) {
                    //parse 2 village lines!
                    line = line.trim();
                    if (line.trim().startsWith("Anzahl:")) {
                        for (int i : parseUnits(line.replaceAll("Anzahl:", "").trim())) {
                            //own units in village
                            troops.add(i);
                        }
                        villageLines--;
                    } else if (line.trim().startsWith("Verluste:")) {
                        int cnt = 0;
                        for (int i : parseUnits(line.replaceAll("Verluste:", "").trim())) {
                            //lost troops
                            troopsLost.add(i);
                            cnt++;
                        }
                        villageLines--;
                    }
                }
            }
            if (villageLines == 0) {
                //add troops to manager
                if ((v != null) && (troops.size() == DataHolder.getSingleton().getUnits().size())) {
                    // TroopsManager.getSingleton().addTroopsForVillage(v, new LinkedList<Integer>(troops));

                    VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                    if (holder == null) {
                        //set troops for village exclusive lost troops
                        int cnt = 0;
                        Hashtable<UnitHolder, Integer> troopsTable = new Hashtable<UnitHolder, Integer>();
                        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                            troopsTable.put(u, troops.get(cnt) - troopsLost.get(cnt));
                            cnt++;
                        }
                        TroopsManager.getSingleton().addTroopsForVillage(v, new LinkedList<Integer>(troops));
                        holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                        holder.setTroopsInVillage(troopsTable);
                    } else {
                        Hashtable<UnitHolder, Integer> inVillage = (Hashtable<UnitHolder, Integer>) holder.getTroopsInVillage().clone();
                        int cnt = 0;
                        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                            int lost = troopsLost.get(cnt);
                            int remain = inVillage.get(unit) - lost;
                            if (remain < 0) {
                                //remaining smaller 0 -> add min value
                                inVillage.put(unit, 0);
                            } else {
                                //put remaining troops
                                inVillage.put(unit, remain);
                            }
                            cnt++;
                        }
                        //put table back to holder
                        holder.setTroopsInVillage(inVillage);
                    }

                    troops.clear();
                    v = null;
                    //found at least one village, so retValue is true    
                    retValue = true;
                }
            }
        }
        if (retValue) {
            TroopsManager.getSingleton().forceUpdate();
        }
        return retValue;
    }

    private static int[] parseUnits(String pLine) {
        String line = pLine.replaceAll("eigene", "").replaceAll("Befehle", "").replaceAll("Truppen", "");
        StringTokenizer t = new StringTokenizer(line, " \t");
        int uCount = t.countTokens();
        int[] units = new int[uCount];
        int cnt = 0;
        while (t.hasMoreTokens()) {
            units[cnt] = Integer.parseInt(t.nextToken());
            cnt++;
        }
        return units;
    }

    public static void main(String[] args) {
        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            ReportParser.parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
