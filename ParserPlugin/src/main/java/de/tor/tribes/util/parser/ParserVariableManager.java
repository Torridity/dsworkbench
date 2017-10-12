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
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class ParserVariableManager {
    
    private static final Logger logger = Logger.getLogger("ParserVariableManager");

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
        File[] files = new File("./templates/Parser Lang").listFiles();
        for (File file : files) {
            if (file.isFile() && fileExtension(file).equals("parserprop")) {
                try {
                    logger.debug("Loading Parser Language file: " + file.getName());
                    variableMappings.load(new StringReader(getTranslation(
                            file.getAbsolutePath(), file.getName().substring(0,
                            file.getName().length() - fileExtension(file).length()))));
                } catch (Exception e) {
                    logger.warn("Failed to load Parser Language file: " + file.getName(), e);
                }
            }
            else if(file.isFile()) {
                logger.debug("File with wrong extension ignored: " + file.getName());
            }
        }
        loadDefaultProperties();
    }
    
    /**
     * All Variables that are stored here are stored inside the template files too...
     */
    private void loadDefaultProperties() {
        DEFAULT.put("de.troops.own", "eigene");
        DEFAULT.put("de.troops.in.village", "im Dorf");
        DEFAULT.put("de.troops.outside", "ausw\u00E4rts");
        DEFAULT.put("de.troops.on.the.way", "unterwegs");
        DEFAULT.put("de.troops.place.from.village", "Aus diesem Dorf");
        DEFAULT.put("de.troops.place.overall", "Insgesamt");
        DEFAULT.put("de.troops.place.in.other.villages", "Truppen in anderen D\u00F6rfern");
        DEFAULT.put("de.troops.commands", "Befehle");
        DEFAULT.put("de.troops", "Truppen");
        DEFAULT.put("de.overview.groups", "Gruppen:");
        DEFAULT.put("de.groups.all", "alle");
        DEFAULT.put("de.groups.edit", "bearbeiten");
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
        DEFAULT.put("de.diplomacy.allies", "Verb\u00fcndete");
        DEFAULT.put("de.diplomacy.nap", "Nicht-Angriffs-Pakt (NAP)");
        DEFAULT.put("de.diplomacy.enemy", "Feinde");
        DEFAULT.put("de.movement.tableHeader.command", "Befehl");
        DEFAULT.put("de.movement.tableHeader.srcVillage", "Herkunftsdorf");
        DEFAULT.put("de.movement.tableHeader.arriveTime", "Ankunft");
        DEFAULT.put("de.movement.type.returning.1", "Zur\u00fcckgeschickt");
        DEFAULT.put("de.movement.type.returning.2", "R\u00fcckzug");
        DEFAULT.put("de.movement.type.returning.3", "R\u00fcckkehr");
        DEFAULT.put("de.movement.type.abortedMovement", "Abgebrochener Befehl");
        DEFAULT.put("de.movement.type.attack", "Angriff");
        DEFAULT.put("de.movement.type.support", "Unterst\u00fctzung");
        DEFAULT.put("de.movement.date.today", "heute");
        DEFAULT.put("de.movement.date.tomorrow", "morgen");
        DEFAULT.put("de.movement.date.format", "'am' dd.MM. 'um' HH:mm:ss");
        DEFAULT.put("de.movement.date.format.ms", "'am' dd.MM. 'um' HH:mm:ss:SSS");
        DEFAULT.put("de.movement.date.format.ouput", "'am' dd.MM.");
        DEFAULT.put("de.report.fight.time", "Kampfzeit");
        DEFAULT.put("de.report.has.won", "hat gewonnen");
        DEFAULT.put("de.report.spy", "ausgekundschaftet");
        DEFAULT.put("de.report.att.luck", "Angreifergl\u00fcck");
        DEFAULT.put("de.report.luck", "Gl\u00fcck");
        DEFAULT.put("de.report.badluck", "Pech");
        DEFAULT.put("de.report.moral", "Moral:");
        DEFAULT.put("de.report.att.player", "Angreifer");
        DEFAULT.put("de.report.village.1", "Dorf");
        DEFAULT.put("de.report.village.2", "Herkunft");
        DEFAULT.put("de.report.village.3", "Ziel");
        DEFAULT.put("de.report.num", "Anzahl");
        DEFAULT.put("de.report.loss", "Verluste");
        DEFAULT.put("de.report.defender.player", "Verteidiger");
        DEFAULT.put("de.report.spy.res", "Ersp\u00E4hte Rohstoffe");
        DEFAULT.put("de.report.haul", "Beute");
        DEFAULT.put("de.report.buildings.main", "Hauptgeb\u00E4ude");
        DEFAULT.put("de.report.buildings.barracks", "Kaserne");
        DEFAULT.put("de.report.buildings.stable", "Stall");
        DEFAULT.put("de.report.buildings.workshop", "Werkstatt");
        DEFAULT.put("de.report.buildings.church", "Kirche");
        DEFAULT.put("de.report.buildings.watchtower", "Wachturm");
        DEFAULT.put("de.report.buildings.academy", "Adelshof");
        DEFAULT.put("de.report.buildings.smithy", "Schmiede");
        DEFAULT.put("de.report.buildings.rally", "Versammlungsplatz");
        DEFAULT.put("de.report.buildings.statue", "Statue");
        DEFAULT.put("de.report.buildings.market", "Marktplatz");
        DEFAULT.put("de.report.buildings.wood", "Holzf\u00E4llerlager");
        DEFAULT.put("de.report.buildings.clay", "Lehmgrube");
        DEFAULT.put("de.report.buildings.iron", "Eisenmine");
        DEFAULT.put("de.report.buildings.farm", "Bauernhof");
        DEFAULT.put("de.report.buildings.storage", "Speicher");
        DEFAULT.put("de.report.buildings.hide", "Versteck");
        DEFAULT.put("de.report.buildings.wall", "Wall");
        DEFAULT.put("de.report.buildings.first.church", "Erste Kirche");
        DEFAULT.put("de.report.damage.ram", "Schaden durch Rammb\u00F6cke");
        DEFAULT.put("de.report.damage.wall", "Wall besch\u00E4digt von Level");
        DEFAULT.put("de.report.damage.to", "auf Level");
        DEFAULT.put("de.report.damage.kata", "Schaden durch Katapultbeschuss");
        DEFAULT.put("de.report.damage.level", "Level");
        DEFAULT.put("de.report.acceptance.1", "Ver\u00E4nderung der Zustimmung");
        DEFAULT.put("de.report.acceptance.2", "Zustimmung gesunken von");
        DEFAULT.put("de.report.acceptance.3", "auf");
        DEFAULT.put("de.report.acceptance.4", "Zustimmung");
        DEFAULT.put("de.report.acceptance.5", "Gesunken von");
        DEFAULT.put("de.report.ontheway", "Truppen des Verteidigers, die unterwegs waren");
        DEFAULT.put("de.report.outside", "Truppen des Verteidigers in anderen D\u00F6rfern");
        DEFAULT.put("de.report.hidden", "Durch Besitzer des Berichts verborgen");
        DEFAULT.put("de.report.full.destruction", "Keiner deiner K\u00E4mpfer ist lebend zur\u00fcckgekehrt");
        DEFAULT.put("de.report.win.win", "gewonnen");
    }

    public String getProperty(String pProperty) {
        String serverID = GlobalOptions.getSelectedServer();
        String countryID = "de";
        if (serverID != null) {
            countryID = serverID.replaceAll("[0-9]", "");
        }
        
        String property = null;
        if (variableMappings != null) {
            property = variableMappings.getProperty(countryID + "." + pProperty);
        }
        if (property == null) {
            property = DEFAULT.getProperty(countryID + "." + pProperty);
        }
        if (variableMappings != null && property == null) {
            property = variableMappings.getProperty("de." + pProperty);
        }
        if (property == null) {
            property = DEFAULT.getProperty("de." + pProperty);
        }
        return property;
    }   

    private String fileExtension(File file) {
        String fileName = file.getName().toLowerCase();
        int index = fileName.lastIndexOf('.');
        if(index != -1) {
            return fileName.substring(index + 1);
        }
        return fileName;
    }
    
    private String getTranslation(String pPath, String pPrefix) throws IOException {
        StringBuilder str = new StringBuilder();
        String[] merged = getMergedString(pPath, 10).toString()
                .split(System.lineSeparator());
        
        for (String line : merged) {
            if(!line.startsWith("#")) {
                //only add line if it is no comment
                str.append(pPrefix).append(line).append(System.lineSeparator());
            }
        }
        return str.toString();
    }
    
    private StringBuilder getMergedString(String pPath, int pRecursion) throws IOException {
        StringBuilder str = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(pPath));
        String line;
        while((line = br.readLine()) != null) {
            line = line.replaceAll("\n", "").replaceAll("\r", "");
            if(line.startsWith("[") && line.endsWith("]")) {
                str.append(getMergedString(new File(pPath).getParent() + "/" +
                        line.substring(1, line.length() - 1), pRecursion - 1));
                str.append(System.lineSeparator());
            }
            else {
                str.append(line).append(System.lineSeparator());
            }
        }
        return str;
    }
}
