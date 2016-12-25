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
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.Tribe;

/**
 *
 * @author Charon
 */
public class InternalEnoblementFilter implements ConquerFilterInterface {

    boolean show = true;

    @Override
    public void setup(Object pFilterComponent) {
        try {
            show = (Boolean) pFilterComponent;
        } catch (Exception e) {
            show = true;
        }
    }

    @Override
    public boolean isValid(Conquer pConquer) {
        Tribe winner = DataHolder.getSingleton().getTribes().get(pConquer.getWinner());
        Tribe loser = DataHolder.getSingleton().getTribes().get(pConquer.getLoser());
        if ((winner == null) || (loser == null)) {
            return true;
        }

        Ally winnerAlly = winner.getAlly();
        Ally loserAlly = loser.getAlly();

        if ((winnerAlly == null || loserAlly == null)) {
            return true;
        }

        boolean internal = false;

        if ((winnerAlly.getId() == loserAlly.getId()) ||
                (loserAlly.getName().toLowerCase().contains(winnerAlly.getName().toLowerCase()) ||
                        winnerAlly.getName().toLowerCase().contains(loserAlly.getName().toLowerCase()))) {
            internal = true;
        }
        if (!internal) {
            return true;
        }
        return show;
    }
}
