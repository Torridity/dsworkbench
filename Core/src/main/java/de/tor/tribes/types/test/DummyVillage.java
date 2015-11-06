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
package de.tor.tribes.types.test;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class DummyVillage extends Village {

    public DummyVillage() {
        this((short) 0, (short) 0);
    }

    public DummyVillage(short pX, short pY) {
        super();
        setX(pX);
        setY(pY);
        Tribe t = new DummyTribe();
        t.addVillage(this);
        setTribe(t);
    }

    @Override
    public int getPoints() {
        return 10019;
    }

    @Override
    public String getName() {
        return "DummyVillage";
    }
}
