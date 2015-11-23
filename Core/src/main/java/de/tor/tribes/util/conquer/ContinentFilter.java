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
package de.tor.tribes.util.conquer;

import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.Village;
import java.awt.Point;

/**
 *
 * @author Charon
 */
public class ContinentFilter implements ConquerFilterInterface {

    Point continentBounds = null;

    @Override
    public void setup(Object pFilterComponent) {
        try {
            continentBounds = (Point) pFilterComponent;
        } catch (Exception e) {
            continentBounds = null;
        }
    }

    @Override
    public boolean isValid(Conquer pConquer) {
        if (continentBounds == null) {
            return true;
        }
        Village v = pConquer.getVillage();
        if (v == null) {
            return false;
        }
        return v.getContinent() >= continentBounds.x && v.getContinent() <= continentBounds.y;
    }
}
