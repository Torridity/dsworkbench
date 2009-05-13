/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import java.awt.Point;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class TimeFrame {

    private long start = 0;
    private long end = 0;
    private List<Point> mFrames = null;
    private int arriveTolerance = 0;

    public TimeFrame(Date pStart, Date pEnd, int pMinHour, int pMaxHour) {
        start = pStart.getTime();
        end = pEnd.getTime();
        mFrames = new LinkedList<Point>();
        mFrames.add(new Point(pMinHour, pMaxHour));
    }

    public TimeFrame(Date pStart, Date pEnd) {
        start = pStart.getTime();
        end = pEnd.getTime();
        mFrames = new LinkedList<Point>();
    }

    public void setArriveTolerance(int pSeconds) {
        arriveTolerance = pSeconds;
    }

    public void addFrame(int pMinHour, int pMaxHour) {
        mFrames.add(new Point(pMinHour, pMaxHour));
    }

    public boolean inside(Date pDate) {
        long t = pDate.getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(pDate);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        boolean inFrame = false;
        //check if time is in time frame
        //use max. arrive time
        //if ((t > start) && (t < end + arriveTolerance * 1000)) {
        if ((t > start) && (t < end)) {
            //general time is ok
            for (Point p : mFrames) {
                //check time frame parts
                inFrame = ((hour >= p.x) && ((hour <= p.y) && (minute <= 59) && (second <= 59)));
                if (inFrame) {
                    break;
                }
            }
            //check end frame
            /*if (!inFrame) {
                //if not yet in frame check end frame
                inFrame = ((t > (end - arriveTolerance * 1000)) && (t < (end + arriveTolerance * 1000)));
            }*/
        }
        return inFrame;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}