/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.types.FightReport;

/**
 *
 * @author Jejkal
 */
public class ReportFormater {

    public String format(FightReport pReport) {
        StringBuffer b = new StringBuffer();

        return "";
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
