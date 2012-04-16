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
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class OBSTReportHandler {

    private static Logger logger = Logger.getLogger("OBSTReportParser");

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        Logger.getRootLogger().setLevel(Level.DEBUG);
        /*
         * String data = "KT-[1] Cithri (550|520) K55 eigene 0	0	375	20	7	0	0	0	0 Befehle\n" + "im Dorf 0	0	375	20	7	0	0	0	0 Truppen\n" +
         * "auswärts 0	0	0	0	0	0	0	0	0\n" + "unterwegs 0	0	0	0	70	0	0	0	0 Befehle\n" + "KT-[1] asdasd (550|520) K55 eigene 0	0	375	20	7	0	0
         * 0	0 Befehle\n" + "im Dorf 0	0	375	20	7	0	0	0	0 Truppen\n" + "auswärts 0	0	0	0	0	0	0	0	0\n" + "unterwegs 0	0	0	0	70	0	0	0	0
         * Befehle\n";
         *
         * Matcher m = Pattern.compile(".*\\s+\\(([0-9]{1,3})\\|([0-9]{1,3})\\)\\s+K[0-9]{1,2}\\s+eigene" +
         * "\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+).*\nim Dorf" +
         * "\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+).*\nauswärts" +
         * "\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+).*\nunterwegs" +
         * "\\s+([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+).*\n").matcher(data); while
         * (m.find()) { for (int i = 1; i <= m.groupCount(); i++) { System.out.println(m.group(i)); } }
         */


        /*
         * GlobalOptions.setSelectedServer("de81"); ProfileManager.getSingleton().loadProfiles();
         * GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de81")[0]);
         * DataHolder.getSingleton().loadData(false); GlobalOptions.loadUserData(); String report =
         * "report=Betreff%20Killer%20M%20(KT-%5B1%5D%20Helgrind)%20greift%20Barbarendorf%20(555%7C529)%20K55%20an%20DS-OBST%20Status%20DS-OBST%20Berichteeinleser%20-%20Einstellungen%20Bitte%20eingeben%20URL:%20Gruppe:%20Taste%20Nick%20Passwort%20Speichern%20warte%20auf%20Befehl%20Gesendet%2009.04.12%2004:47:13%20Der%20Angreifer%20hat%20gewonnen%20Gl%C3%BCck%20(aus%20Sicht%20des%20Angreifers)%20-19.8%25%20Moral:%20100%25%20Angreifer:%20Killer%20M%20%0AHerkunft:%207%20Opily%20(551%7C521)%20K55%20%0AAnzahl:%200%200%203280%200%201501%200%20170%2040%200%20Verluste:%200%200%200%200%200%200%200%200%200%20Glauben:%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%09%09%09%09%09Die%20Einheiten%20waren%20gl%C3%A4ubig.%0A%20%20%20%20%20%20%20%20%09%09%09%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20Kampfkraft:%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20100%25%20Verteidiger:%20---%20%0AZiel:%20Barbarendorf%20(555%7C529)%20K55%20%0AAnzahl:%200%200%200%200%200%200%200%200%200%20Verluste:%200%200%200%200%200%200%200%200%200%20Beute:%203%20.%20931%204%20.%20967%204%20.%20961%2013%20.%20859/152%20.%20880%20Schaden%20durch%20Rammb%C3%B6cke:%20Wall%20besch%C3%A4digt%20von%20Level%202%20auf%20Level%200%20Schaden%20durch%20Katapultbeschuss:%20Hauptgeb%C3%A4ude%20besch%C3%A4digt%20von%20Level%209%20auf%20Level%203%20%C2%BB%20Truppen%20in%20Simulator%20einf%C3%BCgen%20%C2%BB%20%C3%9Cberlebende%20Truppen%20in%20Simulator%20einf%C3%BCgen%20%C2%BB%20Diesen%20Bericht%20ver%C3%B6ffentlichen%20&user=Torridity&pass=cfcaef487fc66a6d8295e8e3f68b4db9&group=-1&world=81";
         * report = URLDecoder.decode(report, "UTF-8"); OBSTReportHandler.handleReport(report);
         */

        /*
         * String data = "Beute:%203%20.%20931%204%20.%20967%204%20.%20961%2013%20.%20859/152%20.%20880%20"; data = URLDecoder.decode(data,
         * "UTF-8"); data = data.replaceAll(" \\. ", "."); System.out.println(data); Matcher m =
         * Pattern.compile("Beute:\\s+([\\.0-9]+)\\s([\\.0-9]+)\\s([\\.0-9]+)").matcher(data); if (m.find()) { if (m.groupCount() == 3) {
         * //all System.out.println(m.group(1) + ", " + m.group(2) + ", " + m.group(3)); } }
         */
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
            //in haul there are spaces in e.g. "3 . 400" ... replace thema first
            data = data.replaceAll(" \\. ", ".");
            m = Pattern.compile("Beute:\\s+([\\.0-9]+)\\s([\\.0-9]+)\\s([\\.0-9]+)").matcher(data);

            if (m.find()) {
                try {
                    int wood = 0;
                    int clay = 0;
                    int iron = 0;
                    if (m.groupCount() == 1) {
                        //wood
                        wood = (int) Math.rint(Double.parseDouble(m.group(1)));
                    } else if (m.groupCount() == 2) {
                        //wood and clay
                        wood = (int) Math.rint(Double.parseDouble(m.group(1)));
                        clay = (int) Math.rint(Double.parseDouble(m.group(2)));
                    } else if (m.groupCount() == 3) {
                        //all
                        wood = (int) Math.rint(Double.parseDouble(m.group(1)));
                        clay = (int) Math.rint(Double.parseDouble(m.group(2)));
                        iron = (int) Math.rint(Double.parseDouble(m.group(3)));
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
