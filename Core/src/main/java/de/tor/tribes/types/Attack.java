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
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.php.json.JSONObject;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.*;
import de.tor.tribes.util.xml.JaxenUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jdom.Element;

import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Charon
 */
public class Attack extends ManageableType implements Serializable, Comparable<Attack>, BBSupport {

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
    private UnitHolder unit = null;
    private Date arriveTime = null;
    private boolean showOnMap = false;
    private int type = NO_TYPE;
    private boolean transferredToBrowser = false;

    public Attack() {
        showOnMap = GlobalOptions.getProperties().getBoolean("draw.attacks.by.default", false);
    }

    public Attack(Attack pAttack) {
        this();
        setSource(pAttack.getSource());
        setTarget(pAttack.getTarget());
        setUnit(pAttack.getUnit());
        setArriveTime(pAttack.getArriveTime());
        setType(pAttack.getType());
        setTransferredToBrowser(pAttack.isTransferredToBrowser());
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

    public final void setSource(Village source) {
        this.source = source;
    }

    public Village getTarget() {
        return target;
    }

    public final void setTarget(Village target) {
        this.target = target;
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

    public final void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    private boolean canUseSnob() {
        return getSource() == null || getTarget() == null || DSCalculator.calculateDistance(getSource(), getTarget()) < ServerSettings.getSingleton().getSnobRange();
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

        long runtime = DSCalculator.calculateMoveTimeInMillis(getSource(), getTarget(), getUnit().getSpeed());
        setArriveTime(new Date(pSendTime.getTime() + runtime));
    }

    public Date getSendTime() {
        long runtime = DSCalculator.calculateMoveTimeInMillis(getSource(), getTarget(), getUnit().getSpeed());
        return new Date(getArriveTime().getTime() - runtime);
    }

    public Date getReturnTime() {
        long runtime = DSCalculator.calculateMoveTimeInMillis(getSource(), getTarget(), getUnit().getSpeed());
        return new Date((getArriveTime().getTime() + runtime) / 1000 * 1000);
    }

    public boolean isShowOnMap() {
        return showOnMap;
    }

    public final void setShowOnMap(boolean showOnMap) {
        this.showOnMap = showOnMap;
    }

    @Override
    public String toXml() {
        StringBuilder b = new StringBuilder();
        b.append("<attack>\n");
        b.append("<source>").append(getSource().getId()).append("</source>\n");
        b.append("<target>").append(getTarget().getId()).append("</target>\n");
        b.append("<arrive>").append(getArriveTime().getTime()).append("</arrive>\n");
        b.append("<unit>").append(getUnit().getPlainName()).append("</unit>\n");
        b.append("<extensions>\n");
        b.append("\t<showOnMap>").append(isShowOnMap()).append("</showOnMap>\n");
        b.append("\t<type>").append(getType()).append("</type>\n");
        b.append("\t<transferredToBrowser>").append(isTransferredToBrowser()).append("</transferredToBrowser>\n");
        b.append("</extensions>\n");
        b.append("</attack>");
        return b.toString();
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
    public final void setType(int type) {
        this.type = type;
    }
    /*
     * <attack> <source>VillageID</source> <target>VillageID</target> <arrive>Timestamp</arrive> <unit>Name</unit> <extensions>
     * <showOnMap>true</showOnMap> <type>0</type> </extensions> </attack>
     */

    public JSONObject toJSON(String pOwner, String pPlanID) throws Exception {
        JSONObject a = new JSONObject();
        a.put("owner", URLEncoder.encode(pOwner, "UTF-8"));
        a.put("source", getSource().getId());
        a.put("target", getTarget().getId());
        a.put("arrive", getArriveTime().getTime());
        a.put("type", getType());
        a.put("unit", getUnit().getPlainName());
        a.put("plan", URLEncoder.encode(pPlanID, "UTF-8"));
        return a;
    }

    @Override
    public String toString() {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String result = getSource() + "\n";
        result += getTarget() + "\n";
        result += getUnit() + "\n";
        result += f.format(getArriveTime());
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
    public final void setTransferredToBrowser(boolean transferredToBrowser) {
        this.transferredToBrowser = transferredToBrowser;
    }

    public static String toInternalRepresentation(Attack pAttack) {
        return pAttack.getSource().getId() + "&" + pAttack.getTarget().getId() + "&" + pAttack.getUnit().getPlainName() + "&" + pAttack.getArriveTime().getTime() + "&" + pAttack.getType() + "&" + pAttack.isShowOnMap() + "&" + pAttack.isTransferredToBrowser();

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
        } catch (Exception e) {
            a = null;
        }
        return a;
    }

    @Override
    public int compareTo(Attack a) {
        if (getSource().getId() == a.getSource().getId()
                && getTarget().getId() == a.getTarget().getId()
                && getArriveTime().getTime() == a.getArriveTime().getTime()) {
            return 0;
        }
        return -1;
    }

    @Override
    public void loadFromXml(Element pElement) {
        setSource(DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("source").getText())));
        setTarget(DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("target").getText())));
        setArriveTime(new Date(Long.parseLong(pElement.getChild("arrive").getText())));
        setUnit(DataHolder.getSingleton().getUnitByPlainName(pElement.getChild("unit").getText()));
        setShowOnMap(Boolean.parseBoolean(JaxenUtils.getNodeValue(pElement, "extensions/showOnMap")));
        try {
            setType(Integer.parseInt(JaxenUtils.getNodeValue(pElement, "extensions/type")));
        } catch (Exception e) {
            //no type set
            setType(NO_TYPE);
        }
        try {
            setTransferredToBrowser(Boolean.parseBoolean(JaxenUtils.getNodeValue(pElement, "extensions/transferredToBrowser")));
        } catch (Exception e) {
            //not transferred yet
            setTransferredToBrowser(false);
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

    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String sendVal = null;
        String arrivetVal = null;

        Date aTime = getArriveTime();
        Date sTime = new Date(aTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(getSource(), getTarget(), getUnit().getSpeed()) * 1000));
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
        switch (getType()) {
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
        if (getSource().getTribe() != Barbarians.getSingleton()) {
            attackerVal = getSource().getTribe().toBBCode();
        } else {
            attackerVal = "Barbaren";
        }
        String sourceVal = getSource().toBBCode();
        String unitVal = "";
        if (pExtended) {
            unitVal = "[unit]" + getUnit().getPlainName() + "[/unit]";
        } else {
            unitVal = getUnit().getName();
        }
        String defenderVal = "";
        if (getTarget().getTribe() != Barbarians.getSingleton()) {
            defenderVal = getTarget().getTribe().toBBCode();
        } else {
            defenderVal = "Barbaren";
        }

        String targetVal = getTarget().toBBCode();

        //replace place var
        String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer()) + "/";
        String placeURL = baseURL + "game.php?village=";
        int uvID = -1;
        List<UserProfile> serverProfiles = Arrays.asList(ProfileManager.getSingleton().getProfiles(GlobalOptions.getSelectedServer()));

        //get attacker and look for UserProfile for this attacker
        final Tribe attacker = getSource().getTribe();
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
        placeURL += getSource().getId() + "&screen=place&mode=command&target=" + getTarget().getId();

        String placeLink = "[url=\"" + placeURL + "\"]Versammlungsplatz[/url]";
        String placeVal = placeLink;
        String placeURLVal = placeURL;
        return new String[]{typeVal, attackerVal, sourceVal, unitVal, defenderVal, targetVal, sendVal, arrivetVal, placeVal, placeURLVal};
    }
}
