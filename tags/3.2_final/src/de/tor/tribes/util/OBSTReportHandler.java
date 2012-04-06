/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.report.ReportManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
public class OBSTReportHandler {

    private static Logger logger = Logger.getLogger("OBSTReportParser");

    public static void main(String[] args) {

        String data = "KT-[1] Cithri (550|520) K55 	eigene 	0	0	375	20	7	0	0	0	0 	Befehle\n"
                + "im Dorf 	0	0	375	20	7	0	0	0	0 	Truppen\n"
                + "auswärts 	0	0	0	0	0	0	0	0	0\n"
                + "unterwegs 	0	0	0	0	70	0	0	0	0 	Befehle\n"
                + "KT-[1] asdasd (550|520) K55 	eigene 	0	0	375	20	7	0	0	0	0 	Befehle\n"
                + "im Dorf 	0	0	375	20	7	0	0	0	0 	Truppen\n"
                + "auswärts 	0	0	0	0	0	0	0	0	0\n"
                + "unterwegs 	0	0	0	0	70	0	0	0	0 	Befehle\n";

        Matcher m = Pattern.compile(".*\\s+\\(([0-9]{1,3})\\|([0-9]{1,3})\\)\\s+K[0-9]{1,2}\\s+eigene"
                + "\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+).*\nim Dorf"
                + "\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+).*\nauswärts"
                + "\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+).*\nunterwegs"
                + "\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+).*\n").matcher(data);
        while (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                System.out.println(m.group(i));
            }
        }


    }

    public static boolean handleReport(String pData) {
        try {
            String data = pData;
            FightReport report = new FightReport();
            Matcher m = Pattern.compile("Gesendet\\s+([0-9]+)\\.([0-9]+)\\.([0-9]+)\\s+([0-9]+):([0-9]+):([0-9]+)").matcher(data);
            if (m.find()) {
                try {
                    //16.03.12 21:00:33
                    String date = m.group(1) + "." + m.group(2) + "." + m.group(3) + " " + m.group(4) + ":" + m.group(5) + ":" + m.group(6);
                    Date sent = new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(date);
                    report.setTimestamp(sent.getTime());
                } catch (Exception e) {
                    logger.warn("Failed to set report timestamp. Using 'NOW'");
                    report.setTimestamp(System.currentTimeMillis());
                }
            } else {
                m = Pattern.compile("Gesendet\\s+([0-9]+)\\.([0-9]+)\\.([0-9]+)\\s+([0-9]+):([0-9]+)").matcher(data);
                if (m.find()) {
                    try {
                        String date = m.group(1) + "." + m.group(2) + "." + m.group(3) + " " + m.group(4) + ":" + m.group(5);
                        Date sent = new SimpleDateFormat("dd.MM.yy HH:mm").parse(date);
                        report.setTimestamp(sent.getTime());
                    } catch (Exception e) {
                        logger.warn("Failed to set report timestamp. Using 'NOW'");
                        report.setTimestamp(System.currentTimeMillis());
                    }
                } else {
                    logger.debug("No report timestamp found");
                    report.setTimestamp(System.currentTimeMillis());
                }
            }

            m = Pattern.compile("Moral:\\s+([0-9]+)").matcher(data);
            if (m.find()) {
                try {
                    report.setMoral(Integer.parseInt(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to get moral from entry " + m.group(1));
                    return false;
                }
            } else {
                logger.error("No moral found");
                return false;
            }

            m = Pattern.compile("Der (Angreifer|Verteidiger) hat gewonnen").matcher(data);
            if (m.find()) {
                report.setWon(m.group(1).equals("Angreifer"));
            }

            m = Pattern.compile("Gl.{1,2}ck \\(aus Sicht des Angreifers\\).*\\s+([\\-0-9]*[0-9]+\\.[0-9]+)%\\s").matcher(data);
            if (m.find()) {
                try {
                    report.setLuck(Double.parseDouble(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to get luck from entry " + m.group(1));
                    return false;
                }
            } else {
                logger.error("No luck found");
                return false;
            }

            m = Pattern.compile("Herkunft:\\s+(.*)\n").matcher(data);
            if (m.find()) {
                List<Village> source = PluginManager.getSingleton().executeVillageParser(m.group(1));
                if (source.isEmpty()) {
                    logger.error("No source village found");
                    return false;
                } else {
                    report.setSourceVillage(source.get(0));
                    report.setAttacker(report.getSourceVillage().getTribe());
                }
            } else {
                logger.error("No source village found");
            }

            m = Pattern.compile("Ziel:\\s+(.*)\n").matcher(data);
            if (m.find()) {
                List<Village> target = PluginManager.getSingleton().executeVillageParser(m.group(1));
                if (target.isEmpty()) {
                    logger.error("No target village found");
                    return false;
                } else {
                    report.setTargetVillage(target.get(0));
                    report.setDefender(report.getTargetVillage().getTribe());
                }
            } else {
                logger.error("No target village found");
            }

            String unitPattern = RegExpHelper.getTroopsPattern(true, true);
            //m = Pattern.compile("Anzahl:\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+)").matcher(data);
            m = Pattern.compile("Anzahl:" + unitPattern).matcher(data);
            if (m.find()) {
                report.setAttackers(parseUnits(m.group(1).trim().split("\\s")));
                if (m.find()) {
                    report.setDefenders(parseUnits(m.group(1).trim().split("\\s")));
                } else {
                    //no second "Amount:" ... lost everything
                    Hashtable<UnitHolder, Integer> amounts = new Hashtable<UnitHolder, Integer>();
                    for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                        amounts.put(unit, -1);
                    }
                    report.setDefenders(amounts);
                }
            }
            //m = Pattern.compile("Verluste:\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+)").matcher(data);
            m = Pattern.compile("Verluste:" + unitPattern).matcher(data);
            if (m.find()) {
                report.setDiedAttackers(parseUnits(m.group(1).trim().split("\\s")));
                if (m.find()) {
                    report.setDiedDefenders(parseUnits(m.group(1).trim().split("\\s")));
                } else {
                    //no second "Losses:" ... lost everything
                    Hashtable<UnitHolder, Integer> amounts = new Hashtable<UnitHolder, Integer>();
                    for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                        amounts.put(unit, -1);
                    }
                    report.setDiedDefenders(amounts);
                }
            }
            unitPattern = RegExpHelper.getTroopsPattern(false, false);
            //m = Pattern.compile("Truppen des Verteidigers, die unterwegs waren\n([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+)").matcher(data);
            m = Pattern.compile("Truppen des Verteidigers, die unterwegs waren\n" + unitPattern).matcher(data);
            if (m.find()) {
                report.setDefendersOnTheWay(parseUnits(m.group(1).trim().split("\\s")));
            } else {
                logger.info("No units on the way");
            }

            m = Pattern.compile("Beute:\\s+([\\.0-9]+)\\s([\\.0-9]+)\\s([\\.0-9]+)").matcher(data);
            if (m.find()) {
                try {
                    int wood = 0;
                    int clay = 0;
                    int iron = 0;
                    if (m.groupCount() == 1) {
                        //wood
                        wood = Integer.parseInt(m.group(1));
                    } else if (m.groupCount() == 2) {
                        //wood and clay
                        wood = Integer.parseInt(m.group(1));
                        clay = Integer.parseInt(m.group(2));
                    } else if (m.groupCount() == 3) {
                        //all
                        wood = Integer.parseInt(m.group(1));
                        clay = Integer.parseInt(m.group(2));
                        iron = Integer.parseInt(m.group(3));
                    }
                    report.setHaul(wood, clay, iron);
                } catch (Exception e) {
                    logger.error("Failed to parse haul", e);
                }
            } else {
                //no haul information
                logger.info("No haul information found");
            }

            m = Pattern.compile("Ersp.{1,2}hte Rohstoffe:\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)").matcher(data);
            if (m.find()) {
                try {
                    int wood = 0;
                    int clay = 0;
                    int iron = 0;
                    if (m.groupCount() == 1) {
                        //wood
                        wood = Integer.parseInt(m.group(1));
                    } else if (m.groupCount() == 2) {
                        //wood and clay
                        wood = Integer.parseInt(m.group(1));
                        clay = Integer.parseInt(m.group(2));
                    } else if (m.groupCount() == 3) {
                        //all
                        wood = Integer.parseInt(m.group(1));
                        clay = Integer.parseInt(m.group(2));
                        iron = Integer.parseInt(m.group(3));
                    }
                    report.setSpyedResources(wood, clay, iron);
                } catch (Exception e) {
                    logger.error("Failed to parse spyed resources", e);
                }
            } else {
                //no spy information
                logger.info("No spy information found");
            }


            m = Pattern.compile("Holzfäller\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
            if (m.find()) {
                try {
                    report.setWoodLevel(Integer.parseInt(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to parse wood level from " + m.group(1));
                }
            } else {
                logger.info("No wood level information found");
            }

            m = Pattern.compile("Lehmgrube\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
            if (m.find()) {
                try {
                    report.setClayLevel(Integer.parseInt(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to parse clay level from " + m.group(1));
                }
            } else {
                logger.info("No clay level information found");
            }

            m = Pattern.compile("Eisenmine\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
            if (m.find()) {
                try {
                    report.setIronLevel(Integer.parseInt(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to parse iron level from " + m.group(1));
                }
            } else {
                logger.info("No iron level information found");
            }

            m = Pattern.compile("Speicher\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
            if (m.find()) {
                try {
                    report.setStorageLevel(Integer.parseInt(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to parse storage level from " + m.group(1));
                }
            } else {
                logger.info("No storage level information found");
            }

            m = Pattern.compile("Versteck\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
            if (m.find()) {
                try {
                    report.setHideLevel(Integer.parseInt(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to parse hide level from " + m.group(1));
                }
            } else {
                logger.info("No hide level information found");
            }

            m = Pattern.compile("Wall\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
            if (m.find()) {
                try {
                    report.setWallLevel(Integer.parseInt(m.group(1)));
                } catch (Exception e) {
                    logger.error("Failed to parse wall level from " + m.group(1));
                }
            } else {
                logger.info("No wall level information found");
            }

            m = Pattern.compile("Kirche\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
            if (m.find()) {
                try {
                    int level = Integer.parseInt(m.group(1));
                    switch (level) {
                        case 1:
                            ChurchManager.getSingleton().addChurch(report.getTargetVillage(), 4);
                            break;
                        case 2:
                            ChurchManager.getSingleton().addChurch(report.getTargetVillage(), 6);
                            break;
                        case 3:
                            ChurchManager.getSingleton().addChurch(report.getTargetVillage(), 8);
                            break;
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse church level from " + m.group(1));
                }
            } else {
                logger.info("No church level information found");
            }

            m = Pattern.compile("Erste Kirche\\s+\\(Stufe\\s+([0-9]+)\\)").matcher(data);
            if (m.find()) {
                ChurchManager.getSingleton().addChurch(report.getTargetVillage(), 6);
            }


            m = Pattern.compile("Schaden durch Rammböcke:\\s+Wall beschädigt von Level\\s+([0-9]+)\\s+auf Level\\s+([0-9]+)").matcher(data);
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

            m = Pattern.compile("Schaden durch Katapultbeschuss:\\s+(.*)\\s+beschädigt von Level\\s+([0-9]+)\\s+auf Level\\s+([0-9]+)").matcher(data);
            if (m.find()) {
                try {
                    report.setAimedBuilding(m.group(1));
                    report.setBuildingBefore(Byte.parseByte(m.group(2)));
                    report.setBuildingAfter(Byte.parseByte(m.group(3)));
                } catch (Exception e) {
                    logger.error("Failed to parse building damage from " + m.group(2) + "/" + m.group(3));
                }
            } else {
                logger.info("No building damage found");
            }

            m = Pattern.compile("Zustimmung:\\s+Gesunken von\\s+([0-9]+)\\s+auf\\s+(.*)\\s").matcher(data);
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

            if (report.isValid()) {
                ReportManager.getSingleton().addManagedElement(report);
                return true;
            }
            logger.error("Report is invalid");
            return false;
        } catch (Exception e) {
             logger.error("Failed to parse report", e);
            return false;
        }
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
}
