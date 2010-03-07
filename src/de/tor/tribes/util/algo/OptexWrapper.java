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

    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<de.tor.tribes.types.Village>> pSources,
            Hashtable<UnitHolder, List<de.tor.tribes.types.Village>> pFakes,
            List<de.tor.tribes.types.Village> pTargets,
            List<de.tor.tribes.types.Village> pFakeTargets,
            Hashtable<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets) {

        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
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
            for (OffVillage src : offs.toArray(new OffVillage[]{})) {
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
        LinkedList<AbstractTroopMovement> result = new LinkedList<AbstractTroopMovement>();
        Enumeration<Village> keys = targetMappings.keys();
        while (keys.hasMoreElements()) {
            Village target = keys.nextElement();
            AbstractTroopMovement move = targetMappings.get(target);
            if (move.getOffCount() > 0) {
                result.add(move);
            }
        }
        return result;
    }

    /**Prepares a list of sources where all sources are removed which do not reach any target in time.
     * The wares count of each source is set depending on the sources occurence.
     */
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
                if (pTimeFrame.inside(new Date(send), null)) {
                    fail = false;
                    break;
                }
            }
            if (fail) {
                //    System.out.println("Remove " + v);
                //removing village that does not reach any target
                //   sourceCopy.remove(v);
            }
        }

        /**Set available wares*/
        for (de.tor.tribes.types.Village v : sourceCopy) {
            if (countMapping.containsKey(v)) {
                //use off source only once
                countMapping.put(v, countMapping.get(v) + 1);
            } else {
                countMapping.put(v, 1);
            }
        }

        /**Build Source-Wares list*/
        Enumeration<de.tor.tribes.types.Village> keys = countMapping.keys();
        while (keys.hasMoreElements()) {
            de.tor.tribes.types.Village v = keys.nextElement();
            offs.add(new OffVillage(new Coordinate((int) v.getX(), (int) v.getY()), countMapping.get(v)));
        }
        return offs;
    }

    /**Builds a list of targets
     */
    private ArrayList<TargetVillage> prepareTargetList(List<de.tor.tribes.types.Village> pVillages, int pAttacksPerVillage) {
        ArrayList<TargetVillage> targets = new ArrayList<TargetVillage>();
        for (de.tor.tribes.types.Village v : pVillages) {
            targets.add(new TargetVillage(new Coordinate((int) v.getX(), (int) v.getY()), pAttacksPerVillage));
        }
        return targets;
    }

    /**Calculate Off costs. The costs are based on the runtime between source and target.
     * Sources which can not reach a target produce Double.MAX_VALUE costs.
     */
    private Hashtable<Destination, Double>[] calculateOffCosts(ArrayList<OffVillage> pOffs, ArrayList<TargetVillage> pTargets, TimeFrame pTimeFrame, double pSpeed) {
        Hashtable<Destination, Double> costs[] = new Hashtable[pOffs.size()];
        for (int i = 0; i < pOffs.size(); i++) {
            costs[i] = new Hashtable<Destination, Double>();
            for (int j = 0; j < pTargets.size(); j++) {
                double dist = pOffs.get(i).distanceTo(pTargets.get(j));
                double runtime = dist * pSpeed * 60000;
                long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                double cost = dist;
                if (!pTimeFrame.inside(new Date(send), null)) {
                    //increase costs to max
                    cost = Double.MAX_VALUE;
                }
                costs[i].put(pTargets.get(j), cost);
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
