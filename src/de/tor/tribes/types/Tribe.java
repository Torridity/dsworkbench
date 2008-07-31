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

    private static final long serialVersionUID = 10L;
    private int id = 0;
    private String name = null;
    private int allyID = 0;
    private Ally ally = null;
    private short villages = 0;
    private int points = 0;
    private int rank = 0;
    private List<Village> villageList = null;
    private int killsAtt = 0;
    private int rankAtt = 0;
    private int killsDef = 0;
    private int rankDef = 0;

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

    public short getVillages() {
        return villages;
    }

    public void setVillages(short villages) {
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
        String tribeInfo = "<html><b>Spieler:</b> " + getName();
        tribeInfo += " <b>Punkte (Rang):</b> " + nf.format(getPoints()) + " (" + nf.format(getRank()) + ")";
        tribeInfo += " <b>Dörfer:</b> " + nf.format(getVillages());
        tribeInfo += " <b>Kills Off (Rang):</b> " + nf.format(getKillsAtt()) + " (" + nf.format(getRankAtt()) + ") ";
        tribeInfo += " <b>Kills Def (Rang):</b> " + nf.format(getKillsDef()) + " (" + nf.format(getRankDef()) + ") " + "</html>";
        return tribeInfo;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String toBBCode() {
        return "[player]" + getName() + "[/player]";
    }

    public int getKillsAtt() {
        return killsAtt;
    }

    public void setKillsAtt(int killsAtt) {
        this.killsAtt = killsAtt;
    }

    public int getRankAtt() {
        return rankAtt;
    }

    public void setRankAtt(int rankAtt) {
        this.rankAtt = rankAtt;
    }

    public int getKillsDef() {
        return killsDef;
    }

    public void setKillsDef(int killsDef) {
        this.killsDef = killsDef;
    }

    public int getRankDef() {
        return rankDef;
    }

    public void setRankDeff(int rankDef) {
        this.rankDef = rankDef;
    }
}
