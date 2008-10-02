/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.types.Village;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class VillageTroopsHolder {

    private Village village = null;
    private List<Integer> troops = null;
    private Date state = null;

    public VillageTroopsHolder(Village pVillage, List<Integer> pTroops, Date pState) {
        setVillage(pVillage);
        setTroops(pTroops);
        setState(pState);
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village mVillage) {
        this.village = mVillage;
    }

    public List<Integer> getTroops() {
        return troops;
    }

    public void setTroops(List<Integer> mTroops) {
        this.troops = mTroops;
    }

    public Date getState() {
        return state;
    }

    public void setState(Date mState) {
        this.state = mState;
    }
}
