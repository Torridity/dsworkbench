/*
 * Tribe.java
 * 
 * Created on 18.07.2007, 18:59:24
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class Tribe implements Serializable {

    private int id = 0;
    private String name = null;
    private int allyID = 0;
    private Ally ally = null;
    private int villages = 0;
    private int points = 0;
    private int rank = 0;
    private List<Village> villageList = null;

    public Tribe() {
        villageList = new LinkedList();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAllyID() {
        return allyID;
    }

    public void setAllyID(int allyID) {
        this.allyID = allyID;
    }

    public int getVillages() {
        return villages;
    }

    public void setVillages(int villages) {
        this.villages = villages;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Ally getAlly() {
        return ally;
    }

    public void setAlly(Ally ally) {
        this.ally = ally;
    }

    public void addVillage(Village v) {
        villageList.add(v);
    }

    public List<Village> getVillageList() {
        return villageList;
    }

    public String getHTMLInfo() {
        NumberFormat nf = NumberFormat.getInstance();
        String tribeInfo = "<html><b>Name:</b> " + getName();
        tribeInfo += " <b>Punkte (Rang):</b> " + nf.format(getPoints()) + " (" + nf.format(getRank()) + ")";
        tribeInfo += " <b>Dörfer:</b> " + nf.format(getVillages()) + "</html>";
        return tribeInfo;
    }

    public String toString() {
        return getName();
    }
}
