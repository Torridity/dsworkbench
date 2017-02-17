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
package de.tor.tribes.util.algo.types;

import de.tor.tribes.types.DefenseTimeSpan;
import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.test.AnyTribe;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.*;

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
  List<LongRange> startRanges = null;
  List<LongRange> arriveRanges = null;
  //This is needed, because the we cant send more than one movement at the same time
  private final long fixedStartTimeRangeSize = 1000;

  public TimeFrame(Date pStartNotBefore, Date pArriveNotBefore, Date pStartNotAfter, Date pArriveNotAfter, List<TimeSpan> pSendTimeSpans, List<TimeSpan> pArriveTimeSpans) {
    startNotBefore = pStartNotBefore.getTime();
    startNotAfter = pStartNotAfter.getTime();
    arriveNotBefore = pArriveNotBefore.getTime();
    arriveNotAfter = pArriveNotAfter.getTime();
    sendTimeSpans = new LinkedList<>();
    arriveTimeSpans = new LinkedList<>();
    if (pSendTimeSpans != null) {
      Collections.copy(sendTimeSpans, pSendTimeSpans);
    }
    if (pArriveTimeSpans != null) {
      Collections.copy(arriveTimeSpans, pArriveTimeSpans);
    }
  }

  public TimeFrame(Date pStartNotBefore, Date pArriveNotBefore, Date pStartNotAfter, Date pArriveNotAfter) {
    this(pStartNotBefore, pArriveNotBefore, pStartNotAfter, pArriveNotAfter, null, null);
  }

  public void setStartNotBefore(Date pStartNotBefore) {
    startNotBefore = pStartNotBefore.getTime();
  }

  public void setArriveNotBefore(Date pArriveNotBefore) {
    arriveNotBefore = pArriveNotBefore.getTime();
  }

  public boolean addStartTimeSpan(TimeSpan pSpan) {
    for (TimeSpan span : sendTimeSpans) {
      if (span.intersects(pSpan)) {
        return false;
      }
    }
    sendTimeSpans.add(pSpan);
    startRanges = null;
    return true;
  }

  public boolean addArriveTimeSpan(DefenseTimeSpan pSpan) {
    arriveTimeSpans.add(pSpan);
    arriveRanges = null;
    return true;
  }

  public boolean addArriveTimeSpan(TimeSpan pSpan) {
    for (TimeSpan span : arriveTimeSpans) {
      if (span.intersects(pSpan)) {
        return false;
      }
    }
    arriveTimeSpans.add(pSpan);
    arriveRanges = null;
    return true;
  }

  public LongRange getStartRange() {
    return new LongRange(startNotBefore, startNotAfter);
  }

  public LongRange getArriveRange() {
    return new LongRange(arriveNotBefore, arriveNotAfter);
  }

  /**
   * Check if a movement with the provided runtime is possible for this
   * AttackFitter
   *
   * @param pRuntime Runtime to check
   * @param pVillage Village for which the runtime is valid
   * @return boolean TRUE=Runtime might be fitted if not all send times are
   * already used
   */
  public boolean isMovementPossible(long pRuntime, de.tor.tribes.types.ext.Village pVillage) {
    if (startRanges == null) {
      startRanges = startTimespansToRanges(pVillage.getTribe());
    }
    if (arriveRanges == null) {
      arriveRanges = arriveTimespansToRanges(pVillage);
    }

    for (LongRange currentStartRange : startRanges) {
      LongRange arriveRangeForStartRange = new LongRange(currentStartRange.getMinimumLong() + pRuntime, currentStartRange.getMaximumLong() + pRuntime);
      for (LongRange currentArriveRange : arriveRanges) {
        if (currentArriveRange.overlapsRange(arriveRangeForStartRange)) {
          //movement with 'pRuntime' starting in 'currentStartRange' will arrive withing 'currentArriveRange'
          return true;
        }
      }
    }
    //no overlapping range was found
    return false;
  }

  /**
   * Returns an arrive date that fits into this AttackFitter and is based on the
   * provided runtime
   *
   * @param pRuntime Runtime to fit into
   * @param pVillage Village for which the arrive date should be valid
   * @param pUsedSendTimes Already used send times (possible times are checked
   * in steps of 10 seconds). This argument may be 'null'. Then send times are
   * not checked.
   * @return Date Fitted arrive time
   */
  public Date getFittedArriveTime(long pRuntime, de.tor.tribes.types.ext.Village pVillage, List<Long> pUsedSendTimes) {
    List<LongRange> startRanges = startTimespansToRanges(pVillage.getTribe());
    List<LongRange> arriveRanges = arriveTimespansToRanges(pVillage);
    for (LongRange currentStartRange : startRanges) {
      LongRange arriveRangeForStartRange = new LongRange(currentStartRange.getMinimumLong() + pRuntime, currentStartRange.getMaximumLong() + pRuntime);
      for (LongRange currentArriveRange : arriveRanges) {
        if (currentArriveRange.overlapsRange(arriveRangeForStartRange)) {
          //movement possible for these 'currentStartRange' and 'currentArriveRange' so fit runtime into
          //           |-----------|
          //   |--------------|
          long minArrive = currentArriveRange.getMinimumLong();
          long minArriveForStartRange = arriveRangeForStartRange.getMinimumLong();
          long checkStart = 0;
          if (minArrive < minArriveForStartRange) {
            //|----------- (Arrive)
            //   |-------------- (ArriveForStart)
            //check everything beginning with 'minArriveForStartRange'
            checkStart = minArriveForStartRange;
          } else if (minArriveForStartRange <= minArrive) {
            //     |----------- (Arrive)
            //|-------------- (ArriveForStart)
            //check everything beginning with 'minArrive'
            checkStart = minArrive;
          }
          long maxArrive = currentArriveRange.getMaximumLong();
          long maxArriveForStartRange = arriveRangeForStartRange.getMaximumLong();
          long checkEnd = 0;
          if (maxArrive < maxArriveForStartRange) {
            //-----------| (Arrive)
            //---------------| (ArriveForStart)
            //check everything until 'maxArrive'
            checkEnd = maxArrive;
          } else if (maxArriveForStartRange <= maxArrive) {
            //-------------| (Arrive)
            //---------| (ArriveForStart)
            //check everything until 'maxArriveForStartRange'
            checkEnd = maxArriveForStartRange;
          }

          int cnt = 0;
          while (cnt < 100) {
            long arriveTime = checkStart + Math.round(Math.random() * (checkEnd - checkStart));
            if (pUsedSendTimes == null || cnt > 100 || !pUsedSendTimes.contains(arriveTime - pRuntime)) {
              if (pUsedSendTimes != null) {
                pUsedSendTimes.add(arriveTime - pRuntime);
              }
              return new Date(arriveTime);
            }
            cnt++;
          }
        }
      }
    }
    return null;
  }

  public List<LongRange> startTimespansToRanges(Tribe pTribe) {
    List<LongRange> ranges = new LinkedList<>();
    Date startDate = DateUtils.truncate(new Date(startNotBefore), Calendar.DAY_OF_MONTH);

    for (TimeSpan span : sendTimeSpans) {
      Date onlyAtDay = span.getAtDate();
      Date thisDate = new Date(startDate.getTime());
      //check if span is valid for provided tribe
      if (pTribe == null || pTribe.equals(AnyTribe.getSingleton()) || span.isValidForTribe(pTribe)) {
        //go through all days from start to end
        while (thisDate.getTime() < startNotAfter) {
          if (onlyAtDay == null || DateUtils.isSameDay(thisDate, onlyAtDay)) {
            if(span.isValidAtExactTime()) {
              //Only one exact time
              Date spanDate = span.getDate();
              LongRange newRange = new LongRange(spanDate.getTime(), spanDate.getTime() + fixedStartTimeRangeSize);
  
              if (newRange.getMinimumLong() >= System.currentTimeMillis()) {
                //add range only if it is in future
                ranges.add(newRange);
              }
            }
            else {
              //span is valid for every day or this day equals the only valid day
              Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
              Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger() - 1);
              spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
              spanEndDate = DateUtils.setSeconds(spanEndDate, 59);
              LongRange newRange = null;
              //check span location relative to start frame
              if (spanStartDate.getTime() >= startNotBefore && spanEndDate.getTime() > startNotBefore
                      && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() <= startNotAfter) {
                //|----------| (startNotBefore - startNotAfter)
                //  |----| (SpanStart - SpanEnd)
                newRange = new LongRange(spanStartDate.getTime(), spanEndDate.getTime());
              } else if (spanStartDate.getTime() < startNotBefore && spanEndDate.getTime() > startNotBefore
                      && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() <= startNotAfter) {
                //  |----------| (startNotBefore - startNotAfter)
                //|----| (SpanStart - SpanEnd)
                //set span start to 'startNotBefore'
                newRange = new LongRange(startNotBefore, spanEndDate.getTime());
              } else if (spanStartDate.getTime() <= startNotBefore && spanEndDate.getTime() > startNotBefore
                      && spanStartDate.getTime() > startNotAfter && spanEndDate.getTime() >= startNotAfter) {
                //  |----------| (startNotBefore - startNotAfter)
                //|--------------| (SpanStart - SpanEnd)
                //set span start to 'startNotBefore'
                newRange = new LongRange(startNotBefore, startNotAfter);
              } else if (spanStartDate.getTime() >= startNotBefore && spanEndDate.getTime() > startNotBefore
                      && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() >= startNotAfter) {
                //|----------| (startNotBefore - startNotAfter)
                //    |---------| (SpanStart - SpanEnd)
                //set span start to 'startNotBefore'
                newRange = new LongRange(spanStartDate.getTime(), startNotAfter);
              }

              if (newRange != null) {
                if (newRange.getMinimumLong() < System.currentTimeMillis()) {
                  //check minimum as current minimum is in past
                  if (newRange.getMaximumLong() > System.currentTimeMillis()) {
                    newRange = new LongRange(System.currentTimeMillis(), newRange.getMaximumLong());
                    ranges.add(newRange);
                  }//ignore as entire range is in past
                } else {
                  //add range as it is in future
                  ranges.add(newRange);
                }
              }
            }
          }
          //increment current date by one day
          thisDate = DateUtils.addDays(thisDate, 1);
        }
      }

    }
    Collections.sort(ranges, new Comparator<LongRange>() {
      @Override
      public int compare(LongRange o1, LongRange o2) {
        return Long.valueOf(o1.getMinimumLong()).compareTo(o2.getMinimumLong());
      }
    });
    return ranges;
  }

  public HashMap<LongRange, TimeSpan> startTimespansToRangesMap(Tribe pTribe) {
    HashMap<LongRange, TimeSpan> rangesMap = new HashMap<>();
    Date startDate = DateUtils.truncate(new Date(startNotBefore), Calendar.DAY_OF_MONTH);

    for (TimeSpan span : sendTimeSpans) {
      Date onlyAtDay = span.getAtDate();
      Date thisDate = new Date(startDate.getTime());
      //check if span is valid for provided tribe
      // if (pTribe == null || pTribe.equals(AnyTribe.getSingleton()) || span.isValidForTribe(pTribe)) {
      //go through all days from start to end

      while (thisDate.getTime() < startNotAfter) {
        if (onlyAtDay == null || DateUtils.isSameDay(thisDate, onlyAtDay)) {
          if(span.isValidAtExactTime()) {
            //Only one exact time
            Date spanDate = span.getDate();
            LongRange newRange = new LongRange(spanDate.getTime(), spanDate.getTime() + fixedStartTimeRangeSize);
            
            if (newRange.getMinimumLong() >= System.currentTimeMillis()) {
              //add range only if it is in future
              rangesMap.put(newRange, span);
            }
          }
          else {
            //span is valid for every day or this day equals the only valid day
            Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
            Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger() - 1);
            spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
            spanEndDate = DateUtils.setSeconds(spanEndDate, 59);
            LongRange newRange = null;
            //check span location relative to start frame
            if (spanStartDate.getTime() >= startNotBefore && spanEndDate.getTime() > startNotBefore
                    && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() <= startNotAfter) {
              //|----------| (startNotBefore - startNotAfter)
              //  |----| (SpanStart - SpanEnd)
              newRange = new LongRange(spanStartDate.getTime(), spanEndDate.getTime());
            } else if (spanStartDate.getTime() < startNotBefore && spanEndDate.getTime() > startNotBefore
                    && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() <= startNotAfter) {
              //  |----------| (startNotBefore - startNotAfter)
              //|----| (SpanStart - SpanEnd)
              //set span start to 'startNotBefore'
              newRange = new LongRange(startNotBefore, spanEndDate.getTime());
            } else if (spanStartDate.getTime() <= startNotBefore && spanEndDate.getTime() > startNotBefore
                    && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() >= startNotAfter) {
              //  |----------| (startNotBefore - startNotAfter)
              //|--------------| (SpanStart - SpanEnd)
              //set span start to 'startNotBefore'
              newRange = new LongRange(startNotBefore, startNotAfter);
            } else if (spanStartDate.getTime() >= startNotBefore && spanEndDate.getTime() > startNotBefore
                    && spanStartDate.getTime() < startNotAfter && spanEndDate.getTime() >= startNotAfter) {
              //|----------| (startNotBefore - startNotAfter)
              //    |---------| (SpanStart - SpanEnd)
              //set span start to 'startNotBefore'
              newRange = new LongRange(spanStartDate.getTime(), startNotAfter);
            }
            if (newRange != null) {
              if (newRange.getMinimumLong() < System.currentTimeMillis()) {
                //check minimum as current minimum is in past
                if (newRange.getMaximumLong() > System.currentTimeMillis()) {
                  newRange = new LongRange(System.currentTimeMillis(), newRange.getMaximumLong());
                  rangesMap.put(newRange, span);
                }//ignore as entire range is in past
              } else {
                //add range as it is in future
                rangesMap.put(newRange, span);
              }
            }
          }
        }
        //increment current date by one day
        thisDate = DateUtils.addDays(thisDate, 1);
      }
      // }
    }

    return rangesMap;
  }

  public List<LongRange> arriveTimespansToRanges(de.tor.tribes.types.ext.Village pVillage) {
    List<LongRange> ranges = new LinkedList<>();
    Date arriveDate = DateUtils.truncate(new Date(arriveNotBefore), Calendar.DAY_OF_MONTH);

    for (TimeSpan span : arriveTimeSpans) {
      Date thisDate = new Date(arriveDate.getTime());
      //go through all days from start to end
      while (thisDate.getTime() < arriveNotAfter) {
        Date onlyValidAtDay = span.getAtDate();
        //check if span is valid on every day or if we check the span's day of validity
        //(if we do so, the span should not be valid for an exact date because then we don't have a timespan)
        if (span.isValidAtEveryDay() || (onlyValidAtDay != null && DateUtils.isSameDay(thisDate, onlyValidAtDay) && !span.isValidAtExactTime())) {
          //span is valid for every day or this day equals the only valid day
          Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
          //set end date to last second in end hour
          Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger() - 1);
          spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
          spanEndDate = DateUtils.setSeconds(spanEndDate, 59);

          if (spanStartDate.getTime() >= arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
                  && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() <= arriveNotAfter) {
            //|----------| (arriveNotBefore - arriveNotAfter)
            //  |----| (SpanStart - SpanEnd)
            ranges.add(new LongRange(spanStartDate.getTime(), spanEndDate.getTime()));
          } else if (spanStartDate.getTime() < arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
                  && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() <= arriveNotAfter) {
            //  |----------| (arriveNotBefore - arriveNotAfter)
            //|----| (SpanStart - SpanEnd)
            ranges.add(new LongRange(arriveNotBefore, spanEndDate.getTime()));
          } else if (spanStartDate.getTime() < arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
                  && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() > arriveNotAfter) {
            //  |----------| (arriveNotBefore - arriveNotAfter)
            //|--------------| (SpanStart - SpanEnd)
            ranges.add(new LongRange(arriveNotBefore, arriveNotAfter));
          } else if (spanStartDate.getTime() >= arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
                  && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() > arriveNotAfter) {
            //|----------| (arriveNotBefore - arriveNotAfter)
            //    |---------| (SpanStart - SpanEnd)
            ranges.add(new LongRange(spanStartDate.getTime(), arriveNotAfter));
          } else {
            //ignore span because it is located completely outside
          }
        } else if (span.isValidAtExactTime()) {
          //time span is only valid at a specific date, so check if this date is located withing arriveStart and arriveEnd
          if (span.getAtDate().getTime() >= arriveNotBefore && span.getAtDate().getTime() <= arriveNotAfter) {
            //add specific date range
            ranges.add(new LongRange(span.getAtDate().getTime(), span.getAtDate().getTime()));
            //for exact arrival we do not increment further
            break;
          }
        } else if (span.isValid() && span instanceof DefenseTimeSpan && ((DefenseTimeSpan) span).isValidForVillage(pVillage)) {
          ranges.add(((DefenseTimeSpan) span).getDefenseSpan());
        }
        //increment current date by one day
        thisDate = DateUtils.addDays(thisDate, 1);
      }
    }
    Collections.sort(ranges, new Comparator<LongRange>() {
      @Override
      public int compare(LongRange o1, LongRange o2) {
        return Long.valueOf(o1.getMinimumLong()).compareTo(o2.getMinimumLong());
      }
    });
    return ranges;
  }

  public HashMap<LongRange, TimeSpan> arriveTimespansToRangesMap(de.tor.tribes.types.ext.Village pVillage) {
    HashMap<LongRange, TimeSpan> rangesMap = new HashMap<>();
    Date arriveDate = DateUtils.truncate(new Date(arriveNotBefore), Calendar.DAY_OF_MONTH);

    for (TimeSpan span : arriveTimeSpans) {
      Date thisDate = new Date(arriveDate.getTime());
      //go through all days from start to end
      while (thisDate.getTime() < arriveNotAfter) {
        Date onlyValidAtDay = span.getAtDate();
        //check if span is valid on every day or if we check the span's day of validity
        //(if we do so, the span should not be valid for an exact date because then we don't have a timespan)
        if (span.isValidAtEveryDay() || (DateUtils.isSameDay(thisDate, onlyValidAtDay) && !span.isValidAtExactTime())) {
          //span is valid for every day or this day equals the only valid day
          Date spanStartDate = DateUtils.setHours(thisDate, span.getSpan().getMinimumInteger());
          //set end date to last second in end hour
          Date spanEndDate = DateUtils.setHours(thisDate, span.getSpan().getMaximumInteger() - 1);
          spanEndDate = DateUtils.setMinutes(spanEndDate, 59);
          spanEndDate = DateUtils.setSeconds(spanEndDate, 59);
          if (spanStartDate.getTime() >= arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
                  && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() <= arriveNotAfter) {
            //|----------| (arriveNotBefore - arriveNotAfter)
            //  |----| (SpanStart - SpanEnd)
            rangesMap.put(new LongRange(spanStartDate.getTime(), spanEndDate.getTime()), span);
          } else if (spanStartDate.getTime() < arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
                  && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() <= arriveNotAfter) {
            //  |----------| (arriveNotBefore - arriveNotAfter)
            //|----| (SpanStart - SpanEnd)
            rangesMap.put(new LongRange(arriveNotBefore, spanEndDate.getTime()), span);
          } else if (spanStartDate.getTime() < arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
                  && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() > arriveNotAfter) {
            //  |----------| (arriveNotBefore - arriveNotAfter)
            //|--------------| (SpanStart - SpanEnd)
            rangesMap.put(new LongRange(arriveNotBefore, arriveNotAfter), span);
          } else if (spanStartDate.getTime() >= arriveNotBefore && spanEndDate.getTime() > arriveNotBefore
                  && spanStartDate.getTime() < arriveNotAfter && spanEndDate.getTime() > arriveNotAfter) {
            //|----------| (arriveNotBefore - arriveNotAfter)
            //    |---------| (SpanStart - SpanEnd)
            rangesMap.put(new LongRange(spanStartDate.getTime(), arriveNotAfter), span);
          } else {
            //ignore span because it is located completely outside
          }
        } else if (span.isValidAtExactTime()) {
          //time span is only valid at a specific date, so check if this date is located withing arriveStart and arriveEnd
          if (span.getAtDate().getTime() >= arriveNotBefore && span.getAtDate().getTime() <= arriveNotAfter) {
            //add specific date range
            rangesMap.put(new LongRange(span.getAtDate().getTime(), span.getAtDate().getTime()), span);
            //for exact arrival we do not increment further
            break;
          }
        } else if (span.isValid() && span instanceof DefenseTimeSpan && ((DefenseTimeSpan) span).isValidForVillage(pVillage)) {
          rangesMap.put(((DefenseTimeSpan) span).getDefenseSpan(), span);
        }
        //increment current date by one day
        thisDate = DateUtils.addDays(thisDate, 1);
      }
    }

    return rangesMap;
  }

  public static void main(String[] args) throws Exception {


    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    Date start1 = f.parse("07.09.11 01:44:38");
    Date start2 = f.parse("08.09.11 01:44:37");
    Date arrive1 = f.parse("07.09.11 16:00:00");
    Date arrive2 = f.parse("07.09.11 16:00:00");

    LongRange r1 = new LongRange(start1.getTime(), start2.getTime());
    LongRange r2 = new LongRange(arrive1.getTime(), arrive2.getTime());
    System.out.println(r1.overlapsRange(r2));
    //TimeFrame frame = new TimeFrame(start1, arrive1, start2, arrive2);


    return;







    /*
     * long pRuntime = DateUtils.MILLIS_PER_DAY + 2 * DateUtils.MILLIS_PER_HOUR + 30 * DateUtils.MILLIS_PER_MINUTE + 10 *
     * DateUtils.MILLIS_PER_SECOND + 100; long runtimeDays = pRuntime / DateUtils.MILLIS_PER_DAY; pRuntime -= (runtimeDays *
     * DateUtils.MILLIS_PER_DAY); long runtimeHours = pRuntime / DateUtils.MILLIS_PER_HOUR; pRuntime -= (runtimeHours *
     * DateUtils.MILLIS_PER_HOUR); long runtimeMinutes = pRuntime / DateUtils.MILLIS_PER_MINUTE; pRuntime -= (runtimeMinutes *
     * DateUtils.MILLIS_PER_MINUTE); long runtimeSeconds = pRuntime / DateUtils.MILLIS_PER_SECOND; long runtimeMillis = pRuntime -
     * (runtimeSeconds * DateUtils.MILLIS_PER_SECOND); Date startDate = new Date(System.currentTimeMillis()); startDate =
     * DateUtils.truncate(startDate, Calendar.DAY_OF_MONTH);
     *
     * SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss"); System.out.println(f.format(startDate));
     */
    // System.out.println(runtimeDays + " " + runtimeHours + " " + runtimeMinutes + " " + runtimeSeconds + " " + runtimeMillis);
    //SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

    /*
     * end = DateUtils.setHours(end, 0); end = DateUtils.setMinutes(end, 0); end = DateUtils.setSeconds(end, 0); end =
     * DateUtils.setMilliseconds(end, 0);
     */

    // TimeFrame frame = new TimeFrame(f.parse("28.12.2010 13:00:00"),
    //        f.parse("29.12.2010 15:00:00"),
    //        f.parse("29.12.2010 15:00:00"),
    //        f.parse("30.12.2010 15:30:00"));
    //frame.addStartTimeSpan(new TimeSpan(new IntRange(10, 11)));
    //frame.addStartTimeSpan(new TimeSpan(f.parse("29.12.2010 00:00:00"), new IntRange(11, 12)));

    // frame.addStartTimeSpan(new TimeSpan(f.parse("30.12.2010 13:41:14")));
    // System.out.println("Start: " + f.format(frame.startNotBefore) + " - " + f.format(frame.startNotAfter));
    // System.out.println("End: " + f.format(frame.arriveNotBefore) + " - " + f.format(frame.arriveNotAfter));
    // List<LongRange> spans = frame.startTimespansToRanges(null);
    // System.out.println("Start:");
    // for (LongRange range : spans) {
    //   System.out.println(f.format(new Date(range.getMinimumLong())) + " - " + f.format(new Date(range.getMaximumLong())));
    // }
    // System.out.println("Arrive:");
    // spans = frame.arriveTimespansToRanges(null);

    // for (LongRange range : spans) {
    //   System.out.println(f.format(new Date(range.getMinimumLong())) + " - " + f.format(new Date(range.getMaximumLong())));
    // }
    // long pRuntime = DateUtils.MILLIS_PER_DAY;
    // System.out.println("Possible: " + frame.isMovementPossible(pRuntime, null));
    // List<Long> sendDates = new LinkedList<Long>();
    /*
     * for (int i = 0; i < 10; i++) {
     *
     * long time = pRuntime + (long) Math.round(10 * Math.random()) * 1000l; System.out.println("Possible: " +
     * frame.isMovementPossible(time, null)); System.out.println("Runtime: " + time); Date d = frame.getFittedArriveTime(time, null,
     * sendDates); if (d != null) { System.out.println("Arrive: " + f.format(d)); System.out.println("Send: " + f.format(new
     * Date(d.getTime() - time))); } else { System.out.println("NO ARRIVE"); } System.out.println("-------------"); }
     */
  }

  public boolean isValid() {
    return !(sendTimeSpans == null || arriveTimeSpans == null || sendTimeSpans.isEmpty() || arriveTimeSpans.isEmpty());
  }

  public String toString() {
    StringBuilder builder = new StringBuilder(200);
    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    builder.append("Start: ").append(f.format(new Date(getStartRange().getMinimumLong()))).append("-").append(f.format(new Date(getStartRange().getMaximumLong()))).append("\n");
    builder.append("Arrive: ").append(f.format(new Date(getArriveRange().getMinimumLong()))).append("-").append(f.format(new Date(getArriveRange().getMaximumLong()))).append("\n");
    builder.append("SendSpans: ").append(sendTimeSpans).append("\n");
    builder.append("ArriveSpans: ").append(arriveTimeSpans).append("\n");
    return builder.toString().trim();
  }
}
