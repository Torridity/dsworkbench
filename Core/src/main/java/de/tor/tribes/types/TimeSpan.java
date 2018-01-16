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

import de.tor.tribes.util.ServerSettings;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Charon
 */
public class TimeSpan implements Comparable<TimeSpan> {
  @Override
  public TimeSpan clone() throws CloneNotSupportedException {
    if (getDirection().equals(DIRECTION.NONE)) {
      throw new CloneNotSupportedException("Divider cannot be cloned");
    }
    TimeSpan s = new TimeSpan();
    s.init(exactSpan, daily);
    s.setDirection(getDirection());
    return s;
  }
  private boolean daily = false;
  private LongRange exactSpan = null;
  private DIRECTION direction = DIRECTION.SEND;

  public TimeSpan() {
  }

  public TimeSpan(IntRange pSpan) { //every Day
    if(pSpan.getMinimumInteger() == pSpan.getMaximumInteger())
        throw new RuntimeException("Span without size not allowed");
    LongRange asLong = new LongRange(pSpan.getMinimumLong() * DateUtils.MILLIS_PER_HOUR, pSpan.getMaximumLong() * DateUtils.MILLIS_PER_HOUR - 1);
    init(asLong, true);
  }

  public TimeSpan(Date pExactDate) { //exact Time
    LongRange asLong = new LongRange(pExactDate.getTime(), pExactDate.getTime());
    init(asLong, false);
  }
  
  public TimeSpan(LongRange pExactRange) { //exact Range
    init(pExactRange, false);
  }

  public TimeSpan(Date pAtDate, IntRange pSpan) { //range at Day
    if(pSpan.getMinimumInteger() == pSpan.getMaximumInteger())
        throw new RuntimeException("Span without size not allowed");
    
    pAtDate = DateUtils.truncate(pAtDate, Calendar.DATE);
    LongRange asLong = new LongRange(pAtDate.getTime() + pSpan.getMinimumLong() * DateUtils.MILLIS_PER_HOUR,
        pAtDate.getTime() + pSpan.getMaximumLong() * DateUtils.MILLIS_PER_HOUR - 1);
    init(asLong, false);
  }

  protected void init(LongRange pExactRange, boolean pDaily) {
      this.exactSpan = pExactRange;
      this.daily = pDaily;
  }

  @Override
  public int compareTo(TimeSpan o) {
    if (getDirection().equals(TimeSpan.DIRECTION.SEND) && o.getDirection().equals(TimeSpan.DIRECTION.NONE)) {
      return -1;
    } else if (getDirection().equals(TimeSpan.DIRECTION.ARRIVE) && o.getDirection().equals(TimeSpan.DIRECTION.NONE)) {
      return 1;
    } else if (getDirection().equals(TimeSpan.DIRECTION.SEND) && o.getDirection().equals(TimeSpan.DIRECTION.ARRIVE)) {
      return -1;
    } else if (getDirection().equals(TimeSpan.DIRECTION.ARRIVE) && o.getDirection().equals(TimeSpan.DIRECTION.SEND)) {
      return 1;
    } else if (getDirection().equals(o.getDirection())) {
      if (!isValidAtEveryDay() && o.isValidAtEveryDay()) {
        return -1;
      } else if (isValidAtEveryDay() && !o.isValidAtEveryDay()) {
        return 1;
      } else {
        //both valid at everyday or both valid at specified range
        return new Long(getSpan().getMinimumLong()).compareTo(o.getSpan().getMinimumLong());
      }
    }
    return 0;
  }
  
  /**
   * @return the direction
   */
  public DIRECTION getDirection() {
    return direction;
  }

  /**
   * @param direction the direction to set
   */
  public void setDirection(DIRECTION direction) {
    this.direction = direction;
  }

  public enum DIRECTION {
    SEND, ARRIVE, NONE
  }

  public boolean isValidAtExactTime() {
      return exactSpan.getMinimumLong() == exactSpan.getMaximumLong();
  }

  public boolean isValidAtEveryDay() {
      return daily;
  }

  public boolean isValidAtSpecificDay() {
      return !isValidAtEveryDay() && !isValidAtExactTime() &&
              DateUtils.isSameDay(new Date(exactSpan.getMinimumLong()), new Date(exactSpan.getMaximumLong()));
  }

  public boolean isValidAtManualRange() {
      return !isValidAtEveryDay() && !isValidAtSpecificDay() && !isValidAtExactTime();
  }

  public boolean isValid() {
    if (isValidAtExactTime() || isValidAtSpecificDay() || isValidAtManualRange()) {
      if (getSpan().getMaximumLong() < System.currentTimeMillis()) {
        //start and end are in past
        return false;
      }
    }

    //date/frame is valid or we use each day
    return true;
  }

  public String getValidityInfo() {
    String typeStr = (getDirection() == DIRECTION.SEND)?("Abschick"):("Ankunfts");
    if (isValidAtExactTime() && getSpan().getMinimumLong() < System.currentTimeMillis()) {
      //exact date is in past
      return typeStr + "datum in der Vergangenheit";
    } else if (isValidAtSpecificDay()) {
      if (getDate().getTime() < System.currentTimeMillis()) {
        return typeStr + "datum in der Vergangenheit";
      }
      if (getSpan().getMaximumLong() < System.currentTimeMillis()) {
        //start and end are in past
        return typeStr + "zeitrahmen in der Vergangenheit";
      }
    } else if(isValidAtManualRange() && exactSpan.getMaximumLong() < System.currentTimeMillis()) {
        return typeStr + "zeitrahmen in der Vergangenheit";
    }

    //date/frame is valid or we use each day
    return null;
  }

