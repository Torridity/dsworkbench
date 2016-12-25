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

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.DataHolder;

/**
 *
 * @author Torridity
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

    /**
     * @return the overallFarm
     */
    public int getOverallFarm() {
        return overallFarm;
    }

    /**
     * @param overallFarm the overallFarm to set
     */
    public void setOverallFarm(int overallFarm) {
        this.overallFarm = overallFarm;
    }

    /**
     * @return the availableFarm
     */
    public int getAvailableFarm() {
        return availableFarm;
    }

    /**
     * @param availableFarm the availableFarm to set
     */
    public void setAvailableFarm(int availableFarm) {
        this.availableFarm = availableFarm;
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
    private int overallFarm = 0;
    private int availableFarm = 0;
    private Direction direction = Direction.BOTH;

    public VillageMerchantInfo(Village pVillage, int pStashCapacity, int pWoodStock, int pClayStock, int pIronStock, int pAvailMerchants, int pMaxMerchants) {
        this(pVillage, pStashCapacity, pWoodStock, pClayStock, pIronStock, pAvailMerchants, pMaxMerchants, 0, 0);
    }

    public VillageMerchantInfo(Village pVillage, int pStashCapacity, int pWoodStock, int pClayStock, int pIronStock, int pAvailMerchants, int pMaxMerchants, int pAvailFarm, int pOverallFarm) {
        setVillage(pVillage);
        this.woodStock = pWoodStock;
        this.clayStock = pClayStock;
        this.stashCapacity = pStashCapacity;
        this.ironStock = pIronStock;
        this.availableMerchants = pAvailMerchants;
        this.overallMerchants = pMaxMerchants;
        this.availableFarm = pAvailFarm;
        this.overallFarm = pOverallFarm;
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

    public void adaptStashCapacity(int pPercent){
    	adaptStashCapacity(pPercent, false);
    }
    
    public void adaptStashCapacity(int pPercent, boolean allowOverflow) {
    	// allowing overflow might be useful, if (e.g. by some AccountManager) resources are used faster than received
        int stashCapacity1 = (int) Math.rint(stashCapacity * ((pPercent > 100 && !allowOverflow) ? 100 : pPercent) / 100.0);
        this.stashCapacity = stashCapacity1;
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

    @Override
    public VillageMerchantInfo clone() {
        VillageMerchantInfo info = new VillageMerchantInfo(village, stashCapacity, woodStock, clayStock, ironStock, availableMerchants, overallMerchants, availableFarm, overallFarm);
        info.setDirection(direction);
        return info;
    }

    @Override
    public String toString() {
        String res = village + " ";
        res += village + ": " + woodStock + ", " + clayStock + ", " + ironStock + " (" + stashCapacity + ") " + availableMerchants + "/" + overallMerchants;
        return res;
    }
}
