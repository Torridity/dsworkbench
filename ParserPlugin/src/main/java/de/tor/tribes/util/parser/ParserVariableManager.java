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
        DEFAULT.put("de.groups.edit", "bearbeiten");
        DEFAULT.put("de.sos.defender", "Verteidiger");
        DEFAULT.put("de.sos.name", "Name:");
        DEFAULT.put("de.attack.arrive.time", "Ankunft:");
        DEFAULT.put("de.sos.source", "Herkunft:");
        DEFAULT.put("de.sos.destination", "Angegriffenes Dorf");
        DEFAULT.put("de.sos.arrive.time", "Ankunftszeit:");
        DEFAULT.put("de.sos.wall.level", "Stufe des Walls:");
        DEFAULT.put("de.sos.date.format", "dd.MM.yy HH:mm:ss");
        DEFAULT.put("de.sos.date.format.ms", "dd.MM.yy HH:mm:ss:SSS");
        DEFAULT.put("de.sos.troops.in.village", "Anwesende Truppen");
        DEFAULT.put("de.overview.groups", "Gruppen:");
        DEFAULT.put("de.groups.all", "alle");
        DEFAULT.put("de.diplomacy.allies", "Verbündete");
        DEFAULT.put("de.diplomacy.nap", "Nicht-Angriffs-Pakt (NAP)");
        DEFAULT.put("de.diplomacy.enemy", "Feinde");
    }

    public String getProperty(String pProperty) {
        String serverID = GlobalOptions.getSelectedServer();
        if (serverID == null) {
            serverID = "de43";
        }
        String countryID = serverID.replaceAll("[0-9]", "");
        String property = null;
        if (variableMappings == null) {
            property = DEFAULT.getProperty(countryID + "." + pProperty);
        } else {
            property = variableMappings.getProperty(countryID + "." + pProperty);
        }
        if (property == null) {
            return DEFAULT.getProperty("de." + pProperty);
        }
        return property;
    }

   
}
