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
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.php.LuckViewInterface;
import de.tor.tribes.php.UnitTableInterface;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.InvalidTribe;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.village.KnownVillage;
import de.tor.tribes.util.xml.JaxenUtils;
import org.jdom.Document;
import org.jdom.Element;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

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
            Set<Village> targetKeys = defendersOutside.keySet();
            for (Village target: targetKeys) {
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
                ? "[b]Schaden durch Katapultbeschuss:[/b] " + Constants.buildingNames[aimedBuildingId] + " beschädigt von Level " + getBuildingBefore() + " auf Level " + getBuildingAfter()
                : "";
        return new String[]{attackerVal, sourceVal, defenderVal, targetVal, sendDateVal, resultVal, luckVal, moraleVal, attackerTroopsVal, defenderTroopsVal, troopsOutsideVal, troopsEnRouteVal, loyalityChangeVal, wallChangeVal, cataChangeVal};
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }
    
    private Logger logger = Logger.getLogger("FightReport");
    
    private boolean won = false;
    private long timestamp = 0;
    private double luck = 0.0;
    private double moral = 100.0;
    private Tribe attacker = null;
    private Village sourceVillage = null;
    private TroopAmountFixed attackers = null;
    private TroopAmountFixed diedAttackers = null;
    private Tribe defender = null;
    private Village targetVillage = null;
    private TroopAmountFixed defenders = null;
    private TroopAmountFixed diedDefenders = null;
    private HashMap<Village, TroopAmountFixed> defendersOutside = null;
    private TroopAmountFixed defendersOnTheWay = null;
    private boolean conquered = false;
    private int wallBefore = -1;
    private int wallAfter = -1;
    private int aimedBuildingId = -1;
    private int buildingBefore = -1;
    private int buildingAfter = -1;
    private int acceptanceBefore = 100;
    private int acceptanceAfter = 100;
    private int[] spyedResources = null;
    private int[] haul = null;
    private int[] buildingLevels;
    
    public final int SPY_LEVEL_NONE = 0;
    public final int SPY_LEVEL_RESOURCES = 1;
    public final int SPY_LEVEL_BUILDINGS = 2;
    public final int SPY_LEVEL_OUTSIDE = 3;
    private int spyLevel = SPY_LEVEL_NONE;

    public FightReport() {
        attackers = new TroopAmountFixed();
        diedAttackers = new TroopAmountFixed();
        defenders = new TroopAmountFixed();
        diedDefenders = new TroopAmountFixed();
        defendersOutside = new HashMap<>();
        defendersOnTheWay = new TroopAmountFixed();
        
        buildingLevels = new int[Constants.buildingNames.length];
        Arrays.fill(buildingLevels, -1);
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

            attackers = new TroopAmountFixed(attackerElement.getChild("before"));
            diedAttackers = new TroopAmountFixed(attackerElement.getChild("died"));
            defenders = new TroopAmountFixed(defenderElement.getChild("before"));
            diedDefenders = new TroopAmountFixed(defenderElement.getChild("died"));
            try {
                defendersOnTheWay = new TroopAmountFixed(defenderElement.getChild("otw"));
            } catch (Exception ignored) {
            }

            Element dDefendersOutside = null;
            try {
                dDefendersOutside = defenderElement.getChild("outside");
            } catch (Exception ignored) {
            }

            defendersOutside = new HashMap<>();
            if (dDefendersOutside != null) {
                for (Element e : (List<Element>) JaxenUtils.getNodes(dDefendersOutside, "support")) {
                    int villageId = e.getAttribute("trg").getIntValue();
                    Village v = DataHolder.getSingleton().getVillagesById().get(villageId);
                    if(v != null) {
                        TroopAmountFixed unitsInvillage = defendersOutside.get(v);
                        if (unitsInvillage == null) {
                            unitsInvillage = new TroopAmountFixed(e);
                        } else {
                            unitsInvillage.addAmount(new TroopAmountFixed(e));
                        }
                        defendersOutside.put(v, unitsInvillage);
                    }
                }
            }

            try {
                Element e = pElement.getChild("wall");
                this.wallBefore = Byte.parseByte(e.getAttribute("before").getValue());
                this.wallAfter = Byte.parseByte(e.getAttribute("after").getValue());
            } catch (Exception e) {
                this.wallBefore = -1;
                this.wallAfter = -1;
            }
            try {
                Element e = pElement.getChild("building");
                this.aimedBuildingId =  Byte.parseByte(e.getAttribute("target").getValue());
                this.buildingBefore = Byte.parseByte(e.getAttribute("before").getValue());
                this.buildingAfter = Byte.parseByte(e.getAttribute("after").getValue());
            } catch (Exception e) {
                this.buildingBefore = -1;
                this.buildingAfter = -1;
                logger.debug("cannot read building damage", e);
            }
            try {
                Element e = pElement.getChild("acceptance");
                this.acceptanceBefore = Byte.parseByte(e.getAttribute("before").getValue());
                this.acceptanceAfter = Byte.parseByte(e.getAttribute("after").getValue());
            } catch (Exception e) {
                this.acceptanceBefore = 100;
                this.acceptanceAfter = 100;
                logger.debug("cannot read acceptance", e);
            }
            try {
                Element e = pElement.getChild("spyBuildings");
                
                for(int i = 0; i < buildingLevels.length; i++) {
                    buildingLevels[i] = Integer.parseInt(e.getChildText(
                            Constants.buildingNames[i]));
                }
            } catch (Exception e) {
                logger.debug("Failed to read buildings", e);
            }
            try {
                spyLevel = Integer.parseInt(pElement.getChildText("spyLevel"));
            } catch (Exception e) {
                logger.debug("Failed to read spy Level", e);
            }
        } catch (Exception e) {
            logger.warn("failed to fully read the report", e);
        }
    }

    @Override
    public String toXml() {
        StringBuilder b = new StringBuilder();
        try {
            b.append("<report>\n");
            //general part
            b.append("<timestamp>").append(timestamp).append("</timestamp>\n");
            b.append("<moral>").append(moral).append("</moral>\n");
            b.append("<luck>").append(luck).append("</luck>\n");
            //attacker part
            b.append("<attacker>\n");
            b.append("<id>").append(attacker.getId()).append("</id>\n");
            b.append("<src>").append(sourceVillage.getId()).append("</src>\n");
            b.append("<before ");
            b.append(attackers.toXml());
            b.append(" />\n");
            b.append("<died ");
            b.append(diedAttackers.toXml());
            b.append(" />\n");
            b.append("</attacker>\n");

            //defender part
            b.append("<defender>\n");
            b.append("<id>").append(defender.getId()).append("</id>\n");
            b.append("<trg>").append(targetVillage.getId()).append("</trg>\n");
            b.append("<before ");
            b.append(defenders.toXml());
            b.append(" />\n");
            b.append("<died ");
            b.append(diedDefenders.toXml());
            b.append(" />\n");
            if (whereDefendersOnTheWay()) {
                b.append("<otw ");
                b.append(defendersOnTheWay.toXml());
                b.append(" />\n");
            }
            if (whereDefendersOutside()) {
                b.append("<outside>\n");
                Set<Village> targetVillages = defendersOutside.keySet();
                for (Village target: targetVillages) {
                    b.append("<target id=\"").append(target.getId()).append("\" ");
                    b.append(defendersOutside.get(target).toXml());
                    b.append(" />\n");
                }
                b.append("</outside>\n");
            }

            b.append("</defender>\n");

            if (wasWallDamaged()) {
                b.append("<wall before=\"").append(getWallBefore()).append("\" after=\"").append(getWallAfter()).append("\"/>\n");
            }
            if (wasBuildingDamaged()) {
                b.append("<building target=\"").append(aimedBuildingId).append("\" before=\"").append(getBuildingBefore()).append("\" after=\"").append(getBuildingAfter()).append("\"/>\n");
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

            b.append("<spyBuildings");
            for(int i = 0; i < buildingLevels.length; i++) {
                b.append(" ").append(Constants.buildingNames[i]).append("=\"");
                b.append(buildingLevels[i]).append("\"");
            }
            b.append("/>\n");
            
            b.append("<spyLevel>").append(spyLevel).append("</spyLevel>\n");
            
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
    public TroopAmountFixed getAttackers() {
        return attackers;
    }

    /**
     * @param attackers the attackers to set
     */
    public void setAttackers(TroopAmountFixed attackers) {
        this.attackers = attackers;
    }

    /**
     * @return the diedAttackers
     */
    public TroopAmountFixed getDiedAttackers() {
        return diedAttackers;
    }

    /**
     * @param diedAttackers the diedAttackers to set
     */
    public void setDiedAttackers(TroopAmountFixed diedAttackers) {
        this.diedAttackers = diedAttackers;
    }

    public TroopAmountFixed getSurvivingAttackers() {
        TroopAmountFixed result = null;
        if (!areAttackersHidden() && attackers != null && diedAttackers != null) {
            result = (TroopAmountFixed) attackers.clone();
            result.removeAmount(diedAttackers);
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

    /**
     * @return the defenders
     */
    public TroopAmountFixed getDefenders() {
        return defenders;
    }

    /**
     * @param defenders the defenders to set
     */
    public void setDefenders(TroopAmountFixed defenders) {
        this.defenders = defenders;
    }

    /**
     * @return the diedDefenders
     */
    public TroopAmountFixed getDiedDefenders() {
        return diedDefenders;
    }

    public TroopAmountFixed getSurvivingDefenders() {
        TroopAmountFixed result = null;
        if (!wasLostEverything() && defenders != null && diedDefenders != null) {
            result = (TroopAmountFixed) defenders.clone();
            result.removeAmount(diedDefenders);
        }
        return result;
    }

    public boolean hasSurvivedDefenders() {
        return (getSurvivingDefenders().getTroopPopCount() != 0);
    }

    /**
     * @param diedDefenders the diedDefenders to set
     */
    public void setDiedDefenders(TroopAmountFixed diedDefenders) {
        this.diedDefenders = diedDefenders;
    }

    public void addDefendersOutside(Village pVillage, TroopAmountFixed pDefenders) {
        defendersOutside.put(pVillage, pDefenders);
    }

    public boolean wasLostEverything() {
        //defenders are set to -1 if no information on them could be achieved as result of a total loss
        return defenders.containsInformation();
    }

    public boolean isSimpleSnobAttack() {
        if (!wasSnobAttack()) {
            //acceptance reduced, must be snob
            return false;
        }
        return (attackers.getTroopSum() < 1000);
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
            attackerCount = attackers.getTroopSum();
            if (attackers.getAmountForUnit("snob") >= 1) {
                isSnobAttack = true;
            }
            if (attackers.getAmountForUnit("spy") >= 1) {
                spyCount = attackers.getAmountForUnit("spy");
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
        return diedAttackers.getTroopSum() == 0;
    }

    public boolean areAttackersHidden() {
        return attackers.containsInformation();
    }

    public boolean whereDefendersOnTheWay() {
        return (defendersOnTheWay != null && defendersOnTheWay.getTroopSum() != 0);
    }

    public boolean whereDefendersOutside() {
        return (defendersOutside != null && !defendersOutside.isEmpty());
    }

    /**
     * @return the defendersOutside
     */
    public TroopAmountFixed getDefendersOnTheWay() {
        return defendersOnTheWay;
    }

    /**
     * @return the defendersOutside
     */
    public HashMap<Village, TroopAmountFixed> getDefendersOutside() {
        return defendersOutside;
    }

    /**
     * @param defendersOnTheWay the defendersOnTheWay to set
     */
    public void setDefendersOnTheWay(TroopAmountFixed defendersOnTheWay) {
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
    public void setWallBefore(int wallBefore) {
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
    public void setWallAfter(int wallAfter) {
        this.wallAfter = wallAfter;
    }

    /**
     * @return the aimedBuilding
     */
    public int getAimedBuildingId() {
        return aimedBuildingId;
    }

    /**
     * @param aimedBuilding the aimedBuilding to set
     */
    public void setAimedBuildingId(int pAimedBuildingId) {
        this.aimedBuildingId = pAimedBuildingId;
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
    public void setBuildingBefore(int buildingBefore) {
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
    public void setBuildingAfter(int buildingAfter) {
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
    public void setAcceptanceBefore(int acceptanceBefore) {
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
    public void setAcceptanceAfter(int acceptanceAfter) {
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
        boolean spySurvived = false;
        TroopAmountFixed survivingAtt = getSurvivingAttackers();
        for (UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            if (unit.getPlainName().equals("spy")) {
                if (survivingAtt.getAmountForUnit(unit) > 0) {
                    spySurvived = true;
                }
            } else {
                if (survivingAtt.getAmountForUnit(unit) > 0) {
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
        return (attacker != null
                && sourceVillage != null
                && attackers != null
                && diedAttackers != null
                && defender != null
                && targetVillage != null
                && defenders != null
                && diedDefenders != null);
    }

    public int getVillageEffects() {
        int effect = 0;
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
                sAttackers += attackers.getAmountForUnit(unit) + " ";
                sAttackersDied += diedAttackers.getAmountForUnit(unit) + " ";
            }
            sAttackers = sAttackers.trim() + "\n";
            sAttackersDied = sAttackersDied.trim() + "\n";
        }

        if (wasLostEverything()) {
            sDefenders = "Keine Informationen\n";
            sDefendersDied = "Keine Informationen\n";
        } else {
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                sDefenders += defenders.getAmountForUnit(unit) + " ";
                sDefendersDied += diedDefenders.getAmountForUnit(unit) + " ";
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
                Set<Village> villageKeys = defendersOutside.keySet();
                for (Village v: villageKeys) {
                    if (v != null) {
                        TroopAmountFixed troops = defendersOutside.get(v);
                        if (troops != null) {
                            result.append(" -> ").append(v).append(" ");
                            for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                                result.append(troops.getAmountForUnit(u)).append(" ");
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
            result.append(Constants.buildingNames[aimedBuildingId]).append(" zerstört von Stufe ").append(getBuildingBefore()).append(" auf ").append(getBuildingAfter()).append("\n");
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
        hash = 53 * hash + this.aimedBuildingId;
        hash = 53 * hash + this.buildingBefore;
        hash = 53 * hash + this.buildingAfter;
        hash = 53 * hash + this.acceptanceBefore;
        hash = 53 * hash + this.acceptanceAfter;
        hash = 53 * hash + Arrays.hashCode(this.spyedResources);
        hash = 53 * hash + Arrays.hashCode(this.haul);
        for(int i = 0; i < this.buildingLevels.length; i++)
            hash = 53 * hash + this.buildingLevels[i];
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
        hash = 53 * hash + this.aimedBuildingId;
        hash = 53 * hash + this.buildingBefore;
        hash = 53 * hash + this.buildingAfter;
        hash = 53 * hash + this.acceptanceBefore;
        hash = 53 * hash + this.acceptanceAfter;
        hash = 53 * hash + Arrays.hashCode(this.spyedResources);
        hash = 53 * hash + Arrays.hashCode(this.haul);
        for(int i = 0; i < this.buildingLevels.length; i++)
            hash = 53 * hash + this.buildingLevels[i];
        return hash;
    }
    
    /*
        This method fills buildings that had not been spyed with zero,
        because buildings with level 0 are not shown by DS
    */
    public void fillMissingSpyInformation() {
        logger.debug(toXml());
        if (spyedResources != null) {
            if(spyedResources[0] != 0) spyLevel = SPY_LEVEL_RESOURCES;
            if(spyedResources[1] != 0) spyLevel = SPY_LEVEL_RESOURCES;
            if(spyedResources[2] != 0) spyLevel = SPY_LEVEL_RESOURCES;
        }
        
        for(int i = 0; i < buildingLevels.length; i++) {
            if(buildingLevels[i] != -1)
                spyLevel = SPY_LEVEL_BUILDINGS;
        }
        
        if(whereDefendersOnTheWay() && spyLevel == SPY_LEVEL_BUILDINGS) {
            //Some Buildings e.g. main cannot be zero
            //outside Troops can only be spyed if buildings were spyed too
            spyLevel = SPY_LEVEL_OUTSIDE;
        }

        //set wall destruction (works also without spying)
        if (wallAfter != -1 && spyLevel < SPY_LEVEL_BUILDINGS) {
            buildingLevels[KnownVillage.getBuildingIdByName("wall")] = wallAfter;
        }

        switch (spyLevel) {
            case SPY_LEVEL_OUTSIDE:
            case SPY_LEVEL_BUILDINGS:
                for(int i = 0; i < this.buildingLevels.length; i++)
                    if(this.buildingLevels[i] == -1) this.buildingLevels[i] = 0;
            case SPY_LEVEL_RESOURCES:
                if(spyedResources == null)
                    spyedResources = new int[]{0, 0, 0};
            default:
        }
        logger.debug(toXml());
    }

    public void setDefendersOutside(HashMap<Village, TroopAmountFixed> pDefendersOutside) {
        this.defendersOutside = pDefendersOutside;
    }

    public void setBuilding(int pBuildingId, int pLevel) {
        buildingLevels[pBuildingId] = pLevel;
    }
    
    public int getBuilding(int pBuilding) {
        return buildingLevels[pBuilding];
    }

    public int getSpyLevel() {
        return spyLevel;
    }
}
