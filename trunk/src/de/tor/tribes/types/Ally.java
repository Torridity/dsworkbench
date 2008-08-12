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
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Charon
 */
public class Ally implements Serializable, Comparable {

    private static final long serialVersionUID = 10L;
    private int id = 0;
    private String name = null;
    private String tag = null;
    private short members = 0;
    private int villages = 0;
    private int points = 0;
    private int all_points = 0;
    private int rank = 0;
    private transient List<Tribe> tribes = null;

    public static Ally parseFromPlainData(String pLine) {
        //$id, $name, $tag, $members, $villages, $points, $all_points, $rank
        StringTokenizer tokenizer = new StringTokenizer(pLine, ",");
        Ally entry = new Ally();
        if (tokenizer.countTokens() < 8) {
            return null;
        }

        try {
            entry.setId(Integer.parseInt(tokenizer.nextToken()));
            entry.setName(URLDecoder.decode(tokenizer.nextToken(), "UTF-8"));
            entry.setTag(URLDecoder.decode(tokenizer.nextToken(), "UTF-8"));
            entry.setMembers(Short.parseShort(tokenizer.nextToken()));
            entry.setVillages(Integer.parseInt(tokenizer.nextToken()));
            entry.setPoints(Integer.parseInt(tokenizer.nextToken()));
            entry.setAll_points(Integer.parseInt(tokenizer.nextToken()));
            entry.setRank(Integer.parseInt(tokenizer.nextToken()));
            return entry;
        } catch (Exception e) {
            //ally entry invalid
        }
        return null;
    }

    public Ally() {
        tribes = new LinkedList<Tribe>();
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

    public short getMembers() {
        return members;
    }

    public void setMembers(short members) {
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
        if (tribes == null) {
            tribes = new LinkedList<Tribe>();
        }
        if (!tribes.contains(t)) {
            tribes.add(t);
        }
    }

    public List<Tribe> getTribes() {
        if (tribes == null) {
            tribes = new LinkedList<Tribe>();
        }
        return tribes;
    }

    public String getHTMLInfo() {
        NumberFormat nf = NumberFormat.getInstance();
        String allyInfo = "<html><b>Stamm (Tag):</b> " + getName() + " (" + getTag() + ")";
        allyInfo += " <b>Punkte (Rang):</b> " + nf.format(getPoints()) + " (" + nf.format(getRank()) + ")";
        allyInfo += " <b>Member (Dörfer):</b> " + nf.format(getMembers()) + " (" + nf.format(getVillages()) + ")</html>";
        return allyInfo;
    }

    @Override
    public String toString() {
        return getName() + " (" + getTag() + ")";
    }

    public String toBBCode() {
        return "[ally]" + getName() + "[/ally]";
    }

    public String createDiff(Ally old) {
        String diff = null;
        if (old == null) {
            diff = getId() + "," + getName() + "," + getTag() + "," + getMembers() + "," + getVillages() + "," + getPoints() + "," + getAll_points() + "," + getRank() + "\n";
            return diff;
        }

        boolean nameChange = false;
        boolean tagChange = false;
        boolean membersChange = false;
        boolean villagesChange = false;
        boolean pointsChange = false;
        boolean allPointsChange = false;
        boolean rankChange = false;

        if (!getName().equals(old.getName())) {
            nameChange = true;
        }

        if (!getTag().equals(old.getTag())) {
            tagChange = true;
        }

        if (getMembers() != old.getMembers()) {
            membersChange = true;
        }

        if (getVillages() != old.getVillages()) {
            villagesChange = true;
        }

        if (getPoints() != old.getPoints()) {
            pointsChange = true;
        }

        if (getAll_points() != old.getAll_points()) {
            allPointsChange = true;
        }

        if (getRank() != old.getRank()) {
            rankChange = true;
        }


        if (nameChange || tagChange || membersChange || villagesChange || pointsChange || allPointsChange || rankChange) {
            diff = Integer.toString(getId()) + ",";
        }

        if (nameChange) {
            diff += getName() + ",";
        } else {
            diff += " ,";
        }

        if (tagChange) {
            diff += getTag() + ",";
        } else {
            diff += " ,";
        }

        if (membersChange) {
            diff += getMembers() + ",";
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
        if (allPointsChange) {
            diff += getAll_points() + ",";
        } else {
            diff += " ,";
        }
        if (rankChange) {
            diff += getRank() + "\n";
        } else {
            diff += " \n";
        }

        return diff;
    }

    @Override
    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }
}
