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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Patrick
 * @author Torridity
 */
public class DiplomacyParser implements SilentParserInterface {
    private static Logger logger = LogManager.getLogger("DiplomacyParser");
    
    public boolean parse(String pData) {
        StringTokenizer lineTok = new StringTokenizer(pData, "\n\r");
        Map<Ally, Color> markers = new HashMap<>();
        boolean allies = false;
        boolean naps = false;
        boolean enemies = false;
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            logger.debug("Try line " + line);

            if (line.trim().contains(getVariable("diplomacy.allies"))) {
                logger.debug("Got allies");
                allies = true;
                naps = false;
                enemies = false;
            } else if (line.trim().contains(getVariable("diplomacy.nap"))) {
                logger.debug("Got naps");
                naps = true;
                allies = false;
                enemies = false;
            } else if (line.trim().contains(getVariable("diplomacy.enemy"))) {
                logger.debug("Got enemies");
                enemies = true;
                naps = false;
                allies = false;
            } else {
                if (allies) {
                    Ally a = getAllyFromLine(line);
                    if (a != null) {
                        logger.debug("Adding ally marker for tag " + a);
                        markers.put(a, Constants.ALLY_MARKER);
                    }
                } else if (naps) {
                    Ally a = getAllyFromLine(line);
                    if (a != null) {
                        logger.debug("Adding nap marker for tag " + a);
                        markers.put(a, Constants.NAP_MARKER);
                    }
                } else if (enemies) {
                    Ally a = getAllyFromLine(line);
                    if (a != null) {
                        logger.debug("Adding enemy marker for tag " + a);
                        markers.put(a, Constants.ENEMY_MARKER);
                    }
                }
            }
        }

        if(markers.isEmpty())return false;
        
        for(Entry<Ally, Color> mark: markers.entrySet()) {
            MarkerManager.getSingleton().addMarker(mark.getKey(), mark.getValue());
        }
        
        return true;
    }

    private Ally getAllyFromLine(String pLine) {
        StringTokenizer allySplit = new StringTokenizer(pLine, " \t");
        String tag = null;
        while (allySplit.hasMoreTokens()) {

            if (tag == null) {
                tag = allySplit.nextToken();
            } else {
                tag += " " + allySplit.nextToken();
            }
            logger.debug("Trying tag '" + tag + "'");
            Ally a = DataHolder.getSingleton().getAllyByTagName(tag);
            if (a != null) {
                return a;
            }
        }
        return null;
    }
    

    private String getVariable(String pProperty) {
        return ParserVariableManager.getSingleton().getProperty(pProperty);
    }
    

    public static void main(String[] args) throws Exception {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        new DiplomacyParser().parse(data);
    }
}
