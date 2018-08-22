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
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.TroopsManager.TROOP_TYPE;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Charon
 */
public class NonPAPlaceParser implements SilentParserInterface {

    public boolean parse(String pTroopsString) {
        StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r");
        //boolean haveVillage = false;
        Village v = null;
        TroopAmountFixed ownTroops = null;
        TroopAmountFixed troopsInVillage = new TroopAmountFixed(0);
        HashMap<Village, TroopAmountFixed> supportsToThis = new HashMap<>();
        HashMap<Village, TroopAmountFixed> supportsFromThis = new HashMap<>();
        while (lineTok.hasMoreElements()) {
            String currentLine = lineTok.nextToken();
            //walk through all lines
            if (v == null) {
                //try to get current village
                try {
                    v = VillageParser.parseSingleLine(currentLine);
                } catch (Exception e) {
                    v = null;
                }
            } else {
                //have current village, find troops
                if (currentLine.trim().startsWith(getVariable("troops.place.from.village"))) {
                    //get own troops from this village
                    ownTroops = parseUnits(currentLine);
                    //get troops from other villages till "Insgesamt" is reached
                    while (true) {
                        currentLine = lineTok.nextToken();
                        if (currentLine.trim().startsWith(getVariable("troops.place.overall"))) {
                            //get all troops in village
                            troopsInVillage = parseUnits(currentLine);
                            //leave while loop
                            break;
                        } else {
                            //get troops from other villages
                            Village supportingVillage = VillageParser.parseSingleLine(currentLine);
                            if (supportingVillage != null) {
                                TroopAmountFixed support = parseUnits(currentLine);
                                supportsToThis.put(supportingVillage, support);
                            } else {
                                //'troops outside' line or invalid village info
                            }
                        }
                        if (!lineTok.hasMoreTokens()) {
                            //wrong information
                            return false;
                        }
                    }
                } else if (currentLine.trim().startsWith(getVariable("troops.place.in.other.villages"))) {
                    while (true) {
                        currentLine = lineTok.nextToken();
                        //get troops in other village
                        Village supportedVillage = VillageParser.parseSingleLine(currentLine);

                        if (supportedVillage != null) {
                            TroopAmountFixed outside = parseUnits(currentLine);
                            supportsFromThis.put(supportedVillage, outside);
                        }

                        if (!lineTok.hasMoreTokens()) {
                            break;
                        }
                    }
                }
            }
        }

        if (ownTroops == null) {
            //no troops found!?
            return false;
        }

        if (v != null) {
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
            HashMap<UnitHolder, Integer> troopsOutside = holder.getTroopsOutside();
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
                    HashMap<UnitHolder, Integer> supportTroops = supportsToThis.get(supporter);
                    HashMap<UnitHolder, Integer> troops2Outside = holder2.getTroopsOutside();
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
                    HashMap<UnitHolder, Integer> supportTroops = supportsFromThis.get(supported);
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
        TroopsManager.getSingleton().revalidate(true);
        return true;
    }

    private TroopAmountFixed parseUnits(String pLine) {
        String line = pLine.replaceAll(getVariable("troops.place.from.village"), "").replaceAll(getVariable("troops.place.overall"), "");
        StringTokenizer t = new StringTokenizer(line, " \t");
        TroopAmountFixed units = new TroopAmountFixed(0);

        List<UnitHolder> allUnits = DataHolder.getSingleton().getUnits();
        int cnt = 0;
        while (t.hasMoreTokens()) {
            try {
                units.setAmountForUnit(allUnits.get(cnt), Integer.parseInt(t.nextToken()));
                cnt++;
            } catch (Exception e) {
                //token with no troops, set counter to 0 if village name contained valid number
                cnt = 0;
            }
        }
        return units;
    }

    private String getVariable(String pProperty) {
        return ParserVariableManager.getSingleton().getProperty(pProperty);
    }
    

    public static void main(String[] args) {

        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            /*String s = " 003 | Spitfire (471|482) K44\n" +
            "eigene    2500    1500    0    1964    500    0    0    1396    0    0    0    0    Befehle\n" +
            "im Dorf    2500    1500    0    1964    500    0    0    1396    0    0    0    0    Truppen\n" +
            "auswärts    0    0    0    0    0    0    0    0    0    0    0    0\n" +
            "unterwegs    0    0    0    0    0    0    0    0    0    0    0    0    Befehle\n" +
            "2Fast4You (475|480) K44\n" +
            "eigene    600    500    0    0    134    0    0    354    0    0    0    1    Befehle\n" +
            "im Dorf    600    500    0    0    134    0    0    354    0    0    0    1    Truppen\n" +
            "auswärts    4400    3000    0    3000    66    0    0    1046    0    0    0    0\n" +
            "unterwegs    0    0    0    0    0    0    0    0    0    0    0    0    Befehle\n";
             */

            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            new NonPAPlaceParser().parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
