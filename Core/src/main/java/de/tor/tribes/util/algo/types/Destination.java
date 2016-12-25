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
 * Destinations passed to the algorithm have to implement this interface.
 *
 * Each destination has an amount of needed wares and another amount of
 * ordered wares (which should be 0 at first). It doesnt have to care
 * about the Orders, since Orders are managed by the Sources (see stp.Source).
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 * @see Source
 */
public interface Destination {

    String toString();

    /**
     * Returns the amount of wares which are still needed.
     *
     * Example: The destination needs 5 wares and has already ordered 3.
     *  		Then the destination still needs 2 wares. ;)
     *
     * @return
     */
    int remainingNeeds();

    /**
     * Increases the amount of ordered wares.
     *
     * @param amount the amount to add
     */
    void addOrdered(int amount);

    /**
     * Returns the original (!) need of wares. Independent from the amount of
     * wares, which already have been ordered.
     *
     * @return
     */
    int getNeeded();
}

