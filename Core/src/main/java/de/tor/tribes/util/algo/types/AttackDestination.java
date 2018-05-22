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
package de.tor.tribes.util.algo.types;

/**
 *
 * @author Torridity
 */
public class AttackDestination extends Village implements Destination {

    /**
     * The source's coordinate.
     */
    protected int needs;
    protected int ordered;

    public AttackDestination(Coordinate c, int needs) {
        this.c = c;
        this.needs = needs;
    }

    @Override
    public String toString() {
        return this.c.toString();
    }

    @Override
    public int remainingNeeds() {
        return this.needs - this.ordered;
    }

    @Override
    public void addOrdered(int amount) {
        this.ordered += amount;
    }

    @Override
    public int getNeeded() {
        return needs;
    }

    public void setNeeded(int needed) {
        this.needs = needed;
    }
}
