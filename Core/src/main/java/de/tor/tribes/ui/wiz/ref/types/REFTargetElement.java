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
package de.tor.tribes.ui.wiz.ref.types;

import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class REFTargetElement {

    private Village village = null;
    private int neededSupports = 0;

    public REFTargetElement(Village pVillage) {
        this.village = pVillage;
    }

    public final void setVillage(Village village) {
        this.village = village;
    }

    public Village getVillage() {
        return village;
    }

    public void setNeededSupports(int neededSupports) {
        this.neededSupports = neededSupports;
    }

    public int getNeededSupports() {
        return neededSupports;
    }
}
