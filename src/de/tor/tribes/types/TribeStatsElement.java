/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
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

        try {
            BufferedReader r = new BufferedReader(new FileReader(pStatFile));
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
        }
        return elem;
    }

    public TribeStatsElement(Tribe pTribe) {
        tribe = pTribe;
        timestampList = new LinkedList<Long>();
        rankList = new LinkedList<Integer>();
        pointList = new LinkedList<Long>();
        villageList = new LinkedList<Short>();
        bashOffList = new LinkedList<Long>();
        rankOffList = new LinkedList<Short>();
        bashDefList = new LinkedList<Long>();
        rankDefList = new LinkedList<Short>();
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
            if (last.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH) &&
                    last.get(Calendar.MONTH) == current.get(Calendar.MONTH) &&
                    last.get(Calendar.YEAR) == current.get(Calendar.YEAR)) {
                //replace last value due to it is from the same day
                timestampList.remove(timestampList.size() - 1);
                rankList.remove(timestampList.size() - 1);
                pointList.remove(timestampList.size() - 1);
                villageList.remove(timestampList.size() - 1);
                bashOffList.remove(timestampList.size() - 1);
                rankOffList.remove(timestampList.size() - 1);
                bashDefList.remove(timestampList.size() - 1);
                rankDefList.remove(timestampList.size() - 1);
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
        for (long timestamp : timestamps) {
            if (timestamp < pTimestamp) {
                timestampList.remove(cnt);
                rankList.remove(cnt);
                pointList.remove(cnt);
                villageList.remove(cnt);
                bashOffList.remove(cnt);
                rankOffList.remove(cnt);
                bashDefList.remove(cnt);
                rankDefList.remove(cnt);
            } else {
                break;
            }
            cnt++;
        }
    }

    public void removeDataAfter(long pTimestamp) {
        Long[] timestamps = timestampList.toArray(new Long[]{});
        int cnt = 0;
        pTimestamp += 1000;
        for (long timestamp : timestamps) {
            if (timestamp > pTimestamp) {
                timestampList.remove(cnt);
                rankList.remove(cnt);
                pointList.remove(cnt);
                villageList.remove(cnt);
                bashOffList.remove(cnt);
                rankOffList.remove(cnt);
                bashDefList.remove(cnt);
                rankDefList.remove(cnt);
            }
            cnt++;
        }
    }
}
