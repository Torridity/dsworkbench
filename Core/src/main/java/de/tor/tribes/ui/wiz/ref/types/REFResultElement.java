/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.ref.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import java.util.Date;

/**
 *
 * @author Torridity
 */
public class REFResultElement {

    private Village source = null;
    private UnitHolder unit = null;
    private Village target = null;
    private Date arriveTime = null;

    public REFResultElement(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime) {
        setSource(pSource);
        setTarget(pTarget);
        setUnit(pUnit);
        setArriveTime(pArriveTime);
    }

    public Attack asAttack() {
        Attack a = new Attack();
        a.setSource(getSource());
        a.setTarget(getTarget());
        a.setUnit(getUnit());
        a.setArriveTime(arriveTime);
        a.setType(Attack.SUPPORT_TYPE);
        return a;
    }

    public void setSource(Village source) {
        this.source = source;
    }

    public Village getSource() {
        return source;
    }

    public void setTarget(Village target) {
        this.target = target;
    }

    public Village getTarget() {
        return target;
    }

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public void setArriveTime(Date arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Date getArriveTime() {
        return arriveTime;
    }

    public Date getSendTime() {
        return new Date(getArriveTime().getTime() - DSCalculator.calculateMoveTimeInMillis(source, target, unit.getSpeed()));
    }
}
