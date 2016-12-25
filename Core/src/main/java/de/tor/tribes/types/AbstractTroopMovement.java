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
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public abstract class AbstractTroopMovement {

  private Village mTarget = null;
  private Hashtable<UnitHolder, List<Village>> mOffs = null;
  private int iMinOffs = 0;
  private int iMaxOffs = 0;
  public final static AttackRuntimeSort RUNTIME_SORT = new AttackRuntimeSort();
  private List<Attack> finalizedAttacks = null;

  public AbstractTroopMovement(Village pTarget, int pMinOffs, int pMaxOffs) {
    setTarget(pTarget);
    mOffs = new Hashtable<>();
    iMinOffs = pMinOffs;
    iMaxOffs = pMaxOffs;
  }

  public void setTarget(Village pTarget) {
    mTarget = pTarget;
  }

  public Village getTarget() {
    return mTarget;
  }

  public void addOff(UnitHolder pUnit, Village mSource) {
    List<Village> sourcesForUnit = mOffs.get(pUnit);
    if (sourcesForUnit == null) {
      sourcesForUnit = new LinkedList<>();
      sourcesForUnit.add(mSource);
      mOffs.put(pUnit, sourcesForUnit);
    } else {
      sourcesForUnit.add(mSource);
    }
  }

  public Hashtable<UnitHolder, List<Village>> getOffs() {
    return mOffs;
  }

  public boolean offValid() {
    Enumeration<UnitHolder> unitKeys = mOffs.keys();
    int offs = 0;
    while (unitKeys.hasMoreElements()) {
      UnitHolder unit = unitKeys.nextElement();
      offs += mOffs.get(unit).size();
    }

    return (offs >= iMinOffs);
  }

  public int getOffCount() {
    Enumeration<UnitHolder> unitKeys = mOffs.keys();
    int offs = 0;
    while (unitKeys.hasMoreElements()) {
      UnitHolder unit = unitKeys.nextElement();
      offs += mOffs.get(unit).size();
    }
    return offs;
  }

  public boolean offComplete() {
    Enumeration<UnitHolder> unitKeys = mOffs.keys();
    int offs = 0;
    while (unitKeys.hasMoreElements()) {
      UnitHolder unit = unitKeys.nextElement();
      offs += mOffs.get(unit).size();
    }
    return (offs == iMaxOffs);
  }

  public void setMaxOffs(int pValue) {
    iMaxOffs = pValue;
  }

  public void setMinOffs(int pValue) {
    iMinOffs = pValue;
  }

  public int getMinOffs() {
    return iMinOffs;
  }

  public int getMaxOffs() {
    return iMaxOffs;
  }

  /**
   * Finalize a movement ignoring used send times. This is used for supports.
   */
  public void finalizeMovement(TimeFrame pTimeframe) {
    finalizedAttacks = getAttacks(pTimeframe, null);
  }

  /**
   * Finalize a movement taking care of already used send times. This is used
   * for attacks.
   */
  public void finalizeMovement(TimeFrame pTimeframe, List<Long> pUsedSendTimes) {
    finalizedAttacks = getAttacks(pTimeframe, pUsedSendTimes);
  }

  public Attack[] getFinalizedAttacks() {
    if (finalizedAttacks == null) {
      return new Attack[0];
    }
    return finalizedAttacks.toArray(new Attack[finalizedAttacks.size()]);
  }

  public abstract List<Attack> getAttacks(TimeFrame pTimeframe, List<Long> pUsedSendTimes);

  protected static class AttackRuntimeSort implements Comparator<Attack>, java.io.Serializable {

    @Override
    public int compare(Attack a1, Attack a2) {
      double d1 = a1.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a1.getSource(), a1.getTarget(), a1.getUnit().getSpeed()) * 1000);
      double d2 = a2.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a2.getSource(), a2.getTarget(), a2.getUnit().getSpeed()) * 1000);
      return Double.compare(d1, d2);
    }
  }
}
