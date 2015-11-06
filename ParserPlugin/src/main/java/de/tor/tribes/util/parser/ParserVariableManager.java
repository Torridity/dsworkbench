/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.util.GlobalOptions;
import java.io.FileInputStream;
import java.lang.reflect.Method;
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
