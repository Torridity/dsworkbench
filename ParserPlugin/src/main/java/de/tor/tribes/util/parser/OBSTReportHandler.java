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
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.NotifierFrame;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.RegExpHelper;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.village.KnownVillage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class OBSTReportHandler implements SilentParserInterface {

    private static Logger logger = Logger.getLogger("OBSTReportParser");

    @Override
    public boolean parse(String pData) {
        try {
            FightReport r = parseReport(pData);
            
            if (!r.isValid()) {
                throw new Exception("No valid report data found");
            }
            r.fillMissingSpyInformation();
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
    
    /*
    Betreff MKich (001) greift 44:81:25 TMendor AG (415|482) K44 an DS-OBST Status
    DS-OBST Berichteeinleser - Einstellungen Bitte eingeben URL: Gruppe: Taste Nick
    Passwort Speichern warte auf Befehl Kampfzeit 09.09.17 22:20:38 TMendor hat
    gewonnen Angreiferglück -22.4% Angreifer: MKich Herkunft: 001 (421|481) K44
    Anzahl: 0 0 0 0 0 0 1 0 0 Verluste: 0 0 0 0 0 0 1 0 0 Verteidiger: TMendor Ziel:
    44:81:25 TMendor AG (415|482) K44 Keiner deiner Kämpfer ist lebend zurückgekehrt.
    Es konnten keine Informationen über die Truppenstärke des Gegners erlangt werden.
    » Truppen in Simulator einfügen » Dieses Dorf angreifen » Mit gleichen Truppen
    noch einmal angreifen » Mit allen Truppen noch einmal angreifen » Diesen Bericht
    veröffentlichen 
    
    */
    private FightReport parseReport(String pData) {
        String data = pData;
        FightReport report = new FightReport();

        Matcher m = Pattern.compile(getVariable("report.moral") + ":\\s+([0-9]+)").matcher(data);
        if (m.find()) {
            try {
                report.setMoral(Integer.parseInt(m.group(1)));
            } catch (Exception e) {
                logger.error("Failed to get moral from entry " + m.group(1));
                report.setMoral(0.0);
            }
        } else {
            if(ServerSettings.getSingleton().getMoralType() == ServerSettings.NO_MORAL) {
                report.setMoral(1.0);
            } else {
                logger.error("No moral found. Using default value.");
                report.setMoral(0.0);
            }
        }

        m = Pattern.compile(getVariable("report.att.luck") + ".*\\s+([\\-0-9]*[0-9]+\\.[0-9]+)%\\s").matcher(data);
        if (m.find()) {
            try {
                report.setLuck(Double.parseDouble(m.group(1)));
            } catch (Exception e) {
                logger.error("Failed to get luck from entry " + m.group(1) + ". Setting default: 0.0");
                report.setLuck(0.0);
            }
        } else {
            logger.error("No luck found. Setting default: 0.0");
            report.setLuck(0.0);
        }

        m = Pattern.compile(getVariable("report.village.2") + ":\\s+(.*)\n").matcher(data);
        if (m.find()) {
            List<Village> source = new VillageParser().parse(m.group(1));
            if (source.isEmpty()) {
                logger.error("No source village found");
                return report;
            } else {
                report.setSourceVillage(source.get(0));
                report.setAttacker(report.getSourceVillage().getTribe());
            }
        } else {
            logger.error("No source village found");
        }

        m = Pattern.compile(getVariable("report.village.3") + ":\\s+(.*)\n").matcher(data);
        if (m.find()) {
            List<Village> target = new VillageParser().parse(m.group(1));
            if (target.isEmpty()) {
                logger.error("No target village found");
                return report;
            } else {
                report.setTargetVillage(target.get(0));
                report.setDefender(report.getTargetVillage().getTribe());
            }
        } else {
            logger.error("No target village found");
        }

        logger.debug("Checking for winner");
        m = Pattern.compile("(" + report.getAttacker().getName() + "|" +
                report.getDefender().getName() + ") " + getVariable("report.has.won")).matcher(data);
        if (m.find()) {
            String first = m.group(1);
            logger.debug("Winner string found: " + first);
            report.setWon(first.equals(report.getAttacker().getName()));
        } else {
            logger.debug("Winner string not found. Checking spy report.");
            m = Pattern.compile(report.getAttacker().getName() + " .* " +
                    report.getDefender().getName() + " " + getVariable("report.spy")).matcher(data);
            if (m.find()) {
                logger.debug("Successful spy report detected. Setting 'isWon' true.");
                report.setWon(true);
            } else {
                logger.debug("No successful spy report detected. Setting 'isWon' false.");
                report.setWon(false);
            }
        }
        
        m = Pattern.compile(getVariable("report.fight.time") + "(.*)" + "(" +
                report.getAttacker().getName() + "|" + report.getDefender().getName() + ")").matcher(data);
        if (m.find()) {
            try {
                //16.03.12 21:00:33
                String date = m.group(1).trim();
                Date sent = new SimpleDateFormat(getVariable("report.date.format")).parse(date);
                report.setTimestamp(sent.getTime());
            } catch (Exception e) {
                logger.warn("Failed to set report timestamp. Using 'NOW'");
                report.setTimestamp(System.currentTimeMillis());
            }
        } else {
            logger.debug("No report timestamp found");
            report.setTimestamp(System.currentTimeMillis());
        }

        boolean haveMilitia = !DataHolder.getSingleton().getUnitByPlainName("militia").equals(UnknownUnit.getSingleton());

        String unitPattern = RegExpHelper.getTroopsPattern(true, false);
        m = Pattern.compile(getVariable("report.num") + ":" + unitPattern).matcher(data);
        if (m.find()) {
            report.setAttackers(parseUnits(m.group(1).trim().split("\\s")));
            if (haveMilitia) {//switch to defender pattern
                unitPattern = RegExpHelper.getTroopsPattern(true, true);
                m = Pattern.compile(getVariable("report.num") + ":" + unitPattern).matcher(data);
            }
            if (m.find()) {
                report.setDefenders(parseUnits(m.group(1).trim().split("\\s")));
            } else {
                //no second "Amount:" ... lost everything
                Hashtable<UnitHolder, Integer> amounts = new Hashtable<>();
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                  amounts.put(unit, -1);
                }
                report.setDefenders(amounts);
            }
        }
        //back to offense pattern 
        unitPattern = RegExpHelper.getTroopsPattern(true, false);
        m = Pattern.compile(getVariable("report.loss") + ":" + unitPattern).matcher(data);
        if (m.find()) {
            report.setDiedAttackers(parseUnits(m.group(1).trim().split("\\s")));
            if (haveMilitia) {//and again, defense pattern
                unitPattern = RegExpHelper.getTroopsPattern(true, true);
                m = Pattern.compile(getVariable("report.loss") + ":" + unitPattern).matcher(data);
            }
            if (m.find()) {
                report.setDiedDefenders(parseUnits(m.group(1).trim().split("\\s")));
            } else {
                //no second "Losses:" ... lost everything
                Hashtable<UnitHolder, Integer> amounts = new Hashtable<>();
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    amounts.put(unit, -1);
                }
                report.setDiedDefenders(amounts);
            }
        }
        unitPattern = RegExpHelper.getTroopsPattern(false, false);
        //m = Pattern.compile("Truppen des Verteidigers, die unterwegs waren\n([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+)").matcher(data);
        m = Pattern.compile(getVariable("report.ontheway") + "\n" + unitPattern).matcher(data);
        if (m.find()) {
            report.setDefendersOnTheWay(parseUnits(m.group(1).trim().split("\\s")));
        } else {
            logger.info("No units on the way");
        }
        //in haul there are spaces in e.g. "3 . 400" ... replace thema first
        data = data.replaceAll(" \\. ", ".");
        m = Pattern.compile(getVariable("report.haul") + ":\\s+([\\.0-9]+)\\s([\\.0-9]+)\\s([\\.0-9]+)").matcher(data);

        if (m.find()) {
            try {
                int wood = 0;
                int clay = 0;
                int iron = 0;
                if (m.groupCount() == 1) {
                    //wood
                    wood = (int) Math.rint(Integer.parseInt(m.group(1).replaceAll("\\.", "")));
                } else if (m.groupCount() == 2) {
                    //wood and clay
                    wood = (int) Math.rint(Integer.parseInt(m.group(1).replaceAll("\\.", "")));
                    clay = (int) Math.rint(Integer.parseInt(m.group(2).replaceAll("\\.", "")));
                } else if (m.groupCount() == 3) {
                    //all
                    wood = (int) Math.rint(Integer.parseInt(m.group(1).replaceAll("\\.", "")));
                    clay = (int) Math.rint(Integer.parseInt(m.group(2).replaceAll("\\.", "")));
                    iron = (int) Math.rint(Integer.parseInt(m.group(3).replaceAll("\\.", "")));
                }
                report.setHaul(wood, clay, iron);
            } catch (Exception e) {
                logger.error("Failed to parse haul", e);
            }
        } else {
            //no haul information
            logger.info("No haul information found");
        }

        m = Pattern.compile(getVariable("report.spy.res") + ":\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)").matcher(data);
        if (m.find()) {
            try {
                int wood = 0;
                int clay = 0;
                int iron = 0;
                if (m.groupCount() == 1) {
                    //wood
                    wood = Integer.parseInt(m.group(1).replaceAll("\\.", ""));
                } else if (m.groupCount() == 2) {
                    //wood and clay
                    wood = Integer.parseInt(m.group(1).replaceAll("\\.", ""));
                    clay = Integer.parseInt(m.group(2).replaceAll("\\.", ""));
                } else if (m.groupCount() == 3) {
                    //all
                    wood = Integer.parseInt(m.group(1).replaceAll("\\.", ""));
                    clay = Integer.parseInt(m.group(2).replaceAll("\\.", ""));
                    iron = Integer.parseInt(m.group(3).replaceAll("\\.", ""));
                }
                report.setSpyedResources(wood, clay, iron);
            } catch (Exception e) {
                logger.error("Failed to parse spyed resources", e);
            }
        } else {
            //no spy information
            logger.info("No spy information found");
        }

        for(int i = 0; i < Constants.buildingNames.length; i++) {
            m = Pattern.compile(getVariable("report.buildings." +
                    Constants.buildingNames[i]) + "\\s+([0-9]+)").matcher(data);
            if (m.find()) {
                try {
                    report.setBuilding(i, Integer.parseInt(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to parse " + Constants.buildingNames[i] + " level from " + m.group(1));
                }
            } else {
                logger.info("No " + Constants.buildingNames[i] + " level information found");
            }
        }

        //m = Pattern.compile("Erste Kirche\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
        m = Pattern.compile(getVariable("report.buildings.first.church") + "\\s+([0-9]+)").matcher(data);
        if (m.find()) {
            report.setBuilding(KnownVillage.getBuildingIdByName("church"), 2);
        }

        m = Pattern.compile(getVariable("report.damage.ram") + ":\\s+"
                + getVariable("report.damage.wall") + "\\s+([0-9]+)\\s+"
                + getVariable("report.damage.to") + "\\s+([0-9]+)").matcher(data);
        if (m.find()) {
            try {
                report.setWallBefore(Byte.parseByte(m.group(1)));
                report.setWallAfter(Byte.parseByte(m.group(2)));
            } catch (Exception e) {
                logger.error("Failed to parse wall damage from " + m.group(1) + "/" + m.group(2));
            }
        } else {
            logger.info("No wall damage found");
        }

        m = Pattern.compile(getVariable("report.damage.kata") + ":\\s+(.*)\\s+" + 
                getVariable("report.damage.level") + "\\s+([0-9]+)\\s+" +
                getVariable("report.damage.to") + "\\s+([0-9]+)").matcher(data);
        if (m.find()) {
            try {
                report.setAimedBuildingId(getBuildingId(m.group(1)));
                report.setBuildingBefore(Byte.parseByte(m.group(2)));
                report.setBuildingAfter(Byte.parseByte(m.group(3)));
            } catch (Exception e) {
                logger.error("Failed to parse building damage from " + m.group(2) + "/" + m.group(3));
            }
        } else {
            logger.info("No building damage found");
        }

        m = Pattern.compile(getVariable("report.acceptance.1") + ":\\s+" +
                getVariable("report.acceptance.2") + "\\s+([0-9]+)\\s+" +
                getVariable("report.acceptance.3") + "\\s+(.*)\\s").matcher(data);
        if (m.find()) {
            try {
                report.setAcceptanceBefore(Byte.parseByte(m.group(1)));
                report.setAcceptanceAfter(Byte.parseByte(m.group(2).split(" ")[0]));
            } catch (Exception e) {
                logger.error("Failed to parse acceptance change from " + m.group(1) + "/" + m.group(2));
            }
        } else {
            logger.info("No acceptance change found");
        }
        
        return report;
    }

    private int getBuildingId(String translatedBuilding) {
        for(int i = 0; i < Constants.buildingNames.length; i++) {
            if(translatedBuilding.equals(getVariable("report.buildings." + Constants.buildingNames[i])))
                return i;
        }
        logger.error("Could not find Building " + translatedBuilding);
        return -1;
    }

    private Hashtable<UnitHolder, Integer> parseUnits(String[] pUnits) {
        int cnt = 0;
        Hashtable<UnitHolder, Integer> units = new Hashtable<>();
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
    
    private String getVariable(String pProperty) {
        return ParserVariableManager.getSingleton().getProperty(pProperty);
    }
}
