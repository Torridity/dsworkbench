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

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.bb.AttackListFormatter;
import de.tor.tribes.util.support.SOSFormater;
import de.tor.tribes.util.xml.JaxenUtils;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class SOSRequest extends ManageableType implements BBSupport {

    private final String[] VARIABLES = new String[]{"%SOS_ICON%", "%TARGET%", "%ATTACKS%", "%DEFENDERS%", "%WALL_INFO%", "%WALL_LEVEL%", "%FIRST_ATTACK%", "%LAST_ATTACK%", "%SOURCE_LIST%", "%SOURCE_DATE_TYPE_LIST%", "%ATTACK_LIST%", "%SOURCE_DATE_LIST%", "%SOURCE_TYPE_LIST%", "%SUMMARY%"};
    private final static String STANDARD_TEMPLATE = "[quote]%SOS_ICON% %TARGET% (%ATTACKS%)\n[quote]%DEFENDERS%\n%WALL_INFO%[/quote]\n\n%FIRST_ATTACK%\n%SOURCE_DATE_LIST%\n%LAST_ATTACK%\n\n%SUMMARY%[/quote]";
    private Tribe mDefender = null;
    private Hashtable<Village, TargetInformation> targetInformations = null;
    private Hashtable<Village, DefenseInformation> defenseInformations = null;

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    public String[] getReplacementsForTarget(Village pTarget, boolean pExtended) {
        String serverURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
        //main quote

        //village info size
        String sosImageVal = "[img]" + serverURL + "/graphic/reqdef.png[/img]";
        String targetVal = pTarget.toBBCode();
        String attackCountVal = "[img]" + serverURL + "/graphic/unit/att.png[/img] " + targetInformations.get(pTarget).getAttacks().size();
        //village details quote

        //add units and wall
        String unitVal = buildUnitInfo(targetInformations.get(pTarget));
        String wallInfoVal = "[img]" + serverURL + "/graphic/buildings/wall.png[/img] " + buildWallInfo(targetInformations.get(pTarget));
        String wallLevelVal = Integer.toString(targetInformations.get(pTarget).getWallLevel());

        //build first-last-attack

        List<TimedAttack> atts = targetInformations.get(pTarget).getAttacks();

        Collections.sort(atts, SOSRequest.ARRIVE_TIME_COMPARATOR);

        //add first and last attack information
        SimpleDateFormat dateFormat = null;
        if (ServerSettings.getSingleton().isMillisArrival()) {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        } else {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
        String firstAttackVal = "[img]" + serverURL + "/graphic/map/attack.png[/img] " + dateFormat.format(new Date(atts.get(0).getlArriveTime()));

        //add details for all attacks
        int fakeCount = 0;
        int snobCount = 0;
        String sourceVal = "";
        String sourceDateVal = "";
        String sourceDateTypeVal = "";
        String attackList = "";
        String sourceTypeVal = "";
        List<Attack> thisAttacks = new ArrayList<>();
        for (TimedAttack att1 : atts) {
            try {
                Attack a = new Attack();
                a.setSource(att1.getSource());
                a.setTarget(pTarget);
                a.setArriveTime(new Date(att1.getlArriveTime()));
                if (att1.isPossibleFake()) {
                    fakeCount++;
                    a.setType(Attack.FAKE_TYPE);
                } else if (att1.isPossibleSnob()) {
                    snobCount++;
                    a.setType(Attack.SNOB_TYPE);
                    a.setUnit(DataHolder.getSingleton().getUnitByPlainName("snob"));
                }
                if (a.getUnit() == null) {
                    a.setUnit(UnknownUnit.getSingleton());
                }
                thisAttacks.add(a);
            } catch (Exception ignored) {
            }
        }

        attackList = new AttackListFormatter().formatElements(thisAttacks, pExtended);

        for (TimedAttack att : atts) {
            try {
                sourceVal += att.getSource().toBBCode() + "\n";
                if (att.isPossibleFake()) {
                    sourceDateTypeVal += att.getSource().toBBCode() + " " + dateFormat.format(new Date(att.getlArriveTime())) + " [b](Fake)[/b]" + "\n";
                    sourceDateVal += att.getSource().toBBCode() + " " + dateFormat.format(new Date(att.getlArriveTime())) + "\n";
                    sourceTypeVal += att.getSource().toBBCode() + " [b](Fake)[/b]" + "\n";
                } else if (att.isPossibleSnob()) {
                    sourceDateTypeVal += att.getSource().toBBCode() + " " + dateFormat.format(new Date(att.getlArriveTime())) + " [b](AG)[/b]" + "\n";
                    sourceDateVal += att.getSource().toBBCode() + " " + dateFormat.format(new Date(att.getlArriveTime())) + "\n";
                    sourceTypeVal += att.getSource().toBBCode() + " [b](AG)[/b]" + "\n";
                } else {
                    sourceDateTypeVal += att.getSource().toBBCode() + " " + dateFormat.format(new Date(att.getlArriveTime())) + "\n";
                    sourceDateVal += att.getSource().toBBCode() + " " + dateFormat.format(new Date(att.getlArriveTime())) + "\n";
                    sourceTypeVal += att.getSource().toBBCode() + "\n";
                }
            } catch (Exception ignored) {
            }
        }

        sourceVal = sourceVal.trim();
        sourceTypeVal = sourceTypeVal.trim();
        sourceDateVal = sourceDateVal.trim();
        sourceDateTypeVal = sourceDateTypeVal.trim();
        String lastAttackVal = "[img]" + serverURL + "/graphic/map/return.png[/img] " + dateFormat.format(new Date(atts.get(atts.size() - 1).getlArriveTime()));
        String summaryVal = "[u]Mögliche Fakes:[/u] " + fakeCount + "\n" + "[u]Mögliche AGs:[/u] " + snobCount;

        return new String[]{sosImageVal, targetVal, attackCountVal, unitVal, wallInfoVal, wallLevelVal, firstAttackVal, lastAttackVal, sourceVal, sourceDateTypeVal, attackList, sourceDateVal, sourceTypeVal, summaryVal};
    }

    private String buildUnitInfo(TargetInformation pTargetInfo) {
        StringBuilder buffer = new StringBuilder();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String defRow = "";
        String offRow = "";

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = pTargetInfo.getTroops().getAmountForUnit(unit);
            if (amount > 0) {
                if (unit.isDefense()) {
                    defRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                } else {
                    offRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                }
            }
        }
        if (defRow.length() > 1) {
            buffer.append(defRow.trim()).append("\n");
        }
        if (offRow.length() > 1) {
            buffer.append(offRow.trim()).append("\n");
        }
        return buffer.toString();
    }

    private String buildWallInfo(TargetInformation pTargetInfo) {
        StringBuilder buffer = new StringBuilder();
        double perc = pTargetInfo.getWallLevel() / 20.0;
        int filledFields = (int) Math.rint(perc * 15.0);
        buffer.append("[color=#00FF00]");
        for (int i = 0; i < filledFields; i++) {
            buffer.append("█");
        }
        buffer.append("[/color]");
        if (filledFields < 15) {
            buffer.append("[color=#EEEEEE]");
            for (int i = 0; i < (15 - filledFields); i++) {
                buffer.append("█");
            }
            buffer.append("[/color]");
        }

        buffer.append(" (").append(pTargetInfo.getWallLevel()).append(")");
        return buffer.toString();
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        return getReplacementsForTarget(targetInformations.keys().nextElement(), pExtended);
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    public SOSRequest() {
        targetInformations = new Hashtable<>();
        defenseInformations = new Hashtable<>();
    }

    public SOSRequest(Tribe pDefender) {
        this();
        mDefender = pDefender;
    }

    public final void setDefender(Tribe pDefender) {
        mDefender = pDefender;
    }

    public Tribe getDefender() {
        return mDefender;
    }

    public TargetInformation addTarget(Village pTarget) {
        TargetInformation targetInfo = targetInformations.get(pTarget);
        if (targetInfo == null) {
            targetInfo = new TargetInformation(pTarget);
            targetInformations.put(pTarget, targetInfo);
            addDefense(pTarget);
        }
        return targetInfo;
    }

    private DefenseInformation addDefense(Village pTarget) {
        DefenseInformation defenseInfo = defenseInformations.get(pTarget);
        if (defenseInfo == null) {
            defenseInfo = new DefenseInformation(getTargetInformation(pTarget));
            defenseInfo.setAnalyzed(false);
            defenseInformations.put(pTarget, defenseInfo);
        }
        return defenseInfo;
    }

    public void resetDefenses() {
        Enumeration<Village> targets = getTargets();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            DefenseInformation info = getDefenseInformation(target);
            info.reset();
        }

    }

    public TargetInformation getTargetInformation(Village pTarget) {
        return targetInformations.get(pTarget);
    }

    public DefenseInformation getDefenseInformation(Village pTarget) {
        return defenseInformations.get(pTarget);
    }

    public void removeTarget(Village pTarget) {
        targetInformations.remove(pTarget);
        defenseInformations.remove(pTarget);
    }

    public Enumeration<Village> getTargets() {
        return targetInformations.keys();
    }

    public String toBBCode() {
        return toBBCode(true);
    }

    public String toBBCode(boolean pDetailed) {
        StringBuilder buffer = new StringBuilder();
        Enumeration<Village> targets = getTargets();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            TargetInformation targetInfo = getTargetInformation(target);
            buffer.append(SOSFormater.format(target, targetInfo, pDetailed));
            buffer.append("\n\n");
        }
        return buffer.toString();
    }

    public String toBBCode(Village pTarget, boolean pDetailed) {
        StringBuilder buffer = new StringBuilder();
        TargetInformation targetInfo = getTargetInformation(pTarget);
        if (targetInfo == null) {
            return "";
        }
        buffer.append(SOSFormater.format(pTarget, targetInfo, pDetailed));
        return buffer.toString();
    }

    public void merge(SOSRequest pOther) {
        if (mDefender == null || pOther == null || pOther.getDefender() == null || mDefender.getId() != pOther.getDefender().getId()) {
            throw new IllegalArgumentException("Cannot merge with unequal defender");
        }

        Enumeration<Village> otherTargets = pOther.getTargets();
        while (otherTargets.hasMoreElements()) {
            Village otherTarget = otherTargets.nextElement();
            TargetInformation otherInfo = pOther.getTargetInformation(otherTarget);
            TargetInformation thisInfo = addTarget(otherTarget);
            thisInfo.setDelta(0);
            thisInfo.setWallLevel(otherInfo.getWallLevel());
            int addCount = 0;
            for (TimedAttack att : otherInfo.getAttacks()) {
                if (thisInfo.addAttack(att.getSource(), new Date(att.getlArriveTime()),
                        att.getUnit(), att.isPossibleFake(), att.isPossibleSnob())) {
                    addCount++;
                    getDefenseInformation(otherTarget).setAnalyzed(false);
                }
            }
            thisInfo.setDelta(addCount);
        }
    }

    @Override
    public String toString() {
        String result = "Verteidiger: " + mDefender + "\n";
        Enumeration<Village> targets = getTargets();

        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            result += " Ziel: " + target + "\n";
            result += getTargetInformation(target);
            //result += "\n";
        }

        return result;
    }

    @Override
    public String getElementIdentifier() {
        return "sosRequest";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "sosRequests";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }

    @Override
    public String toXml() {
        try {
            StringBuilder b = new StringBuilder();
            b.append("<").append(getElementIdentifier()).append(" defender=\"").append(mDefender.getId()).append("\">\n");
            b.append("<targetInformations>\n");
            Enumeration<Village> targetKeys = getTargets();
            while (targetKeys.hasMoreElements()) {
                Village target = targetKeys.nextElement();
                TargetInformation targetInfo = getTargetInformation(target);
                if (targetInfo != null) {
                    b.append("<targetInformation target=\"").append(target.getId()).append("\">\n");
                    b.append(targetInfo.toXml());
                    b.append("</targetInformation>\n");
                }
            }
            b.append("</targetInformations>\n");
            b.append("<defenseInformations>\n");
            targetKeys = getTargets();
            while (targetKeys.hasMoreElements()) {
                Village target = targetKeys.nextElement();
                DefenseInformation defense = getDefenseInformation(target);
                if (defense != null) {
                    b.append("<defenseInformation target=\"").
                            append(target.getId()).
                            append("\" analyzed=\"").
                            append(defense.isAnalyzed()).
                            append("\" ignored=\"").
                            append(defense.isIgnored()).append("\">\n");
                    b.append(defense.toXml());
                    b.append("</defenseInformation>\n");
                }
            }
            b.append("</defenseInformations>\n");
            b.append("</").append(getElementIdentifier()).append(">");
            return b.toString();
        } catch (Throwable t) {
            //getting xml data failed
        }
        return null;
    }

    @Override
    public void loadFromXml(Element e) {
        int defenderId = Integer.parseInt(e.getAttributeValue("defender"));
        mDefender = DataHolder.getSingleton().getTribes().get(defenderId);
        for (Element targetInfo : (List<Element>) JaxenUtils.getNodes(e, "targetInformations/targetInformation")) {
            int targetId = Integer.parseInt(targetInfo.getAttributeValue("target"));
            Village target = DataHolder.getSingleton().getVillagesById().get(targetId);
            addTarget(target);
            getTargetInformation(target).loadFromXml(targetInfo);
        }
        for (Element defenseInfo : (List<Element>) JaxenUtils.getNodes(e, "defenseInformations/defenseInformation")) {
            int targetId = Integer.parseInt(defenseInfo.getAttributeValue("target"));
            boolean analyzed = Boolean.parseBoolean(defenseInfo.getAttributeValue("analyzed"));
            boolean ignored = Boolean.parseBoolean(defenseInfo.getAttributeValue("ignored"));
            Village target = DataHolder.getSingleton().getVillagesById().get(targetId);
            DefenseInformation info = addDefense(target);
            info.loadFromXml(defenseInfo);
            info.setAnalyzed(analyzed);
            info.setIgnored(ignored);
        }
    }
    public static final Comparator<TimedAttack> ARRIVE_TIME_COMPARATOR = new ArriveTimeComparator();

    private static class ArriveTimeComparator implements Comparator<TimedAttack>, java.io.Serializable {

        @Override
        public int compare(TimedAttack s1, TimedAttack s2) {
            return s1.getlArriveTime().compareTo(s2.getlArriveTime());
        }
    }
}
