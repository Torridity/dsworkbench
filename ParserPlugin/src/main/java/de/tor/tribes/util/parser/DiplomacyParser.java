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
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.Marker;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.mark.MarkerManager;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Patrick
 * @author Torridity
 */
public class DiplomacyParser implements SilentParserInterface {

    private static final boolean DEBUG = false;

    public boolean parse(String pData) {
        StringTokenizer lineTok = new StringTokenizer(pData, "\n\r");
        List<Marker> markers = new ArrayList<>();
        boolean allies = false;
        boolean naps = false;
        boolean enemies = false;
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            debug("Try line " + line);

            if (line.trim().contains(ParserVariableManager.getSingleton().getProperty("diplomacy.allies"))) {
                debug("Got allies");
                allies = true;
                naps = false;
                enemies = false;
            } else if (line.trim().contains(ParserVariableManager.getSingleton().getProperty("diplomacy.nap"))) {
                debug("Got naps");
                naps = true;
                allies = false;
                enemies = false;
            } else if (line.trim().contains(ParserVariableManager.getSingleton().getProperty("diplomacy.enemy"))) {
                debug("Got enemies");
                enemies = true;
                naps = false;
                allies = false;
            } else {
                if (allies) {
                    Marker m = getMarkerFromLine(line, Constants.ALLY_MARKER);
                    if (m != null) {
                        debug("Adding ally marker for tag " + m.getView().getAlly());
                        markers.add(m);
                    }
                } else if (naps) {
                    Marker m = getMarkerFromLine(line, Constants.NAP_MARKER);
                    if (m != null) {
                        debug("Adding nap marker for tag " + m.getView().getAlly());
                        markers.add(m);
                    }
                } else if (enemies) {
                    Marker m = getMarkerFromLine(line, Constants.ENEMY_MARKER);
                    if (m != null) {
                        debug("Adding enemy marker for tag " + m.getView().getAlly());
                        markers.add(m);
                    }
                }
            }
        }

        if(markers.isEmpty())return false;
        
		for(Marker mark : markers) MarkerManager.getSingleton().addManagedElement(mark);
		
		return true;
        
    }

    private Marker getMarkerFromLine(String pLine, Color pMarkerColor) {
        StringTokenizer allySplit = new StringTokenizer(pLine, " \t");
        String tag = null;
        while (allySplit.hasMoreTokens()) {

            if (tag == null) {
                tag = allySplit.nextToken();
            } else {
                tag += " " + allySplit.nextToken();
            }
            debug("Trying tag '" + tag + "'");
            Ally a = DataHolder.getSingleton().getAllyByTagName(tag);
            if (a != null) {
                Marker m = new Marker();
                m.setMarkerType(Marker.ALLY_MARKER_TYPE);
                m.setMarkerID(a.getId());
                m.setMarkerColor(pMarkerColor);
                return m;
            }
        }
        return null;
    }

    private void debug(String pLine) {
        if (DEBUG) {
            System.out.println(pLine);
        }
    }

    public static void main(String[] args) throws Exception {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        new DiplomacyParser().parse(data);
    }
}
