/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        return new Double(getWoodStatus() * capacity + getClayStatus() * capacity + getIronStatus() * capacity).compareTo(new Double(o.getWoodStatus() * o.getCapacity() + o.getClayStatus() * o.getCapacity() + o.getIronStatus() * o.getCapacity()));
    }
}
