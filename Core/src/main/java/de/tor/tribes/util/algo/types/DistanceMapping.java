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
package de.tor.tribes.util.algo.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;

/**
 *
 * @author Torridity
 */
public class DistanceMapping implements Comparable<DistanceMapping> {

    private Village source = null;
    private Village target = null;
    private double distance = 0.0;

    public DistanceMapping(Village pSource, Village pTarget) {
        source = pSource;
        target = pTarget;
        distance = DSCalculator.calculateDistance(pSource, pTarget);
    }

    public Village getSource() {
        return source;
    }

    public Village getTarget() {
        return target;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(DistanceMapping o) {
        if (getDistance() < o.getDistance()) {
            return -1;
        } else if (getDistance() > o.getDistance()) {
            return 1;
        }
        return 0;
    }

    public String toString(){
        String result = source + " -> " + target + " (" + distance + ")";
        return result;
    }
}
