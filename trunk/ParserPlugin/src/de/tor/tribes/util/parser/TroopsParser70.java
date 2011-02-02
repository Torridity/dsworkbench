/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.NotifierFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author Charon
 */
public class TroopsParser70 implements SilentParserInterface {

    /*
    003 | Spitfire (471|482) K44  
    eigene	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Befehle
    im Dorf	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Truppen
    auswärts	0	0	0	0	0	0	0	0	0	0	0	0
    unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle
     */

    /*Barbarendorf (452|787) K74  	eigene	0	0	5493	0	200	2449	257	0	300	30	0	1	Befehle
    im Dorf	15000	15000	5493	15000	950	2449	257	5000	300	30	0	1	Truppen
    auswärts	0	0	0	0	0	0	0	0	0	0	0	0
    unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle

     */
    public boolean parse( String pTroopsString ) {
	StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r");
	int villageLines = -1;
	boolean retValue = false;
	int foundTroops = 0;
	//boolean haveVillage = false;
	Village v = null;
	String line = null;
	// List<Integer> troops = new LinkedList<Integer>();
	Hashtable<UnitHolder, Integer> ownTroops = new Hashtable<UnitHolder, Integer>();
	Hashtable<UnitHolder, Integer> troopsInVillage = new Hashtable<UnitHolder, Integer>();
	Hashtable<UnitHolder, Integer> troopsOutside = new Hashtable<UnitHolder, Integer>();
	Hashtable<UnitHolder, Integer> troopsOnTheWay = new Hashtable<UnitHolder, Integer>();
	while ( lineTok.hasMoreElements() ) {

	    //parse single line for village
	    if ( line == null ) {
		line = lineTok.nextToken();
	    }
	    //tokenize line by tab and space
	    //  StringTokenizer elemTok = new StringTokenizer(line, " \t");
	    //parse single line for village
	    if ( v != null ) {
		//parse 4 village lines!
		line = line.trim();
		if ( line.trim().indexOf(ParserVariableManager.getSingleton().getProperty("troops.own")) > -1 ) {
		    int cnt = 0;
		    for ( int i : parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.own"))).replaceAll(ParserVariableManager.getSingleton().getProperty("troops.own"), "").trim()) ) {
			//own units in village
			//troops.add(i);
			ownTroops.put(DataHolder.getSingleton().getUnits().get(cnt), i);
			cnt++;
		    }
		} else if ( line.trim().indexOf(ParserVariableManager.getSingleton().getProperty("troops.in.village")) > -1 ) {
		    int cnt = 0;
		    for ( int i : parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.in.village"))).replaceAll(ParserVariableManager.getSingleton().getProperty("troops.in.village"), "").trim()) ) {
			//all units in village
			troopsInVillage.put(DataHolder.getSingleton().getUnits().get(cnt), i);
			cnt++;
		    }
		} else if ( line.trim().indexOf(ParserVariableManager.getSingleton().getProperty("troops.outside")) > -1 ) {
		    int cnt = 0;
		    for ( int i : parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.outside"))).replaceAll(ParserVariableManager.getSingleton().getProperty("troops.outside"), "").trim()) ) {
			//own units in other village
			troopsOutside.put(DataHolder.getSingleton().getUnits().get(cnt), i);
			cnt++;
		    }
		} else if ( line.trim().indexOf(ParserVariableManager.getSingleton().getProperty("troops.on.the.way")) > -1 ) {
		    // int[] underway = parseUnits(line.replaceAll("unterwegs", "").trim());
		    int cnt = 0;
		    //own units on the way
		    for ( int i : parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.on.the.way"))).replaceAll(ParserVariableManager.getSingleton().getProperty("troops.on.the.way"), "").trim()) ) {
			//troops.set(i, troops.get(i) + underway[i]);
			troopsOnTheWay.put(DataHolder.getSingleton().getUnits().get(cnt), i);
			cnt++;
		    }
		}
		villageLines--;
		line = null;
	    } else {
		try {
		    Village current = new VillageParser().parse(line).get(0);
		    if ( current != null ) {
			v = current;
			villageLines = 4;
		    }
		} catch ( Exception e ) {
		    v = null;
		    villageLines = 0;
		    line = null;
		}
	    }
	    //add troops information
	    if ( villageLines == 0 ) {
		int troopsCount = DataHolder.getSingleton().getUnits().size();
		if ( (v != null)
		     && (ownTroops.size() == troopsCount)
		     && (troopsInVillage.size() == troopsCount)
		     && (troopsOutside.size() == troopsCount)
		     && (troopsOnTheWay.size() == troopsCount) ) {
		    //add troops to manager
		    TroopsManager.getSingleton().addTroopsForVillageFast(v, new LinkedList<Integer>());
		    VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
		    holder.setOwnTroops(ownTroops);
		    holder.setTroopsInVillage(troopsInVillage);
		    holder.setTroopsOutside(troopsOutside);
		    holder.setTroopsOnTheWay(troopsOnTheWay);
		    //troops.clear();
		    ownTroops.clear();
		    troopsInVillage.clear();
		    troopsOutside.clear();
		    troopsOnTheWay.clear();
		    v = null;
		    foundTroops++;
		    //found at least one village, so retValue is true
		    retValue = true;
		} else {
		    v = null;
		    troopsInVillage.clear();
		    troopsOutside.clear();
		    troopsOnTheWay.clear();
		    // troops.clear();
		}
	    }
	}
	if ( retValue ) {
	    NotifierFrame.doNotification("DS Workbench hat Truppeninformationen zu " + foundTroops + ((foundTroops == 1) ? " Dorf " : " Dörfern ") + " in die Truppenübersicht eingetragen.", NotifierFrame.NOTIFY_INFO);
	    TroopsManager.getSingleton().forceUpdate();
	}
	return retValue;
    }

