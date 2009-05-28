/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.db;

/**
 *
 * @author Jejkal
 */
public class DatabaseServerEntry {

    private String serverID = null;
    private String serverURL = null;
    private double acceptanceRiseSpeed = 1.0;
    private int dataVersion = 0;

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
     *@DEPRECATED
     */
    public int getDataVersion() {
        return dataVersion;
    }

    /**
     *@DEPRECATED
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
}
