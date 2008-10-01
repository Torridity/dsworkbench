/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.troops.TroopsManager;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Charon
 */
public class TroopsParser {

    /*
    003 | Spitfire (471|482) K44  
    eigene	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Befehle
    im Dorf	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Truppen
    auswärts	0	0	0	0	0	0	0	0	0	0	0	0
    unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
    2Fast4You (475|480) K44  
    eigene	600	500	0	0	134	0	0	354	0	0	0	1	Befehle
    im Dorf	600	500	0	0	134	0	0	354	0	0	0	1	Truppen
    auswärts	4400	3000	0	3000	66	0	0	1046	0	0	0	0
    unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
     */
    public static void parse(String pTroopsString) {
        StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r");
        int villageLines = -1;
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
                    for (int i : parseUnits(line.replaceAll("unterwegs", "").trim())) {
                        //own units on the way
                        //ignore for now
                    }
                }
                villageLines--;
            } else {
                while (elemTok.hasMoreElements()) {
                    String currentToken = elemTok.nextToken();
                    //search village
                    if (currentToken.startsWith("(") && currentToken.endsWith(")")) {
                        //got village
                        if (currentToken.matches("\\([0-9]+\\|[0-9]+\\)")) {
                            String[] split = currentToken.trim().split("[(\\|)]");
                            /* for(String s : split){
                            System.out.println(s);
                            }*/
                            v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[1])][Integer.parseInt(split[2])];
                            // System.out.println(v);
                            //next 4 lines are village
                            villageLines = 4;
                            break;
                        }
                    }
                }
            }
            if (villageLines == 0) {
                //add to manager and reset all
                TroopsManager.getSingleton().addTroopsForVillage(v, troops);
                troops.clear();
                v = null;
            }
        }
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

        /* Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
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
        }*/
        String token = "(120|192)";
        System.out.println(token.matches("\\([0-9]+\\|[0-9]+\\)"));

    //TroopsParser.parse(pTroopsString);
    }
}
