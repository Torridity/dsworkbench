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

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import de.tor.tribes.util.village.KnownVillageManager;
import java.awt.Point;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DSCalculator {

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
        return calculateMoveTimeInMillis(pSource, pTarget, pMinPerField) / 1000.0;
    }

    public static long calculateMoveTimeInMillis(Village pSource, Village pTarget, double pMinPerField) {
        return Math.round(calculateDistance(pSource, pTarget) * pMinPerField * 60L) * 1000L;
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

    public static int calculateEstimatedResourceBuildingLevel(double pResourcesDelta, double pTimeDelta) {
        return (int) Math.ceil(Math.log(pResourcesDelta / (pTimeDelta * ServerSettings.getSingleton().getResourceConstant() *
                ServerSettings.getSingleton().getSpeed())) / Math.log(BuildingSettings.RESOURCE_PRODUCTION_FACTOR) + 1);
    }

    public static int calculateEstimatedStorageLevel(double pResourcesInStorage) {
        return (int) Math.ceil(Math.log(pResourcesInStorage / BuildingSettings.STORAGE_CAPACITY) /
                Math.log(BuildingSettings.STORAGE_CAPACITY_FACTOR) + 1);
    }
    
    public static double calculateRiseSpeed() {
        return ServerSettings.getSingleton().getSpeed();
    }
    
    public static String calculateMorale(Tribe pAttacker, Tribe pDefender) {
        String moral;
        switch(ServerSettings.getSingleton().getMoralType()) {
            //TODO Correct this
            case ServerSettings.TIME_LIMITED_POINTBASED_MORAL:
            case ServerSettings.TIMEBASED_MORAL:
                moral = "Unbekannt";
                break;
            case ServerSettings.POINTBASED_MORAL:
                int temp = (int) (((pDefender.getPoints() / pAttacker.getPoints()) * 3 + 0.3) * 100);
                temp = (temp > 100) ? 100 : temp;
                moral = temp + "%";
                break;
            case ServerSettings.NO_MORAL:
            default:
                moral = "100%";
                break;
        }
        return moral;
    }

    public static float getFarmSpaceRatio(Village pVillage) {
        VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
        VillageTroopsHolder otw = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.ON_THE_WAY);
        VillageTroopsHolder out = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OUTWARDS);
        
        double usedPop = 0;
        if(own != null) {
            usedPop += own.getTroops().getTroopPopCount();
        }
        if(otw != null) {
            usedPop += otw.getTroops().getTroopPopCount();
        }
        if(out != null) {
            usedPop += out.getTroops().getTroopPopCount();
        }
        
        int max;
        if(GlobalOptions.getProperties().getBoolean("farm.popup.use.real")) {
            max = KnownVillageManager.getSingleton().getKnownVillage(pVillage).getFarmSpace();
            
            if(max == -1) {
                //use other value as fallback
                max = GlobalOptions.getProperties().getInt("max.farm.space");
            }
        } else {
            max = GlobalOptions.getProperties().getInt("max.farm.space");
        }

        //calculate farm space depending on pop bonus
        float res = (float) (usedPop / (double) max);
        
        return (res > 1.0f) ? 1.0f : res;
    }
}
