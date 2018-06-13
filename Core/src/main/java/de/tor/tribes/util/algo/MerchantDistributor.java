/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.util.algo.types.Coordinate;
import de.tor.tribes.util.algo.types.Destination;
import de.tor.tribes.util.algo.types.MerchantDestination;
import de.tor.tribes.util.algo.types.MerchantSource;
import de.tor.tribes.util.algo.types.Order;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class MerchantDistributor extends Thread {

    private List<VillageMerchantInfo> infos = null;
    private List<de.tor.tribes.types.ext.Village> incomingOnly = null;
    private List<de.tor.tribes.types.ext.Village> outgoingOnly = null;
    private int[] targetRes = null;
    private int[] remainRes = null;
    private int[] resOrder = new int[]{0, 1, 2};
    private boolean limitMerchants = true;
    private List<List<MerchantSource>> results = null;
    private MerchantDistributorListener listener = null;
    private boolean running = false;

    public MerchantDistributor() {
        setDaemon(true);
        setName("MerchantDistributor");
        setPriority(MIN_PRIORITY);
    }

    public void initialize(List<VillageMerchantInfo> pInfos, List<de.tor.tribes.types.ext.Village> pIncomingOnly, List<de.tor.tribes.types.ext.Village> pOutgoingOnly, int[] pTargetRes, int[] pRemainRes, int[] pResOrder, boolean pLimitMerchants) {
        infos = pInfos;
        incomingOnly = pIncomingOnly;
        outgoingOnly = pOutgoingOnly;
        targetRes = pTargetRes;
        remainRes = pRemainRes;
        resOrder = pResOrder;
        limitMerchants = pLimitMerchants;
    }

    public void setMerchantDistributorListener(MerchantDistributorListener pListener) {
        listener = pListener;
    }

    @Override
    public void run() {
        running = true;
        results = calculate(infos, incomingOnly, outgoingOnly, targetRes, remainRes, limitMerchants);
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public List<List<MerchantSource>> calculate(List<VillageMerchantInfo> pInfos, int[] pTargetRes, int[] pRemainRes) {
        return calculate(pInfos, new LinkedList<de.tor.tribes.types.ext.Village>(), new LinkedList<de.tor.tribes.types.ext.Village>(), pTargetRes, pRemainRes, limitMerchants);
    }

    public boolean hasResult() {
        return results != null;
    }

    public List<List<MerchantSource>> getResults() {
        return results;
    }

    public List<List<MerchantSource>> calculate(List<VillageMerchantInfo> pInfos,
            List<de.tor.tribes.types.ext.Village> pIncomingOnly,
            List<de.tor.tribes.types.ext.Village> pOutgoingOnly,
            int[] pTargetRes,
            int[] pRemainRes,
            boolean pLimitMerchants) {
        ArrayList<MerchantSource> sources = new ArrayList<>();
        ArrayList<MerchantDestination> destinations = new ArrayList<>();
        List<List<MerchantSource>> results = new LinkedList<>();


        int usedResources = 0;
        double merchantLimit = 1.0;// /3.0;
        if (pLimitMerchants) {
            for (int aResOrder : resOrder) {
                if (aResOrder >= 0 && aResOrder <= 2) {
                    usedResources++;
                }
            }
            merchantLimit = 1.0 / (double) usedResources;
        }

        for (int i = 0; i < pTargetRes.length; i++) {
            sources.clear();
            destinations.clear();
            if (listener != null) {
                listener.fireCalculatingResourceEvent(resOrder[i]);
            }
            for (VillageMerchantInfo info : pInfos) {
                int resourcesInStorage = 0;
                boolean skip = false;
                switch (resOrder[i]) {
                    case 0:
                        resourcesInStorage = info.getWoodStock();
                        break;
                    case 1:
                        resourcesInStorage = info.getClayStock();
                        break;
                    case 2:
                        resourcesInStorage = info.getIronStock();
                        break;
                    default:
                        skip = true;
                }

                if (skip) {
                    continue;
                }
                int targetValue = pTargetRes[i];
                int usableResources = (int) (Math.round((double) (resourcesInStorage - pRemainRes[i]) / 1000.0 + .5));
                //limit to resources in storage
                usableResources = Math.min(usableResources, resourcesInStorage / 1000);


                //get available merchants and limit them to the available amount
                int availableTransports = Math.min(info.getAvailableMerchants(), (int) Math.rint(merchantLimit * (double) info.getOverallMerchants()));



                //try to add receiver
                if (usableResources < 0 || resourcesInStorage < targetValue) {//village can not deliver, so it is receiver
                    targetValue = Math.min(pTargetRes[i], info.getStashCapacity());

                    int neededResources = (int) (Math.round((double) (targetValue - resourcesInStorage) / 1000.0 + .5));
                    neededResources = Math.max(neededResources, 0);
                    if (resourcesInStorage + neededResources > info.getStashCapacity()) {//remove resources overlap
                        neededResources -= (resourcesInStorage + neededResources - info.getStashCapacity());
                    }

                    if (neededResources > 0 && !pOutgoingOnly.contains(info.getVillage())) {
                        //add to receiver (destination) list if village is not sender-only and if it needs resources
                        MerchantDestination d = new MerchantDestination(new Coordinate(info.getVillage().getX(), info.getVillage().getY()), neededResources);
                        destinations.add(d);
                    }
                }

                //try to add sender
                if (usableResources > availableTransports) {
                    if (!pIncomingOnly.contains(info.getVillage()) && usableResources > 0) {
                        MerchantSource s = new MerchantSource(new Coordinate(info.getVillage().getX(), info.getVillage().getY()), availableTransports);
                        sources.add(s);
                    }
                } else {
                    //use max available
                    if (!pIncomingOnly.contains(info.getVillage()) && usableResources > 0) {
                        MerchantSource s = new MerchantSource(new Coordinate(info.getVillage().getX(), info.getVillage().getY()), usableResources);
                        sources.add(s);
                    }

                }
            }
            //calculate
            if (sources.isEmpty() || destinations.isEmpty()) {
                results.add(new LinkedList<MerchantSource>());
            } else {
                calculateInternal(sources, destinations);
                List<MerchantSource> sourcesCopy = new LinkedList<>(sources);
                results.add(sourcesCopy);

                // <editor-fold defaultstate="collapsed" desc=" Result building">
                for (MerchantSource source : sources) {
                    for (Order o : source.getOrders()) {
                        int usedMerchants = o.getAmount();
                        MerchantDestination d = (MerchantDestination) o.getDestination();
                        //System.out.println(source + " " + o);
                        for (VillageMerchantInfo info : pInfos) {
                            if (info.getVillage().getX() == source.getC().getX() && info.getVillage().getY() == source.getC().getY()) {
                                switch (resOrder[i]) {
                                    case 0:
                                        info.setWoodStock(info.getWoodStock() - usedMerchants * 1000);
                                        break;
                                    case 1:
                                        info.setClayStock(info.getClayStock() - usedMerchants * 1000);
                                        break;
                                    case 2:
                                        info.setIronStock(info.getIronStock() - usedMerchants * 1000);
                                        break;
                                }
                                info.setAvailableMerchants(info.getAvailableMerchants() - usedMerchants);
                            } else if (info.getVillage().getX() == d.getC().getX() && info.getVillage().getY() == d.getC().getY()) {
                                switch (resOrder[i]) {
                                    case 0:
                                        info.setWoodStock(info.getWoodStock() + usedMerchants * 1000);
                                        break;
                                    case 1:
                                        info.setClayStock(info.getClayStock() + usedMerchants * 1000);
                                        break;
                                    case 2:
                                        info.setIronStock(info.getIronStock() + usedMerchants * 1000);
                                        break;
                                }
                            }
                        }
                    }
                }
                // </editor-fold>
            }

        }

        if (listener != null) {
            listener.fireCalculationFinishedEvent();
        }
        return results;
    }

    private void calculateInternal(ArrayList<MerchantSource> pSources, ArrayList<MerchantDestination> pDestinations) {
        HashMap<Destination, Double>[] costs = calculateCosts(pSources, pDestinations);
        Optex<MerchantSource, MerchantDestination> algo = new Optex<>(pSources, pDestinations, costs);
        try {
            algo.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<Destination, Double>[] calculateCosts(
            ArrayList<MerchantSource> pSources,
            ArrayList<MerchantDestination> pDestinations) {
        HashMap<Destination, Double> costs[] = new HashMap[pSources.size()];
        for (int i = 0; i < pSources.size(); i++) {
            costs[i] = new HashMap<>();
            for (MerchantDestination pDestination : pDestinations) {
                double cost = pSources.get(i).distanceTo(pDestination);
                if (cost == 0) {// || cost > 19) {
                    cost = 99999.0;
                }
                costs[i].put(pDestination, cost);
            }
        }
        return costs;
    }

    public static void main(String[] args) {

        //overall settings
        int[] targetRes = new int[]{100000, 150000, 100000};
        int[] minRemainRes = new int[]{100000, 150000, 100000};

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

        for (int i = 0; i < targetRes.length; i++) {
            System.out.println("--------Round " + i + "--------");
            ArrayList<MerchantSource> sources = new ArrayList<>();
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
            ArrayList<MerchantDestination> destinations = new ArrayList<>();
            destinations.add(dest);
            new MerchantDistributor().calculateInternal(sources, destinations);
            System.out.println("--------Round Done--------");
        }
        /*
         * Offs: Hier entlang! (434|876) K84 7.28	10.251	92.154 36.371 154.905 400000	235/235	5212/20476	am 21.06. um 16:21 Uhr Offs: Hier
         * entlang! (436|880) K84 10.82	10.387	171.896 195.970 175.433 400000	235/235	6312/24000	am 21.06. um 06:50 Uhr Rattennest (-1|33)
         * (485|866) K84 58.55	10.019	71.270 323.198 263.667 400000	110/110	16981/24000 Rattennest (-31|45) (455|878) K84 28.28	10.019
         * 96.649 385.743 222.033 400000	110/110	18441/24000 Rattennest (-32|15) (454|848) K84 37.48	10.019	23.599 219.792 160.859 400000
         * 110/110	10091/24000 Rattennest (-33|44) (453|877) K84 26.17	10.019	134.644 400.000 161.743 400000	110/110	17641/24000
         */

    }

    public interface MerchantDistributorListener {

        void fireCalculatingResourceEvent(int pResourceId);

        void fireCalculationFinishedEvent();
    }
}