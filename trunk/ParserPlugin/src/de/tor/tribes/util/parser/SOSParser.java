/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GenericParserInterface;
import de.tor.tribes.util.ServerSettings;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class SOSParser implements GenericParserInterface<SOSRequest> {

    private boolean debug = true;
    private static Logger logger = Logger.getLogger("SOSParser");
    /*
    [b]Verteidiger[/b]
    Name: [player]Rattenfutter[/player]
    Stamm: [ally][KdS][/ally]
    Punkte: 1887516
    
    [b]Angegriffenes Dorf[/b]
    [coord]444|867[/coord]
    Punkte: 10000
    Stufe des Walls: 20
    
    
    [b]Anwesende Truppen[/b]
    [unit]axe[/unit] 8000
    [unit]spy[/unit] 6
    
    [b]1. Angriff[/b]
    Angreifer: [player]Rattenfutter[/player]
    Stamm: [ally][KdS][/ally]
    Punkte: 1887516
    Herkunft: [coord]444|868[/coord]
    Ankunftszeit: 17.04.10 18:30:10:935
    [b]Angegriffenes Dorf[/b]
    [coord]443|871[/coord]
    Punkte: 10014
    Stufe des Walls: 20
    
    
    [b]Anwesende Truppen[/b]
    [unit]axe[/unit] 41
    [unit]spy[/unit] 45
    [unit]ram[/unit] 8
    
    [b]1. Angriff[/b]
    Angreifer: [player]Rattenfutter[/player]
    Stamm: [ally][KdS][/ally]
    Punkte: 1887516
    Herkunft: [coord]444|868[/coord]
    Ankunftszeit: 17.04.10 18:44:00:931
     
     [b]2. Angriff[/b]
    Angreifer: [player]Rattenfutter[/player]
    Stamm: [ally][KdS][/ally]
    Punkte: 1887516
    Herkunft: [coord]444|867[/coord]
    Ankunftszeit: 17.04.10 18:44:00:931
     */

    public List<SOSRequest> parse(String pData) {
        print("Start parsing SOS request");
        List<SOSRequest> requests = new LinkedList<SOSRequest>();
        try {
            Hashtable<Tribe, SOSRequest> parsedData = parseRequests(pData);
            if (parsedData.isEmpty()) {
                print("Check short version");
                parsedData = parseRequestsShort(pData);
            } else {
                print("Got results for long version");
            }
            Enumeration<Tribe> keys = parsedData.keys();
            while (keys.hasMoreElements()) {
                requests.add(parsedData.get(keys.nextElement()));
            }
        } catch (Exception e) {
        }
        return requests;
    }

    private Hashtable<Tribe, SOSRequest> parseRequests(String pData) {
        String[] lines = pData.split("\n");
        Hashtable<Tribe, SOSRequest> requests = new Hashtable<Tribe, SOSRequest>();
        SOSRequest currentRequest = null;
        boolean waitForTarget = false;
        boolean waitForTroops = false;
        Village source = null;
        Village target = null;

        SimpleDateFormat dateFormat = null;
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            dateFormat = new SimpleDateFormat(ParserVariableManager.getSingleton().getProperty("sos.date.format"));
        } else {
            dateFormat = new SimpleDateFormat(ParserVariableManager.getSingleton().getProperty("sos.date.format.ms"));
        }

        for (String line : lines) {
            if (line.indexOf(ParserVariableManager.getSingleton().getProperty("sos.defender")) > -1) {
                //start new support request
                if (currentRequest != null && currentRequest.getDefender() != null) {
                    requests.put(currentRequest.getDefender(), currentRequest);
                }

                currentRequest = new SOSRequest();
                //  System.out.println("Create new reuqest");
            } else if (line.indexOf(ParserVariableManager.getSingleton().getProperty("sos.name")) > -1) {
                //defender name
                String defender = line.replaceAll(ParserVariableManager.getSingleton().getProperty("sos.name"), "").replaceAll("\\[player\\]", "").replaceAll("\\[\\/player\\]", "").trim();
                // System.out.println("Defender: " + defender);
                Tribe defTribe = DataHolder.getSingleton().getTribeByName(defender);
                if (requests.get(defTribe) != null) {
                    currentRequest = requests.get(defTribe);
                } else {
                    currentRequest.setDefender(defTribe);
                    requests.put(defTribe, currentRequest);
                }
            } else if (line.indexOf(ParserVariableManager.getSingleton().getProperty("sos.destination")) > -1) {
                //new target village
                waitForTarget = true;
                //  System.out.println("Wait for target");
            } else if (line.indexOf(ParserVariableManager.getSingleton().getProperty("sos.source")) > -1) {
                List<Village> sourceVillage = new VillageParser().parse(line);
                if (sourceVillage.size() > 0) {
                    //set target
                    source = sourceVillage.get(0);
                    //System.out.println("source: " + source);
                }
            } else if (line.indexOf(ParserVariableManager.getSingleton().getProperty("sos.arrive.time")) > -1) {
                //arrive time
                try {
                    String arrive = line.replaceAll(ParserVariableManager.getSingleton().getProperty("sos.arrive.time"), "").trim();
                    Date arriveDate = dateFormat.parse(arrive);
                    // System.out.println("Got arrive");
                    currentRequest.getTargetInformation(target).addAttack(source, arriveDate);
                } catch (Exception e) {
                    //  System.out.println("NO ARRIVE!!");
                }
            } else if (line.indexOf(ParserVariableManager.getSingleton().getProperty("sos.wall.level")) > -1) {
                try {
                    int wallLevel = Integer.parseInt(line.replaceAll(ParserVariableManager.getSingleton().getProperty("sos.wall.level"), "").trim());
                    currentRequest.getTargetInformation(target).setWallLevel(wallLevel);
                } catch (Exception e) {
                }
            } else if (line.indexOf(ParserVariableManager.getSingleton().getProperty("sos.troops.in.village")) > -1) {
                // System.out.println("WAIT FOR TROOPS");
                waitForTroops = true;
            } else {
                if (waitForTarget) {
                    List<Village> targetVillage = new VillageParser().parse(line);
                    if (targetVillage.size() > 0) {
                        //set target
                        target = targetVillage.get(0);
                        currentRequest.addTarget(target);
                        // System.out.println("Got target " + target);
                        waitForTarget = false;
                    }
                } else if (waitForTroops) {
                    //System.out.println("WAIT TROOPS " + line);
                    try {
                        String plainUnit = line.replaceAll("\\[unit\\]", "").replaceAll("\\[\\/unit\\]", "").trim();
                        //System.out.println("UNIT: " + plainUnit);
                        if (plainUnit.trim().length() != 0) {
                            //skip empty line

                            String[] unitAmount = plainUnit.split(" ");
                            UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(unitAmount[0]);

                            //System.out.println("UNIT : " + u);
                            Integer amount = Integer.parseInt(unitAmount[1]);
                            // System.out.println("AM " + amount);
                            currentRequest.getTargetInformation(target).addTroopInformation(u, amount);
                        }
                    } catch (Exception e) {
                        waitForTroops = false;
                    }
                }
            }
        }

        return requests;
    }

    /**
    [b]Dorf:[/b] [coord]454|943[/coord]
    [b]Wallstufe:[/b] 20
    [b]Verteidiger:[/b]  18314  13982  0  18659  353  0  0  4825  0  17  0  0
    
    LKAV?, 20 Checkpoint (465|883) , K84 [coord]465|883[/coord] --> Ankunftszeit: 24.08.11 23:38:53:674 [player]Frank R.[/player]
    Ramm, 31 dieBrüder 02 (460|888) , K84 [coord]460|888[/coord] --> Ankunftszeit: 25.08.11 17:14:33:064 [player]Frank R.[/player]
    
    [b]Dorf:[/b] [coord]453|943[/coord]
    [b]Wallstufe:[/b] 20
    [b]Verteidiger:[/b]  14998  13360  5929  14998  100  2809  260  3000 300  40  0  0
    
    Ramm, 22 Checkpoint (452|898) , K84 [coord]452|898[/coord] --> Ankunftszeit: 25.08.11 12:01:23:952 [player]Frank R.[/player]
    
    [b]Dorf:[/b] [coord]452|944[/coord]
    [b]Wallstufe:[/b] 20
    [b]Verteidiger:[/b]  18581  13354  0  18581  410  0  0  4994  0  19  0  0
    
    Ramm, 42 The White Knigth 04 (448|894) , K84 [coord]448|894[/coord] --> Ankunftszeit: 25.08.11 14:46:51:846 [player]Frank R.[/player]
    
    ================================================
    Dorf: Just4Testing (454|943) K55
    Wallstufe: 20
    Verteidiger: 18314 13982 0 18659 353 0 0 4825 0 17 0 0
    
    LKAV?, 20 Checkpoint (465|883) , K84 Just4Testing (465|883) K55 --> Ankunftszeit: 24.08.11 23:38:53:674 Frank R.
    Ramm, 31 dieBrüder 02 (460|888) , K84 Just4Testing (460|888) K55 --> Ankunftszeit: 25.08.11 17:14:33:064 Frank R.
    
    Dorf: Just4Testing (453|943) K55
    Wallstufe: 20
    Verteidiger: 14998 13360 5929 14998 100 2809 260 3000 300 40 0 0
    
    Ramm, 22 Checkpoint (452|898) , K84 Just4Testing (452|898) K55 --> Ankunftszeit: 25.08.11 12:01:23:952 Torridity
    
    Dorf: Just4Testing (452|944) K55
    Wallstufe: 20
    Verteidiger: 18581 13354 0 18581 410 0 0 4994 0 19 0 0
    
    Ramm, 42 The White Knigth 04 (448|894) , K84 Just4Testing (448|894) K55 --> Ankunftszeit: 25.08.11 14:46:51:846 Torridity
    
     */
    private Hashtable<Tribe, SOSRequest> parseRequestsShort(String pData) {
        String[] lines = pData.split("\n");
        Hashtable<Tribe, SOSRequest> requests = new Hashtable<Tribe, SOSRequest>();
        Village destination = null;
        SOSRequest request = null;
        SimpleDateFormat dateFormat = null;
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            dateFormat = new SimpleDateFormat(ParserVariableManager.getSingleton().getProperty("sos.date.format"));
        } else {
            dateFormat = new SimpleDateFormat(ParserVariableManager.getSingleton().getProperty("sos.date.format.ms"));
        }

        for (String line : lines) {
            line = line.trim();
            if (line.contains("Dorf:")) {
                print("Village line '" + line + "'");
                if (request != null && destination != null) {
                    print("Store last request");
                    requests.put(destination.getTribe(), request);
                }

                destination = null;
                List<Village> villages = new VillageParser().parse(line);
                //check if there is a village in the line
                if (!villages.isEmpty()) {
                    //got destination
                    destination = villages.get(villages.size() - 1);
                    print("Destination: " + destination);
                    //check for existing request
                    request = requests.get(destination.getTribe());

                    if (request == null) {
                        print("New request");
                        //create new request
                        request = new SOSRequest(destination.getTribe());
                        requests.put(destination.getTribe(), request);
                    }
                    print("Adding target " + destination);
                    request.addTarget(destination);
                }
            }


            if (destination != null) {
                print("Check destination in '" + line + "'");
                if (line.contains("Wallstufe:")) {
                    print("Check wall in line '" + line + "'");
                    String wallSplit[] = line.split(" ");
                    if (wallSplit != null && wallSplit.length >= 2) {
                        print("Check for valid wall");
                        try {
                            Integer wall = Integer.parseInt(wallSplit[wallSplit.length - 1]);
                            print("Wall: " + wall);
                            request.getTargetInformation(destination).setWallLevel(wall);
                        } catch (Exception e) {
                            print("Failed to get Wall " + e.getMessage());
                        }
                    } else {
                        print("Invalid wall entry '" + wallSplit + "'");
                    }
                } else if (line.contains("Verteidiger:")) {
                    print("Get units from line '" + line + "'");
                    int[] units = parseUnits(line);
                    if (units.length != 0) {
                        print("Valid units, add to destination");
                        int cnt = 0;
                        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                            request.getTargetInformation(destination).addTroopInformation(unit, units[cnt]);
                            cnt++;
                        }
                    }
                } else if (line.contains("-->")) {
                    //got attack?
                    print("Check attack in line '" + line + "'");
                    String[] attackSplit = line.split("-->");
                    if (attackSplit != null && attackSplit.length >= 2) {
                        print("Try to get attacker in split '" + attackSplit[0]);
                        List<Village> sources = new VillageParser().parse(attackSplit[0]);
                        if (!sources.isEmpty()) {
                            print("Got source");
                            Village source = sources.get(sources.size() - 1);
                            Date arrive = null;

                            try {
                                String[] arriveSplit = attackSplit[1].trim().split(" ");
                                if (arriveSplit != null && arriveSplit.length >= 4) {
                                    print("Try to check arrive time");
                                    String arriveValue = arriveSplit[1] + " " + arriveSplit[2];
                                    arrive = dateFormat.parse(arriveValue);
                                } else {
                                    print("Invalid arrive '" + attackSplit[1]);
                                }
                            } catch (Exception e) {
                                print("Failed to parse date (" + e.getMessage() + ")");
                            }
                            if (source != null && arrive != null) {
                                request.getTargetInformation(destination).addAttack(source, arrive);
                            }
                        }
                    } else {
                        print("Invalid split");
                    }
                }
            }
        }

        if (request != null && destination != null) {
            print("Store last request");
            requests.put(destination.getTribe(), request);
        }
        return requests;
    }

    private static int[] parseUnits(String pLine) {
        String line = pLine.replaceAll(ParserVariableManager.getSingleton().getProperty("troops.own"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops.commands"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops"), "");
        StringTokenizer t = new StringTokenizer(line, " \t");
        int uCount = DataHolder.getSingleton().getUnits().size();
        int[] units = new int[uCount];
        int cnt = 0;
        while (t.hasMoreTokens()) {
            try {
                units[cnt] = Integer.parseInt(t.nextToken());
                cnt++;
            } catch (Exception e) {
                //token with no troops
            }
        }
        if (cnt < uCount) {
            return new int[]{};
        }
        return units;
    }

    private void print(String pText) {
        if (logger.isDebugEnabled()) {
            logger.debug(pText);
        }

        /*if (debug) {
        System.out.println(pText);
        }*/
    }

    public static Hashtable<Tribe, SOSRequest> attacksToSOSRequests(List<Attack> pAttacks) {
        Hashtable<Tribe, SOSRequest> requests = new Hashtable<Tribe, SOSRequest>();

        for (Attack attack : pAttacks) {
            Tribe defender = attack.getTarget().getTribe();
            SOSRequest requestForDefender = requests.get(defender);
            if (requestForDefender == null) {
                requestForDefender = new SOSRequest(defender);
                requests.put(defender, requestForDefender);
            }
            requestForDefender.addTarget(attack.getTarget());
            SOSRequest.TargetInformation targetInfo = requestForDefender.getTargetInformation(attack.getTarget());
            targetInfo.addAttack(attack.getSource(), attack.getArriveTime());
        }
        return requests;
    }

    public static void main(String[] args) {
        try {
            Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            new SOSParser().parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
