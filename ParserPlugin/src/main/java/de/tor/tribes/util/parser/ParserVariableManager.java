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

import de.tor.tribes.util.GlobalOptions;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author Torridity
 */
public class ParserVariableManager {

    private static ParserVariableManager SINGLETON = null;
    private Properties variableMappings = null;
    private Properties DEFAULT = new Properties();

    public static synchronized ParserVariableManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ParserVariableManager();
        }
        return SINGLETON;
    }

    ParserVariableManager() {
        variableMappings = new Properties();
        try {
            variableMappings.load(new FileInputStream("./templates/parser.properties"));
        } catch (Exception e) {
            variableMappings = null;
        }
        loadDefaultProperties();
    }

    private void loadDefaultProperties() {
        DEFAULT.put("de.troops.own", "eigene");
        DEFAULT.put("de.troops.in.village", "im Dorf");
        DEFAULT.put("de.troops.outside", "auswärts");
        DEFAULT.put("de.troops.on.the.way", "unterwegs");
        DEFAULT.put("de.troops.place.from.village", "Aus diesem Dorf");
        DEFAULT.put("de.troops.place.overall", "Insgesamt");
        DEFAULT.put("de.troops.place.in.other.villages", "Truppen in anderen Dörfern");
        DEFAULT.put("de.troops.commands", "Befehle");
        DEFAULT.put("de.troops", "Truppen");
        DEFAULT.put("de.overview.groups", "Gruppen:");
        DEFAULT.put("de.groups.all", "alle");
        DEFAULT.put("de.groups.edit", "bearbeiten");
        DEFAULT.put("de.attack.arrive.time", "Ankunft:");
        DEFAULT.put("de.sos.defender", "Verteidiger");
        DEFAULT.put("de.sos.name", "Name:");
        DEFAULT.put("de.sos.source", "Herkunft:");
        DEFAULT.put("de.sos.destination", "Angegriffenes Dorf");
        DEFAULT.put("de.sos.arrive.time", "Ankunftszeit:");
        DEFAULT.put("de.sos.wall.level", "Stufe des Walls:");
        DEFAULT.put("de.sos.date.format", "dd.MM.yy HH:mm:ss");
        DEFAULT.put("de.sos.date.format.ms", "dd.MM.yy HH:mm:ss:SSS");
        DEFAULT.put("de.sos.troops.in.village", "Anwesende Truppen");
        DEFAULT.put("de.sos.short.village", "Dorf:");
        DEFAULT.put("de.sos.short.wall.level", "Wallstufe:");
        DEFAULT.put("de.sos.short.defender", "Verteidiger:");
        DEFAULT.put("de.sos.short.movement", "-->");
        DEFAULT.put("de.diplomacy.allies", "Verbündete");
        DEFAULT.put("de.diplomacy.nap", "Nicht-Angriffs-Pakt (NAP)");
        DEFAULT.put("de.diplomacy.enemy", "Feinde");
        DEFAULT.put("de.movement.tableHeader.command", "Befehl");
        DEFAULT.put("de.movement.tableHeader.srcVillage", "Herkunftsdorf");
        DEFAULT.put("de.movement.tableHeader.arriveTime", "Ankunft");
        DEFAULT.put("de.movement.type.returning.1", "Zurückgeschickt");
        DEFAULT.put("de.movement.type.returning.2", "Rückzug");
        DEFAULT.put("de.movement.type.abortedMovement", "Abgebrochener Befehl");
        DEFAULT.put("de.movement.type.attack", "Angriff");
        DEFAULT.put("de.movement.type.support", "Unterstützung");
        DEFAULT.put("de.movement.date.today", "heute");
        DEFAULT.put("de.movement.date.tomorrow", "morgen");
        DEFAULT.put("de.movement.date.format", "'am' dd.MM. 'um' HH:mm:ss");
        DEFAULT.put("de.movement.date.format.ms", "'am' dd.MM. 'um' HH:mm:ss:SSS");
        DEFAULT.put("de.movement.date.format.ouput", "'am' dd.MM.");
        DEFAULT.put("de.report.fight.time", "Kampfzeit");
        DEFAULT.put("de.report.has.won", "hat gewonnen");
        DEFAULT.put("de.report.spy", "ausgekundschaftet");
        DEFAULT.put("de.report.att.luck", "Angreiferglück");
        DEFAULT.put("de.report.luck", "Glück");
        DEFAULT.put("de.report.badluck", "Pech");
        DEFAULT.put("de.report.moral", "Moral:");
        DEFAULT.put("de.report.att.player", "Angreifer");
        DEFAULT.put("de.report.village.1", "Dorf");
        DEFAULT.put("de.report.village.2", "Herkunft");
        DEFAULT.put("de.report.village.3", "Ziel");
        DEFAULT.put("de.report.buildings.wood", "Holzfäller");
        DEFAULT.put("de.report.buildings.clay", "Lehmgrube");
        DEFAULT.put("de.report.buildings.iron", "Eisenmine");
        DEFAULT.put("de.report.buildings.storage", "Speicher");
        DEFAULT.put("de.report.buildings.hide", "Versteck");
        DEFAULT.put("de.report.buildings.wall", "Wall");
        DEFAULT.put("de.report.buildings.first.church", "Erste Kirche");
        DEFAULT.put("de.report.buildings.curch", "Kirche");
        DEFAULT.put("de.report.damage.ram", "Schaden durch Rammböcke:");
        DEFAULT.put("de.report.damage.wall", "Wall beschädigt von Level");
        DEFAULT.put("de.report.damage.to", "auf Level");
        DEFAULT.put("de.report.damage.kata", "Schaden durch Katapultbeschuss:");
        DEFAULT.put("de.report.damage.level", "Level");
        DEFAULT.put("de.report.acceptance.1", "Veränderung der Zustimmung");
        DEFAULT.put("de.report.acceptance.2", "Zustimmung gesunken von");
        DEFAULT.put("de.report.acceptance.3", "auf");
        DEFAULT.put("de.report.acceptance.4", "Zustimmung:");
        DEFAULT.put("de.report.acceptance.5", "Gesunken von");
        DEFAULT.put("de.report.ontheway", "Truppen des Verteidigers, die unterwegs waren");
        DEFAULT.put("de.report.outside", "Truppen des Verteidigers in anderen Dörfern");
        DEFAULT.put("de.report.hidden", "Durch Besitzer des Berichts verborgen");
        DEFAULT.put("de.report.full.destruction", "Keiner deiner Kämpfer ist lebend zurückgekehrt");
        DEFAULT.put("de.report.win.win", "gewonnen");
        DEFAULT.put("de.report.win.spy", "ausgekundschaftet");
        DEFAULT.put("de.report.", "");
    }

    public String getProperty(String pProperty) {
        String serverID = GlobalOptions.getSelectedServer();
        if (serverID == null) {
            serverID = "de43";
        }
        String countryID = serverID.replaceAll("[0-9]", "");
        String property = null;
        if (variableMappings != null) {
            property = variableMappings.getProperty(countryID + "." + pProperty);
        }
        if (property == null) {
            property = DEFAULT.getProperty(countryID + "." + pProperty);
        }
        if (property == null) {
            return DEFAULT.getProperty("de." + pProperty);
        }
        return property;
    }

   
}
