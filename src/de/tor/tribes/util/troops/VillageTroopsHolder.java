/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
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
    private int iOffPower = -1;
    private int iDefPower = -1;
    private int iArchDefPower = -1;
    private int iCavDefPower = -1;

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

    public int getTroopsOfUnit(UnitHolder pUnit) {
        int cnt = 0;
        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            if (u.equals(pUnit)) {
                return getTroops().get(cnt);
            }
            cnt++;
        }
        return 0;
    }

    public void setTroops(List<Integer> mTroops) {
        iOffPower = -1;
        iDefPower = -1;
        iArchDefPower = -1;
        iCavDefPower = -1;
        initTroopsPowers();
        this.troops = mTroops;
    }

    public Date getState() {
        return state;
    }

    public void setState(Date mState) {
        this.state = mState;
    }

    public double getOffValue() {
        if (iOffPower < 0) {
            initTroopsPowers();
        }
        return iOffPower;
    }

    public double getDefValue() {
        if (iOffPower < 0) {
            initTroopsPowers();
        }
        return iDefPower;
    }

    public double getDefArcherValue() {
        if (iOffPower < 0) {
            initTroopsPowers();
        }
        return iArchDefPower;
    }

    public double getDefCavalryValue() {
        if (iOffPower < 0) {
            initTroopsPowers();
        }
        return iCavDefPower;
    }

    private void initTroopsPowers() {
        if ((iOffPower < 0) || (iDefPower < 0) || (iArchDefPower < 0) || (iCavDefPower < 0)) {
            try {
                for (int i = 0; i < troops.size(); i++) {
                    iOffPower += troops.get(i) * DataHolder.getSingleton().getUnits().get(i).getAttack();
                    iDefPower += troops.get(i) * DataHolder.getSingleton().getUnits().get(i).getDefense();
                    iArchDefPower += troops.get(i) * DataHolder.getSingleton().getUnits().get(i).getDefenseArcher();
                    iCavDefPower += troops.get(i) * DataHolder.getSingleton().getUnits().get(i).getDefenseCavalry();
                }
                iOffPower = (iOffPower < 0) ? 0 : iOffPower;
                iDefPower = (iDefPower < 0) ? 0 : iDefPower;
                iArchDefPower = (iArchDefPower < 0) ? 0 : iArchDefPower;
                iCavDefPower = (iCavDefPower < 0) ? 0 : iCavDefPower;
            } catch (Exception e) {
                //units not loaded yet
            }
        }
    }
}
