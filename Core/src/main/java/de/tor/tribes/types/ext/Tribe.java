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
package de.tor.tribes.types.ext;

import de.tor.tribes.types.Marker;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.Color;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Charon
 */
public class Tribe implements Comparable<Tribe>, Serializable {

    private int id = 0;
    private String name = null;
    private int allyID = 0;
    private Ally ally = null;
    private double points = 0;
    private int rank = 0;
    private List<Village> villageList = null;
    private double killsAtt = 0;
    private int rankAtt = 0;
    private double killsDef = 0;
    private int rankDef = 0;

    public Tribe() {
        villageList = new LinkedList<Village>();
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
            entry.setName(URLDecoder.decode(tokenizer.nextToken(), "UTF-8"));
            entry.setAllyID(Integer.parseInt(tokenizer.nextToken()));
            //skip villages amount as we hold this information separately
            tokenizer.nextToken();
            entry.setPoints(Double.parseDouble(tokenizer.nextToken()));
            entry.setRank(Integer.parseInt(tokenizer.nextToken()));
            return entry;
        } catch (Exception e) {
            //tribe entry invalid
        }
        return null;
    }

    public String toPlainData() {
        StringBuilder b = new StringBuilder();
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
        if (name == null) {
            this.name = "";
        } else {
            this.name = name;
        }
    }

    public int getAllyID() {
        return allyID;
    }

    public void setAllyID(int allyID) {
        this.allyID = allyID;
    }

    public short getVillages() {
        if (villageList == null) {
            return 0;
        }
        return (short) villageList.size();
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
        if (ally != null) {
            setAllyID(ally.getId());
        }
    }

    public void addVillage(Village v, boolean pChecked) {
        if (villageList == null) {
            villageList = new LinkedList<Village>();
        }
        if (!pChecked || !villageList.contains(v)) {
            villageList.add(v);
        }
    }

    public void addVillage(Village v) {
        addVillage(v, false);
    }

    public boolean removeVillage(Village pVillage) {
        if (villageList == null) {
            villageList = new LinkedList<Village>();
        }

        return villageList.remove(pVillage);
    }

    public boolean ownsVillage(Village pVillage) {
        if (villageList == null) {
            villageList = new LinkedList<Village>();
        }

        return villageList.contains(pVillage);
    }

    public Village[] getVillageList() {
        if (villageList == null) {
            villageList = new LinkedList<Village>();
        }
        return villageList.toArray(new Village[villageList.size()]);
    }

    public String getHTMLInfo() {
        StringBuilder b = new StringBuilder();
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

    public String getToolTipText() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String res = "<html><table style='border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;'>";
        res += "<tr><td><b>Name:</b> </td><td>" + getName() + "</td></tr>";
        res += "<tr><td>&nbsp;&nbsp;&nbsp;Punkte:</td><td>" + nf.format(getPoints()) + " (" + nf.format(getRank()) + ")</td></tr>";
        res += "<tr><td>&nbsp;&nbsp;&nbsp;Besiegte Gegner (Off):</td><td>" + nf.format(getKillsAtt()) + " (" + nf.format(getRankAtt()) + ")</td></tr>";
        res += "<tr><td>&nbsp;&nbsp;&nbsp;Besiegte Gegner (Deff):</td><td>" + nf.format(getKillsDef()) + " (" + nf.format(getRankDef()) + ")</td></tr>";
        if (getAlly() != null) {
            res += "<tr><td><b>Stamm:</b> </td><td>" + getAlly().toString() + "</td></tr>";
            res += "<tr><td>&nbsp;&nbsp;&nbsp;Mitglieder: </td><td>" + nf.format(getAlly().getMembers()) + "</td></tr>";
            res += "<tr><td>&nbsp;&nbsp;&nbsp;Punkte: </td><td>" + nf.format(getAlly().getPoints()) + "(" + nf.format(getAlly().getRank()) + ")</td></tr>";
        }
        Tribe current = GlobalOptions.getSelectedProfile().getTribe();
        if (current != null) {
            if (current.getId() != getId()) {
                double moral = ((getPoints() / current.getPoints()) * 3 + 0.3) * 100;
                moral = (moral > 100) ? 100 : moral;
                res += "<tr><td><b>Moral:</b> </td><td>" + nf.format(moral) + "</td></tr>";
            }
        }
        res += "</table></html>";
        return res;
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

    public Color getMarkerColor() {
        Marker m = MarkerManager.getSingleton().getMarker(this);
        if (m != null) {
            return m.getMarkerColor();
        }

        if (this.equals(Barbarians.getSingleton())) {
            return Color.LIGHT_GRAY;
        }

        if (getId() == GlobalOptions.getSelectedProfile().getTribe().getId()) {
            return Color.YELLOW;
        }
        Color DEFAULT = null;
        try {
            int mark = Integer.parseInt(GlobalOptions.getProperty("default.mark"));
            if (mark == 0) {
                DEFAULT = Constants.DS_DEFAULT_MARKER;
            } else if (mark == 1) {
                DEFAULT = Color.RED;
            } else if (mark == 2) {
                DEFAULT = Color.WHITE;
            }

        } catch (Exception e) {
            DEFAULT = Constants.DS_DEFAULT_MARKER;
        }
        return DEFAULT;
    }

    @Override
    public int compareTo(Tribe o) {
        return getName().compareTo(o.getName());
    }
    public static final Comparator<Tribe> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

    private static class CaseInsensitiveComparator
            implements Comparator<Tribe>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Tribe s1, Tribe s2) {
            if (s1 == null) {
                return 1;
            }
            if (s2 == null) {
                return -1;
            }

            if (s1 == null && s2 == null) {
                return 0;
            }

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
