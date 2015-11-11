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
package de.tor.tribes.util.html;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Village;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author Torridity
 */
public class FightReportHTMLToolTipGenerator {

    private final static String WINNER_STRING = "\\$WINNER_STRING";
    private final static String SEND_TIME = "\\$SEND_TIME";
    private final static String LUCK_STRING = "\\$LUCK_STRING";
    private final static String LUCK_BAR = "\\$LUCK_BAR";
    private final static String MORAL = "\\$MORAL";
    private final static String ATTACKER = "\\$ATTACKER";
    private final static String SOURCE = "\\$SOURCE";
    private final static String DEFENDER = "\\$DEFENDER";
    private final static String TARGET = "\\$TARGET";
    private final static String ATTACKER_TABLE = "\\$ATTACKER_TABLE";
    private final static String DEFENDER_TABLE = "\\$DEFENDER_TABLE";
    private final static String MISC_TABLES = "\\$MISC_TABLES";
    private final static String RAM_DAMAGE = "\\$RAM_DAMAGE";
    private final static String CATA_DAMAGE = "\\$CATA_DAMAGE";
    private final static String SNOB_INFLUENCE = "\\$SNOB_INFLUENCE";
    private final static String LUCK_NEG = "\\$LUCK_NEG";
    private final static String LUCK_POS = "\\$LUCK_POS";
    private final static String LUCK_ICON1 = "\\$LUCK_ICON1";
    private final static String LUCK_ICON2 = "\\$LUCK_ICON2";
    private static String pTemplateData = "";

