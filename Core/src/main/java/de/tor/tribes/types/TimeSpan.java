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
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.time.DateUtils;

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
    TimeSpan s = new TimeSpan(exactSpan, daily);
    s.setDirection(getDirection());
    return s;
  }
  private boolean daily = false;
  private Range<Long> exactSpan = null;
  private DIRECTION direction = DIRECTION.SEND;

  public TimeSpan() {
  }
  
  public TimeSpan(Range<Long> pSpan, boolean repeatDaily) {
      this.exactSpan = pSpan;
      this.daily = repeatDaily;
  }
  
  public TimeSpan(Date pExactDate) { //exact Time
    this(Range.between(pExactDate.getTime(), pExactDate.getTime()), false);
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
        return getSpan().getMaximum().compareTo(o.getSpan().getMinimum());
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
      return exactSpan.getMinimum() == exactSpan.getMaximum();
  }

  public boolean isValidAtEveryDay() {
      return daily;
  }

  public boolean isValidAtSpecificDay() {
      return !isValidAtEveryDay() && !isValidAtExactTime() &&
              DateUtils.isSameDay(new Date(exactSpan.getMinimum()), new Date(exactSpan.getMaximum()));
  }

  public boolean isValidAtManualRange() {
      return !isValidAtEveryDay() && !isValidAtSpecificDay() && !isValidAtExactTime();
  }

  public boolean isValid() {
    if (isValidAtExactTime() || isValidAtSpecificDay() || isValidAtManualRange()) {
      if (getSpan().getMaximum() < System.currentTimeMillis()) {
        //start and end are in past
        return false;
      }
    }

    //date/frame is valid or we use each day
    return true;
  }

  public String getValidityInfo() {
    String typeStr = (getDirection() == DIRECTION.SEND)?("Abschick"):("Ankunfts");
    if (isValidAtExactTime() && getSpan().getMinimum() < System.currentTimeMillis()) {
      //exact date is in past
      return typeStr + "datum in der Vergangenheit";
    } else if (isValidAtSpecificDay()) {
      if (getDate().getTime() < System.currentTimeMillis()) {
        return typeStr + "datum in der Vergangenheit";
      }
      if (getSpan().getMaximum() < System.currentTimeMillis()) {
        //start and end are in past
        return typeStr + "zeitrahmen in der Vergangenheit";
      }
    } else if(isValidAtManualRange() && exactSpan.getMaximum() < System.currentTimeMillis()) {
        return typeStr + "zeitrahmen in der Vergangenheit";
    }

    //date/frame is valid or we use each day
    return null;
  }

  public boolean intersectsWithNightBonus() {
    if (!ServerSettings.getSingleton().isNightBonusActive()) {
        return false;
    }
    TimeSpan nightBonusSpan = new TimeSpan(
            Range.between(ServerSettings.getSingleton().getNightBonusStartHour() * DateUtils.MILLIS_PER_HOUR,
            ServerSettings.getSingleton().getNightBonusEndHour() * DateUtils.MILLIS_PER_HOUR), true);
    return nightBonusSpan.intersects(this);
  }

  /**
   * @return the date
   */
  public Date getDate() {
    if(isValidAtSpecificDay()) {
      return DateUtils.truncate(new Date(exactSpan.getMinimum()), Calendar.DATE);
    }
    return null;
  }
  
  public Range<Long> getSpan() {
    return exactSpan;
  }
  
  public void setSpan(Range<Long> pSpan) {
    exactSpan = pSpan;
  }

  public boolean intersects(TimeSpan pSpan) {
    if (!this.getDirection().equals(pSpan.getDirection())) {
      //different directions
      return false;
    }
    
    //one of the spans uses manual Time (new intersect)
    Range<Long> thisSpan = this.getSpan();
    Range<Long> theOtherSpan = pSpan.getSpan();
    
    if(this.isValidAtEveryDay() || pSpan.isValidAtEveryDay()) {
        if(this.isValidAtSpecificDay() || pSpan.isValidAtSpecificDay()) {
          //remove day Information
          Long thisStart = DateUtils.getFragmentInMilliseconds(new Date(thisSpan.getMinimum()), Calendar.DATE);
          Long thisEnd = DateUtils.getFragmentInMilliseconds(new Date(thisSpan.getMaximum()), Calendar.DATE);
          thisSpan = Range.between(thisStart, thisEnd);
          
          Long otherStart = DateUtils.getFragmentInMilliseconds(new Date(theOtherSpan.getMinimum()), Calendar.DATE);
          Long otherEnd = DateUtils.getFragmentInMilliseconds(new Date(theOtherSpan.getMaximum()), Calendar.DATE);
          
          theOtherSpan = Range.between(otherStart, otherEnd);
          
          return thisSpan.isOverlappedBy(theOtherSpan);
        } else if(this.isValidAtEveryDay() && pSpan.isValidAtEveryDay()) {
          //both valid at every Day - just compare spans
          return thisSpan.isOverlappedBy(theOtherSpan);
        } else {
            //one span is for everyDay the other is over multiple Days
            //manual intersect
            Range<Long> always;
            Range<Long> manual;
            if(this.isValidAtEveryDay()) {
                always = thisSpan;
                manual = theOtherSpan;
            } else {
                always = theOtherSpan;
                manual = thisSpan;
            }
            
            long manualDate = DateUtils.truncate(new Date(manual.getMinimum()), Calendar.DATE).getTime();
            long manualStart = manual.getMinimum() - manualDate;
            long manualEnd = manual.getMaximum() - manualDate;
            
            if(manualEnd - manualStart > DateUtils.MILLIS_PER_DAY) {
                //must intersect somehow because span is longer than 1 Day
                return true;
            }
            //direct intersection
            manual = Range.between(manualStart, manualEnd);
            if(always.isOverlappedBy(manual)) return true;
            
            //should not be possible, because it should be handeld by isValidAtSpecificDay
            if(manualEnd <= DateUtils.MILLIS_PER_DAY) return false;
            
            //maybe intersection at next day
            manual = Range.between(new Long(0), manualEnd - DateUtils.MILLIS_PER_DAY);
            return always.isOverlappedBy(manual);
        }
    }
    
    return thisSpan.isOverlappedBy(theOtherSpan);
  }

  public static TimeSpan fromPropertyString(String pString) {
    String[] split = pString.split(",");
    try {
      long start = Long.parseLong(split[0]);
      long end = Long.parseLong(split[1]);
      boolean daily = Boolean.parseBoolean(split[2]);
      int dir = Integer.parseInt(split[3]);
      TimeSpan t = new TimeSpan(Range.between(start, end), daily);
      switch (dir) {
        case 0:
          t.setDirection(DIRECTION.SEND);
          break;
        case 1:
          t.setDirection(DIRECTION.ARRIVE);
          break;
      }
      return t;
    } catch (Exception ignored) {
    }
    return null;
  }

  public String toPropertyString() {
    String res = "";
    res += exactSpan.getMinimum() + ",";
    res += exactSpan.getMaximum() + ",";
    res += daily + ",";
    res += getDirection().equals(DIRECTION.SEND) ? "0" : "1";
    return res;
  }

  @Override
  public String toString() {
    String result = null;
    
    if (isValidAtSpecificDay()) {
      SimpleDateFormat fDate = new SimpleDateFormat("dd.MM.yy");
      int startHour =(int) DateUtils.getFragmentInHours(new Date(getSpan().getMinimum()), Calendar.DATE);
      int endHour =(int) DateUtils.getFragmentInHours(new Date(getSpan().getMaximum() + 1), Calendar.DATE);
      
      result = "Am " + fDate.format(getDate()) + ", von " + startHour + " Uhr bis " + endHour + " Uhr";
    } else if (isValidAtEveryDay()) {
      int startHour =(int) (getSpan().getMinimum() / DateUtils.MILLIS_PER_HOUR);
      int endHour =(int) ((getSpan().getMaximum() + 1) / DateUtils.MILLIS_PER_HOUR);
      
      result = "T\u00E4glich, von " + startHour + " Uhr bis " + endHour + " Uhr";
    } else if (isValidAtExactTime()) {
      SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
      result = "Am " + f.format(getSpan().getMinimum());
    } else if (isValidAtManualRange()) {
      SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
      result = "Von " + f.format(new Date(getSpan().getMinimum())) + " bis " + f.format(new Date(getSpan().getMaximum()));
    } else {
      result = "";
    }

    return result;
  }
}
