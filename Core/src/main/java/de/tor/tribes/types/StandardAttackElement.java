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
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * @author Charon
 */
public class StandardAttackElement {

    private static Logger logger = Logger.getLogger("StandardAttackElement");
    public static final String ALL_TROOPS = "Alle";
    private UnitHolder unit = null;
    private Integer fixedAmount = 0;
    private String dynamicAmount = ALL_TROOPS;

    public StandardAttackElement(UnitHolder pUnit, Integer pFixedAmout) {
        unit = pUnit;
        fixedAmount = pFixedAmout;
        dynamicAmount = null;
    }

    public StandardAttackElement(UnitHolder pUnit, String pDynAmount) {
        unit = pUnit;
        fixedAmount = -1;
        dynamicAmount = pDynAmount;
    }

    public static StandardAttackElement fromXml(Element e) throws Exception {
        UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(e.getAttributeValue("unit"));
        Integer fixed = 0;
        try {
            fixed = e.getAttribute("fixAmount").getIntValue();
        } catch (Exception ignored) {
        }
        String dyn = null;
        try {
            dyn = URLDecoder.decode(e.getAttributeValue("dynAmount"), "UTF-8");
        } catch (Exception ignored) {
        }
        if (dyn == null) {
            if (fixed == -1) {
                return new StandardAttackElement(unit, ALL_TROOPS);
            } else {
                return new StandardAttackElement(unit, fixed);
            }
        } else {
            if (fixed == -1) {
                return new StandardAttackElement(unit, dyn);
            }
        }
        throw new Exception("Invalid entry");
    }

    public String toXml() {
        String result = null;
        if (dynamicAmount == null) {
            result = "<attackElement unit=\"" + unit.getPlainName() + "\" fixAmount=\"" + fixedAmount + "\"/>\n";
        } else {
            try {
                result = "<attackElement unit=\"" + unit.getPlainName() + "\" fixAmount=\"-1\" dynAmount=\"" + URLEncoder.encode(dynamicAmount, "UTF-8") + "\"/>\n";
            } catch (Exception e) {
                result = "<attackElement unit=\"" + unit.getPlainName() + "\" fixAmount=\"" + fixedAmount + "\"/>\n";
            }
        }
        return result;

    }

    public boolean affectsUnit(UnitHolder pUnit) {
        return !(pUnit == null || unit == null) && unit.getPlainName().equals(pUnit.getPlainName());
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public StandardAttackElement(UnitHolder pUnit) {
        unit = pUnit;
        fixedAmount = -1;
        dynamicAmount = ALL_TROOPS;
    }

    public boolean isFixed() {
        return fixedAmount != -1;
    }

    public void setFixedAmount(int pAmount) {
        fixedAmount = pAmount;
        dynamicAmount = null;
    }

    public void setDynamicBySubstraction(int pRemainTroops) {
        fixedAmount = -1;
        dynamicAmount = ALL_TROOPS + " - " + pRemainTroops;
    }

    public void setDynamicByPercent(int pPercent) {
        fixedAmount = -1;
        //catch values smaller 0 and larger 100
        int percent = (pPercent > 100) ? 100 : pPercent;
        percent = (percent < 0) ? 0 : percent;
        dynamicAmount = percent + "%";
    }

    public void setAll() {
        fixedAmount = -1;
        dynamicAmount = ALL_TROOPS;
    }

    public int getTroopsAmount() {
        if (isFixed()) {
            return fixedAmount;
        } else {
            return 0;
        }
    }

    public int getTroopsAmount(Village pVillage) {
        VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
        if (own == null) {
            //no info available
            if (logger.isDebugEnabled()) {
                logger.debug("No troop information found for village '" + pVillage + "'");
            }
            if(isFixed())
            	return fixedAmount;
            return 0; 
        }

        Integer availableAmount = own.getTroopsOfUnitInVillage(unit);

        if (availableAmount == 0) {
            //no troops of this type in village
            return 0;
        }

        if (isFixed()) {
            //fixed amount
            if (availableAmount >= fixedAmount) {
                //enough troops available
                return fixedAmount;
            } else {
                //return max. avail count
                return availableAmount;
            }
        } else {
            //dyn amount
            if (dynamicAmount.equals(ALL_TROOPS)) {
                //return all troops
                return availableAmount;
            } else if (dynamicAmount.startsWith(ALL_TROOPS + " - ")) {
                //return all minus X
                String v = dynamicAmount.replaceAll(ALL_TROOPS + " - ", "").trim();
                int substract = Integer.parseInt(v);
                return Math.max(availableAmount - substract, 0);
            } else if (dynamicAmount.contains("%")) {
                String v = dynamicAmount.replaceAll("%", "").trim();
                double perc = (double) Integer.parseInt(v);
                perc /= 100;
                return Math.max((int) Math.rint(perc * availableAmount), 0);
            }
        }
        return 0;
    }

    public boolean trySettingAmount(String pValue) {
        try {
            //check for fixed value
            int fix = Integer.parseInt(pValue);
            if (fix >= 0) {
                setFixedAmount(fix);
            }
            return true;
        } catch (Exception e) {
            //no fixed value
        }

        if (pValue.equals(ALL_TROOPS)) {
            setAll();
            return true;
        }

        try {
            //check for ALL - X
            int remain = Integer.parseInt(pValue.replaceAll(ALL_TROOPS, "").replaceAll("-", "").trim());
            if (remain >= 0) {
                setDynamicBySubstraction(remain);
            }
            return true;
        } catch (Exception e) {
            //no ALL - X
        }
        try {
            //try X%
            int percent = Integer.parseInt(pValue.replaceAll("%", "").trim());
            if (percent >= 0) {
                setDynamicByPercent(percent);
            }
            return true;
        } catch (Exception e) {
            //no percent
        }
        return false;
    }

    @Override
    public String toString() {
        String result = "";
        if (fixedAmount != -1) {
            result += fixedAmount;
        } else {
            result += dynamicAmount;
        }

        return result;
    }
}
