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

    private double woodStatus = 0;
    private double clayStatus = 0;
    private double ironStatus = 0;
    private double capacity = 0;

    public StorageStatus(int pWood, int pClay, int pIron, int pStorageCapacity) {
        woodStatus = (double) pWood / (double) pStorageCapacity;
        clayStatus = (double) pClay / (double) pStorageCapacity;
        ironStatus = (double) pIron / (double) pStorageCapacity;
        capacity = pStorageCapacity;
    }

    public void update(int pWood, int pClay, int pIron, int pStorageCapacity) {
        woodStatus = (double) pWood / (double) pStorageCapacity;
        clayStatus = (double) pClay / (double) pStorageCapacity;
        ironStatus = (double) pIron / (double) pStorageCapacity;
        capacity = pStorageCapacity;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getWoodStatus() {
        return woodStatus;
    }

    public double getClayStatus() {
        return clayStatus;
    }

    public double getIronStatus() {
        return ironStatus;
    }

    @Override
    public int compareTo(StorageStatus o) {
        return new Double(getWoodStatus() * capacity + getClayStatus() * capacity + getIronStatus() * capacity).compareTo(o.getWoodStatus() * o.getCapacity() + o.getClayStatus() * o.getCapacity() + o.getIronStatus() * o.getCapacity());
    }
}
