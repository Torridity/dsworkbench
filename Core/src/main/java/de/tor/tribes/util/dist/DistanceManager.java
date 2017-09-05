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
package de.tor.tribes.util.dist;

import de.tor.tribes.types.ext.Village;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class DistanceManager {

    private List<Village> villages = null;
    private static DistanceManager SINGLETON = null;

    public static synchronized DistanceManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DistanceManager();
        }
        return SINGLETON;
    }

    DistanceManager() {
        villages = new LinkedList<>();
    }

    public void clear() {
        villages.clear();
    }

    public Village[] getVillages() {
        return villages.toArray(new Village[villages.size()]);
    }

    public void removeVillages(int[] pIds) {
        List<Village> tmp = new LinkedList<>();

        for (int col : pIds) {
            int tCol = col - 1;
            if (tCol >= 0) {
                tmp.add(villages.get(tCol));
            }
        }

        for (Village v : tmp) {
            villages.remove(v);
        }
    }

    public void addVillage(Village pVillage) {
        if (!villages.contains(pVillage)) {
            villages.add(pVillage);
        }
    }
}
