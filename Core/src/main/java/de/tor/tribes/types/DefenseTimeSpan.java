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
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.LongRange;

/**
 *
 * @author Charon
 */
public class DefenseTimeSpan extends TimeSpan {

    @Override
    public DefenseTimeSpan clone() throws CloneNotSupportedException {
        if (getDirection().equals(DIRECTION.NONE)) {
            throw new CloneNotSupportedException("Divider cannot be cloned");
        }
        DefenseTimeSpan s = new DefenseTimeSpan(validFor, span);
        s.setDirection(getDirection());
        return s;
    }
    private Village validFor = null;
    private LongRange span = null;

    public DefenseTimeSpan() {
    }

    public DefenseTimeSpan(Village pVillage, LongRange pSpan) {
        validFor = pVillage;
        span = pSpan;
    }

    @Override
    public IntRange getSpan() {
        return null;
    }

    @Override
    public int compareTo(TimeSpan o) {
        if (getDirection().equals(DefenseTimeSpan.DIRECTION.SEND) && o.getDirection().equals(DefenseTimeSpan.DIRECTION.NONE)) {
            return -1;
        } else if (getDirection().equals(DefenseTimeSpan.DIRECTION.ARRIVE) && o.getDirection().equals(DefenseTimeSpan.DIRECTION.NONE)) {
            return 1;
        } else if (getDirection().equals(DefenseTimeSpan.DIRECTION.SEND) && o.getDirection().equals(DefenseTimeSpan.DIRECTION.ARRIVE)) {
            return -1;
        } else if (getDirection().equals(DefenseTimeSpan.DIRECTION.ARRIVE) && o.getDirection().equals(DefenseTimeSpan.DIRECTION.SEND)) {
            return 1;
        } else if (getDirection().equals(o.getDirection())) {
            if (getAtDate() != null && o.getAtDate() == null) {
                return -1;
            } else if (getAtDate() == null && o.getAtDate() != null) {
                return 1;
            } else if (getAtDate() == null && o.getAtDate() == null) {
                //return new Integer(getSpan().getMinimumInteger()).compareTo(o.getSpan().getMinimumInteger());
                return 0;
            }
        }
        return 0;
    }

    @Override
    public boolean isValidAtExactTime() {
        return false;
    }

    @Override
    public DIRECTION getDirection() {
        return TimeSpan.DIRECTION.ARRIVE;
    }

    @Override
    public boolean isValidAtEveryDay() {
        return false;
    }

    @Override
    public boolean isValidAtSpecificDay() {
        return false;
    }

    @Override
    public boolean isValid() {
        return (span.getMaximumLong() > System.currentTimeMillis());
    }

    @Override
    public String getValidityInfo() {
        if (!isValid()) {
            return "Ankunft in der Vergangenheit";
        }

        //date/frame is valid or we use each day
        return null;
    }

    @Override
    public boolean intersectsWithNightBonus() {
        return false;
    }

    public LongRange getDefenseSpan() {
        return span;
    }

    public void setDefenseSpan(LongRange pSpan) {
        span = pSpan;
    }

    public boolean isValidForVillage(Village pVillage) {
        return validFor.equals(pVillage);
    }

    @Override
    public boolean intersects(TimeSpan pSpan) {
        return false;
    }

    public boolean intersects(DefenseTimeSpan pSpan) {
        return false;
    }

    public static DefenseTimeSpan fromPropertyString(String pString) {
        return null;
    }

    @Override
    public String toPropertyString() {
        return "";
    }

    @Override
    public String toString() {
        String result = "Verteidigung " + validFor + " vom ";
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
        result += f.format(new Date(span.getMinimumLong())) + " bis " + f.format(new Date(span.getMaximumLong()));
        return result;
    }
}
