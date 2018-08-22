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
import de.tor.tribes.util.StringHelper;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.Color;
import java.io.Serializable;
import java.net.URLDecoder;
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
        villageList = new LinkedList<>();
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
            this.allyID = ally.getId();
        }
    }

    public void addVillage(Village v, boolean pChecked) {
        if (villageList == null) {
            villageList = new LinkedList<>();
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
            villageList = new LinkedList<>();
        }

        return villageList.remove(pVillage);
    }

    public boolean ownsVillage(Village pVillage) {
        if (villageList == null) {
            villageList = new LinkedList<>();
        }

        return villageList.contains(pVillage);
    }

    public Village[] getVillageList() {
        if (villageList == null) {
            villageList = new LinkedList<>();
        }
        return villageList.toArray(new Village[villageList.size()]);
    }

    @Override
    public String toString() {
        return getName();
    }

    private String toolTip = null;
    public String getToolTipText() {
        if(toolTip == null) {
            StringBuilder toolTipBuilder = new StringBuilder();
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            toolTipBuilder.append("<html><table style='border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;'>");
            toolTipBuilder.append("<tr><td><b>Name:</b> </td><td>").append(getName()).append("</td></tr>");
            toolTipBuilder.append("<tr><td>&nbsp;&nbsp;&nbsp;Punkte:</td><td>").append(nf.format(getPoints())).append(" (").append(nf.format(getRank())).append(")</td></tr>");
            toolTipBuilder.append("<tr><td>&nbsp;&nbsp;&nbsp;Besiegte Gegner (Off):</td><td>").append(nf.format(killsAtt)).append(" (").append(nf.format(rankAtt)).append(")</td></tr>");
            toolTipBuilder.append("<tr><td>&nbsp;&nbsp;&nbsp;Besiegte Gegner (Deff):</td><td>").append(nf.format(killsDef)).append(" (").append(nf.format(rankDef)).append(")</td></tr>");
            if (getAlly() != null) {
                toolTipBuilder.append("<tr><td><b>Stamm:</b> </td><td>").append(getAlly().toString()).append("</td></tr>");
                toolTipBuilder.append("<tr><td>&nbsp;&nbsp;&nbsp;Mitglieder: </td><td>").append(nf.format(getAlly().getMembers())).append("</td></tr>");
                toolTipBuilder.append("<tr><td>&nbsp;&nbsp;&nbsp;Punkte: </td><td>").append(nf.format(getAlly().getPoints())).append("(").append(nf.format(getAlly().getRank())).append(")</td></tr>");
            }
            toolTipBuilder.append("</table></html>");
            toolTip = toolTipBuilder.toString();
        }
        return toolTip;
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
        switch(GlobalOptions.getProperties().getInt("default.mark")) {
            case 1:
                DEFAULT = Color.RED;
                break;
            case 2:
                DEFAULT = Color.WHITE;
                break;
            case 0:
            default:
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
            return StringHelper.compareByStringRepresentations(s1, s2);
        }
    }
}
