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
package de.tor.tribes.types;

/**
 *
 * @author Torridity
 */
public class StorageStatus implements Comparable<StorageStatus> {

    private int wood = 0;
    private int clay = 0;
    private int iron = 0;
    private int capacity = 0;

    public StorageStatus(int pWood, int pClay, int pIron, int pStorageCapacity) {
        wood = pWood;
        clay = pClay;
        iron = pIron;
        capacity = pStorageCapacity;
    }

    public void update(int pWood, int pClay, int pIron, int pStorageCapacity) {
        wood = pWood;
        clay = pClay;
        iron = pIron;
        capacity = pStorageCapacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getWoodInStorage() {
        return wood;
    }

    public int getClayInStorage() {
        return clay;
    }

    public int getIronInStorage() {
        return iron;
    }

    @Override
    public int compareTo(StorageStatus o) {
        return new Integer(wood + clay + iron).compareTo(o.getWoodInStorage() + o.getClayInStorage() + o.getIronInStorage());
    }
}
