/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class BruteForce {

    public static Hashtable<Village, Hashtable<Village, UnitHolder>> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pMaxCleanPerSnob,
            Date pStartTime,
            Date pArriveTime,
            int pMinTimeBetweenAttacks,
            int pTimeFrameStartHour,
            int pTimeFrameEndHour,
            boolean pNightBlock,
            boolean pRandomize) {
        Enumeration<UnitHolder> unitKeys = pSources.keys();
        Hashtable<Village, Hashtable<Village, UnitHolder>> attacks = new Hashtable<Village, Hashtable<Village, UnitHolder>>();
        List<Village> notAssigned = new LinkedList<Village>();
        Hashtable<Tribe, Integer> attacksPerTribe = new Hashtable<Tribe, Integer>();

        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> sources = pSources.get(unit);
            if (sources != null) {
                for (Village source : sources) {

                    //time when the fist attacks should begin
                    long minSendTime = pStartTime.getTime();
                    //time when the attacks should arrive
                    long arrive = pArriveTime.getTime();
                    //max. number of attacks per target village
                    int maxAttacksPerVillage = pMaxAttacksPerVillage;
                    Village vTarget = null;

                    //search all tribes and villages for targets
                    for (Village v : pTargets) {
                        double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());
                        long sendTime = arrive - (long) time * 1000;
                        //check if attack is somehow possible

                        if (sendTime > minSendTime) {
                            //check time frame
                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(sendTime);
                            int hour = c.get(Calendar.HOUR_OF_DAY);
                            int minute = c.get(Calendar.MINUTE);
                            int second = c.get(Calendar.SECOND);
                            boolean inTimeFrame = false;

                            int min = pTimeFrameStartHour;
                            int max = pTimeFrameEndHour;
                            if ((hour >= min) && ((hour <= max) && (minute <= 59) && (second <= 59))) {
                                inTimeFrame = true;
                            }

                            if (inTimeFrame) {
                                //only calculate if time is in time frame
                                //get list of source villages for current target
                                Hashtable<Village, UnitHolder> attacksForVillage = attacks.get(v);
                                if (attacksForVillage == null) {
                                    //no attack found for this village
                                    //get number of attacks on this tribe
                                    Integer cnt = pMaxAttacksPerVillage;
                                    if (cnt == null) {
                                        //no attacks on this tribe yet
                                        cnt = 0;
                                    }
                                    //create new table of attacks
                                    attacksForVillage = new Hashtable<Village, UnitHolder>();
                                    attacksForVillage.put(source, unit);
                                    attacks.put(v, attacksForVillage);
                                    attacksPerTribe.put(v.getTribe(), cnt + 1);
                                    vTarget = v;
                                } else {
                                    //there are already attacks on this village
                                    if (attacksForVillage.keySet().size() < maxAttacksPerVillage) {
                                        //more attacks on this village are allowed
                                        Integer cnt = attacksPerTribe.get(v.getTribe());
                                        if (cnt == null) {
                                            cnt = 0;
                                        }
                                        //max number of attacks neither for villages nor for player reached
                                        attacksForVillage.put(source, unit);
                                        attacksPerTribe.put(v.getTribe(), cnt + 1);
                                        vTarget = v;
                                    } else {
                                        //max number of attacks per village reached, continue search
                                        }
                                }
                            }
                        }
                        if (vTarget != null) {
                            break;
                        }
                    }

                    if (vTarget == null) {
                        notAssigned.add(source);
                    }
                }
            }
        }

        int validAttacks = 0;
        Enumeration<Village> villages = attacks.keys();
        while (villages.hasMoreElements()) {
            Village v = villages.nextElement();
            System.out.println("Attacked: " + v + ", Attacks: " + attacks.get(v).size());
            Hashtable<Village, UnitHolder> attackTable = attacks.get(v);
            Enumeration<Village> enu = attackTable.keys();
            while(enu.hasMoreElements()){
                System.out.println(" - " + enu.nextElement());
            }
            validAttacks += attacks.get(v).size();
        }
        System.out.println("AttackedVillages: " + attacks.size());
        System.out.println("Attacks: " + validAttacks);
        System.out.println("Not Assigned: " + notAssigned.size());
        return null;
    }
}
