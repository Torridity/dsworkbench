/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Charon
 */
public class TimeSpan {

    private Tribe validFor = null;
    private Date atDate = null;
    private Point span = null;

    public TimeSpan(Point pSpan) {
        span = pSpan;
        validFor = null;
    }

    public TimeSpan(Date pAtDate, Point pSpan) {
        atDate = pAtDate;
        span = pSpan;
        validFor = null;
    }

    public TimeSpan(Point pSpan, Tribe pTribe) {
        span = pSpan;
        validFor = pTribe;
    }

    public TimeSpan(Date pAtDate, Point pSpan, Tribe pTribe) {
        atDate = pAtDate;
        span = pSpan;
        validFor = pTribe;
    }

    public Date getAtDate() {
        return atDate;
    }

    public Point getSpan() {
        return span;
    }

    public void setSpan(Point pSpan) {
        span = pSpan;
    }

    public Tribe isValidFor() {
        return validFor;
    }

    public static TimeSpan fromPropertyString(String pString) {
        String[] elems = pString.split(",");
        Date date = null;
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        try {
            date = f.parse(elems[0]);
        } catch (Exception e) {
            date = null;
        }
        int spanStart = 0;
        int spanEnd = 0;
        try {
            spanStart = Integer.parseInt(elems[1]);
            spanEnd = Integer.parseInt(elems[2]);
        } catch (Exception e) {
            //invalid!?
            return null;
        }
        Tribe t = null;
        try {
            t = DataHolder.getSingleton().getTribeByName(elems[3]);
        } catch (Exception e) {
        }
        if (date == null && t == null) {
            return new TimeSpan(new Point(spanStart, spanEnd));
        } else if (date != null && t == null) {
            return new TimeSpan(date, new Point(spanStart, spanEnd));
        } else {
            return new TimeSpan(date, new Point(spanStart, spanEnd), t);
        }
    }

    public String toPropertyString() {
        String res = "";
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        if (atDate != null) {
            res = f.format(atDate) + "," + span.x + "," + span.y + ",";
            if (validFor != null) {
                res += validFor;
            } else {
                res += "*";
            }
        } else {
            res = "*," + span.x + "," + span.y + ",";
            //res = "Täglich, von " + span.x + " Uhr bis " + span.y + " Uhr";
            if (validFor != null) {
                res += validFor;
            } else {
                res += "*";
            }
        }
        return res;
    }

    @Override
    public String toString() {
        String result = null;
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        if (atDate != null) {
            result = "Am " + f.format(atDate) + ", von " + span.x + " Uhr bis " + span.y + " Uhr";
            if (validFor != null) {
                result += " (" + validFor.toString() + ")";
            } else {
                result += " (Alle)";
            }
        } else {
            result = "Täglich, von " + span.x + " Uhr bis " + span.y + " Uhr";
            if (validFor != null) {
                result += " (" + validFor.toString() + ")";
            } else {
                result += " (Alle)";
            }
        }

        return result;
    }
}
