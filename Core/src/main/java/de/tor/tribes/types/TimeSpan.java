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

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.test.AnyTribe;
import de.tor.tribes.util.ServerSettings;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.math.IntRange;
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
    TimeSpan s = new TimeSpan(date, span, validFor);
    s.setDirection(getDirection());
    return s;
  }
  private Tribe validFor = null;
  private Date date = null;
  private IntRange span = null;
  private DIRECTION direction = DIRECTION.SEND;

  public TimeSpan() {
  }

  public TimeSpan(IntRange pSpan) {
    this(null, pSpan, null);
  }

  public TimeSpan(Date pExactDate) {
    this(pExactDate, null, null);
  }

  public TimeSpan(Date pExactDate, Tribe pTribe) {
    this(pExactDate, null, pTribe);
  }

  public TimeSpan(Date pAtDate, IntRange pSpan) {
    this(pAtDate, pSpan, null);
  }

  public TimeSpan(IntRange pSpan, Tribe pTribe) {
    this(null, pSpan, pTribe);
  }

  public TimeSpan(Date pAtDate, IntRange pSpan, Tribe pTribe) {
    date = pAtDate;
    span = pSpan;
    validFor = pTribe;
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

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
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
      if (getAtDate() != null && o.getAtDate() == null) {
        return -1;
      } else if (getAtDate() == null && o.getAtDate() != null) {
        return 1;
      } else if (getAtDate() == null && o.getAtDate() == null) {
        if (getSpan() != null && o.getSpan() != null) {
          return new Integer(getSpan().getMinimumInteger()).compareTo(o.getSpan().getMinimumInteger());
        } else {
          return 0;
        }
      }
    }
    return 0;
  }

  public enum DIRECTION {

    SEND, ARRIVE, NONE
  }

  public boolean isValidAtExactTime() {
      return (date != null && span == null);
  }

  public boolean isValidAtEveryDay() {
      return (date == null && span != null);
  }

  public boolean isValidAtSpecificDay() {
      return (date != null && span != null);
  }

  public boolean isValid() {
    if (isValidAtExactTime() && getAtDate().getTime() < System.currentTimeMillis()) {
      //exact date is in past
      return false;
    } else if (isValidAtSpecificDay()) {
      Date day = DateUtils.setHours(getAtDate(), 0);
      day = DateUtils.setMinutes(day, 0);
      day = DateUtils.setSeconds(day, 0);
      day = DateUtils.setMilliseconds(day, 0);
      long start = day.getTime() + getSpan().getMinimumInteger() * DateUtils.MILLIS_PER_HOUR;
      long end = day.getTime() + getSpan().getMaximumInteger() * DateUtils.MILLIS_PER_HOUR;
      if (start < System.currentTimeMillis() && end < System.currentTimeMillis()) {
        //start and end are in past
        return false;
      }
    }

    //date/frame is valid or we use each day
    return true;
  }

  public String getValidityInfo() {
    if (isValidAtExactTime() && getAtDate().getTime() < System.currentTimeMillis()) {
      //exact date is in past
      return "Abschickdatum in der Vergangenheit";
    } else if (isValidAtSpecificDay()) {
      Date day = DateUtils.setHours(getAtDate(), 0);
      day = DateUtils.setMinutes(day, 0);
      day = DateUtils.setSeconds(day, 0);
      day = DateUtils.setMilliseconds(day, 0);

      if (day.getTime() < System.currentTimeMillis()) {
        return "Abschickdatum in der Vergangenheit";
      }
      long start = day.getTime() + getSpan().getMinimumInteger() * DateUtils.MILLIS_PER_HOUR;
      long end = day.getTime() + getSpan().getMaximumInteger() * DateUtils.MILLIS_PER_HOUR;
      if (start < System.currentTimeMillis() && end < System.currentTimeMillis()) {
        //start and end are in past
        return "Abschickzeitrahmen in der Vergangenheit";
      }
    }

    //date/frame is valid or we use each day
    return null;
  }

  public boolean intersectsWithNightBonus() {
      if (!ServerSettings.getSingleton().isNightBonusActive()) {
          return false;
      }

      IntRange nightBonusRange = new IntRange(ServerSettings.getSingleton().getNightBonusStartHour(), ServerSettings.getSingleton().getNightBonusEndHour());
      IntRange thisRange = getSpan();
      if (thisRange == null) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(date);
          thisRange = new IntRange(cal.get(Calendar.HOUR_OF_DAY));
      }

      return thisRange.getMinimumInteger() != nightBonusRange.getMaximumInteger() && thisRange.overlapsRange(nightBonusRange);

  }

  public Date getAtDate() {
      return date;
  }

  public IntRange getSpan() {
    if (span == null) {
      //no span defined, return new span valid for hour of 'atDay'
      Calendar cal = Calendar.getInstance();
        cal.setTime(date);
      return new IntRange(cal.get(Calendar.HOUR_OF_DAY));
    }
    return span;
  }

  public void setSpan(IntRange pSpan) {
    span = pSpan;
  }

  public Tribe isValidFor() {
    return validFor;
  }

  public boolean isValidForTribe(Tribe pTribe) {
    return pTribe == null || validFor == null || validFor.getId() == pTribe.getId() || (pTribe != null && pTribe.equals(AnyTribe.getSingleton()));
  }

  public boolean intersects(TimeSpan pSpan) {

    if (!this.getDirection().equals(pSpan.getDirection())) {
      //different directions
      return false;
    }
    Tribe thisTribe = isValidFor();
    Tribe theOtherTribe = pSpan.isValidFor();
    if (thisTribe != null && theOtherTribe != null && thisTribe.getId() != theOtherTribe.getId()) {
      //both spans are for different tribes, so they can't intersect by definition
      return false;
    }

    Date thisDay = getAtDate();
    Date theOtherDay = pSpan.getAtDate();

    if (thisDay != null && theOtherDay != null) {
      if (DateUtils.isSameDay(thisDay, theOtherDay) && getSpan() != null && pSpan.getSpan() != null) {
        //we are at the same day, so intersection is possible if no exact TOA was provided
        IntRange thisRange = getSpan();
        IntRange theOtherRange = pSpan.getSpan();

        return thisRange.overlapsRange(theOtherRange)
                //                   && thisRange.getMinimumInteger() != theOtherRange.getMinimumInteger()
                //                   && thisRange.getMaximumInteger() != theOtherRange.getMaximumInteger()
                && thisRange.getMaximumInteger() != theOtherRange.getMinimumInteger()
                && thisRange.getMinimumInteger() != theOtherRange.getMaximumInteger();
      } else {
        //this day and the other day are differnt, so no intersection can happen
        return false;
      }
    } else {
      //there was no day specified, intersection is possible
      IntRange thisRange = getSpan();
      IntRange theOtherRange = pSpan.getSpan();
      return thisRange.overlapsRange(theOtherRange)
              //                    && thisRange.getMinimumInteger() != theOtherRange.getMinimumInteger()
              //                  && thisRange.getMaximumInteger() != theOtherRange.getMaximumInteger()
              && thisRange.getMaximumInteger() != theOtherRange.getMinimumInteger()
              && thisRange.getMinimumInteger() != theOtherRange.getMaximumInteger();
    }
  }

  public static TimeSpan fromPropertyString(String pString) {
    String[] split = pString.split(",");
    TimeSpan t = new TimeSpan();
    try {
      int type = Integer.parseInt(split[0]);
      switch (type) {
        case 0: { //every day
          int start = Integer.parseInt(split[1]);
          int end = Integer.parseInt(split[2]);
          int dir = Integer.parseInt(split[3]);
          t.setDate(null);
          t.setSpan(new IntRange(start, end));
          switch (dir) {
            case 0:
              t.setDirection(DIRECTION.SEND);
              break;
            case 1:
              t.setDirection(DIRECTION.ARRIVE);
              break;
          }
          break;
        }
        case 1: { //specific day
          long date = Long.parseLong(split[1]);
          int start = Integer.parseInt(split[2]);
          int end = Integer.parseInt(split[3]);
          int dir = Integer.parseInt(split[4]);
          t.setDate(new Date(date));
          t.setSpan(new IntRange(start, end));
          switch (dir) {
            case 0:
              t.setDirection(DIRECTION.SEND);
              break;
            case 1:
              t.setDirection(DIRECTION.ARRIVE);
              break;
          }
          break;
        }
        case 2: {
          //exact time
          long date = Long.parseLong(split[1]);
          int dir = Integer.parseInt(split[2]);
          t.setDate(new Date(date));
          t.setSpan(null);
          switch (dir) {
            case 0:
              t.setDirection(DIRECTION.SEND);
              break;
            case 1:
              t.setDirection(DIRECTION.ARRIVE);
              break;
          }
          break;
        }
      }

    } catch (Exception e) {
      return null;
    }

    return t;
  }

  public String toPropertyString() {
    String res = "";

    if (isValidAtEveryDay()) {
      res += "0,";
      res += getSpan().getMinimumInteger() + "," + getSpan().getMaximumInteger();
    } else if (isValidAtSpecificDay()) {
      res += "1,";
        res += date.getTime() + ",";
      res += getSpan().getMinimumInteger() + "," + getSpan().getMaximumInteger();
    } else if (isValidAtExactTime()) {
      res += "2,";
        res += date.getTime();
    }

    res += getDirection().equals(DIRECTION.SEND) ? ",0" : ",1";
    return res;
  }

  @Override
  public String toString() {
    String result = null;
      if (date != null && span != null) {
      SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
          result = "Am " + f.format(date) + ", von " + getSpan().getMinimumInteger() + " Uhr bis " + getSpan().getMaximumInteger() + " Uhr";
      if (validFor != null) {
        result += " (" + validFor.toString() + ")";
      } else {
        result += " (Alle)";
      }
    } else if (date == null && span != null) {
      result = "Täglich, von " + getSpan().getMinimumInteger() + " Uhr bis " + getSpan().getMaximumInteger() + " Uhr";
      if (validFor != null) {
        result += " (" + validFor.toString() + ")";
      } else {
        result += " (Alle)";
      }
    } else if (date != null && span == null) {
      SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
        result = "Am " + f.format(date);
      if (validFor != null) {
        result += " (" + validFor.toString() + ")";
      } else {
        result += " (Alle)";
      }
    } else {
      result = "";
    }

    return result;
  }
}
