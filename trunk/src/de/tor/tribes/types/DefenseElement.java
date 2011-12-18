/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.util.Date;

/**
 *
 * @author Torridity
 */
public class DefenseElement {

    public enum DEFENSE_STATUS {

        UNKNOWN, DANGEROUS, FINE, SAVE
    };
    private Village target = null;
    private SOSRequest.TargetInformation targetInfo = null;
    private DEFENSE_STATUS status = DEFENSE_STATUS.UNKNOWN;
    private double lossRatio = 0.0;
    private int neededSupports = 0;
    private int cleanAfter = 0;

    public SOSRequest.TargetInformation getTargetInformation() {
        return targetInfo;
    }

    public void setTargetInformation(SOSRequest.TargetInformation pInfo) {
        targetInfo = pInfo;
    }

    public void setDefenseStatus(DEFENSE_STATUS pStatus) {
        status = pStatus;
    }

    public DEFENSE_STATUS getStatus() {
        return status;
    }

    public double getLossRatio() {
        return lossRatio;
    }

    public void setLossRation(double pValue) {
        lossRatio = pValue;
    }

    public void setCleanAfter(int pValue) {
        cleanAfter = pValue;
    }

    public int getCleanAfter() {
        return cleanAfter;
    }

    public void setTarget(Village pTarget) {
        target = pTarget;
    }

    public Village getTarget() {
        return target;
    }

    public int getWallLevel() {
        return targetInfo.getWallLevel();
    }

    public int getAttackCount() {
        return targetInfo.getOffs();
    }

    public int getFakeCount() {
        return targetInfo.getFakes();
    }

    public Date getFirstAttack() {
        return new Date(targetInfo.getFirstAttack());
    }

    public Date getLastAttack() {
        return new Date(targetInfo.getLastAttack());
    }

    public int getDelta() {
        return targetInfo.getDelta();
    }

    public int getNeededSupports() {
        return neededSupports;
    }

    public void setNeededSupports(int pValue) {
        neededSupports = pValue;
    }
}
