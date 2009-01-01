/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class TroopsParser50 {

    private static Logger logger = Logger.getLogger("TroopsParserv5.0");

    public static boolean parse(String pTroopsString) {
        StringTokenizer lines = new StringTokenizer(pTroopsString, "\n\r");
        boolean retValue = false;
        //boolean haveVillage = false;
        Village currentVillage = null;
        List<Integer> troops = new LinkedList<Integer>();
        Tribe userTribe = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe();
        while (lines.hasMoreElements()) {

            //parse single line for village
            String line = lines.nextToken();
            for (Village v : userTribe.getVillageList()) {
                String village = "(" + v.getX() + "|" + v.getY() + ")";
                if (line.indexOf(village) > -1) {
                    if (line.indexOf("Übersichten") == -1) {
                        currentVillage = v;
                    }
                }
            }

            //parse village units
            if (currentVillage != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Parsing units for village '" + currentVillage.toString() + "'");
                }
                //ignore overviews line if markes
                for (int i = 0;;) {
                    line = lines.nextToken().trim();
                    if (line.startsWith("eigene")) {
                        logger.debug("Found 'own units' line");
                        int[] own = parseUnits(line.replaceAll("eigene", "").trim());
                        List<Integer> troopList = new LinkedList<Integer>();
                        for (int t : own) {
                            troopList.add(t);
                        }
                        TroopsManager.getSingleton().addTroopsForVillage(currentVillage, troopList);
                        i++;
                    } else if (line.startsWith("im Dorf")) {
                        //logger.debug("Found 'in village' line");
                        //int[] inVillage = parseUnits(line.replaceAll("im Dorf", "").trim());
                        i++;
                    } else if (line.startsWith("auswärts")) {
                        //logger.debug("Found 'outside' line");
                        //int[] outside = parseUnits(line.replaceAll("auswärts", "").trim());
                        i++;
                    } else if (line.startsWith("unterwegs")) {
                        //logger.debug("Found 'on way' line");
                        //int[] onTheWay = parseUnits(line.replaceAll("unterwegs", "").trim());
                        i++;
                    }
                    if (i == 4) {
                        break;
                    }
                }//end of unit for loop
            }//end of village line
        }//end of all
        return retValue;
    }

    private static int[] parseUnits(String pLine) {
        String line = pLine.replaceAll("eigene", "").replaceAll("Befehle", "").replaceAll("Truppen", "");
        StringTokenizer t = new StringTokenizer(line, " \t");
        int uCount = t.countTokens();
        int[] units = new int[uCount];
        int cnt = 0;
        while (t.hasMoreTokens()) {
            units[cnt] = Integer.parseInt(t.nextToken());
            cnt++;
        }
        return units;
    }

    public static void main(String[] args) {

        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            String s = " 003 | Spitfire (471|482) K44\n" +
                    "eigene	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Befehle\n" +
                    "im Dorf	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Truppen\n" +
                    "auswärts	0	0	0	0	0	0	0	0	0	0	0	0\n" +
                    "unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle\n" +
                    "2Fast4You (475|480) K44\n" +
                    "eigene	600	500	0	0	134	0	0	354	0	0	0	1	Befehle\n" +
                    "im Dorf	600	500	0	0	134	0	0	354	0	0	0	1	Truppen\n" +
                    "auswärts	4400	3000	0	3000	66	0	0	1046	0	0	0	0\n" +
                    "unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle\n";


            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            TroopsParser50.parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
