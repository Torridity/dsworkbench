/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.Tribe;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jejkal
 */
public class TimeFrame {

    private long start = 0;
    private long end = 0;
    private List<TimeSpan> timeSpans = null;
    private boolean variableArriveTime = false;
    private int variableArriveStartHour = -1;
    private int variableArriveEndHour = -1;

    public TimeFrame(Date pStart, Date pEnd, int pMinHour, int pMaxHour) {
        start = pStart.getTime();
        end = pEnd.getTime();
        timeSpans = new LinkedList<TimeSpan>();
    }

    public TimeFrame(Date pStart, Date pEnd) {
        start = pStart.getTime();
        end = pEnd.getTime();
        timeSpans = new LinkedList<TimeSpan>();
    }

    public void setStart(long pTime) {
        start = pTime;
    }

    public void setEnd(long pTime) {
        end = pTime;
    }

    public void setArriveSpan(int pStartHour, int pEndHour) {
        variableArriveStartHour = pStartHour;
        variableArriveEndHour = pEndHour;
    }

    public void setUseVariableArriveTime(boolean pValue) {
        variableArriveTime = pValue;
    }

    public boolean isVariableArriveTime() {
        return variableArriveTime;
    }

    public void addTimeSpan(TimeSpan pSpan) {
        timeSpans.add(pSpan);
    }

    public boolean inside(Date pDate, Tribe pTribe) {
        long t = pDate.getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(pDate);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        boolean inFrame = false;
        if ((t > start) && (t < end)) {
            for (TimeSpan span : timeSpans) {
                // System.out.println("Check frame for " + pTribe);
                if (span.isValidFor() == null || span.isValidFor() == pTribe) {
                    //System.out.println("Tribe " + span.isValidFor() + " affected by " + span);
                    Date d = span.getAtDate();
                    if (d != null) {
                        //check day
                        Calendar c2 = Calendar.getInstance();
                        c2.setTime(d);
                        if (c2.get(Calendar.DAY_OF_MONTH) == day && c2.get(Calendar.MONTH) == month && c2.get(Calendar.YEAR) == year) {
                            //date is the same, so check span
                            Point p = span.getSpan();
                            inFrame = ((hour >= p.x) && ((hour <= p.y) && (minute <= 59) && (second <= 59)));
                            if (inFrame) {
                                break;
                            }
                        }
                    } else {
                        //"every day" span, so only check time
                        Point p = span.getSpan();
                        inFrame = ((hour >= p.x) && ((hour <= p.y) && (minute <= 59) && (second <= 59)));
                        if (inFrame) {
                            break;
                        }
                    }
                }
            }
        }
        return inFrame;
    }

    public Date fitInto(long pRuntime, int pArriveStartHour, int pArriveEndHour, Tribe pTribe, List<Long> usedDates) {
        Date arrive = new Date(end);
        Calendar c = Calendar.getInstance();
        c.setTime(arrive);
        c.set(Calendar.HOUR_OF_DAY, pArriveStartHour);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date minDate = c.getTime();
        c = Calendar.getInstance();
        c.setTime(arrive);
        c.set(Calendar.HOUR_OF_DAY, pArriveEndHour - 1);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        Date maxDate = c.getTime();
        long pEnd = end;
        for (long l = minDate.getTime(); l <= maxDate.getTime(); l += 60000) {
            end = l;
            Date current = new Date(l - pRuntime);
            if (inside(current, pTribe)) {
                if (usedDates.contains(current.getTime())) {
                    l += 10000;
                } else {
                    end = pEnd;
                    return current;
                }
            }
        }
        end = pEnd;
        return null;
    }

