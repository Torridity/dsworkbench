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
import java.util.List;

/**
 *
 * @author Charon
 */
public class Village implements Serializable {

    private static final long serialVersionUID = 10L;
    private int id = 0;
    private String name = null;
    private short x = 0;
    private short y = 0;
    private int tribeID = 0;
    private transient Tribe tribe = null;
    private int points = 0;
    private int rank = 0;
    private byte type = 0;

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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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
            for(String tag : tags){
                villageInfo += tag + "|";
            }
            villageInfo = villageInfo.substring(0, villageInfo.length()-1);
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
}