  public boolean intersectsWithNightBonus() {
    if (!ServerSettings.getSingleton().isNightBonusActive()) {
        return false;
    }
    TimeSpan nightBonusSpan = new TimeSpan(new IntRange(ServerSettings.getSingleton().getNightBonusStartHour(), ServerSettings.getSingleton().getNightBonusEndHour()));
    return nightBonusSpan.intersects(this);
  }

  /**
   * @return the date
   */
  public Date getDate() {
    if(isValidAtSpecificDay()) {
      return DateUtils.truncate(new Date(exactSpan.getMinimumLong()), Calendar.DATE);
    }
    return null;
  }
  
  public LongRange getSpan() {
    return exactSpan;
  }
  
  public void setSpan(LongRange pSpan) {
    exactSpan = pSpan;
  }

  public boolean intersects(TimeSpan pSpan) {
    if (!this.getDirection().equals(pSpan.getDirection())) {
      //different directions
      return false;
    }
    
    //one of the spans uses manual Time (new intersect)
    LongRange thisSpan = this.getSpan();
    LongRange theOtherSpan = pSpan.getSpan();
    
    if(this.isValidAtEveryDay() || pSpan.isValidAtEveryDay()) {
        if(this.isValidAtSpecificDay() || pSpan.isValidAtSpecificDay()) {
          //remove day Information
          Long thisStart = DateUtils.getFragmentInMilliseconds(new Date(thisSpan.getMinimumLong()), Calendar.DATE);
          Long thisEnd = DateUtils.getFragmentInMilliseconds(new Date(thisSpan.getMaximumLong()), Calendar.DATE);
          thisSpan = new LongRange(thisStart, thisEnd);
          
          Long otherStart = DateUtils.getFragmentInMilliseconds(new Date(theOtherSpan.getMinimumLong()), Calendar.DATE);
          Long otherEnd = DateUtils.getFragmentInMilliseconds(new Date(theOtherSpan.getMaximumLong()), Calendar.DATE);
          
          theOtherSpan = new LongRange(otherStart, otherEnd);
          
          return thisSpan.overlapsRange(theOtherSpan);
        } else if(this.isValidAtEveryDay() && pSpan.isValidAtEveryDay()) {
          //both valid at every Day - just compare spans
          return thisSpan.overlapsRange(theOtherSpan);
        } else {
            //one span is for everyDay the other is over multiple Days
            //manual intersect
            LongRange always;
            LongRange manual;
            if(this.isValidAtEveryDay()) {
                always = thisSpan;
                manual = theOtherSpan;
            } else {
                always = theOtherSpan;
                manual = thisSpan;
            }
            
            long manualDate = DateUtils.truncate(new Date(manual.getMinimumLong()), Calendar.DATE).getTime();
            long manualStart = manual.getMinimumLong() - manualDate;
            long manualEnd = manual.getMaximumLong() - manualDate;
            
            if(manualEnd - manualStart > DateUtils.MILLIS_PER_DAY) {
                //must intersect somehow because span is longer than 1 Day
                return true;
            }
            //direct intersection
            manual = new LongRange(manualStart, manualEnd);
            if(always.overlapsRange(manual)) return true;
            
            //should not be possible, because it should be handeld by isValidAtSpecificDay
            if(manualEnd <= DateUtils.MILLIS_PER_DAY) return false;
            
            //maybe intersection at next day
            manual = new LongRange(0, manualEnd - DateUtils.MILLIS_PER_DAY);
            return always.overlapsRange(manual);
        }
    }
    
    return thisSpan.overlapsRange(theOtherSpan);
  }

  public static TimeSpan fromPropertyString(String pString) {
    String[] split = pString.split(",");
    TimeSpan t = new TimeSpan();
    try {
      long start = Long.parseLong(split[0]);
      long end = Long.parseLong(split[1]);
      boolean daily = Boolean.parseBoolean(split[2]);
      int dir = Integer.parseInt(split[3]);
      t.init(new LongRange(start, end), daily);
      switch (dir) {
        case 0:
          t.setDirection(DIRECTION.SEND);
          break;
        case 1:
          t.setDirection(DIRECTION.ARRIVE);
          break;
      }
    } catch (Exception e) {
      return null;
    }

    return t;
  }

  public String toPropertyString() {
    String res = "";
    res += exactSpan.getMinimumLong() + ",";
    res += exactSpan.getMaximumLong() + ",";
    res += daily + ",";
    res += getDirection().equals(DIRECTION.SEND) ? "0" : "1";
    return res;
  }

  @Override
  public String toString() {
    String result = null;
    
    if (isValidAtSpecificDay()) {
      SimpleDateFormat fDate = new SimpleDateFormat("dd.MM.yy");
      int startHour =(int) DateUtils.getFragmentInHours(new Date(getSpan().getMinimumLong()), Calendar.DATE);
      int endHour =(int) DateUtils.getFragmentInHours(new Date(getSpan().getMaximumLong() + 1), Calendar.DATE);
      
      result = "Am " + fDate.format(getDate()) + ", von " + startHour + " Uhr bis " + endHour + " Uhr";
    } else if (isValidAtEveryDay()) {
      int startHour =(int) (getSpan().getMinimumLong() / DateUtils.MILLIS_PER_HOUR);
      int endHour =(int) ((getSpan().getMaximumLong() + 1) / DateUtils.MILLIS_PER_HOUR);
      
      result = "T\u00E4glich, von " + startHour + " Uhr bis " + endHour + " Uhr";
    } else if (isValidAtExactTime()) {
      SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
      result = "Am " + f.format(getSpan().getMinimumLong());
    } else if (isValidAtManualRange()) {
      SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
      result = "Von " + f.format(new Date(getSpan().getMinimumLong())) + " bis " + f.format(new Date(getSpan().getMaximumLong()));
    } else {
      result = "";
    }

    return result;
  }
}