    private static Village extractVillage( String pLine ) {
	//tokenize line by tab and space
	StringTokenizer elemTok = new StringTokenizer(pLine, " \t");
	//try to find village line
	String nextToken = null;
	while ( elemTok.hasMoreElements() ) {
	    String currentToken = null;
	    if ( nextToken == null ) {
		currentToken = elemTok.nextToken();
	    } else {
		currentToken = nextToken;
	    }
	    if ( elemTok.hasMoreTokens() ) {
		nextToken = elemTok.nextToken();
	    }

	    //search village
	    if ( currentToken.startsWith("(") && currentToken.endsWith(")") ) {
		//check if we got a village
		try {
		    String coord = currentToken.substring(currentToken.lastIndexOf("(") + 1, currentToken.lastIndexOf(")"));
		    if ( ServerSettings.getSingleton().getCoordType() != 2 ) {
			String[] split = coord.trim().split("[(\\:)]");
			int[] xy = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
			return DataHolder.getSingleton().getVillages()[xy[0]][xy[1]];
		    } else {
			String[] split = coord.trim().split("[(\\|)]");
			return DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
		    }
		} catch ( Exception e ) {
		}
	    }
	}
	return null;
    }

    private static int[] parseUnits( String pLine ) {
	String line = pLine.replaceAll(ParserVariableManager.getSingleton().getProperty("troops.own"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops.commands"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops"), "");
	StringTokenizer t = new StringTokenizer(line, " \t");
	int uCount = DataHolder.getSingleton().getUnits().size();
	int[] units = new int[uCount];
	int cnt = 0;
	while ( t.hasMoreTokens() ) {
	    try {
		units[cnt] = Integer.parseInt(t.nextToken());
		cnt++;
	    } catch ( Exception e ) {
		//token with no troops
	    }
	}
	if ( cnt < uCount ) {
	    return new int[]{};
	}
	return units;
    }

    public static void main( String[] args ) {

	//Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
	try {
	    String s = "Barbarendorf (452|787) K74  	eigene	0	0	5493	0	200	2449	257	0	300	30	0	1	Befehle"
		       + "im Dorf	15000	15000	5493	15000	950	2449	257	5000	300	30	0	1	Truppen"
		       + "auswärts	0	0	0	0	0	0	0	0	0	0	0	0"
		       + "unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle";

	    //  String data = (String) t.getTransferData(DataFlavor.stringFlavor);
	    new TroopsParser70().parse(s);
	} catch ( Exception e ) {
	    e.printStackTrace();
	}



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
