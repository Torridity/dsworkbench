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

import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DummyTribe extends Tribe {

    private List<Village> v = new LinkedList<Village>();

    public DummyTribe() {
        super();
    }

    
    @Override
    public String getName() {
        return "Dummy";
    }

    @Override
    public Ally getAlly() {
        return super.getAlly();
    }

    @Override
    public Village[] getVillageList() {
        return v.toArray(new Village[v.size()]);
    }

    @Override
    public short getVillages() {
        return (short) v.size();
    }
}
