/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.types.AnyTribe;
import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.Tribe;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Torridity
 */
public class TimeFrame {

    private long startNotBefore = 0;
    private long startNotAfter = 0;
    private long arriveNotBefore = 0;
    private long arriveNotAfter = 0;
    private List<TimeSpan> sendTimeSpans = null;
    private List<TimeSpan> arriveTimeSpans = null;

    public TimeFrame( Date pStartNotBefore, Date pArriveNotBefore, Date pStartNotAfter, Date pArriveNotAfter, List<TimeSpan> pSendTimeSpans, List<TimeSpan> pArriveTimeSpans ) {
	startNotBefore = pStartNotBefore.getTime();
	startNotAfter = pStartNotAfter.getTime();
	arriveNotBefore = pArriveNotBefore.getTime();
	arriveNotAfter = pArriveNotAfter.getTime();
	sendTimeSpans = new LinkedList<TimeSpan>();
	arriveTimeSpans = new LinkedList<TimeSpan>();
	if ( pSendTimeSpans != null ) {
	    Collections.copy(sendTimeSpans, pSendTimeSpans);
	}
	if ( pArriveTimeSpans != null ) {
	    Collections.copy(arriveTimeSpans, pArriveTimeSpans);
	}
    }

    public TimeFrame( Date pStartNotBefore, Date pArriveNotBefore, Date pStartNotAfter, Date pArriveNotAfter ) {
	this(pStartNotBefore, pStartNotAfter, pArriveNotBefore, pArriveNotAfter, null, null);
    }

    public void setStartNotBefore( Date pStartNotBefore ) {
	startNotBefore = pStartNotBefore.getTime();
    }

    public void setArriveNotBefore( Date pArriveNotBefore ) {
	arriveNotBefore = pArriveNotBefore.getTime();
    }

    public boolean addStartTimeSpan( TimeSpan pSpan ) {
	for ( TimeSpan span : sendTimeSpans ) {
	    if ( span.intersects(pSpan) ) {
		return false;
	    }
	}
	sendTimeSpans.add(pSpan);
	return true;
    }

    public boolean addArriveTimeSpan( TimeSpan pSpan ) {
	for ( TimeSpan span : arriveTimeSpans ) {
	    if ( span.intersects(pSpan) ) {
		return false;
	    }
	}
	arriveTimeSpans.add(pSpan);
	return true;
    }

    public LongRange getStartRange() {
	return new LongRange(startNotBefore, startNotAfter);
    }

    public LongRange getArriveRange() {
	return new LongRange(arriveNotBefore, arriveNotAfter);
    }

    /**Check if a movement with the provided runtime is possible for this AttackFitter
     * @param pRuntime Runtime to check
     * @param pTribe Tribe for which the runtime is valid
     * @return boolean TRUE=Runtime might be fitted if not all send times are already used
     */
    public boolean isMovementPossible( long pRuntime, Tribe pTribe ) {
	List<LongRange> startRanges = startTimespansToRanges(pTribe);
	List<LongRange> arriveRanges = arriveTimespansToRanges(pTribe);

	for ( LongRange currentStartRange : startRanges ) {
	    LongRange arriveRangeForStartRange = new LongRange(currentStartRange.getMinimumLong() + pRuntime, currentStartRange.getMaximumLong() + pRuntime);
	    for ( LongRange currentArriveRange : arriveRanges ) {
		if ( currentArriveRange.overlapsRange(arriveRangeForStartRange) ) {
		    //movement with 'pRuntime' starting in 'currentStartRange' will arrive withing 'currentArriveRange'
		    return true;
		}
	    }
	}

	//no overlapping range was found
	return false;
    }

