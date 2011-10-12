/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.php.json.JSONObject;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.Serializable;
import java.util.Date;
import org.jdom.Element;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

/**
 *
 * @author Charon
 */
public class Attack extends ManageableType implements Serializable, Comparable<Attack>, BBSupport {

    private final static String[] VARIABLES = new String[]{"%TYPE%", "%ATTACKER%", "%SOURCE%", "%UNIT%", "%DEFENDER%", "%TARGET%", "%SEND%", "%ARRIVE%", "%PLACE%", "%PLACE_URL%"};
    private final static String STANDARD_TEMPLATE = "%TYPE% von %ATTACKER% aus %SOURCE% mit %UNIT% auf %DEFENDER% in %TARGET% startet am [color=#ff0e0e]%SEND%[/color] und kommt am [color=#2eb92e]%ARRIVE%[/color] an (%PLACE%)";
    public static final int NO_TYPE = 0;
    public static final int CLEAN_TYPE = 1;
    public static final int SNOB_TYPE = 2;
    public static final int SUPPORT_TYPE = 3;
    public static final int FAKE_TYPE = 4;
    public static final int FAKE_DEFF_TYPE = 5;
    public static final int SPY_TYPE = 6;
    private static final long serialVersionUID = 10L;
    private Village source = null;
    private Village target = null;
    private UnitHolder unit = null;
    private Date arriveTime = null;
    private boolean showOnMap = false;
    private int type = 0;
    private boolean transferredToBrowser = false;

    public Attack() {
        try {
            showOnMap = Boolean.parseBoolean(GlobalOptions.getProperty("draw.attacks.by.default"));
        } catch (Exception e) {
        }
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

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    private boolean canUseSnob() {
        if (getSource() == null || getTarget() == null) {
            return true;
        }
        if (DSCalculator.calculateDistance(getSource(), getTarget()) < ServerSettings.getSingleton().getSnobRange()) {
            return true;
        }
        return false;
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
            this.arriveTime = new Date((long) Math.floor((double) arriveTime.getTime() / 1000.0) * 1000l);
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

    public boolean isShowOnMap() {
        return showOnMap;
    }

    public void setShowOnMap(boolean showOnMap) {
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
    public void setType(int type) {
        this.type = type;
    }
    /*
    <attack>
    <source>VillageID</source>
    <target>VillageID</target>
    <arrive>Timestamp</arrive>
    <unit>Name</unit>
    <extensions>
    <showOnMap>true</showOnMap>
    <type>0</type>
    </extensions>
    </attack>
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
    public void setTransferredToBrowser(boolean transferredToBrowser) {
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
        if (GlobalOptions.getSelectedProfile() != null) {
            uvID = GlobalOptions.getSelectedProfile().getUVId();
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
