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

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.TroopAmountDynamic;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.*;
import de.tor.tribes.util.attack.StandardAttackManager;
import de.tor.tribes.util.xml.JDomUtils;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author Charon
 */
public class Attack extends ManageableType implements Serializable, Comparable<Attack>, BBSupport {

    private static Logger logger = LogManager.getLogger("AttackTableModel");

    private final static String[] VARIABLES = new String[]{
        "%TYPE%", "%STD_NAME%", "%UNIT%",
        "%ATTACKER%", "%SOURCE%", "%ATTACKER_NO_BB%", "%SOURCE_NO_BB%", "%ATTACKER_ALLY%", "%ATTACKER_ALLY_NO_BB%", "%ATTACKER_ALLY_NAME%",
        "%DEFENDER%", "%TARGET%", "%DEFENDER_NO_BB%", "%TARGET_NO_BB%", "%DEFENDER_ALLY%", "%DEFENDER_ALLY_NO_BB%", "%DEFENDER_ALLY_NAME%",
        "%SEND%", "%ARRIVE%", "%PLACE%", "%PLACE_URL%"
    };
    private final static String STANDARD_TEMPLATE = "%TYPE% von %ATTACKER% aus %SOURCE% mit %UNIT% auf %DEFENDER% in %TARGET% startet am [color=#ff0e0e]%SEND%[/color] und kommt am [color=#2eb92e]%ARRIVE%[/color] an (%PLACE%)";
    public static final int NO_TYPE = StandardAttack.NO_ICON;
    public static final int CLEAN_TYPE = StandardAttack.OFF_ICON;
    public static final int SNOB_TYPE = StandardAttack.SNOB_ICON;
    public static final int SUPPORT_TYPE = StandardAttack.SUPPORT_ICON;
    public static final int FAKE_TYPE = StandardAttack.FAKE_ICON;
    public static final int FAKE_DEFF_TYPE = ImageManager.NOTE_SYMBOL_FAKE_DEF;
    public static final int SPY_TYPE = ImageManager.NOTE_SYMBOL_SPY;
    private static final long serialVersionUID = 10L;
    private Village source = null;
    private Village target = null;
    private UnitHolder unit = null; //used to cut troops by speed
    private Date arriveTime = null;
    private boolean showOnMap = false;
    private int type = NO_TYPE;
    private TroopAmountDynamic amounts = null;
    private boolean transferredToBrowser = false;
    private short multiplier = 1;

    public Attack() {
        showOnMap = GlobalOptions.getProperties().getBoolean("draw.attacks.by.default");
    }

    public Attack(Attack pAttack) {
        this();
        this.source = pAttack.getSource();
        this.target = pAttack.getTarget();
        this.unit = pAttack.getUnit();
        this.amounts = pAttack.getTroops().clone();
        setArriveTime(pAttack.getArriveTime());
        this.type = pAttack.getType();
        this.transferredToBrowser = pAttack.isTransferredToBrowser();
    }

    public boolean isSourceVillage(Village pVillage) {
        return (pVillage == source);
    }

    public boolean isTargetVillage(Village pVillage) {
        return (pVillage == target);
    }

    public Village getSource() {
        return source;
    }

    public void setSource(Village source) {
        this.source = source;
    }

    public Village getTarget() {
        return target;
    }

    public void setTarget(Village target) {
        this.target = target;
    }

    public UnitHolder getRealUnit() {
        UnitHolder slowest = null;
        if(amounts != null) {
            slowest = amounts.getSlowestUnit();
        }
        if(slowest != null) {
            return slowest; 
        }
        return getUnit();
    }

    public UnitHolder getUnit() {
        if (unit != null && unit.getPlainName().equals("snob")) {
            if (canUseSnob()) {
                return unit;
            } else {
                return ImpossibleSnobUnit.getSingleton();
            }
        }
        return unit;
    }

    public void setUnit(UnitHolder pUnit) {
        this.unit = pUnit;
    }
    
    private boolean canUseSnob() {
        return source == null || target == null || DSCalculator.calculateDistance(source, target) < ServerSettings.getSingleton().getSnobRange();
    }

    public Date getArriveTime() {
        return arriveTime;
    }

