/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.NotifierFrame;
import de.tor.tribes.ui.models.ReportManagerTableModel;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.report.ReportManager;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * @TODO (Diff) Fixed report parser for old worlds
 * @author Torridity
 */
public class ReportParser implements SilentParserInterface {

    public boolean parse( String pData ) {
	try {
	    FightReport r = parseReport(pData);

	    if ( !r.isValid() ) {
		throw new Exception("No valid report data found");
	    }

	    String activeSet = ReportManagerTableModel.getSingleton().getActiveReportSet();
	    ReportManager.getSingleton().getReportSet(activeSet).addReport(r);
	    NotifierFrame.doNotification("DS Workbench hat einen Kampfbericht erfolgreich eingelesen.", NotifierFrame.NOTIFY_INFO);
	    ReportManager.getSingleton().forceUpdate(activeSet);
	    return true;
	} catch ( Exception e ) {
	    //no valid report data found
	}
	return false;
    }

    private static FightReport parseReport( String pData ) {
	StringTokenizer t = new StringTokenizer(pData, "\n");
	boolean luckPart = false;
	boolean attackerPart = false;
	boolean defenderPart = false;
	boolean troopsOnTheWayPart = false;
	boolean troopsOutside = false;
	boolean haveTime = false;
	int serverTroopCount = DataHolder.getSingleton().getUnits().size();
	FightReport result = new FightReport();
	while ( t.hasMoreTokens() ) {
	    String line = t.nextToken().trim();
	    if ( line.startsWith("Gesendet") ) {
		line = line.replaceAll("Gesendet", "").trim();
		SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		try {
		    Date d = f.parse(line);
		    result.setTimestamp(d.getTime());
		    //System.out.println("Sent " + f.format(new Date(result.getTimestamp())));
		    haveTime = true;
		} catch ( Exception e ) {
		    result.setTimestamp(0l);
		}
	    } else if ( line.startsWith("Der Angreifer hat gewonnen") ) {
		//System.out.println("Won");
		result.setWon(true);
	    } else if ( line.startsWith("Der Verteidiger hat gewonnen") ) {
		//System.out.println("Lost");
		result.setWon(false);
	    } else if ( line.startsWith("Glück") ) {
		//System.out.println("Luck");
		line = line.replaceAll("Glück \\(aus Sicht des Angreifers\\)", "").replaceAll("Glück \\(aus Sicht des Verteidigers\\)", "").trim();
		if ( line.indexOf("%") > 0 ) {
		    //negative luck is in same line, try it!
		    try {
			double luck = Double.parseDouble(line.replaceAll("Glück", "").replaceAll("%", "").trim());
			result.setLuck(luck);
		    } catch ( Exception e ) {
			//e.printStackTrace();
			result.setLuck(0.0);
		    }
		    luckPart = false;
		} else {
		    //probably positive luck, handle with next line
		    luckPart = true;
		}
	    } else if ( line.startsWith("Glück (aus Sicht des Verteidigers)") ) {
		//System.out.println("LuckPart");
		luckPart = true;
	    } else if ( line.startsWith("Moral") ) {
		//System.out.println("Moral");
		line = line.replaceAll("Moral:", "").trim().replaceAll("%", "");
		if ( line.indexOf("Angreifer") > -1 ) {
		    //Opera only -.-
		    attackerPart = true;
		    int attackerPos = line.indexOf("Angreifer");
		    String attacker = line.substring(attackerPos).replaceAll("Angreifer:", "").trim();
		  //  System.out.println("Attacker2 " + attacker);
		    result.setAttacker(DataHolder.getSingleton().getTribeByName(attacker));
		    line = line.substring(0, attackerPos);
		}
		try {
		    double moral = Double.parseDouble(line);
		 //   System.out.println("Moral: " + moral);
		    result.setMoral(moral);
		} catch ( Exception e ) {
		}
	    } else if ( line.startsWith("Angreifer") || line.indexOf("Angreifer") > -1 ) {
		attackerPart = true;
		line = line.replaceAll("Angreifer:", "").trim();
		//System.out.println("Attacker2 " + line);
		result.setAttacker(DataHolder.getSingleton().getTribeByName(line));
	    } else if ( line.startsWith("Dorf") || line.startsWith("Herkunft") || line.startsWith("Ziel") ) {
		line = line.replaceAll("Dorf:", "").replaceAll("Herkunft:", "").replaceAll("Ziel:", "").trim();
		//System.out.println("Village " + line);
		if ( attackerPart ) {
		    result.setSourceVillage(new VillageParser().parse(line).get(0));
		} else if ( defenderPart ) {
		    result.setTargetVillage(new VillageParser().parse(line).get(0));
		}
	    } else if ( line.startsWith("Anzahl") ) {
		line = line.replaceAll("Anzahl:", "").trim();
		//System.out.println("Amoi" + line);
		if ( attackerPart ) {
		    String[] troops = line.split("\t");
		    if ( troops.length == serverTroopCount ) {
			result.setAttackers(parseUnits(troops));
		    }
		} else if ( defenderPart ) {
		    String[] troops = line.split("\t");
		    if ( troops.length == serverTroopCount ) {
			result.setDefenders(parseUnits(troops));
		    }
		}
	    } else if ( line.startsWith("Verluste") ) {
		line = line.replaceAll("Verluste:", "").trim();
		if ( attackerPart ) {
		    String[] troops = line.split("\t");
		    if ( troops.length == serverTroopCount ) {
			result.setDiedAttackers(parseUnits(troops));
			attackerPart = false;
		    }
		} else if ( defenderPart ) {
		    String[] troops = line.split("\t");
		    if ( troops.length == serverTroopCount ) {
			result.setDiedDefenders(parseUnits(troops));
		    }
		}
	    } else if ( line.startsWith("Verteidiger") ) {
		defenderPart = true;
		line = line.replaceAll("Verteidiger:", "").trim();
		//System.out.println("Def " + line);
		result.setDefender(DataHolder.getSingleton().getTribeByName(line));
	    } else if ( line.startsWith("Schaden durch Rammböcke") ) {
		line = line.replaceAll("Schaden durch Rammböcke:", "").trim();
		line = line.replaceAll("Wall beschädigt von Level", "").trim().replaceAll("auf Level", "");
		StringTokenizer wallT = new StringTokenizer(line, " \t");
		try {
		    result.setWallBefore(Byte.parseByte(wallT.nextToken()));
		    result.setWallAfter(Byte.parseByte(wallT.nextToken()));
		} catch ( Exception e ) {
		    result.setWallBefore((byte) -1);
		    result.setWallAfter((byte) -1);
		}
	    } else if ( line.startsWith("Schaden durch Katapultbeschuss") ) {
		//Schaden durch Katapultbeschuss: Wall beschädigt von Level 8 auf Level 7
		line = line.replaceAll("Schaden durch Katapultbeschuss:", "").trim().replaceAll("Level", "");
		//Wall beschädigt von 8 auf 7
		StringTokenizer cataT = new StringTokenizer(line, " ");
		String target = cataT.nextToken();
		//"damaged" token
		cataT.nextToken();
		//"from" token
		cataT.nextToken();
		try {
		    byte buildingBefore = Byte.parseByte(cataT.nextToken());
		    //"to" token
		    cataT.nextToken();
		    byte buildingAfter = Byte.parseByte(cataT.nextToken());
		    result.setAimedBuilding(target);
		    result.setBuildingBefore(buildingBefore);
		    result.setBuildingAfter(buildingAfter);
		} catch ( Exception e ) {
		}
	    } else if ( line.startsWith("Veränderung der Zustimmung") || line.startsWith("Zustimmung:") ) {
		line = line.replaceAll("Veränderung der Zustimmung", "").trim().replaceAll("Zustimmung gesunken von", "").replaceAll("auf", "");
		//version 6.0
		line = line.replaceAll("Zustimmung:", "").replaceAll("Gesunken von", "");
		StringTokenizer acceptT = new StringTokenizer(line, " \t");
		try {
		    result.setAcceptanceBefore(Byte.parseByte(acceptT.nextToken()));
		    result.setAcceptanceAfter(Byte.parseByte(acceptT.nextToken()));
		} catch ( Exception e ) {
		    result.setAcceptanceBefore((byte) 100);
		    result.setAcceptanceAfter((byte) 100);
		}
	    } else if ( line.startsWith("Truppen des Verteidigers, die unterwegs waren") ) {
		troopsOnTheWayPart = true;
	    } else if ( line.startsWith("Truppen des Verteidigers in anderen Dörfern") ) {
		troopsOutside = true;
	    } else if ( line.startsWith("Durch Besitzer des Berichts verborgen") ) {
		if ( attackerPart ) {
		    String[] unknownAttackers = new String[serverTroopCount];
		    for ( int i = 0; i < serverTroopCount; i++ ) {
			unknownAttackers[i] = "-1";
		    }
		    result.setAttackers(parseUnits(unknownAttackers));
		    result.setDiedAttackers(parseUnits(unknownAttackers));
		    attackerPart = false;
		} else {
		    String[] unknownDefender = new String[serverTroopCount];
		    for ( int i = 0; i < serverTroopCount; i++ ) {
			unknownDefender[i] = "-1";
		    }
		    result.setDefenders(parseUnits(unknownDefender));
		    result.setDiedDefenders(parseUnits(unknownDefender));
		    defenderPart = false;
		}
	    } else if ( line.startsWith("Keiner deiner Kämpfer ist lebend zurückgekehrt") ) {
		defenderPart = false;
		String[] unknownDefender = new String[serverTroopCount];
		for ( int i = 0; i < serverTroopCount; i++ ) {
		    unknownDefender[i] = "-1";
		}
		result.setDefenders(parseUnits(unknownDefender));
		result.setDiedDefenders(parseUnits(unknownDefender));
	    } else {
		if ( !haveTime ) {
		    line = line.trim();
		    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		    try {
			Date d = f.parse(line);
			result.setTimestamp(d.getTime());
			haveTime = true;
		    } catch ( Exception e ) {
			result.setTimestamp(0l);
		    }
		}

		if ( troopsOnTheWayPart ) {
		    String[] troops = line.split("\t");
		    if ( troops.length == serverTroopCount ) {
			troopsOnTheWayPart = false;
			result.setDefendersOnTheWay(parseUnits(troops));
		    }
		} else if ( troopsOutside ) {
		    try {
			Village v = new VillageParser().parse(line).get(0);
			if ( v == null ) {
			    throw new Exception();
			}
			line = line.substring(line.indexOf("\t")).trim();
			String[] troops = line.split("\t");
			if ( troops.length == serverTroopCount ) {
			    result.addDefendersOutside(v, parseUnits(troops));
			}
		    } catch ( Exception e ) {
			//no additional troops outside
			troopsOutside = false;
		    }
		} else if ( luckPart ) {
		    if ( line.indexOf("%") > 0 ) {
			luckPart = false;
			try {
			    // System.out.println("LuckLine " + line);
			    double luck = Double.parseDouble(line.replaceAll("Pech", "").replaceAll("%", "").trim());
			    //System.out.println("L " + luck);
			    result.setLuck(luck);
			} catch ( Exception e ) {
			    e.printStackTrace();
			    result.setLuck(0.0);
			}
		    }
		}
	    }
	}
	return result;
    }

    private static Hashtable<UnitHolder, Integer> parseUnits( String[] pUnits ) {
	int cnt = 0;
	Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
	for ( UnitHolder unit : DataHolder.getSingleton().getUnits() ) {
	    units.put(unit, Integer.parseInt(pUnits[cnt]));
	    cnt++;

	}


	return units;
    }

    public static void main( String[] args ) {
	//  ReportParser.parseReport();
        /*  String test = "1\t2\t3\t4\t5";
	String[] split = test.split("\t");
	for(String t : split){
	System.out.println(t);
	}*/ try {
	    Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
	    String data = (String) t.getTransferData(DataFlavor.stringFlavor);
	    new ReportParser().parse(data);
	} catch ( Exception e ) {
	}
    }

}
