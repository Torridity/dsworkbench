/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.util.GlobalOptions;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Conquer extends ManageableType {

    private Village village = null;
    private long timestamp = 0;
    private Tribe loser = null;
    private Tribe winner = null;

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
    public Village getVillage() {
        return village;
    }

    /**
     * @param villageID the villageID to set
     */
    public void setVillage(Village pVillage) {
        this.village = pVillage;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the loser
     */
    public Tribe getLoser() {
        return loser;
    }

    /**
     * @param loser the loser to set
     */
    public void setLoser(Tribe loser) {
        this.loser = loser;
    }

    /**
     * @return the winner
     */
    public Tribe getWinner() {
        return winner;
    }

    /**
     * @param winner the winner to set
     */
    public void setWinner(Tribe winner) {
        this.winner = winner;
    }

    @Override
    public String getElementIdentifier() {
        return "conquer";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "conquers";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }

    @Override
    public String toXml() {
        try {
            String result = "<conquer>\n";
            result += "<villageID>" + getVillage().getId() + "</villageID>\n";
            result += "<timestamp>" + getTimestamp() + "</timestamp>\n";
            result += "<winner>" + getWinner().getId() + "</winner>\n";
            result += "<loser>" + getLoser().getId() + "</loser>\n";
            result += "</conquer>";
            return result;
        } catch (Throwable t) {
            //getting xml data failed
        }
        return null;
    }

    @Override
    public void loadFromXml(Element pElement) {
        int villageId = Integer.parseInt(pElement.getChild("villageID").getText());
        int timestamp = Integer.parseInt(pElement.getChild("timestamp").getText());
        int winner = Integer.parseInt(pElement.getChild("winner").getText());
        int loser = Integer.parseInt(pElement.getChild("loser").getText());
        setVillage(DataHolder.getSingleton().getVillagesById().get(villageId));
        setTimestamp(timestamp);
        setLoser(DataHolder.getSingleton().getTribes().get(loser));
        setWinner(DataHolder.getSingleton().getTribes().get(winner));
    }
}
