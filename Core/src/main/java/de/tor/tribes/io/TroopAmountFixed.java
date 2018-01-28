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
package de.tor.tribes.io;

import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * This Class holds a fixed troop split
 * 
 * @author extremeCrazyCoder
 */
public class TroopAmountFixed extends TroopAmount {
    private static Logger logger = Logger.getLogger("TroopAmount");
    HashMap<UnitHolder, Integer> amounts;

    @Override
    public String getElementIdentifier() {
        return "trpAmountFix";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "trpAmountFix";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }
    
    public TroopAmountFixed() {
        this(-1);
    }
    
    public TroopAmountFixed(int pAmount) {
        amounts = new HashMap<>();
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            amounts.put(unit, pAmount);
        }
    }

    public TroopAmountFixed(Element pElement) {
        loadFromXml(pElement);
    }

    public void setAmountForUnit(UnitHolder pUnit, int pAmount) {
        amounts.put(pUnit, pAmount);
    }

    public void setAmountForUnit(String pUnitPlainName, int pAmount) {
        UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(pUnitPlainName);
        if(unit != UnknownUnit.getSingleton())
            setAmountForUnit(unit, pAmount);
    }

    public int getAmountForUnit(UnitHolder unit) {
        Integer ammount = amounts.get(unit);
        if(ammount == null) return -1;
        return ammount;
    }
    
    /**
     * 
     * @param pUnitPlainName name of Unit to get
     * @return Amount
     * returns -1 if Unit has no Amount
     * returns -2 if Unit has not been found
     */
    public int getAmountForUnit(String pUnitPlainName) {
        UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(pUnitPlainName);
        if(unit == UnknownUnit.getSingleton())
            return -2;
        return getAmountForUnit(unit);
    }
    
    @Override
    public void loadFromXml(Element pElement) {
        if(pElement == null) return;
        
        amounts = new HashMap<>();
        //load from xml (saved as attributes)
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            try {
                Attribute attrib = pElement.getAttribute(unit.getPlainName());
                amounts.put(unit, attrib.getIntValue());
            } catch (Exception ignored) {
                amounts.put(unit, -1);
            }
        }
    }

    /**
     * Converts the troops to xml attributes
     * without leading or trailing space 
     * @return the converted xml
     */
    @Override
    public String toXml() {
        StringBuilder xml = new StringBuilder();
        boolean first = true;
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            int elm = getAmountForUnit(unit);
            if(elm >= 0) {
                //Information stored in sub element
                if(!first)
                    xml.append(" ");
                else
                    first = false;

                xml.append(unit.getPlainName()).append("=\"");
                //base 64 encode to ensure everything can be saved
                xml.append(elm).append("\"");
            }
        }
        return xml.toString();
    }

    public TroopAmountFixed loadFromProperty(String pProperty) {
        String[] splited = pProperty.split("/");
        for(String split: splited) {
            UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(
                    split.substring(0, split.indexOf('=')));
            if(unit != UnknownUnit.getSingleton()) {
                //valid unit
                setAmountForUnit(unit, Integer.parseInt(split.substring(split.indexOf('=') + 1)));
            }
        }
        return this;
    }
    
    public String toProperty() {
        StringBuilder prop = new StringBuilder();
        boolean first = true;
        for(UnitHolder unit: getContainedUnits()) {
            if(!first)
                prop.append("/");
            else
                first = false;
            
            prop.append(unit.getPlainName()).append("=");
            prop.append(getAmountForUnit(unit));
        }
        return prop.toString();
    }

    public void fill(int pFill) {
        amounts.clear();
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            amounts.put(unit, pFill);
        }
    }

    @Override
    public void addAmount(TroopAmount pAdd) {
        TroopAmountFixed toAdd;
        if(pAdd instanceof TroopAmountFixed) {
            toAdd = (TroopAmountFixed) pAdd;
        } else if(pAdd instanceof TroopAmountDynamic) {
            if(!((TroopAmountDynamic) pAdd).isFixed()) {
                logger.error("Tried to add Dynamic troop amount");
                throw new IllegalArgumentException("Tried to add Dynamic troop amount");
            }
            toAdd = ((TroopAmountDynamic) pAdd).transformToFixed(null);
        } else {
            logger.error("Unknown Class Type " + pAdd.getClass().getName());
            throw new IllegalArgumentException("Unknown Class Type " + pAdd.getClass().getName());
        }
        
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            int amountThis = getAmountForUnit(unit);
            int amountAdd = toAdd.getAmountForUnit(unit);
            
            if(amountThis > 0 || amountAdd > 0) {
                amountThis = Math.max(amountThis, 0);
                amountAdd = Math.max(amountAdd, 0);
                
                setAmountForUnit(unit, amountThis + amountAdd);
            }
        }
    }

    @Override
    public void removeAmount(TroopAmount pRemove) {
        TroopAmountFixed toRemove;
        if(pRemove instanceof TroopAmountFixed) {
            toRemove = (TroopAmountFixed) pRemove;
        } else if(pRemove instanceof TroopAmountDynamic) {
            if(!((TroopAmountDynamic) pRemove).isFixed()) {
                logger.error("Tried to add Dynamic troop amount");
                throw new IllegalArgumentException("Tried to add Dynamic troop amount");
            }
            toRemove = ((TroopAmountDynamic) pRemove).transformToFixed(null);
        } else {
            logger.error("Unknown Class Type " + pRemove.getClass().getName());
            throw new IllegalArgumentException("Unknown Class Type " + pRemove.getClass().getName());
        }
        
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            int amountThis = getAmountForUnit(unit);
            int amountRemove = toRemove.getAmountForUnit(unit);
            
            if(amountThis > 0 || amountRemove > 0) {
                amountThis = Math.max(amountThis, 0);
                amountRemove = Math.max(amountRemove, 0);
                
                //Limit to zero
                amountThis = Math.max(amountThis - amountRemove, 0);
                setAmountForUnit(unit, amountThis);
            }
        }
    }

    @Override
    public void multiplyWith(double factor) {
        if(factor <= 0) {
            //throw exception also for 0 because fill should be used in that case
            logger.error("Tried to multiply with negative value " + factor);
            throw new RuntimeException("Tried to multiply with negative value " + factor);
        }
        
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            int amount = getAmountForUnit(unit);
            
            if(amount > 0) {
                amount = (int) Math.rint(factor * amount);
                setAmountForUnit(unit, amount);
            }
        }
    }

    @Override
    protected int getInternalAmountForUnit(UnitHolder pUnit, Village pVillage) {
        if(pVillage == null) {
            return getAmountForUnit(pUnit);
        } else {
            VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
            if (own == null) {
                //no info available
                //just use 0 for all Units
                return 0;
            }
            return Math.min(own.getTroops().getAmountForUnit(pUnit), getAmountForUnit(pUnit));
        }
    }
    
    @Override
    public boolean equals(Object pOther) {
        if(pOther instanceof TroopAmountDynamic
                || pOther instanceof StandardAttack) {
            TroopAmountDynamic otherDyn;
            if(pOther instanceof StandardAttack) {
                otherDyn = ((StandardAttack) pOther).getTroops();
            } else {
                otherDyn = (TroopAmountDynamic) pOther;
            }
            
            if (!otherDyn.isFixed()) return false;
            return equals(otherDyn.transformToFixed(null));
        }
        if(pOther instanceof TroopAmountFixed) {
            TroopAmountFixed otherFix = (TroopAmountFixed) pOther;
            for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
                if(getAmountForUnit(unit) != otherFix.getAmountForUnit(unit)) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.amounts.hashCode();
        return hash;
    }
    
    @Override
    public TroopAmountFixed clone() {
        TroopAmountFixed clone = new TroopAmountFixed(-1);
        
        for(UnitHolder unit: getContainedUnits()) {
            clone.setAmountForUnit(unit, getAmountForUnit(unit));
        }
        return clone;
    }
    
    public int getOffValue() {
        return super.getOffValue(null);
    }

    public int getRealOffValue() {
        return super.getRealOffValue(null);
    }

    public int getDefValue() {
        return super.getDefValue(null);
    }

    public int getDefArcherValue() {
        return super.getDefArcherValue(null);
    }

    public int getDefCavalryValue() {
        return super.getDefCavalryValue(null);
    }

    public int getTroopPopCount() {
        return super.getTroopPopCount(null);
    }

    public int getTroopSum() {
        return super.getTroopSum(null);
    }
    
    public int getFarmCapacity() {
        return super.getFarmCapacity(null);
    }
    
    /**
     * @return Speed of slowest Unit
     */
    public double getSpeed() {
        return super.getSpeed(null);
    }

    public UnitHolder getSlowestUnit() {
        return super.getSlowestUnit(null);
    }
    
    /**
     * Returns all Contained Units for sending from that Village
     * @return all contained Units
     * checks all units against -1 and 0
     */
    @Override
    public List<UnitHolder> getContainedUnits(Village pVillage) {
        List<UnitHolder> contained = new ArrayList<>();
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            if(getAmountForUnit(unit) > 0) {
                contained.add(unit);
            }
        }
        return contained;
    }
    
    /**
     * Function to check wether are any troops stored
     * (Checks if every element > 0)
     */
    public boolean hasUnits() {
        return !getContainedUnits(null).isEmpty();
    }

    /**
     * Returns all Contained Units
     * @return all contained Units
     * checks fix units against -1
     */
    @Override
    public List<UnitHolder> getContainedUnits() {
        List<UnitHolder> contained = new ArrayList<>();
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            if(getAmountForUnit(unit) >= 0) {
                contained.add(unit);
            }
        }
        return contained;
    }

    /**
     * Function to check wether there is any troop information stored
     * (Checks if every element != -1)
     */
    public boolean containsInformation() {
        return !getContainedUnits().isEmpty();
    }

    TroopAmountDynamic transformToDynamic() {
        TroopAmountDynamic dynamic = new TroopAmountDynamic(-1);
        
        for(UnitHolder unit: getContainedUnits()) {
            dynamic.setAmount(new TroopAmountElement(unit, getAmountForUnit(unit)));
        }
        return dynamic;
    }
}
