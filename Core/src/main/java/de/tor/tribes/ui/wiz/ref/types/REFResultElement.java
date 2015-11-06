/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
