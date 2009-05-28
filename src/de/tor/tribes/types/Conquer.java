/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.ServerManager;
import de.tor.tribes.util.GlobalOptions;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Conquer {

    private int villageID = 0;
    private int timestamp = 0;
    private int loser = 0;
    private int winner = 0;

    public static Conquer fromXml(Element pElement) throws Exception {

        int villageId = Integer.parseInt(pElement.getChild("villageID").getText());
        int timestamp = Integer.parseInt(pElement.getChild("timestamp").getText());
        int winner = Integer.parseInt(pElement.getChild("winner").getText());
        int loser = Integer.parseInt(pElement.getChild("loser").getText());
        Conquer c = new Conquer();
        c.setVillageID(villageId);
        c.setTimestamp(timestamp);
        c.setLoser(loser);
        c.setWinner(winner);
        return c;
    }

    public String toXml() {
        String result = "<conquer>\n";
        result += "<villageID>" + getVillageID() + "</villageID>\n";
        result += "<timestamp>" + getTimestamp() + "</timestamp>\n";
        result += "<winner>" + getWinner() + "</winner>\n";
        result += "<loser>" + getLoser() + "</loser>\n";
        result += "</conquer>";
        return result;
    }

    public int getCurrentAcceptance() {
        long time = getTimestamp();
        long diff = System.currentTimeMillis() / 1000 - time;
        double risePerHour = 1.0;
        try {
            risePerHour = ServerManager.getServerAcceptanceRiseSpeed(GlobalOptions.getSelectedServer());
        } catch (Exception e) {
        }
        int rise = 25 + (int) Math.rint((diff / (60 * 60)) * risePerHour);
        return rise;
    }

    /**
     * @return the villageID
     */
    public int getVillageID() {
        return villageID;
    }

    /**
     * @param villageID the villageID to set
     */
    public void setVillageID(int villageID) {
        this.villageID = villageID;
    }

    /**
     * @return the timestamp
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the loser
     */
    public int getLoser() {
        return loser;
    }

    /**
     * @param loser the loser to set
     */
    public void setLoser(int loser) {
        this.loser = loser;
    }

    /**
     * @return the winner
     */
    public int getWinner() {
        return winner;
    }

    /**
     * @param winner the winner to set
     */
    public void setWinner(int winner) {
        this.winner = winner;
    }
}
