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
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

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

    public static Tribe parseFromPlainData(String pLine) {
        //$id, $name, $ally, $villages, $points, $rank
        StringTokenizer tokenizer = new StringTokenizer(pLine, ",");
        Tribe entry = new Tribe();
        if (tokenizer.countTokens() < 6) {
            return null;
        }

        try {
            entry.setId(Integer.parseInt(tokenizer.nextToken()));
            entry.setName(URLDecoder.decode(tokenizer.nextToken(), "UTF-8"));
            entry.setAllyID(Integer.parseInt(tokenizer.nextToken()));
            entry.setVillages(Short.parseShort(tokenizer.nextToken()));
            entry.setPoints(Integer.parseInt(tokenizer.nextToken()));
            entry.setRank(Integer.parseInt(tokenizer.nextToken()));
            return entry;
        } catch (Exception e) {
            //tribe entry invalid
        }
        return null;
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

    public String createDiff(Tribe old) {
        String diff = null;
        if (old == null) {
            diff = getId() + "," + getName() + "," + getAllyID() + "," + getVillages() + "," + getPoints() + "," + getRank() + "\n";
            return diff;
        }

        boolean allyChange = false;
        boolean villagesChange = false;
        boolean pointsChange = false;
        boolean rankChange = false;

        if (getAllyID() != old.getAllyID()) {
            allyChange = true;
        }

        if (getVillages() != old.getVillages()) {
            villagesChange = true;
        }

        if (getPoints() != old.getPoints()) {
            pointsChange = true;
        }

        if (getRank() != old.getRank()) {
            rankChange = true;
        }

        if (allyChange || villagesChange || pointsChange || rankChange) {
            diff = Integer.toString(getId()) + ", ,";

            if (allyChange) {
                diff += getAllyID() + ",";
            } else {
                diff += " ,";
            }

            if (villagesChange) {
                diff += getVillages() + ",";
            } else {
                diff += " ,";
            }

            if (pointsChange) {
                diff += getPoints() + ",";
            } else {
                diff += " ,";
            }

            if (rankChange) {
                diff += getRank();
            } else {
                diff += " ";
            }
        }

        return diff;
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
