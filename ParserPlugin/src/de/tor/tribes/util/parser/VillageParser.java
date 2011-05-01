/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GenericParserInterface;
import de.tor.tribes.util.ServerSettings;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Charon
 */
public class VillageParser implements GenericParserInterface<Village> {

    public List<Village> parse( String pVillagesString ) {
	List<Village> villages = new LinkedList<Village>();
	if ( pVillagesString == null ) {
	    return villages;
	}
	StringTokenizer lines = new StringTokenizer(pVillagesString, "\n\r");
	while ( lines.hasMoreTokens() ) {
	    if ( ServerSettings.getSingleton().getCoordType() != 2 ) {
		StringTokenizer t = new StringTokenizer(lines.nextToken(), " []\t");
		Village lastLineVillage = null;
		while ( t.hasMoreTokens() ) {
		    try {
			String token = t.nextToken();
			if ( token.matches("\\(*[0-9]{1,2}\\:[0-9]{1,2}\\:[0-9]{1,2}\\)*") ) {
			    token = token.replaceAll("\\(", "").replaceAll("\\)", "");
			    String[] split = token.split(":");
			    int[] coord = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
			    Village v = null;
			    if ( DataHolder.getSingleton().isDataValid() ) {
				v = DataHolder.getSingleton().getVillages()[coord[0]][coord[1]];
			    } else {
				v = new DummyVillage();
				v.setX((byte) coord[0]);
				v.setY((byte) coord[1]);
			    }
			    if ( v != null ) {
				lastLineVillage = v;
			    }
			}
		    } catch ( Exception e ) {
			//skip token
		    }
		}
		if ( lastLineVillage != null ) {
		    villages.add(lastLineVillage);
		}
	    } else {
		StringTokenizer t = new StringTokenizer(lines.nextToken(), " []\t");
		Village lastLineVillage = null;
		while ( t.hasMoreTokens() ) {
		    try {
			String token = t.nextToken();
			if ( token.matches("\\(*[0-9]{1,3}\\|[0-9]{1,3}\\)*") ) {
			    token = token.replaceAll("\\(", "").replaceAll("\\)", "");
			    String[] split = token.split("\\|");
			    // Village v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
			    Village v = null;
			    if ( DataHolder.getSingleton().isDataValid() ) {
				v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
			    } else {
				v = new DummyVillage();
				v.setX((byte) Integer.parseInt(split[0]));
				v.setY((byte) Integer.parseInt(split[1]));
			    }
			    if ( v != null ) {
				lastLineVillage = v;
			    }
			}
		    } catch ( Exception e ) {
			//skip token
		    }
		}
		if ( lastLineVillage != null ) {
		    villages.add(lastLineVillage);
		}
	    }
	}
	return villages;
    }

    public static void main( String[] args ) throws Exception {
	/*   Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
	System.out.println(VillageParser.parse((String) t.getTransferData(DataFlavor.stringFlavor)));
	 */
	String text = "[village](422|324)[/village]";
	System.out.println(text.matches(".{0,}\\({0,1}[0-9]{1,3}\\|[0-9]{1,3}\\){0,1}.{0,}"));
    }

}