    /**Returns an arrive date that fits into this AttackFitter and is based on the provided runtime
     * @param pRuntime Runtime to fit into
     * @param pTribe Tribe for which the arrive date should be valid
     * @param pUsedSendTimes Already used send times (possible times are checked in steps of 10 seconds)
     * @return Date Fitted arrive time
     */
    public Date getFittedArriveTime( long pRuntime, Tribe pTribe, List<Long> pUsedSendTimes ) {
	List<LongRange> startRanges = startTimespansToRanges(pTribe);
	List<LongRange> arriveRanges = arriveTimespansToRanges(pTribe);

	for ( LongRange currentStartRange : startRanges ) {
	    LongRange arriveRangeForStartRange = new LongRange(currentStartRange.getMinimumLong() + pRuntime, currentStartRange.getMaximumLong() + pRuntime);
	    for ( LongRange currentArriveRange : arriveRanges ) {
		if ( currentArriveRange.overlapsRange(arriveRangeForStartRange) ) {
		    //movement possible for these 'currentStartRange' and 'currentArriveRange' so fit runtime into
		    //           |-----------|
		    //   |--------------|
		    long minArrive = currentArriveRange.getMinimumLong();
		    long minArriveForStartRange = arriveRangeForStartRange.getMinimumLong();
		    long checkStart = 0;
		    if ( minArrive < minArriveForStartRange ) {

			//|----------- (Arrive)
			//   |-------------- (ArriveForStart)
			//check everything beginning with 'minArriveForStartRange'
			checkStart = minArriveForStartRange;
		    } else if ( minArriveForStartRange <= minArrive ) {

			//     |----------- (Arrive)
			//|-------------- (ArriveForStart)
			//check everything beginning with 'minArrive'
			checkStart = minArrive;
		    }
		    long maxArrive = currentArriveRange.getMaximumLong();
		    long maxArriveForStartRange = arriveRangeForStartRange.getMaximumLong();
		    long checkEnd = 0;
		    if ( maxArrive < maxArriveForStartRange ) {
			//-----------| (Arrive)
			//---------------| (ArriveForStart)
			//check everything until 'maxArrive'
			checkEnd = maxArrive;
		    } else if ( maxArriveForStartRange <= maxArrive ) {
			//-------------| (Arrive)
			//---------| (ArriveForStart)
			//check everything until 'maxArriveForStartRange'
			checkEnd = maxArriveForStartRange;
		    }

		    int cnt = 0;
		    while ( cnt < 100 ) {
			long arriveTime = checkStart + Math.round(Math.random() * (checkEnd - checkStart));
			if ( cnt > 100 || !pUsedSendTimes.contains(arriveTime - pRuntime) ) {
			    pUsedSendTimes.add(arriveTime - pRuntime);
			    return new Date(arriveTime);
			}
			cnt++;
		    }

		    /* for (long arriveTime = checkStart; arriveTime <= checkEnd; arriveTime += 10000) {
		    if (!pUsedSendTimes.contains(arriveTime - pRuntime)) {
		    pUsedSendTimes.add(arriveTime - pRuntime);
		    return new Date(arriveTime);
		    }
		    }*/
		}
	    }
	}
	return null;
    }

