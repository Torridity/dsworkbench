/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.DSCalculator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author Charon
 */
public class OptexWrapper {

    public void calculateAttacks(
            Hashtable<UnitHolder, List<de.tor.tribes.types.Village>> pSources,
            Hashtable<UnitHolder, List<de.tor.tribes.types.Village>> pFakes,
            List<de.tor.tribes.types.Village> pTargets,
            int pMaxAttacksPerVillage,
            int pMaxCleanPerSnob,
            TimeFrame pTimeFrame,
            boolean pRandomize,
            boolean pUse5Snobs) {
        /*
        // <editor-fold defaultstate="collapsed" desc="Calculate snob destinations">

        UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        List<de.tor.tribes.types.Village> snobSources = pSources.get(snob);
        Hashtable<TargetVillage, Integer> bestSolution = null;
        List<de.tor.tribes.types.Village> enoblementTargets = new LinkedList<de.tor.tribes.types.Village>(pTargets);
        int noEnoblements = 0;
        Hashtable<TargetVillage, List<Village>> bestSources = null;
        if (snobSources != null && snobSources.size() >= ((pUse5Snobs) ? 5 : 4)) {
        //try to find enoblements
        int maxPossible = (int) Math.round((double) snobSources.size() / ((pUse5Snobs) ? 5.0 : 4.0));
        while (true) {
        noEnoblements = 0;
        bestSolution = null;
        List<de.tor.tribes.types.Village> targetCopy = new LinkedList<de.tor.tribes.types.Village>(enoblementTargets);
        while (true) {
        System.out.println("Targets: " + targetCopy.size());
        Hashtable<TargetVillage, Integer> enoblements = calculateEnoblements(snobSources, pSources.get(ram), targetCopy, pTimeFrame, ((pUse5Snobs) ? 5 : 4), pMaxCleanPerSnob);
        Enumeration<TargetVillage> keys = enoblements.keys();
        TargetVillage worst = null;
        int worstCount = ((pUse5Snobs) ? 5 : 4);
        int validCount = 0;
        while (keys.hasMoreElements()) {
        TargetVillage t = keys.nextElement();
        if (enoblements.get(t) < worstCount) {
        worst = t;
        worstCount = enoblements.get(t);
        System.out.println("Worst target to remove: " + worst + " (" + worstCount + ")");
        } else if (enoblements.get(t) == ((pUse5Snobs) ? 5 : 4)) {
        validCount++;
        }
        //System.out.println("DEST " + t.getC().getX() + "|" + t.getC().getY() + ": " + enoblements.get(t));
        }

        if (validCount > noEnoblements) {
        System.out.println("Setting best solution with " + validCount + " enoblements");
        bestSolution = enoblements;
        noEnoblements = validCount;
        if (noEnoblements == maxPossible) {
        System.out.println("Have max enoblement count");
        break;
        }
        }
        if (validCount == 0) {
        System.out.println("No Enoblements possible. Taking best solution");
        break;
        }
        if (worst != null) {
        targetCopy.remove(DataHolder.getSingleton().getVillages()[worst.getC().getX()][worst.getC().getY()]);
        } else {
        System.out.println("No further optimization possible");
        break;
        }
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Calculate clean offs">

        if (bestSolution != null && !bestSolution.isEmpty()) {
        Enumeration<TargetVillage> keys = bestSolution.keys();
        ArrayList<TargetVillage> enoTargets = new ArrayList<TargetVillage>();
        pMaxCleanPerSnob = 3;
        while (keys.hasMoreElements()) {
        TargetVillage t = keys.nextElement();
        if (bestSolution.get(t) == 4) {
        enoTargets.add(new TargetVillage(t.getC(), pMaxCleanPerSnob));
        }
        }


        List<de.tor.tribes.types.Village> offSources = pSources.get(ram);
        ArrayList<OffVillage> offs = prepareSourceList(offSources, pTargets, pTimeFrame, ram.getSpeed());
        Hashtable<Destination, Double> costs2[] = calculateOffCosts(offs, enoTargets, pTimeFrame, pMaxCleanPerSnob);

        // Create an algorithm instance
        Optex<OffVillage, TargetVillage> algo2 = new Optex<OffVillage, TargetVillage>(offs, enoTargets, costs2);
        Hashtable<TargetVillage, List<Village>> usedSources = new Hashtable<TargetVillage, List<Village>>();
        // Run the algorithm
        try {
        algo2.run();
        for (Source src : offs) {
        for (Order o : src.getOrders()) {
        Village s = (Village) src;
        if (o.getAmount() > 0) {
        TargetVillage dest = (TargetVillage) o.getDestination();
        double dist = s.distanceTo(dest);
        double runtime = dist * snob.getSpeed() * 60000;
        long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
        if (!pTimeFrame.inside(new Date(send))) {
        //System.out.println("Invalid Mapping: " + s.getC().getX() + "|" + s.getC().getY() + " -> " + dest.getC().getX() + "|" + dest.getC().getY());
        } else {
        // System.out.println("Mapping: " + s.getC().getX() + "|" + s.getC().getY() + " -> " + dest.getC().getX() + "|" + dest.getC().getY());
        if (usedSources.containsKey(dest)) {
        List<Village> l = usedSources.get(dest);
        l.add(s);
        usedSources.put(dest, l);
        } else {
        List<Village> l = new LinkedList<Village>();
        l.add(s);
        usedSources.put(dest, l);
        }
        }
        }
        }
        }

        System.out.println("Valid Enoblements ");
        keys = usedSources.keys();
        TargetVillage worst = null;
        int worstCount = 3;
        int validCount = 0;
        while (keys.hasMoreElements()) {
        TargetVillage t = keys.nextElement();
        if (usedSources.get(t).size() == pMaxCleanPerSnob) {
        System.out.println("FULLY VALID: " + t.getC().getX() + "|" + t.getC().getY());
        validCount++;
        } else {
        if (usedSources.get(t).size() < worstCount) {
        worstCount = usedSources.get(t).size();
        worst = t;
        System.out.println("Setting worst off target " + t + " (" + worstCount + ")");
        }
        }
        }
        if (validCount == bestSolution.size()) {
        System.out.println("All enoblements are valid");
        break;
        }
        if (validCount > validCount) {
        bestSources = usedSources;
        noEnoblements = validCount;
        }
        if (worst != null) {
        System.out.println("Cleaning target list and removing worst");
        enoblementTargets.remove(DataHolder.getSingleton().getVillages()[worst.getC().getX()][worst.getC().getY()]);
        } else {
        System.out.println("Worst is null");
        break;
        }
        } catch (Exception e) {
        e.printStackTrace();
        }
        } else {
        //no enoblements founds
        System.out.println("No Enoblements found!!");
        break;
        }
        }

        // </editor-fold>
        }*/
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        List<de.tor.tribes.types.Village> snobSources = pSources.get(ram);
        ArrayList<OffVillage> offs = prepareSourceList(snobSources, pTargets, pTimeFrame, ram.getSpeed());
        ArrayList<TargetVillage> targets = prepareTargetList(pTargets, 3);
        Hashtable<Destination, Double>[] costs = calculateOffCosts(offs, targets, pTimeFrame, ram.getSpeed());
        Optex<OffVillage, TargetVillage> algo = new Optex<OffVillage, TargetVillage>(offs, targets, costs);
        System.out.println(offs);
        System.out.println(targets);
        try {
            algo.run();
            Hashtable<TargetVillage, Integer> counts = new Hashtable<TargetVillage, Integer>();
            for (Source src : offs) {
                for (Order o : src.getOrders()) {
                    Village s = (Village) src;
                    if (o.getAmount() > 0) {
                        TargetVillage dest = (TargetVillage) o.getDestination();
                        double dist = s.distanceTo(dest);
                        double runtime = dist * ram.getSpeed() * 60000;
                        long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                        if (pTimeFrame.inside(new Date(send))) {
                            if (counts.containsKey(dest)) {
                                System.out.println("Second " + dest + " ((" + counts.get(dest) + ")) (" + o.getAmount() + ")");
                                counts.put(dest, counts.get(dest) + o.getAmount());
                            } else {
                                System.out.println("First " + dest + " (" + o.getAmount() + ")");
                                counts.put(dest, o.getAmount());
                            }
                        }else{
                            System.out.println("INVALID " + src);
                        }
                    }
                }
            }

            Enumeration<TargetVillage> keys = counts.keys();
            while(keys.hasMoreElements()){
                TargetVillage t = keys.nextElement();
                System.out.println(t +" -> " + counts.get(t));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Hashtable<TargetVillage, Integer> calculateEnoblements(List<de.tor.tribes.types.Village> pSnobSources,
            List<de.tor.tribes.types.Village> pOffSources,
            List<de.tor.tribes.types.Village> pTargets,
            TimeFrame pTimeframe,
            int pSnobsPerEnoblement,
            int pCleanOffsPerEnoblement) {
        ArrayList<OffVillage> snobSources = new ArrayList<OffVillage>();
        ArrayList<OffVillage> offs = new ArrayList<OffVillage>();
        ArrayList<TargetVillage> targets = new ArrayList<TargetVillage>();

        UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        //build lists
        snobSources = prepareSourceList(pSnobSources, pTargets, pTimeframe, snob.getSpeed());
        offs = prepareSourceList(pOffSources, pTargets, pTimeframe, ram.getSpeed());
        targets = prepareTargetList(pTargets, pSnobsPerEnoblement);
        // calculate transport costs based on enoblement including timeframe
        Hashtable<Destination, Double> costs[] = calculateSnobCosts(snobSources, offs, targets, pTimeframe, snob.getSpeed(), ram.getSpeed(), pSnobsPerEnoblement, pCleanOffsPerEnoblement);

        // Create an algorithm instance
        Optex<OffVillage, TargetVillage> algo = new Optex<OffVillage, TargetVillage>(snobSources, targets, costs);

        // Run the algorithm
        Hashtable<TargetVillage, Integer> enoblements = new Hashtable<TargetVillage, Integer>();
        try {
            algo.run();
            int cnt = 0;
            for (Source src : snobSources) {
                for (Order o : src.getOrders()) {
                    Village s = (Village) src;
                    if (o.getAmount() > 0) {
                        TargetVillage dest = (TargetVillage) o.getDestination();
                        double dist = s.distanceTo(dest);
                        double runtime = dist * snob.getSpeed() * 60000;
                        long send = pTimeframe.getEnd() - (long) Math.round(runtime);
                        if (pTimeframe.inside(new Date(send))) {
                            if (!enoblements.containsKey(dest)) {
                                enoblements.put(dest, o.getAmount());
                            } else {
                                enoblements.put(dest, enoblements.get(dest) + o.getAmount());
                            }
                            cnt++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enoblements;
    }

    private ArrayList<OffVillage> prepareSourceList(List<de.tor.tribes.types.Village> pSources, List<de.tor.tribes.types.Village> pTargets, TimeFrame pTimeFrame, double pSpeed) {
        ArrayList<OffVillage> offs = new ArrayList<OffVillage>();
        Hashtable<de.tor.tribes.types.Village, Integer> countMapping = new Hashtable<de.tor.tribes.types.Village, Integer>();
        List<de.tor.tribes.types.Village> sourceCopy = new LinkedList<de.tor.tribes.types.Village>(pSources);
        for (de.tor.tribes.types.Village v : sourceCopy.toArray(new de.tor.tribes.types.Village[]{})) {
            boolean fail = true;
            for (de.tor.tribes.types.Village vt : pTargets) {
                double dist = DSCalculator.calculateDistance(v, vt);
                double runtime = dist * pSpeed * 60000;
                long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                if (pTimeFrame.inside(new Date(send))) {
                    fail = false;
                    break;
                }
            }
            if (fail) {
                //removing village that does not reach any target
                sourceCopy.remove(v);
            }
        }


        for (de.tor.tribes.types.Village v : sourceCopy) {
            if (countMapping.containsKey(v)) {
                //use off source only once
                countMapping.put(v, countMapping.get(v) + 1);
            } else {
                countMapping.put(v, 1);
            }
        }

        Enumeration<de.tor.tribes.types.Village> keys = countMapping.keys();
        while (keys.hasMoreElements()) {
            de.tor.tribes.types.Village v = keys.nextElement();
            offs.add(new OffVillage(new Coordinate((int) v.getX(), (int) v.getY()), countMapping.get(v)));
        }
        return offs;
    }

    private ArrayList<TargetVillage> prepareTargetList(List<de.tor.tribes.types.Village> pVillages, int pAttacksPerVillage) {
        ArrayList<TargetVillage> targets = new ArrayList<TargetVillage>();
        for (de.tor.tribes.types.Village v : pVillages) {
            targets.add(new TargetVillage(new Coordinate((int) v.getX(), (int) v.getY()), pAttacksPerVillage));
        }
        return targets;
    }

    private Hashtable<Destination, Double>[] calculateOffCosts(ArrayList<OffVillage> pOffs, ArrayList<TargetVillage> pTargets, TimeFrame pTimeFrame, double pSpeed) {
        Hashtable<Destination, Double> costs[] = new Hashtable[pOffs.size()];
        for (int i = 0; i < pOffs.size(); i++) {
            costs[i] = new Hashtable<Destination, Double>();
            for (int j = 0; j < pTargets.size(); j++) {
                double dist = pOffs.get(i).distanceTo(pTargets.get(j));
                double runtime = dist * pSpeed * 60000;
                long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                double cost = dist;
                if (!pTimeFrame.inside(new Date(send))) {
                    //increase costs to max
                    cost = Double.MAX_VALUE;
                }
                costs[i].put(pTargets.get(j), cost);
            }
        }
        return costs;
    }

    private Hashtable<Destination, Double>[] calculateSnobCosts(ArrayList<OffVillage> pSnobSources,
            ArrayList<OffVillage> pOffs,
            ArrayList<TargetVillage> pTargets,
            TimeFrame pTimeFrame,
            double pSnobSpeed,
            double pOffSpeed,
            int pSnobCount,
            int pCleanOffs) {
        Hashtable<Destination, Double> costs[] = new Hashtable[pSnobSources.size()];
        for (int i = 0; i < pSnobSources.size(); i++) {
            //calculate cost matrix for each snob source
            costs[i] = new Hashtable<Destination, Double>();
            for (int j = 0; j < pTargets.size(); j++) {
                //calculate matrix row for target
                //check how many off sources can reach the target
                int offCount = 0;
                for (OffVillage v : pOffs) {
                    double dist = v.distanceTo(pTargets.get(j));
                    double runtime = dist * pOffSpeed * 60000;
                    long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                    if (pTimeFrame.inside(new Date(send))) {
                        offCount++;
                    }
                }
                //check how many snobs can reach target
                int snobCount = 0;
                for (OffVillage v : pSnobSources) {
                    double dist = v.distanceTo(pTargets.get(j));
                    double runtime = dist * pSnobSpeed * 60000;
                    long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                    if (pTimeFrame.inside(new Date(send))) {
                        snobCount++;
                    }
                }

                //calculate distance
                double dist = pSnobSources.get(i).distanceTo(pTargets.get(j));
                double runtime = dist * pSnobSpeed * 60000;
                long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                double cost = dist;
                if (!pTimeFrame.inside(new Date(send))) {
                    //increase costs to max if current source cannot reach target in time
                    cost = Double.MAX_VALUE;
                } else if (offCount < pCleanOffs) {
                    //increase costs to max if not enough offs can reach target
                    //  System.out.println("MAX const due to off count");
                    cost = Double.MAX_VALUE;
                } else if (snobCount < pSnobCount) {
                    //increase costs to max if not enough snobs can reach target in time
                    //System.out.println("MAX const due to snob count");
                    cost = Double.MAX_VALUE;
                }
                costs[i].put(pTargets.get(j), cost);
            }
        }
        return costs;
    }

    public static void draw(ArrayList<OffVillage> offs, ArrayList<TargetVillage> targets) {
        JFrame drawer = new JFrame("Drawer");
        STPDrawer stpdrawer = new STPDrawer(offs, targets);
        stpdrawer.setSize(1000, 1000);
        drawer.add(stpdrawer);

        drawer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        drawer.setSize(500, 500);
        drawer.setVisible(true);
    }
}
