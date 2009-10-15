/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Enoblement;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Off;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class Optex extends AbstractAttackAlgorithm {

    private static Logger logger = Logger.getLogger("Algorithm_Optex");
    private List<Village> notAssignedSources = null;

    @Override
    public List<Village> getNotAssignedSources() {
        return notAssignedSources;
    }

    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pCleanPerSnob,
            TimeFrame pTimeFrame,
            boolean pRandomize,
            boolean pUse5Snobs) {


        List<Assignment> assi = new LinkedList<Assignment>();

        List<Village> sources = new LinkedList<Village>();

        for (Village target : pTargets) {
            Assignment a = new Assignment(target, (pUse5Snobs) ? 5 : 4, pCleanPerSnob, pMaxAttacksPerVillage);
            //assign offs
            UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
            List<Village> ramSources = pSources.get(ram);
            for (Village source : ramSources) {
                double dist = DSCalculator.calculateDistance(source, target);
                long dur = (long) (dist * ram.getSpeed() * 60000.0);
                Date send = new Date(pTimeFrame.getEnd() - dur);
                if (pTimeFrame.inside(send)) {
                    a.addOff(source);
                    sources.add(source);
                }
            }

            //assign fakes
            List<Village> ramSourcesFake = pFakes.get(ram);
            for (Village source : ramSourcesFake) {
                double dist = DSCalculator.calculateDistance(source, target);
                long dur = (long) (dist * ram.getSpeed() * 60000.0);
                Date send = new Date(pTimeFrame.getEnd() - dur);
                if (pTimeFrame.inside(send)) {
                    a.addFake(source);
                    sources.add(source);
                }
            }

            //assign snobs
            UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
            List<Village> snobSources = pSources.get(ram);
            for (Village source : snobSources) {
                double dist = DSCalculator.calculateDistance(source, target);
                long dur = (long) (dist * snob.getSpeed() * 60000.0);
                Date send = new Date(pTimeFrame.getEnd() - dur);
                if (pTimeFrame.inside(send)) {
                    a.addSnob(source);
                    sources.add(source);
                }
            }
        }//all assignments done...start solving






        List<Assignment> result = new LinkedList<Assignment>();
        //get all assignments which are already done
        for (Assignment a : assi) {
            if (a.isEnoblement()) {
                System.out.println("Done Enoblement");
                result.add(a);
            } else if (a.isOff()) {
                System.out.println("Done Off");
                result.add(a);
            } else if (a.isFake()) {
                System.out.println("Done Fake");
                result.add(a);
            }
        }

        for (Assignment a : result) {
            assi.remove(a);
        }

        solveAssignments(assi, result, sources, 0);

        return null;
    }

    public List<AbstractTroopMovement> calculate(
            List<Village> pOffSources,
            List<Village> pSnobSources,
            List<Village> pEnoblementTargets,
            List<Village> pFakeSources,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pCleanPerSnob,
            TimeFrame pTimeFrame,
            boolean pRandomize,
            boolean pUse5Snobs) {


        int maxEnoblements = (int) Math.floor(pSnobSources.size() / ((pUse5Snobs) ? 5 : 4));
        UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
        //build mappings between targets and potential sources
        Hashtable<Village, List<DistanceMapping>> targetMappings = new Hashtable<Village, List<DistanceMapping>>();
        List<Village> usedSources = new LinkedList<Village>();
        List<Village> usedTargets = new LinkedList<Village>();
        for (Village target : pEnoblementTargets) {
            List<DistanceMapping> mappings = buildSourceTargetsMapping(target, pSnobSources);
            for (int i = 0; i < mappings.size(); i++) {
                long dur = (long) (mappings.get(i).getDistance() * snob.getSpeed() * 60000.0);
                Date send = new Date(pTimeFrame.getEnd() - dur);
                if (!pTimeFrame.inside(send)) {
                    mappings = mappings.subList(0, i - 1);
                    break;
                }
            }
            if (mappings.size() >= ((pUse5Snobs) ? 5 : 4)) {
                //use only targets which can be reached by enough sources
                targetMappings.put(target, mappings);
                //store list of used sources and targets so sort out later
                usedTargets.add(target);
                for (DistanceMapping mapping : mappings) {
                    Village source = mapping.getTarget();
                    if (!usedSources.contains(source)) {
                        usedSources.add(source);
                    }
                }
            }
        }

        Hashtable<Village, List<DistanceMapping>> result = new Hashtable<Village, List<DistanceMapping>>();
        sortOutEnoblements(targetMappings, usedTargets, usedSources, ((pUse5Snobs) ? 5 : 4), result);


        return null;
    }


    private void sortOutEnoblements(Hashtable<Village, List<DistanceMapping>> pTargetMappings, List<Village> pTargets, List<Village> pSources, int pSnobs, Hashtable<Village, List<DistanceMapping>> pResult){
       Village target = pTargets.remove(0);
       List<DistanceMapping> mappings = pTargetMappings.get(target);
       if(mappings.size() == pSnobs){
           
       }
    }





    private void solveAssignments(
            List<Assignment> pInitialAssignments,
            List<Assignment> pFinalAssigments,
            List<Village> pSources,
            int removeIndex) {


        Village current = null;
        if (pSources.size() == 0 || pInitialAssignments.size() == 0) {
            return;
        } else {
            if (removeIndex > pSources.size() - 1) {
                removeIndex = 0;
            }
            current = pSources.remove(removeIndex);
        }




        for (Assignment a : pInitialAssignments) {
            if (a.isEnoblement()) {
                System.out.println("Done Enoblement");
                pFinalAssigments.add(a);
            } else if (a.isOff()) {
                System.out.println("Done Off");
                pFinalAssigments.add(a);
            } else if (a.isFake()) {
                System.out.println("Done Fake");
                pFinalAssigments.add(a);
            }
        }

        for (Assignment a : pFinalAssigments) {
            pInitialAssignments.remove(a);
        }

        solveAssignments(pInitialAssignments, pFinalAssigments, pSources, removeIndex);
    }

    private class Assignment {

        private Village target = null;
        private List<Village> snobs = new LinkedList<Village>();
        private List<Village> offs = new LinkedList<Village>();
        private List<Village> fakes = new LinkedList<Village>();
        int snobCount = 0;
        int cleanPerSnob = 0;
        int maxOffs = 0;

        public Assignment(Village pTarget, int pSnobCount, int pCleanPerSnob, int pMaxOffs) {
            target = pTarget;
            snobCount = pSnobCount;
            cleanPerSnob = pCleanPerSnob;
            maxOffs = pMaxOffs;
        }

        public boolean isEnoblement() {
            return (snobs.size() >= snobCount) && (offs.size() >= cleanPerSnob);
        }

        public boolean isOff() {
            return (snobs.size() < snobCount) && (offs.size() > 0 && offs.size() <= maxOffs);
        }

        public boolean isFake() {
            return (fakes.size() <= maxOffs);
        }

        public List<Village> getSnobList() {
            return snobs;
        }

        public List<Village> getOffList() {
            return offs;
        }

        public List<Village> getFakeList() {
            return fakes;
        }

        public void addSnob(Village pVillage) {
            snobs.add(pVillage);
        }

        public void addOff(Village pVillage) {
            offs.add(pVillage);
        }

        public void addFake(Village pVillage) {
            fakes.add(pVillage);
        }

        public void removeSnob(Village pVillage) {
            snobs.remove(pVillage);
        }

        public void removeOff(Village pVillage) {
            offs.remove(pVillage);
        }

        public void removeFake(Village pVillage) {
            fakes.remove(pVillage);
        }
    }
}


