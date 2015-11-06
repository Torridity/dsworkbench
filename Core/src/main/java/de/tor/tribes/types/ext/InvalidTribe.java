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
package de.tor.tribes.types.ext;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.types.ext.NoAlly;

/**
 *
 * @author Torridity
 */
public class InvalidTribe extends Tribe {

    private static InvalidTribe SINGLETON = null;

    public static synchronized InvalidTribe getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new InvalidTribe();
        }
        return SINGLETON;
    }

    @Override
    public int getId() {
        return -666;
    }

    @Override
    public String getName() {
        return "gelöscht";
    }

    @Override
    public Village[] getVillageList() {
        return new Village[]{};
    }

    @Override
    public Ally getAlly() {
        return NoAlly.getSingleton();
    }
}
