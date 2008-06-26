/*
 * TribesStructure.java
 * 
 * Created on 18.07.2007, 18:58:23
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
public class Ally implements Serializable {

    private int id = 0;
    private String name = null;
    private String tag = null;
    private int members = 0;
    private int villages = 0;
    private int points = 0;
    private int all_points = 0;
    private int rank = 0;
    private List<Tribe> tribes = null;

    public Ally() {
        tribes = new LinkedList();
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
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

    public int getAll_points() {
        return all_points;
    }

    public void setAll_points(int all_points) {
        this.all_points = all_points;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void addTribe(Tribe t) {
        tribes.add(t);
    }

    public List<Tribe> getTribes() {
        return tribes;
    }

    public String getHTMLInfo() {
        NumberFormat nf = NumberFormat.getInstance();
        String allyInfo = "<html><b>Name (Tag):</b> " + getName() + " (" + getTag() + ")";
        allyInfo += " <b>Punkte (Rang):</b> " + nf.format(getPoints()) + " (" + nf.format(getRank()) + ")";
        allyInfo += " <b>Member (Dörfer):</b> " + nf.format(getMembers()) + " (" + nf.format(getVillages()) + ")</html>";
        return allyInfo;
    }

    public String toString() {
        return getName();
    }
}
