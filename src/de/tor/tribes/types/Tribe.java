/*
 * Tribe.java
 * 
 * Created on 18.07.2007, 18:59:24
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Charon
 */
public class Tribe implements Serializable, Comparable {

    private static final long serialVersionUID = 10L;
    private int id = 0;
    private String name = null;
    private int allyID = 0;
    private transient Ally ally = null;
    private short villages = 0;
    private double points = 0;
    private int rank = 0;
    private transient List<Village> villageList = null;
    private double killsAtt = 0;
    private int rankAtt = 0;
    private double killsDef = 0;
    private int rankDef = 0;

    public Tribe() {
        villageList = new LinkedList();
    }
    //$id, $name, $ally, $villages, $points, $rank

    public static Tribe parseFromPlainData(String pLine) {
        //$id, $name, $ally, $villages, $points, $rank
        StringTokenizer tokenizer = new StringTokenizer(pLine, ",");
        Tribe entry = new Tribe();
        if (tokenizer.countTokens() < 6) {
            return null;
        }

        try {
            entry.setId(Integer.parseInt(tokenizer.nextToken()));
            String nn = tokenizer.nextToken();
            String n = URLDecoder.decode(nn, "UTF-8");

            entry.setName(n);
            entry.setAllyID(Integer.parseInt(tokenizer.nextToken()));
            entry.setVillages(Short.parseShort(tokenizer.nextToken()));
            entry.setPoints(Double.parseDouble(tokenizer.nextToken()));
            entry.setRank(Integer.parseInt(tokenizer.nextToken()));
            return entry;
        } catch (Exception e) {
            //tribe entry invalid
        }
        return null;
    }

    public String toPlainData() {
        StringBuffer b = new StringBuffer();
        b.append(getId());
        b.append(",");
        try {
            b.append(URLEncoder.encode(getName(), "UTF-8"));
        } catch (Exception e) {
            b.append(getName());
        }
        b.append(",");
        b.append(getAllyID());
        b.append(",");
        b.append(getVillages());
        b.append(",");
        b.append(getPoints());
        b.append(",");
        b.append(getRank());
        return b.toString();
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

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
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
        if (villageList == null) {
            villageList = new LinkedList<Village>();
        }
        if (!villageList.contains(v)) {
            villageList.add(v);
        }
    }

    public List<Village> getVillageList() {
        if (villageList == null) {
            villageList = new LinkedList<Village>();
        }

        return villageList;
    }

    public String getHTMLInfo() {
        StringBuffer b = new StringBuffer();
        NumberFormat nf = NumberFormat.getInstance();
        b.append("<html><b>Spieler:</b> ");
        b.append(getName());
        b.append(" <b>Punkte (Rang):</b> ");
        b.append(nf.format(getPoints()));
        b.append(" (");
        b.append(nf.format(getRank()));
        b.append(")");
        b.append(" <b>Dörfer:</b> ");
        b.append(nf.format(getVillages()));
        b.append(" <b>Kills Off (Rang):</b> ");
        b.append(nf.format(getKillsAtt()));
        b.append(" (");
        b.append(nf.format(getRankAtt()));
        b.append(") ");
        b.append(" <b>Kills Deff (Rang):</b> ");
        b.append(nf.format(getKillsDef()));
        b.append(" (");
        b.append(nf.format(getRankDef()));
        b.append(") ");
        b.append("</html>");
        return b.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    public String toBBCode() {
        return "[player]" + getName() + "[/player]";
    }

    public double getKillsAtt() {
        return killsAtt;
    }

    public void setKillsAtt(double killsAtt) {
        this.killsAtt = killsAtt;
    }

    public int getRankAtt() {
        return rankAtt;
    }

    public void setRankAtt(int rankAtt) {
        this.rankAtt = rankAtt;
    }

    public double getKillsDef() {
        return killsDef;
    }

    public void setKillsDef(double killsDef) {
        this.killsDef = killsDef;
    }

    public int getRankDef() {
        return rankDef;
    }

    public void setRankDeff(int rankDef) {
        this.rankDef = rankDef;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Tribe) {
            return getName().compareTo(((Tribe) o).getName());
        }
        return -1;
    }
    public static final Comparator<Tribe> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

    private static class CaseInsensitiveComparator
            implements Comparator<Tribe>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Tribe s1, Tribe s2) {
            int n1 = s1.toString().length(), n2 = s2.toString().length();
            for (int i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
                char c1 = s1.toString().charAt(i1);
                char c2 = s2.toString().charAt(i2);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 - n2;
        }
    }
}
