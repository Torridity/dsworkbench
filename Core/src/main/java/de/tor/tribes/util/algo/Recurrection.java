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

import de.tor.tribes.util.algo.types.Order;
import de.tor.tribes.util.algo.types.TimeFrame;
import de.tor.tribes.util.algo.types.TargetVillage;
import de.tor.tribes.util.algo.types.Destination;
import de.tor.tribes.util.algo.types.Coordinate;
import de.tor.tribes.util.algo.types.OffVillage;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Off;
import de.tor.tribes.types.ext.Village;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class Recurrection extends AbstractAttackAlgorithm {

    @Override
    public List<AbstractTroopMovement> calculateAttacks(Hashtable<UnitHolder, List<Village>> pSources, Hashtable<UnitHolder, List<Village>> pFakes, List<Village> pTargets, List<Village> pFakeTargets, Hashtable<Village, Integer> pMaxAttacksTable, TimeFrame pTimeFrame, boolean pFakeOffTargets) {


        List<Village> snobSources = pSources.get(DataHolder.getSingleton().getUnitByPlainName("snob"));

        ArrayList<OffVillage> sources = new ArrayList<OffVillage>();
        ArrayList<TargetVillage> targets = new ArrayList<TargetVillage>();
        List<Village> ramSources = pSources.get(DataHolder.getSingleton().getUnitByPlainName("ram"));

        for (Village ramSource : ramSources) {
            sources.add(new OffVillage(new Coordinate(ramSource.getX(), ramSource.getY()), 1));
        }
        for (Village target : pTargets) {
            targets.add(new TargetVillage(new Coordinate(target.getX(), target.getY()), 1));

        }
        Hashtable<Destination, Double> costs[] = new Hashtable[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            costs[i] = new Hashtable<Destination, Double>();
            for (int j = 0; j < targets.size(); j++) {
                double dist = sources.get(i).distanceTo(targets.get(j));
                if (pTimeFrame.isMovementPossible(Math.round(dist * 30.0), null)) {
                    costs[i].put(targets.get(j), sources.get(i).distanceTo(targets.get(j)));
                } else {
                    costs[i].put(targets.get(j), 99999.0);
                }
            }
        }


        Optex<OffVillage, TargetVillage> optex = new Optex<OffVillage, TargetVillage>(sources, targets, costs);
        try {
            optex.run();
        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedList<AbstractTroopMovement>();
        }

        List<AbstractTroopMovement> moves = new LinkedList<AbstractTroopMovement>();
        for (OffVillage v : sources) {
            for (Order o : v.getOrders()) {
                if (o.getAmount() > 0) {
                    TargetVillage d = (TargetVillage) o.getDestination();
                    Off off = new Off(DataHolder.getSingleton().getVillages()[d.getC().getX()][d.getC().getY()], o.getAmount());
                    off.addOff(DataHolder.getSingleton().getUnitByPlainName("ram"), DataHolder.getSingleton().getVillages()[v.getC().getX()][v.getC().getY()]);
                    moves.add(off);
                }
            }
        }

        return moves;
    }

    public static void main(String[] args) {
    }
}
