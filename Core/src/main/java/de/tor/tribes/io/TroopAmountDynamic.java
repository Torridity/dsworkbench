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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 * This Class holds a troop Amount
 * wichs Amounts depend on the Amount of Troops in Village
 * 
 * @author extremeCrazyCoder
 */
public class TroopAmountDynamic extends TroopAmount {
    private static final Logger logger = LogManager.getLogger("TroopAmountDynamic");
    HashMap<UnitHolder, TroopAmountElement> amounts;
    
    public TroopAmountDynamic() {
        this(-1);
    }
    
    public TroopAmountDynamic(int pAmount) {
        fill("" + pAmount);
    }

    /**
     * This ignores the unit set in TroopAmountElement!
     * @param pAmount the Amount for !ALL! units to set
     */
    public TroopAmountDynamic(TroopAmountElement pAmount) {
        fill(pAmount.toString());
    }

    public TroopAmountDynamic(TroopAmountElement[] pAmounts) {
        this(-1); //init all amounts to -1 to deal with amounts that are not in array
        for(TroopAmountElement elm: pAmounts) {
            amounts.put(elm.getUnit(), elm);
        }
    }

    public TroopAmountDynamic(Element pElement) {
        loadFromXml(pElement);
    }

    public TroopAmountElement getElementForUnit(UnitHolder pUnit) {
        TroopAmountElement elm = amounts.get(pUnit);
        if(elm == null) {
            return new TroopAmountElement(pUnit, -1);
        }
        return elm;
    }

    /**
     * Sets the Amount for Unit contained within pAmount
     * @param pAmount the Amount & Unit to set
     */
    public void setAmount(TroopAmountElement pAmount) {
        amounts.put(pAmount.getUnit(), pAmount);
    }

    public int getAmountForUnit(UnitHolder pUnit, Village pVillage) {
        return amounts.get(pUnit).getTroopsAmount(pVillage);
    }

