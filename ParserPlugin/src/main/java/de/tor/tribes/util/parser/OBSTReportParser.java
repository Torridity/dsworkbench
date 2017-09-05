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

import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.SilentParserInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Torridity
 */
public class OBSTReportParser implements SilentParserInterface {
    //TODO remove or rewrite with using Parser variable manager
    
    @Override
    public boolean parse(String pData) {
        return false;
    }
    
    private FightReport parseReport(String pData) {
        StringTokenizer t = new StringTokenizer(pData, "\n");
        FightReport result = new FightReport();
        
        while (t.hasMoreTokens()) {
            String token = t.nextToken().trim();
            
            if (token.startsWith("Gesendet")) {
                String sendTime = token.replaceAll("Gesendet", "").trim();
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm");
                try {
                    Date d = f.parse(sendTime);
                    result.setTimestamp(d.getTime());
                } catch (Exception e) {
                    f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                    try {
                        Date d = f.parse(sendTime);
                        result.setTimestamp(d.getTime());
                        result.setTimestamp(d.getTime());
                    } catch (Exception inner) {
                        result.setTimestamp(0L);
                    }
                }
            } else if (token.startsWith("Moral")) {
                String moral = token.replaceAll("Moral", "").replace("%", "").trim();
                try {
                    double dMoral = Double.parseDouble(moral);
                    result.setMoral(dMoral);
                } catch (Exception e) {
                    result.setMoral(0);
                }
            } else if (token.startsWith("Herkunft:")) {
                String source = token.replaceAll("Herkunft:", "").trim();
                List<Village> sourceVillages = PluginManager.getSingleton().executeVillageParser(source);
                if (!sourceVillages.isEmpty()) {
                    result.setSourceVillage(sourceVillages.get(0));
                }                
            }
            
            
        }
        
        return result;
    }
    
    public static void main(String[] args) {
//        Hallo Returned Warlord! | Start - Berichte - Profil - Credits - Logout
//OBST [B~O~B!] - Bericht anzeigen
//
//Letzte 10...
//Alle Berichte
//Bericht einlesen
//Suche
//
//Gruppe: keine Gruppe
//Welt: 74
//Eingelesen von: Returned Warlord
//Eingelesen am: 01.03.2012, 21:20
//Diesen Bericht löschen
//Diesen Bericht in die Gruppe einordnen
//Diesen Bericht der Welt zuordnen
//
//Betreff Willi vom Wald greift 15Kostromo* an
//Gesendet 01.03.2012 16:56
//Der Verteidiger hat gewonnen
//Glück (aus Sicht des Angreifers)
//Pech
//
//Glück
//Moral: 100%
//Angreifer: Willi vom Wald
//Herkunft: 012 Delta1 (791|712) K77
//
//Anzahl: 0 0 0 0 500 0 0 0 0 0 0 0
//Verluste: 0 0 0 0 126 0 0 0 0 0 0 0
//
//Verteidiger: pidonis
//Ziel: 15Kostromo* (770|728) K77
//
//Anzahl: 499 411 0 0 399 5 0 140 0 0 0 0
//Verluste: 0 0 0 0 0 0 0 0 0 0 0 0
//
//Spionage
//Erspähte Rohstoffe: 42 0 549
//Gebäude: Hauptgebäude (Stufe 20)
//Kaserne (Stufe 25)
//Stall (Stufe 20)
//Werkstatt (Stufe 10)
//Adelshof (Stufe 1)
//Schmiede (Stufe 20)
//Versammlungsplatz (Stufe 1)
//Statue (Stufe 1)
//Marktplatz (Stufe 20)
//Holzfäller (Stufe 30)
//Lehmgrube (Stufe 30)
//Eisenmine (Stufe 30)
//Bauernhof (Stufe 30)
//Speicher (Stufe 30)
//Versteck (Stufe 10)
//Wall (Stufe 15)
//
//
//Beute: Holz 0 Lehm 0 Eisen 0 0/0
//
//
//[1]
//Kommentare
//Keine Kommentare bisher.
//Kommentar verfassen
//
//
//
//
//
//OBST v1.0.0.1
//© Michael Nußbaumer, 2011.
    }
}
