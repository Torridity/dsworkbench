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
import java.util.Hashtable;

/**
 *
 * @author Torridity
 */
public class SupportType {

    public enum DIRECTION {

        INCOMING, OUTGOING
    }
    private Village village = null;
    private Hashtable<UnitHolder, Integer> support = null;
    private DIRECTION direction = null;

    public SupportType(Village pVillage, Hashtable<UnitHolder, Integer> pSupport, DIRECTION pDirection) {
        village = pVillage;
        support = (Hashtable<UnitHolder, Integer>) pSupport.clone();
        direction = pDirection;
    }

    public Village getVillage() {
        return village;
    }

    public Hashtable<UnitHolder, Integer> getSupport() {
        return support;
    }

    public Integer getTroopsOfUnit(UnitHolder pUnit) {
        Integer amount = support.get(pUnit);
        return (amount == null) ? 0 : amount;
    }

    public DIRECTION getDirection() {
        return direction;
    }
}