    @Override
    public final void loadFromXml(Element pElement) {
        if(pElement == null) return;
        
        amounts = new HashMap<>();
        //load from xml (saved as attributes)
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            try {
                String amount = pElement.getAttributeValue(unit.getPlainName());
                amounts.put(unit, new TroopAmountElement(unit, "-1").loadFromBase64(amount));
            } catch (Exception ignored) {
                amounts.put(unit, new TroopAmountElement(unit, "-1"));
            }
        }
    }

    /**
     * Converts the troops to xml attributes
     * without leading or trailing space 
     * @return converted xml
     */
    @Override
    public String toXml() {
        StringBuilder xml = new StringBuilder();
        boolean first = true;
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            TroopAmountElement elm = getElementForUnit(unit);
            if(!elm.isFixed() || elm.getTroopsAmount(null) >=0) {
                //Information stored in sub element
                if(!first)
                    xml.append(" ");
                else
                    first = false;

                xml.append(unit.getPlainName()).append("=\"");
                //base 64 encode to ensure everything can be saved
                xml.append(elm.toBase64()).append("\"");
            }
        }
        return xml.toString();
    }
    
    public TroopAmountDynamic loadFromProperty(String pProperty) {
        if(pProperty == null || !pProperty.contains("=")) {
            logger.debug("Loding from incorrect element tried");
            return this;
        }
        String[] splited = pProperty.split("/");
        for(String split: splited) {
            UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(
                    split.substring(0, split.indexOf('=')));
            if(unit != UnknownUnit.getSingleton()) {
                //valid unit
                amounts.put(unit, new TroopAmountElement(unit, "0")
                        .loadFromBase64(split.substring(split.indexOf('=') + 1)));
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
            prop.append(getElementForUnit(unit).toBase64());
        }
        return prop.toString();
    }

    @Override
    public void addAmount(TroopAmount pAdd) {
        TroopAmountDynamic toAdd;
        if(pAdd instanceof TroopAmountFixed) {
            toAdd = ((TroopAmountFixed) pAdd).transformToDynamic();
        } else if(pAdd instanceof TroopAmountDynamic) {
            toAdd = (TroopAmountDynamic) pAdd;
        } else {
            logger.error("Unknown Class Type " + pAdd.getClass().getName());
            throw new IllegalArgumentException("Unknown Class Type " + pAdd.getClass().getName());
        }
        
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            TroopAmountElement elm = amounts.get(unit);
            TroopAmountElement aElm = toAdd.getElementForUnit(unit);
            
            String elmStr = elm.toString();
            String aElmStr = aElm.toString();
            if(elm.isFixed() && elm.getTroopsAmount(null) < 0) {
                elmStr = "0";
            }
            if(aElm.isFixed() && aElm.getTroopsAmount(null) < 0) {
                aElmStr = "0";
            }

            elm.setDynamicAmount(elmStr + "+" + aElmStr);
            amounts.put(unit, elm);
        }
    }

    @Override
    public void removeAmount(TroopAmount pRemove) {
        TroopAmountDynamic toRemove;
        if(pRemove instanceof TroopAmountFixed) {
            toRemove = ((TroopAmountFixed) pRemove).transformToDynamic();
        } else if(pRemove instanceof TroopAmountDynamic) {
            toRemove = (TroopAmountDynamic) pRemove;
        } else {
            logger.error("Unknown Class Type " + pRemove.getClass().getName());
            throw new IllegalArgumentException("Unknown Class Type " + pRemove.getClass().getName());
        }
        
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            TroopAmountElement elm = amounts.get(unit);
            TroopAmountElement rElm = toRemove.getElementForUnit(unit);
            if(elm.isFixed() && elm.getTroopsAmount(null) < 0) {
                //no information stored in sublement
                if(!rElm.isFixed() || rElm.getTroopsAmount(null) < 0) {
                    //toRemove element contains information just set to zero
                    elm = new TroopAmountElement(unit, "0");
                }
            } else if(elm.isFixed()) {
                if(rElm.isFixed() && rElm.getTroopsAmount(null) < 0) {
                    int amount = elm.getTroopsAmount(null) - rElm.getTroopsAmount(null);
                    //limit to zero
                    amount = Math.max(amount, 0);
                    elm = new TroopAmountElement(unit, amount);
                } else if(!rElm.isFixed()) {
                    elm.setDynamicAmount(elm.toString() + "-" + rElm.toString());
                }
            } else {
                //Dynamic information stored in sub element
                elm.setDynamicAmount(elm.toString() + "-" + rElm.toString());
            }
            amounts.put(unit, elm);
        }
    }

    @Override
    public void multiplyWith(double factor) {
        if(factor < 0) {
            logger.error("Tried to multiply with negative value " + factor);
            throw new RuntimeException("Tried to multiply with negative value " + factor);
        }
        if(factor == 0) {
            logger.info("multiplyed with 0, use fill instead");
            fill("0");
            return;
        }
        
        for(UnitHolder unit: amounts.keySet()) {
            TroopAmountElement elm = getElementForUnit(unit);
            
            if(!elm.isFixed()
                    || elm.getTroopsAmount(null) > 0) {
                elm.setDynamicAmount("(" + amounts.get(unit).toString() + ")*" + factor);
                amounts.put(unit, elm);
            }
        }
    }

    @Override
    protected int getInternalAmountForUnit(UnitHolder pUnit, Village pVillage) {
        if(pVillage == null && !isFixed()) {
            logger.error("Tried to read fixed troops from Dynamic amount");
            throw new IllegalArgumentException("Tried to read fixed troops from Dynamic amount");
        }
        return getElementForUnit(pUnit).getTroopsAmount(pVillage);
    }
    
    public TroopAmountFixed transformToFixed(Village pVillage) {
        TroopAmountFixed transformed = new TroopAmountFixed();
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            transformed.setAmountForUnit(unit, getInternalAmountForUnit(unit, pVillage));
        }
        return transformed;
    }
    
    @Override
    public boolean equals(Object pOther) {
        if(pOther instanceof TroopAmountFixed) {
            if (!isFixed()) return false;
            return transformToFixed(null).equals(pOther);
        }
        if(pOther instanceof TroopAmountDynamic
                || pOther instanceof StandardAttack) {
            TroopAmountDynamic otherDyn;
            if(pOther instanceof StandardAttack) {
                otherDyn = ((StandardAttack) pOther).getTroops();
            } else {
                otherDyn = (TroopAmountDynamic) pOther;
            }
            
            for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
                if(!getElementForUnit(unit).equals(otherDyn.getElementForUnit(unit))) {
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
        hash = 43 * hash + this.amounts.hashCode();
        return hash;
    }

    public boolean isFixed() {
        for(TroopAmountElement elm: amounts.values()) {
            if(!elm.isFixed())
                return false;
        }
        return true;
    }

    /**
     * Checks wether there are any Values inserted
     * ignores Values that are Zero or -1
     * @return any Values stored
     */
    public boolean hasUnits() {
        if(isFixed()) {
            return !getContainedUnits(null).isEmpty();
        }
        return !getContainedUnits().isEmpty();
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
            TroopAmountElement elm = getElementForUnit(unit);
            if(elm.isFixed()) {
                if(elm.getTroopsAmount(null) != -1)
                    contained.add(unit);
            } else {
                contained.add(unit);
            }
        }
        return contained;
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
            if(getElementForUnit(unit).getTroopsAmount(pVillage) > 0) {
                contained.add(unit);
            }
        }
        return contained;
    }

    public UnitHolder getSlowestUnit() {
        UnitHolder slowest = null;
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            TroopAmountElement elm = getElementForUnit(unit);
            if((!elm.isFixed() || elm.getTroopsAmount(null) > 0)
                    && (slowest == null || slowest.getSpeed() < unit.getSpeed())) {
                slowest = unit;
            }
        }
        return slowest;
    }
    
    @Override
    public TroopAmountDynamic clone() {
        TroopAmountDynamic clone = new TroopAmountDynamic(-1);
        
        for(UnitHolder unit: getContainedUnits()) {
            clone.setAmount(new TroopAmountElement(unit,
                    getElementForUnit(unit).toString()));
        }
        return clone;
    }

    public final void fill(String pAmount) {
        amounts = new HashMap<>();
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            amounts.put(unit, new TroopAmountElement(unit, pAmount));
        }
    }
}
