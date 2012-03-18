/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class BuildingHolder {

    private String name = null;
    private int maxLevel = 0;
    private int minLevel = 0;
    private int wood = 0;
    private int stone = 0;
    private int iron = 0;
    private int pop = 0;
    private double woodFactor = 0;
    private double stoneFactor = 0;
    private double ironFactor = 0;
    private double popFactor = 0;
    private double buildTime = 0;
    private double buildTimeFactor = 0;

    public BuildingHolder(Element pElement) throws Exception {
        try {
            if (pElement.getName().equals("main")) {
                setName("Hauptgebäude");
            } else if (pElement.getName().equals("barracks")) {
                setName("Kaserne");
            } else if (pElement.getName().equals("stable")) {
                setName("Stall");
            } else if (pElement.getName().equals("garage")) {
                setName("Werkstatt");
            } else if (pElement.getName().equals("snob")) {
                setName("Adelshof");
            } else if (pElement.getName().equals("smith")) {
                setName("Schmiede");
            } else if (pElement.getName().equals("place")) {
                setName("Versammlungsplatz");
            } else if (pElement.getName().equals("statue")) {
                setName("Statue");
            } else if (pElement.getName().equals("market")) {
                setName("Markt");
            } else if (pElement.getName().equals("wood")) {
                setName("Holzfäller");
            } else if (pElement.getName().equals("stone")) {
                setName("Lehmgrube");
            } else if (pElement.getName().equals("iron")) {
                setName("Eisenmine");
            } else if (pElement.getName().equals("farm")) {
                setName("Bauernhof");
            } else if (pElement.getName().equals("storage")) {
                setName("Speicher");
            } else if (pElement.getName().equals("hide")) {
                setName("Versteck");
            } else if (pElement.getName().equals("wall")) {
                setName("Wall");
            } else {
                setName("Unbekannt (" + pElement.getName() + ")");
            }

            setMaxLevel(Integer.parseInt(pElement.getChild("max_level").getText()));
            setMinLevel(Integer.parseInt(pElement.getChild("min_level").getText()));
            setWood(Integer.parseInt(pElement.getChild("wood").getText()));
            setIron(Integer.parseInt(pElement.getChild("iron").getText()));
            setStone(Integer.parseInt(pElement.getChild("stone").getText()));
            setPop(Integer.parseInt(pElement.getChild("pop").getText()));
            setWoodFactor(Double.parseDouble(pElement.getChild("wood_factor").getText()));
            setStoneFactor(Double.parseDouble(pElement.getChild("stone_factor").getText()));
            setIronFactor(Double.parseDouble(pElement.getChild("iron_factor").getText()));
            setPopFactor(Double.parseDouble(pElement.getChild("pop_factor").getText()));
            setBuildTime(Double.parseDouble(pElement.getChild("build_time").getText()));
            setBuildTimeFactor(Double.parseDouble(pElement.getChild("build_time_factor").getText()));
        } catch (Exception e) {
            throw new Exception("Fehler beim Einlesen von Gebäude '" + pElement.getName() + "'");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
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

    public double getWoodFactor() {
        return woodFactor;
    }

    public void setWoodFactor(double woodFactor) {
        this.woodFactor = woodFactor;
    }

    public double getStoneFactor() {
        return stoneFactor;
    }

    public void setStoneFactor(double stoneFactor) {
        this.stoneFactor = stoneFactor;
    }

    public double getIronFactor() {
        return ironFactor;
    }

    public void setIronFactor(double ironFactor) {
        this.ironFactor = ironFactor;
    }

    public double getPopFactor() {
        return popFactor;
    }

    public void setPopFactor(double popFactor) {
        this.popFactor = popFactor;
    }

    public double getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(double buildTime) {
        this.buildTime = buildTime;
    }

    public double getBuildTimeFactor() {
        return buildTimeFactor;
    }

    public void setBuildTimeFactor(double buildTimeFactor) {
        this.buildTimeFactor = buildTimeFactor;
    }

    public String toString() {
        return getName();
    }
}
