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
import java.util.Date;

/**
 *
 * @author Torridity
 */
public class TimedAttack {

    private Village mSource = null;
    private UnitHolder unit = null;
    private long lArriveTime = 0;
    private boolean possibleFake = false;
    private boolean possibleSnob = false;

    public TimedAttack(Village pSource, Date pArriveTime) {
        mSource = pSource;
        lArriveTime = pArriveTime.getTime();
    }

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    /**
     * @return the mSource
     */
    public Village getSource() {
        return mSource;
    }

    /**
     * @param mSource the mSource to set
     */
    public void setSource(Village mSource) {
        this.mSource = mSource;
    }

    /**
     * @return the lArriveTime
     */
    public Long getlArriveTime() {
        return lArriveTime;
    }

    /**
     * @param lArriveTime the lArriveTime to set
     */
    public void setlArriveTime(long lArriveTime) {
        this.lArriveTime = lArriveTime;
    }

    /**
     * @return the possibleFake
     */
    public boolean isPossibleFake() {
        return possibleFake;
    }

    /**
     * @param possibleFake the possibleFake to set
     */
    public void setPossibleFake(boolean possibleFake) {
        this.possibleFake = possibleFake;
    }

    /**
     * @return the possibleSnob
     */
    public boolean isPossibleSnob() {
        return possibleSnob;
    }

    /**
     * @param possibleSnob the possibleSnob to set
     */
    public void setPossibleSnob(boolean possibleSnob) {
        this.possibleSnob = possibleSnob;
    }

    @Override
    public String toString() {
        return getSource().getFullName();
    }
}
