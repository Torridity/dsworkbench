/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @TODO (DIFF) More tolerant parsing, also for copied DSWB table data!
 *Parses villages, separated by space or tab, from a string
 * @author Charon
 */
public class VillageParser {

    public static List<Village> parse(String pVillagesString) {
        List<Village> villages = new LinkedList<Village>();
        if (pVillagesString == null) {
            return villages;
        }
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            StringTokenizer t = new StringTokenizer(pVillagesString, " \t\n\r");
            while (t.hasMoreTokens()) {
                try {
                    String token = t.nextToken();
                    if (token.matches("\\(*[0-9]{1,2}\\:[0-9]{1,2}\\:[0-9]{1,2}\\)*")) {
                        token = token.replaceAll("\\(", "").replaceAll("\\)", "");
                        String[] split = token.split(":");
                        int[] coord = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                        Village v = DataHolder.getSingleton().getVillages()[coord[0]][coord[1]];
                        if (v != null) {
                            villages.add(v);
                        }
                    }
                } catch (Exception e) {
                    //skip token
                }
            }
        } else {
            StringTokenizer t = new StringTokenizer(pVillagesString, " \t\r\n");
            while (t.hasMoreTokens()) {
                try {
                    String token = t.nextToken();
                    if (token.matches("\\(*[0-9]{1,3}\\|[0-9]{1,3}\\)*")) {
                        token = token.replaceAll("\\(", "").replaceAll("\\)", "");
                        String[] split = token.split("\\|");
                        Village v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
                        if (v != null) {
                            villages.add(v);
                        }
                    }
                } catch (Exception e) {
                    //skip token
                }
            }
        }
        return villages;
    }

    public static void main(String[] args) throws Exception {
        /*   Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        System.out.println(VillageParser.parse((String) t.getTransferData(DataFlavor.stringFlavor)));
         */
    }
}
