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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Charon
 */
public class TroopsParser70 implements SilentParserInterface {

    private static Logger logger = Logger.getLogger("TroopsParser70");
    private static final boolean IS_DEBUG = false;

    /*
     * 003 | Spitfire (472|480) K44 eigene	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Befehle im Dorf	2500	1500	0	1964	500	0	0	1396	0	0	0	0
     * Truppen auswärts	0	0	0	0	0	0	0	0	0	0	0	0 unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
     */

    /*
     * Barbarendorf (452|787) K74 eigene	0	0	5493	0	200	2449	257	0	300	30	0	1	Befehle im Dorf	15000	15000	5493	15000	950	2449	257	5000	300
     * 30	0	1	Truppen auswärts	0	0	0	0	0	0	0	0	0	0	0	0 unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
     *
     */
    @Override
    public boolean parse(String pData) {
        StringTokenizer lineTokenizer = new StringTokenizer(pData, "\n\r");
        List<String> lineList = new LinkedList<>();

        while (lineTokenizer.hasMoreElements()) {
            String line = lineTokenizer.nextToken();
            //"cheap snob rebuild linebreak"-hack
            if (line.trim().endsWith("+")) {
                line += lineTokenizer.nextToken();
            }
            debug("Push line to stack: " + line);
            lineList.add(line);
        }

        // used to update group on the fly, if not "all" selected
        String groupName = null;
        // groups could be multiple lines, detection is easiest for first line (starts with "Gruppen:")
        boolean groupLines = false;
        // store visited villages, so we can add em to selected group
        List<Village> villages = new LinkedList<>();

        int foundTroops = 0;
        TroopsManager.getSingleton().invalidate();
        while (!lineList.isEmpty()) {
            String currentLine = lineList.remove(0);
            Village v = null;
            try {
                v = VillageParser.parseSingleLine(currentLine);
            } catch (Exception e) {
                //no village in line
            }
            if (v != null) {
                if (processEntry(v, currentLine, lineList)) {
                    foundTroops++;
                    // add village to list of villages in selected group
                    if(groupName != null)villages.add(v);
                	groupLines = false; //should already be false. set to false again, to avoid searching for group name in garbage if user copied nonsense
                }
            } else {
                // Check if current line is first group line. In case it is, store selected group
                if(currentLine.trim().startsWith(getVariable("overview.groups")))
                	groupLines = true;
                // Check if current line contains active group. In case it does, store group name and stop searching
                if(groupLines && currentLine.matches(".*>.*?<.*")){
                	groupLines = false;
                	groupName = StringUtils.substringBetween(currentLine, ">", "<"); // = line.replaceAll(".*>|<.*",""); if we stop using Apache Commons   
                	debug("Found selected group in line '" + currentLine + "'");
                	debug("Selected group '"+groupName+"'");
                } else
                	debug("Dropping line '" + currentLine + "'");
            }
        }
        boolean retValue = (foundTroops != 0);
        if (retValue) {
            try {
                DSWorkbenchMainFrame.getSingleton().showSuccess("DS Workbench hat Truppeninformationen zu " + foundTroops + ((foundTroops == 1) ? " Dorf " : " Dörfern ") + " in die Truppenübersicht eingetragen.");
            } catch (Exception e) {
                NotifierFrame.doNotification("DS Workbench hat Truppeninformationen zu " + foundTroops + ((foundTroops == 1) ? " Dorf " : " Dörfern ") + " in die Truppenübersicht eingetragen.", NotifierFrame.NOTIFY_INFO);
            }
        }
        TroopsManager.getSingleton().revalidate(retValue);

        //update selected group, if any
        if(groupName != null && !groupName.equals(getVariable("groups.all"))){
        	Hashtable<String, List<Village>> groupTable = new Hashtable<>();
        	groupTable.put(groupName, villages);
        	DSWorkbenchMainFrame.getSingleton().fireGroupParserEvent(groupTable);
        }
        
        return retValue;
    }

