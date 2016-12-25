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
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DummyAlly extends Ally {
    
    List<Tribe> t = new LinkedList<>();
    
    public DummyAlly() {
        t.add(new DummyTribe());
    }
    
    @Override
    public int getId() {
        return 1;
    }
    
    @Override
    public short getMembers() {
        return 1;
    }
    
    @Override
    public Tribe[] getTribes() {
        return t.toArray(new Tribe[1]);
    }
}
