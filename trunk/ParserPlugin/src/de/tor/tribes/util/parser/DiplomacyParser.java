/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.Marker;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.interfaces.GenericParserInterface;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Torridity
 */
public class DiplomacyParser implements GenericParserInterface<Marker> {

    /**de29
    Verbündete
    ~DPG~
    SAWÜ6
    OMS
    Nicht-Angriffs-Pakt (NAP)
    SAW
    Feinde
    +AR+
    DKGD
    A-T-A
    -WZ-
     *BC*
    Knight
    ~LoW~
    N.O.D.
    ~DN~
    nod-J
    nod-OB
    Clan
    -WP-
    [M]
    STK
    ANSI
    PUNCH!
    MW
    bzsz
     */
    /**en0
    Verbündete
    ~B~B~ 	terminate
    Nicht-Angriffs-Pakt (NAP)
    K54 	terminate
    Enemies
    
     */
    private final boolean DEBUG = false;

    public List<Marker> parse(String pData) {

        StringTokenizer lineTok = new StringTokenizer(pData, "\n\r");
        List<Marker> markers = new ArrayList<Marker>();
        boolean allies = false;
        boolean naps = false;
        boolean enemies = false;
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            debug("Try line " + line);

            if (line.trim().indexOf("Verbündete") > -1) {
                debug("Got allies");
                allies = true;
                naps = false;
                enemies = false;
            } else if (line.trim().indexOf("Nicht-Angriffs-Pakt (NAP)") > -1) {
                debug("Got naps");
                naps = true;
                allies = false;
                enemies = false;
            } else if (line.trim().indexOf("Feinde") > -1) {
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

        return markers;
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
