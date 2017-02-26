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
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.php.LuckViewInterface;
import de.tor.tribes.php.UnitTableInterface;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.InvalidTribe;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.TroopHelper;
import de.tor.tribes.util.xml.JaxenUtils;
import de.tor.tribes.util.xml.XMLHelper;
import org.jdom.Document;
import org.jdom.Element;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Torridity
 */
public class FightReport extends ManageableType implements Comparable<FightReport>, BBSupport {

    private final static String[] VARIABLES = new String[]{"%ATTACKER%", "%SOURCE%", "%DEFENDER%", "%TARGET%", "%SEND_TIME%", "%RESULT%", "%LUCK%", "%MORALE%", "%ATTACKER_TROOPS%",
        "%DEFENDER_TROOPS%", "%DEFENDERS_OUTSIDE%", "%DEFENDERS_EN_ROUTE%", "%LOYALITY_CHANGE%", "%WALL_CHANGE%", "%BUILDING_CHANGE%"};
    private final static String STANDARD_TEMPLATE = "[quote][i][b]Betreff:[/b][/i] %ATTACKER% greift %TARGET% an\n[i][b]Gesendet:[/b][/i] %SEND_TIME%\n[size=16]%RESULT%[/size]\n"
            + "[b]Glück:[/b] %LUCK%\n[b]Moral:[/b] %MORALE%\n\n[b]Angreifer:[/b] %ATTACKER%\n[b]Dorf:[/b] %SOURCE%\n%ATTACKER_TROOPS%\n\n[b]Verteidiger:[/b] %DEFENDER%\n"
            + "[b]Dorf:[/b] %TARGET%\n %DEFENDER_TROOPS%\n\n%DEFENDERS_OUTSIDE%\n%DEFENDERS_EN_ROUTE%\n%LOYALITY_CHANGE%\n%WALL_CHANGE%\n%BUILDING_CHANGE%[/quote]";

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String attackerVal = attacker.toBBCode();
        String targetVal = targetVillage.toBBCode();
        SimpleDateFormat d = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String sendDateVal = d.format(new Date(timestamp));
        String resultVal = (won) ? "Der Angreifer hat gewonnen" : "Der Verteidiger hat gewonnen";

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMinimumFractionDigits(1);

        String luckVal = "[img]" + LuckViewInterface.createLuckIndicator(luck) + "[/img] " + nf.format(luck) + "%";
        nf.setMinimumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        String moraleVal = nf.format(moral) + " %";

        String sourceVal = sourceVillage.toBBCode();
        String attackerTroopsVal = (areAttackersHidden())
                ? "Durch Besitzer des Berichts verborgen"
                : "[img]" + UnitTableInterface.createAttackerUnitTableLink(attackers, diedAttackers) + "[/img]";

        String defenderVal = defender.toBBCode();
        String defenderTroopsVal = (wasLostEverything())
                ? "Keiner deiner Kämpfer ist lebend zurückgekehrt.\nEs konnten keine Informationen über die Truppenstärke des Gegners erlangt werden."
                : "[img]" + UnitTableInterface.createDefenderUnitTableLink(defenders, diedDefenders) + "[/img]";

        String troopsEnRouteVal = (whereDefendersOnTheWay())
                ? "[b]Truppen des Verteidigers, die unterwegs waren[/b]\n\n" + "[img]" + UnitTableInterface.createAttackerUnitTableLink(defendersOnTheWay) + "[/img]"
                : "";
        String troopsOutsideVal = "";
        if (whereDefendersOutside()) {
            Enumeration<Village> targetKeys = defendersOutside.keys();
            while (targetKeys.hasMoreElements()) {
                Village target = targetKeys.nextElement();
                troopsOutsideVal += target.toBBCode() + "\n\n";
                troopsOutsideVal += "[img]" + UnitTableInterface.createAttackerUnitTableLink(defendersOutside.get(target)) + "[/img]\n\n";
            }
        }

        String loyalityChangeVal = (wasSnobAttack())
                ? "[b]Veränderung der Zustimmung:[/b] Zustimmung gesunken von " + nf.format(getAcceptanceBefore()) + " auf " + getAcceptanceAfter()
                : "";

