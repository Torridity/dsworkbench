/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author Jejkal
 */
public class MerchantDistributor {

    public MerchantDistributor() {
    }

    public void calculate(ArrayList<MerchantSource> pSources, ArrayList<MerchantDestination> pDestinations) {
        //ArrayList<MerchantSource> sources = prepareSourceList();
        // = prepareDestinationList();
        Hashtable<Destination, Double>[] costs = calulateCosts(pSources, pDestinations);
        Optex<MerchantSource, MerchantDestination> algo = new Optex<MerchantSource, MerchantDestination>(pSources, pDestinations, costs);
        try {
            algo.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (MerchantSource source : pSources) {
            for (Order o : source.getOrders()) {
                System.out.println(source + " " + o);
            }
        }
    }

    private ArrayList<MerchantSource> prepareSourceList() {
        MerchantSource source = new MerchantSource(new Coordinate(0, 0), 100);
        MerchantSource source1 = new MerchantSource(new Coordinate(0, 1), 100);
        MerchantSource source2 = new MerchantSource(new Coordinate(0, 2), 100);
        ArrayList<MerchantSource> sources = new ArrayList<MerchantSource>();
        sources.add(source);
        sources.add(source1);
        sources.add(source2);
        return sources;
    }

    private ArrayList<MerchantDestination> prepareDestinationList() {
        MerchantDestination destination = new MerchantDestination(new Coordinate(1, 0), 50);
        MerchantDestination destination1 = new MerchantDestination(new Coordinate(0, 0), 200);
        MerchantDestination destination2 = new MerchantDestination(new Coordinate(1, 2), 50);
        ArrayList<MerchantDestination> destinations = new ArrayList<MerchantDestination>();
        destinations.add(destination);
        destinations.add(destination1);
        destinations.add(destination2);
        return destinations;
    }

    public Hashtable<Destination, Double>[] calulateCosts(
            ArrayList<MerchantSource> pSources,
            ArrayList<MerchantDestination> pDestinations) {
        Hashtable<Destination, Double> costs[] = new Hashtable[pSources.size()];
        for (int i = 0; i < pSources.size(); i++) {
            costs[i] = new Hashtable<Destination, Double>();
            for (int j = 0; j < pDestinations.size(); j++) {
                double cost = pSources.get(i).distanceTo(pDestinations.get(j));
                if (cost == 0) {
                    cost = Double.MAX_VALUE;
                }
                costs[i].put(pDestinations.get(j), cost);
            }
        }
        return costs;
    }

    public static void main(String[] args) {

        //overall settings
        int[] targetRes = new int[]{50000, 60000, 30000};
        int[] minRemainRes = new int[]{20000, 20000, 20000};

        //source settings
        Coordinate s1Coord = new Coordinate(0, 0);
        int[] s1Res = new int[]{30000, 70000, 20000};
        int s1Merchants = 110;
        Coordinate s2Coord = new Coordinate(0, 1);
        int[] s2Res = new int[]{60000, 90000, 40000};
        int s2Merchants = 110;
        //destination settings
        Coordinate d1Coord = new Coordinate(1, 1);

        int[] d1Res = new int[]{40000, 20000, 5000};

        /*  int resSum = 0;
        for (int res : targetRes) {
        resSum += res;
        }*/


        for (int i = 0; i < targetRes.length; i++) {
            System.out.println("--------Round " + i + "--------");
            ArrayList<MerchantSource> sources = new ArrayList<MerchantSource>();
            //double d = (double) targetRes[i] / (double) resSum;
            int s1MaxMerchantsNeed = s1Res[i] - minRemainRes[i];
            if (s1MaxMerchantsNeed <= 0) {
                //source not available
            } else {
                int s1MerchantMaxCapacity = (int) Math.rint((double) s1Merchants / 3.0) * 1000;
                MerchantSource source1 = null;
                if (s1MaxMerchantsNeed > s1MerchantMaxCapacity) {
                    System.out.println("S1Cap " + s1MerchantMaxCapacity);
                    source1 = new MerchantSource(s1Coord, s1MerchantMaxCapacity);
                } else {
                    System.out.println("S1Cap " + s1MaxMerchantsNeed);
                    source1 = new MerchantSource(s1Coord, s1MaxMerchantsNeed);
                }
                sources.add(source1);
            }
            int s2MaxMerchantsNeed = s2Res[i] - minRemainRes[i];
            if (s2MaxMerchantsNeed <= 0) {
                //source not available
            } else {
                int s2MerchantMaxCapacity = (int) Math.rint((double) s1Merchants / 3.0) * 1000;
                MerchantSource source2 = null;
                if (s2MaxMerchantsNeed > s2MerchantMaxCapacity) {
                    System.out.println("S2Cap " + s2MerchantMaxCapacity);
                    source2 = new MerchantSource(s2Coord, s2MerchantMaxCapacity);
                } else {
                    System.out.println("S2Cap " + s2MaxMerchantsNeed);
                    source2 = new MerchantSource(s2Coord, s2MaxMerchantsNeed);
                }
                sources.add(source2);
            }

            //CHECK!!!! Needs MUST be larger 0!
            MerchantDestination dest = new MerchantDestination(d1Coord, targetRes[i] - d1Res[i]);
            ArrayList<MerchantDestination> destinations = new ArrayList<MerchantDestination>();
            destinations.add(dest);
            new MerchantDistributor().calculate(sources, destinations);
            System.out.println("--------Round Done--------");
        }


    }
}