    public Date getRandomArriveTime(long pRuntime, Tribe pTribe, List<Long> usedDates) {
        List<Long> l = timespansToTimeframes(pTribe);
        //SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Calendar arriveStart = Calendar.getInstance();
        arriveStart.setTimeInMillis(end);
        arriveStart.set(Calendar.HOUR_OF_DAY, variableArriveStartHour);
        arriveStart.set(Calendar.MINUTE, 0);
        arriveStart.set(Calendar.SECOND, 0);
        arriveStart.set(Calendar.MILLISECOND, 0);
        Calendar arriveEnd = Calendar.getInstance();
        arriveEnd.setTimeInMillis(end);
        arriveEnd.set(Calendar.HOUR_OF_DAY, variableArriveEndHour - 1);
        arriveEnd.set(Calendar.MINUTE, 59);
        arriveEnd.set(Calendar.SECOND, 59);
        arriveEnd.set(Calendar.MILLISECOND, 999);
        boolean first = true;

        while (!l.isEmpty()) {
            long s = l.remove(0);
            if (first) {
                //use start time in first iteration
                if (s < start) {
                    s = start;
                    first = false;
                }
            }
            long e = l.remove(0);
            //System.out.println("Start: " + f.format(new Date(s)));
            //System.out.println("End: " + f.format(new Date(e)));
            Calendar calStart = Calendar.getInstance();
            calStart.setTimeInMillis(s + pRuntime);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTimeInMillis(e + pRuntime);
            //System.out.println("CalStart: " + f.format(calStart.getTime()));
            //System.out.println("CalEnd: " + f.format(calEnd.getTime()));

            boolean startFits = (calStart.get(Calendar.HOUR_OF_DAY) >= arriveStart.getTimeInMillis() && calStart.get(Calendar.HOUR_OF_DAY) < arriveEnd.getTimeInMillis());
            boolean endFits = (calEnd.get(Calendar.HOUR_OF_DAY) >= arriveStart.getTimeInMillis() && calEnd.get(Calendar.HOUR_OF_DAY) < arriveEnd.getTimeInMillis());
            long firstTerm = -1;
            long randomTerm = -1;

            if (startFits && endFits) {
                //return new Date(s + (long) Math.round(Math.random() * (double) (e - s)));
                firstTerm = s;
                randomTerm = e - s;
            } else if (startFits) {
                long diff = arriveEnd.getTimeInMillis() - calStart.getTimeInMillis();
                // return new Date(s + (long) Math.round(Math.random() * (double) (diff)));
                firstTerm = s;
                randomTerm = diff;
            } else if (endFits) {
                long diff = arriveEnd.getTimeInMillis() - calEnd.getTimeInMillis();
                //return new Date(e + (long) Math.round(Math.random() * (double) (diff)));
                firstTerm = e;
                randomTerm = diff;
            } else {
                long startPoss = -1;
                long endPoss = -1;
                for (long i = s; i < e; i += 60000) {
                    if (i + pRuntime >= arriveStart.getTimeInMillis() && i + pRuntime < arriveEnd.getTimeInMillis()) {
                        if (startPoss == -1) {
                            startPoss = i;
                        } else {
                            endPoss = i;
                        }
                    } else {
                        if (startPoss != -1) {
                            //end reached, not longer valid
                            break;
                        }
                    }
                }
                if (startPoss != -1 && endPoss != -1) {
                    //valid start and end  found
                    long diff = endPoss - startPoss;
                    //return new Date(startPoss + (long) Math.round(Math.random() * (double) (diff)) + pRuntime);
                    firstTerm = startPoss + pRuntime;
                    randomTerm = diff;
                }
            }

            if (firstTerm != -1 && randomTerm != -1) {
                Date d = new Date(firstTerm + Math.round(Math.random() * (double) randomTerm));
                while (usedDates.contains(d.getTime())) {
                    d = new Date(firstTerm + Math.round(Math.random() * (double) randomTerm));
                }
                return d;
            }
        }
        return null;
    }

