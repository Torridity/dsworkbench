/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;

/**
 *
 * @author Jejkal
 */
public class VillageMerchantInfo implements Cloneable {

    /**
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public enum Direction {

        INCOMING, OUTGOING, BOTH
    }
    private Village village = null;
    private int stashCapacity = 0;
    private int woodStock = 0;
    private int clayStock = 0;
    private int ironStock = 0;
    private int overallMerchants = 0;
    private int availableMerchants = 0;
    private Direction direction = Direction.BOTH;

    public VillageMerchantInfo(Village pVillage, int pStashCapacity, int pWoodStock, int pClayStock, int pIronStock, int pAvailMerchants, int pMaxMerchants) {
        setVillage(pVillage);
        setWoodStock(pWoodStock);
        setClayStock(pClayStock);
        setStashCapacity(pStashCapacity);
        setIronStock(pIronStock);
        setAvailableMerchants(pAvailMerchants);
        setOverallMerchants(pMaxMerchants);
    }

    /**
     * @return the village
     */
    public Village getVillage() {
        return village;
    }

    /**
     * @param village the village to set
     */
    public void setVillage(Village village) {
        this.village = DataHolder.getSingleton().getVillages()[village.getX()][village.getY()];
    }

    /**
     * @return the stashCapacity
     */
    public int getStashCapacity() {
        return stashCapacity;
    }

    public void adaptStashCapacity(int pPercent) {
        setStashCapacity((int) Math.rint(getStashCapacity() * ((pPercent > 100) ? 100 : pPercent) / 100.0));
    }

    /**
     * @param stashCapacity the stashCapacity to set
     */
    public void setStashCapacity(int stashCapacity) {
        this.stashCapacity = stashCapacity;
    }

    /**
     * @return the woodStock
     */
    public int getWoodStock() {
        return woodStock;
    }

    /**
     * @param woodStock the woodStock to set
     */
    public void setWoodStock(int woodStock) {
        this.woodStock = woodStock;
    }

    /**
     * @return the clayStock
     */
    public int getClayStock() {
        return clayStock;
    }

    /**
     * @param clayStock the clayStock to set
     */
    public void setClayStock(int clayStock) {
        this.clayStock = clayStock;
    }

    /**
     * @return the ironStock
     */
    public int getIronStock() {
        return ironStock;
    }

    /**
     * @param ironStock the ironStock to set
     */
    public void setIronStock(int ironStock) {
        this.ironStock = ironStock;
    }

    /**
     * @return the overallMerchants
     */
    public int getOverallMerchants() {
        return overallMerchants;
    }

    /**
     * @param overallMerchants the overallMerchants to set
     */
    public void setOverallMerchants(int overallMerchants) {
        this.overallMerchants = overallMerchants;
    }

    /**
     * @return the availableMerchants
     */
    public int getAvailableMerchants() {
        return availableMerchants;
    }

    /**
     * @param availableMerchants the availableMerchants to set
     */
    public void setAvailableMerchants(int availableMerchants) {
        this.availableMerchants = availableMerchants;
    }

    public VillageMerchantInfo clone() {
        VillageMerchantInfo info = new VillageMerchantInfo(village, stashCapacity, woodStock, clayStock, ironStock, availableMerchants, overallMerchants);
        info.setDirection(getDirection());
        return info;
    }

    public String toString() {
        String res = getVillage() + " ";
        res += getVillage() + ": " + getWoodStock() + ", " + getClayStock() + ", " + getIronStock() + " (" + getStashCapacity() + ") " + getAvailableMerchants() + "/" + getOverallMerchants();
        return res;
    }
}
