/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Jejkal
 */
public class TimeFrame {

    private long start = 0;
    private long end = 0;
    private int minHour = 0;
    private int maxHour = 0;

    public TimeFrame(Date pStart, Date pEnd, int pMinHour, int pMaxHour) {
        start = pStart.getTime();
        end = pEnd.getTime();
        minHour = pMinHour;
        maxHour = pMaxHour;
    }

    public boolean inside(Date pDate) {
        long t = pDate.getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(pDate);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        if ((t > start) && (t < end)) {
            return ((hour >= minHour) && ((hour <= maxHour) && (minute <= 59) && (second <= 59)));
        }
        return false;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}