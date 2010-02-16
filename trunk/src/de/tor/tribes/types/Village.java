/*
 * Village.java
 *
 * Created on 18.07.2007, 18:58:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.html.VillageHTMLTooltipGenerator;
import de.tor.tribes.util.tag.TagManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
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
public class Village implements Comparable, Serializable {

    public static final Comparator<Village> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();
    public static final Comparator<Village> ALLY_TRIBE_VILLAGE_COMPARATOR = new AllyTribeVillageComparator();
    public final static int ORDER_ALPHABETICALLY = 0;
    public final static int ORDER_BY_COORDINATES = 1;
    private static int orderType = ORDER_ALPHABETICALLY;
    private int id;
    private String name = null;
    private short x;
    private short y;
    private int tribeID;
    private Tribe tribe = null;
    private int points;
    private byte type;
    private String stringRepresentation = null;
    private boolean visibleOnMap = true;
    private int continent = -1;
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
            n = n.replaceAll("&quot;", "\"").replaceAll("&amp;", "&");
            n = n.replaceAll("&tilde;", "~");
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
        String coord = "";
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] hier = DSCalculator.xyToHierarchical((int) getX(), (int) getY());
            coord = "(" + hier[0] + ":" + hier[1] + ":" + hier[2] + ")";
        } else {
            coord = "(" + getX() + "|" + getY() + ")";
        }
        return coord;
    }

    public String getFullName() {
        String res = getName();
        res += " " + getCoordAsString();
        int cont = getContinent();
        if (cont < 10) {
            res += " K0" + cont;
        } else {
            res += " K" + cont;
        }
        return res;
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

    public int getContinent() {
        if (continent == -1) {
            if (ServerSettings.getSingleton().getCoordType() != 2) {
                continent = DSCalculator.xyToHierarchical(getX(), getY())[0];
            } else {
                continent = DSCalculator.getContinent(getX(), getY());
            }
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
        return tribe;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    public String getHTMLInfo() {
        StringBuffer b = new StringBuffer();
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            b.append("<html><p><b>Name (C:S:S):</b> ");
            b.append(getName().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
            b.append(" (");
            int[] hier = DSCalculator.xyToHierarchical(getX(), getY());
            b.append(hier[0]);
            b.append(":");
            b.append(hier[1]);
            b.append(":");
            b.append(hier[2]);
        } else {
            b.append("<html><p><b>Name (X|Y):</b> ");
            b.append(getName().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
            b.append(" (");
            b.append(getX());
            b.append("|");
            b.append(getY());
        }
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
        int bonusType = DataHolder.getSingleton().getCurrentBonusType();
        if (bonusType == 0) {
            switch (getType()) {
                case 1: {
                    //holz
                    b.append("+ 10% </p></html>");
                    break;
                }
                case 2: {
                    //lehm
                    b.append("+ 10% </html>");
                    break;
                }
                case 3: {
                    //eisen
                    b.append("+ 10% </p></html>");
                    break;
                }
                case 4: {
                    //bevölkerung
                    b.append("+ 10% </p></html>");
                    break;
                }
                case 5: {
                    //kaserne
                    b.append("+ 10% </p></html>");
                    break;
                }
                case 6: {
                    //stall
                    b.append("+ 10% </p></html>");
                    break;
                }
                case 7: {
                    //werkstatt
                    b.append("+ 10% </p></html>");
                    break;
                }
                case 8: {
                    //alle ressourcen
                    b.append("+ 3% </p></html>");
                    break;
                }
            }
        } else {
            switch (getType()) {
                case 1: {
                    //holz
                    b.append("+ 100% </p></html>");
                    break;
                }
                case 2: {
                    //lehm
                    b.append("+ 100% </html>");
                    break;
                }
                case 3: {
                    //eisen
                    b.append("+ 100% </p></html>");
                    break;
                }
                case 4: {
                    //bevölkerung
                    b.append("+ 10% </p></html>");
                    break;
                }
                case 5: {
                    //kaserne
                    b.append("+ 50% </p></html>");
                    break;
                }
                case 6: {
                    //stall
                    b.append("+ 50% </p></html>");
                    break;
                }
                case 7: {
                    //werkstatt
                    b.append("+ 100% </p></html>");
                    break;
                }
                case 8: {
                    //alle rohstoffe
                    b.append("+ 30% </p></html>");
                    break;
                }
                case 9: {
                    //speicher + markt
                    b.append("+ 50% </p></html>");
                    break;
                }
            }
        }

        return b.toString();
    }

    public Rectangle2D.Double getVirtualBounds() {
        int w = GlobalOptions.getSkin().getBasicFieldHeight();
        int h = GlobalOptions.getSkin().getBasicFieldHeight();
        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        return new Rectangle2D.Double(getX(), getY(), (double) w / z, (double) h / z);
    }

    @Override
    public String toString() {
        if (stringRepresentation == null) {
            if (ServerSettings.getSingleton().getCoordType() != 2) {
                int[] hier = DSCalculator.xyToHierarchical(getX(), getY());
                stringRepresentation = getName() + " (" + ((hier[0] < 10) ? "0" + hier[0] : hier[0]) + ":" + ((hier[1] < 10) ? "0" + hier[1] : hier[1]) + ":" + ((hier[2] < 10) ? "0" + hier[2] : hier[2]) + ")";
            } else {
                stringRepresentation = getName() + " (" + getX() + "|" + getY() + ")";
            }
        }
        return stringRepresentation;
    }

    public String getToolTipText() {
        return VillageHTMLTooltipGenerator.buildToolTip(this, false);
    }

    public String getExtendedTooltip() {
        return VillageHTMLTooltipGenerator.buildToolTip(this);
    }

    public String toBBCode() {
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] hier = DSCalculator.xyToHierarchical(getX(), getY());
            return "[village]" + hier[0] + ":" + hier[1] + ":" + hier[2] + "[/village]";
        }
        return "[village]" + getX() + "|" + getY() + "[/village]";
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

                if (getX() < v.getX()) {
                    return -1;
                } else if (getX() > v.getX()) {
                    return 1;
                } else {
                    if (getY() < v.getY()) {
                        return -1;
                    } else if (getY() > v.getY()) {
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

    private static class CaseInsensitiveComparator implements Comparator<Village>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
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
            /* if (Village.getOrderType() == ORDER_ALPHABETICALLY) {
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
            }*/
        }
    }
}
