/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.test.AnyTribe;
import de.tor.tribes.db.DatabaseServerEntry;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.util.GlobalOptions;
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
        TimeSpan s = new TimeSpan(getAtDate(), getSpan(), isValidFor());
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
        //TODO implement compareTo!!
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
                return new Integer(getSpan().getMinimumInteger()).compareTo(o.getSpan().getMinimumInteger());
            }
        }
        return 0;
    }

    public enum DIRECTION {

        SEND, ARRIVE, NONE
    }

    public boolean isValidAtExactTime() {
        return (getDate() != null && span == null);
    }

    public boolean isValidAtEveryDay() {
        return (getDate() == null && span != null);
    }

    public boolean isValidAtSpecificDay() {
        return (getDate() != null && span != null);
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
        int nightBonus = ServerManager.getNightBonusRange(GlobalOptions.getSelectedServer());
        IntRange nightBonusRange = null;
        IntRange thisRange = getSpan();
        if (thisRange == null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getDate());
            thisRange = new IntRange(cal.get(Calendar.HOUR_OF_DAY));
        }
        switch (nightBonus) {
            case DatabaseServerEntry.NO_NIGHT_BONUS: {
                return false;
            }
            case DatabaseServerEntry.NIGHT_BONUS_0to7: {
                nightBonusRange = new IntRange(0, 7);
                if (thisRange.getMinimumInteger() == 7) {
                    return false;
                } else {
                    return thisRange.overlapsRange(nightBonusRange);
                }
            }
            default: {
                nightBonusRange = new IntRange(0, 8);
                if (thisRange.getMinimumInteger() == 8) {
                    return false;
                } else {
                    return thisRange.overlapsRange(nightBonusRange);
                }
            }
        }

    }

    public Date getAtDate() {
        return getDate();
    }

    public IntRange getSpan() {
        if (span == null) {
            //no span defined, return new span valid for hour of 'atDay'
            Calendar cal = Calendar.getInstance();
            cal.setTime(getDate());
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
        return (validFor == null || validFor.equals(pTribe) || (pTribe == null && pTribe.equals(AnyTribe.getSingleton())));
    }

    public boolean intersects(TimeSpan pSpan) {

        if (!this.getDirection().equals(pSpan.getDirection())) {
            //different directions
            return false;
        }
        Tribe thisTribe = isValidFor();
        Tribe theOtherTribe = pSpan.isValidFor();
        if (thisTribe != null && theOtherTribe != null && !thisTribe.equals(theOtherTribe)) {
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
        String[] elems = pString.split(",");

        Date date = null;
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        boolean noSpan = false;
        try {
            date = f.parse(elems[0]);
        } catch (Exception e) {
            try {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
                date = f.parse(elems[0]);
                noSpan = true;
            } catch (Exception e1) {
                date = null;
            }
        }
        int spanStart = 0;
        int spanEnd = 0;
        try {
            spanStart = Integer.parseInt(elems[1]);
            spanEnd = Integer.parseInt(elems[2]);
        } catch (Exception e) {
            //invalid!?
            if (!noSpan) {
                return null;
            }
        }
        Tribe t = null;
        try {
            if (elems[3].equals("*")) {
                t = null;
            } else {
                t = DataHolder.getSingleton().getTribeByName(elems[3]);
            }
        } catch (Exception e) {
        }

        DIRECTION dir = DIRECTION.NONE;
        try {
            dir = (elems[4].equals("0") ? DIRECTION.SEND : DIRECTION.ARRIVE);
        } catch (IndexOutOfBoundsException iobe) {
            //old format
        }
        TimeSpan result = null;
        if (!noSpan) {
            result = new TimeSpan(date, new IntRange(spanStart, spanEnd), t);
        } else {
            result = new TimeSpan(date, null, t);
        }
        result.setDirection(dir);
        return result;
    }

    public String toPropertyString() {
        String res = "";

        if (getDate() != null && span != null) {
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
            res = f.format(getDate()) + "," + getSpan().getMinimumInteger() + "," + getSpan().getMaximumInteger() + ",";
            if (validFor != null) {
                res += validFor;
            } else {
                res += "*";
            }
        } else if (getDate() == null && span != null) {
            res = "*," + getSpan().getMinimumInteger() + "," + getSpan().getMaximumInteger() + ",";
            if (validFor != null) {
                res += validFor;
            } else {
                res += "*";
            }
        } else {
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
            res = f.format(getDate()) + "," + getSpan().getMinimumInteger() + "," + getSpan().getMaximumInteger() + ",";
            if (validFor != null) {
                res += validFor;
            } else {
                res += "*";
            }
        }
        res += getDirection().equals(DIRECTION.SEND) ? ",0" : ",1";
        return res;
    }

    @Override
    public String toString() {
        String result = null;
        if (getDate() != null && span != null) {
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
            result = "Am " + f.format(getDate()) + ", von " + getSpan().getMinimumInteger() + " Uhr bis " + getSpan().getMaximumInteger() + " Uhr";
            if (validFor != null) {
                result += " (" + validFor.toString() + ")";
            } else {
                result += " (Alle)";
            }
        } else if (getDate() == null && span != null) {
            result = "Täglich, von " + getSpan().getMinimumInteger() + " Uhr bis " + getSpan().getMaximumInteger() + " Uhr";
            if (validFor != null) {
                result += " (" + validFor.toString() + ")";
            } else {
                result += " (Alle)";
            }
        } else if (getDate() != null && span == null) {
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss 'Uhr'");
            result = "Am " + f.format(getDate());
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

    public static void main(String[] args) throws Exception {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        System.out.println(new TimeSpan(f.parse("29.10.2010 23:59:59")).intersectsWithNightBonus());

    }
}
