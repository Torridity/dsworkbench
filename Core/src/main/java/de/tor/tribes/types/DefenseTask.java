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
import org.apache.commons.lang3.Range;

/**
 *
 * @author Torridity
 */
public class DefenseTask {

    private Village target = null;
    private int necessaryDefenses = 0;
    private Range<Long> arriveTimeFrame = null;

    public DefenseTask(Village pTarget, int pDefenses, Range<Long> pTimeFrame) {
        target = pTarget;
        necessaryDefenses = pDefenses;
        arriveTimeFrame = pTimeFrame;
    }

    /**
     * @return the target
     */
    public Village getTarget() {
        return target;
    }

    /**
     * @return the necessaryDefenses
     */
    public int getNecessaryDefenses() {
        return necessaryDefenses;
    }

    /**
     * @return the arriveTimeFrame
     */
    public Range<Long> getArriveTimeFrame() {
        return arriveTimeFrame;
    }
}
