/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *@TODO(1.5) Separate troop informations and use them in different tools
 * @author Charon
 */
public class TroopsParser {

    /*
    003 | Spitfire (471|482) K44  
    eigene	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Befehle
    im Dorf	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Truppen
    auswärts	0	0	0	0	0	0	0	0	0	0	0	0
    unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
     */
    public static boolean parse(String pTroopsString) {
        StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r");
        int villageLines = -1;
        boolean retValue = false;
        //boolean haveVillage = false;
        Village v = null;
        List<Integer> troops = new LinkedList<Integer>();
        while (lineTok.hasMoreElements()) {

            //parse single line for village
            String line = lineTok.nextToken();
            //tokenize line by tab and space
            StringTokenizer elemTok = new StringTokenizer(line, " \t");
            //parse single line for village
            if (v != null) {
                //parse 4 village lines!
                line = line.trim();
                if (line.startsWith("eigene")) {
                    for (int i : parseUnits(line.replaceAll("eigene", "").trim())) {
                        //own units in village
                        troops.add(i);
                    }
                } else if (line.startsWith("im Dorf")) {
                    for (int i : parseUnits(line.replaceAll("im Dorf", "").trim())) {
                        //all units in village
                        //ignored for now
                    }
                } else if (line.startsWith("auswärts")) {
                    for (int i : parseUnits(line.replaceAll("auswärts", "").trim())) {
                        //own units in other village
                        //ignore for now
                    }
                } else if (line.startsWith("unterwegs")) {
                    int[] underway = parseUnits(line.replaceAll("unterwegs", "").trim());
                    //own units on the way
                    for (int i = 0; i < underway.length; i++) {
                        troops.set(i, troops.get(i) + underway[i]);
                    }
                }
                villageLines--;
            } else {
                String nextToken = null;
                while (elemTok.hasMoreElements()) {
                    String currentToken = null;
                    if (nextToken == null) {
                        currentToken = elemTok.nextToken();
                    } else {
                        currentToken = nextToken;
                    }
                    if (elemTok.hasMoreTokens()) {
                        nextToken = elemTok.nextToken();
                    }

                    //search village
                    if (currentToken.startsWith("(") && currentToken.endsWith(")")) {
                        //check if we got a village

                        /* if (ServerSettings.getSingleton().getCoordType() != 2) {
                        if (currentToken.matches("\\([0-9]+\\:[0-9]+\\:[0-9]+\\)") && (nextToken != null) && (nextToken.startsWith("K"))) {
                        //extract village coordinates
                        String[] split = currentToken.trim().split("[(\\:)]");
                        int[] xy = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                        v = DataHolder.getSingleton().getVillages()[xy[0]][xy[1]];
                        villageLines = 4;
                        break;
                        }
                        } else {
                        if (currentToken.matches("\\([0-9]+\\|[0-9]+\\)") && (nextToken != null) && (nextToken.startsWith("K"))) {
                        //extract village coordinates
                        String[] split = currentToken.trim().split("[(\\|)]");
                        v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[1])][Integer.parseInt(split[2])];
                        //next 4 lines are village
                        villageLines = 4;
                        break;
                        }
                        }*/
                        try {
                            String coord = currentToken.substring(currentToken.lastIndexOf("(") + 1, currentToken.lastIndexOf(")"));
                            if (ServerSettings.getSingleton().getCoordType() != 2) {
                                String[] split = coord.trim().split("[(\\:)]");
                                int[] xy = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                                v = DataHolder.getSingleton().getVillages()[xy[0]][xy[1]];
                                villageLines = 4;
                                break;
                            } else {
                                String[] split = coord.trim().split("[(\\|)]");
                                v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
                                villageLines = 4;
                                break;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
            if (villageLines == 0) {
                if ((v != null) && (troops.size() == DataHolder.getSingleton().getUnits().size())) {
                    //add troops to manager
                    TroopsManager.getSingleton().addTroopsForVillageFast(v, new LinkedList<Integer>(troops));
                    troops.clear();
                    v = null;
                    //found at least one village, so retValue is true    
                    retValue = true;
                } else {
                    v = null;
                    troops.clear();
                }
            }
        }
        TroopsManager.getSingleton().forceUpdate();
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
            TroopsParser.parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    /* String token = "(120|192)";
    System.out.println(token.matches("\\([0-9]+\\|[0-9]+\\)"));
     */
    // TroopsParser.parse(pTroopsString);
    }
    /*
    kirscheye3	435|447 FaNtAsY wOrLd ... <3	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:06:46
    02.10.08 23:41:33
    Torridity	437|445 FaNtAsY wOrLd ... 10	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:26:00
    02.10.08 23:41:33
    Torridity	438|445 Barbarendorf (12)	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:16:57
    02.10.08 23:41:33
    Torridity	439|445 Barbarendorf (13)	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:06:46
    02.10.08 23:41:33
    
     */
    /*
    LGK88 (1) (458|465) K44  
    eigene	0	0	6000	0	2300	0	300	50	0	Befehle
    im Dorf	0	0	6000	0	2300	0	300	50	0	0	Truppen
    auswärts	0	0	0	0	0	0	0	0	0	0	0	0
    unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle 
     */
}
