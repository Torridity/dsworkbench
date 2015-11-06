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
package de.tor.tribes.ui.wiz.dep.types;

import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class SupportSourceElement {

    private Village village = null;
    private int supports = 0;
    private boolean ignored = false;

    public SupportSourceElement(Village pVillage, int pSupports) {
        village = pVillage;
        supports = pSupports;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SupportSourceElement) {
            return ((SupportSourceElement) obj).getVillage().equals(getVillage());
        }
        return false;
    }

    public Village getVillage() {
        return village;
    }

    public int getSupports() {
        return supports;
    }

    public void setSupports(int pSupports) {
        supports = pSupports;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean pValue) {
        ignored = pValue;
    }
}
