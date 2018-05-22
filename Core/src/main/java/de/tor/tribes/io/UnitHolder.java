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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class UnitHolder implements Serializable {

    public static final Comparator<UnitHolder> RUNTIME_COMPARATOR = new RuntimeComparator();
    private static final long serialVersionUID = 10L;
    private String plainName = null;
    private String name = null;
    private double pop = 0;
    private double speed = 0;
    private double attack = 0;
    private double defense = 0;
    private double defenseCavalry = 0;
    private double defenseArcher = 0;
    private double carry = 0;
    private double buildTime = 0;

    public UnitHolder() {
        name = "";
        plainName = null;
    }

    public UnitHolder(Element pElement) throws Exception {
        try {
            this.plainName = pElement.getName();
            if (pElement.getName().equals("spear")) {
                this.name = "Speerträger";
            } else if (pElement.getName().equals("sword")) {
                this.name = "Schwertkämpfer";
            } else if (pElement.getName().equals("axe")) {
                this.name = "Axtkämpfer";
            } else if (pElement.getName().equals("archer")) {
                this.name = "Bogenschütze";
            } else if (pElement.getName().equals("spy")) {
                this.name = "Späher";
            } else if (pElement.getName().equals("light")) {
                this.name = "Leichte Kavallerie";
            } else if (pElement.getName().equals("marcher")) {
                this.name = "Berittener Bogenschütze";
            } else if (pElement.getName().equals("heavy")) {
                this.name = "Schwere Kavallerie";
            } else if (pElement.getName().equals("ram")) {
                this.name = "Ramme";
            } else if (pElement.getName().equals("catapult")) {
                this.name = "Katapult";
            } else if (pElement.getName().equals("knight")) {
                this.name = "Paladin";
            } else if (pElement.getName().equals("snob")) {
                this.name = "Adelsgeschlecht";
            } else if (pElement.getName().equals("militia")) {
                this.name = "Miliz";
            } else {
                this.name = "Unbekannt (" + pElement.getName() + ")";
            }
            
            this.pop = Double.parseDouble(pElement.getChild("pop").getText());
            this.speed = Double.parseDouble(pElement.getChild("speed").getText());
            this.attack = Double.parseDouble(pElement.getChild("attack").getText());
            this.defense = Double.parseDouble(pElement.getChild("defense").getText());
            this.defenseCavalry = Double.parseDouble(pElement.getChild("defense_cavalry").getText());
            this.defenseArcher = Double.parseDouble(pElement.getChild("defense_archer").getText());
            this.carry = Double.parseDouble(pElement.getChild("carry").getText());
            this.buildTime = Double.parseDouble(pElement.getChild("build_time").getText());
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
        return carry > 0 || (getPlainName() != null && getPlainName().equals("spy"));
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
        return plain != null && (plain.equals("spear") || plain.equals("sword") || plain.equals("archer") || plain.equals("spy") || plain.equals("heavy") || plain.equals("catapult") || plain.equals("knight"));
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
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof UnitHolder) {
            UnitHolder otherU = (UnitHolder) other;
            return hashCode() == other.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.plainName);
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.pop) ^ (Double.doubleToLongBits(this.pop) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.speed) ^ (Double.doubleToLongBits(this.speed) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.attack) ^ (Double.doubleToLongBits(this.attack) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.defense) ^ (Double.doubleToLongBits(this.defense) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.defenseCavalry) ^ (Double.doubleToLongBits(this.defenseCavalry) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.defenseArcher) ^ (Double.doubleToLongBits(this.defenseArcher) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.carry) ^ (Double.doubleToLongBits(this.carry) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.buildTime) ^ (Double.doubleToLongBits(this.buildTime) >>> 32));
        return hash;
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
