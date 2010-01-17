/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.ui.models.ReportManagerTableModel;
import de.tor.tribes.util.parser.VillageParser;
import de.tor.tribes.util.report.ReportManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *
 * @author Torridity
 */
public class ReportParser {

    public static boolean parseReport(String pData) {
        try {
            FightReport r = parse(pData);
            if (!r.isValid()) {
                throw new Exception("No valid report data found");
            }
            /*  Document d = JaxenUtils.getDocument("<reports>"+r.toXml() + "</reports>");
            System.out.println("<reports>"+r.toXml() + "</reports>");
            System.out.println(JaxenUtils.getNodes(d, "//reports/report"));
            System.out.println(new FightReport((Element)JaxenUtils.getNodes(d, "//reports/report").get(0)));*/
            /* ReportManager.getSingleton().createReportSet("Test");
            ReportManager.getSingleton().getReportSet("Test").addReport(r);
            ReportManager.getSingleton().saveReportsToFile("reports.xml");
            ReportManager.getSingleton().loadReportsFromFile("reports.xml");
            for (FightReport re : ReportManager.getSingleton().getReportSet("Test").getReports()) {
            System.out.println(re);
            }*/
            String activeSet = ReportManagerTableModel.getSingleton().getActiveReportSet();
            ReportManager.getSingleton().getReportSet(activeSet).addReport(r);
            ReportManager.getSingleton().forceUpdate(activeSet);
            return true;
        } catch (Exception e) {
 //           e.printStackTrace();
        }
        return false;
    }

    private static FightReport parse(String pData) {
        StringTokenizer t = new StringTokenizer(pData, "\n");
        boolean luckPart = false;
        boolean attackerPart = false;
        boolean defenderPart = false;
        boolean troopsOnTheWayPart = false;
        int serverTroopCount = 12;
        FightReport result = new FightReport();
        while (t.hasMoreTokens()) {
            String line = t.nextToken();

            if (line.startsWith("Gesendet")) {
                line = line.replaceAll("Gesendet", "").trim();
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm");
                try {
                    Date d = f.parse(line);
                    result.setTimestamp(d.getTime());
                } catch (Exception e) {
                    result.setTimestamp(0l);
                }
            } else if (line.startsWith("Der Angreifer hat gewonnen")) {
                result.setWon(true);
            } else if (line.startsWith("Der Verteidiger hat gewonnen")) {
                result.setWon(false);
            } else if (line.startsWith("Glück")) {
                line = line.replaceAll("Glück \\(aus Sicht des Angreifers\\)", "").replaceAll("Glück \\(aus Sicht des Verteidigers\\)", "").trim();
                if (line.indexOf("%") > 0) {
                    //negative luck is in same line, try it!
                    try {

                        double luck = Double.parseDouble(line.trim().replaceAll("%", ""));
                        result.setLuck(luck);
                    } catch (Exception e) {
                        result.setLuck(0.0);
                    }
                    luckPart = false;
                } else {
                    //probably positive luck, handle with next line
                    luckPart = true;
                }
            } else if (line.startsWith("Glück (aus Sicht des Verteidigers)")) {
                luckPart = true;

            } else if (line.startsWith("Moral")) {
                line = line.replaceAll("Moral:", "").trim().replaceAll("%", "");
                try {
                    double moral = Double.parseDouble(line);
                    result.setMoral(moral);
                } catch (Exception e) {
                }
            } else if (line.startsWith("Angreifer")) {
                attackerPart = true;
                line = line.replaceAll("Angreifer:", "").trim();
                result.setAttacker(DataHolder.getSingleton().getTribeByName(line));
            } else if (line.startsWith("Dorf")) {
                line = line.replaceAll("Dorf:", "").trim();
                if (attackerPart) {
                    result.setSourceVillage(VillageParser.parse(line).get(0));
                } else if (defenderPart) {
                    result.setTargetVillage(VillageParser.parse(line).get(0));
                }
            } else if (line.startsWith("Anzahl")) {
                line = line.replaceAll("Anzahl:", "").trim();
                if (attackerPart) {
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        result.setAttackers(parseUnits(troops));
                    }
                } else if (defenderPart) {
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        result.setDefenders(parseUnits(troops));
                    }
                }
            } else if (line.startsWith("Verluste")) {
                line = line.replaceAll("Verluste:", "").trim();
                if (attackerPart) {
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        result.setDiedAttackers(parseUnits(troops));
                        attackerPart = false;
                    }
                } else if (defenderPart) {
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        result.setDiedDefenders(parseUnits(troops));
                    }
                }
            } else if (line.startsWith("Verteidiger")) {
                defenderPart = true;
                line = line.replaceAll("Verteidiger:", "").trim();
                result.setDefender(DataHolder.getSingleton().getTribeByName(line));
            } else if (line.startsWith("Schaden durch Rammböcke")) {
                line = line.replaceAll("Schaden durch Rammböcke:", "").trim();
                line = line.replaceAll("Wall beschädigt von Level", "").trim().replaceAll("auf Level", "");
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
            } else if (line.startsWith("Veränderung der Zustimmung")) {
                line = line.replaceAll("Veränderung der Zustimmung", "").trim().replaceAll("Zustimmung gesunken von", "").replaceAll("auf", "");
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
                    result.setDefenders(parseUnits(unknownDefender));
                    defenderPart = false;
                }
            } else if (line.startsWith("Keiner deiner Kämpfer ist lebend zurückgekehrt")) {
                defenderPart = false;
                String[] unknownDefender = new String[serverTroopCount];
                for (int i = 0; i < serverTroopCount; i++) {
                    unknownDefender[i] = "-1";
                }
                result.setDefenders(parseUnits(unknownDefender));
                result.setDiedDefenders(parseUnits(unknownDefender));
            } else {
                if (troopsOnTheWayPart) {
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        troopsOnTheWayPart = false;
                        result.setDefendersOutside(parseUnits(troops));
                    }
                } else if (luckPart) {
                    if (line.indexOf("%") > 0) {
                        luckPart = false;
                        try {
                            double luck = Double.parseDouble(line.trim().replaceAll("%", ""));
                            result.setLuck(luck);
                        } catch (Exception e) {
                            result.setLuck(0.0);
                        }
                    }
                }
            }
        }

        return result;
    }

    private static Hashtable<UnitHolder, Integer> parseUnits(String[] pUnits) {
        int cnt = 0;
        Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            units.put(unit, Integer.parseInt(pUnits[cnt]));
            cnt++;

        }


        return units;
    }

    public static void main(String[] args) {
        //  ReportParser.parseReport();
        /*  String test = "1\t2\t3\t4\t5";
        String[] split = test.split("\t");
        for(String t : split){
        System.out.println(t);
        }*/
        String line = "Glück (aus Sicht des Angreifers)14.2%";
        System.out.println(line.replaceAll("Glück \\(aus Sicht des Angreifers\\)", ""));
    }
}
