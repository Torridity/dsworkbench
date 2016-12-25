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
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.util.BBSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Torridity
 */
public class TribeStatsElement {

    private Tribe tribe = null;
    private List<Long> timestampList = null;
    private List<Integer> rankList = null;
    private List<Long> pointList = null;
    private List<Short> villageList = null;
    private List<Long> bashOffList = null;
    private List<Short> rankOffList = null;
    private List<Long> bashDefList = null;
    private List<Short> rankDefList = null;

    public static TribeStatsElement loadFromFile(File pStatFile) {
        TribeStatsElement elem = new TribeStatsElement(null);
        Integer tribeId = Integer.parseInt(pStatFile.getName().substring(0, pStatFile.getName().lastIndexOf(".")));
        elem.setTribe(DataHolder.getSingleton().getTribes().get(tribeId));
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(pStatFile));
            String line = "";
            while ((line = r.readLine()) != null) {
                String[] data = line.split(";");
                long timestamp = Long.parseLong(data[0]);
                int rank = Integer.parseInt(data[1]);
                long points = Long.parseLong(data[2]);
                short villages = Short.parseShort(data[3]);
                long bashOff = Long.parseLong(data[4]);
                short rankOff = Short.parseShort(data[5]);
                long bashDef = Long.parseLong(data[6]);
                short rankDef = Short.parseShort(data[7]);
                elem.addLoadedData(timestamp, rank, points, villages, bashOff, rankOff, bashDef, rankDef);
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                r.close();
            } catch (Exception ignored) {
            }
        }
        return elem;
    }

    public TribeStatsElement(Tribe pTribe) {
        tribe = pTribe;
        timestampList = new LinkedList<>();
        rankList = new LinkedList<>();
        pointList = new LinkedList<>();
        villageList = new LinkedList<>();
        bashOffList = new LinkedList<>();
        rankOffList = new LinkedList<>();
        bashDefList = new LinkedList<>();
        rankDefList = new LinkedList<>();
    }

    protected void addLoadedData(long pTimestamp, int pRank, long pPoints, short pVillages, long pBashOff, short pRankOff, long pBashDef, short pRankDef) {
        timestampList.add(pTimestamp);
        rankList.add(pRank);
        pointList.add(pPoints);
        villageList.add(pVillages);
        bashOffList.add(pBashOff);
        rankOffList.add(pRankOff);
        bashDefList.add(pBashDef);
        rankDefList.add(pRankDef);
    }

    public void takeSnapshot(long pTimestamp) {
        if (timestampList.size() > 0) {
            Long lastValue = timestampList.get(timestampList.size() - 1);
            Calendar last = Calendar.getInstance();
            last.setTimeInMillis(lastValue);
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(pTimestamp);
            if (last.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)
                    && last.get(Calendar.MONTH) == current.get(Calendar.MONTH)
                    && last.get(Calendar.YEAR) == current.get(Calendar.YEAR)) {
                //replace last value due to it is from the same day
                int duplicatedIdx = timestampList.size() - 1;
                timestampList.remove(duplicatedIdx);
                rankList.remove(duplicatedIdx);
                pointList.remove(duplicatedIdx);
                villageList.remove(duplicatedIdx);
                bashOffList.remove(duplicatedIdx);
                rankOffList.remove(duplicatedIdx);
                bashDefList.remove(duplicatedIdx);
                rankDefList.remove(duplicatedIdx);
            }
        }

        timestampList.add(pTimestamp);
        rankList.add(tribe.getRank());
        pointList.add((long) tribe.getPoints());
        villageList.add(tribe.getVillages());
        bashOffList.add((long) tribe.getKillsAtt());
        rankOffList.add((short) tribe.getRankAtt());
        bashDefList.add((long) tribe.getKillsDef());
        rankDefList.add((short) tribe.getRankDef());
    }

    public void addRandomSnapshots() {
        long[] snapshots = new long[]{System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY, System.currentTimeMillis()};
        int cnt = 0;
        for (long snapshot : snapshots) {
            timestampList.add(snapshot);
            rankList.add(tribe.getRank() + (int) Math.round(cnt * .1 * tribe.getRank()));
            pointList.add((long) tribe.getPoints() + (int) Math.round(cnt * .1 * tribe.getPoints()));
            villageList.add(tribe.getVillages());
            bashOffList.add((long) tribe.getKillsAtt() + (int) Math.round(cnt * .1 * tribe.getKillsAtt()));
            rankOffList.add((short) (tribe.getRankAtt() + (int) Math.round(cnt * .1 * tribe.getRankAtt())));
            bashDefList.add((long) tribe.getKillsDef() + (int) Math.round(cnt * .1 * tribe.getKillsDef()));
            rankDefList.add((short) (tribe.getRankDef() + (int) Math.round(cnt * .1 * tribe.getRankDef())));
            cnt++;
        }
    }

    public void store(File pFile) throws Exception {
        FileOutputStream fout = new FileOutputStream(pFile, false);
        for (int id = 0; id < timestampList.size(); id++) {
            String line = timestampList.get(id) + ";" + rankList.get(id) + ";" + pointList.get(id) + ";";
            line += villageList.get(id) + ";" + bashOffList.get(id) + ";" + rankOffList.get(id) + ";";
            line += bashDefList.get(id) + ";" + rankDefList.get(id) + "\n";
            fout.write(line.getBytes());
        }
        fout.flush();
        fout.close();
    }

    public Long[] getTimestamps() {
        return timestampList.toArray(new Long[]{});
    }

    public Integer[] getRanks() {
        return rankList.toArray(new Integer[]{});
    }

    public Short[] getVillages() {
        return villageList.toArray(new Short[]{});
    }

    public Long[] getPoints() {
        return pointList.toArray(new Long[]{});
    }

    public Long[] getBashOffPoints() {
        return bashOffList.toArray(new Long[]{});
    }

    public Short[] getBashOffRank() {
        return rankOffList.toArray(new Short[]{});
    }

    public Long[] getBashDefPoints() {
        return bashDefList.toArray(new Long[]{});
    }

    public Short[] getBashDefRank() {
        return rankDefList.toArray(new Short[]{});
    }

    public Tribe getTribe() {
        return tribe;
    }

    private void setTribe(Tribe pTribe) {
        tribe = pTribe;
    }

    public void removeDataBefore(long pTimestamp) {
        Long[] timestamps = timestampList.toArray(new Long[]{});
        int cnt = 0;
        pTimestamp -= 1000;
        List<Integer> toRemove = new LinkedList<>();
        for (long timestamp : timestamps) {
            if (timestamp < pTimestamp) {
                toRemove.add(cnt);
            } /*else {
            break;
            }*/
            cnt++;
        }
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            int idx = toRemove.get(i);
            timestampList.remove(idx);
            rankList.remove(idx);
            pointList.remove(idx);
            villageList.remove(idx);
            bashOffList.remove(idx);
            rankOffList.remove(idx);
            bashDefList.remove(idx);
            rankDefList.remove(idx);
        }
    }

    public void removeDataAfter(long pTimestamp) {
        Long[] timestamps = timestampList.toArray(new Long[]{});
        int cnt = 0;
        pTimestamp += 1000;
        List<Integer> toRemove = new LinkedList<>();
        for (long timestamp : timestamps) {
            if (timestamp > pTimestamp) {
                toRemove.add(cnt);
            }
            cnt++;
        }
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            int idx = toRemove.get(i);
            timestampList.remove(idx);
            rankList.remove(idx);
            pointList.remove(idx);
            villageList.remove(idx);
            bashOffList.remove(idx);
            rankOffList.remove(idx);
            bashDefList.remove(idx);
            rankDefList.remove(idx);
        }

    }

    public void removeDataBetween(long pStartTimestamp, long pEndTimestamp) {
        Long[] timestamps = timestampList.toArray(new Long[]{});
        int cnt = 0;
        pStartTimestamp += 1000;
        pEndTimestamp -= 1000;
        List<Integer> toRemove = new LinkedList<>();
        for (long timestamp : timestamps) {
            if (timestamp > pStartTimestamp && timestamp < pEndTimestamp) {
                toRemove.add(cnt);
            }
            cnt++;
        }

        for (int i = toRemove.size() - 1; i >= 0; i--) {
            int idx = toRemove.get(i);
            timestampList.remove(idx);
            rankList.remove(idx);
            pointList.remove(idx);
            villageList.remove(idx);
            bashOffList.remove(idx);
            rankOffList.remove(idx);
            bashDefList.remove(idx);
            rankDefList.remove(idx);
        }
    }

    public Stats generateStats(long pStart, long pEnd) {
        Long[] timestamps = timestampList.toArray(new Long[]{});
        int cnt = 0;
        int startIndex = -1;
        int endIndex = -1;
        for (long timestamp : timestamps) {
            if (timestamp >= pStart && startIndex == -1) {
                startIndex = cnt;
            }
            if (timestamp >= pEnd && endIndex == -1) {
                endIndex = cnt;
            }
            if (startIndex > -1 && endIndex > -1) {
                //have both indices
                break;
            }
            cnt++;
        }

        if (startIndex == -1) {
            //no start found, obv. no data was collected
            startIndex = 0;
        }
        if (endIndex == -1) {
            //no end found, take last element
            endIndex = cnt - 1;
        }

        //get values
        long pointStart = pointList.get(startIndex);
        long pointEnd = pointList.get(endIndex);
        int rankStart = rankList.get(startIndex);
        int rankEnd = rankList.get(endIndex);
        int villageStart = villageList.get(startIndex);
        int villageEnd = villageList.get(endIndex);
        long bashOffStart = bashOffList.get(startIndex);
        long bashOffEnd = bashOffList.get(endIndex);
        int rankOffStart = rankOffList.get(startIndex);
        int rankOffEnd = rankOffList.get(endIndex);
        long bashDefStart = bashDefList.get(startIndex);
        long bashDefEnd = bashDefList.get(endIndex);
        int rankDefStart = rankDefList.get(startIndex);
        int rankDefEnd = rankDefList.get(endIndex);

        Stats result = new Stats(this);
        result.setPoints(pointStart, pointEnd);
        result.setRank(rankStart, rankEnd);
        result.setVillages(villageStart, villageEnd);
        result.setBashOff(bashOffStart, bashOffEnd);
        result.setRankOff(rankOffStart, rankOffEnd);
        result.setBashDef(bashDefStart, bashDefEnd);
        result.setRankDef(rankDefStart, rankDefEnd);
        return result;
    }

    public static class Stats implements Comparable<Stats>, BBSupport {

        private TribeStatsElement parent = null;
        private long pointStart = 0;
        private long pointEnd = 0;
        private int rankStart = 0;
        private int rankEnd = 0;
        private int villageStart = 0;
        private int villageEnd = 0;
        private long bashOffStart = 0;
        private long bashOffEnd = 0;
        private int rankOffStart = 0;
        private int rankOffEnd = 0;
        private long bashDefStart = 0;
        private long bashDefEnd = 0;
        private int rankDefStart = 0;
        private int rankDefEnd = 0;

        public Stats(TribeStatsElement pParent) {
            parent = pParent;
        }

        public TribeStatsElement getParent() {
            return parent;
        }
        // <editor-fold defaultstate="collapsed" desc="Setters for start and end values">

        public void setPoints(long pStart, long pEnd) {
            pointStart = pStart;
            pointEnd = pEnd;
        }

        public void setRank(int pStart, int pEnd) {
            rankStart = pStart;
            rankEnd = pEnd;
        }

        public void setVillages(int pStart, int pEnd) {
            villageStart = pStart;
            villageEnd = pEnd;
        }

        public void setBashOff(long pStart, long pEnd) {
            bashOffStart = pStart;
            bashOffEnd = pEnd;
        }

        public void setRankOff(int pStart, int pEnd) {
            rankOffStart = pStart;
            rankOffEnd = pEnd;
        }

        public void setBashDef(long pStart, long pEnd) {
            bashDefStart = pStart;
            bashDefEnd = pEnd;
        }

        public void setRankDef(int pStart, int pEnd) {
            rankDefStart = pStart;
            rankDefEnd = pEnd;
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Getters for start and end values">
        /**
         * @return the pointStart
         */
        public long getPointStart() {
            return pointStart;
        }

        /**
         * @return the pointEnd
         */
        public long getPointEnd() {
            return pointEnd;
        }

        /**
         * @return the rankStart
         */
        public int getRankStart() {
            return rankStart;
        }

        /**
         * @return the rankEnd
         */
        public int getRankEnd() {
            return rankEnd;
        }

        /**
         * @return the villageStart
         */
        public int getVillageStart() {
            return villageStart;
        }

        /**
         * @return the villageEnd
         */
        public int getVillageEnd() {
            return villageEnd;
        }

        /**
         * @return the bashOffStart
         */
        public long getBashOffStart() {
            return bashOffStart;
        }

        /**
         * @return the bashOffEnd
         */
        public long getBashOffEnd() {
            return bashOffEnd;
        }

        /**
         * @return the rankOffStart
         */
        public int getRankOffStart() {
            return rankOffStart;
        }

        /**
         * @return the rankOffEnd
         */
        public int getRankOffEnd() {
            return rankOffEnd;
        }

        /**
         * @return the bashDefStart
         */
        public long getBashDefStart() {
            return bashDefStart;
        }

        /**
         * @return the bashDefEnd
         */
        public long getBashDefEnd() {
            return bashDefEnd;
        }

        /**
         * @return the rankDefStart
         */
        public int getRankDefStart() {
            return rankDefStart;
        }

        /**
         * @return the rankDefEnd
         */
        public int getRankDefEnd() {
            return rankDefEnd;
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Diff getters">
        public Long getPointDiff() {
            return (pointEnd - pointStart);
        }

        public Integer getRankDiff() {
            return (rankEnd - rankStart);
        }

        public Integer getVillageDiff() {
            return (villageEnd - villageStart);
        }

        public Long getBashOffDiff() {
            return (bashOffEnd - bashOffStart);
        }

        public Integer getRankOffDiff() {
            return (rankOffEnd - rankOffStart);
        }

        public Long getBashDefDiff() {
            return (bashDefEnd - bashDefStart);
        }

        public Integer getRankDefDiff() {
            return (rankDefEnd - rankDefStart);
        }

        public Double getKillPerPoint() {
            long bashOffDiff = getBashOffDiff();
            if (bashOffDiff == 0) {
                return 0.0;
            }
            double pDiff = getPointDiff();
            pDiff = (pDiff == 0) ? 1 : pDiff;
            return (double) bashOffDiff / pDiff;
        }

        public Double getExpansion() {
            double perc = 0;
            if (pointStart > 0) {
                perc = (double) 100 * (double) getPointDiff() / (double) pointStart;
            }
            return perc;
        }
// </editor-fold>

        @Override
        public int compareTo(Stats o) {
            return 0;
        }
        public static final Comparator<Stats> POINTS_COMPARATOR = new StatPointsComparator();

        @Override
        public String getStandardTemplate() {
            return "";
        }

        @Override
        public String[] getBBVariables() {
            return new String[]{};
        }

        @Override
        public String[] getReplacements(boolean pExtended) {
            return new String[]{};
        }

        private static class StatPointsComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getPointDiff().compareTo(s1.getPointDiff());
            }
        }
        public static final Comparator<Stats> RANK_COMPARATOR = new StatRankComparator();

        private static class StatRankComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getRankDiff().compareTo(s1.getRankDiff());
            }
        }
        public static final Comparator<Stats> VILLAGE_COMPARATOR = new StatVillageComparator();

        private static class StatVillageComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getVillageDiff().compareTo(s1.getVillageDiff());
            }
        }
        public static final Comparator<Stats> BASH_OFF_COMPARATOR = new StatBashOffComparator();

        private static class StatBashOffComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getBashOffDiff().compareTo(s1.getBashOffDiff());
            }
        }
        public static final Comparator<Stats> RANK_OFF_COMPARATOR = new StatRankOffComparator();

        private static class StatRankOffComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getRankOffDiff().compareTo(s1.getRankOffDiff());
            }
        }
        public static final Comparator<Stats> BASH_DEF_COMPARATOR = new StatBashDefComparator();

        private static class StatBashDefComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getBashDefDiff().compareTo(s1.getBashDefDiff());
            }
        }
        public static final Comparator<Stats> RANK_DEF_COMPARATOR = new StatRankDefComparator();

        private static class StatRankDefComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getRankDefDiff().compareTo(s1.getRankDefDiff());
            }
        }
        public static final Comparator<Stats> KILLS_PER_POINT_COMPARATOR = new StatKillsPerPointComparator();

        private static class StatKillsPerPointComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getKillPerPoint().compareTo(s1.getKillPerPoint());
            }
        }
        public static final Comparator<Stats> EXPANSION_COMPARATOR = new ExpansionComparator();

        private static class ExpansionComparator implements Comparator<Stats>, java.io.Serializable {

            @Override
            public int compare(Stats s1, Stats s2) {
                return s2.getExpansion().compareTo(s1.getExpansion());
            }
        }
    }
}