    static {
        FileReader fr = null;
        try {
            fr = new FileReader(new File("templates/report.tmpl"));
            BufferedReader r = new BufferedReader(fr);
            String line = "";
            while ((line = r.readLine()) != null) {
                pTemplateData += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    public static String buildToolTip(FightReport pReport) {
        String res = pTemplateData;
        String[] tables = buildUnitTables(pReport);
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        res = res.replaceAll(WINNER_STRING, ((pReport.isWon() ? "Der Angreifer hat gewonnen" : "Der Verteidiger hat gewonnen")));
        res = res.replaceAll(SEND_TIME, f.format(new Date(pReport.getTimestamp())));
        res = res.replaceAll(ATTACKER_TABLE, tables[0]);
        res = res.replaceAll(DEFENDER_TABLE, tables[1]);
        res = res.replaceAll(MISC_TABLES, buildMiscTables(pReport));
        res = res.replaceAll(LUCK_STRING, "Gl&uuml;ck (aus Sicht des Angreifers)");
        res = res.replaceAll(LUCK_BAR, buildLuckBar(pReport.getLuck()));
        res = res.replaceAll(MORAL, nf.format(pReport.getMoral()) + "%");
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        res = res.replaceAll(LUCK_NEG, ((pReport.getLuck() < 0) ? "<b>" + nf.format(pReport.getLuck()) + "%</b>" : ""));
        res = res.replaceAll(LUCK_POS, ((pReport.getLuck() >= 0) ? "<b>" + nf.format(pReport.getLuck()) + "%</b>" : ""));
        res = res.replaceAll(LUCK_ICON1, "<img src=\"" + ((pReport.getLuck() <= 0) ? FightReportHTMLToolTipGenerator.class.getResource("/res/rabe.png") : FightReportHTMLToolTipGenerator.class.getResource("/res/rabe_grau.png")) + "\"/>");
        res = res.replaceAll(LUCK_ICON2, "<img src=\"" + ((pReport.getLuck() >= 0) ? FightReportHTMLToolTipGenerator.class.getResource("/res/klee.png") : FightReportHTMLToolTipGenerator.class.getResource("/res/klee_grau.png")) + "\"/>");
        res = res.replaceAll(ATTACKER, StringEscapeUtils.escapeHtml(pReport.getAttacker().getName()));
        res = res.replaceAll(SOURCE, StringEscapeUtils.escapeHtml(pReport.getSourceVillage().getFullName()));
        res = res.replaceAll(DEFENDER, StringEscapeUtils.escapeHtml(pReport.getDefender().getName()));
        res = res.replaceAll(TARGET, StringEscapeUtils.escapeHtml(pReport.getTargetVillage().getFullName()));
        res = res.replaceAll(RAM_DAMAGE, ((pReport.wasWallDamaged()) ? "Wall besch&auml;digt von Level <b>" + pReport.getWallBefore() + "</b> auf Level <b>" + pReport.getWallAfter() + "</b>" : ""));
        res = res.replaceAll(CATA_DAMAGE, ((pReport.wasBuildingDamaged()) ? pReport.getAimedBuilding() + " besch&auml;digt von Level <b>" + pReport.getBuildingBefore() + "</b> auf Level <b>" + pReport.getBuildingAfter() + "</b>" : ""));
        res = res.replaceAll(SNOB_INFLUENCE, ((pReport.wasSnobAttack()) ? "Zustimmung gesunken von <b>" + pReport.getAcceptanceBefore() + "</b> auf <b>" + pReport.getAcceptanceAfter() + "</b>" : ""));

        return res;
    }

    private static String[] buildUnitTables(FightReport pReport) {
        StringBuilder bAttacker = new StringBuilder();
        StringBuilder bDefender = new StringBuilder();
        bAttacker.append("<table width=\"100%\" style=\"border: solid 1px black; padding: 4px;background-color:#EFEBDF;\">");
        bDefender.append("<table width=\"100%\" style=\"border: solid 1px black; padding: 4px;background-color:#EFEBDF;\">");
        bAttacker.append("<tr>");
        bDefender.append("<tr>");

        String headerRow = "<td width=\"100\">&nbsp;</td>";
        String attackerAmountRow = "<tr><td width=\"100\"><div align=\"center\">Anzahl:</div></td>";
        String defenderAmountRow = "<tr><td width=\"100\"><div align=\"center\">Anzahl:</div></td>";
        String attackerLossRow = "<tr><td width=\"100\"><div align=\"center\">Verluste:</div></td>";
        String defenderLossRow = "<tr><td width=\"100\"><div align=\"center\">Verluste:</div></td>";
        String attackerSurviveRow = "<tr><td width=\"100\"><div align=\"center\">&Uuml;berlebende:</div></td>";
        String defenderSurviveRow = "<tr><td width=\"100\"><div align=\"center\">&Uuml;berlebende:</div></td>";
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            headerRow += "<td><img src=\"" + FightReportHTMLToolTipGenerator.class.getResource("/res/ui/" + unit.getPlainName() + ".png") + "\"</td>";
            int amount = pReport.getAttackers().get(unit);
            int died = pReport.getDiedAttackers().get(unit);
            if (amount == 0) {
                attackerAmountRow += "<td style=\"color:#DED3B9;\">" + amount + "</td>";
            } else {
                attackerAmountRow += "<td>" + amount + "</td>";
            }
            if (died == 0) {
                attackerLossRow += "<td style=\"color:#DED3B9;\">" + died + "</td>";
            } else {
                attackerLossRow += "<td>" + died + "</td>";
            }
            if (amount - died == 0) {
                attackerSurviveRow += "<td style=\"color:#DED3B9;\">" + (amount - died) + "</td>";
            } else {
                attackerSurviveRow += "<td>" + (amount - died) + "</td>";
            }
            amount = pReport.getDefenders().get(unit);
            died = pReport.getDiedDefenders().get(unit);

            if (amount == 0) {
                defenderAmountRow += "<td style=\"color:#DED3B9;\">" + amount + "</td>";
            } else {
                defenderAmountRow += "<td>" + amount + "</td>";
            }
            if (died == 0) {
                defenderLossRow += "<td style=\"color:#DED3B9;\">" + died + "</td>";
            } else {
                defenderLossRow += "<td>" + died + "</td>";
            }
            if (amount - died == 0) {
                defenderSurviveRow += "<td style=\"color:#DED3B9;\">" + (amount - died) + "</td>";
            } else {
                defenderSurviveRow += "<td>" + (amount - died) + "</td>";
            }
        }

        headerRow += "</tr>";
        attackerAmountRow += "</tr>";
        attackerLossRow += "</tr>";
        attackerSurviveRow += "</tr>";
        defenderAmountRow += "</tr>";
        defenderLossRow += "</tr>";
        defenderSurviveRow += "</tr>";

        bAttacker.append(headerRow);
        if (pReport.areAttackersHidden()) {
            bAttacker.append("<tr><td width=\"100\"><div align=\"center\">Anzahl:</div></td>");
            bAttacker.append("<td colspan=\"12\" rowspan=\"3\" ><div align=\"center\" valign=\"center\">Durch den Besitzer des Berichts verborgen</div></td></tr>");
            bAttacker.append("<tr><td width=\"100\"><div align=\"center\">Verluste:</div></td></tr>");
            bAttacker.append("<tr><td width=\"100\"><div align=\"center\">&Uuml;berlebende:</div></td></tr>");
        } else {
            bAttacker.append(attackerAmountRow);
            bAttacker.append(attackerLossRow);
            bAttacker.append(attackerSurviveRow);
        }
        bAttacker.append("</table>");

        if (pReport.wasLostEverything()) {
            bDefender.append("<tr><td width=\"100\"><div align=\"center\">Anzahl:</div></td>");
            bDefender.append("<td colspan=\"12\" rowspan=\"3\" ><div align=\"center\" valign=\"center\">Keiner deiner Kämpfer ist lebend zurückgekehrt.<BR/>Es konnten keine Informationen über die Truppenstärke des Gegners erlangt werden.</div></td></tr>");
            bDefender.append("<tr><td width=\"100\"><div align=\"center\">Verluste:</div></td></tr>");
            bDefender.append("<tr><td width=\"100\"><div align=\"center\">&Uuml;berlebende:</div></td></tr>");

        } else {
            bDefender.append(headerRow);
            bDefender.append(defenderAmountRow);
            bDefender.append(defenderLossRow);
            bDefender.append(defenderSurviveRow);
        }
        bDefender.append("</table>");

        return new String[]{bAttacker.toString(), bDefender.toString()};
    }

    private static String buildMiscTables(FightReport pReport) {
        String res = "";
        if (!pReport.wasConquered()) {
            res += "<tr>";
            res += "<td colspan=\"5\">&nbsp;</td>";
            res += "</tr>";
            return res;
        }

        String onTheWayTable = "<table width=\"100%\" style=\"border: solid 1px black; padding: 4px;background-color:#EFEBDF;\">";
        String outsideTable = "<table width=\"100%\" style=\"border: solid 1px black; padding: 4px;background-color:#EFEBDF;\">";
        onTheWayTable += "<tr>";
        outsideTable += "<tr>";


        String headerRow = "<tr>";
        String onTheWayRow = "<tr>";

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            headerRow += "<td style=\"background-color:#E1D5BE;\"><img src=\"" + FightReportHTMLToolTipGenerator.class.getResource("/res/ui/" + unit.getPlainName() + ".png") + "\"/></td>";

            int amount = 0;
            if (pReport.whereDefendersOnTheWay()) {
                amount = pReport.getDefendersOnTheWay().get(unit);
            }
            if (amount == 0) {
                onTheWayRow += "<td style=\"color:#DED3B9;\">" + amount + "</td>";
            } else {
                onTheWayRow += "<td>" + amount + "</td>";
            }
        }
        headerRow += "</tr>";
        onTheWayRow += "</tr>";

        onTheWayTable += headerRow;
        onTheWayTable += onTheWayRow;

        onTheWayTable += "</table>";
        res = "<tr>";
        res += "<td colspan=\"5\"><b>Truppen des Verteidigers, die unterwegs waren:</td>";
        res += "</tr>";
        res += "<tr>";
        res += "<td colspan=\"5\">";
        res += onTheWayTable;
        res += "</td>";
        res += "</tr>";
        String outsideRow = "";

        if (pReport.whereDefendersOutside()) {
            headerRow = "<tr style=\"background-color:#E1D5BE;\"><td width=\"100\">&nbsp;</td>";
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                headerRow += "<td><img src=\"" + FightReportHTMLToolTipGenerator.class.getResource("/res/ui/" + unit.getPlainName() + ".png") + "\"/></td>";
            }
            headerRow += "</tr>";

            Enumeration<Village> outside = pReport.getDefendersOutside().keys();
            while (outside.hasMoreElements()) {
                Village v = outside.nextElement();
                outsideRow += "<tr><td width=\"100\"><div align=\"center\">" + v.getFullName() + "</div></td>";

                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    int amount = pReport.getDefendersOutside().get(v).get(unit);
                    if (amount == 0) {
                        outsideRow += "<td style=\"color:#DED3B9;\">" + amount + "</td>";
                    } else {
                        outsideRow += "<td>" + amount + "</td>";
                    }
                }
                outsideRow += "</tr>";
            }

            res += "<tr>";
            res += "<td colspan=\"5\"><b>Truppen ausserhalb:</td>";
            res += "</tr>";
            res += "<tr>";
            res += "<td colspan=\"5\">";
            outsideTable += headerRow;
            outsideTable += outsideRow;
            outsideTable += "</table>";
            res += outsideTable;
            res += "</td>";
            res += "</tr>";
        }
        return res;
    }

    public static String buildLuckBar(double pLuck) {
        String res = "<table cellspacing=\"0\" cellpadding=\"0\" style=\"border: solid 1px black; padding: 0px;\">";
        res += "<tr>";
        if (pLuck == 0) {
            res += "<td width=\"" + 50 + "\" height=\"12\"></td>";
            res += "<td width=\"" + 0 + "\" style=\"background-color:#FF0000;\"></td>";
            res += "<td width=\"2\" style=\"background-color:rgb(0, 0, 0)\"></td>";
            res += "<td width=\"0\" style=\"background-color:#009300\"></td>";
            res += "<td width=\"50\"></td>";
        } else if (pLuck < 0) {
            double luck = Math.abs(pLuck);
            double filled = luck / 25 * 50;
            double notFilled = 50 - filled;
            res += "<td width=\"" + notFilled + "\" height=\"12\"></td>";
            res += "<td width=\"" + filled + "\" style=\"background-color:#FF0000;\"></td>";
            res += "<td width=\"2\" style=\"background-color:rgb(0, 0, 0)\"></td>";
            res += "<td width=\"0\" style=\"background-color:#009300\"></td>";
            res += "<td width=\"50\"></td>";
        } else {
            double filled = pLuck / 25 * 50;
            double notFilled = 50 - filled;
            res += "<td width=\"50\" height=\"12\"></td>";
            res += "<td width=\"0\" style=\"background-color:#F00;\"></td>";
            res += "<td width=\"2\" style=\"background-color:rgb(0, 0, 0)\"></td>";
            res += "<td width=\"" + filled + "\" style=\"background-color:#009300\"></td>";
            res += "<td width=\"" + notFilled + "\"></td>";
        }

        res += "</tr>";
        res += "</table>";
        return res;
    }
}
