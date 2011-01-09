/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.Tribe;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.time.DateUtils;

/**
 * @author Jejkal
 */
public class TimeFrame_old {

    private long start = 0;
    private long end = 0;
    private List<TimeSpan> timeSpans = null;
    private List<TimeSpan> arriveTimeSpans = null;
    private boolean variableArriveTime = false;
    private int variableArriveStartHour = -1;
    private int variableArriveEndHour = -1;

    public TimeFrame_old(Date pStart, Date pEnd, int pMinHour, int pMaxHour) {
        start = pStart.getTime();
        end = pEnd.getTime();
        timeSpans = new LinkedList<TimeSpan>();
        arriveTimeSpans = new LinkedList<TimeSpan>();
    }

    public TimeFrame_old(Date pStart, Date pEnd) {
        start = pStart.getTime();
        end = pEnd.getTime();
        timeSpans = new LinkedList<TimeSpan>();
        arriveTimeSpans = new LinkedList<TimeSpan>();
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

    public void addArriveTimeSpan(TimeSpan pSpan) {
        arriveTimeSpans.add(pSpan);
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
                            IntRange p = span.getSpan();
                            inFrame = ((hour >= p.getMinimumInteger()) && ((hour <= p.getMaximumInteger()) && (minute <= 59) && (second <= 59)));
                            if (inFrame) {
                                break;
                            }
                        }
                    } else {
                        //"every day" span, so only check time
                        IntRange p = span.getSpan();
                        inFrame = ((hour >= p.getMinimumInteger()) && ((hour <= p.getMaximumInteger()) && (minute <= 59) && (second <= 59)));
                        if (inFrame) {
                            break;
                        }
                    }
                }
            }
        }
        return inFrame;
    }

    public Date fitInto(long pRuntime, Tribe pTribe, List<Long> usedDates) {
        List<LongRange> ranges = timespansToRanges(pTribe);

        for (LongRange range : ranges) {
            LongRange arriveRange = new LongRange(range.getMinimumLong() + pRuntime, range.getMaximumLong() + pRuntime);




        }

        return null;
    }

    public Date getRandomArriveTime(long pRuntime, Tribe pTribe, List<Long> usedDates) {
        List<Long> l = timespansToTimeframes(pTribe);

        long runtimeDays = pRuntime / DateUtils.MILLIS_PER_DAY;
        pRuntime -= (runtimeDays * DateUtils.MILLIS_PER_DAY);
        long runtimeHours = pRuntime / DateUtils.MILLIS_PER_HOUR;
        pRuntime -= (runtimeHours * DateUtils.MILLIS_PER_HOUR);
        long runtimeMinutes = pRuntime / DateUtils.MILLIS_PER_MINUTE;
        pRuntime -= (runtimeMinutes * DateUtils.MILLIS_PER_MINUTE);
        long runtimeSeconds = pRuntime / DateUtils.MILLIS_PER_SECOND;
        long runtimeMillis = pRuntime - (runtimeSeconds * DateUtils.MILLIS_PER_SECOND);
        for (TimeSpan span : timeSpans) {
            if (span.isValidFor() == null || span.isValidFor().equals(pTribe)) {
                //span is valid for this or all tribes
            }
        }

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
                            c.set(Calendar.HOUR_OF_DAY, span.getSpan().getMinimumInteger());
                            startEndTimes.add(c.getTimeInMillis());
                            c.set(Calendar.HOUR_OF_DAY, span.getSpan().getMaximumInteger());
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
                        c.set(Calendar.HOUR_OF_DAY, span.getSpan().getMinimumInteger());
                        startEndTimes.add(c.getTimeInMillis());
                        c.set(Calendar.HOUR_OF_DAY, span.getSpan().getMaximumInteger());
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

    private List<LongRange> timespansToRanges(Tribe pTribe) {
        List<LongRange> ranges = new LinkedList<LongRange>();
        Date startDate = new Date(start);
        startDate = DateUtils.truncate(startDate, Calendar.DAY_OF_MONTH);
        Date endDate = new Date(end);
        endDate = DateUtils.ceiling(endDate, Calendar.DAY_OF_MONTH);

        for (TimeSpan span : timeSpans) {
            Date onlyAtDay = span.getAtDate();
            Date thisDate = new Date(startDate.getTime());
            //check if span is valid for provided tribe
            if (pTribe == null || span.isValidForTribe(pTribe)) {
                //go through all days from start to end
                while (thisDate.getTime() < endDate.getTime()) {
                    if (onlyAtDay == null || DateUtils.isSameDay(thisDate, onlyAtDay)) {
                        //span is valid for every day or this day equals the only valid day
                        Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
                        Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger());
                        spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
                        spanEndDate = DateUtils.setSeconds(spanEndDate, 59);
                        if (spanStartDate.getTime() > start && spanEndDate.getTime() < end) {
                            ranges.add(new LongRange(spanStartDate.getTime(), spanEndDate.getTime()));
                        }
                    }
                    //increment current date by one day
                    thisDate = DateUtils.addDays(thisDate, 1);
                }
            }
        }

        return ranges;
    }

    private List<LongRange> arriveTimespansToRanges(Tribe pTribe) {
        List<LongRange> ranges = new LinkedList<LongRange>();
        Date startDate = new Date();
        startDate = DateUtils.truncate(startDate, Calendar.DAY_OF_MONTH);
        Date endDate = new Date(end);
        endDate = DateUtils.ceiling(endDate, Calendar.DAY_OF_MONTH);

        for (TimeSpan span : arriveTimeSpans) {
            Date onlyAtDay = span.getAtDate();
            Date thisDate = new Date(startDate.getTime());
            //check if span is valid for provided tribe
            if (pTribe == null || span.isValidForTribe(pTribe)) {
                //go through all days from start to end
                while (thisDate.getTime() < endDate.getTime()) {
                    if (onlyAtDay == null || DateUtils.isSameDay(thisDate, onlyAtDay)) {
                        //span is valid for every day or this day equals the only valid day
                        Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
                        Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger());
                        spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
                        spanEndDate = DateUtils.setSeconds(spanEndDate, 59);
                        if (spanStartDate.getTime() > start && spanEndDate.getTime() < end) {
                            ranges.add(new LongRange(spanStartDate.getTime(), spanEndDate.getTime()));
                        }
                    }
                    //increment current date by one day
                    thisDate = DateUtils.addDays(thisDate, 1);
                }
            }
        }

        return ranges;
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
                            IntRange p = span.getSpan();
                            inFrame = ((sendHour >= p.getMinimumInteger()) && ((sendHour <= p.getMaximumInteger()) && (sendMinute <= 59) && (sendMinute <= 59)));
                            if (inFrame) {
                                arriveCal.setTimeInMillis(arriveTime);
                                return arriveCal.getTime();
                            }
                        }
                    } else {
                        //"every day" span, so only check time
                        IntRange p = span.getSpan();
                        inFrame = ((sendHour >= p.getMinimumInteger()) && ((sendHour <= p.getMaximumInteger()) && (sendMinute <= 59) && (sendMinute <= 59)));
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

    public static void main(String[] args) {
        /* long pRuntime = DateUtils.MILLIS_PER_DAY + 2 * DateUtils.MILLIS_PER_HOUR + 30 * DateUtils.MILLIS_PER_MINUTE + 10 * DateUtils.MILLIS_PER_SECOND + 100;
        long runtimeDays = pRuntime / DateUtils.MILLIS_PER_DAY;
        pRuntime -= (runtimeDays * DateUtils.MILLIS_PER_DAY);
        long runtimeHours = pRuntime / DateUtils.MILLIS_PER_HOUR;
        pRuntime -= (runtimeHours * DateUtils.MILLIS_PER_HOUR);
        long runtimeMinutes = pRuntime / DateUtils.MILLIS_PER_MINUTE;
        pRuntime -= (runtimeMinutes * DateUtils.MILLIS_PER_MINUTE);
        long runtimeSeconds = pRuntime / DateUtils.MILLIS_PER_SECOND;
        long runtimeMillis = pRuntime - (runtimeSeconds * DateUtils.MILLIS_PER_SECOND);
        Date startDate = new Date(System.currentTimeMillis());
        startDate = DateUtils.truncate(startDate, Calendar.DAY_OF_MONTH);

        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        System.out.println(f.format(startDate));*/
        // System.out.println(runtimeDays + " " + runtimeHours + " " + runtimeMinutes + " " + runtimeSeconds + " " + runtimeMillis);
        Date start = DateUtils.addDays(Calendar.getInstance().getTime(), -2);
        Date end = DateUtils.addDays(Calendar.getInstance().getTime(), 2);
        end = DateUtils.setHours(end, 0);
        end = DateUtils.setMinutes(end, 0);
        end = DateUtils.setSeconds(end, 0);
        end = DateUtils.setMilliseconds(end, 0);

        TimeFrame_old frame = new TimeFrame_old(start, end);
        frame.addTimeSpan(new TimeSpan(new IntRange(15, 16)));
        frame.addTimeSpan(new TimeSpan(new IntRange(17, 19)));
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        System.out.println("Start: " + f.format(start));
        System.out.println("End: " + f.format(end));
        for (LongRange range : frame.timespansToRanges(null)) {
            System.out.println(f.format(new Date(range.getMinimumLong())) + " - " + f.format(new Date(range.getMaximumLong())));
        }

    }
}
