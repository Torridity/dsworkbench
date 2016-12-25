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
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.TroopsManager.TROOP_TYPE;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author Charon
 */
public class NonPAPlaceParser implements SilentParserInterface {

    public boolean parse(String pTroopsString) {
        StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r");
        //boolean haveVillage = false;
        Village v = null;
        Hashtable<UnitHolder, Integer> ownTroops = new Hashtable<UnitHolder, Integer>();
        Hashtable<UnitHolder, Integer> troopsInVillage = new Hashtable<UnitHolder, Integer>();
        Hashtable<Village, Hashtable<UnitHolder, Integer>> supportsToThis = new Hashtable<Village, Hashtable<UnitHolder, Integer>>();
        Hashtable<Village, Hashtable<UnitHolder, Integer>> supportsFromThis = new Hashtable<Village, Hashtable<UnitHolder, Integer>>();
        while (lineTok.hasMoreElements()) {
            String currentLine = lineTok.nextToken();
            //walk through all lines
            if (v == null) {
                //try to get current village
                try {
                    v = new VillageParser().parse(currentLine).get(0);
                } catch (Exception e) {
                    v = null;
                }
            } else {
                //have current village, find troops
                if (currentLine.trim().startsWith(ParserVariableManager.getSingleton().getProperty("troops.place.from.village"))) {
                    //get own troops from this village
                    int[] ownInVillage = parseUnits(currentLine);
                    int cnt = 0;
                    for (int i : ownInVillage) {
                        //all units in village
                        ownTroops.put(DataHolder.getSingleton().getUnits().get(cnt), i);
                        cnt++;
                    }
                    //get troops from other villages till "Insgesamt" is reached
                    while (true) {
                        currentLine = lineTok.nextToken();
                        if (currentLine.trim().startsWith(ParserVariableManager.getSingleton().getProperty("troops.place.overall"))) {
                            //get all troops in village
                            int[] allInVillage = parseUnits(currentLine);
                            cnt = 0;
                            for (int i : allInVillage) {
                                //all units in village
                                troopsInVillage.put(DataHolder.getSingleton().getUnits().get(cnt), i);
                                cnt++;
                            }
                            //leave while loop
                            break;
                        } else {
                            //get troops from other villages
                            Village supportingVillage = extractVillage(currentLine);
                            if (supportingVillage != null) {
                                int[] support = parseUnits(currentLine);
                                cnt = 0;
                                Hashtable<UnitHolder, Integer> supportTroops = new Hashtable<UnitHolder, Integer>();
                                for (int i : support) {
                                    //all units in village
                                    supportTroops.put(DataHolder.getSingleton().getUnits().get(cnt), i);
                                    cnt++;
                                }
                                supportsToThis.put(supportingVillage, supportTroops);
                            } else {
                                //'troops outside' line or invalid village info
                            }
                        }
                        if (!lineTok.hasMoreTokens()) {
                            //wrong information
                            return false;
                        }
                    }
                } else if (currentLine.trim().startsWith(ParserVariableManager.getSingleton().getProperty("troops.place.in.other.villages"))) {
                    while (true) {
                        currentLine = lineTok.nextToken();
                        //get troops in other village
                        Village supportedVillage = extractVillage(currentLine);

                        if (supportedVillage != null) {
                            int[] outside = parseUnits(currentLine);
                            int cnt = 0;
                            Hashtable<UnitHolder, Integer> supportTroops = new Hashtable<UnitHolder, Integer>();
                            for (int i : outside) {
                                //all units in village
                                supportTroops.put(DataHolder.getSingleton().getUnits().get(cnt), i);
                                cnt++;
                            }
                            supportsFromThis.put(supportedVillage, supportTroops);
                        }

                        if (!lineTok.hasMoreTokens()) {
                            break;
                        }
                    }
                }
            }
        }

        if (ownTroops.isEmpty()) {
            //no troops found!?
            return false;
        }

        if (v != null) {
            int troopsCount = DataHolder.getSingleton().getUnits().size();
            if ((v != null)
                    && (ownTroops.size() == troopsCount)
                    && (troopsInVillage.size() == troopsCount)) {
                TroopsManager.getSingleton().invalidate();
               // VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(v, TROOP_TYPE.OWN, true);
                if (own == null) {
                    own = new VillageTroopsHolder(v, new Date());
                    own.setTroops(ownTroops);
                    TroopsManager.getSingleton().addManagedElement(TroopsManager.OWN_GROUP, own);
                } else {
                    own.setState(new Date());
                    own.setTroops(ownTroops);
                }
                VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(v, TROOP_TYPE.IN_VILLAGE, true);
                if (inVillage == null) {
                    inVillage = new VillageTroopsHolder(v, new Date());
                    inVillage.setTroops(troopsInVillage);
                    TroopsManager.getSingleton().addManagedElement(TroopsManager.IN_VILLAGE_GROUP, inVillage);

                } else {
                    inVillage.setState(new Date());
                    inVillage.setTroops(troopsInVillage);
                }
                //@TODO handle supports
                /*  if (holder == null) {
                TroopsManager.getSingleton().addTroopsForVillageFast(v, new LinkedList<Integer>());
                holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                }
                //set current state
                holder.setState(Calendar.getInstance().getTime());
                holder.setOwnTroops(ownTroops);
                holder.setTroopsInVillage(troopsInVillage);*/


                //set supports
  /*              Enumeration<Village> supportsToThisKeys = supportsToThis.keys();
                Hashtable<UnitHolder, Integer> troopsOutside = holder.getTroopsOutside();
                if (troopsOutside.isEmpty()) {
                    for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                        troopsOutside.put(u, 0);
                    }
                }




                //adding all supports to  current village 'v'
                while (supportsToThisKeys.hasMoreElements()) {
                    //get supporting village
                    Village supporter = supportsToThisKeys.nextElement();
                    //get/create holder for supporting village
                    VillageTroopsHolder holder2 = TroopsManager.getSingleton().getTroopsForVillage(supporter);
                    if (holder2 == null) {
                        TroopsManager.getSingleton().addTroopsForVillageFast(supporter, new LinkedList<Integer>());
                        holder2 = TroopsManager.getSingleton().getTroopsForVillage(supporter);
                    }
                    //add current village as support target
                    if (holder2.addSupportTarget(v)) {
                        //add support only if support not already in
                        //correct outside troops for supporting village
                        Hashtable<UnitHolder, Integer> supportTroops = supportsToThis.get(supporter);
                        Hashtable<UnitHolder, Integer> troops2Outside = holder2.getTroopsOutside();
                        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                            troops2Outside.put(u, troops2Outside.get(u) + supportTroops.get(u));
                        }
                        //set troops outside for supporting village
                        holder2.setTroopsOutside(troops2Outside);
                        //add support for current village 'v'
                        holder.addSupport(supporter, supportTroops);
                        holder.updateSupportValues();
                    }
                }

                //set supporters
                Enumeration<Village> supportFromThisKeys = supportsFromThis.keys();
                //adding all supports from current village 'v'
                while (supportFromThisKeys.hasMoreElements()) {
                    //get supported village
                    Village supported = supportFromThisKeys.nextElement();
                    //check if supported village is already in list for village 'v' and add it if not
                    if (holder.addSupportTarget(supported)) {
                        //supported village is not yet in list, get/create troops holder for supported village
                        VillageTroopsHolder holder2 = TroopsManager.getSingleton().getTroopsForVillage(supported);
                        if (holder2 == null) {
                            TroopsManager.getSingleton().addTroopsForVillageFast(supported, new LinkedList<Integer>());
                            holder2 = TroopsManager.getSingleton().getTroopsForVillage(supported);
                        }
                        //get support units for supported village
                        Hashtable<UnitHolder, Integer> supportTroops = supportsFromThis.get(supported);
                        //add support from current village 'v' to supported village
                        holder2.addSupport(v, supportTroops);
                        //correct troops outside for current village 'v'
                        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                            troopsOutside.put(u, troopsOutside.get(u) + supportTroops.get(u));
                        }
                        holder2.updateSupportValues();
                    }
                    holder.setTroopsOutside(troopsOutside);
                }*/
            }
        }
        TroopsManager.getSingleton().revalidate(true);
        return true;
    }