    public List<LongRange> startTimespansToRanges( Tribe pTribe ) {
	List<LongRange> ranges = new LinkedList<LongRange>();
	Date startDate = DateUtils.truncate(new Date(startNotBefore), Calendar.DAY_OF_MONTH);

	for ( TimeSpan span : sendTimeSpans ) {
	    Date onlyAtDay = span.getAtDate();
	    Date thisDate = new Date(startDate.getTime());
	    //check if span is valid for provided tribe
	    if ( pTribe == null || pTribe.equals(AnyTribe.getSingleton()) || span.isValidForTribe(pTribe) ) {
		//go through all days from start to end
		while ( thisDate.getTime() < startNotAfter ) {
		    if ( onlyAtDay == null || DateUtils.isSameDay(thisDate, onlyAtDay) ) {
			//span is valid for every day or this day equals the only valid day
			Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
			Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger() - 1);
			spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
			spanEndDate = DateUtils.setSeconds(spanEndDate, 59);
			//check span location relative to start frame
			if ( spanStartDate.getTime() >= startNotBefore && spanEndDate.getTime() > startNotBefore
			     && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() <= startNotAfter ) {
			    //|----------| (startNotBefore - startNotAfter)
			    //  |----| (SpanStart - SpanEnd)
			    ranges.add(new LongRange(spanStartDate.getTime(), spanEndDate.getTime()));
			} else if ( spanStartDate.getTime() < startNotBefore && spanEndDate.getTime() > startNotBefore
					&& spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() <= startNotAfter ) {
			    //  |----------| (startNotBefore - startNotAfter)
			    //|----| (SpanStart - SpanEnd)
			    //set span start to 'startNotBefore'
			    ranges.add(new LongRange(startNotBefore, spanEndDate.getTime()));
			} else if ( spanStartDate.getTime() <= startNotBefore && spanEndDate.getTime() > startNotBefore
					&& spanStartDate.getTime() > startNotAfter && spanEndDate.getTime() >= startNotAfter ) {
			    //  |----------| (startNotBefore - startNotAfter)
			    //|--------------| (SpanStart - SpanEnd)
			    //set span start to 'startNotBefore'
			    ranges.add(new LongRange(startNotBefore, startNotAfter));
			} else if ( spanStartDate.getTime() >= startNotBefore && spanEndDate.getTime() > startNotBefore
					&& spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() >= startNotAfter ) {
			    //|----------| (startNotBefore - startNotAfter)
			    //    |---------| (SpanStart - SpanEnd)
			    //set span start to 'startNotBefore'
			    ranges.add(new LongRange(spanStartDate.getTime(), startNotAfter));
			}
		    }
		    //increment current date by one day
		    thisDate = DateUtils.addDays(thisDate, 1);
		}
	    }

	}
	Collections.sort(ranges, new Comparator<LongRange>() {

	    @Override
	    public int compare( LongRange o1, LongRange o2 ) {
		return new Long(o1.getMinimumLong()).compareTo(new Long(o2.getMinimumLong()));
	    }

	});
	return ranges;
    }

    public HashMap<LongRange, TimeSpan> startTimespansToRangesMap( Tribe pTribe ) {
	HashMap<LongRange, TimeSpan> rangesMap = new HashMap<LongRange, TimeSpan>();
	Date startDate = DateUtils.truncate(new Date(startNotBefore), Calendar.DAY_OF_MONTH);

	for ( TimeSpan span : sendTimeSpans ) {
	    Date onlyAtDay = span.getAtDate();
	    Date thisDate = new Date(startDate.getTime());
	    //check if span is valid for provided tribe
	    if ( pTribe == null || pTribe.equals(AnyTribe.getSingleton()) || span.isValidForTribe(pTribe) ) {
		//go through all days from start to end

		while ( thisDate.getTime() < startNotAfter ) {
		    if ( onlyAtDay == null || DateUtils.isSameDay(thisDate, onlyAtDay) ) {
			//span is valid for every day or this day equals the only valid day
			Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
			Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger() - 1);
			spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
			spanEndDate = DateUtils.setSeconds(spanEndDate, 59);
			//check span location relative to start frame
			if ( spanStartDate.getTime() >= startNotBefore && spanEndDate.getTime() > startNotBefore
			     && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() <= startNotAfter ) {
			    //|----------| (startNotBefore - startNotAfter)
			    //  |----| (SpanStart - SpanEnd)
			    rangesMap.put(new LongRange(spanStartDate.getTime(), spanEndDate.getTime()), span);
			} else if ( spanStartDate.getTime() < startNotBefore && spanEndDate.getTime() > startNotBefore
					&& spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() <= startNotAfter ) {
			    //  |----------| (startNotBefore - startNotAfter)
			    //|----| (SpanStart - SpanEnd)
			    //set span start to 'startNotBefore'
			    rangesMap.put(new LongRange(startNotBefore, spanEndDate.getTime()), span);
			} else if ( spanStartDate.getTime() <= startNotBefore && spanEndDate.getTime() > startNotBefore
					&& spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() >= startNotAfter ) {
			    //  |----------| (startNotBefore - startNotAfter)
			    //|--------------| (SpanStart - SpanEnd)
			    //set span start to 'startNotBefore'
			    rangesMap.put(new LongRange(startNotBefore, startNotAfter), span);
			} else if ( spanStartDate.getTime() >= startNotBefore && spanEndDate.getTime() > startNotBefore
					&& spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() >= startNotAfter ) {
			    //|----------| (startNotBefore - startNotAfter)
			    //    |---------| (SpanStart - SpanEnd)
			    //set span start to 'startNotBefore'
			    rangesMap.put(new LongRange(spanStartDate.getTime(), startNotAfter), span);
			}
		    }
		    //increment current date by one day
		    thisDate = DateUtils.addDays(thisDate, 1);
		}
	    }
	}

	return rangesMap;
    }

    public List<LongRange> arriveTimespansToRanges( Tribe pTribe ) {
	List<LongRange> ranges = new LinkedList<LongRange>();
	Date arriveDate = DateUtils.truncate(new Date(arriveNotBefore), Calendar.DAY_OF_MONTH);

	for ( TimeSpan span : arriveTimeSpans ) {
	    Date thisDate = new Date(arriveDate.getTime());
	    //check if span is valid for provided tribe
	    if ( pTribe == null || pTribe.equals(AnyTribe.getSingleton()) || span.isValidForTribe(pTribe) ) {
		//go through all days from start to end
		while ( thisDate.getTime() < arriveNotAfter ) {
		    Date onlyValidAtDay = span.getAtDate();
		    //check if span is valid on every day or if we check the span's day of validity
		    //(if we do so, the span should not be valid for an exact date because then we don't have a timespan)
		    if ( span.isValidAtEveryDay() || (DateUtils.isSameDay(thisDate, onlyValidAtDay) && !span.isValidAtExactTime()) ) {
			//span is valid for every day or this day equals the only valid day
			Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
			//set end date to last second in end hour
			Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger() - 1);
			spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
			spanEndDate = DateUtils.setSeconds(spanEndDate, 59);


			if ( spanStartDate.getTime() >= arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
			     && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() <= arriveNotAfter ) {
			    //|----------| (arriveNotBefore - arriveNotAfter)
			    //  |----| (SpanStart - SpanEnd)
			    ranges.add(new LongRange(spanStartDate.getTime(), spanEndDate.getTime()));
			} else if ( spanStartDate.getTime() < arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
					&& spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() <= arriveNotAfter ) {
			    //  |----------| (arriveNotBefore - arriveNotAfter)
			    //|----| (SpanStart - SpanEnd)
			    ranges.add(new LongRange(arriveNotBefore, spanEndDate.getTime()));
			} else if ( spanStartDate.getTime() < arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
					&& spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() > arriveNotAfter ) {
			    //  |----------| (arriveNotBefore - arriveNotAfter)
			    //|--------------| (SpanStart - SpanEnd)
			    ranges.add(new LongRange(arriveNotBefore, arriveNotAfter));
			} else if ( spanStartDate.getTime() >= arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
					&& spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() > arriveNotAfter ) {
			    //|----------| (arriveNotBefore - arriveNotAfter)
			    //    |---------| (SpanStart - SpanEnd)
			    ranges.add(new LongRange(spanStartDate.getTime(), arriveNotAfter));
			} else {
			    //ignore span because it is located completely outside
			}
		    } else if ( span.isValidAtExactTime() ) {
			//time span is only valid at a specific date, so check if this date is located withing arriveStart and arriveEnd
			if ( span.getAtDate().getTime() >= arriveNotBefore && span.getAtDate().getTime() <= arriveNotAfter ) {
			    //add specific date range
			    ranges.add(new LongRange(span.getAtDate().getTime(), span.getAtDate().getTime()));
			    //for exact arrival we do not increment further
			    break;
			}
		    }
		    //increment current date by one day
		    thisDate = DateUtils.addDays(thisDate, 1);
		}
	    }
	}
	Collections.sort(ranges, new Comparator<LongRange>() {

	    @Override
	    public int compare( LongRange o1, LongRange o2 ) {
		return new Long(o1.getMinimumLong()).compareTo(new Long(o2.getMinimumLong()));
	    }

	});
	return ranges;
    }

    public HashMap<LongRange, TimeSpan> arriveTimespansToRangesMap( Tribe pTribe ) {
	HashMap<LongRange, TimeSpan> rangesMap = new HashMap<LongRange, TimeSpan>();
	Date arriveDate = DateUtils.truncate(new Date(arriveNotBefore), Calendar.DAY_OF_MONTH);

	for ( TimeSpan span : arriveTimeSpans ) {
	    Date thisDate = new Date(arriveDate.getTime());
	    //check if span is valid for provided tribe
	    if ( pTribe == null || pTribe.equals(AnyTribe.getSingleton()) || span.isValidForTribe(pTribe) ) {
		//go through all days from start to end
		while ( thisDate.getTime() < arriveNotAfter ) {
		    Date onlyValidAtDay = span.getAtDate();
		    //check if span is valid on every day or if we check the span's day of validity
		    //(if we do so, the span should not be valid for an exact date because then we don't have a timespan)
		    if ( span.isValidAtEveryDay() || (DateUtils.isSameDay(thisDate, onlyValidAtDay) && !span.isValidAtExactTime()) ) {
			//span is valid for every day or this day equals the only valid day
			Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
			//set end date to last second in end hour
			Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger() - 1);
			spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
			spanEndDate = DateUtils.setSeconds(spanEndDate, 59);
			if ( spanStartDate.getTime() >= arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
			     && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() <= arriveNotAfter ) {
			    //|----------| (arriveNotBefore - arriveNotAfter)
			    //  |----| (SpanStart - SpanEnd)
			    rangesMap.put(new LongRange(spanStartDate.getTime(), spanEndDate.getTime()), span);
			} else if ( spanStartDate.getTime() < arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
					&& spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() <= arriveNotAfter ) {
			    //  |----------| (arriveNotBefore - arriveNotAfter)
			    //|----| (SpanStart - SpanEnd)
			    rangesMap.put(new LongRange(arriveNotBefore, spanEndDate.getTime()), span);
			} else if ( spanStartDate.getTime() < arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
					&& spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() > arriveNotAfter ) {
			    //  |----------| (arriveNotBefore - arriveNotAfter)
			    //|--------------| (SpanStart - SpanEnd)
			    rangesMap.put(new LongRange(arriveNotBefore, arriveNotAfter), span);
			} else if ( spanStartDate.getTime() >= arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
					&& spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() > arriveNotAfter ) {
			    //|----------| (arriveNotBefore - arriveNotAfter)
			    //    |---------| (SpanStart - SpanEnd)
			    rangesMap.put(new LongRange(spanStartDate.getTime(), arriveNotAfter), span);
			} else {
			    //ignore span because it is located completely outside
			}
		    } else if ( span.isValidAtExactTime() ) {
			//time span is only valid at a specific date, so check if this date is located withing arriveStart and arriveEnd
			if ( span.getAtDate().getTime() >= arriveNotBefore && span.getAtDate().getTime() <= arriveNotAfter ) {
			    //add specific date range
			    rangesMap.put(new LongRange(span.getAtDate().getTime(), span.getAtDate().getTime()), span);
			    //for exact arrival we do not increment further
			    break;
			}
		    }
		    //increment current date by one day
		    thisDate = DateUtils.addDays(thisDate, 1);
		}
	    }
	}

	return rangesMap;
    }

    public static void main( String[] args ) throws Exception {
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
	SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

	/* end = DateUtils.setHours(end, 0);
	end = DateUtils.setMinutes(end, 0);
	end = DateUtils.setSeconds(end, 0);
	end = DateUtils.setMilliseconds(end, 0);*/

	TimeFrame frame = new TimeFrame(f.parse("28.12.2010 13:00:00"),
					f.parse("29.12.2010 15:00:00"),
					f.parse("29.12.2010 15:00:00"),
					f.parse("30.12.2010 15:30:00"));
	frame.addStartTimeSpan(new TimeSpan(new IntRange(10, 11)));
	//frame.addStartTimeSpan(new TimeSpan(f.parse("29.12.2010 00:00:00"), new IntRange(11, 12)));

	// frame.addStartTimeSpan(new TimeSpan(f.parse("30.12.2010 13:41:14")));
	System.out.println("Start: " + f.format(frame.startNotBefore) + " - " + f.format(frame.startNotAfter));
	System.out.println("End: " + f.format(frame.arriveNotBefore) + " - " + f.format(frame.arriveNotAfter));
	List<LongRange> spans = frame.startTimespansToRanges(null);
	System.out.println("Start:");
	for ( LongRange range : spans ) {
	    System.out.println(f.format(new Date(range.getMinimumLong())) + " - " + f.format(new Date(range.getMaximumLong())));
	}
	System.out.println("Arrive:");
	spans = frame.arriveTimespansToRanges(null);

	for ( LongRange range : spans ) {
	    System.out.println(f.format(new Date(range.getMinimumLong())) + " - " + f.format(new Date(range.getMaximumLong())));
	}
	long pRuntime = DateUtils.MILLIS_PER_DAY;
	System.out.println("Possible: " + frame.isMovementPossible(pRuntime, null));
	List<Long> sendDates = new LinkedList<Long>();
	/* for (int i = 0; i < 10; i++) {

	long time = pRuntime + (long) Math.round(10 * Math.random()) * 1000l;
	System.out.println("Possible: " + frame.isMovementPossible(time, null));
	System.out.println("Runtime: " + time);
	Date d = frame.getFittedArriveTime(time, null, sendDates);
	if (d != null) {
	System.out.println("Arrive: " + f.format(d));
	System.out.println("Send: " + f.format(new Date(d.getTime() - time)));
	} else {
	System.out.println("NO ARRIVE");
	}
	System.out.println("-------------");
	}*/
    }

    public String toString() {
	StringBuilder builder = new StringBuilder(200);
	builder.append("SendSpans: ").append(sendTimeSpans).append("\n");
	builder.append("ArriveSpans: ").append(arriveTimeSpans).append("\n");
	return builder.toString().trim();
    }

    /* public static void main( String[] args ) throws Exception {
    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    Date start1 = f.parse("15.02.11 08:00:00");
    Date start2 = f.parse("15.02.11 14:00:00");
    Date arrive1 = f.parse("16.02.11 08:00:00");
    Date arrive2 = f.parse("16.02.11 14:00:00");
    TimeFrame frame = new TimeFrame(start1, arrive1, start2, arrive2);



    }*/
}
