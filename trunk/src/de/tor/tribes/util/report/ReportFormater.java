/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.Village;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * @author Jejkal
 */
public class ReportFormater {

    public static String format(FightReport pReport) {
        StringBuffer b = new StringBuffer();
        SimpleDateFormat d = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        b.append("[quote]");
        b.append("[i][b]Betreff:[/b][/i] " + pReport.getAttacker().toBBCode() + " greift " + pReport.getTargetVillage().toBBCode() + " an\n");
        b.append("[i][b]Gesendet:[/b][/i] " + d.format(new Date(pReport.getTimestamp())) + "\n\n");
        if (pReport.isWon()) {
            b.append("[size=16]Der Angreifer hat gewonnen[/size]\n");
        } else {
            b.append("[size=16]Der Verteidiger hat gewonnen[/size]\n");
        }
        b.append("\n");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMinimumFractionDigits(1);

        b.append("[b]Glück:[/b] " + "[img]http://dsextra.net/ic/luck_" + nf.format(pReport.getLuck()) + "[/img] " + nf.format(pReport.getLuck()) + "%\n");
        nf.setMinimumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        b.append("[b]Moral:[/b] " + nf.format(pReport.getMoral()) + " %\n");
        b.append("\n");
        b.append("[b]Angreifer:[/b] " + pReport.getAttacker().toBBCode() + "\n");
        b.append("[b]Dorf:[/b] " + pReport.getSourceVillage().toBBCode() + "\n\n");
        String graphUrl = "";
        if (!pReport.areAttackersHidden()) {

            graphUrl = "http://dsextra.net/ic/knights_";
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = pReport.getAttackers().get(unit);
                graphUrl += amount + "_";
            }
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = pReport.getDiedAttackers().get(unit);
                graphUrl += amount + "_";
            }
            graphUrl = graphUrl.substring(0, graphUrl.lastIndexOf("_"));
            b.append("[img]" + graphUrl + "[/img]\n");
        } else {
            b.append("Durch Besitzer des Berichts verborgen\n");
        }
        b.append("\n");

        b.append("[b]Verteidiger:[/b] " + pReport.getDefender().toBBCode() + "\n");
        b.append("[b]Dorf:[/b] " + pReport.getTargetVillage().toBBCode() + "\n\n");
        if (!pReport.wasLostEverything()) {
            graphUrl = "http://dsextra.net/ic/knights_";
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = pReport.getDefenders().get(unit);
                graphUrl += amount + "_";
            }
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = pReport.getDiedDefenders().get(unit);
                graphUrl += amount + "_";
            }
            graphUrl = graphUrl.substring(0, graphUrl.lastIndexOf("_"));
            b.append("[img]" + graphUrl + "[/img]\n");
        } else {
            b.append("\nKeiner deiner Kämpfer ist lebend zurückgekehrt.\nEs konnten keine Informationen über die Truppenstärke des Gegners erlangt werden.\n");
        }

        b.append("\n");
        boolean wasAdditionTroops = false;
        if (pReport.whereDefendersOnTheWay()) {
            wasAdditionTroops = true;
            b.append("[quote]\n");
            graphUrl = "http://dsextra.net/ic/knights_";
            b.append("[b]Truppen des Verteidigers, die unterwegs waren[/b]\n\n");
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = pReport.getDefendersOnTheWay().get(unit);
                graphUrl += amount + "_";
            }
            graphUrl = graphUrl.substring(0, graphUrl.lastIndexOf("_"));
            b.append("[img]" + graphUrl + "[/img]\n\n");
        }
        if (pReport.whereDefendersOutside()) {
            wasAdditionTroops = true;
            Enumeration<Village> targetKeys = pReport.getDefendersOutside().keys();
            while (targetKeys.hasMoreElements()) {
                Village target = targetKeys.nextElement();
                b.append(target.toBBCode() + "\n\n");
                graphUrl = "http://dsextra.net/ic/knights_";
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    Integer amount = pReport.getDefendersOutside().get(target).get(unit);
                    graphUrl += amount + "_";
                }
                graphUrl = graphUrl.substring(0, graphUrl.lastIndexOf("_"));
                b.append("[img]" + graphUrl + "[/img]\n\n");
            }
        }


        if (wasAdditionTroops) {
            b.append("[/quote]\n");
        }

        if (pReport.wasSnobAttack()) {
            b.append("[b]Veränderung der Zustimmung:[/b] Zustimmung gesunken von " + nf.format(pReport.getAcceptanceBefore()) + " auf " + pReport.getAcceptanceAfter() + "\n");
        }

        if (pReport.wasWallDamaged()) {
            b.append("[b]Schaden durch Rammen:[/b] Wall beschädigt von Level " + pReport.getWallBefore() + " auf Level " + pReport.getWallAfter() + "\n");
        }

        if (pReport.wasBuildingDamaged()) {
            b.append("[b]Schaden durch Katapultbeschuss:[/b] " + pReport.getAimedBuilding() + " beschädigt von Level " + pReport.getBuildingBefore() + " auf Level " + pReport.getBuildingAfter() + "\n");
        }


        b.append("\n");
        b.append("[size=7]Formatiert mit [url=http://www.dsworkbench.de/index.php?id=23]DS Workbench[/url], powered by [url=http://dsextra.net/report]dsextra report[/url]");
        b.append("[/quote]");
        return b.toString();
    }
    /*
    [quote]
    [i][b]Betreff [/b][/i]: [player]Rattenfutter[/player] greift [village]498|816[/village] an
    [i][b]Gesendet[/b][/i]:	01.02.10 02:42
    [size=16]Der Angreifer hat gewonnen[/size]

    [b]Glück:[/b]  [img]http://www.die-staemme.de/graphic/klee.png[/img]
    [b]Moral:[/b] 71 %

    [b]Angreifer:[/b][player]Rattenfutter[/player]
    [b]Dorf:[/b] [village]485|822[/village]
    [u][b][i]Anzahl:[/i][/b][/u]

    0[img]http://www.die-staemme.de/graphic/unit/unit_spear.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_sword.png[/img] 6300[img]http://www.die-staemme.de/graphic/unit/unit_axe.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_archer.png[/img] 200[img]http://www.die-staemme.de/graphic/unit/unit_spy.png[/img] 2450[img]http://www.die-staemme.de/graphic/unit/unit_light.png[/img] 300[img]http://www.die-staemme.de/graphic/unit/unit_marcher.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_heavy.png[/img] 100[img]http://www.die-staemme.de/graphic/unit/unit_ram.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_catapult.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_knight.png[/img] 1[img]http://www.die-staemme.de/graphic/unit/unit_snob.png[/img]
    [u][b][i]Verluste:[/i][/b][/u]

    0[img]http://www.die-staemme.de/graphic/unit/unit_spear.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_sword.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_axe.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_archer.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_spy.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_light.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_marcher.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_heavy.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_ram.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_catapult.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_knight.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_snob.png[/img]
    [b]Verteidiger:[/b][player]axel520[/player]
    [b]Dorf:[/b] [village]498|816[/village]
    [u][b][i]Anzahl:[/i][/b][/u]

    0[img]http://www.die-staemme.de/graphic/unit/unit_spear.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_sword.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_axe.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_archer.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_spy.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_light.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_marcher.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_heavy.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_ram.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_catapult.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_knight.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_snob.png[/img]
    [u][b][i]Verluste:[/i][/b][/u]

    0[img]http://www.die-staemme.de/graphic/unit/unit_spear.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_sword.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_axe.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_archer.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_spy.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_light.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_marcher.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_heavy.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_ram.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_catapult.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_knight.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_snob.png[/img]


    [b][i]Beute :[/i][/b]


    [b][i]Veränderung der Zustimmung: [/i][/b]: Zustimmung gesunken von 27 auf -3

    [u][b][i]Einheiten die unterwegs waren:[/i][/b][/u]

    0[img]http://www.die-staemme.de/graphic/unit/unit_spear.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_sword.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_axe.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_archer.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_spy.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_light.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_marcher.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_heavy.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_ram.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_catapult.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_knight.png[/img] 0[img]http://www.die-staemme.de/graphic/unit/unit_snob.png[/img]

    by [url="http://bericht.terenceds.de/?lang=de"]DS-Berichtformatierer[/url]
    [/quote]

     */
}
