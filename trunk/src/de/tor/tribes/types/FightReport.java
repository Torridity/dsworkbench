/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.xml.JaxenUtils;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

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
    private final static String TEMPLATE_PROPERTY = "report.bbexport.template";

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String attackerVal = getAttacker().toBBCode();
        String targetVal = getTargetVillage().toBBCode();
        SimpleDateFormat d = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String sendDateVal = d.format(new Date(getTimestamp()));
        String resultVal = "";
        if (isWon()) {
            resultVal = "Der Angreifer hat gewonnen";
        } else {
            resultVal = "Der Verteidiger hat gewonnen";
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMinimumFractionDigits(1);

        String luckVal = "[img]http://dsextra.net/ic/luck_" + nf.format(getLuck()) + "[/img] " + nf.format(getLuck()) + "%";
        nf.setMinimumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        String moraleVal = nf.format(getMoral()) + " %";

        String sourceVal = getSourceVillage().toBBCode();
        String attackerTroopsVal = "";
        if (!areAttackersHidden()) {

            attackerTroopsVal = "http://dsextra.net/ic/knights_";
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = getAttackers().get(unit);
                attackerTroopsVal += amount + "_";
            }
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = getDiedAttackers().get(unit);
                attackerTroopsVal += amount + "_";
            }
            attackerTroopsVal = attackerTroopsVal.substring(0, attackerTroopsVal.lastIndexOf("_"));
            attackerTroopsVal = "[img]" + attackerTroopsVal + "[/img]";
        } else {
            attackerTroopsVal = "Durch Besitzer des Berichts verborgen";
        }


        String defenderVal = getDefender().toBBCode();
        String defenderTroopsVal = "";
        if (!wasLostEverything()) {
            defenderTroopsVal = "http://dsextra.net/ic/knights_";
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = getDefenders().get(unit);
                defenderTroopsVal += amount + "_";
            }
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = getDiedDefenders().get(unit);
                defenderTroopsVal += amount + "_";
            }
            defenderTroopsVal = defenderTroopsVal.substring(0, defenderTroopsVal.lastIndexOf("_"));
            defenderTroopsVal = "[img]" + defenderTroopsVal + "[/img]";
        } else {
            defenderTroopsVal = "Keiner deiner Kämpfer ist lebend zurückgekehrt.\nEs konnten keine Informationen über die Truppenstärke des Gegners erlangt werden.";
        }

        boolean wasAdditionTroops = false;
        String troopsEnRouteVal = "";
        if (whereDefendersOnTheWay()) {
            wasAdditionTroops = true;
            troopsEnRouteVal = "http://dsextra.net/ic/knights_";
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = getDefendersOnTheWay().get(unit);
                troopsEnRouteVal += amount + "_";
            }
            troopsEnRouteVal = troopsEnRouteVal.substring(0, troopsEnRouteVal.lastIndexOf("_"));
            troopsEnRouteVal = "[b]Truppen des Verteidigers, die unterwegs waren[/b]\n\n" + "[img]" + troopsEnRouteVal + "[/img]";
        }
        String troopsOutsideVal = "";
        if (whereDefendersOutside()) {
            wasAdditionTroops = true;
            Enumeration<Village> targetKeys = getDefendersOutside().keys();
            while (targetKeys.hasMoreElements()) {
                Village target = targetKeys.nextElement();
                troopsOutsideVal += target.toBBCode() + "\n\n";
                String graphUrl = "http://dsextra.net/ic/knights_";
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    Integer amount = getDefendersOutside().get(target).get(unit);
                    graphUrl += amount + "_";
                }
                graphUrl = graphUrl.substring(0, graphUrl.lastIndexOf("_"));
                troopsOutsideVal += "[img]" + graphUrl + "[/img]\n\n";
            }
        }

        String loyalityChangeVal = "";
        if (wasSnobAttack()) {
            loyalityChangeVal = "[b]Veränderung der Zustimmung:[/b] Zustimmung gesunken von " + nf.format(getAcceptanceBefore()) + " auf " + getAcceptanceAfter();
        }

        String wallChangeVal = "";
        if (wasWallDamaged()) {
            wallChangeVal = "[b]Schaden durch Rammen:[/b] Wall beschädigt von Level " + getWallBefore() + " auf Level " + getWallAfter();
        }
        String cataChangeVal = "";
        if (wasBuildingDamaged()) {
            cataChangeVal = "[b]Schaden durch Katapultbeschuss:[/b] " + getAimedBuilding() + " beschädigt von Level " + getBuildingBefore() + " auf Level " + getBuildingAfter();
        }
        return new String[]{attackerVal, sourceVal, defenderVal, targetVal, sendDateVal, resultVal, luckVal, moraleVal, attackerTroopsVal, defenderTroopsVal, troopsOutsideVal, troopsEnRouteVal, loyalityChangeVal, wallChangeVal, cataChangeVal};
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    @Override
    public String getTemplateProperty() {
        return TEMPLATE_PROPERTY;
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

    public FightReport() {
        attackers = new Hashtable<UnitHolder, Integer>();
        diedAttackers = new Hashtable<UnitHolder, Integer>();
        defenders = new Hashtable<UnitHolder, Integer>();
        diedDefenders = new Hashtable<UnitHolder, Integer>();
        defendersOutside = new Hashtable<Village, Hashtable<UnitHolder, Integer>>();
        defendersOnTheWay = new Hashtable<UnitHolder, Integer>();
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
            setTimestamp(Long.parseLong(pElement.getChild("timestamp").getText()));
            setMoral(Double.parseDouble(pElement.getChild("moral").getText()));
            setLuck(Double.parseDouble(pElement.getChild("luck").getText()));
            //attacker stuff
            Element attackerElement = pElement.getChild("attacker");
            Element defenderElement = pElement.getChild("defender");
            int source = Integer.parseInt(attackerElement.getChild("src").getText());
            setSourceVillage(DataHolder.getSingleton().getVillagesById().get(source));

            int attackerId = Integer.parseInt(attackerElement.getChild("id").getText());
            Tribe attElement = DataHolder.getSingleton().getTribes().get(attackerId);

            if (attElement != null) {
                setAttacker(attElement);
            } else {
                if (getSourceVillage() != null && getSourceVillage().getTribe() != null) {
                    setAttacker(getSourceVillage().getTribe());
                } else {
                    setAttacker(Barbarians.getSingleton());
                }
            }

            int target = Integer.parseInt(defenderElement.getChild("trg").getText());
            setTargetVillage(DataHolder.getSingleton().getVillagesById().get(target));

            int defenderId = Integer.parseInt(defenderElement.getChild("id").getText());
            Tribe defElement = DataHolder.getSingleton().getTribes().get(defenderId);
            if (defElement != null) {
                setDefender(defElement);
            } else {
                if (getTargetVillage() != null && getTargetVillage().getTribe() != null) {
                    setDefender(getTargetVillage().getTribe());
                } else {
                    setDefender(Barbarians.getSingleton());
                }
            }

            Element aAmount = attackerElement.getChild("amount");
            Element aDiedAmount = attackerElement.getChild("died");
            Element dAmount = defenderElement.getChild("amount");
            Element dDiedAmount = defenderElement.getChild("died");
            Element dDefendersOnTheWay = null;

            try {
                dDefendersOnTheWay = defenderElement.getChild("ontheway");
            } catch (Exception e) {
            }

            Element dDefendersOutside = null;
            try {
                dDefendersOutside = defenderElement.getChild("outside");
            } catch (Exception e) {
            }

            attackers = new Hashtable<UnitHolder, Integer>();
            diedAttackers = new Hashtable<UnitHolder, Integer>();
            defenders = new Hashtable<UnitHolder, Integer>();
            diedDefenders = new Hashtable<UnitHolder, Integer>();
            defendersOnTheWay = new Hashtable<UnitHolder, Integer>();
            defendersOutside = new Hashtable<Village, Hashtable<UnitHolder, Integer>>();
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
                                unitsInvillage = new Hashtable<UnitHolder, Integer>();
                                defendersOutside.put(v, unitsInvillage);
                            }
                            unitsInvillage.put(unit, amount);
                        }
                    }
                }
            }

            try {
                Element e = pElement.getChild("wall");
                setWallBefore(Byte.parseByte(e.getAttribute("before").getValue()));
                setWallAfter(Byte.parseByte(e.getAttribute("after").getValue()));
            } catch (Exception e) {
                setWallBefore((byte) -1);
                setWallAfter((byte) -1);
            }
            try {
                Element e = pElement.getChild("building");
                setAimedBuilding(URLDecoder.decode(e.getAttribute("target").getValue(), "UTF-8"));
                setBuildingBefore(Byte.parseByte(e.getAttribute("before").getValue()));
                setBuildingAfter(Byte.parseByte(e.getAttribute("after").getValue()));
            } catch (Exception e) {
                setBuildingBefore((byte) -1);
                setBuildingAfter((byte) -1);
            }
            try {
                Element e = pElement.getChild("acceptance");
                setAcceptanceBefore(Byte.parseByte(e.getAttribute("before").getValue()));
                setAcceptanceAfter(Byte.parseByte(e.getAttribute("after").getValue()));
            } catch (Exception e) {
                setAcceptanceBefore((byte) 100);
                setAcceptanceAfter((byte) 100);
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public String toXml() {
        try {
            String xml = "<report>\n";
            //general part
            xml += "<timestamp>" + getTimestamp() + "</timestamp>\n";
            xml += "<moral>" + getMoral() + "</moral>\n";
            xml += "<luck>" + getLuck() + "</luck>\n";
            //attacker part
            xml += "<attacker>\n";
            xml += "<id>" + getAttacker().getId() + "</id>\n";
            xml += "<src>" + getSourceVillage().getId() + "</src>\n";
            String sAttackers = "<amount ";
            String sDiedAttackers = "<died ";
            String sDefenders = "<amount ";
            String sDiedDefenders = "<died ";
            String sDefendersOnTheWay = null;
            if (whereDefendersOnTheWay()) {
                sDefendersOnTheWay = "<ontheway ";
            }

            Enumeration<UnitHolder> units = attackers.keys();
            while (units.hasMoreElements()) {
                UnitHolder unit = units.nextElement();
                sAttackers += unit.getPlainName() + "=\"" + attackers.get(unit) + "\" ";
                sDiedAttackers += unit.getPlainName() + "=\"" + diedAttackers.get(unit) + "\" ";
                sDefenders += unit.getPlainName() + "=\"" + defenders.get(unit) + "\" ";
                sDiedDefenders += unit.getPlainName() + "=\"" + diedDefenders.get(unit) + "\" ";
                if (sDefendersOnTheWay != null) {
                    sDefendersOnTheWay += unit.getPlainName() + "=\"" + defendersOnTheWay.get(unit) + "\" ";
                }
            }
            xml += sAttackers + "/>\n";
            xml += sDiedAttackers + "/>\n";
            xml += "</attacker>\n";
            //defender part
            xml += "<defender>\n";
            xml += "<id>" + getDefender().getId() + "</id>\n";
            xml += "<trg>" + getTargetVillage().getId() + "</trg>\n";
            xml += sDefenders + "/>\n";
            xml += sDiedDefenders + "/>\n";
            if (sDefendersOnTheWay != null) {
                xml += sDefendersOnTheWay + "/>\n";
            }

            if (whereDefendersOutside()) {
                xml += "<outside>\n";

                Enumeration<Village> targetVillages = defendersOutside.keys();

                while (targetVillages.hasMoreElements()) {
                    Village v = targetVillages.nextElement();
                    if (v != null) {
                        String villageString = "<support ";
                        Hashtable<UnitHolder, Integer> support = defendersOutside.get(v);
                        if (support != null) {
                            villageString += "trg=\"" + v.getId() + "\" ";
                            Enumeration<UnitHolder> suppUnits = support.keys();
                            while (suppUnits.hasMoreElements()) {
                                UnitHolder unit = suppUnits.nextElement();
                                villageString += unit.getPlainName() + "=\"" + support.get(unit) + "\" ";
                            }
                            villageString += "/>\n";
                            xml += villageString;
                        }
                    }
                }
                xml += "</outside>\n";
            }

            xml += "</defender>\n";
            if (wasWallDamaged()) {
                xml += "<wall before=\"" + getWallBefore() + "\" after=\"" + getWallAfter() + "\"/>\n";
            }
            if (wasBuildingDamaged()) {
                xml += "<building target=\"" + URLEncoder.encode(getAimedBuilding(), "UTF-8") + "\" before=\"" + getBuildingBefore() + "\" after=\"" + getBuildingAfter() + "\"/>\n";
            }
            if (wasSnobAttack()) {
                xml += "<acceptance before=\"" + getAcceptanceBefore() + "\" after=\"" + getAcceptanceAfter() + "\"/>\n";
            }

            xml += "</report>";
            return xml;
        } catch (Exception e) {
            e.printStackTrace();
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
            Tribe t = new Tribe();
            t.setName("gelöscht");
            t.setId(Integer.MAX_VALUE);
            this.attacker = t;
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
        if (!areAttackersHidden() && getAttackers() != null && getDiedAttackers() != null) {
            result = new Hashtable<UnitHolder, Integer>();
            Hashtable<UnitHolder, Integer> att = getAttackers();
            Hashtable<UnitHolder, Integer> diedAtt = getDiedAttackers();
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
        if (!wasLostEverything() && getDefenders() != null && getDiedDefenders() != null) {
            result = new Hashtable<UnitHolder, Integer>();
            Hashtable<UnitHolder, Integer> def = getDefenders();
            Hashtable<UnitHolder, Integer> diedDef = getDiedDefenders();
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
        //defenders are set to -1 if no information on them could be achived as result of a total loss
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

        if (spyCount == attackerCount) {
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
     * @param defendersOutside the defendersOutside to set
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
        /* System.out.println(getAttacker());
        System.out.println(getSourceVillage());
        System.out.println(getDefender());
        System.out.println(getTargetVillage());
        System.out.println(getAttackers());
        System.out.println(getDiedAttackers());
        System.out.println(getDefenders());
        System.out.println(getDiedDefenders());*/
        return (getAttacker() != null
                && getSourceVillage() != null
                && !getAttackers().isEmpty()
                && !getDiedAttackers().isEmpty()
                && getDefender() != null
                && getTargetVillage() != null
                && !getDefenders().isEmpty()
                && !getDiedDefenders().isEmpty());
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
        result.append("Gesendet: ").append(f.format(new Date(getTimestamp()))).append("\n");
        result.append(isWon() ? "Gewonnen\n" : "Verloren\n");

        if (isSpyReport()) {
            result.append("Farbe: Blau\n");
        } else if (wasLostEverything()) {
            result.append("Farbe: Rot\n");
        } else if (wasLostNothing()) {
            result.append("Farbe: Grün\n");
        } else {
            result.append("Farbe: Gelb\n");
        }
        result.append("Moral: ").append(getMoral()).append("\n");
        result.append("Glück: ").append(getLuck()).append("\n");
        result.append("Angreifer: ").append(getAttacker()).append("\n");
        result.append("Herkunft: ").append(getSourceVillage()).append("\n");
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
        result.append("Verteidiger: ").append(getDefender()).append("\n");
        result.append("Ziel: ").append(getTargetVillage()).append("\n");
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
                                result.append(troops.get(u) + " ");
                            }
                        }
                        result.append("\n");
                    }
                }
            }
        }


        if (wasWallDamaged()) {
            result.append("Wall zerstört von Stufe " + getWallBefore() + " auf " + getWallAfter() + "\n");
        }
        if (wasBuildingDamaged()) {
            result.append(getAimedBuilding() + " zerstört von Stufe " + getBuildingBefore() + " auf " + getBuildingAfter() + "\n");
        }
        if (wasSnobAttack()) {
            result.append("Zustimmung gesenkt von " + getAcceptanceBefore() + " auf " + getAcceptanceAfter() + "\n");
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
}
