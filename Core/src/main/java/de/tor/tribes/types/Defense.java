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

/**
 *
 * @author Torridity
 */
public class Defense {

    private DefenseInformation parent = null;
    private Village supporter = null;
    private UnitHolder unit = null;
    private boolean transferredToBrowser = false;

    public Defense(DefenseInformation pParent, Village pSupporter, UnitHolder pUnit) {
        parent = pParent;
        supporter = pSupporter;
        unit = pUnit;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public Village getSupporter() {
        return supporter;
    }

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    public void setTransferredToBrowser(boolean transferredToBrowser) {
        this.transferredToBrowser = transferredToBrowser;
    }

    public boolean isTransferredToBrowser() {
        return transferredToBrowser;
    }

    public int getSupports() {
        return parent.getSupports().length;
    }

    public int getNeededSupports() {
        return parent.getNeededSupports();
    }

    public Village getTarget() {
        return parent.getTarget();
    }

    public long getBestSendTime() {
        long first = parent.getFirstAttack().getTime();
        long moveTime = DSCalculator.calculateMoveTimeInMillis(supporter, parent.getTarget(), unit.getSpeed());
        return first - moveTime;

    }

    public long getWorstSendTime() {
        long last = parent.getLastAttack().getTime();
        long moveTime = DSCalculator.calculateMoveTimeInMillis(supporter, parent.getTarget(), unit.getSpeed());
        return last - moveTime;

    }
}
