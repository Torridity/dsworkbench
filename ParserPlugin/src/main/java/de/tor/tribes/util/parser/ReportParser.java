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
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.NotifierFrame;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.report.ReportManager;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 * @author Torridity
 */
public class ReportParser implements SilentParserInterface {

    private static Logger logger = Logger.getLogger("ReportParser");

    public boolean parse(String pData) {
        try {
            FightReport r = parseReport(pData);

            if (!r.isValid()) {
                throw new Exception("No valid report data found");
            }
            ReportManager.getSingleton().addManagedElement(r);
            try {
                DSWorkbenchMainFrame.getSingleton().showSuccess("DS Workbench hat einen Kampfbericht erfolgreich eingelesen und in das Berichtset 'default' übertragen.");
            } catch (Exception e) {
                NotifierFrame.doNotification("DS Workbench hat einen Kampfbericht erfolgreich eingelesen und in das Berichtset 'default' übertragen.", NotifierFrame.NOTIFY_INFO);
            }
            return true;
        } catch (Exception e) {
            //no valid report data found
            logger.info("No valid report data found.", e);
        }
        return false;
    }

    private static void debug(String pMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(pMessage);
        }
    }

    private static FightReport parseReport(String pData) {
        StringTokenizer t = new StringTokenizer(pData, "\n");
        boolean luckPart = false;
        boolean attackerPart = false;
        boolean defenderPart = false;
        boolean troopsOnTheWayPart = false;
        boolean troopsOutside = false;
        boolean haveTime = false;

        boolean searchChurch = ServerSettings.getSingleton().isChurch();
        int serverTroopCount = DataHolder.getSingleton().getUnits().size();
        FightReport result = new FightReport();
        String winString = null;
        while (t.hasMoreTokens()) {
            String line = t.nextToken().trim();
            debug("Line: " + line);
            if (line.startsWith("Kampfzeit")) {
                debug("Found send line");
                line = line.replaceAll("Kampfzeit", "").trim();
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                try {
                    Date d = f.parse(line);
                    result.setTimestamp(d.getTime());
                    debug("  Sent: " + f.format(new Date(result.getTimestamp())));
                    haveTime = true;
                } catch (Exception e) {
                    result.setTimestamp(0l);
                    debug(" Failed to parse sent");
                }
            } else if (line.contains("hat gewonnen")) {
                debug("Found 'won' line: " + line);
                winString = line;
            } else if (line.startsWith("ausgekundschaftet")) {
                debug("Found 'spied' line: " + line);
                winString = line;
            } else if (line.startsWith("Angreiferglück")) {
                debug("Found luck line");
                line = line.replaceAll("Angreiferglück", "").trim();
                if (line.indexOf("%") > 0) {
                    //negative luck is in same line, try it!
                    try {
                        double luck = Double.parseDouble(line.replaceAll("Glück", "").replaceAll("%", "").trim());
                        result.setLuck(luck);
                        debug(" Luck: " + luck);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        result.setLuck(0.0);
                        debug(" Failed to parse luck");
                    }
                    luckPart = false;
                } else {
                    //probably positive luck, handle with next line
                    luckPart = true;
                }
            } else if (line.startsWith("Angreiferglück")) {
                debug("Found luck part");
                luckPart = true;
            } else if (line.startsWith("Moral")) {
                debug("Found moral line");
                line = line.replaceAll("Moral:", "").trim().replaceAll("%", "");
                if (line.contains("Angreifer")) {
                    //Opera only -.-
                    debug(" Special moral handling (Opera)");
                    attackerPart = true;
                    int attackerPos = line.indexOf("Angreifer");
                    String attacker = line.substring(attackerPos).replaceAll("Angreifer:", "").trim();
                    debug(" Found attacker: " + attacker);
                    result.setAttacker(DataHolder.getSingleton().getTribeByName(attacker));
                    line = line.substring(0, attackerPos);
                }
                try {
                    double moral = Double.parseDouble(line);
                    debug(" Set moral to " + moral);
                    result.setMoral(moral);
                } catch (Exception e) {
                }
            } else if (line.startsWith("Angreifer") || line.indexOf("Angreifer") > -1) {
                attackerPart = true;
                line = line.replaceAll("Angreifer:", "").trim();
                debug("Found attacker in normal mode: " + line);
                result.setAttacker(DataHolder.getSingleton().getTribeByName(line));
            } else if (line.startsWith("Dorf") || line.startsWith("Herkunft") || line.startsWith("Ziel")) {
                line = line.replaceAll("Dorf:", "").replaceAll("Herkunft:", "").replaceAll("Ziel:", "").trim();
                debug("Found village: " + line);
                if (attackerPart) {
                    debug(" Use village as source");
                    result.setSourceVillage(new VillageParser().parse(line).get(0));
                } else if (defenderPart) {
                    debug(" Use village as target");
                    result.setTargetVillage(new VillageParser().parse(line).get(0));
                }
            } else if (line.startsWith("Anzahl")) {
                line = line.replaceAll("Anzahl:", "").trim();
                debug("Found amount line: '" + line + "'");
                if (attackerPart) {
                    debug(" Use amount for attacker");
                    String[] troops = line.split("\t");
                    //hack for militia servers (militia is not shown for attacker)
                    if (troops.length == serverTroopCount || troops.length == serverTroopCount - 1) {
                        result.setAttackers(parseUnits(troops));
                    }
                } else if (defenderPart) {
                    debug(" Use amount for defender");
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        result.setDefenders(parseUnits(troops));
                    }
                }
            } else if (line.startsWith("Verluste")) {
                line = line.replaceAll("Verluste:", "").trim();
                debug("Found losses line: '" + line + "'");
                if (attackerPart) {
                    debug(" Use losses for attacker");
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount || troops.length == serverTroopCount - 1) {
                        //hack for militia servers (militia is not shown for attacker)
                        debug(" Line seems valid");
                        result.setDiedAttackers(parseUnits(troops));
                        attackerPart = false;
                    } else {
                        debug(" Line seems NOT valid (" + troops.length + " != " + serverTroopCount + ")");
                    }
                } else if (defenderPart) {
                    debug(" Use losses for defender");
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        debug(" Line seems valid");
                        result.setDiedDefenders(parseUnits(troops));
                    } else {
                        debug(" Line seems NOT valid (" + troops.length + " != " + serverTroopCount + ")");
                    }

                }
            } else if (line.startsWith("Verteidiger")) {
                defenderPart = true;
                line = line.replaceAll("Verteidiger:", "").trim();
                debug("Found defender line: '" + line + "'");
                result.setDefender(DataHolder.getSingleton().getTribeByName(line));
            } else if (line.indexOf("Erspähte Rohstoffe:") > -1) {
                debug("Found spyed resources");
                String resources = line.substring(line.lastIndexOf(":") + 1).trim();
                String[] spyedResources = resources.split(" ");
                try {
                    int wood = Integer.parseInt(spyedResources[0].replaceAll("\\.", ""));
                    int clay = Integer.parseInt(spyedResources[1].replaceAll("\\.", ""));
                    int iron = Integer.parseInt(spyedResources[2].replaceAll("\\.", ""));
                    result.setSpyedResources(wood, clay, iron);
                    debug("Successfully set spyed resources to " + wood + "/" + clay + "/" + iron);
                } catch (Exception e) {
                    debug("Failed to set spyed resources from " + resources);
                    //no spyed resources
                }
            } else if (line.contains("Beute:")) {
                debug("Found haul");
                String haul = line.substring(line.lastIndexOf(":") + 1).trim();
                String[] hauledResource = haul.split(" ");
                try {
                    int wood = 0;
                    int clay = 0;
                    int iron = 0;
                    for (int i = 0; i < hauledResource.length; i++) {
                        if (i == 0) {
                            try {
                                wood = Integer.parseInt(hauledResource[0].replaceAll("\\.", ""));
                            } catch (NumberFormatException nfe) {
                                break;
                            }
                        } else if (i == 1) {
                            try {
                                clay = Integer.parseInt(hauledResource[1].replaceAll("\\.", ""));
                            } catch (NumberFormatException nfe) {
                                break;
                            }
                        } else if (i == 2) {
                            try {
                                iron = Integer.parseInt(hauledResource[2].replaceAll("\\.", ""));
                            } catch (NumberFormatException nfe) {
                                break;
                            }
                        }
                    }
                    result.setHaul(wood, clay, iron);
                    debug("Successfully set haul to " + wood + "/" + clay + "/" + iron);
                } catch (Exception e) {
                    debug("Failed to set haul from " + haul);
                    //no haul
                }
            } else if (line.contains("Holzfäller")) {
                debug("Parse wood mine");
                //int val = parseIntFromPattern(line, "Holzfäller(.*)\\(Stufe(.*?)\\)", 2);
                int val = parseIntFromPattern(line, "Holzfäller(.*)(.*?)", 2);
                if (val != -1) {
                    debug("Got wood mine level " + val);
                    result.setWoodLevel(val);
                } else {
                    debug("No valid wood mine level from " + line);
                }
            } else if (line.contains("Lehmgrube")) {
                debug("Parse clay mine");
                //int val = parseIntFromPattern(line, "Lehmgrube(.*)\\(Stufe(.*?)\\)", 2);
                int val = parseIntFromPattern(line, "Lehmgrube(.*)(.*?)", 2);
                if (val != -1) {
                    debug("Got clay mine level " + val);
                    result.setClayLevel(val);
                } else {
                    debug("No valid clay mine level from " + line);
                }
            } else if (line.contains("Eisenmine")) {
                debug("Parse clay mine");
                // int val = parseIntFromPattern(line, "Eisenmine(.*)\\(Stufe(.*?)\\)", 2);
                int val = parseIntFromPattern(line, "Eisenmine(.*)(.*?)", 2);
                if (val != -1) {
                    debug("Got iron mine level " + val);
                    result.setIronLevel(val);
                } else {
                    debug("No valid iron mine level from " + line);
                }
            } else if (line.contains("Speicher")) {
                debug("Parse storage");
                // int val = parseIntFromPattern(line, "Speicher(.*)\\(Stufe(.*?)\\)", 2);
                int val = parseIntFromPattern(line, "Speicher(.*)(.*?)", 2);
                if (val != -1) {
                    debug("Got storage level " + val);
                    result.setStorageLevel(val);
                } else {
                    debug("No valid storage level from " + line);
                }
            } else if (line.contains("Versteck")) {
                debug("Parse hide");
                //int val = parseIntFromPattern(line, "Versteck(.*)\\(Stufe(.*?)\\)", 2);
                int val = parseIntFromPattern(line, "Versteck(.*)(.*?)", 2);
                if (val != -1) {
                    debug("Got hide level " + val);
                    result.setHideLevel(val);
                } else {
                    debug("No valid hide level from " + line);
                }
            } else if (line.contains("Wall")) {
                debug("Parse wall");
                //int val = parseIntFromPattern(line, "Wall(.*)\\(Stufe(.*?)\\)", 2);
                int val = parseIntFromPattern(line, "Wall(.*)(.*?)", 2);
                if (val != -1) {
                    debug("Got wall level " + val);
                    result.setWallLevel(val);
                } else {
                    debug("No valid wall level from " + line);
                }
            } else if (searchChurch && line.contains("Erste Kirche")) {
                debug("Try adding first church");
                try {
                    ChurchManager.getSingleton().addChurch(result.getTargetVillage(), 6);
                } catch (Exception e) {
                    debug("Failed to add first church");
                }
            } else if (searchChurch && line.contains("Kirche")) {
                debug("Parse church");
                //int val = parseIntFromPattern(line, "Kirche(.*)\\(Stufe(.*?)\\)", 2);
                int val = parseIntFromPattern(line, "Kirche(.*)(.*?)", 2);
                switch (val) {
                    case 1:
                        ChurchManager.getSingleton().addChurch(result.getTargetVillage(), 4);
                        debug("Church level 1 added");
                        break;
                    case 2:
                        ChurchManager.getSingleton().addChurch(result.getTargetVillage(), 6);
                        debug("Church level 2 added");
                        break;
                    case 3:
                        ChurchManager.getSingleton().addChurch(result.getTargetVillage(), 8);
                        debug("Church level 3 added");
                        break;
                    default:
                        debug("Failed to add curch");
                }
            } else if (line.startsWith("Schaden durch Rammböcke")) {
                line = line.replaceAll("Schaden durch Rammböcke:", "").trim();
                line = line.replaceAll("Wall beschädigt von Level", "").trim().replaceAll("auf Level", "");
                debug("Found wall line");
                StringTokenizer wallT = new StringTokenizer(line, " \t");
                try {
                    result.setWallBefore(Byte.parseByte(wallT.nextToken()));
                    result.setWallAfter(Byte.parseByte(wallT.nextToken()));
                } catch (Exception e) {
                    result.setWallBefore((byte) -1);
                    result.setWallAfter((byte) -1);
                }
            } else if (line.startsWith("Schaden durch Katapultbeschuss")) {
                //Schaden durch Katapultbeschuss: Wall beschädigt von Level 8 auf Level 7
                line = line.replaceAll("Schaden durch Katapultbeschuss:", "").trim().replaceAll("Level", "");
                debug("Found cata line");
                //Wall beschädigt von 8 auf 7
                StringTokenizer cataT = new StringTokenizer(line, " ");
                String target = cataT.nextToken();
                //"damaged" token
                cataT.nextToken();
                //"from" token
                cataT.nextToken();
                try {
                    byte buildingBefore = Byte.parseByte(cataT.nextToken());
                    //"to" token
                    cataT.nextToken();
                    byte buildingAfter = Byte.parseByte(cataT.nextToken());
                    result.setAimedBuilding(target);
                    result.setBuildingBefore(buildingBefore);
                    result.setBuildingAfter(buildingAfter);
                } catch (Exception e) {
                }
            } else if (line.startsWith("Veränderung der Zustimmung") || line.startsWith("Zustimmung:")) {
                line = line.replaceAll("Veränderung der Zustimmung", "").trim().replaceAll("Zustimmung gesunken von", "").replaceAll("auf", "");
                debug("Found acceptance line");

                //version 6.0
                line = line.replaceAll("Zustimmung:", "").replaceAll("Gesunken von", "");
                StringTokenizer acceptT = new StringTokenizer(line, " \t");
                try {
                    result.setAcceptanceBefore(Byte.parseByte(acceptT.nextToken()));
                    result.setAcceptanceAfter(Byte.parseByte(acceptT.nextToken()));
                } catch (Exception e) {
                    result.setAcceptanceBefore((byte) 100);
                    result.setAcceptanceAfter((byte) 100);
                }
            } else if (line.startsWith("Truppen des Verteidigers, die unterwegs waren")) {
                troopsOnTheWayPart = true;
                debug("Found troops on the way line");
            } else if (line.startsWith("Truppen des Verteidigers in anderen Dörfern")) {
                troopsOutside = true;
                debug("Found troops outside line");
            } else if (line.startsWith("Durch Besitzer des Berichts verborgen")) {
                if (attackerPart) {
                    String[] unknownAttackers = new String[serverTroopCount];
                    for (int i = 0; i < serverTroopCount; i++) {
                        unknownAttackers[i] = "-1";
                    }
                    result.setAttackers(parseUnits(unknownAttackers));
                    result.setDiedAttackers(parseUnits(unknownAttackers));
                    attackerPart = false;
                } else {
                    String[] unknownDefender = new String[serverTroopCount];
                    for (int i = 0; i < serverTroopCount; i++) {
                        unknownDefender[i] = "-1";
                    }
                    result.setDefenders(parseUnits(unknownDefender));
                    result.setDiedDefenders(parseUnits(unknownDefender));
                    defenderPart = false;
                }
            } else if (line.startsWith("Keiner deiner Kämpfer ist lebend zurückgekehrt")) {
                debug("Found full destruction line");
                defenderPart = false;
                String[] unknownDefender = new String[serverTroopCount];
                for (int i = 0; i < serverTroopCount; i++) {
                    unknownDefender[i] = "-1";
                }
                result.setDefenders(parseUnits(unknownDefender));
                result.setDiedDefenders(parseUnits(unknownDefender));
            } else {
                if (!haveTime) {
                    debug("Parse sent date");
                    line = line.trim();
                    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                    try {
                        Date d = f.parse(line);
                        result.setTimestamp(d.getTime());
                        haveTime = true;
                    } catch (Exception e) {
                        result.setTimestamp(0l);
                    }
                }

                if (troopsOnTheWayPart) {
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        troopsOnTheWayPart = false;
                        result.setDefendersOnTheWay(parseUnits(troops));
                    }
                } else if (troopsOutside) {
                    try {
                        Village v = new VillageParser().parse(line).get(0);
                        if (v == null) {
                            throw new Exception();
                        }
                        line = line.substring(line.indexOf("\t")).trim();
                        String[] troops = line.split("\t");
                        if (troops.length == serverTroopCount) {
                            result.addDefendersOutside(v, parseUnits(troops));
                        }
                    } catch (Exception e) {
                        //no additional troops outside
                        troopsOutside = false;
                    }
                } else if (luckPart) {
                    if (line.indexOf("%") > 0) {
                        luckPart = false;
                        try {
                            // System.out.println("LuckLine " + line);
                            double luck = Double.parseDouble(line.replaceAll("Pech", "").replaceAll("%", "").trim());
                            //System.out.println("L " + luck);
                            result.setLuck(luck);
                        } catch (Exception e) {
                            result.setLuck(0.0);
                        }
                    }
                }
            }
        }
        Tribe attacker = result.getAttacker();
        Tribe defender = result.getDefender();
        if (winString == null) {
            debug("No winString found. Leaving isWon as it is.");
        } else {
            if (attacker != null && defender != null) {
                if (winString.contains("gewonnen")) {
                    result.setWon(winString.contains(attacker.getName()));
                    debug("'won' set to " + result.isWon() + " due to winString " + winString + " and attacker " + attacker.getName());
                } else if (winString.contains("ausgekundschaftet")) {
                    result.setWon(winString.indexOf(attacker.getName()) < winString.indexOf(defender.getName()));
                    debug("'won' set to " + result.isWon() + " due to winString " + winString + " and attacker " + attacker.getName());
                }
            } else {
                debug("Either attacker or defender is null. Leaving isWon as it is.");
            }
        }
        return result;
    }

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

    public static int parseIntFromPattern(String pLine, String pPattern, int pGroupIndex) {
        try {
            String val = pLine.replaceAll(pPattern, "$" + pGroupIndex).trim();
            return Integer.parseInt(val);
        } catch (Exception e) {
            return -1;
        }
    }

    public static void main(String[] args) throws Exception {

    //  ReportParser.parseReport();
        /*
         * String test = "1\t2\t3\t4\t5"; String[] split = test.split("\t"); for(String t : split){ System.out.println(t); }
         */
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) -  %m%n")));
        GlobalOptions.setSelectedServer("de85");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de85")[0]);
        DataHolder.getSingleton().loadData(false); // GlobalOptions.loadUserData(); 
        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        System.out.println(new VillageParser().parse("OMIX-A0001 (280|661) K62"));
        System.out.println(new ReportParser().parse(data));
        //System.out.println(Integer.parseInt("4.344".replaceAll("\\.", "")));
    }
}
