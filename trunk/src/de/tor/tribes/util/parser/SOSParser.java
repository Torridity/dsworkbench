/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class SOSParser {
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

    public static Hashtable<Tribe, SOSRequest> parse(String pData) {
        String[] lines = pData.split("\n");
        Hashtable<Tribe, SOSRequest> requests = new Hashtable<Tribe, SOSRequest>();
        SOSRequest currentRequest = null;
        boolean waitForTarget = false;
        boolean waitForTroops = false;
        Village source = null;
        Village target = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss:SSS");
        for (String line : lines) {
            if (line.indexOf("Verteidiger") > -1) {
                //start new support request
                if (currentRequest != null) {
                    System.out.println("PUTTING REQUEST " + currentRequest);
                    requests.put(currentRequest.getDefender(), currentRequest);
                }

                currentRequest = new SOSRequest();
                //  System.out.println("Create new reuqest");
            } else if (line.indexOf("Name:") > -1) {
                //defender name
                String defender = line.replaceAll("Name:", "").replaceAll("\\[player\\]", "").replaceAll("\\[\\/player\\]", "").trim();
                // System.out.println("Defender: " + defender);
                Tribe defTribe = DataHolder.getSingleton().getTribeByName(defender);
                if (requests.get(defTribe) != null) {
                    currentRequest = requests.get(defTribe);
                } else {
                    currentRequest.setDefender(defTribe);
                    requests.put(defTribe, currentRequest);
                }
            } else if (line.indexOf("Angegriffenes Dorf") > -1) {
                //new target village
                waitForTarget = true;
                //  System.out.println("Wait for target");
            } else if (line.indexOf("Herkunft:") > -1) {
                List<Village> sourceVillage = VillageParser.parse(line);
                if (sourceVillage.size() > 0) {
                    //set target
                    source = sourceVillage.get(0);
                    //System.out.println("source: " + source);
                }
            } else if (line.indexOf("Ankunftszeit:") > -1) {
                //arrive time
                try {
                    String arrive = line.replaceAll("Ankunftszeit:", "").trim();
                    Date arriveDate = dateFormat.parse(arrive);
                    // System.out.println("Got arrive");
                    currentRequest.getTargetInformation(target).addAttack(source, arriveDate);
                } catch (Exception e) {
                    //  System.out.println("NO ARRIVE!!");
                }
            } else if (line.indexOf("Stufe des Walls:") > -1) {
                try {
                    int wallLevel = Integer.parseInt(line.replaceAll("Stufe des Walls:", "").trim());
                    currentRequest.getTargetInformation(target).setWallLevel(wallLevel);
                } catch (Exception e) {
                }
            } else if (line.indexOf("Anwesende Truppen") > -1) {
                // System.out.println("WAIT FOR TROOPS");
                waitForTroops = true;
            } else {
                if (waitForTarget) {
                    List<Village> targetVillage = VillageParser.parse(line);
                    if (targetVillage.size() > 0) {
                        //set target
                        target = targetVillage.get(0);
                        currentRequest.addTarget(target);
                        // System.out.println("Got target " + target);
                        waitForTarget = false;
                    }/*else{
                    System.out.println("NO TARGET in line " + line);
                    }*/
                } else if (waitForTroops) {
                    //System.out.println("WAIT TROOPS " + line);
                    try {
                        String plainUnit = line.replaceAll("\\[unit\\]", "").replaceAll("\\[\\/unit\\]", "").trim();
                        //    System.out.println("UNIT: " + plainUnit);
                        String[] unitAmount = plainUnit.split(" ");
                        UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(unitAmount[0]);
                        // System.out.println("UNIT : " + u);
                        Integer amount = Integer.parseInt(unitAmount[1]);
                        //  System.out.println("AM " + amount);
                        currentRequest.getTargetInformation(target).addTroopInformation(u, amount);
                    } catch (Exception e) {
                        waitForTroops = false;
                    }
                }
            }
        }

      /*  if (currentRequest != null) {
            System.out.println("Putting final Request");
            requests.put(currentRequest.getDefender(), currentRequest);
        }*/
        System.out.println(requests);
        return requests;
    }

    public static void main(String[] args) {
        try {
            Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            SOSParser.parse(data);
        } catch (Exception e) {
        }
    }
}
