/*
 * Village.java
 *
 * Created on 18.07.2007, 18:58:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.tag.TagManager;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Charon
 */
public class Village implements Serializable, Comparable {

    public final static int ORDER_ALPHABETICALLY = 0;
    public final static int ORDER_BY_COORDINATES = 1;
    private static final long serialVersionUID = 10L;
    private static int orderType = ORDER_ALPHABETICALLY;
    private int id;
    private String name = null;
    private short x;
    private short y;
    private int tribeID;
    private transient Tribe tribe = null;
    private int points;
    private byte type;
    private String stringRepresentation = null;
    //$id, $name, $x, $y, $tribe, $points, $type

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
            n = n.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
            entry.setName(n);
            entry.setX(Short.parseShort(tokenizer.nextToken()));
            entry.setY(Short.parseShort(tokenizer.nextToken()));
            entry.setTribeID(Integer.parseInt(tokenizer.nextToken()));
            entry.setPoints(Integer.parseInt(tokenizer.nextToken()));
            entry.setType(Byte.parseByte(tokenizer.nextToken()));
            return entry;
        } catch (Exception e) {
            //village invalid
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
        b.append(getX());
        b.append(",");
        b.append(getY());
        b.append(",");
        b.append(getTribeID());
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

    public void updateFromDiff(String pDiff) {
        StringTokenizer t = new StringTokenizer(pDiff, ",");
        //skip id
        t.nextToken();
        try {
            String n = URLDecoder.decode(t.nextToken().trim(), "UTF-8");
            //replace HTML characters
            n = n.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
            setName(n);
        } catch (Exception e) {
        }
        //skip x 
        t.nextToken();
        //skip y
        t.nextToken();
        try {
            setTribeID(Integer.parseInt(t.nextToken().trim()));
        } catch (Exception e) {
        }
        try {
            setPoints(Integer.parseInt(t.nextToken().trim()));
        } catch (Exception e) {
        }
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
        stringRepresentation = null;
    }

    public short getX() {
        return x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
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
        return tribe;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    public String getHTMLInfo() {
        StringBuffer b = new StringBuffer();
        b.append("<html><p><b>Name (X|Y):</b> ");
        b.append(getName().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        b.append(" (");
        b.append(getX());
        b.append("|");
        b.append(getY());
        b.append("), <b>Punkte:</b> ");
        b.append(getPoints());
        b.append(",");
        List<Tag> tags = TagManager.getSingleton().getTags(this);
        b.append("<b>Tags:</b> ");
        if ((tags == null) || (tags.size() == 0)) {
            b.append("keine, ");
        } else {
            for (int i = 0; i < tags.size(); i++) {
                b.append(tags.get(i));
                if (i < tags.size() - 1) {
                    b.append("|");
                } else {
                    b.append(",");
                }
            }
        }
        b.append("<b>Bonus:</b> ");
        switch (getType()) {
            case 1:
                b.append("+ 10% </p></html>");
                break;

            case 2:
                b.append("+ 10% </html>");
                break;

            case 3:
                b.append("+ 10% </p></html>");
                break;

            case 4:
                b.append("+ 10% </p></html>");
                break;

            case 5:
                b.append("+ 10% </p></html>");
                break;

            case 6:
                b.append("+ 10% </p></html>");
                break;

            case 7:
                b.append("+ 10% </p></html>");
                break;

            case 8:
                b.append("+ 3% </p></html>");
                break;

        }
        return b.toString();
    }

    @Override
    public String toString() {
        if (stringRepresentation == null) {
            stringRepresentation = getName() + " (" + getX() + "|" + getY() + ")";
        }
        return stringRepresentation;
    }

    public String toBBCode() {
        return "[village]" + getX() + "|" + getY() + "[/village]";
    }

    /**Create diff between upto-date village and an older version of this village*/
    public String createDiff(Village old) {
        String diff = null;
        if (old == null) {
            diff = getId() + "," + getName() + "," + getX() + "," + getY() + "," + getTribeID() + "," + getPoints() + "," + getType() + "\n";
            return diff;
        }
        boolean nameChange = false;
        boolean tribeChange = false;
        boolean pointsChange = false;

        if (!getName().equals(old.getName())) {
            nameChange = true;
        }

        if (getTribeID() != old.getTribeID()) {
            tribeChange = true;
        }

        if (getPoints() != old.getPoints()) {
            pointsChange = true;
        }

        if (nameChange || tribeChange || pointsChange) {
            diff = Integer.toString(getId()) + ",";
            if (nameChange) {
                try {
                    diff += URLEncoder.encode(getName(), "UTF-8") + ",";
                } catch (Exception e) {
                }
            } else {
                diff += " ,";
            }
            //add coord placeholders
            diff += " , ,";

            if (tribeChange) {
                diff += getTribeID() + ",";
            } else {
                diff += " ,";
            }

            if (pointsChange) {
                diff += getPoints() + ",";
            } else {
                diff += " ,";
            }
            //add type placeholder
            diff += " \n";
        }

        return diff;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        //switch order type
        if (orderType == ORDER_ALPHABETICALLY) {
            return toString().toLowerCase().compareTo(o.toString().toLowerCase());
        } else {
            try {
                Village v = (Village) o;

                if (getX() > v.getX()) {
                    return -1;
                } else if (getX() < v.getX()) {
                    return 1;
                } else {
                    if (getY() > v.getY()) {
                        return -1;
                    } else if (getY() < v.getY()) {
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
    public static final Comparator<Village> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

    private static class CaseInsensitiveComparator implements Comparator<Village>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        public int compare(Village s1, Village s2) {
            if (Village.getOrderType() == ORDER_ALPHABETICALLY) {
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
            } else {
                return s1.compareTo(s2);
            }
        }
    }
}
