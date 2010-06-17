/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Off;
import de.tor.tribes.types.Village;
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
public class OptexWrapper extends AbstractAttackAlgorithm {

   
    public Hashtable<Village, List<Village>> calculateSupports(List<Village> pSources, List<Village> pTargets, int pMaxSplit, int pMaxSupports) {
        Hashtable<Village, List<Village>> supports = new Hashtable<Village, List<Village>>();
        boolean improving = true;
        while (improving) {
          //  System.out.println("NEXT ROUND");
            ArrayList<OffVillage> sources = prepareSourceList(pSources, pTargets, supports, pMaxSplit);
            ArrayList<TargetVillage> targets = prepareTargetList(pTargets, supports, pMaxSupports);
            Hashtable<Destination, Double>[] costs = calculateOffCosts(sources, targets, supports);
            Optex<OffVillage, TargetVillage> algo = new Optex<OffVillage, TargetVillage>(sources, targets, costs);
            try {
                improving = false;
                algo.run();
                for (OffVillage src : sources.toArray(new OffVillage[]{})) {
                    for (Order o : src.getOrders()) {
                        if (o.getAmount() > 0) {

                            TargetVillage dest = (TargetVillage) o.getDestination();
                            Village v = DataHolder.getSingleton().getVillages()[dest.getC().getX()][dest.getC().getY()];
                            if (v != null) {
                                Village source = DataHolder.getSingleton().getVillages()[src.getC().getX()][src.getC().getY()];
                                if (source != null) {
                                    //have new support mapping
                                    List<Village> support = supports.get(source);
                                    if (support == null) {
                                        support = new LinkedList<Village>();
                                        supports.put(source, support);
                                    }
                                    if (!support.contains(v)) {
                                        improving = true;
                                        support.add(v);
                                    } else {
                                    //    System.out.println("BREKING");
                                        improving = false;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                improving = false;
            }
        }

        return supports;
    }

    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<de.tor.tribes.types.Village>> pSources,
            Hashtable<UnitHolder, List<de.tor.tribes.types.Village>> pFakes,
            List<de.tor.tribes.types.Village> pTargets,
            List<de.tor.tribes.types.Village> pFakeTargets,
            Hashtable<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets) {

        /*UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        List<de.tor.tribes.types.Village> offSources = pSources.get(ram);

        int pMaxAttacksPerVillage = 0;
        ArrayList<OffVillage> offs = prepareSourceList(offSources, pTargets, pTimeFrame, ram.getSpeed());
        ArrayList<TargetVillage> offTargets = prepareTargetList(pTargets, pMaxAttacksPerVillage);
        Hashtable<Destination, Double>[] costs = calculateOffCosts(offs, offTargets, pTimeFrame, ram.getSpeed());
        Optex<OffVillage, TargetVillage> algo = new Optex<OffVillage, TargetVillage>(offs, offTargets, costs);
        Hashtable<Village, AbstractTroopMovement> targetMappings = new Hashtable<Village, AbstractTroopMovement>();

        try {
        algo.run();

        /**Build movement mappings*/
        /*     for (OffVillage src : offs.toArray(new OffVillage[]{})) {
        for (Order o : src.getOrders()) {
        de.tor.tribes.util.algo.Village s = (de.tor.tribes.util.algo.Village) src;
        if (o.getAmount() > 0) {
        TargetVillage dest = (TargetVillage) o.getDestination();
        Village v = DataHolder.getSingleton().getVillages()[dest.getC().getX()][dest.getC().getY()];
        if (v != null) {
        pTargets.remove(v);
        AbstractTroopMovement mapping = targetMappings.get(v);
        if (mapping == null) {
        mapping = new Off(v, pMaxAttacksPerVillage);
        targetMappings.put(v, mapping);
        }

        Village source = DataHolder.getSingleton().getVillages()[src.getC().getX()][src.getC().getY()];
        if (source != null) {
        double dist = DSCalculator.calculateDistance(v, source);
        double runtime = dist * ram.getSpeed() * 60000;
        long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
        // if (pTimeFrame.inside(new Date(send), null)) {
        //   System.out.println("Costs: " + algo._getCosts(src, dest));
        mapping.addOff(ram, source);
        // }
        }
        }
        }
        }
        }
        } catch (Exception e) {
        e.printStackTrace();
        }


        /**Combine results*/
        /* LinkedList<AbstractTroopMovement> result = new LinkedList<AbstractTroopMovement>();
        Enumeration<Village> keys = targetMappings.keys();
        while (keys.hasMoreElements()) {
        Village target = keys.nextElement();
        AbstractTroopMovement move = targetMappings.get(target);
        if (move.getOffCount() > 0) {
        result.add(move);
        }
        }
        return result;*/
        return null;
    }

    /**Prepares a list of sources where all sources are removed which do not reach any target in time.
     * The wares count of each source is set depending on the sources occurence.
     */
    private ArrayList<OffVillage> prepareSourceList(List<de.tor.tribes.types.Village> pSources, List<de.tor.tribes.types.Village> pTargets, Hashtable<Village, List<Village>> pSupports, int pSplitCount) {
        ArrayList<OffVillage> offs = new ArrayList<OffVillage>();
        List<de.tor.tribes.types.Village> sourceCopy = new LinkedList<de.tor.tribes.types.Village>(pSources);

        /**Set available wares*/
        for (de.tor.tribes.types.Village v : sourceCopy) {
            List<Village> targets = pSupports.get(v);
            if (targets == null) {
               // System.out.println("Use source (1) " + v);
                offs.add(new OffVillage(new Coordinate((int) v.getX(), (int) v.getY()), pSplitCount));
            } else {
                //add only sources with less than max targets
                int amount = pSplitCount - targets.size();
                boolean supportsAllTargets = true;
                for (Village target : pTargets) {
                    if (!targets.contains(target)) {
                        supportsAllTargets = false;
                    }
                }
                if (amount > 0 && !supportsAllTargets) {
                   // System.out.println("Use source " + v);
                    offs.add(new OffVillage(new Coordinate((int) v.getX(), (int) v.getY()), amount));
                } /*else {
                    System.out.println("Skip source " + v);
                }*/
            }
        }

        return offs;
    }

    /**Builds a list of targets
     */
    private ArrayList<TargetVillage> prepareTargetList(List<de.tor.tribes.types.Village> pVillages, Hashtable<Village, List<Village>> pSupports, int pSupportsPerVillage) {
        ArrayList<TargetVillage> targets = new ArrayList<TargetVillage>();
        Hashtable<Village, Integer> amounts = new Hashtable<Village, Integer>();
        Enumeration<Village> supportKeys = pSupports.keys();
        for (Village v : pVillages) {
            while (supportKeys.hasMoreElements()) {
                Village supportSource = supportKeys.nextElement();
                List<Village> supportTargets = pSupports.get(supportSource);
                if (supportTargets.contains(v)) {
                    Integer amount = amounts.get(v);
                    if (amount == null) {
                        amounts.put(v, 1);
                    } else {
                        amounts.put(v, amount + 1);
                    }
                }
            }
            if (amounts.get(v) == null) {
                amounts.put(v, 0);
            }
        }
        // System.out.println(amounts);
        for (de.tor.tribes.types.Village v : pVillages) {
            // int amount = amounts.get(v);
            // if (amount < pSupportsPerVillage) {
            //   System.out.println("add " + v);
            targets.add(new TargetVillage(new Coordinate((int) v.getX(), (int) v.getY()), 1));
            // }
        }
        return targets;
    }

    /**Calculate Off costs. The costs are based on the runtime between source and target.
     * Sources which can not reach a target produce Double.MAX_VALUE costs.
     */
    private Hashtable<Destination, Double>[] calculateOffCosts(ArrayList<OffVillage> pOffs, ArrayList<TargetVillage> pTargets, Hashtable<Village, List<Village>> pSupports) {
        Hashtable<Destination, Double> costs[] = new Hashtable[pOffs.size()];
        for (int i = 0; i < pOffs.size(); i++) {
            costs[i] = new Hashtable<Destination, Double>();
            for (int j = 0; j < pTargets.size(); j++) {
             //   double cost = pOffs.get(i).distanceTo(pTargets.get(j));
                if (pOffs.get(i).mappingExists(pTargets.get(j))) {
                   // System.out.println("Max mapping for " + pOffs.get(i) + "," + pTargets.get(j));
                    costs[i].put(pTargets.get(j), Double.MAX_VALUE);
                } else {
                   // costs[i].put(pTargets.get(j), cost);
                     costs[i].put(pTargets.get(j), 1.0);
                }
            }
        }
        return costs;
    }

    /**Calculate snob costs.
     * Cost between source and target are Double.MAX_VALUE if
     * - not enough snobs can reach target
     * - not enough clean offs can reach target
     * - snob from source cannot reach target in time
     */
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
