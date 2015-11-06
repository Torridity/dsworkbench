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

import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class TAPAttackTargetElement {

    private Village village = null;
    private int attacks = 1;
    private boolean fake = false;
    private boolean ignored = false;

    public TAPAttackTargetElement(Village pVillage) {
        this(pVillage, false, 1);
    }

    public TAPAttackTargetElement(Village pVillage, boolean pFake) {
        this(pVillage, pFake, 1);
    }

    public TAPAttackTargetElement(Village pVillage, boolean pFake, int pAmount) {
        village = pVillage;
        fake = pFake;
        attacks = pAmount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TAPAttackTargetElement) {
            return ((TAPAttackTargetElement) obj).getVillage().equals(getVillage());
        }
        return false;
    }

    public Village getVillage() {
        return village;
    }

    public int getAttacks() {
        return attacks;
    }

    public void addAttack() {
        attacks++;
    }

    public boolean removeAttack() {
        attacks--;
        boolean modified = (attacks <= 0) ? false : true;
        attacks = Math.max(1, attacks);
        return modified;
    }

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean pValue) {
        fake = pValue;
    }

    public void setAttacks(int attacks) {
        this.attacks = attacks;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }
    
    
}
