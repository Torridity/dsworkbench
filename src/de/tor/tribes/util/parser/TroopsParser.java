/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
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
            String line = lineTok.nextToken();
            StringTokenizer elemTok = new StringTokenizer(line, " \t");
            //  System.out.println("Line " + line);
            while (elemTok.hasMoreElements()) {
                String currentToken = elemTok.nextToken();
                //search village
                if (villageLines == 0) {
                    haveVillage = false;
                }
                if (haveVillage) {  
                    //next 4 lines are for village
                } else if (currentToken.startsWith("(") && currentToken.endsWith(")")) {
                    //got village
                    System.out.println("Village " + currentToken);
                    haveVillage = true;
                    //next 4 lines are village, the fifth is the current one
                    villageLines = 5;
                    break;
                }
            }
            if (haveVillage) {
                System.out.println("VillageLine " + line);
                villageLines--;
            }

        }
        System.out.println("\n");
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


            //String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            TroopsParser.parse(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    //TroopsParser.parse(pTroopsString);
    }
}
