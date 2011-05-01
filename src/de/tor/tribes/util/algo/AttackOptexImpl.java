/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Off;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class AttackOptexImpl extends AbstractAttackAlgorithm {

    public void calculate() throws Exception {
        ArrayList<Source> sources = new ArrayList<Source>();
        ArrayList<Destination> destinations = new ArrayList<Destination>();
        sources.add(new AttackSource(new Coordinate(0, 0), 2));

        destinations.add(new AttackDestination(new Coordinate(10, 10), 1));
        destinations.add(new AttackDestination(new Coordinate(11, 11), 2));
        destinations.add(new AttackDestination(new Coordinate(9, 12), 2));
        Hashtable<Destination, Double> costs[] = new Hashtable[sources.size()];
        int cnt = 0;
        for (Source source : sources) {
            Hashtable<Destination, Double> cost = new Hashtable<Destination, Double>();
            costs[cnt] = cost;
            for (Destination destination : destinations) {
                if (((AttackSource) source).distanceTo((AttackDestination) destination) <= 15) {
                    cost.put(destination, ((AttackSource) source).distanceTo((AttackDestination) destination));
                } else {
                    cost.put(destination, Double.MAX_VALUE);
                }
            }
            cnt++;
        }

        new AttackOptex(sources, destinations, costs).run();
        for (Source source : sources) {
            System.out.println(((AttackSource) source));
        }
    }

    public static void main(String[] args) throws Exception {
        new AttackOptexImpl().calculate();
    }

    @Override
    public List<AbstractTroopMovement> calculateAttacks(Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            List<Village> pFakeTargets,
            Hashtable<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets) {

        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        List<Village> ramSources = pSources.get(ram);
        ArrayList<Source> attackSources = new ArrayList<Source>();
        List<Village> usedVillages = new ArrayList<Village>();
        int amCnt = 0;

        for (Village source : ramSources) {
            if (!usedVillages.contains(source)) {
                int amount = Collections.frequency(ramSources, source);
                amCnt += amount;
                AttackSource s = new AttackSource(new Coordinate(source.getX(), source.getY()), amount);
                attackSources.add(s);
                usedVillages.add(source);
            }
        }
        System.out.println("Got " + attackSources.size() + " sources (" + amCnt);

        int aam = 0;
        ArrayList<Destination> attackDestinations = new ArrayList<Destination>();
        for (Village destination : pTargets) {
            int amount = pMaxAttacksTable.get(destination);
            aam += amount;
            AttackDestination d = new AttackDestination(new Coordinate(destination.getX(), destination.getY()), amount);
            attackDestinations.add(d);
        }
        System.out.println("Got " + attackDestinations.size() + " targets (" + aam);
        Hashtable<Destination, Double> costs[] = new Hashtable[attackSources.size()];
        int cnt = 0;
        int rem = 0;
        int use = 0;
        for (Source source : attackSources) {
            Hashtable<Destination, Double> cost = new Hashtable<Destination, Double>();
            costs[cnt] = cost;
            for (Destination destination : attackDestinations) {
                long runtime = (long) DSCalculator.calculateMoveTimeInSeconds(DataHolder.getSingleton().getVillages()[((AttackSource) source).getC().getX()][((AttackSource) source).getC().getY()],
                        DataHolder.getSingleton().getVillages()[((AttackDestination) destination).getC().getX()][((AttackDestination) destination).getC().getY()], ram.getSpeed()) * 1000l;
                if (pTimeFrame.isMovementPossible(runtime, DataHolder.getSingleton().getVillages()[((AttackSource) source).getC().getX()][((AttackSource) source).getC().getY()].getTribe())) {
                    cost.put(destination, ((AttackSource) source).distanceTo((AttackDestination) destination));
                    use++;
                } else {
                    rem++;
                    cost.put(destination, Double.MAX_VALUE);
                }
            }
            cnt++;
        }
        System.out.println("Using " + use + " combinations");
        System.out.println("Removed " + rem + " combinations");
        try {
            new AttackOptex(attackSources, attackDestinations, costs).run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cnt = 0;
        int scnt = 0;
        int ocnt = 0;
        List<AbstractTroopMovement> movements = new LinkedList<AbstractTroopMovement>();
        for (Source source : attackSources) {
            AttackSource aSource = (AttackSource) source;
            List<Order> orders = aSource.getOrders();
            ocnt += orders.size();
            scnt++;
            for (Order o : orders) {
                if (o.getAmount() > 0) {
                    AttackDestination destination = (AttackDestination) o.getDestination();
                    Village target = DataHolder.getSingleton().getVillages()[destination.getC().getX()][destination.getC().getY()];
                    Off off = new Off(target, pMaxAttacksTable.get(target));
                    off.addOff(ram, DataHolder.getSingleton().getVillages()[aSource.getC().getX()][aSource.getC().getY()]);
                    movements.add(off);
                    cnt++;
                    System.out.println("added " + cnt + " offs");
                }
            }
        }
        System.out.println("Sources: " + scnt);
        System.out.println("Orders; " + ocnt);
        return movements;
    }
}
