/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.sim;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class OldSimulator {

    boolean DEBUG = true;
    private Hashtable<UnitHolder, FighterPart> off = null;
    private Hashtable<UnitHolder, FighterPart> def = null;
    private boolean nightBonus = false;
    private double luck = 0.0;
    private double moral = 100;
    private int wallLevel = 0;
    private int buildingLevel = 0;
    private List<UnitHolder> units = new LinkedList<UnitHolder>();
    private boolean win = false;
    private double offDecrement = 0;
    private double defDecrement = 0;
    private int wallResult = 0;
    private int cataResult = 0;

    public void test() {
        parseUnits();
        Hashtable<UnitHolder, FighterPart> testOff = new Hashtable<UnitHolder, FighterPart>();
        Hashtable<UnitHolder, FighterPart> testDef = new Hashtable<UnitHolder, FighterPart>();
        FighterPart p = new FighterPart(getUnitByPlainName("axe"), 2000, 1);
        //build off
        testOff.put(getUnitByPlainName("axe"), p);
        //p = new SidePart(getUnitByPlainName("heavy"), 10, 1);
        //testOff.put(getUnitByPlainName("heavy"), p);
        p = new FighterPart(getUnitByPlainName("ram"), 100, 3);
        testOff.put(getUnitByPlainName("ram"), p);
        //build def
        p = new FighterPart(getUnitByPlainName("sword"), 150, 1);
        testDef.put(getUnitByPlainName("sword"), p);

        calculate(testOff, testDef, false, 0, 100, 17, 0);
    }

    public void calculate(Hashtable<UnitHolder, FighterPart> pOff, Hashtable<UnitHolder, FighterPart> pDef, boolean pNightBonus, double pLuck, double pMoral, int pWallLevel, int pBuildingLevel) {
        off = pOff;
        def = pDef;
        nightBonus = pNightBonus;
        luck = pLuck;
        moral = pMoral;
        wallLevel = pWallLevel;
        buildingLevel = pBuildingLevel;
        double offInfantryValue = calculateInfantryValue(off);
        double offCavaleryValue = calculateCavaleryValue(off);
        double infantryDefValue = calculateInfantryDefValue();
        double cavaleryDefValue = calculateCavaleryDefValue();
        double infantryRation = offInfantryValue / (offInfantryValue + offCavaleryValue);
        double cavaleryRation = 1 - infantryRation;
        double defStrength = infantryRation * infantryDefValue + cavaleryRation * cavaleryDefValue;
        FighterPart rams = off.get(getUnitByPlainName("ram"));
        double ramCount = 0;
        double ramAttPoint = 0;
        double wallAtFight = wallLevel;
        if (rams != null) {
            ramCount = rams.getUnitCount();
            double wallReduction = ramCount / (4 * Math.pow(1.09, wallLevel));
            if (wallReduction > (double) wallLevel / 2) {
                wallReduction = (double) wallLevel / 2;
            }
            switch (rams.getUnitTech()) {
                case 2: {
                    ramAttPoint = 2.5;
                    break;
                }
                case 3: {
                    ramAttPoint = 2.8;
                    break;
                }
                default: {
                    ramAttPoint = 2;
                }
            }
            println("WallReduction: " + wallReduction);
            wallAtFight = Math.round(wallLevel - wallReduction);
            println("WallAtFight: " + wallAtFight);
            println("RamCount: " + ramCount);
            println("RamAttPoints: " + ramAttPoint);
        }

        FighterPart cata = off.get(getUnitByPlainName("catapult"));
        double cataAttPoint = 0;
        if (cata != null) {
            switch (cata.getUnitTech()) {
                case 2: {
                    cataAttPoint = 125;
                    break;
                }
                case 3: {
                    cataAttPoint = 140;
                    break;
                }
                default: {
                    cataAttPoint = 100;
                }
            }
        }
        //double wallReduction = off.get(getUnitByPlainName("ram")) / (4 * 1.09 ^ wall level)
        //include wall
        defStrength = (20 + 50 * wallAtFight) + (defStrength * Math.pow(1.037, wallAtFight));

        println("OffInf " + offInfantryValue);
        println("OffCav " + offCavaleryValue);

        println("DefInf " + (infantryDefValue * infantryRation));
        println("DefCav " + (cavaleryDefValue * cavaleryRation));
        println("InfRatio " + infantryRation);
        println("CavRatio " + cavaleryRation);
        println("---------------");
        double offStrength = offInfantryValue + offCavaleryValue;
        println("OffStrength " + offStrength);
        println("DefStrength " + defStrength);

        double lossRatioOff = Math.pow((defStrength / offStrength), 1.5);
        double lossRatioDef = Math.pow((offStrength / defStrength), 1.5);
        println("LossOff: " + lossRatioOff);
        println("LossDef: " + lossRatioDef);
        double wallAfter = wallLevel;
        if (lossRatioOff > 1) {
            //attack losses
            double wallDemolish = Math.pow((offStrength / defStrength), 1.5) * (ramAttPoint * ramCount) / (8 * Math.pow(1.09, wallLevel));
            println("Demo " + wallDemolish);
            wallAfter = Math.round(wallLevel - wallDemolish);
        } else {
            //attacker wins
            double wallDemolish = (2 - Math.pow((defStrength / offStrength), 1.5)) * (ramAttPoint * ramCount) / (8 * Math.pow(1.09, wallLevel));
            println("Demo " + wallDemolish);
            wallAfter = Math.round(wallLevel - wallDemolish);
        }

        double buildingAfter = buildingLevel;
        if (cata != null) {
            if (lossRatioOff > 1) {
                //attack losses
                double buildingDemolish = Math.pow((offStrength / defStrength), 1.5) * (cataAttPoint * cata.getUnitCount()) / (600 * Math.pow(1.09, buildingLevel));
                println("DemoBuild " + buildingDemolish);
                buildingAfter = Math.round(buildingLevel - buildingDemolish);
            } else {
                //attacker wins
                double buildingDemolish = (2 - Math.pow((defStrength / offStrength), 1.5)) * (cataAttPoint * cata.getUnitCount()) / (600 * Math.pow(1.09, buildingLevel));
                println("DemoBuild " + buildingDemolish);
                buildingAfter = Math.round(buildingLevel - buildingDemolish);
            }
        }
        println("WallAfter: " + wallAfter);
        println("BuildingAfter: " + buildingAfter);
        win = (lossRatioOff < 1);
        offDecrement = lossRatioOff;
        defDecrement = lossRatioDef;
        wallResult = (wallAfter <= 0) ? 0 : (int) wallAfter;
        cataResult = (buildingAfter <= 0) ? 0 : (int) buildingAfter;
    }

    private void println(String value) {
        if (DEBUG) {
            System.out.println(value);
        }
    }

    public boolean hasWon() {
        return win;
    }

    public double getOffDecrement() {
        return offDecrement;
    }

    public double getDefDecrement() {
        return defDecrement;
    }

    public int getWallResult() {
        return wallResult;
    }

    public int getCataResult() {
        return cataResult;
    }

    //calc infantry strength for spear, sword, axe, ram, cata, snob
    private double calculateInfantryValue(Hashtable<UnitHolder, FighterPart> pLocation) {
        UnitHolder unit = getUnitByPlainName("spear");
        FighterPart part = pLocation.get(unit);
        double techFactor = 0;
        double luckFactor = (100 + luck) / 100;
        double result = 0;
        if (part != null) {
            techFactor = getTechFactor(part.getUnitTech());
            result = part.getUnitCount() * unit.getAttack() * moral / 100 * techFactor;
        }
        unit = getUnitByPlainName("sword");
        part = pLocation.get(unit);
        if (part != null) {
            techFactor = getTechFactor(part.getUnitTech());
            result += part.getUnitCount() * unit.getAttack() * moral / 100 * techFactor;
        }
        unit = getUnitByPlainName("axe");
        part = pLocation.get(unit);
        if (part != null) {
            techFactor = getTechFactor(part.getUnitTech());
            result += part.getUnitCount() * unit.getAttack() * moral / 100 * techFactor;
        }
        unit = getUnitByPlainName("ram");
        part = pLocation.get(unit);
        if (part != null) {
            techFactor = getTechFactor(part.getUnitTech());
            result += part.getUnitCount() * unit.getAttack() * moral / 100 * techFactor;
        }
        unit = getUnitByPlainName("catapult");
        part = pLocation.get(unit);
        if (part != null) {
            techFactor = getTechFactor(part.getUnitTech());
            result += part.getUnitCount() * unit.getAttack() * moral / 100 * techFactor;
        }
        unit = getUnitByPlainName("snob");
        part = pLocation.get(unit);
        if (part != null) {
            techFactor = getTechFactor(part.getUnitTech());
            result += part.getUnitCount() * unit.getAttack() * moral / 100 * techFactor;
        }
        return result * luckFactor;
    }

    //calc calvalery strength for light and heavy
    private double calculateCavaleryValue(Hashtable<UnitHolder, FighterPart> pLocation) {
        UnitHolder unit = getUnitByPlainName("light");
        double techFactor = 0;
        double luckFactor = (100 + luck) / 100;
        double result = 0;
        FighterPart part = pLocation.get(unit);
        if (part != null) {
            techFactor = getTechFactor(part.getUnitTech());
            result = part.getUnitCount() * unit.getAttack() * moral / 100 * techFactor;
        }
        unit = getUnitByPlainName("heavy");
        part = pLocation.get(unit);
        if (part != null) {
            techFactor = getTechFactor(part.getUnitTech());
            result += part.getUnitCount() * unit.getAttack() * moral / 100 * techFactor;
        }
        return result * luckFactor;
    }

    private double calculateInfantryDefValue() {
        double result = 0;
        for (UnitHolder unit : units) {
            FighterPart part = def.get(unit);
            if (part != null) {
                double techFactor = getTechFactor(part.getUnitTech());
                result += part.getUnitCount() * unit.getDefense() * techFactor;
            }
        }
        return result * ((nightBonus) ? 2 : 1);
    }

    private double calculateCavaleryDefValue() {
        double result = 0;
        for (UnitHolder unit : units) {
            FighterPart part = def.get(unit);
            if (part != null) {
                double techFactor = getTechFactor(part.getUnitTech());
                result += part.getUnitCount() * unit.getDefenseCavalry() * techFactor;
            }
        }
        return result * ((nightBonus) ? 2 : 1);
    }

    private double getTechFactor(int pLevel) {
        switch (pLevel) {
            case 2:
                return 1.25;
            case 3:
                return 1.4;
            default:
                return 1;
        }
    }

    /**Parse the list of units*/
    public void parseUnits() {
        String unitFile = "H:/Software/DSWorkbench/servers/de8/units_mod.xml";
        //buildingsFile += "/units.xml";

        try {
            Document d = JaxenUtils.getDocument(new File(unitFile));
            d = JaxenUtils.getDocument(new File(unitFile));
            List<Element> l = JaxenUtils.getNodes(d, "/config/*");
            for (Element e : l) {
                try {
                    units.add(new UnitHolder(e));
                } catch (Exception inner) {
                    inner.printStackTrace();
                }
            }
        } catch (Exception outer) {
            outer.printStackTrace();
        }
    }

    /**Get a unit by its name*/
    public UnitHolder getUnitByPlainName(String pName) {
        for (UnitHolder u : units) {
            if (u.getPlainName().equals(pName)) {
                return u;
            }
        }
        return null;
    }

    public List<UnitHolder> getUnits() {
        return units;
    }

    public static void main(String[] args) {
        OldSimulator sim = new OldSimulator();
        sim.test();

    }
}
