/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class FightReport {

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
    private Hashtable<UnitHolder, Integer> defendersOutside = null;
    private boolean conquered = false;
    private byte wallBefore = -1;
    private byte wallAfter = -1;
    private String aimedBuilding = null;
    private byte buildingBefore = -1;
    private byte buildingAfter = -1;
    private byte acceptanceBefore = 100;
    private byte acceptanceAfter = 100;

    public FightReport() {
    }

    public FightReport(Element pElement) throws Exception {
        setTimestamp(Long.parseLong(pElement.getChild("timestamp").getText()));
        setMoral(Double.parseDouble(pElement.getChild("moral").getText()));
        setLuck(Double.parseDouble(pElement.getChild("luck").getText()));
        //attacker stuff
        Element attackerElement = pElement.getChild("attacker");
        Element defenderElement = pElement.getChild("defender");
        int attackerId = Integer.parseInt(attackerElement.getChild("id").getText());
        setAttacker(DataHolder.getSingleton().getTribes().get(attackerId));
        int source = Integer.parseInt(attackerElement.getChild("src").getText());
        setSourceVillage(DataHolder.getSingleton().getVillagesById().get(source));
        int defenderId = Integer.parseInt(defenderElement.getChild("id").getText());
        setDefender(DataHolder.getSingleton().getTribes().get(defenderId));
        int target = Integer.parseInt(defenderElement.getChild("trg").getText());
        setTargetVillage(DataHolder.getSingleton().getVillagesById().get(target));
        Element aAmount = attackerElement.getChild("amount");
        Element aDiedAmount = attackerElement.getChild("died");
        Element dAmount = defenderElement.getChild("amount");
        Element dDiedAmount = defenderElement.getChild("died");
        attackers = new Hashtable<UnitHolder, Integer>();
        diedAttackers = new Hashtable<UnitHolder, Integer>();
        defenders = new Hashtable<UnitHolder, Integer>();
        diedDefenders = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            String unitName = unit.getPlainName();
            attackers.put(unit, aAmount.getAttribute(unitName).getIntValue());
            diedAttackers.put(unit, aDiedAmount.getAttribute(unitName).getIntValue());
            defenders.put(unit, dAmount.getAttribute(unitName).getIntValue());
            diedDefenders.put(unit, dDiedAmount.getAttribute(unitName).getIntValue());
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
            setAimedBuilding(e.getAttribute("target").getValue());
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
    }

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
            Enumeration<UnitHolder> units = attackers.keys();
            while (units.hasMoreElements()) {
                UnitHolder unit = units.nextElement();
                sAttackers += unit.getPlainName() + "=\"" + attackers.get(unit) + "\" ";
                sDiedAttackers += unit.getPlainName() + "=\"" + diedAttackers.get(unit) + "\" ";
                sDefenders += unit.getPlainName() + "=\"" + defenders.get(unit) + "\" ";
                sDiedDefenders += unit.getPlainName() + "=\"" + diedDefenders.get(unit) + "\" ";
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
        this.attacker = attacker;
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
        this.defender = defender;
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

    /**
     * @param diedDefenders the diedDefenders to set
     */
    public void setDiedDefenders(Hashtable<UnitHolder, Integer> diedDefenders) {
        this.diedDefenders = diedDefenders;
    }

    public boolean wasLostEverything() {
        try {
            return (defenders.get(defenders.keys().nextElement()) < 0);
        } catch (Exception e) {
            return false;
        }
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

    /**
     * @return the defendersOutside
     */
    public Hashtable<UnitHolder, Integer> getDefendersOutside() {
        return defendersOutside;
    }

    /**
     * @param defendersOutside the defendersOutside to set
     */
    public void setDefendersOutside(Hashtable<UnitHolder, Integer> defendersOutside) {
        this.defendersOutside = defendersOutside;
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
        return (getAttacker() != null &&
                getSourceVillage() != null &&
                !getAttackers().isEmpty() &&
                !getDiedAttackers().isEmpty() &&
                getDefender() != null &&
                getTargetVillage() != null &&
                !getDefenders().isEmpty() &&
                !getDiedDefenders().isEmpty());
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm");
        result.append("Gesendet: " + f.format(new Date(getTimestamp())) + "\n");
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
        result.append("Moral: " + getMoral() + "\n");
        result.append("Glück: " + getLuck() + "\n");
        result.append("Angreifer: " + getAttacker() + "\n");
        result.append("Herkunft: " + getSourceVillage() + "\n");
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

        result.append("Anzahl: " + sAttackers);
        result.append("Verluste: " + sAttackersDied);
        result.append("Verteidiger: " + getDefender() + "\n");
        result.append("Ziel: " + getTargetVillage() + "\n");
        result.append("Anzahl: " + sDefenders);
        result.append("Verluste: " + sDefendersDied);


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

    public String getToolTipText() {
        String res = "<table width=\"468\"\">" +
                "<tr>" +
                "<td colspan=\"2\">Gesendet</td>" +
                "<td colspan=\"3\">13.12.10 14:00</td>" +
                "</tr>" +
                "<tr>" +
                "<td width=\"75\" height=\"24\">-10.9</td>" +
                "<td width=\"25\">&nbsp;</td>" +
                "<td width=\"154\">" +
                "<table class=\"luck\" cellspacing=\"0\" cellpadding=\"0\">" +
                "<tr>" +
                "<td width=\"29.87779497\" height=\"12\"></td>" +
                "<td width=\"20.12220503\" style=\"background-color:#F00;\"></td>" +
                "<td width=\"2\" style=\"background-color:rgb(0, 0, 0)\"></td>" +
                "<td width=\"0\" style=\"background-image:url(graphic/balken_glueck.png?1);\"></td>" +
                "<td width=\"50\"></td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "<td width=\"21\">&nbsp;</td>" +
                "<td width=\"159\">&nbsp;</td>" +
                "</tr>" +
                "<tr>" +
                "<td colspan=\"5\">Moral: 100%</td>" +
                " </tr>" +
                "</table>";
        return res;
    }
}
