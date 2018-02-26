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

import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.html.VillageHTMLTooltipGenerator;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class Village implements Comparable<Village>, Serializable, BBSupport {
    private static Logger logger = Logger.getLogger("Village");

    private final static String[] VARIABLES = new String[]{"%NAME%", "%X%", "%Y%", "%CONTINENT%", "%FULL_NAME%", "%TRIBE%", "%ALLY%", "%POINTS%"};
    private final static String STANDARD_TEMPLATE = "[coord]%X%|%Y%[/coord]";
    public final static int NO_BONUS = 0;
    public final static int WOOD_BONUS = 1;
    public final static int CLAY_BONUS = 2;
    public final static int IRON_BONUS = 3;
    public final static int FARM_BONUS = 4;
    public final static int BARRACKS_BONUS = 5;
    public final static int STABLE_BONUS = 6;
    public final static int WORKSHOP_BONUS = 7;
    public final static int RESOURCES_BONUS = 8;
    public final static int STORAGE_BONUS = 9;

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Village) {
            return id == ((Village) obj).getId();
        }
        return super.equals(obj);
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String nameVal = getName();
        String xVal = Short.toString(x);
        String yVal = Short.toString(y);
        int cont = getContinent();
        String contVal = "K" + Integer.toString(cont);
        String fullNameVal = getFullName();
        String tribeVal = getTribe().toBBCode();
        Ally a = getTribe().getAlly();
        if (a == null) {
            a = NoAlly.getSingleton();
        }
        String allyVal = a.toBBCode();
        NumberFormat f = NumberFormat.getInstance();
        f.setMaximumFractionDigits(0);
        f.setMinimumFractionDigits(0);
        String pointsVal = f.format(getPoints());

        return new String[]{nameVal, xVal, yVal, contVal, fullNameVal, tribeVal, allyVal, pointsVal};
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }
    public static final Comparator<Village> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();
    public static final Comparator<Village> ALLY_TRIBE_VILLAGE_COMPARATOR = new AllyTribeVillageComparator();
    public static final Comparator<String> ALPHA_NUM_COMPARATOR = new IntuitiveStringComparator<>();
    public final static int ORDER_ALPHABETICALLY = 0;
    public final static int ORDER_BY_COORDINATES = 1;
    private static int orderType = ORDER_ALPHABETICALLY;
    private int id;
    private String name = null;
    private short x;
    private short y;
    private Point position = null;
    private int tribeID;
    private Tribe tribe = null;
    private int points = -1;
    private byte type = -1;
    //cached values
    private String stringRepresentation = null;
    private String coordAsString = null;
    private String contAsString = null;
    private boolean visibleOnMap = true;
    private int continent = -1;
    //$id, $name, $x, $y, $tribe, $points, $type

    public Village() {
        position = new Point();
    }

    public Village(int x, int y) {
        this();
        setX((short) x);
        setY((short) y);
    }

    public static Village parseFromPlainData(String pLine) {
        StringTokenizer tokenizer = new StringTokenizer(pLine, ",");
        Village entry = new Village();
        if (tokenizer.countTokens() < 7) {
            return null;
        }

        try {
            entry.setId(Integer.parseInt(tokenizer.nextToken()));
            String n = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
            //replace HTML characters 
            if (n.contains("&")) {
                n = n.replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("&quot;",
                        "\"").replaceAll("&amp;", "&").replaceAll("&tilde;", "~");
            }
            entry.setName(n);
            entry.setX(Short.parseShort(tokenizer.nextToken()));
            entry.setY(Short.parseShort(tokenizer.nextToken()));
            entry.setTribeID(Integer.parseInt(tokenizer.nextToken()));
            entry.setPoints(Integer.parseInt(tokenizer.nextToken()));
            entry.setType(Byte.parseByte(tokenizer.nextToken()));
            if (entry.getPoints() < 21) {
                //invalid village (event stuff?)
                return null;
            }
            //check if village within coordinate range
            Rectangle dim = ServerSettings.getSingleton().getMapDimension();
            if (entry.getX() >= dim.getMinX() && entry.getX() <= dim.getMaxX()
                    && entry.getY() >= dim.getMinY() && entry.getY() <= dim.getMaxY()) {
                return entry;
            } else {
                logger.warn("Imported village out of Range " + entry.getId() + "/" + entry.getCoordAsString());
                return null;
            }
        } catch (Exception e) { //village invalid 
        }

        return null;
    }

    public String toPlainData() {
        StringBuilder b = new StringBuilder();
        b.append(id);
        b.append(",");
        try {
            b.append(URLEncoder.encode(getName(), "UTF-8"));
        } catch (Exception e) {
            b.append(getName());
        }
        b.append(",");
        b.append(x);
        b.append(",");
        b.append(y);
        b.append(",");
        b.append(tribeID);
        b.append(",");
        b.append(getPoints());
        b.append(",");
        b.append(getType());
        return b.toString();
    }

    public static void setOrderType(int pOrderType) {
        if (pOrderType == ORDER_BY_COORDINATES) {
            orderType = ORDER_BY_COORDINATES;
        } else {
            orderType = ORDER_ALPHABETICALLY;
        }
    }

    public static int getOrderType() {
        return orderType;
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

    public String getCoordAsString() {
        if (coordAsString == null) {
            coordAsString = "(" + x + "|" + y + ")";
        }
        return coordAsString;
    }

    public String getFullName() {
        if (stringRepresentation == null) {
            StringBuilder b = new StringBuilder();
            b.append(getName()).append(" ").append(getCoordAsString()).append(" ").append(getContinentString());
            stringRepresentation = b.toString();
        }
        return stringRepresentation;
    }

    public String getShortName() {
        return getCoordAsString() + " " + getContinentString();
    }

    public String getContinentString() {
        if (contAsString == null) {
            int cont = getContinent();
            if (cont < 10 && cont > 0) {
                contAsString = "K0" + cont;
            } else {
                contAsString = "K" + cont;
            }
        }
        return contAsString;
    }

    public void setName(String name) {
        this.name = name;
        stringRepresentation = null;
    }

    public short getX() {
        return x;
    }

    public final void setX(short x) {
        this.x = x;
        position.x = x;
    }

    public short getY() {
        return y;
    }

    public final void setY(short y) {
        this.y = y;
        position.y = y;
    }

    public Point getPosition() {
        return position;
    }

    public int getContinent() {
        if (continent == -1) {
            continent = DSCalculator.getContinent(x, y);
        }
        return continent;
    }

    public int getTribeID() {
        return tribeID;
    }

    public void setTribeID(int tribeID) {
        this.tribeID = tribeID;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return this.type;
    }

    public Tribe getTribe() {
        if (tribe == null) {
            return Barbarians.getSingleton();
        }
        return tribe;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    public Rectangle2D.Double getVirtualBounds() {
        int w = GlobalOptions.getSkin().getBasicFieldHeight();
        int h = GlobalOptions.getSkin().getBasicFieldHeight();
        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        return new Rectangle2D.Double(x, y, (double) w / z, (double) h / z);
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public String getToolTipText() {
        return VillageHTMLTooltipGenerator.buildToolTip(this, false);
    }

    public String getExtendedTooltip() {
        return VillageHTMLTooltipGenerator.buildToolTip(this);
    }

    public String toBBCode() {
        return "[coord]" + x + "|" + y + "[/coord]";
    }

    @Override
    public int compareTo(Village o) {
        if (o == null) {
            return -1;
        }
        //switch order type

        if (orderType == ORDER_ALPHABETICALLY) {
            return toString().compareTo(o.toString());
            //   return ALPHA_NUM_COMPARATOR.compare(toString(), o.toString());
        } else {
            try {

                if (x < o.getX()) {
                    return -1;
                } else if (x > o.getX()) {
                    return 1;
                } else {
                    if (y < o.getY()) {
                        return -1;
                    } else if (y > o.getY()) {
                        return 1;
                    } else {
                        return 0;
                    }

                }
            } catch (Exception e) {
                //left hand side is no village, compare strings
                return toString().toLowerCase().compareTo(o.toString().toLowerCase());
            }
        }
    }

    /**
     * @return the visibleOnMap
     */
    public boolean isVisibleOnMap() {
        return visibleOnMap;
    }

    /**
     * @param visibleOnMap the visibleOnMap to set
     */
    public void setVisibleOnMap(boolean visibleOnMap) {
        this.visibleOnMap = visibleOnMap;
    }

    public int getGraphicsType() {
        int graphicsType = Skin.ID_V1;

        boolean isLeft = false;
        if (getTribe() == Barbarians.getSingleton()) {
            isLeft = true;
        }

        if (getPoints() < 300) {
            if (!isLeft) {
                //changed
                if (getType() != 0) {
                    graphicsType = Skin.ID_B1;
                }
            } else {
                if (getType() == 0) {
                    graphicsType = Skin.ID_V1_LEFT;
                } else {
                    graphicsType = Skin.ID_B1_LEFT;
                }
            }
        } else if (getPoints() < 1000) {
            graphicsType = Skin.ID_V2;
            if (!isLeft) {
                if (getType() != 0) {
                    graphicsType = Skin.ID_B2;
                }
            } else {
                if (getType() == 0) {
                    graphicsType = Skin.ID_V2_LEFT;
                } else {
                    graphicsType = Skin.ID_B2_LEFT;
                }
            }
        } else if (getPoints() < 3000) {
            graphicsType = Skin.ID_V3;
            if (!isLeft) {
                if (getType() != 0) {
                    graphicsType = Skin.ID_B3;
                }
            } else {
                if (getType() == 0) {
                    graphicsType = Skin.ID_V3_LEFT;
                } else {
                    graphicsType = Skin.ID_B3_LEFT;
                }
            }
        } else if (getPoints() < 9000) {
            graphicsType = Skin.ID_V4;
            if (!isLeft) {
                if (getType() != 0) {
                    graphicsType = Skin.ID_B4;
                }
            } else {
                if (getType() == 0) {
                    graphicsType = Skin.ID_V4_LEFT;
                } else {
                    graphicsType = Skin.ID_B4_LEFT;
                }
            }
        } else if (getPoints() < 11000) {
            graphicsType = Skin.ID_V5;
            if (!isLeft) {
                if (getType() != 0) {
                    graphicsType = Skin.ID_B5;
                }
            } else {
                if (getType() == 0) {
                    graphicsType = Skin.ID_V5_LEFT;
                } else {
                    graphicsType = Skin.ID_B5_LEFT;
                }
            }
        } else {
            graphicsType = Skin.ID_V6;
            if (!isLeft) {
                if (getType() != 0) {
                    graphicsType = Skin.ID_B6;
                }

            } else {
                if (getType() == 0) {
                    graphicsType = Skin.ID_V6_LEFT;
                } else {
                    graphicsType = Skin.ID_B6_LEFT;
                }
            }
        }

        return graphicsType;
    }

    public static String getBonusDescription(Village pVillage) {
        if (ServerSettings.getSingleton().getNewBonus() == 0) {
            switch (pVillage.getType()) {
                case WOOD_BONUS:
                    return "10% mehr Holzproduktion";
                case CLAY_BONUS:
                    return "10% mehr Lehmproduktion";
                case IRON_BONUS:
                    return "10% mehr Eisenproduktion";
                case FARM_BONUS:
                    return "10% mehr Bevölkerung";
                case BARRACKS_BONUS:
                    return "10% schnellere Produktion in der Kaserne";
                case STABLE_BONUS:
                    return "10% schnellere Produktion im Stall";
                case WORKSHOP_BONUS:
                    return "10% schnellere Produktion in der Werkstatt";
                case RESOURCES_BONUS:
                    return "3% höhere Rohstoffproduktion";
                default:
                    return "";
            }
        } else {
            switch (pVillage.getType()) {
                case WOOD_BONUS:
                    return "100% mehr Holzproduktion";
                case CLAY_BONUS:
                    return "100% mehr Lehmproduktion";
                case IRON_BONUS:
                    return "100% mehr Eisenproduktion";
                case FARM_BONUS:
                    return "10% mehr Bevölkerung";
                case BARRACKS_BONUS:
                    return "33% schnellere Produktion in der Kaserne";
                case STABLE_BONUS:
                    return "33% schnellere Produktion im Stall";
                case WORKSHOP_BONUS:
                    return "50% schnellere Produktion in der Werkstatt";
                case RESOURCES_BONUS:
                    return "30% höhere Rohstoffproduktion";
                case STORAGE_BONUS:
                    return "50% mehr Speicherkapazität und Händler";
                default:
                    return "";
            }
        }
    }

    private static class CaseInsensitiveComparator implements Comparator<Village>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Village s1, Village s2) {

            /*
             * if (Village.getOrderType() == ORDER_ALPHABETICALLY) { int n1 = s1.toString().length(), n2 = s2.toString().length(); for (int
             * i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) { char c1 = s1.toString().charAt(i1); char c2 = s2.toString().charAt(i2); if
             * (c1 != c2) { c1 = Character.toUpperCase(c1); c2 = Character.toUpperCase(c2); if (c1 != c2) { c1 = Character.toLowerCase(c1);
             * c2 = Character.toLowerCase(c2); if (c1 != c2) { returcompareTon c1 - c2; } } } } return n1 - n2; } else {
             */
            return s1.compareTo(s2);
            // }
        }
    }

    private static class IntuitiveStringComparator<T extends CharSequence> implements Comparator<T> {

        private T str1, str2;
        private int pos1, pos2, len1, len2;

        public int compare(T s1, T s2) {
            str1 = s1;
            str2 = s2;
            len1 = str1.length();
            len2 = str2.length();
            pos1 = pos2 = 0;

            if (len1 == 0) {
                return len2 == 0 ? 0 : -1;
            } else if (len2 == 0) {
                return 1;
            }

            while (pos1 < len1 && pos2 < len2) {
                char ch1 = str1.charAt(pos1);
                char ch2 = str2.charAt(pos2);
                int result = 0;

                if (Character.isDigit(ch1)) {
                    result = Character.isDigit(ch2) ? compareNumbers() : -1;
                } else if (Character.isLetter(ch1)) {
                    result = Character.isLetter(ch2) ? compareOther(true) : 1;
                } else {
                    result = Character.isDigit(ch2) ? 1
                            : Character.isLetter(ch2) ? -1
                            : compareOther(false);
                }

                if (result != 0) {
                    return result;
                }
            }

            return len1 - len2;
        }

        private int compareNumbers() {
            int delta = 0;
            int zeroes1 = 0, zeroes2 = 0;
            char ch1 = (char) 0, ch2 = (char) 0;

            // Skip leading zeroes, but keep a count of them.  
            while (pos1 < len1 && (ch1 = str1.charAt(pos1++)) == '0') {
                zeroes1++;
            }
            while (pos2 < len2 && (ch2 = str2.charAt(pos2++)) == '0') {
                zeroes2++;
            }

            // If one sequence contains more significant digits than the  
            // other, it's a larger number.  In case they turn out to have  
            // equal lengths, we compare digits at each position; the first  
            // unequal pair determines which is the bigger number.  
            while (true) {
                boolean noMoreDigits1 = (ch1 == 0) || !Character.isDigit(ch1);
                boolean noMoreDigits2 = (ch2 == 0) || !Character.isDigit(ch2);

                if (noMoreDigits1 && noMoreDigits2) {
                    return delta != 0 ? delta : zeroes1 - zeroes2;
                } else if (noMoreDigits1) {
                    return -1;
                } else if (noMoreDigits2) {
                    return 1;
                } else if (delta == 0 && ch1 != ch2) {
                    delta = ch1 - ch2;
                }

                ch1 = pos1 < len1 ? str1.charAt(pos1++) : (char) 0;
                ch2 = pos2 < len2 ? str2.charAt(pos2++) : (char) 0;
            }
        }

        private int compareOther(boolean isLetters) {
            char ch1 = str1.charAt(pos1++);
            char ch2 = str2.charAt(pos2++);

            if (ch1 == ch2) {
                return 0;
            }

            if (isLetters) {
                ch1 = Character.toUpperCase(ch1);
                ch2 = Character.toUpperCase(ch2);
                if (ch1 != ch2) {
                    ch1 = Character.toLowerCase(ch1);
                    ch2 = Character.toLowerCase(ch2);
                }
            }

            return ch1 - ch2;
        }
    }

    private static class AllyTribeVillageComparator implements Comparator<Village>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Village s1, Village s2) {

            Tribe t1 = s1.getTribe();
            Tribe t2 = s2.getTribe();

            if (t1 == null) {
                t1 = Barbarians.getSingleton();
            }

            if (t2 == null) {
                t2 = Barbarians.getSingleton();
            }
            Ally a1 = t1.getAlly();
            Ally a2 = t2.getAlly();
            if (a1 == null) {
                a1 = NoAlly.getSingleton();
            }
            if (a2 == null) {
                a2 = NoAlly.getSingleton();
            }

            int result = Ally.CASE_INSENSITIVE_ORDER.compare(a1, a2);

            if (result == 0) {
                result = Tribe.CASE_INSENSITIVE_ORDER.compare(t1, t2);
                if (result == 0) {
                    return Village.CASE_INSENSITIVE_ORDER.compare(s1, s2);
                } else {
                    return result;
                }
            } else {
                return result;
            }
            /*
             * if (Village.getOrderType() == ORDER_ALPHABETICALLY) { int n1 = s1.toString().length(), n2 = s2.toString().length(); for (int
             * i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) { char c1 = s1.toString().charAt(i1); char c2 = s2.toString().charAt(i2); if
             * (c1 != c2) { c1 = Character.toUpperCase(c1); c2 = Character.toUpperCase(c2); if (c1 != c2) { c1 = Character.toLowerCase(c1);
             * c2 = Character.toLowerCase(c2); if (c1 != c2) { return c1 - c2; } } } } return n1 - n2; } else { return s1.compareTo(s2); }
             */
        }
    }
}
