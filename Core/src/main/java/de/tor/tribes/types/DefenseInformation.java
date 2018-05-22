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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.xml.JaxenUtils;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class DefenseInformation {

    public enum DEFENSE_STATUS {

        UNKNOWN, DANGEROUS, FINE, SAVE
    }

    private Village target = null;
    private TargetInformation targetInfo = null;
    private List<Defense> defenses = new LinkedList<>();
    private DEFENSE_STATUS status = DEFENSE_STATUS.UNKNOWN;
    private double lossRatio = 0.0;
    private int neededSupports = 0;
    private int cleanAfter = 0;
    private boolean analyzed = false;
    private boolean ignored = false;

    public DefenseInformation(TargetInformation pInfo) {
        targetInfo = pInfo;
        target = targetInfo.getTarget();
    }

    public boolean addSupport(final Village pSource, UnitHolder pUnit, boolean pPrimary, boolean pMultiUse) {
        long runtime = DSCalculator.calculateMoveTimeInMillis(pSource, target, pUnit.getSpeed());
        boolean allowed = false;
        if (getFirstAttack().getTime() - runtime > System.currentTimeMillis() + DateUtils.MILLIS_PER_MINUTE) {
            //high priority
            allowed = true;
        } else if (getLastAttack().getTime() - runtime > System.currentTimeMillis() + DateUtils.MILLIS_PER_MINUTE) {
            //low priority
            allowed = !pPrimary;
        } else {// if (getLastAttack().getTime() - runtime < System.currentTimeMillis() - DateUtils.MILLIS_PER_MINUTE) {
            //impossible
        }
        if (allowed) {
            Object result = CollectionUtils.find(defenses, new Predicate() {

                @Override
                public boolean evaluate(Object o) {
                    return ((Defense) o).getSupporter().equals(pSource);
                }
            });
            if (result == null || pMultiUse) {
                defenses.add(new Defense(this, pSource, pUnit));
                return true;
            }
        }
        return false;
    }

    private void addSupportInternal(Village pSource, UnitHolder pUnit, boolean pTransferred) {
        Defense d = new Defense(this, pSource, pUnit);
        d.setTransferredToBrowser(pTransferred);
        defenses.add(d);
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public Defense[] getSupports() {
        return defenses.toArray(new Defense[defenses.size()]);
    }

    public boolean isSave() {
        return isDefenseAvailable() && neededSupports == getSupports().length;
    }

    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public boolean isDefenseAvailable() {
        return !defenses.isEmpty();
    }

    public void reset() {
        defenses.clear();
        lossRatio = 0.0;
        neededSupports = 0;
        cleanAfter = 0;
        status = DEFENSE_STATUS.UNKNOWN;
        this.analyzed = false;
    }

    public void setTargetInformation(TargetInformation pInfo) {
        targetInfo = pInfo;
    }

    public TargetInformation getTargetInformation() {
        return targetInfo;
    }

    public void setDefenseStatus(DEFENSE_STATUS pStatus) {
        status = pStatus;
    }

    public DEFENSE_STATUS getStatus() {
        return status;
    }

    public double getLossRatio() {
        return lossRatio;
    }

    public void setLossRation(double pValue) {
        lossRatio = pValue;
    }

    public void setCleanAfter(int pValue) {
        cleanAfter = pValue;
    }

    public int getCleanAfter() {
        return cleanAfter;
    }

    public void setTarget(Village pTarget) {
        target = pTarget;
    }

    public Village getTarget() {
        return target;
    }

    public int getWallLevel() {
        return targetInfo.getWallLevel();
    }

    public int getAttackCount() {
        return targetInfo.getOffs();
    }

    public int getFakeCount() {
        return targetInfo.getFakes();
    }

    public Date getFirstAttack() {
        return new Date(targetInfo.getFirstAttack());
    }

    public Date getLastAttack() {
        return new Date(targetInfo.getLastAttack());
    }

    public int getDelta() {
        return targetInfo.getDelta();
    }

    public int getNeededSupports() {
        return neededSupports;
    }

    private int getNeededSupportsInternal() {
        return neededSupports;
    }

    public void setNeededSupports(int pValue) {
        neededSupports = pValue;
    }

    public String toXml() {
        StringBuilder b = new StringBuilder();
        b.append("<status>").append(status.toString()).append("</status>\n");
        b.append("<lossRatio>").append(Double.toString(lossRatio)).append("</lossRatio>\n");
        b.append("<neededSupports>").append(neededSupports).append("</neededSupports>\n");
        b.append("<cleanAfter>").append(cleanAfter).append("</cleanAfter>\n");
        if (!defenses.isEmpty()) {
            b.append("<defenses>\n");
            for (Defense d : defenses) {
                b.append("<defense " + "unit=\"").append(d.getUnit().getPlainName()).
                        append("\" id=\"").append(d.getSupporter().getId()).
                        append("\" transferred=\"").append(d.isTransferredToBrowser()).append("\"/>\n");
            }
            b.append("</defenses>\n");
        }

        return b.toString();
    }

    public void loadFromXml(Element e) {
        status = DEFENSE_STATUS.valueOf(e.getChild("status").getText());
        lossRatio = Double.parseDouble(e.getChild("lossRatio").getText());
        neededSupports = Integer.parseInt(e.getChild("neededSupports").getText());
        cleanAfter = Integer.parseInt(e.getChild("cleanAfter").getText());
        Element defenseElement = e.getChild("defenses");
        if (defenseElement != null) {
            for (Element defense : (List<Element>) JaxenUtils.getNodes(e, "defenses/defense")) {
                Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(defense.getAttributeValue("id")));
                UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(defense.getAttributeValue("unit"));
                boolean transferred = Boolean.parseBoolean(defense.getAttributeValue("transferred"));
                addSupportInternal(v, unit, transferred);
            }
        }
    }

    public void updateAttackInfo() {
        targetInfo.updateAttackInfo();
    }
}
