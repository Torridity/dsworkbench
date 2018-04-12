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
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.*;
import de.tor.tribes.util.attack.StandardAttackManager;
import de.tor.tribes.util.xml.JaxenUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jdom.Element;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class Attack extends ManageableType implements Serializable, Comparable<Attack>, BBSupport {

    private static Logger logger = Logger.getLogger("AttackTableModel");

    private final static String[] VARIABLES = new String[]{"%TYPE%", "%ATTACKER%", "%SOURCE%", "%UNIT%", "%DEFENDER%", "%TARGET%", "%SEND%", "%ARRIVE%", "%PLACE%", "%PLACE_URL%"};
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
        UnitHolder slowest = amounts.getSlowestUnit();
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

    public void setArriveTime(Date arriveTime) {
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
        
        TroopAmountDynamic typeAmount = StandardAttackManager.getSingleton().getElementByIcon(this.type).getTroops();

        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            if(unit.getSpeed() <= this.unit.getSpeed()) {
                //faster or equal
                this.amounts.setAmount(typeAmount.getElementForUnit(unit));
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
        str.append(getTroops().toProperty());
        return str.toString();
    }

    public static Attack fromInternalRepresentation(String pLine) {
        Attack a = null;
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
    public String toXml() {
        StringBuilder b = new StringBuilder();
        b.append("<attack>\n");
        b.append("<source>").append(source.getId()).append("</source>\n");
        b.append("<target>").append(target.getId()).append("</target>\n");
        b.append("<arrive>").append(arriveTime.getTime()).append("</arrive>\n");
        b.append("<unit>").append(getUnit().getPlainName()).append("</unit>\n");
        b.append("<extensions>\n");
        b.append("\t<amounts ").append(amounts.toXml()).append(" />\n");
        b.append("\t<showOnMap>").append(showOnMap).append("</showOnMap>\n");
        b.append("\t<type>").append(type).append("</type>\n");
        b.append("\t<transferredToBrowser>").append(transferredToBrowser).append("</transferredToBrowser>\n");
        b.append("</extensions>\n");
        b.append("</attack>");
        return b.toString();
    }

    @Override
    public void loadFromXml(Element pElement) {
        this.source = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("source").getText()));
        this.target = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("target").getText()));
        setArriveTime(new Date(Long.parseLong(pElement.getChild("arrive").getText())));
        this.unit = DataHolder.getSingleton().getUnitByPlainName(pElement.getChild("unit").getText());
        
        try {
            this.type = Integer.parseInt(JaxenUtils.getNodeValue(pElement, "extensions/type"));
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
        this.showOnMap = Boolean.parseBoolean(JaxenUtils.getNodeValue(pElement, "extensions/showOnMap"));
        try {
            this.transferredToBrowser = Boolean.parseBoolean(JaxenUtils.getNodeValue(pElement, "extensions/transferredToBrowser"));
        } catch (Exception e) {
            //not transferred yet
            this.transferredToBrowser = false;
        }
    }

    @Override
    public String getElementIdentifier() {
        return "attack";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "plan";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "key";
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
        if (pExtended) {
            if (ServerSettings.getSingleton().isMillisArrival()) {
                sendVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(sTime);
                arrivetVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(aTime);
            } else {
                sendVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(sTime);
                arrivetVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(aTime);
            }
        } else {
            if (ServerSettings.getSingleton().isMillisArrival()) {
                sendVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(sTime);
                arrivetVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(aTime);
            } else {
                sendVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(sTime);
                arrivetVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(aTime);
            }
        }
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

        String attackerVal = "";
        if (source.getTribe() != Barbarians.getSingleton()) {
            attackerVal = source.getTribe().toBBCode();
        } else {
            attackerVal = "Barbaren";
        }
        String sourceVal = source.toBBCode();
        String unitVal = "";
        if (pExtended) {
            unitVal = "[unit]" + getUnit().getPlainName() + "[/unit]";
        } else {
            unitVal = getUnit().getName();
        }
        String defenderVal = "";
        if (target.getTribe() != Barbarians.getSingleton()) {
            defenderVal = target.getTribe().toBBCode();
        } else {
            defenderVal = "Barbaren";
        }

        String targetVal = target.toBBCode();

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
        return new String[]{typeVal, attackerVal, sourceVal, unitVal, defenderVal, targetVal, sendVal, arrivetVal, "[url=\"" + placeURL + "\"]Versammlungsplatz[/url]", placeURLVal};
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
}
