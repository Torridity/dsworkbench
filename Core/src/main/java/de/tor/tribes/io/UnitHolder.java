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
package de.tor.tribes.io;

import org.jdom.Element;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author Charon
 */
public class UnitHolder implements Serializable {

    public static final Comparator<UnitHolder> RUNTIME_COMPARATOR = new RuntimeComparator();
    private static final long serialVersionUID = 10L;
    private String plainName = null;
    private String name = null;
    private double wood = 0;
    private double stone = 0;
    private double iron = 0;
    private double pop = 0;
    private double speed = 0;
    private double attack = 0;
    private double defense = 0;
    private double defenseCavalry = 0;
    private double defenseArcher = 0;
    private double carry = 0;
    private double buildTime = 0;

    public UnitHolder() {
    }

    public UnitHolder(Element pElement) throws Exception {
        try {
            setPlainName(pElement.getName());
            if (pElement.getName().equals("spear")) {
                setName("Speerträger");
            } else if (pElement.getName().equals("sword")) {
                setName("Schwertkämpfer");
            } else if (pElement.getName().equals("axe")) {
                setName("Axtkämpfer");
            } else if (pElement.getName().equals("archer")) {
                setName("Bogenschütze");
            } else if (pElement.getName().equals("spy")) {
                setName("Späher");
            } else if (pElement.getName().equals("light")) {
                setName("Leichte Kavallerie");
            } else if (pElement.getName().equals("marcher")) {
                setName("Berittener Bogenschütze");
            } else if (pElement.getName().equals("heavy")) {
                setName("Schwere Kavallerie");
            } else if (pElement.getName().equals("ram")) {
                setName("Ramme");
            } else if (pElement.getName().equals("catapult")) {
                setName("Katapult");
            } else if (pElement.getName().equals("knight")) {
                setName("Paladin");
            } else if (pElement.getName().equals("snob")) {
                setName("Adelsgeschlecht");
            } else if (pElement.getName().equals("militia")) {
                setName("Miliz");
            } else {
                setName("Unbekannt (" + pElement.getName() + ")");
            }

            setWood(Double.parseDouble(pElement.getChild("wood").getText()));
            setStone(Double.parseDouble(pElement.getChild("stone").getText()));
            setIron(Double.parseDouble(pElement.getChild("iron").getText()));
            setPop(Double.parseDouble(pElement.getChild("pop").getText()));
            setSpeed(Double.parseDouble(pElement.getChild("speed").getText()));
            setAttack(Double.parseDouble(pElement.getChild("attack").getText()));
            setDefense(Double.parseDouble(pElement.getChild("defense").getText()));
            setDefenseCavalry(Double.parseDouble(pElement.getChild("defense_cavalry").getText()));
            setDefenseArcher(Double.parseDouble(pElement.getChild("defense_archer").getText()));
            setCarry(Double.parseDouble(pElement.getChild("carry").getText()));
            setBuildTime(Double.parseDouble(pElement.getChild("build_time").getText()));
        } catch (Exception e) {
            throw new Exception("Fehler beim laden von Einheit '" + pElement.getName() + "'", e);
        }
    }

    public String getPlainName() {
        return plainName;
    }

    public void setPlainName(String name) {
        this.plainName = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWood() {
        return wood;
    }

    public void setWood(double wood) {
        this.wood = wood;
    }

    public double getStone() {
        return stone;
    }

    public void setStone(double stone) {
        this.stone = stone;
    }

    public double getIron() {
        return iron;
    }

    public void setIron(double iron) {
        this.iron = iron;
    }

    public double getPop() {
        return pop;
    }

    public void setPop(double pop) {
        this.pop = pop;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getAttack() {
        return attack;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public double getDefenseCavalry() {
        return defenseCavalry;
    }

    public void setDefenseCavalry(double defenseCavalry) {
        this.defenseCavalry = defenseCavalry;
    }

    public double getDefenseArcher() {
        return defenseArcher;
    }

    public void setDefenseArcher(double defenseArcher) {
        this.defenseArcher = defenseArcher;
    }

    public double getCarry() {
        return carry;
    }

    public void setCarry(double carry) {
        this.carry = carry;
    }

    public boolean isFarmUnit() {
        return getCarry() > 0 || (getPlainName() != null && getPlainName().equals("spy"));
    }

    public boolean isInfantry() {
        String plain = getPlainName();
        return plain != null && (plain.equals("spear") || plain.equals("sword") || plain.equals("archer") || plain.equals("axe"));
    }

    public boolean isCavalry() {
        String plain = getPlainName();
        return plain != null && (plain.equals("spy") || plain.equals("light") || plain.equals("marcher") || plain.equals("heavy"));
    }

    public boolean isOther() {
        return !isInfantry() && !isCavalry();
    }

    public boolean isDefense() {
        String plain = getPlainName();
        return plain != null && (plain.equals("spear") || plain.equals("sword") || plain.equals("archer") || plain.equals("spy") || plain.equals("heavy") || plain.equals("catapult"));
    }

    public boolean isOffense() {
        String plain = getPlainName();
        return plain != null && (plain.equals("axe") || plain.equals("spy") || plain.equals("light") || plain.equals("marcher") || plain.equals("ram") || plain.equals("catapult"));
    }

    public boolean isSpecial() {
        return (!isDefense() && !isOffense());
    }

    public boolean isSpy() {
        String plain = getPlainName();
        return plain != null && plain.equals("spy");
    }

    public boolean isSnob() {
        String plain = getPlainName();
        return plain != null && plain.equals("snob");
    }

    public boolean isRetimeUnit() {
        String plain = getPlainName();
        return plain != null && !plain.equals("spy") && !plain.equals("snob") && !plain.equals("militia");
    }

    public double getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(double buildTime) {
        this.buildTime = buildTime;
    }

    public String toBBCode() {
        return "[unit]" + getPlainName() + "[/unit]";
    }

    @Override
    public String toString() {
        return getName();// + "(" + getSpeed() + " Minuten/Feld)";
    }

    private static class RuntimeComparator implements Comparator<UnitHolder>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(UnitHolder s1, UnitHolder s2) {
            return new Double(s1.getSpeed()).compareTo(s2.getSpeed());
        }
    }
}
