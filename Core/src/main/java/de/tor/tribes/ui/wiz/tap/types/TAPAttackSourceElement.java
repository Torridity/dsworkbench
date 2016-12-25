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
package de.tor.tribes.ui.wiz.tap.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class TAPAttackSourceElement {

    private Village village = null;
    private UnitHolder unit = null;
    private boolean fake = false;
    private boolean ignored = false;

    public TAPAttackSourceElement(Village pVillage, UnitHolder pUnit) {
        village = pVillage;
        unit = pUnit;
    }

    public TAPAttackSourceElement(Village pVillage, UnitHolder pUnit, boolean pFake) {
        this(pVillage, pUnit);
        fake = pFake;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TAPAttackSourceElement && ((TAPAttackSourceElement) obj).getVillage().equals(village);
    }

    public Village getVillage() {
        return village;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public void setUnit(UnitHolder pUnit) {
        unit = pUnit;
    }

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean pValue) {
        fake = pValue;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean pValue) {
        ignored = pValue;
    }
}
