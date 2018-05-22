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
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.xml.JaxenUtils;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class TargetInformation {
    
    private Village target = null;
    private Hashtable<Village, List<TimedAttack>> timedAttacks = null;
    private int iWallLevel = 20;
    private TroopAmountFixed troops = null;
    private int delta = 0;
    private int snobs = 0;
    private int fakes = 0;
    private long first = Long.MAX_VALUE;
    private long last = Long.MIN_VALUE;
    private final Logger logger = Logger.getLogger("TargetInformation");
    
    public TargetInformation(Village pTarget) {
        target = pTarget;
        troops = new TroopAmountFixed();
        timedAttacks = new Hashtable<>();
    }
    
    public void updateAttackInfo() {
        snobs = 0;
        fakes = 0;
        first = Long.MAX_VALUE;
        last = Long.MIN_VALUE;
        for (TimedAttack a : getAttacks()) {
            if (a.isPossibleFake()) {
                fakes++;
            } else if (a.isPossibleSnob()) {
                snobs++;
            }
            if (a.getlArriveTime() < first) {
                first = a.getlArriveTime();
            }
            if (a.getlArriveTime() > last) {
                last = a.getlArriveTime();
            }
        }
        logger.debug(target.getCoordAsString() + " found " + snobs + " snobs and " + fakes + " fakes");
    }
    
    public void setTarget(Village target) {
        this.target = target;
    }
    
    public Village getTarget() {
        return target;
    }
    
    public int getDelta() {
        return delta;
    }
    
    public void setDelta(int pDelta) {
        delta = pDelta;
    }

    /**
     * @return the attacks
     */
    public List<TimedAttack> getAttacks() {
        Enumeration<Village> sourceKeys = timedAttacks.keys();
        List<TimedAttack> result = new LinkedList<>();
        while (sourceKeys.hasMoreElements()) {
            Village source = sourceKeys.nextElement();
            List<TimedAttack> attacksFromSource = timedAttacks.get(source);
            for (TimedAttack a : attacksFromSource) {
                result.add(a);
            }
        }
        return result;
    }
    
    public int getSourceCount() {
        return timedAttacks.size();
    }
    
    public Enumeration<Village> getSources() {
        return timedAttacks.keys();
    }
    
    public int getAttackCountFromSource(Village pSource) {
        List<TimedAttack> attacksForSource = timedAttacks.get(pSource);
        if (attacksForSource == null || attacksForSource.isEmpty()) {
            return 0;
        }
        return attacksForSource.size();
    }
    
    public List<TimedAttack> getAttacksFromSource(Village pSource) {
        List<TimedAttack> atts = timedAttacks.get(pSource);
        if (atts != null) {
            return atts;
        } else {
            return new LinkedList<>();
        }
    }
    
    public boolean addAttack(final Village pSource, final Date pArrive, boolean fake, boolean snob) {
        return addAttack(pSource, pArrive, null, fake, snob);
    }
    
    public boolean addAttack(final Village pSource, final Date pArrive, UnitHolder pUnit, boolean fake, boolean snob) {
        List<TimedAttack> attacksFromSource = timedAttacks.get(pSource);
        if (attacksFromSource == null) {
            attacksFromSource = new LinkedList<>();
            timedAttacks.put(pSource, attacksFromSource);
        }
        
        Object result = CollectionUtils.find(attacksFromSource, new Predicate() {
            
            @Override
            public boolean evaluate(Object o) {
                TimedAttack t = (TimedAttack) o;
                return t.getSource().equals(pSource) && t.getlArriveTime().equals(pArrive.getTime());
            }
        });
        
        if (result == null) {
            TimedAttack a = new TimedAttack(pSource, pArrive);
            a.setPossibleFake(fake);
            a.setPossibleSnob(snob);
            a.setUnit(pUnit);
            attacksFromSource.add(a);
            Collections.sort(attacksFromSource, SOSRequest.ARRIVE_TIME_COMPARATOR);
            updateAttackInfo();
            return true;
        }
        return false;
    }

    public boolean addAttack(final Village pSource, final Date pArrive) {
        return addAttack(pSource, pArrive, null);
    }

    public boolean addAttack(final Village pSource, final Date pArrive, UnitHolder pUnit) {
        return addAttack(pSource, pArrive, false, false);
    }
    
    public int getFakes() {
        return fakes;
    }
    
    public int getSnobs() {
        return snobs;
    }
    
    public int getOffs() {
        return getAttacks().size() - fakes;
    }
    
    public long getFirstAttack() {
        return first;
    }
    
    public TimedAttack getFirstTimedAttack() {
        Enumeration<Village> sources = timedAttacks.keys();
        while (sources.hasMoreElements()) {
            Village source = sources.nextElement();
            List<TimedAttack> attsForSource = timedAttacks.get(source);
            for (TimedAttack a : attsForSource) {
                if (a.getlArriveTime() == first) {
                    return a;
                }
            }
        }
        return null;
    }
    
    public long getLastAttack() {
        return last;
    }

    /**
     * @return the iWallLevel
     */
    public int getWallLevel() {
        return iWallLevel;
    }

    /**
     * @param iWallLevel the iWallLevel to set
     */
    public void setWallLevel(int iWallLevel) {
        this.iWallLevel = iWallLevel;
    }

    /**
     * @return the troops
     */
    public TroopAmountFixed getTroops() {
        return troops;
    }

    /**
     * @param pUnit the Unit wich's amount should be set
     * @param pAmount the amount to set
     */
    public void addTroopInformation(UnitHolder pUnit, int pAmount) {
        troops.setAmountForUnit(pUnit, pAmount);
    }

    /**
     * @param pTroops the troops to set
     */
    public void setTroops(TroopAmountFixed pTroops) {
        this.troops = pTroops.clone();
    }
    
    public String getTroopInformationAsHTML() {
        StringBuilder b = new StringBuilder();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Integer amount = troops.getAmountForUnit(unit);
            if (amount != null) {
                b.append("<img src=\"").append(SOSRequest.class.getResource("/res/ui/" + unit.getPlainName() + ".png")).append("\"/>&nbsp;").append(amount).append("\n");
            }
        }
        return b.toString();
    }
    
    public void merge(TargetInformation pInfo) {
        if (pInfo == null) {
            logger.error("Tryed to merge Target info with null");
            return;
        }
        boolean millis = ServerSettings.getSingleton().isMillisArrival();
        List<TimedAttack> thisAttacks = getAttacks();
        int attCount = thisAttacks.size();
        List<TimedAttack> theOtherAttacks = null;
            theOtherAttacks = pInfo.getAttacks();
        TroopAmountFixed theOtherTroopInfo = pInfo.getTroops();
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            addTroopInformation(unit, theOtherTroopInfo.getAmountForUnit(unit));
        }
        if (theOtherAttacks != null) {
            for (TimedAttack theOtherAttack : theOtherAttacks) {
                boolean add = true;
                if (millis) {
                    //only check if millis are enabled...otherwise we should keep all attacks, as we cannot separate them clearly
                    for (TimedAttack theNewAttack : getAttacks()) {
                        //go through all attacks and check if one is equal
                        if (theNewAttack.getSource().equals(theOtherAttack.getSource()) && theNewAttack.getlArriveTime().equals(theOtherAttack.getlArriveTime())) {
                            //attack seems to be the same...skip adding
                            add = false;
                            break;
                        }
                    }
                }
                if (add) {
                    //attack seems not to exist...add it
                    addAttack(theOtherAttack.getSource(), new Date(theOtherAttack.getlArriveTime()),
                        theOtherAttack.getUnit(), theOtherAttack.isPossibleFake(), theOtherAttack.isPossibleSnob());
                }
            }
        }
        delta = getAttacks().size() - attCount;
        updateAttackInfo();
    }
    
    @Override
    public String toString() {
        String result = " Stufe des Walls: " + iWallLevel + "\n";
        if (troops.containsInformation()) {
            result += " Truppen im Dorf: -Keine Informationen-\n\n";
        } else {
            result += " Truppen im Dorf:\n";
            for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
                result += "  " + troops.getAmountForUnit(unit) + " " + unit + "\n";
            }
        }
        result += "\n";
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
        for (TimedAttack attack : getAttacks()) {
            result += " * " + attack.getSource() + "(" + format.format(new Date(attack.getlArriveTime())) + ")\n";
        }
        result += "\n";
        return result;
    }
    
    public String toXml() {
        StringBuilder b = new StringBuilder();
        b.append("<wall>").append(iWallLevel).append("</wall>\n");
        b.append("<troops ").append(troops.toXml()).append("< />\n");
        b.append("<attacks>\n");
        for (TimedAttack a : getAttacks()) {
            b.append("<attack source=\"").append(a.getSource().getId()).append("\" arrive=\"").append(a.getlArriveTime()).append("\"").
                    append(" fake=\"").append(a.isPossibleFake()).append("\" snob=\"").append(a.isPossibleSnob()).
                    append("\"/>\n");
        }
        b.append("</attacks>\n");
        return b.toString();
    }
    
    public void loadFromXml(Element e) {
        this.iWallLevel = Integer.parseInt(e.getChild("wall").getText());
        troops = new TroopAmountFixed(e.getChild("troops"));
        
        for (Element attack : (List<Element>) JaxenUtils.getNodes(e, "attacks/attack")) {
            int sourceId = Integer.parseInt(attack.getAttributeValue("source"));
            long arrive = Long.parseLong(attack.getAttributeValue("arrive"));
            boolean fake = Boolean.parseBoolean(attack.getAttributeValue("fake"));
            boolean snob = Boolean.parseBoolean(attack.getAttributeValue("snob"));
            addAttack(DataHolder.getSingleton().getVillagesById().get(sourceId), new Date(arrive), fake, snob);
        }
    }
}
