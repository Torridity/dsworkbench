/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class UnitHolder {

    private String name = null;
    private int wood = 0;
    private int stone = 0;
    private int iron = 0;
    private int pop = 0;
    private int speed = 0;
    private int attack = 0;
    private int defense = 0;
    private int defenseCavalry = 0;
    private int defenseArcher = 0;
    private int carry = 0;
    private double buildTime = 0;

    public UnitHolder(Element pElement) throws Exception {
        try {
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
            } else {
                setName("Unbekannt (" + pElement.getName() + ")");
            }

            setWood(Integer.parseInt(pElement.getChild("wood").getText()));
            setStone(Integer.parseInt(pElement.getChild("stone").getText()));
            setIron(Integer.parseInt(pElement.getChild("iron").getText()));
            setPop(Integer.parseInt(pElement.getChild("pop").getText()));
            setSpeed(Integer.parseInt(pElement.getChild("speed").getText()));
            setAttack(Integer.parseInt(pElement.getChild("attack").getText()));
            setDefense(Integer.parseInt(pElement.getChild("defense").getText()));
            setDefenseCavalry(Integer.parseInt(pElement.getChild("defense_cavalry").getText()));
            setDefenseArcher(Integer.parseInt(pElement.getChild("defense_archer").getText()));
            setCarry(Integer.parseInt(pElement.getChild("carry").getText()));
            setBuildTime(Double.parseDouble(pElement.getChild("build_time").getText()));
        } catch (Exception e) {
            throw new Exception("Fehler beim laden von Einheit '" + pElement.getName() + "'");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWood() {
        return wood;
    }

    public void setWood(int wood) {
        this.wood = wood;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }

    public int getIron() {
        return iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public int getPop() {
        return pop;
    }

    public void setPop(int pop) {
        this.pop = pop;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getDefenseCavalry() {
        return defenseCavalry;
    }

    public void setDefenseCavalry(int defenseCavalry) {
        this.defenseCavalry = defenseCavalry;
    }

    public int getDefenseArcher() {
        return defenseArcher;
    }

    public void setDefenseArcher(int defenseArcher) {
        this.defenseArcher = defenseArcher;
    }

    public int getCarry() {
        return carry;
    }

    public void setCarry(int carry) {
        this.carry = carry;
    }

    public double getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(double buildTime) {
        this.buildTime = buildTime;
    }
}
