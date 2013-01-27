/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.db;

/**
 *
 * @author Jejkal
 */
public class DatabaseServerEntry implements Comparable<DatabaseServerEntry> {

    public final static byte NO_NIGHT_BONUS = 0;
    public final static byte NIGHT_BONUS_0to7 = 1;
    public final static byte NIGHT_BONUS_0to8 = 2;
    public final static byte NIGHT_BONUS_1to7 = 3;
    private String serverID = null;
    private String serverURL = null;
    private double acceptanceRiseSpeed = 1.0;
    private byte nightBonus = NIGHT_BONUS_0to8;
    private int dataVersion = 0;
    private int decoration = 0;

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    /**
     * @DEPRECATED
     */
    public int getDataVersion() {
        return dataVersion;
    }

    /**
     * @DEPRECATED
     */
    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    /**
     * @return the acceptanceRiseSpeed
     */
    public double getAcceptanceRiseSpeed() {
        return acceptanceRiseSpeed;
    }

    /**
     * @param acceptanceRiseSpeed the acceptanceRiseSpeed to set
     */
    public void setAcceptanceRiseSpeed(double acceptanceRiseSpeed) {
        this.acceptanceRiseSpeed = acceptanceRiseSpeed;
    }

    /**
     * @return the nightBonus
     */
    public byte getNightBonus() {
        return nightBonus;
    }

    /**
     * @param nightBonus the nightBonus to set
     */
    public void setNightBonus(byte nightBonus) {
        this.nightBonus = nightBonus;
    }

    /**
     * @return the decoration
     */
    public int getDecoration() {
        return decoration;
    }

    /**
     * @param decoration the decoration to set
     */
    public void setDecoration(int decoration) {
        this.decoration = decoration;
    }

    public String toString() {
        return getServerID();
    }

    @Override
    public int compareTo(DatabaseServerEntry o) {
        String o1 = getServerID();
        String o2 = o.getServerID();
        if (o1.length() < o2.length()) {
            return -1;
        } else if (o1.length() > o2.length()) {
            return 1;
        }
        return o1.compareTo(o2);
    }
}