    public final void setArriveTime(Date arriveTime) {
        if (arriveTime == null) {
            return;
        }
        if (ServerSettings.getSingleton().isMillisArrival()) {
            this.arriveTime = arriveTime;
        } else {
            this.arriveTime = new Date((long) Math.floor((double) arriveTime.getTime() / 1000.0) * 1000L);
        }
    }

    public void setSendTime(Date pSendTime) {
        if (pSendTime == null) {
            return;
        }

        long runtime = DSCalculator.calculateMoveTimeInMillis(source, target, getRealUnit().getSpeed());
        setArriveTime(new Date(pSendTime.getTime() + runtime));
    }

    public Date getSendTime() {
        long runtime = DSCalculator.calculateMoveTimeInMillis(source, target, getRealUnit().getSpeed());
        return new Date(arriveTime.getTime() - runtime);
    }

    public Date getReturnTime() {
        long runtime = DSCalculator.calculateMoveTimeInMillis(source, target, getRealUnit().getSpeed());
        return new Date((arriveTime.getTime() + runtime) / 1000 * 1000);
    }

    public boolean isShowOnMap() {
        return showOnMap;
    }

    public void setShowOnMap(boolean showOnMap) {
        this.showOnMap = showOnMap;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * Get the troop element of this Attack
     * it returns the original Object if you want to modify it you will have to clone it first
     * 
     * @return troop Object
     */
    public TroopAmountDynamic getTroops() {
        if(this.amounts == null){
            setTroops(new TroopAmountDynamic(0));
        }
        return this.amounts;
    }
    
    public void setTroops(TroopAmountDynamic pTroops) {
        this.amounts = pTroops;
    }

    public void setTroopsByType() {
        this.amounts = new TroopAmountDynamic(0);
        if(this.type == NO_TYPE) {
            return;
        }
        
        StandardAttack byType = StandardAttackManager.getSingleton().getElementByIcon(this.type);
        if(byType == null) {
            //no StandardAttack exists for that type
            return;
        }
        
        this.amounts = byType.getTroops().clone();
    }

    public void setTroopsByTypeIgnoreToSlow() {
        this.amounts = new TroopAmountDynamic(0);
        if(this.type == NO_TYPE) {
            return;
        }
        
        StandardAttack byType = StandardAttackManager.getSingleton().getElementByIcon(this.type);
        if(byType == null) {
            //no StandardAttack exists for that type
            return;
        }
        
        logger.debug("Setting from std\n{}", byType);
        TroopAmountDynamic typeAmount = byType.getTroops();

        for(UnitHolder u: DataHolder.getSingleton().getUnits()) {
            if(u.getSpeed() <= unit.getSpeed()) {
                //faster or equal
                logger.debug("Setting {} with {}", u.getPlainName(), typeAmount.getElementForUnit(u));
                this.amounts.setAmount(typeAmount.getElementForUnit(u));
            }
        }
    }
    
    @Override
    public String toString() {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String result = source + "\n";
        result += target + "\n";
        result += getUnit() + "\n";
        result += f.format(arriveTime);
        return result;
    }

    /**
     * @return the transferredToBrowser
     */
    public boolean isTransferredToBrowser() {
        return transferredToBrowser;
    }

    /**
     * @param transferredToBrowser the transferredToBrowser to set
     */
    public void setTransferredToBrowser(boolean transferredToBrowser) {
        this.transferredToBrowser = transferredToBrowser;
    }

    public String toInternalRepresentation() {
        StringBuilder str = new StringBuilder();
        str.append(getSource().getId()).append("&");
        str.append(getTarget().getId()).append("&");
        str.append(getUnit().getPlainName()).append("&");
        str.append(getArriveTime().getTime()).append("&");
        str.append(getType()).append("&");
        str.append(isShowOnMap()).append("&");
        str.append(isTransferredToBrowser()).append("&");
        str.append(getTroops().toProperty()).append("&");
        str.append(getMultiplier());
        return str.toString();
    }

    public static Attack fromInternalRepresentation(String pLine) {
        Attack a;
        try {
            String[] split = pLine.trim().split("&");
            a = new Attack();
            a.setSource(DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(split[0])));
            a.setTarget(DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(split[1])));
            a.setUnit(DataHolder.getSingleton().getUnitByPlainName(split[2]));
            a.setArriveTime(new Date(Long.parseLong(split[3])));
            a.setType(Integer.parseInt(split[4]));
            a.setShowOnMap(Boolean.parseBoolean(split[5]));
            a.setTransferredToBrowser(Boolean.parseBoolean(split[6]));
            a.setTroops(new TroopAmountDynamic().loadFromProperty(split[7]));
            a.setMultiplier((short) Integer.parseInt(split[8]));
        } catch (Exception e) {
            a = null;
        }
        return a;
    }

    @Override
    public int compareTo(Attack a) {
        if (source.getId() == a.getSource().getId()
                && target.getId() == a.getTarget().getId()
                && arriveTime.getTime() == a.getArriveTime().getTime()) {
            return 0;
        }
        return -1;
    }

    @Override
    public Element toXml(String elementName) {
        Element attack = new Element(elementName);
        attack.addContent(new Element("source").setText(Integer.toString(source.getId())));
        attack.addContent(new Element("target").setText(Integer.toString(target.getId())));
        attack.addContent(new Element("arrive").setText(Long.toString(arriveTime.getTime())));
        attack.addContent(new Element("unit").setText(getUnit().getPlainName()));

        Element extensions = new Element("extensions");
        extensions.addContent(amounts.toXml("amounts"));
        extensions.addContent(new Element("showOnMap").setText(Boolean.toString(showOnMap)));
        extensions.addContent(new Element("type").setText(Integer.toString(type)));
        extensions.addContent(new Element("transferredToBrowser").setText(Boolean.toString(transferredToBrowser)));
        extensions.addContent(new Element("multiplier").setText(Integer.toString(multiplier)));
        
        attack.addContent(extensions);
        return attack;
    }

    @Override
    public void loadFromXml(Element pElement) {
        this.source = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("source").getText()));
        this.target = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("target").getText()));
        setArriveTime(new Date(Long.parseLong(pElement.getChild("arrive").getText())));
        this.unit = DataHolder.getSingleton().getUnitByPlainName(pElement.getChild("unit").getText());
        
        try {
            this.type = Integer.parseInt(JDomUtils.getNodeValue(pElement, "extensions/type"));
            try {
                this.amounts = new TroopAmountDynamic(pElement.getChild("extensions").getChild("amounts"));
            } catch (Exception e) {
                //for backward compatibility load from type
                setTroopsByType();
            }
        } catch (Exception e) {
            //no type set
            this.type = NO_TYPE;
            this.amounts = new TroopAmountDynamic(0);
        }
        this.showOnMap = Boolean.parseBoolean(JDomUtils.getNodeValue(pElement, "extensions/showOnMap"));
        this.transferredToBrowser = Boolean.parseBoolean(JDomUtils.getNodeValue(pElement, "extensions/transferredToBrowser"));
        
        if(JDomUtils.getNodeValue(pElement, "extensions/multiplier") != null)
            this.multiplier = (short) Integer.parseInt(JDomUtils.getNodeValue(pElement, "extensions/multiplier"));
    }

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String sendVal = null;
        String arrivetVal = null;

        Date aTime = getArriveTime();
        Date sTime = getSendTime();
        SimpleDateFormat sdf;
        if(ServerSettings.getSingleton().isMillisArrival()) {
            if(pExtended) {
                sdf = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'");
            } else {
                sdf = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS");
            }
        } else {
            sdf = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss");
        }
        sendVal = sdf.format(sTime);
        arrivetVal = sdf.format(sTime);
        
        String typeVal = "";
        switch (type) {
            case Attack.CLEAN_TYPE: {
                typeVal = "Angriff (Clean-Off)";
                break;
            }
            case Attack.FAKE_TYPE: {
                typeVal = "Angriff (Fake)";
                break;
            }
            case Attack.SNOB_TYPE: {
                typeVal = "Angriff (AG)";
                break;
            }
            case Attack.SUPPORT_TYPE: {
                typeVal = "Unterstützung";
                break;
            }
            default: {
                typeVal = "Angriff";
            }
        }
        
        String stdName = "Unbenannt";
        StandardAttack a = StandardAttackManager.getSingleton().getElementByIcon(getType());
        if(a != null) {
            stdName = a.getName();
        }
        
        String unitVal = "";
        if (pExtended) {
            unitVal = "[unit]" + getUnit().getPlainName() + "[/unit]";
        } else {
            unitVal = getUnit().getName();
        }

        String attackerVal = "";
        String attackerNoBBVal = "";
        Ally attAlly = null;
        if (source.getTribe() != Barbarians.getSingleton()) {
            attackerVal = source.getTribe().toBBCode();
            attackerNoBBVal = source.getTribe().getName();
            attAlly = source.getTribe().getAlly();
        } else {
            attackerVal = "Barbaren";
            attackerNoBBVal = "Barbaren";
        }
        if (attAlly == null) {
            attAlly = NoAlly.getSingleton();
        }
        String attackerAllyVal = attAlly.toBBCode();
        String attackerAllyNoBBVal = attAlly.getTag();
        String attackerAllyNameVal = attAlly.getName();
        String sourceVal = source.toBBCode();
        String sourceNoBBVal = source.getCoordAsString();
        
        String defenderVal = "";
        String defenderNoBBVal = "";
        Ally defAlly = null;
        if (target.getTribe() != Barbarians.getSingleton()) {
            defenderVal = target.getTribe().toBBCode();
            defenderNoBBVal = target.getTribe().getName();
            defAlly = target.getTribe().getAlly();
        } else {
            defenderVal = "Barbaren";
            defenderNoBBVal = "Barbaren";
        }
        if (defAlly == null) {
            defAlly = NoAlly.getSingleton();
        }
        String defenderAllyVal = defAlly.toBBCode();
        String defenderAllyNoBBVal = defAlly.getTag();
        String defenderAllyNameVal = defAlly.getName();
        String targetVal = target.toBBCode();
        String targetNoBBVal = target.getCoordAsString();

        //replace place var
        String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer()) + "/";
        String placeURL = baseURL + "game.php?village=";
        int uvID = -1;
        List<UserProfile> serverProfiles = Arrays.asList(ProfileManager.getSingleton().getProfiles(GlobalOptions.getSelectedServer()));

        //get attacker and look for UserProfile for this attacker
        final Tribe attacker = source.getTribe();
        UserProfile profileForAttacker = (UserProfile) CollectionUtils.find(serverProfiles, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                UserProfile profile = (UserProfile) o;
                if (attacker != null && profile.getTribe() != null) {
                    return profile.getTribe().getId() == attacker.getId();
                }
                //no attacker found
                return false;
            }
        });

        if (profileForAttacker != null) {
            //profile for attacker exists...check if it is set to UV
            if (profileForAttacker.isUVAccount()) {
                uvID = profileForAttacker.getTribe().getId();
            } else {
                uvID = -1;
            }
        } else {
            //no profile found...should be no UV
            uvID = -1;
        }

        if (uvID >= 0) {
            placeURL = baseURL + "game.php?t=" + uvID + "&village=";
        }
        placeURL += source.getId() + "&screen=place&mode=command&target=" + target.getId();

        String placeURLVal = placeURL;
        
        return new String[]{
            typeVal, stdName, unitVal,
            attackerVal, sourceVal, attackerNoBBVal, sourceNoBBVal, attackerAllyVal, attackerAllyNoBBVal, attackerAllyNameVal,
            defenderVal, targetVal, defenderNoBBVal, targetNoBBVal, defenderAllyVal, defenderAllyNoBBVal, defenderAllyNameVal,
            sendVal, arrivetVal, "[url=\"" + placeURL + "\"]Versammlungsplatz[/url]", placeURLVal};
    }
    
    @Override
    public boolean equals(Object other) {
        if(! (other instanceof Attack)) return false;
        
        Attack otherAtt = (Attack) other;
        if(!source.equals(otherAtt.getSource())) return false;
        if(!target.equals(otherAtt.getTarget())) return false;
        if(!amounts.equals(otherAtt.getTroops())) return false;
        if(!arriveTime.equals(otherAtt.getArriveTime())) return false;
        if(showOnMap != otherAtt.isShowOnMap()) return false;
        if(type != otherAtt.getType()) return false;
        if(transferredToBrowser != otherAtt.isTransferredToBrowser()) return false;
        
        return true;
    }

    public void setMultiplier(Short pMultiplier) {
        this.multiplier = pMultiplier;
    }

    public short getMultiplier() {
        return this.multiplier;
    }
}