    private boolean processEntry(Village pVillage, String pCurrentLine, List<String> pLineStack) {
        String inVillageLine = null;
        String outsideLine = null;
        String onTheWayLine = null;
        try {
            inVillageLine = pLineStack.remove(0);
            outsideLine = pLineStack.remove(0);
            onTheWayLine = pLineStack.remove(0);
            debug("Processing village " + pVillage);

            TroopAmountFixed ownTroops = handleLine(pCurrentLine, getVariable("troops.own"));
            if (ownTroops == null) {
                throw new Exception("OwnTroops line is invalid");
            }
            TroopAmountFixed troopsInVillage = handleLine(inVillageLine, getVariable("troops.in.village"));
            if (troopsInVillage == null) {
                throw new RuntimeException("InVillage line is invalid");
            }
            TroopAmountFixed troopsOutside = handleLine(outsideLine, getVariable("troops.outside"));
            if (troopsOutside == null) {
                throw new RuntimeException("TroopsOutside line is invalid");
            }
            TroopAmountFixed troopsOnTheWay = handleLine(onTheWayLine, getVariable("troops.on.the.way"));
            if (troopsOnTheWay == null) {
                throw new RuntimeException("TroopsOnTheWay line is invalid");
            }
            //add troops to troops manager
            if (!IS_DEBUG) {
                int cnt = 0;
                VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN, true);
                VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE, true);
                VillageTroopsHolder outside = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OUTWARDS, true);
                VillageTroopsHolder onTheWay = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.ON_THE_WAY, true);

                own.setTroops(ownTroops);
                inVillage.setTroops(troopsInVillage);
                outside.setTroops(troopsOutside);
                onTheWay.setTroops(troopsOnTheWay);
            } else {
                debug("Skip adding troops due to debug mode");
            }
            return true;
        } catch (Exception e) {
            debug(e);
            if (onTheWayLine != null) {
                debug("Pushing back TroopsOnTheWay line");
                pLineStack.add(0, onTheWayLine);
            }
            if (outsideLine != null) {
                debug("Pushing back TroopsOutside line");
                pLineStack.add(0, outsideLine);
            }
            if (inVillageLine != null) {
                debug("Pushing back TroopsInVillage line");
                pLineStack.add(0, inVillageLine);
            }
        }
        return false;
    }

    private TroopAmountFixed handleLine(String pLine, String pTypeProperty) {
        try {
            debug("Test line '" + pLine + "' for property '" + pTypeProperty + "'");
            if (pLine.trim().contains(pTypeProperty)) {
                debug("Handle line '" + pLine + "' for property '" + pTypeProperty + "'");
                TroopAmountFixed units = parseUnits(pLine.substring(pLine.indexOf(pTypeProperty)));
                if (!units.containsInformation()) {
                    throw new RuntimeException("Line is invalid (UnitCount)");
                }
                debug("Got units " + units.getContainedUnits(null).size());
                return units;
            } else {
                throw new RuntimeException("Line is invalid (TypeProperty)");
            }
        } catch (Exception e) {
            debug(e);
        }
        return null;
    }

    private void debug(Object pItem) {
        if (IS_DEBUG) {
            if (pItem != null) {
                System.out.println(pItem.toString());
            } else {
                System.out.println("Item is 'null'");
            }
        } else {
            logger.debug(pItem);
        }
    }
    
    private TroopAmountFixed parseUnits(String pLine) throws RuntimeException {
        String line = pLine.replaceAll(getVariable("troops.own"), "").
                replaceAll(getVariable("troops.commands"), "").
                replaceAll(getVariable("troops"), "").
                replaceAll(Pattern.quote("+"), "").trim();
        debug("Getting units from line '" + line + "'");
        StringTokenizer t = new StringTokenizer(line, " \t");
        int uCount = DataHolder.getSingleton().getUnits().size();
        List<UnitHolder> allUnits = DataHolder.getSingleton().getUnits();

        TroopAmountFixed units = new TroopAmountFixed(-1);
        int cnt = 0;
        while (t.hasMoreTokens()) {
            try {
                units.setAmountForUnit(allUnits.get(cnt), Integer.parseInt(t.nextToken()));
                cnt++;
            } catch (Exception e) {
                //token with no troops
            }
        }
        if (cnt < uCount) {
            throw new RuntimeException("Unit count does not match");
        }
        debug("Units: ");
        for (UnitHolder unit: allUnits) {
            debug(units.getAmountForUnit(unit));
        }

        return units;
    }

    private String getVariable(String pProperty) {
        return ParserVariableManager.getSingleton().getProperty(pProperty);
    }
    

    public static void main(String[] args) {
    	
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {

            /*
             * String data = "Rattennest (0|0) (486|833) K84 eigene 14 8 6490 0 181 2550 300 0 300 30 0 +3 + Befehle\n" + "im Dorf	15000
             * 15000	5493	15000	950	2449	257	5000	300	30	0	1	Truppen\n" + "auswärts	0	0	0	0	0	0	0	0	0	0	0	0\n" + "unterwegs	0	0	0	0	0	0	0	0
             * 0	0	0	0	Befehle\n";
             *
             */
            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            new TroopsParser70().parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }



        // TroopsParser.parse(pTroopsString);
    }
    /*
     * kirscheye3	435|447 FaNtAsY wOrLd ... <3	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:06:46 02.10.08 23:41:33
     * Torridity	437|445 FaNtAsY wOrLd ... 10	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:26:00 02.10.08 23:41:33
     * Torridity	438|445 Barbarendorf (12)	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:16:57 02.10.08 23:41:33
     * Torridity	439|445 Barbarendorf (13)	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:06:46 02.10.08 23:41:33
     *
     */
    /*
     * LGK88 (1) (458|465) K44 eigene	0	0	6000	0	2300	0	300	50	0	Befehle im Dorf	0	0	6000	0	2300	0	300	50	0	0	Truppen auswärts	0	0	0	0	0	0	0
     * 0	0	0	0	0 unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
     */
}
