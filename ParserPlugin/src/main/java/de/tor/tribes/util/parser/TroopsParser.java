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
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.NotifierFrame;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Charon
 */
public class TroopsParser implements SilentParserInterface {

    /*
     * 003 | Spitfire (471|482) K44 eigene    2500    1500    0    1964    500    0    0    1396    0    0    0    0    Befehle im Dorf    2500    1500    0    1964    500    0    0    1396    0    0    0    0
     * Truppen auswärts    0    0    0    0    0    0    0    0    0    0    0    0 unterwegs    0    0    0    0    0    0    0    0    0    0    0    0    Befehle
     */
    public boolean parse(String pTroopsString) {
        StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r");
        int villageLines = -1;
        boolean retValue = false;
        int foundTroops = 0;
        //boolean haveVillage = false;
        Village v = null;
        TroopAmountFixed ownTroops = null;
        TroopAmountFixed troopsInVillage = null;
        TroopAmountFixed troopsOutside = null;
        TroopAmountFixed troopsOnTheWay = null;
        TroopsManager.getSingleton().invalidate();
        // used to update group on the fly, if not "all" selected
        String groupName = null;
        // groups could be multiple lines, detection is easiest for first line (starts with "Gruppen:")
        boolean groupLines = false;
        // store visited villages, so we can add em to selected group
        List<Village> villages = new LinkedList<>();
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            //tokenize line by tab and space
            //  StringTokenizer elemTok = new StringTokenizer(line, " \t");
            //parse single line for village
            if (v != null) {
                //parse 4 village lines!
                line = line.trim();
                if (line.trim().startsWith(getVariable("troops.own"))) {
                    ownTroops = parseUnits(line.replaceAll(getVariable("troops.own"), "").trim());
                } else if (line.trim().startsWith(getVariable("troops.in.village"))) {
                    troopsInVillage = parseUnits(line.replaceAll(getVariable("troops.in.village"), "").trim());
                } else if (line.trim().startsWith(getVariable("troops.outside"))) {
                    troopsOutside = parseUnits(line.replaceAll(getVariable("troops.outside"), "").trim());
                } else if (line.trim().startsWith(getVariable("troops.on.the.way"))) {
                    troopsOnTheWay = parseUnits(line.replaceAll(getVariable("troops.on.the.way"), "").trim());
                }
                villageLines--;
            } else {
                try {
                    Village current = VillageParser.parseSingleLine(line);
                    if (current != null) {
                        v = current;
                        villageLines = 4;
                        // we are not searching for further group names
                        groupLines = false;
                        // add village to list of villages in selected group
                        if(groupName != null)villages.add(v);                        
                    } else {
                        // Check if current line is first group line. In case it is, store selected group
                        if(line.trim().startsWith(getVariable("overview.groups")))
                            groupLines = true;                    
                        // Check if current line contains active group. In case it does, store group name and stop searching
                        if(groupLines && line.matches(".*>.*?<.*")){
                            groupLines = false;
                            groupName = StringUtils.substringBetween(line, ">", "<"); // = line.replaceAll(".*>|<.*",""); if we stop using Apache Commons            
                        }                        
                    }
                } catch (Exception e) {
                    v = null;
                    villageLines = 0;
                    // Check if current line is first group line. In case it is, store selected group
                    if(line.trim().startsWith(getVariable("overview.groups")))
                        groupLines = true;                    
                    // Check if current line contains active group. In case it does, store group name and stop searching
                    if(groupLines && line.matches(".*>.*?<.*")){
                        groupLines = false;
                        groupName = StringUtils.substringBetween(line, ">", "<"); // = line.replaceAll(".*>|<.*",""); if we stop using Apache Commons            
                    }
                }
            }
            //add troops information
            if (villageLines == 0) {
                if (v != null && ownTroops != null && troopsInVillage != null
                        && troopsOutside != null && troopsOnTheWay != null) {
                    //add troops to manager
                    VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN, true);
                    VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.IN_VILLAGE, true);
                    VillageTroopsHolder outside = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OUTWARDS, true);
                    VillageTroopsHolder onTheWay = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.ON_THE_WAY, true);

                    own.setTroops(ownTroops);
                    inVillage.setTroops(troopsInVillage);
                    outside.setTroops(troopsOutside);
                    onTheWay.setTroops(troopsOnTheWay);
                    ownTroops = null;
                    troopsInVillage = null;
                    troopsOutside = null;
                    troopsOnTheWay = null;
                    v = null;
                    foundTroops++;
                    //found at least one village, so retValue is true    
                    retValue = true;
                } else {
                    v = null;
                    ownTroops = null;
                    troopsInVillage = null;
                    troopsOutside = null;
                    troopsOnTheWay = null;
                }
            }
        }
        if (retValue) {
            try {
                DSWorkbenchMainFrame.getSingleton().showSuccess("DS Workbench hat Truppeninformationen zu " + foundTroops + ((foundTroops == 1) ? " Dorf " : " Dörfern ") + " in die Truppenübersicht eingetragen.");
            } catch (Exception e) {
                NotifierFrame.doNotification("DS Workbench hat Truppeninformationen zu " + foundTroops + ((foundTroops == 1) ? " Dorf " : " Dörfern ") + " in die Truppenübersicht eingetragen.", NotifierFrame.NOTIFY_INFO);
            }
        }
        
        //update selected group, if any
        if(groupName != null && !groupName.equals(getVariable("groups.all"))){
            Hashtable<String, List<Village>> groupTable = new Hashtable<>();
            groupTable.put(groupName, villages);
            DSWorkbenchMainFrame.getSingleton().fireGroupParserEvent(groupTable);
        }
        
        TroopsManager.getSingleton().revalidate(retValue);
        return retValue;
    }

    private TroopAmountFixed parseUnits(String pLine) {
        String line = pLine.replaceAll(getVariable("troops.own"), "").replaceAll(getVariable("troops.commands"), "").replaceAll(getVariable("troops"), "");
        StringTokenizer t = new StringTokenizer(line, " \t");
        TroopAmountFixed units = new TroopAmountFixed(0);
        List<UnitHolder> allUnits = DataHolder.getSingleton().getUnits();
        int cnt = 0;
        while (t.hasMoreTokens()) {
            try {
                units.setAmountForUnit(allUnits.get(cnt), Integer.parseInt(t.nextToken()));
                cnt++;
            } catch (Exception e) {
                //token with no troops
            }
        }
        return units;
    }

    private String getVariable(String pProperty) {
        return ParserVariableManager.getSingleton().getProperty(pProperty);
    }
    

    public static void main(String[] args) {


        /*
         * Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null); try { String s = " 003 |
         * Spitfire (471|482) K44\n" + "eigene    2500    1500    0    1964    500    0    0    1396    0    0    0    0    Befehle\n" + "im Dorf    2500    1500    0    1964    500    0    0    1396    0    0
         * 0    0    Truppen\n" + "auswärts    0    0    0    0    0    0    0    0    0    0    0    0\n" + "unterwegs    0    0    0    0    0    0    0    0    0    0    0    0    Befehle\n" + "2Fast4You (475|480)
         * K44\n" + "eigene    600    500    0    0    134    0    0    354    0    0    0    1    Befehle\n" + "im Dorf    600    500    0    0    134    0    0    354    0    0    0    1    Truppen\n" + "auswärts
         * 4400    3000    0    3000    66    0    0    1046    0    0    0    0\n" + "unterwegs    0    0    0    0    0    0    0    0    0    0    0    0    Befehle\n";
         *
         *
         * String data = (String) t.getTransferData(DataFlavor.stringFlavor); TroopsParser.parse(data); } catch (Exception e) {
         * e.printStackTrace();
        }
         */

        String token = "(120|192)";
        System.out.println(token.matches("\\(*[0-9]{1,3}\\|[0-9]{1,3}\\)*"));
        token = "(12:23:12)";
        System.out.println(token.matches("\\(*[0-9]{1,2}\\:[0-9]{1,2}\\:[0-9]{1,2}\\)*"));


        // TroopsParser.parse(pTroopsString);
    }
    /*
     * kirscheye3    435|447 FaNtAsY wOrLd ... <3    Schwere Kavallerie    Torridity    436|444 FaNtAsY wOrLd ... 12    02.10.08 23:06:46 02.10.08 23:41:33
     * Torridity    437|445 FaNtAsY wOrLd ... 10    Schwere Kavallerie    Torridity    436|444 FaNtAsY wOrLd ... 12    02.10.08 23:26:00 02.10.08 23:41:33
     * Torridity    438|445 Barbarendorf (12)    Schwere Kavallerie    Torridity    436|444 FaNtAsY wOrLd ... 12    02.10.08 23:16:57 02.10.08 23:41:33
     * Torridity    439|445 Barbarendorf (13)    Schwere Kavallerie    Torridity    436|444 FaNtAsY wOrLd ... 12    02.10.08 23:06:46 02.10.08 23:41:33
     *
     */
    /*
     * LGK88 (1) (458|465) K44 eigene    0    0    6000    0    2300    0    300    50    0    Befehle im Dorf    0    0    6000    0    2300    0    300    50    0    0    Truppen auswärts    0    0    0    0    0    0    0
     * 0    0    0    0    0 unterwegs    0    0    0    0    0    0    0    0    0    0    0    0    Befehle
     */
}
