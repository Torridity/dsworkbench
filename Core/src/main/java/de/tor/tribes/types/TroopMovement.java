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

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.TroopHelper;
import de.tor.tribes.util.algo.types.TimeFrame;

import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class TroopMovement {
  private Village mTarget = null;
  private Hashtable<UnitHolder, List<Village>> mOffs = null;
  private int iMaxOffs = 0;
  public final static AttackRuntimeSort RUNTIME_SORT = new AttackRuntimeSort();
  private List<Attack> finalizedAttacks = null;
  private int type = -1;

  public TroopMovement(Village pTarget, int pMaxOffs, int pType) {
    mTarget = pTarget;
    mOffs = new Hashtable<>();
    iMaxOffs = pMaxOffs;
    type = pType;
  }

  public void setTarget(Village pTarget) {
    mTarget = pTarget;
  }

  public Village getTarget() {
    return mTarget;
  }

  public void addOff(UnitHolder pUnit, Village mSource) {
    TroopHelper.fillSourcesWithAttacksForUnit(mSource, mOffs, null, pUnit);
  }

  public Hashtable<UnitHolder, List<Village>> getOffs() {
    return mOffs;
  }
  
  public boolean offComplete() {
    return (getOffCount() == iMaxOffs);
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

  public void setMaxOffs(int pValue) {
    iMaxOffs = pValue;
  }

  public int getMaxOffs() {
    return iMaxOffs;
  }

  /**
   * Finalize a movement ignoring used send times. This is used for supports.
   */
  public void finalizeMovement(TimeFrame pTimeframe) {
    finalizeMovement(pTimeframe, null);
  }

  /**
   * Finalize a movement taking care of already used send times. This is used
   * for attacks.
   */
  public void finalizeMovement(TimeFrame pTimeFrame, List<Long> pUsedSendTimes) {
    List<Attack> result = new LinkedList<>();
    Enumeration<UnitHolder> unitKeys = getOffs().keys();
    Village target = getTarget();

    while (unitKeys.hasMoreElements()) {
        UnitHolder unit = unitKeys.nextElement();

        List<Village> sources = getOffs().get(unit);
        for (Village offSource : sources) {
            long runtime = Math.round(DSCalculator.calculateMoveTimeInSeconds(offSource, target, unit.getSpeed()) * 1000);
            Date fittedTime = pTimeFrame.getFittedArriveTime(runtime, pUsedSendTimes);
            if (fittedTime != null) {
                Attack a = new Attack();
                a.setTarget(target);
                a.setSource(offSource);
                a.setArriveTime(fittedTime);
                a.setUnit(unit);
                a.setType(type);
                a.setTroopsByType();
                result.add(a);
            }
        }
    }

    finalizedAttacks = result;
  }

  public Attack[] getFinalizedAttacks() {
    if (finalizedAttacks == null) {
      return new Attack[0];
    }
    return finalizedAttacks.toArray(new Attack[finalizedAttacks.size()]);
  }

  protected static class AttackRuntimeSort implements Comparator<Attack>, java.io.Serializable {
    @Override
    public int compare(Attack a1, Attack a2) {
      return Long.compare(a1.getSendTime().getTime(), a2.getSendTime().getTime());
    }
  }
}
