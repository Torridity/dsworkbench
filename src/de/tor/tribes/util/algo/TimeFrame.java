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
    private long arriveTolerance = 0;

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

    public void setArriveTolerance(long pHours) {
        arriveTolerance = pHours;
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

    public Date getArriveDate(long runtime) {
        Calendar sendCal = Calendar.getInstance();
        long TOLERANCE = arriveTolerance * 60 * 60 * 1000;
        long TWENTY_MINUTES = 20 * 60 * 1000;
        for (long l = start; l < end; l += TWENTY_MINUTES) {
            long sendTime = l;
            long arriveTime = sendTime + runtime;
            sendCal.setTimeInMillis(sendTime);
            int sendHour = sendCal.get(Calendar.HOUR_OF_DAY);
            int sendMinute = sendCal.get(Calendar.MINUTE);
            int sendSecond = sendCal.get(Calendar.SECOND);
            Calendar arriveCal = Calendar.getInstance();
            arriveCal.setTimeInMillis(arriveTime);
            int arriveHour = arriveCal.get(Calendar.HOUR_OF_DAY);
            boolean inFrame = false;
            if (arriveHour >= 0 && arriveHour < 8) {
                //only possible in night bonus
            } else if (Math.abs(arriveTime - end) > TOLERANCE) {
                //too far away
            } else {
                for (Point p : mFrames) {
                    //check time frame parts
                    inFrame = ((sendHour >= p.x) && ((sendHour <= p.y) && (sendMinute <= 59) && (sendSecond <= 59)));
                    if (inFrame) {
                        arriveCal.setTimeInMillis(arriveTime);
                        return arriveCal.getTime();
                    }
                }
            }
        }
        return null;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}