        String wallChangeVal = (wasWallDamaged())
                ? "[b]Schaden durch Rammen:[/b] Wall beschädigt von Level " + getWallBefore() + " auf Level " + getWallAfter()
                : "";
        String cataChangeVal = (wasBuildingDamaged())
                ? "[b]Schaden durch Katapultbeschuss:[/b] " + aimedBuilding + " beschädigt von Level " + getBuildingBefore() + " auf Level " + getBuildingAfter()
                : "";
        return new String[]{attackerVal, sourceVal, defenderVal, targetVal, sendDateVal, resultVal, luckVal, moraleVal, attackerTroopsVal, defenderTroopsVal, troopsOutsideVal, troopsEnRouteVal, loyalityChangeVal, wallChangeVal, cataChangeVal};
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }
    private boolean won = false;
    private long timestamp = 0;
    private double luck = 0.0;
    private double moral = 100.0;
    private Tribe attacker = null;
    private Village sourceVillage = null;
    private Hashtable<UnitHolder, Integer> attackers = null;
    private Hashtable<UnitHolder, Integer> diedAttackers = null;
    private Tribe defender = null;
    private Village targetVillage = null;
    private Hashtable<UnitHolder, Integer> defenders = null;
    private Hashtable<UnitHolder, Integer> diedDefenders = null;
    private Hashtable<Village, Hashtable<UnitHolder, Integer>> defendersOutside = null;
    private Hashtable<UnitHolder, Integer> defendersOnTheWay = null;
    private boolean conquered = false;
    private byte wallBefore = -1;
    private byte wallAfter = -1;
    private String aimedBuilding = null;
    private byte buildingBefore = -1;
    private byte buildingAfter = -1;
    private byte acceptanceBefore = 100;
    private byte acceptanceAfter = 100;
    private int[] spyedResources = null;
    private int[] haul = null;
    private int woodLevel = -1;
    private int clayLevel = -1;
    private int ironLevel = -1;
    private int storageLevel = -1;
    private int hideLevel = -1;
    private int wallLevel = -1;

    public FightReport() {
        attackers = new Hashtable<>();
        diedAttackers = new Hashtable<>();
        defenders = new Hashtable<>();
        diedDefenders = new Hashtable<>();
        defendersOutside = new Hashtable<>();
        defendersOnTheWay = new Hashtable<>();
    }

    public static String toInternalRepresentation(FightReport pReport) {
        String xml = pReport.toXml();
        xml = xml.replaceAll("\n", "");
        return xml;
    }

    public static FightReport fromInternalRepresentation(String pLine) {
        FightReport r = new FightReport();
        try {
            Document d = JaxenUtils.getDocument(pLine);
            r.loadFromXml((Element) JaxenUtils.getNodes(d, "//report").get(0));
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void loadFromXml(Element pElement) {
        if (pElement.getChild("ver") != null) {
            loadFromXml10(pElement);
            return;
        }

        try {
            this.timestamp = Long.parseLong(pElement.getChild("timestamp").getText());
            this.moral = Double.parseDouble(pElement.getChild("moral").getText());
            this.luck = Double.parseDouble(pElement.getChild("luck").getText());
            //attacker stuff
            Element attackerElement = pElement.getChild("attacker");
            Element defenderElement = pElement.getChild("defender");
            int source = Integer.parseInt(attackerElement.getChild("src").getText());
            this.sourceVillage = DataHolder.getSingleton().getVillagesById().get(source);

            int attackerId = Integer.parseInt(attackerElement.getChild("id").getText());
            Tribe attElement = DataHolder.getSingleton().getTribes().get(attackerId);
            if (attElement != null) {
                setAttacker(attElement);
            } else {
                if (attackerId != -666 && sourceVillage != null && sourceVillage.getTribe() != null) {
                    setAttacker(sourceVillage.getTribe());
                } else {
                    setAttacker(InvalidTribe.getSingleton());
                }
            }

            int target = Integer.parseInt(defenderElement.getChild("trg").getText());
            this.targetVillage = DataHolder.getSingleton().getVillagesById().get(target);
            int defenderId = Integer.parseInt(defenderElement.getChild("id").getText());
            Tribe defendingTribe = DataHolder.getSingleton().getTribes().get(defenderId);
            if (defendingTribe != null) {
                setDefender(defendingTribe);
            } else {
                if (defenderId > 0 && targetVillage != null && targetVillage.getTribe() != null) {
                    setDefender(targetVillage.getTribe());
                } else {
                    if (defenderId == -666) {
                        setDefender(InvalidTribe.getSingleton());
                    } else {
                        setDefender(Barbarians.getSingleton());
                    }
                }
            }

            Element aAmount = attackerElement.getChild("amount");
            Element aDiedAmount = attackerElement.getChild("died");
            Element dAmount = defenderElement.getChild("amount");
            Element dDiedAmount = defenderElement.getChild("died");
            Element dDefendersOnTheWay = null;

            try {
                dDefendersOnTheWay = defenderElement.getChild("ontheway");
            } catch (Exception ignored) {
            }

            Element dDefendersOutside = null;
            try {
                dDefendersOutside = defenderElement.getChild("outside");
            } catch (Exception ignored) {
            }

            attackers = new Hashtable<>();
            diedAttackers = new Hashtable<>();
            defenders = new Hashtable<>();
            diedDefenders = new Hashtable<>();
            defendersOnTheWay = new Hashtable<>();
            defendersOutside = new Hashtable<>();
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                String unitName = unit.getPlainName();
                attackers.put(unit, aAmount.getAttribute(unitName).getIntValue());
                diedAttackers.put(unit, aDiedAmount.getAttribute(unitName).getIntValue());
                defenders.put(unit, dAmount.getAttribute(unitName).getIntValue());
                diedDefenders.put(unit, dDiedAmount.getAttribute(unitName).getIntValue());
                if (dDefendersOnTheWay != null) {
                    defendersOnTheWay.put(unit, dDefendersOnTheWay.getAttribute(unitName).getIntValue());
                }
                if (dDefendersOutside != null) {
                    for (Element e : (List<Element>) JaxenUtils.getNodes(dDefendersOutside, "support")) {
                        int villageId = e.getAttribute("trg").getIntValue();
                        int amount = e.getAttribute(unitName).getIntValue();
                        Village v = DataHolder.getSingleton().getVillagesById().get(villageId);
                        if (v != null) {
                            Hashtable<UnitHolder, Integer> unitsInvillage = defendersOutside.get(v);
                            if (unitsInvillage == null) {
                                unitsInvillage = new Hashtable<>();
                                defendersOutside.put(v, unitsInvillage);
                            }
                            unitsInvillage.put(unit, amount);
                        }
                    }
                }
            }

            try {
                Element e = pElement.getChild("wall");
                this.wallBefore = Byte.parseByte(e.getAttribute("before").getValue());
                this.wallAfter = Byte.parseByte(e.getAttribute("after").getValue());
            } catch (Exception e) {
                this.wallBefore = (byte) -1;
                this.wallAfter = (byte) -1;
            }
            try {
                Element e = pElement.getChild("building");
                this.aimedBuilding = URLDecoder.decode(e.getAttribute("target").getValue(), "UTF-8");
                this.buildingBefore = Byte.parseByte(e.getAttribute("before").getValue());
                this.buildingAfter = Byte.parseByte(e.getAttribute("after").getValue());
            } catch (Exception e) {
                this.buildingBefore = (byte) -1;
                this.buildingAfter = (byte) -1;
            }
            try {
                Element e = pElement.getChild("acceptance");
                this.acceptanceBefore = Byte.parseByte(e.getAttribute("before").getValue());
                this.acceptanceAfter = Byte.parseByte(e.getAttribute("after").getValue());
            } catch (Exception e) {
                this.acceptanceBefore = (byte) 100;
                this.acceptanceAfter = (byte) 100;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Loader for version 1.0
     */
    public void loadFromXml10(Element pElement) {
        try {
            this.timestamp = Long.parseLong(pElement.getChild("timestamp").getText());
            this.moral = Double.parseDouble(pElement.getChild("moral").getText());
            this.luck = Double.parseDouble(pElement.getChild("luck").getText());
            //attacker stuff
            Element attackerElement = pElement.getChild("attacker");
            Element defenderElement = pElement.getChild("defender");
            int source = Integer.parseInt(attackerElement.getChild("src").getText());
            this.sourceVillage = DataHolder.getSingleton().getVillagesById().get(source);

            int attackerId = Integer.parseInt(attackerElement.getChild("id").getText());
            Tribe attElement = DataHolder.getSingleton().getTribes().get(attackerId);
            if (attElement != null) {
                setAttacker(attElement);
            } else {
                if (attackerId != -666 && sourceVillage != null && sourceVillage.getTribe() != null) {
                    setAttacker(sourceVillage.getTribe());
                } else {
                    setAttacker(InvalidTribe.getSingleton());
                }
            }

            int target = Integer.parseInt(defenderElement.getChild("trg").getText());
            this.targetVillage = DataHolder.getSingleton().getVillagesById().get(target);

            int defenderId = Integer.parseInt(defenderElement.getChild("id").getText());
            Tribe defendingTribe = DataHolder.getSingleton().getTribes().get(defenderId);
            if (defendingTribe != null) {
                setDefender(defendingTribe);
            } else {
                if (defenderId > 0 && targetVillage != null && targetVillage.getTribe() != null) {
                    setDefender(targetVillage.getTribe());
                } else {
                    if (defenderId == -666) {
                        setDefender(InvalidTribe.getSingleton());
                    } else {
                        setDefender(Barbarians.getSingleton());
                    }
                }
            }

            Element aAmount = attackerElement.getChild("before");
            Element aDiedAmount = attackerElement.getChild("died");
            Element dAmount = defenderElement.getChild("before");
            Element dDiedAmount = defenderElement.getChild("died");
            Element dDefendersOnTheWay = defenderElement.getChild("otw");
            Element dDefendersOutside = defenderElement.getChild("outside");

            this.attackers = XMLHelper.xmlToTroops(aAmount);
            this.diedAttackers = XMLHelper.xmlToTroops(aDiedAmount);
            this.defenders = XMLHelper.xmlToTroops(dAmount);
            this.diedDefenders = XMLHelper.xmlToTroops(dDiedAmount);

            if (dDefendersOnTheWay != null) {
                this.defendersOnTheWay = XMLHelper.xmlToTroops(dDefendersOnTheWay);
            }

            if (dDefendersOutside != null) {
                for (Element e : (List<Element>) dDefendersOutside.getChildren("target")) {
                    int id = Integer.parseInt(e.getAttributeValue("id"));
                    Village targetVillage = DataHolder.getSingleton().getVillagesById().get(id);
                    addDefendersOutside(targetVillage, XMLHelper.xmlToTroops(e));
                }
            }
            /*
             * attackers = new Hashtable<UnitHolder, Integer>(); diedAttackers = new Hashtable<UnitHolder, Integer>(); defenders = new
             * Hashtable<UnitHolder, Integer>(); diedDefenders = new Hashtable<UnitHolder, Integer>(); defendersOnTheWay = new
             * Hashtable<UnitHolder, Integer>(); defendersOutside = new Hashtable<Village, Hashtable<UnitHolder, Integer>>(); for
             * (UnitHolder unit : DataHolder.getSingleton().getUnits()) { String unitName = unit.getPlainName(); attackers.put(unit,
             * aAmount.getAttribute(unitName).getIntValue()); diedAttackers.put(unit, aDiedAmount.getAttribute(unitName).getIntValue());
             * defenders.put(unit, dAmount.getAttribute(unitName).getIntValue()); diedDefenders.put(unit,
             * dDiedAmount.getAttribute(unitName).getIntValue()); if (dDefendersOnTheWay != null) { defendersOnTheWay.put(unit,
             * dDefendersOnTheWay.getAttribute(unitName).getIntValue()); } if (dDefendersOutside != null) { for (Element e : (List<Element>)
             * JaxenUtils.getNodes(dDefendersOutside, "support")) { int villageId = e.getAttribute("trg").getIntValue(); int amount =
             * e.getAttribute(unitName).getIntValue(); Village v = DataHolder.getSingleton().getVillagesById().get(villageId); if (v !=
             * null) { Hashtable<UnitHolder, Integer> unitsInvillage = defendersOutside.get(v); if (unitsInvillage == null) { unitsInvillage
             * = new Hashtable<UnitHolder, Integer>(); defendersOutside.put(v, unitsInvillage); } unitsInvillage.put(unit, amount); } } } }
             */


            Element wallElement = pElement.getChild("wall");
            if (wallElement != null) {
                this.wallBefore = Byte.parseByte(wallElement.getAttribute("before").getValue());
                this.wallAfter = Byte.parseByte(wallElement.getAttribute("after").getValue());

            }
            Element buildingElement = pElement.getChild("building");
            if (buildingElement != null) {
                this.aimedBuilding = URLDecoder.decode(buildingElement.getAttribute("target").getValue(), "UTF-8");
                this.buildingBefore = Byte.parseByte(buildingElement.getAttribute("before").getValue());
                this.buildingAfter = Byte.parseByte(buildingElement.getAttribute("after").getValue());
            }

            Element accElement = pElement.getChild("acceptance");
            if (accElement != null) {
                this.acceptanceBefore = Byte.parseByte(accElement.getAttribute("before").getValue());
                this.acceptanceAfter = Byte.parseByte(accElement.getAttribute("after").getValue());
            }

            Element haul = pElement.getChild("haul");
            if (haul != null) {
                setHaul(Integer.parseInt(haul.getAttributeValue("wood")),
                        Integer.parseInt(haul.getAttributeValue("clay")),
                        Integer.parseInt(haul.getAttributeValue("iron")));
            }
            Element spy = pElement.getChild("spy");
            if (spy != null) {
                setSpyedResources(Integer.parseInt(spy.getAttributeValue("wood")),
                        Integer.parseInt(spy.getAttributeValue("clay")),
                        Integer.parseInt(spy.getAttributeValue("iron")));
            }

            Element buildings = pElement.getChild("buildings");
            if (buildings != null) {
                this.woodLevel = Integer.parseInt(buildings.getAttributeValue("wood"));
                this.clayLevel = Integer.parseInt(buildings.getAttributeValue("clay"));
                this.ironLevel = Integer.parseInt(buildings.getAttributeValue("iron"));
                this.storageLevel = Integer.parseInt(buildings.getAttributeValue("storage"));
                this.hideLevel = Integer.parseInt(buildings.getAttributeValue("hide"));
                if (buildingElement.getAttribute("wall") != null) {
                    this.wallLevel = Integer.parseInt(buildings.getAttributeValue("wall"));
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toXml() {
        StringBuilder b = new StringBuilder();
        try {
            b.append("<report>\n");
            //general part
            b.append("<ver>1.0</ver>\n");
            b.append("<timestamp>").append(timestamp).append("</timestamp>\n");
            b.append("<moral>").append(moral).append("</moral>\n");
            b.append("<luck>").append(luck).append("</luck>\n");
            //attacker part
            b.append("<attacker>\n");
            b.append("<id>").append(attacker.getId()).append("</id>\n");
            b.append("<src>").append(sourceVillage.getId()).append("</src>\n");
            b.append("<before>\n");
            b.append(XMLHelper.troopsToXML(attackers));
            b.append("</before>\n");
            b.append("<died>\n");
            b.append(XMLHelper.troopsToXML(diedAttackers));
            b.append("</died>\n");
            b.append("</attacker>\n");

            /*
             * String sAttackers = "<amount "; String sDiedAttackers = "<died "; String sDefenders = "<amount "; String sDiedDefenders =
             * "<died "; String sDefendersOnTheWay = null; if (whereDefendersOnTheWay()) { sDefendersOnTheWay = "<ontheway "; }
             *
             * Enumeration<UnitHolder> units = attackers.keys(); while (units.hasMoreElements()) { UnitHolder unit = units.nextElement();
             * sAttackers += unit.getPlainName() + "=\"" + attackers.get(unit) + "\" "; sDiedAttackers += unit.getPlainName() + "=\"" +
             * diedAttackers.get(unit) + "\" "; sDefenders += unit.getPlainName() + "=\"" + defenders.get(unit) + "\" "; sDiedDefenders +=
             * unit.getPlainName() + "=\"" + diedDefenders.get(unit) + "\" "; if (sDefendersOnTheWay != null) { sDefendersOnTheWay +=
             * unit.getPlainName() + "=\"" + defendersOnTheWay.get(unit) + "\" "; } } xml += sAttackers + "/>\n"; xml += sDiedAttackers +
             * "/>\n"; xml += "</attacker>\n";
             */
            //defender part
            b.append("<defender>\n");
            b.append("<id>").append(defender.getId()).append("</id>\n");
            b.append("<trg>").append(targetVillage.getId()).append("</trg>\n");
            b.append("<before>\n");
            b.append(XMLHelper.troopsToXML(defenders));
            b.append("</before>\n");
            b.append("<died>\n");
            b.append(XMLHelper.troopsToXML(diedDefenders));
            b.append("</died>\n");
            if (whereDefendersOnTheWay()) {
                b.append("<otw>\n");
                b.append(XMLHelper.troopsToXML(defendersOnTheWay));
                b.append("</otw>\n");
            }
            if (whereDefendersOutside()) {
                b.append("<outside>\n");
                Enumeration<Village> targetVillages = defendersOutside.keys();
                while (targetVillages.hasMoreElements()) {
                    Village target = targetVillages.nextElement();
                    b.append("<target id=\"").append(target.getId()).append("\">\n");
                    b.append(XMLHelper.troopsToXML(defendersOutside.get(target)));
                    b.append("</target>\n");
                }
                b.append("</outside>\n");
            }

            b.append("</defender>\n");

            if (wasWallDamaged()) {
                b.append("<wall before=\"").append(getWallBefore()).append("\" after=\"").append(getWallAfter()).append("\"/>\n");
            }
            if (wasBuildingDamaged()) {
                b.append("<building target=\"").append(URLEncoder.encode(aimedBuilding, "UTF-8")).append("\" before=\"").append(getBuildingBefore()).append("\" after=\"").append(getBuildingAfter()).append("\"/>\n");
            }
            if (wasSnobAttack()) {
                b.append("<acceptance before=\"").append(getAcceptanceBefore()).append("\" after=\"").append(getAcceptanceAfter()).append("\"/>\n");
            }

            if (haul != null) {
                b.append("<haul wood=\"").append(haul[0]).append("\" clay=\"").append(haul[1]).append("\" iron=\"").append(haul[2]).append("\"/>\n");
            }

            if (spyedResources != null) {
                b.append("<spy wood=\"").append(spyedResources[0]).append("\" clay=\"").append(spyedResources[1]).append("\" iron=\"").append(spyedResources[2]).append("\"/>\n");
            }

            b.append("<buildings wood=\"").
                    append(woodLevel).
                    append("\" clay=\"").
                    append(clayLevel).
                    append("\" iron=\"").
                    append(ironLevel).
                    append("\" storage=\"").
                    append(storageLevel).
                    append("\" hide=\"").
                    append(hideLevel).
                    append("\" wall=\"").
                    append(wallLevel).
                    append("\"/>\n");

            b.append("</report>");
            return b.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the attacker
     */
    public Tribe getAttacker() {
        return attacker;
    }

    /**
     * @param attacker the attacker to set
     */
    public void setAttacker(Tribe attacker) {
        if (attacker == null) {
            this.attacker = Barbarians.getSingleton();
        } else {
            this.attacker = attacker;
        }
    }

    /**
     * @return the sourceVillage
     */
    public Village getSourceVillage() {
        return sourceVillage;
    }

    /**
     * @param sourceVillage the sourceVillage to set
     */
    public void setSourceVillage(Village sourceVillage) {
        this.sourceVillage = sourceVillage;
    }

    /**
     * @return the attackers
     */
    public Hashtable<UnitHolder, Integer> getAttackers() {
        return attackers;
    }

    /**
     * @param attackers the attackers to set
     */
    public void setAttackers(Hashtable<UnitHolder, Integer> attackers) {
        this.attackers = attackers;
    }

    /**
     * @return the diedAttackers
     */
    public Hashtable<UnitHolder, Integer> getDiedAttackers() {
        return diedAttackers;
    }

    /**
     * @param diedAttackers the diedAttackers to set
     */
    public void setDiedAttackers(Hashtable<UnitHolder, Integer> diedAttackers) {
        this.diedAttackers = diedAttackers;
    }

    public Hashtable<UnitHolder, Integer> getSurvivingAttackers() {
        Hashtable<UnitHolder, Integer> result = null;
        if (!areAttackersHidden() && attackers != null && diedAttackers != null) {
            result = new Hashtable<>();
            Hashtable<UnitHolder, Integer> att = attackers;
            Hashtable<UnitHolder, Integer> diedAtt = diedAttackers;
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                try {
                    int survivors = att.get(unit) - diedAtt.get(unit);
                    survivors = (survivors >= 0) ? survivors : 0;
                    result.put(unit, survivors);
                } catch (Exception e) {
                    result.put(unit, 0);
                }
            }
        }
        return result;
    }

    /**
     * @return the defender
     */
    public Tribe getDefender() {
        return defender;
    }

    /**
     * @param defender the defender to set
     */
    public void setDefender(Tribe defender) {
        if (defender == null) {
            this.defender = Barbarians.getSingleton();
        } else {
            this.defender = defender;
        }
    }

    /**
     * @return the targetVillage
     */
    public Village getTargetVillage() {
        return targetVillage;
    }

    /**
     * @param targetVillage the targetVillage to set
     */
    public void setTargetVillage(Village targetVillage) {
        this.targetVillage = targetVillage;
    }

    public void setSpyedResources(int pWood, int pClay, int pIron) {
        spyedResources = new int[]{pWood, pClay, pIron};
    }

    public int[] getSpyedResources() {
        return spyedResources;
    }

    public void setHaul(int pWood, int pClay, int pIron) {
        haul = new int[]{pWood, pClay, pIron};
    }

    public int[] getHaul() {
        return haul;
    }

    public void setWoodLevel(int woodLevel) {
        this.woodLevel = woodLevel;
    }

    public int getWoodLevel() {
        return woodLevel;
    }

    public void setClayLevel(int clayLevel) {
        this.clayLevel = clayLevel;
    }

    public int getClayLevel() {
        return clayLevel;
    }

    public void setIronLevel(int ironLevel) {
        this.ironLevel = ironLevel;
    }

    public int getIronLevel() {
        return ironLevel;
    }

    public void setStorageLevel(int storageLevel) {
        this.storageLevel = storageLevel;
    }

    public int getStorageLevel() {
        return storageLevel;
    }

    public void setHideLevel(int hideLevel) {
        this.hideLevel = hideLevel;
    }

    public int getHideLevel() {
        return hideLevel;
    }

    public void setWallLevel(int wallLevel) {
        this.wallLevel = wallLevel;
    }

    public int getWallLevel() {
        return wallLevel;
    }

    /**
     * @return the defenders
     */
    public Hashtable<UnitHolder, Integer> getDefenders() {
        return defenders;
    }

    /**
     * @param defenders the defenders to set
     */
    public void setDefenders(Hashtable<UnitHolder, Integer> defenders) {
        this.defenders = defenders;
    }

    /**
     * @return the diedDefenders
     */
    public Hashtable<UnitHolder, Integer> getDiedDefenders() {
        return diedDefenders;
    }

    public Hashtable<UnitHolder, Integer> getSurvivingDefenders() {
        Hashtable<UnitHolder, Integer> result = null;
        if (!wasLostEverything() && defenders != null && diedDefenders != null) {
            result = new Hashtable<>();
            Hashtable<UnitHolder, Integer> def = defenders;
            Hashtable<UnitHolder, Integer> diedDef = diedDefenders;
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                try {
                    int survivors = def.get(unit) - diedDef.get(unit);
                    survivors = (survivors >= 0) ? survivors : 0;
                    result.put(unit, survivors);
                } catch (Exception e) {
                    result.put(unit, 0);
                }
            }
        }
        return result;
    }

    public boolean hasSurvivedDefenders() {
        return (TroopHelper.getPopulation(getSurvivingDefenders()) != 0);
    }

    /**
     * @param diedDefenders the diedDefenders to set
     */
    public void setDiedDefenders(Hashtable<UnitHolder, Integer> diedDefenders) {
        this.diedDefenders = diedDefenders;
    }

    public void addDefendersOutside(Village pVillage, Hashtable<UnitHolder, Integer> pDefenders) {
        defendersOutside.put(pVillage, pDefenders);
    }

    public boolean wasLostEverything() {
        //defenders are set to -1 if no information on them could be achieved as result of a total loss
        try {
            return (defenders.get(defenders.keys().nextElement()) < 0);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSimpleSnobAttack() {
        if (!wasSnobAttack()) {
            //acceptance reduced, must be snob
            return false;
        }
        int attackerCount = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            attackerCount += attackers.get(unit);
        }
        return (attackerCount < 1000);
    }

    //@TODO configurable guess
    public int guessType() {
        if (wasSnobAttack() || isSimpleSnobAttack()) {
            //acceptance reduced, must be snob
            return Attack.SNOB_TYPE;
        }

        if (areAttackersHidden()) {
            //attackers hidden, no info possible
            return Attack.NO_TYPE;
        }

        boolean isSnobAttack = false;
        int attackerCount = 0;
        int spyCount = 0;
        if (attackers != null) {
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                attackerCount += attackers.get(unit);
                if (unit.getPlainName().equals("snob") && attackers.get(unit) >= 1) {
                    isSnobAttack = true;
                }
                if (unit.getPlainName().equals("spy") && attackers.get(unit) >= 1) {
                    spyCount = attackers.get(unit);
                }
            }
        }
        if (isSnobAttack) {
            //snob joined attack but no acceptance was reduces
            return Attack.SNOB_TYPE;
        }

        double spyPerc = 100.0 * (double) spyCount / (double) attackerCount;

        if (spyPerc > 50.0) {
            //only spies joined the attack
            return Attack.SPY_TYPE;
        }

        if (attackerCount < 500) {
            return Attack.FAKE_TYPE;
        }

        return Attack.CLEAN_TYPE;
    }

    public boolean wasLostNothing() {
        if (areAttackersHidden()) {
            return false;
        }
        Enumeration<UnitHolder> units = diedAttackers.keys();
        while (units.hasMoreElements()) {
            if (diedAttackers.get(units.nextElement()) > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean areAttackersHidden() {
        try {
            return (attackers.get(attackers.keys().nextElement()) < 0);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean whereDefendersOnTheWay() {
        return (defendersOnTheWay != null && !defendersOnTheWay.isEmpty());
    }

    public boolean whereDefendersOutside() {
        return (defendersOutside != null && !defendersOutside.isEmpty());
    }

    /**
     * @return the defendersOutside
     */
    public Hashtable<UnitHolder, Integer> getDefendersOnTheWay() {
        return defendersOnTheWay;
    }

    /**
     * @return the defendersOutside
     */
    public Hashtable<Village, Hashtable<UnitHolder, Integer>> getDefendersOutside() {
        return defendersOutside;
    }

    /**
     * @param defendersOnTheWay the defendersOnTheWay to set
     */
    public void setDefendersOnTheWay(Hashtable<UnitHolder, Integer> defendersOnTheWay) {
        this.defendersOnTheWay = defendersOnTheWay;
    }

    /**
     * @return the conquered
     */
    public boolean isConquered() {
        return conquered;
    }

    /**
     * @param conquered the conquered to set
     */
    public void setConquered(boolean conquered) {
        this.conquered = conquered;
    }

    /**
     * @return the wallBefore
     */
    public int getWallBefore() {
        return wallBefore;
    }

    /**
     * @param wallBefore the wallBefore to set
     */
    public void setWallBefore(byte wallBefore) {
        this.wallBefore = wallBefore;
    }

    /**
     * @return the wallAfter
     */
    public int getWallAfter() {
        return wallAfter;
    }

    /**
     * @param wallAfter the wallAfter to set
     */
    public void setWallAfter(byte wallAfter) {
        this.wallAfter = wallAfter;
    }

    /**
     * @return the aimedBuilding
     */
    public String getAimedBuilding() {
        return aimedBuilding;
    }

    /**
     * @param aimedBuilding the aimedBuilding to set
     */
    public void setAimedBuilding(String aimedBuilding) {
        this.aimedBuilding = aimedBuilding;
    }

    /**
     * @return the buildingBefore
     */
    public int getBuildingBefore() {
        return buildingBefore;
    }

    /**
     * @param buildingBefore the buildingBefore to set
     */
    public void setBuildingBefore(byte buildingBefore) {
        this.buildingBefore = buildingBefore;
    }

    /**
     * @return the buildingAfter
     */
    public int getBuildingAfter() {
        return buildingAfter;
    }

    /**
     * @param buildingAfter the buildingAfter to set
     */
    public void setBuildingAfter(byte buildingAfter) {
        this.buildingAfter = buildingAfter;
    }

    /**
     * @return the acceptanceBefore
     */
    public int getAcceptanceBefore() {
        return acceptanceBefore;
    }

    /**
     * @param acceptanceBefore the acceptanceBefore to set
     */
    public void setAcceptanceBefore(byte acceptanceBefore) {
        this.acceptanceBefore = acceptanceBefore;
    }

    /**
     * @return the acceptanceAfter
     */
    public int getAcceptanceAfter() {
        return acceptanceAfter;
    }

    /**
     * @param acceptanceAfter the acceptanceAfter to set
     */
    public void setAcceptanceAfter(byte acceptanceAfter) {
        this.acceptanceAfter = acceptanceAfter;
    }

    public boolean wasWallDamaged() {
        return (getWallBefore() > 0);
    }

    public boolean wasBuildingDamaged() {
        return (getBuildingBefore() > 0);
    }

    public boolean isSpyReport() {
        if (wasLostEverything()) {
            return false;
        }
        Enumeration<UnitHolder> units = attackers.keys();
        boolean spySurvived = false;
        while (units.hasMoreElements()) {
            UnitHolder unit = units.nextElement();
            if (unit.getPlainName().equals("spy")) {
                if (attackers.get(unit) - diedAttackers.get(unit) > 0) {
                    spySurvived = true;
                }
            } else {
                if (attackers.get(unit) - diedAttackers.get(unit) > 0) {
                    //something else survived too
                    return false;
                }
            }
        }
        return spySurvived;
    }

    public int getDestroyedWallLevels() {
        if (wasWallDamaged()) {
            return getWallBefore() - getWallAfter();
        }
        return 0;
    }

    public int getDestroyedBuildingLevels() {
        if (wasBuildingDamaged()) {
            return getBuildingBefore() - getBuildingAfter();
        }
        return 0;
    }

    public boolean wasSnobAttack() {
        return getAcceptanceAfter() < getAcceptanceBefore();
    }

    public boolean wasConquered() {
        return (getAcceptanceAfter() <= 0);
    }

    /**
     * @return the won
     */
    public boolean isWon() {
        return won;
    }

    /**
     * @param won the won to set
     */
    public void setWon(boolean won) {
        this.won = won;
    }

    /**
     * @return the luck
     */
    public double getLuck() {
        return luck;
    }

    /**
     * @param luck the luck to set
     */
    public void setLuck(double luck) {
        this.luck = luck;
    }

    /**
     * @return the moral
     */
    public double getMoral() {
        return moral;
    }

    /**
     * @param moral the moral to set
     */
    public void setMoral(double moral) {
        this.moral = moral;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isValid() {

//        System.out.println(getAttacker());
//        System.out.println(getSourceVillage());
//        System.out.println(getDefender());
//        System.out.println(getTargetVillage());
//        System.out.println(getAttackers());
//        System.out.println(getDiedAttackers());
//        System.out.println(getDefenders());
//        System.out.println(getDiedDefenders());

        return (attacker != null
                && sourceVillage != null
                && !attackers.isEmpty()
                && !diedAttackers.isEmpty()
                && defender != null
                && targetVillage != null
                && !defenders.isEmpty()
                && !diedDefenders.isEmpty());
    }

    public byte getVillageEffects() {
        byte effect = 0;
        if (wasWallDamaged()) {
            effect += 1;
        }
        if (wasBuildingDamaged()) {
            effect += 2;
        }
        if (wasConquered()) {
            effect += 4;
        }
        return effect;
    }

    public Integer getComparableValue() {
        if (areAttackersHidden()) {
            //grey report
            return 4;
        } else if (isSpyReport()) {
            //blue report
            return 2;
        } else if (wasLostEverything()) {
            //red report
            return 3;
        } else if (wasLostNothing()) {
            //green report
            return 0;
        } else {
            //yellow report
            return 1;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm");
        result.append("Gesendet: ").append(f.format(new Date(timestamp))).append("\n");
        result.append(won ? "Gewonnen\n" : "Verloren\n");

        if (isSpyReport()) {
            result.append("Farbe: Blau\n");
        } else if (wasLostEverything()) {
            result.append("Farbe: Rot\n");
        } else if (wasLostNothing()) {
            result.append("Farbe: Grün\n");
        } else {
            result.append("Farbe: Gelb\n");
        }
        result.append("Moral: ").append(moral).append("\n");
        result.append("Glück: ").append(luck).append("\n");
        result.append("Angreifer: ").append(attacker).append("\n");
        result.append("Herkunft: ").append(sourceVillage).append("\n");
        String sAttackers = "";
        String sAttackersDied = "";
        String sDefenders = "";
        String sDefendersDied = "";

        if (areAttackersHidden()) {
            sAttackers = "Verborgen\n";
            sAttackersDied = "Verborgen\n";
        } else {
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                sAttackers += attackers.get(unit) + " ";
                sAttackersDied += diedAttackers.get(unit) + " ";
            }
            sAttackers = sAttackers.trim() + "\n";
            sAttackersDied = sAttackersDied.trim() + "\n";
        }

        if (wasLostEverything()) {
            sDefenders = "Keine Informationen\n";
            sDefendersDied = "Keine Informationen\n";
        } else {
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                sDefenders += defenders.get(unit) + " ";
                sDefendersDied += diedDefenders.get(unit) + " ";
            }
            sDefenders = sDefenders.trim() + "\n";
            sDefendersDied = sDefendersDied.trim() + "\n";
        }

        result.append("Anzahl: ").append(sAttackers);
        result.append("Verluste: ").append(sAttackersDied);
        result.append("Verteidiger: ").append(defender).append("\n");
        result.append("Ziel: ").append(targetVillage).append("\n");
        result.append("Anzahl: ").append(sDefenders);
        result.append("Verluste: ").append(sDefendersDied);

        if (wasConquered()) {
            if (whereDefendersOutside()) {
                Enumeration<Village> villageKeys = defendersOutside.keys();
                while (villageKeys.hasMoreElements()) {
                    Village v = villageKeys.nextElement();
                    if (v != null) {
                        Hashtable<UnitHolder, Integer> troops = defendersOutside.get(v);
                        if (troops != null) {
                            result.append(" -> ").append(v).append(" ");
                            for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                                result.append(troops.get(u)).append(" ");
                            }
                        }
                        result.append("\n");
                    }
                }
            }
        }


        if (wasWallDamaged()) {
            result.append("Wall zerstört von Stufe ").append(getWallBefore()).append(" auf ").append(getWallAfter()).append("\n");
        }
        if (wasBuildingDamaged()) {
            result.append(aimedBuilding).append(" zerstört von Stufe ").append(getBuildingBefore()).append(" auf ").append(getBuildingAfter()).append("\n");
        }
        if (wasSnobAttack()) {
            result.append("Zustimmung gesenkt von ").append(getAcceptanceBefore()).append(" auf ").append(getAcceptanceAfter()).append("\n");
        }
        return result.toString();
    }

    @Override
    public int compareTo(FightReport o) {
        return getComparableValue().compareTo(o.getComparableValue());
    }

    @Override
    public String getElementIdentifier() {
        return "fightReport";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "reportSet";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "name";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FightReport)) {
            return false;
        }

        FightReport theOther = (FightReport) obj;
        return hashCode() == theOther.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.won ? 1 : 0);
        hash = 53 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.luck) ^ (Double.doubleToLongBits(this.luck) >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.moral) ^ (Double.doubleToLongBits(this.moral) >>> 32));
        hash = 53 * hash + (this.attacker != null ? this.attacker.hashCode() : 0);
        hash = 53 * hash + (this.sourceVillage != null ? this.sourceVillage.hashCode() : 0);
        hash = 53 * hash + (this.attackers != null ? this.attackers.hashCode() : 0);
        hash = 53 * hash + (this.diedAttackers != null ? this.diedAttackers.hashCode() : 0);
        hash = 53 * hash + (this.defender != null ? this.defender.hashCode() : 0);
        hash = 53 * hash + (this.targetVillage != null ? this.targetVillage.hashCode() : 0);
        hash = 53 * hash + (this.defenders != null ? this.defenders.hashCode() : 0);
        hash = 53 * hash + (this.diedDefenders != null ? this.diedDefenders.hashCode() : 0);
        hash = 53 * hash + (this.defendersOutside != null ? this.defendersOutside.hashCode() : 0);
        hash = 53 * hash + (this.defendersOnTheWay != null ? this.defendersOnTheWay.hashCode() : 0);
        hash = 53 * hash + (this.conquered ? 1 : 0);
        hash = 53 * hash + this.wallBefore;
        hash = 53 * hash + this.wallAfter;
        hash = 53 * hash + (this.aimedBuilding != null ? this.aimedBuilding.hashCode() : 0);
        hash = 53 * hash + this.buildingBefore;
        hash = 53 * hash + this.buildingAfter;
        hash = 53 * hash + this.acceptanceBefore;
        hash = 53 * hash + this.acceptanceAfter;
        hash = 53 * hash + Arrays.hashCode(this.spyedResources);
        hash = 53 * hash + Arrays.hashCode(this.haul);
        hash = 53 * hash + this.woodLevel;
        hash = 53 * hash + this.clayLevel;
        hash = 53 * hash + this.ironLevel;
        hash = 53 * hash + this.storageLevel;
        hash = 53 * hash + this.hideLevel;
        return hash;
    }

    public int cleanupHashCode() {
        int hash = 5;
        hash = 53 * hash + (this.won ? 1 : 0);
        hash = 53 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.luck) ^ (Double.doubleToLongBits(this.luck) >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.moral) ^ (Double.doubleToLongBits(this.moral) >>> 32));
        hash = 53 * hash + (this.attacker != null ? this.attacker.hashCode() : 0);
        hash = 53 * hash + (this.sourceVillage != null ? this.sourceVillage.hashCode() : 0);
        hash = 53 * hash + (this.attackers != null ? this.attackers.hashCode() : 0);
        hash = 53 * hash + (this.diedAttackers != null ? this.diedAttackers.hashCode() : 0);
        hash = 53 * hash + (this.defender != null ? this.defender.hashCode() : 0);
        hash = 53 * hash + (this.targetVillage != null ? this.targetVillage.hashCode() : 0);
        hash = 53 * hash + (this.defenders != null ? this.defenders.hashCode() : 0);
        hash = 53 * hash + (this.diedDefenders != null ? this.diedDefenders.hashCode() : 0);
        hash = 53 * hash + (this.defendersOutside != null ? this.defendersOutside.hashCode() : 0);
        hash = 53 * hash + (this.defendersOnTheWay != null ? this.defendersOnTheWay.hashCode() : 0);
        hash = 53 * hash + (this.conquered ? 1 : 0);
        hash = 53 * hash + this.wallBefore;
        hash = 53 * hash + this.wallAfter;
        hash = 53 * hash + (this.aimedBuilding != null ? this.aimedBuilding.hashCode() : 0);
        hash = 53 * hash + this.buildingBefore;
        hash = 53 * hash + this.buildingAfter;
        hash = 53 * hash + this.acceptanceBefore;
        hash = 53 * hash + this.acceptanceAfter;
        hash = 53 * hash + Arrays.hashCode(this.spyedResources);
        hash = 53 * hash + Arrays.hashCode(this.haul);
        hash = 53 * hash + this.woodLevel;
        hash = 53 * hash + this.clayLevel;
        hash = 53 * hash + this.ironLevel;
        hash = 53 * hash + this.storageLevel;
        hash = 53 * hash + this.hideLevel;
        return hash;
    }
}