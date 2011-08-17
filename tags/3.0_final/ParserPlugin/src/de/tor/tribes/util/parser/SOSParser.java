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
import de.tor.tribes.types.UnknownUnit;
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

/**
 *
 * @author Torridity
 */
public class SOSParser implements GenericParserInterface<SOSRequest> {
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
    [unit]axe[/unit] 5
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
     */

    public List<SOSRequest> parse(String pData) {
        Hashtable<Tribe, SOSRequest> parsedData = parseRequests(pData);
        Enumeration<Tribe> keys = parsedData.keys();
        List<SOSRequest> requests = new LinkedList<SOSRequest>();
        while (keys.hasMoreElements()) {
            requests.add(parsedData.get(keys.nextElement()));
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
                if (currentRequest != null) {
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
