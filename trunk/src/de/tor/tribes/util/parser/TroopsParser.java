/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import java.awt.Color;
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
        boolean haveVillage = false;
        
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            //tokenize line by tab and space
            StringTokenizer elemTok = new StringTokenizer(line, " \t");
            //parse single line for village
            if (haveVillage) {
                // System.out.println("VillageLine " + line);
                //parse 4 village lines!
                line = line.trim();
                if (line.startsWith("eigene")) {
                    System.out.println("EIGENE");
                    for (int i : parseUnits(line.replaceAll("eigene", "").trim())) {
                        System.out.println(i);
                    }
                } else if (line.startsWith("im Dorf")) {
                    System.out.println("IM D");
                    for (int i : parseUnits(line.replaceAll("im Dorf", "").trim())) {
                        System.out.println(i);
                    }
                } else if (line.startsWith("auswärts")) {
                    System.out.println("AUSW");
                    for (int i : parseUnits(line.replaceAll("auswärts", "").trim())) {
                        System.out.println(i);
                    }
                } else if (line.startsWith("unterwegs")) {
                    System.out.println("UNTER");
                    for (int i : parseUnits(line.replaceAll("unterwegs", "").trim())) {
                        System.out.println(i);
                    }
                }
                villageLines--;
            } else {
                while (elemTok.hasMoreElements()) {
                    String currentToken = elemTok.nextToken();
                    //search village
                    if (currentToken.startsWith("(") && currentToken.endsWith(")")) {
                        //got village
                        System.out.println("Village " + currentToken);
                        haveVillage = true;
                        //next 4 lines are village
                        villageLines = 4;
                        break;
                    }
                }
            }
            if (villageLines == 0) {
                haveVillage = false;
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
        System.out.println(Color.decode("#1905ff"));
        /*
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
         * 
         * */
    //TroopsParser.parse(pTroopsString);
    }
}
