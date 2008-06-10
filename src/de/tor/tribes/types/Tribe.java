/*
 * Tribe.java
 * 
 * Created on 18.07.2007, 18:59:24
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.tor.tribes.types;

/**
 *
 * @author Charon
 */
public class Tribe {

    private int id = 0;
    private String name = null;
    private int allyID = 0;
    private Ally ally = null;
    private int villages = 0;
    private int points = 0;
    private int rank = 0;
    
    public Tribe() {
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

}
