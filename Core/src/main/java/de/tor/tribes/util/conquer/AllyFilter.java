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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import java.util.List;

/**
 *
 * @author Charon
 */
public class AllyFilter implements ConquerFilterInterface {

    private List<Ally> validAllies = null;

    @Override
    public void setup(Object pFilterComponent) {
        try {
            validAllies = (List<Ally>) pFilterComponent;
        } catch (Exception e) {
            validAllies = null;
        }
    }

    @Override
    public boolean isValid(Conquer pConquer) {
        if (validAllies == null) {
            return false;
        }
        Tribe t = DataHolder.getSingleton().getTribes().get(pConquer.getWinner());
        if (t == null) {
            return false;
        }
        if (t.getAlly() == null) {
            return validAllies.contains(NoAlly.getSingleton());
        } else {
            return validAllies.contains(t.getAlly());
        }
    }
}
