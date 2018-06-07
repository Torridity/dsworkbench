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

import de.tor.tribes.types.TimeSpan;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.time.DateUtils;

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
  List<Range<Long>> startRanges = null;
  List<Range<Long>> arriveRanges = null;
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

  public Range<Long> getStartRange() {
    return Range.between(startNotBefore, startNotAfter);
  }

  public Range<Long> getArriveRange() {
    return Range.between(arriveNotBefore, arriveNotAfter);
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
  public boolean isMovementPossible(long pRuntime) {
    if (startRanges == null) {
      startRanges = startTimespansToRanges();
    }
    if (arriveRanges == null) {
      arriveRanges = arriveTimespansToRanges();
    }

    for (Range<Long> currentStartRange : startRanges) {
      Range<Long> arriveRangeForStartRange = Range.between(currentStartRange.getMinimum() + pRuntime, currentStartRange.getMaximum() + pRuntime);
      for (Range<Long> currentArriveRange : arriveRanges) {
        if (currentArriveRange.isOverlappedBy(arriveRangeForStartRange)) {
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
   * in steps of 1ms). This argument may be 'null'. Then send times are
   * not checked.
   * @return Date Fitted arrive time
   */
  public Date getFittedArriveTime(long pRuntime, List<Long> pUsedSendTimes) {
    List<Range<Long>> startRanges = startTimespansToRanges();
    List<Range<Long>> arriveRanges = arriveTimespansToRanges();
    List<Range<Long>> possible = new ArrayList();
    for (Range<Long> currentStartRange : startRanges) {
      Range<Long> arriveRangeForStartRange = Range.between(currentStartRange.getMinimum() + pRuntime, currentStartRange.getMaximum() + pRuntime);
      for (Range<Long> currentArriveRange : arriveRanges) {
        if (currentArriveRange.isOverlappedBy(arriveRangeForStartRange)) {
          //movement possible for these 'currentStartRange' and 'currentArriveRange' so fit runtime into
          //           |-----------|
          //   |--------------|
          long minArrive = currentArriveRange.getMinimum();
          long minArriveForStartRange = arriveRangeForStartRange.getMinimum();
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
          long maxArrive = currentArriveRange.getMaximum();
          long maxArriveForStartRange = arriveRangeForStartRange.getMaximum();
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
          
          if(checkStart != checkEnd) {
            //We are handling real Ranges
            int cnt = 0;
            while (cnt < 20) {
              long arriveTime = checkStart + Math.round(Math.random() * (checkEnd - checkStart));
              if (pUsedSendTimes == null || !pUsedSendTimes.contains(arriveTime - pRuntime)) {
                if (pUsedSendTimes != null) {
                  pUsedSendTimes.add(arriveTime - pRuntime);
                }
                return new Date(arriveTime);
              }
              cnt++;
            }
            //We found nothing with random, try systematic search
            for(long i = checkStart; i <= checkEnd; i++) {
                if(!pUsedSendTimes.contains(i - pRuntime)) {
                  pUsedSendTimes.add(i - pRuntime);
                  return new Date(i);
                }
            }
          } else {
            //We are handling a fixed arrive Time
            if(pUsedSendTimes == null || !pUsedSendTimes.contains(checkStart - pRuntime)) {
              if (pUsedSendTimes != null) {
                pUsedSendTimes.add(checkStart - pRuntime);
              }
              return new Date(checkStart);
            }
          }
          possible.add(Range.between(checkStart, checkEnd));
        }
      }
    }
    
    if(!possible.isEmpty()) {
        long cnt = 0;
        for(Range<Long> r: possible) {
            cnt += r.getMaximum() - r.getMinimum() + 1;
        }
        cnt = (long) (Math.random() * cnt);
        for(Range<Long> r: possible) {
            Long span = r.getMaximum() - r.getMinimum() + 1;
            if(cnt < span) {
                return new Date(r.getMinimum() + cnt);
            }
            cnt -= span;
        }
    }
    
    return null;
  }

  public List<Range<Long>> startTimespansToRanges() {
    List<Range<Long>> ranges = new LinkedList<>();
    Date startDate = DateUtils.truncate(new Date(startNotBefore), Calendar.DATE);

    for (TimeSpan span : sendTimeSpans) {
      if(!span.isValidAtEveryDay()) {
          Range<Long> range;
          //just copy range
          if(span.isValidAtExactTime()) {
            range = Range.between(span.getSpan().getMinimum(), span.getSpan().getMaximum() + fixedStartTimeRangeSize);
          } else {
            range = Range.between(span.getSpan().getMinimum(), span.getSpan().getMaximum());
          }
          
          if (range.getMaximum() > System.currentTimeMillis()) {
            if(range.getMinimum() <= System.currentTimeMillis()) {
                //rebuild Range
                range = Range.between(System.currentTimeMillis(), range.getMaximum());
            }
            //add range only if it is in future
            ranges.add(range);
          }
      }
      else {
        //span is valid for every day
        Date thisDate = new Date(startDate.getTime());
        //go through all days from start to end
        while (thisDate.getTime() < startNotAfter) {
          long spanStart = thisDate.getTime() + span.getSpan().getMinimum();
          long spanEnd = thisDate.getTime() + span.getSpan().getMaximum();
          Range<Long> newRange = null;
          //check span location relative to start frame
          if (spanStart >= startNotBefore && spanEnd > startNotBefore
              && spanStart < startNotAfter && spanEnd <= startNotAfter) {
            //|----------| (startNotBefore - startNotAfter)
            //  |----| (SpanStart - SpanEnd)
            newRange = Range.between(spanStart, spanEnd);
          } else if (spanStart < startNotBefore && spanEnd > startNotBefore
              && spanStart < startNotAfter && spanEnd <= startNotAfter) {
            //  |----------| (startNotBefore - startNotAfter)
            //|----| (SpanStart - SpanEnd)
            //set span start to 'startNotBefore'
            newRange = Range.between(startNotBefore, spanEnd);
          } else if (spanStart <= startNotBefore && spanEnd > startNotBefore
              && spanStart > startNotAfter && spanEnd >= startNotAfter) {
            //  |----------| (startNotBefore - startNotAfter)
            //|--------------| (SpanStart - SpanEnd)
            //set span start to 'startNotBefore'
            newRange = Range.between(startNotBefore, startNotAfter);
          } else if (spanStart >= startNotBefore && spanEnd > startNotBefore
              && spanStart < startNotAfter && spanEnd >= startNotAfter) {
            //|----------| (startNotBefore - startNotAfter)
            //    |---------| (SpanStart - SpanEnd)
            //set span start to 'startNotBefore'
            newRange = Range.between(spanStart, startNotAfter);
          }

          if (newRange != null) {
            if (newRange.getMinimum() < System.currentTimeMillis()) {
              //check minimum as current minimum is in past
              if (newRange.getMaximum() > System.currentTimeMillis()) {
                newRange = Range.between(System.currentTimeMillis(), newRange.getMaximum());
                ranges.add(newRange);
              }//ignore as entire range is in past
            } else {
              //add range as it is in future
              ranges.add(newRange);
            }
          }
          //increment current date by one day
          thisDate = DateUtils.addDays(thisDate, 1);
        }
      }
    }
    Collections.sort(ranges, new Comparator<Range<Long>>() {
      @Override
      public int compare(Range<Long> o1, Range<Long> o2) {
        return o1.getMinimum().compareTo(o2.getMinimum());
      }
    });
    return ranges;
  }

  public List<Range<Long>> arriveTimespansToRanges() {
    List<Range<Long>> ranges = new LinkedList<>();
    Date arriveDate = DateUtils.truncate(new Date(arriveNotBefore), Calendar.DAY_OF_MONTH);

    for (TimeSpan span : arriveTimeSpans) {
      if(!span.isValidAtEveryDay()) {
          Range<Long> range;
          //just copy range
          range = Range.between(span.getSpan().getMinimum(), span.getSpan().getMaximum());
          
          if (range.getMaximum() > System.currentTimeMillis()) {
            if(range.getMinimum() <= System.currentTimeMillis()) {
                //rebuild Range
                range = Range.between(System.currentTimeMillis(), range.getMaximum());
            }
            //add range only if it is in future
            ranges.add(range);
          }
      }
      else {
        //span is valid for every day
        Date thisDate = new Date(arriveDate.getTime());
        //go through all days from start to end
        while (thisDate.getTime() < arriveNotAfter) {
          long spanStart = thisDate.getTime() + span.getSpan().getMinimum();
          long spanEnd = thisDate.getTime() + span.getSpan().getMaximum();
          Range<Long> newRange = null;
          //check span location relative to start frame
          if (spanStart >= arriveNotBefore && spanEnd > arriveNotBefore
              && spanStart < arriveNotAfter && spanEnd <= arriveNotAfter) {
            //|----------| (startNotBefore - startNotAfter)
            //  |----| (SpanStart - SpanEnd)
            newRange = Range.between(spanStart, spanEnd);
          } else if (spanStart < arriveNotBefore && spanEnd > arriveNotBefore
              && spanStart < arriveNotAfter && spanEnd <= arriveNotAfter) {
            //  |----------| (startNotBefore - startNotAfter)
            //|----| (SpanStart - SpanEnd)
            //set span start to 'startNotBefore'
            newRange = Range.between(arriveNotBefore, spanEnd);
          } else if (spanStart <= arriveNotBefore && spanEnd > arriveNotBefore
              && spanStart > arriveNotAfter && spanEnd >= arriveNotAfter) {
            //  |----------| (startNotBefore - startNotAfter)
            //|--------------| (SpanStart - SpanEnd)
            //set span start to 'startNotBefore'
            newRange = Range.between(arriveNotBefore, arriveNotAfter);
          } else if (spanStart >= arriveNotBefore && spanEnd > arriveNotBefore
              && spanStart < arriveNotAfter && spanEnd >= arriveNotAfter) {
            //|----------| (startNotBefore - startNotAfter)
            //    |---------| (SpanStart - SpanEnd)
            //set span start to 'startNotBefore'
            newRange = Range.between(spanStart, arriveNotAfter);
          }

          if (newRange != null) {
            if (newRange.getMinimum() < System.currentTimeMillis()) {
              //check minimum as current minimum is in past
              if (newRange.getMaximum() > System.currentTimeMillis()) {
                newRange = Range.between(System.currentTimeMillis(), newRange.getMaximum());
                ranges.add(newRange);
              }//ignore as entire range is in past
            } else {
              //add range as it is in future
              ranges.add(newRange);
            }
          }
          //increment current date by one day
          thisDate = DateUtils.addDays(thisDate, 1);
        }
      }
    }
    Collections.sort(ranges, new Comparator<Range<Long>>() {
      @Override
      public int compare(Range<Long> o1, Range<Long> o2) {
        return o1.getMinimum().compareTo(o2.getMinimum());
      }
    });
    return ranges;
  }

  public boolean isValid() {
    return !(sendTimeSpans == null || arriveTimeSpans == null || sendTimeSpans.isEmpty() || arriveTimeSpans.isEmpty());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    builder.append("Start: ").append(f.format(new Date(getStartRange().getMinimum()))).append("-").append(f.format(new Date(getStartRange().getMaximum()))).append("\n");
    builder.append("Arrive: ").append(f.format(new Date(getArriveRange().getMinimum()))).append("-").append(f.format(new Date(getArriveRange().getMaximum()))).append("\n");
    builder.append("SendSpans: ").append(sendTimeSpans).append("\n");
    builder.append("ArriveSpans: ").append(arriveTimeSpans).append("\n");
    return builder.toString();
  }
}
