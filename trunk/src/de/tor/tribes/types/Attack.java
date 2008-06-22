/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.types;

import de.tor.tribes.io.UnitHolder;
import java.util.Date;

/**
 *
 * @author Charon
 */
public class Attack {

    private Village source = null;
    private Village target = null;
    private UnitHolder unit = null;
    private Date arriveTime = null;

    public boolean isSourceVillage(Village pVillage){
        return (pVillage == source);
    }
    
    public boolean isTargetVillage(Village pVillage){
        return (pVillage == target);
    }
    
    public Village getSource() {
        return source;
    }

    public void setSource(Village source) {
        this.source = source;
    }

    public Village getTarget() {
        return target;
    }

    public void setTarget(Village target) {
        this.target = target;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    public Date getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Date arriveTime) {
        this.arriveTime = arriveTime;
    }
}
