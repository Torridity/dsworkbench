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
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Charon
 */
public class Village implements Serializable {

    private static final long serialVersionUID = 10L;
    private int id;
    private String name = null;
    private short x;
    private short y;
    private int tribeID;
    private transient Tribe tribe = null;
    private int points;
    private byte type;

    public static Village parseFromPlainData(String pLine) {
        StringTokenizer tokenizer = new StringTokenizer(pLine, ",");

        Village entry = new Village();
        if (tokenizer.countTokens() < 7) {
            return null;
        }

        try {
            entry.setId(Integer.parseInt(tokenizer.nextToken()));
            String name = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
            //replace HTML characters
            name = name.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
            entry.setName(name);
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
        String villageInfo = "<html><b>Name (X|Y):</b> " + getName() + " (" + getX() + "|" + getY() + "), <b>Punkte:</b> " + getPoints() + ",";
        List<String> tags = GlobalOptions.getTags(this);
        villageInfo += "<b>Tags:</b> ";
        if (tags == null) {
            villageInfo += "keine, ";
        } else {
            for (String tag : tags) {
                villageInfo += tag + "|";
            }
            villageInfo = villageInfo.substring(0, villageInfo.length() - 1);
            villageInfo += ", ";
        }
        villageInfo += "<b>Bonus:</b> ";
        switch (getType()) {
            case 1:
                villageInfo += "+ 10% </html>";
                break;

            case 2:
                villageInfo += "+ 10% </html>";
                break;

            case 3:
                villageInfo += "+ 10% </html>";
                break;

            case 4:
                villageInfo += "+ 10% </html>";
                break;

            case 5:
                villageInfo += "+ 10% </html>";
                break;

            case 6:
                villageInfo += "+ 10% </html>";
                break;

            case 7:
                villageInfo += "+ 10% </html>";
                break;

            case 8:
                villageInfo += "+ 3% </html>";
                break;

        }
        return villageInfo;
    }

    @Override
    public String toString() {
        return getX() + "|" + getY() + " " + getName();
    }

    public String toBBCode() {
        return "[village](" + getX() + "|" + getY() + ")[/village]";
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
                diff += getName() + ",";
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
            diff += " ";
        }

        return diff;
    }
}