    private static Village extractVillage(String pLine) {
        //tokenize line by tab and space
        StringTokenizer elemTok = new StringTokenizer(pLine, " \t");
        //try to find village line
        String nextToken = null;
        while (elemTok.hasMoreElements()) {
            String currentToken = null;
            if (nextToken == null) {
                currentToken = elemTok.nextToken();
            } else {
                currentToken = nextToken;
            }

            if (elemTok.hasMoreTokens()) {
                nextToken = elemTok.nextToken();
            }

//search village
            if (currentToken.startsWith("(") && currentToken.endsWith(")")) {
                //check if we got a village
                try {
                    String coord = currentToken.substring(currentToken.lastIndexOf("(") + 1, currentToken.lastIndexOf(")"));
                    if (ServerSettings.getSingleton().getCoordType() != 2) {
                        String[] split = coord.trim().split("[(\\:)]");
                        int[] xy = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                        return DataHolder.getSingleton().getVillages()[xy[0]][xy[1]];
                    } else {
                        String[] split = coord.trim().split("[(\\|)]");
                        return DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
                    }

                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    private static int[] parseUnits(String pLine) {
        String line = pLine.replaceAll(ParserVariableManager.getSingleton().getProperty("troops.place.from.village"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops.place.overall"), "");
        StringTokenizer t = new StringTokenizer(line, " \t");
        int uCount = DataHolder.getSingleton().getUnits().size();

        int[] units = new int[uCount];
        int cnt = 0;
        while (t.hasMoreTokens()) {
            try {
                units[cnt] = Integer.parseInt(t.nextToken());
                cnt++;
            } catch (Exception e) {
                //token with no troops, set counter to 0 if village name contained valid number
                cnt = 0;
            }
        }
        if (units.length < uCount) {
            return new int[]{};
        }

        return units;
    }

    public static void main(String[] args) {

        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            /*String s = " 003 | Spitfire (471|482) K44\n" +
            "eigene	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Befehle\n" +
            "im Dorf	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Truppen\n" +
            "auswärts	0	0	0	0	0	0	0	0	0	0	0	0\n" +
            "unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle\n" +
            "2Fast4You (475|480) K44\n" +
            "eigene	600	500	0	0	134	0	0	354	0	0	0	1	Befehle\n" +
            "im Dorf	600	500	0	0	134	0	0	354	0	0	0	1	Truppen\n" +
            "auswärts	4400	3000	0	3000	66	0	0	1046	0	0	0	0\n" +
            "unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle\n";
             */

            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            new NonPAPlaceParser().parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