    private List<Long> timespansToTimeframes(Tribe pTribe) {
        Calendar cs = Calendar.getInstance();
        cs.setTimeInMillis(start);
        // int hour = cs.get(Calendar.HOUR_OF_DAY);
        List<Calendar> days = new LinkedList<Calendar>();
        cs.set(Calendar.HOUR_OF_DAY, 0);
        cs.set(Calendar.MINUTE, 0);
        cs.set(Calendar.SECOND, 0);
        cs.set(Calendar.MILLISECOND, 0);
        Calendar ce = Calendar.getInstance();
        ce.setTimeInMillis(end);
        ce.set(Calendar.HOUR_OF_DAY, 0);
        ce.set(Calendar.MINUTE, 0);
        ce.set(Calendar.SECOND, 0);
        ce.set(Calendar.MILLISECOND, 0);
        days.add(cs);
        long ONE_DAY = 1000 * 60 * 60 * 24;
        long startMillis = cs.getTimeInMillis();
        while (startMillis < ce.getTimeInMillis()) {
            //increment by one day and add to day list
            Calendar c = Calendar.getInstance();
            startMillis += ONE_DAY;
            c.setTimeInMillis(startMillis);
            days.add(c);
        }

        Collections.sort(days);
        List<Long> startEndTimes = new LinkedList<Long>();
        for (TimeSpan span : timeSpans) {
            if (span.isValidFor() == null || span.isValidFor().equals(pTribe)) {
                Date d = span.getAtDate();
                if (d != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    for (Calendar day : days) {
                        if (day.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)
                                && day.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                                && day.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                            //valid day
                            Calendar c = Calendar.getInstance();
                            c.setTime(day.getTime());
                            c.set(Calendar.HOUR_OF_DAY, span.getSpan().x);
                            startEndTimes.add(c.getTimeInMillis());
                            c.set(Calendar.HOUR_OF_DAY, span.getSpan().y);
                            c.set(Calendar.MINUTE, 59);
                            c.set(Calendar.SECOND, 59);
                            startEndTimes.add(c.getTimeInMillis());
                        }
                    }
                } else {
                    for (Calendar day : days) {
                        //valid day
                        Calendar c = Calendar.getInstance();
                        c.setTime(day.getTime());
                        c.set(Calendar.HOUR_OF_DAY, span.getSpan().x);
                        startEndTimes.add(c.getTimeInMillis());
                        c.set(Calendar.HOUR_OF_DAY, span.getSpan().y);
                        c.set(Calendar.MINUTE, 59);
                        c.set(Calendar.SECOND, 59);
                        startEndTimes.add(c.getTimeInMillis());
                    }
                }
            }
        }
        Collections.sort(startEndTimes);
        return startEndTimes;
    }

    public Date getArriveDate(long runtime) {
        Calendar sendCal = Calendar.getInstance();
        long TOLERANCE = 0 * 60 * 60 * 1000;
        long TWENTY_MINUTES = 20 * 60 * 1000;
        for (long l = start; l < end; l += TWENTY_MINUTES) {
            long sendTime = l;
            long arriveTime = sendTime + runtime;
            sendCal.setTimeInMillis(sendTime);
            int sendHour = sendCal.get(Calendar.HOUR_OF_DAY);
            int sendMinute = sendCal.get(Calendar.MINUTE);
            int sendSecond = sendCal.get(Calendar.SECOND);
            int day = sendCal.get(Calendar.DAY_OF_MONTH);
            int month = sendCal.get(Calendar.MONTH);
            int year = sendCal.get(Calendar.YEAR);

            Calendar arriveCal = Calendar.getInstance();
            arriveCal.setTimeInMillis(arriveTime);
            int arriveHour = arriveCal.get(Calendar.HOUR_OF_DAY);
            boolean inFrame = false;
            if (arriveHour >= 0 && arriveHour < 8) {
                //only possible in night bonus
            } else if (Math.abs(arriveTime - end) > TOLERANCE) {
                //too far away
            } else {


                /*  for (Point p : mFrames) {
                //check time frame parts
                inFrame = ((sendHour >= p.x) && ((sendHour <= p.y) && (sendMinute <= 59) && (sendSecond <= 59)));
                if (inFrame) {
                arriveCal.setTimeInMillis(arriveTime);
                return arriveCal.getTime();
                }
                }*/

                for (TimeSpan span : timeSpans) {
                    Date d = span.getAtDate();
                    if (d != null) {
                        //check day
                        Calendar c2 = Calendar.getInstance();
                        c2.setTime(d);
                        if (c2.get(Calendar.DAY_OF_MONTH) == day && c2.get(Calendar.MONTH) == month && c2.get(Calendar.YEAR) == year) {
                            //date is the same, so check span
                            Point p = span.getSpan();
                            inFrame = ((sendHour >= p.x) && ((sendHour <= p.y) && (sendMinute <= 59) && (sendMinute <= 59)));
                            if (inFrame) {
                                arriveCal.setTimeInMillis(arriveTime);
                                return arriveCal.getTime();
                            }
                        }
                    } else {
                        //"every day" span, so only check time
                        Point p = span.getSpan();
                        inFrame = ((sendHour >= p.x) && ((sendHour <= p.y) && (sendMinute <= 59) && (sendMinute <= 59)));
                        if (inFrame) {
                            arriveCal.setTimeInMillis(arriveTime);
                            return arriveCal.getTime();
                        }
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
