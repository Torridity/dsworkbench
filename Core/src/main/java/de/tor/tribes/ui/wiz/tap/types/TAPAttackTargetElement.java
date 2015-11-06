/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.tap.types;

import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class TAPAttackTargetElement {

    private Village village = null;
    private int attacks = 1;
    private boolean fake = false;
    private boolean ignored = false;

    public TAPAttackTargetElement(Village pVillage) {
        this(pVillage, false, 1);
    }

    public TAPAttackTargetElement(Village pVillage, boolean pFake) {
        this(pVillage, pFake, 1);
    }

    public TAPAttackTargetElement(Village pVillage, boolean pFake, int pAmount) {
        village = pVillage;
        fake = pFake;
        attacks = pAmount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TAPAttackTargetElement) {
            return ((TAPAttackTargetElement) obj).getVillage().equals(getVillage());
        }
        return false;
    }

    public Village getVillage() {
        return village;
    }

    public int getAttacks() {
        return attacks;
    }

    public void addAttack() {
        attacks++;
    }

    public boolean removeAttack() {
        attacks--;
        boolean modified = (attacks <= 0) ? false : true;
        attacks = Math.max(1, attacks);
        return modified;
    }

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean pValue) {
        fake = pValue;
    }

    public void setAttacks(int attacks) {
        this.attacks = attacks;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }
    
    
}
