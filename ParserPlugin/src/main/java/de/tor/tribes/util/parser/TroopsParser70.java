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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.NotifierFrame;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;

/**
 * @author Charon
 */
public class TroopsParser70 implements SilentParserInterface {

    private static Logger logger = Logger.getLogger("TroopsParser70");
    private static boolean IS_DEBUG = false;

    private static Hashtable<UnitHolder, Integer> parseUnits(String[] pUnits) {
        int cnt = 0;
        Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (cnt < pUnits.length) {
                units.put(unit, Integer.parseInt(pUnits[cnt]));
            } else {
                units.put(unit, 0);
            }
            cnt++;
        }

        return units;
    }

    /*
     * 003 | Spitfire (472|480) K44 eigene	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Befehle im Dorf	2500	1500	0	1964	500	0	0	1396	0	0	0	0
     * Truppen auswärts	0	0	0	0	0	0	0	0	0	0	0	0 unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
     */

    /*
     * Barbarendorf (452|787) K74 eigene	0	0	5493	0	200	2449	257	0	300	30	0	1	Befehle im Dorf	15000	15000	5493	15000	950	2449	257	5000	300
     * 30	0	1	Truppen auswärts	0	0	0	0	0	0	0	0	0	0	0	0 unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
     *
     */
    public boolean parse(String pData) {
/*
        try {
            Matcher m = Pattern.compile(".*\\s+\\(([0-9]{1,3})\\|([0-9]{1,3})\\)\\s+K[0-9]{1,2}\\s+eigene"
                    + RegExpHelper.getTroopsPattern(true, true) + ".*\nim Dorf"
                    + RegExpHelper.getTroopsPattern(true, true) + ".*\nauswärts"
                    + RegExpHelper.getTroopsPattern(true, true) + ".*\nunterwegs"
                    + RegExpHelper.getTroopsPattern(true, true) + ".*\n").matcher(pData);
            int x = 0;
            int y = 0;
            String ownTroops = "";
            String troopsInVillage = "";
            String troopsOutside = "";
            String troopsOnTheWay = "";
            boolean foundOneEntry = false;
            while (m.find()) {
                foundOneEntry = true;
                for (int i = 1; i <= m.groupCount(); i++) {
                    x = (i == 1) ? Integer.parseInt(m.group(i)) : x;
                    y = (i == 2) ? Integer.parseInt(m.group(i)) : y;
                    ownTroops = (i == 3) ? m.group(i) : ownTroops;
                    troopsInVillage = (i == 4) ? m.group(i) : troopsInVillage;
                    troopsOutside = (i == 5) ? m.group(i) : troopsOutside;
                    troopsOnTheWay = (i == 6) ? m.group(i) : troopsOnTheWay;
                }


                Village v = DataHolder.getSingleton().getVillages()[x][y];
                if (v != null) {
                    VillageTroopsHolder h = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN, true);
                    String[] units = ownTroops.split("\\s");
                    h.setState(new Date(System.currentTimeMillis()));
                    h.setTroops(parseUnits(units));
                    h = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.IN_VILLAGE, true);
                    units = troopsInVillage.split("\\s");
                    h.setState(new Date(System.currentTimeMillis()));
                    h.setTroops(parseUnits(units));
                    h = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OUTWARDS, true);
                    units = troopsOutside.split("\\s");
                    h.setState(new Date(System.currentTimeMillis()));
                    h.setTroops(parseUnits(units));
                    h = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.ON_THE_WAY, true);
                    units = troopsOnTheWay.split("\\s");
                    h.setState(new Date(System.currentTimeMillis()));
                    h.setTroops(parseUnits(units));
                }
            }
            if (foundOneEntry) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

*/
        StringTokenizer lineTokenizer = new StringTokenizer(pData, "\n\r");
        List<String> lineList = new LinkedList<String>();

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
        List<Village> villages = new LinkedList<Village>();

        int foundTroops = 0;
        TroopsManager.getSingleton().invalidate();
        while (!lineList.isEmpty()) {
            String currentLine = lineList.remove(0);
            Village v = null;
            try {
                v = new VillageParser().parse(currentLine).get(0);
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
                if(currentLine.trim().startsWith(ParserVariableManager.getSingleton().getProperty("overview.groups")))
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
        if(groupName != null && !groupName.equals(ParserVariableManager.getSingleton().getProperty("groups.all"))){
        	Hashtable<String, List<Village>> groupTable = new Hashtable<String, List<Village>>();
        	groupTable.put(groupName, villages);
        	DSWorkbenchMainFrame.getSingleton().fireGroupParserEvent(groupTable);
        }
        
        return retValue;
    }

    private boolean processEntry(Village pVillage, String pCurrentLine, List<String> pLineStack) {
        String ownTroopsLine = pCurrentLine;
        String inVillageLine = null;
        String outsideLine = null;
        String onTheWayLine = null;
        try {
            inVillageLine = pLineStack.remove(0);
            outsideLine = pLineStack.remove(0);
            onTheWayLine = pLineStack.remove(0);
            debug("Processing village " + pVillage);

            int[] ownUnits = handleLine(ownTroopsLine, ParserVariableManager.getSingleton().getProperty("troops.own"));
            if (ownUnits == null) {
                throw new Exception("OwnTroops line is invalid");
            }
            int[] inVillageUnits = handleLine(inVillageLine, ParserVariableManager.getSingleton().getProperty("troops.in.village"));
            if (inVillageUnits == null) {
                throw new RuntimeException("InVillage line is invalid");
            }
            int[] outsideUnits = handleLine(outsideLine, ParserVariableManager.getSingleton().getProperty("troops.outside"));
            if (outsideUnits == null) {
                throw new RuntimeException("TroopsOutside line is invalid");
            }
            int[] onTheWayUnits = handleLine(onTheWayLine, ParserVariableManager.getSingleton().getProperty("troops.on.the.way"));
            if (onTheWayUnits == null) {
                throw new RuntimeException("TroopsOnTheWay line is invalid");
            }
            //add troops to troops manager
            if (!IS_DEBUG) {
                int cnt = 0;
                Hashtable<UnitHolder, Integer> ownTroops = new Hashtable<UnitHolder, Integer>();
                Hashtable<UnitHolder, Integer> troopsInVillage = new Hashtable<UnitHolder, Integer>();
                Hashtable<UnitHolder, Integer> troopsOutside = new Hashtable<UnitHolder, Integer>();
                Hashtable<UnitHolder, Integer> troopsOnTheWay = new Hashtable<UnitHolder, Integer>();
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    ownTroops.put(unit, ownUnits[cnt]);
                    troopsInVillage.put(unit, inVillageUnits[cnt]);
                    troopsOutside.put(unit, outsideUnits[cnt]);
                    troopsOnTheWay.put(unit, onTheWayUnits[cnt]);
                    cnt++;
                }
                /*
                 * TroopsManager.getSingleton().addTroopsForVillageFast(pVillage, new LinkedList<Integer>()); VillageTroopsHolder holder =
                 * TroopsManager.getSingleton().getTroopsForVillage(pVillage); holder.setOwnTroops(ownTroops);
                 * holder.setTroopsInVillage(troopsInVillage); holder.setTroopsOutside(troopsOutside);
                 * holder.setTroopsOnTheWay(troopsOnTheWay);
                 */

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

    private int[] handleLine(String pLine, String pTypeProperty) {
        try {
            debug("Test line '" + pLine + "' for property '" + pTypeProperty + "'");
            if (pLine.trim().indexOf(pTypeProperty) > -1) {
                debug("Handle line '" + pLine + "' for property '" + pTypeProperty + "'");
                int[] units = parseUnits(pLine.substring(pLine.indexOf(pTypeProperty)));
                if (units.length == 0) {
                    throw new RuntimeException("Line is invalid (UnitCount)");
                }
                debug("Got units " + units.length);
                return units;
            } else {
                throw new RuntimeException("Line is invalid (TypeProperty)");
            }
        } catch (Exception e) {
            debug(e);
        }
        return null;
    }

    private static void debug(Object pItem) {
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

    /*
     * public boolean parse1(String pTroopsString) { StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r"); int villageLines
     * = -1; boolean retValue = false; int foundTroops = 0; //boolean haveVillage = false; Village v = null; String line = null; //
     * List<Integer> troops = new LinkedList<Integer>(); Hashtable<UnitHolder, Integer> ownTroops = new Hashtable<UnitHolder, Integer>();
     * Hashtable<UnitHolder, Integer> troopsInVillage = new Hashtable<UnitHolder, Integer>(); Hashtable<UnitHolder, Integer> troopsOutside =
     * new Hashtable<UnitHolder, Integer>(); Hashtable<UnitHolder, Integer> troopsOnTheWay = new Hashtable<UnitHolder, Integer>(); while
     * (lineTok.hasMoreElements()) {
     *
     * //parse single line for village if (line == null) { line = lineTok.nextToken(); } //tokenize line by tab and space //
     * StringTokenizer elemTok = new StringTokenizer(line, " \t"); //parse single line for village if (v != null) { //parse 4 village lines!
     * line = line.trim(); if (line.trim().indexOf(ParserVariableManager.getSingleton().getProperty("troops.own")) > -1) { int cnt = 0; for
     * (int i :
     * parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.own"))).replaceAll(ParserVariableManager.getSingleton().getProperty("troops.own"),
     * "").trim())) { //own units in village //troops.add(i); ownTroops.put(DataHolder.getSingleton().getUnits().get(cnt), i); cnt++; } }
     * else if (line.trim().indexOf(ParserVariableManager.getSingleton().getProperty("troops.in.village")) > -1) { int cnt = 0; for (int i :
     * parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.in.village"))).replaceAll(ParserVariableManager.getSingleton().getProperty("troops.in.village"),
     * "").trim())) { //all units in village troopsInVillage.put(DataHolder.getSingleton().getUnits().get(cnt), i); cnt++; } } else if
     * (line.trim().indexOf(ParserVariableManager.getSingleton().getProperty("troops.outside")) > -1) { int cnt = 0; for (int i :
     * parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.outside"))).replaceAll(ParserVariableManager.getSingleton().getProperty("troops.outside"),
     * "").trim())) { //own units in other village troopsOutside.put(DataHolder.getSingleton().getUnits().get(cnt), i); cnt++; } } else if
     * (line.trim().indexOf(ParserVariableManager.getSingleton().getProperty("troops.on.the.way")) > -1) { // int[] underway =
     * parseUnits(line.replaceAll("unterwegs", "").trim()); int cnt = 0; //own units on the way for (int i :
     * parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.on.the.way"))).replaceAll(ParserVariableManager.getSingleton().getProperty("troops.on.the.way"),
     * "").trim())) { //troops.set(i, troops.get(i) + underway[i]); troopsOnTheWay.put(DataHolder.getSingleton().getUnits().get(cnt), i);
     * cnt++; } } villageLines--; line = null; } else { try { Village current = new VillageParser().parse(line).get(0); if (current != null)
     * { v = current; villageLines = 4; } } catch (Exception e) { v = null; villageLines = 0; line = null; } } //add troops information if
     * (villageLines == 0) { int troopsCount = DataHolder.getSingleton().getUnits().size();
     *
     * if ((v != null) && (ownTroops.size() == troopsCount) && (troopsInVillage.size() == troopsCount) && (troopsOutside.size() ==
     * troopsCount) && (troopsOnTheWay.size() == troopsCount)) { //add troops to manager /*
     * TroopsManager.getSingleton().addTroopsForVillageFast(v, new LinkedList<Integer>()); VillageTroopsHolder holder =
     * TroopsManager.getSingleton().getTroopsForVillage(v); holder.setOwnTroops(ownTroops); holder.setTroopsInVillage(troopsInVillage);
     * holder.setTroopsOutside(troopsOutside); holder.setTroopsOnTheWay(troopsOnTheWay);
     */
    /*
     * VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN, true);
     * VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.IN_VILLAGE, true);
     * VillageTroopsHolder outside = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OUTWARDS, true);
     * VillageTroopsHolder onTheWay = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.ON_THE_WAY, true);
     *
     *
     * own.setTroops(ownTroops); inVillage.setTroops(troopsInVillage); outside.setTroops(troopsOutside); onTheWay.setTroops(troopsOnTheWay);
     * //troops.clear(); ownTroops.clear(); troopsInVillage.clear(); troopsOutside.clear(); troopsOnTheWay.clear(); v = null; foundTroops++;
     * //found at least one village, so retValue is true retValue = true; } else { v = null; troopsInVillage.clear(); troopsOutside.clear();
     * troopsOnTheWay.clear(); // troops.clear(); } } } if (retValue) { NotifierFrame.doNotification("DS Workbench hat Truppeninformationen
     * zu " + foundTroops + ((foundTroops == 1) ? " Dorf " : " Dörfern ") + " in die Truppenübersicht eingetragen.",
     * NotifierFrame.NOTIFY_INFO); TroopsManager.getSingleton().forceUpdate(); } return retValue; }
     */
    private static int[] parseUnits(String pLine) throws RuntimeException {
        String line = pLine.replaceAll(ParserVariableManager.getSingleton().getProperty("troops.own"), "").
                replaceAll(ParserVariableManager.getSingleton().getProperty("troops.commands"), "").
                replaceAll(ParserVariableManager.getSingleton().getProperty("troops"), "").
                replaceAll(Pattern.quote("+"), "").trim();
        debug("Getting units from line '" + line + "'");
        StringTokenizer t = new StringTokenizer(line, " \t");
        int uCount = DataHolder.getSingleton().getUnits().size();

        if (IS_DEBUG) {
            uCount = 9;
        }
        int[] units = new int[uCount];
        int cnt = 0;
        while (t.hasMoreTokens()) {
            try {
                units[cnt] = Integer.parseInt(t.nextToken());
                cnt++;
            } catch (Exception e) {
                //token with no troops
            }
        }
        if (cnt < uCount) {
            throw new RuntimeException("Unit count does not match");
        }
        debug("Units: ");
        for (int u : units) {
            debug(u);
        }

        return units;
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
