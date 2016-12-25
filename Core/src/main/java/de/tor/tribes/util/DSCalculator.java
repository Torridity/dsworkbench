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
package de.tor.tribes.util;

import de.tor.tribes.types.ext.Village;
import java.awt.Point;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DSCalculator {

    private static double RESOURCE_PRODUCTION_CONTANT = 1.163118;
    private static double STORAGE_CAPACITY_CONTANT = 1.2294934;
    private static double HIDE_CAPACITY_CONTANT = 1.3335;

    public static double calculateDistance(Village pSource, Village pTarget) {
        if ((pSource == null) || (pTarget == null)) {
            return 0;
        }
        return pSource.getPosition().distance(pTarget.getPosition());
    }

    public static double calculateMoveTimeInMinutes(Village pSource, Village pTarget, double pMinPerField) {
        return calculateMoveTimeInSeconds(pSource, pTarget, pMinPerField) / 60.0;
    }

    public static double calculateMoveTimeInSeconds(Village pSource, Village pTarget, double pMinPerField) {
        if (ServerSettings.getSingleton().isMillisArrival()) {
            return calculateDistance(pSource, pTarget) * pMinPerField * 60.0;
        } else {
            return Math.round(calculateDistance(pSource, pTarget) * pMinPerField * 60.0);
        }
    }

    public static long calculateMoveTimeInMillis(Village pSource, Village pTarget, double pMinPerField) {
        if (ServerSettings.getSingleton().isMillisArrival()) {
            return Math.round(calculateDistance(pSource, pTarget) * pMinPerField * 60000L);
        } else {
            return Math.round(calculateDistance(pSource, pTarget) * pMinPerField * 60L) * 1000L;
        }
    }

    public static Point calculateCenterOfMass(List<Village> pVillages) {
        double mass = pVillages.size();
        double xMass = 0;
        double yMass = 0;
        for (Village v : pVillages) {
            xMass += v.getX();
            yMass += v.getY();
        }
        xMass = Math.rint(xMass / mass);
        yMass = Math.rint(yMass / mass);
        return new Point((int) xMass, (int) yMass);
    }

    public static int[] xyToHierarchical(int x, int y) {
        if (Math.abs(x) > 499 || Math.abs(y) > 499) {
            return null; // out of range
        }
        x *= 2;
        y *= 2;
        int con = (int) (Math.floor(y / 100) * 10 + Math.floor(x / 100));
        int sec = (int) ((Math.floor(y / 10) % 10) * 10 + (Math.floor(x / 10) % 10));
        int sub = (int) ((y % 10) * 2.5 + (x % 10) / 2);
        return new int[]{con, sec, sub};
    }

    public static int getContinent(int x, int y) {
        if (Math.abs(x) > 999 || Math.abs(y) > 999) {
            return 0; // out of range
        }
        return (int) (Math.floor(y / 100) * 10 + Math.floor(x / 100));
    }

    public static int[] hierarchicalToXy(int con, int sec, int sub) {
        if (con < 0 || con > 99 || sec < 0 || sec > 99 || sub < 0 || sub > 24) {
            return null; // invalid s3-coords
        }
        int x = (con % 10) * 50 + (sec % 10) * 5 + (sub % 5);
        int y = (int) (Math.floor(con / 10) * 50 + Math.floor(sec / 10) * 5 + Math.floor(sub / 5));
        return new int[]{x, y};
    }

    public static String formatTimeInMinutes(double pTime) {
        double dur = pTime;
        int hour = (int) Math.floor(dur / 60.0);
        dur -= hour * 60.0;
        int min = (int) Math.floor(dur);
        if (min == 60) {
            hour++;
            min -= 60;
        }
        int sec = (int) Math.rint((dur - min) * 60);
        if (sec == 60) {
            min++;
            sec -= 60;
            if (min == 60) {
                hour++;
                min -= 60;
            }
        }
        String result = "";
        if (hour < 10) {
            result += "0" + hour + ":";
        } else {
            result += hour + ":";
        }
        if (min < 10) {
            result += "0" + min + ":";
        } else {
            result += min + ":";
        }
        if (sec < 10) {
            result += "0" + sec;
        } else {
            result += sec;
        }
        return result;
    }

    public static double calculateResourcesPerHour(int pBuildingLevel) {
        return 30 * ServerSettings.getSingleton().getSpeed() * Math.pow(RESOURCE_PRODUCTION_CONTANT, (pBuildingLevel - 1));
    }

    public static int calculateEstimatedResourceBuildingLevel(double pResourcesDelta, double pTimeDelta) {
        return (int) Math.ceil(Math.log(pResourcesDelta / (pTimeDelta * 30 * ServerSettings.getSingleton().getSpeed())) / Math.log(RESOURCE_PRODUCTION_CONTANT) + 1);
    }

    public static int calculateMaxResourcesInStorage(int pStorageLevel) {
        return (int) Math.round(1000 * Math.pow(STORAGE_CAPACITY_CONTANT, (pStorageLevel - 1)));
    }

    public static int calculateEstimatedStorageLevel(double pResourcesInStorage) {
        return (int) Math.ceil(Math.log(pResourcesInStorage / 1000.0) / Math.log(STORAGE_CAPACITY_CONTANT) + 1);
    }

    public static int calculateMaxHiddenResources(int pHideLevel) {
        return (int) Math.round(150 * Math.pow(HIDE_CAPACITY_CONTANT, pHideLevel - 1));
    }
}
