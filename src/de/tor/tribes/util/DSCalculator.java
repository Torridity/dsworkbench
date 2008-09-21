/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.types.Village;

/**
 *
 * @author Jejkal
 */
public class DSCalculator {

    public static double calculateDistance(Village pSource, Village pTarget) {
        if ((pSource == null) || (pTarget == null)) {
            return 0;
        }
        return Math.sqrt(Math.pow(pTarget.getX() - pSource.getX(), 2) + Math.pow(pTarget.getY() - pSource.getY(), 2));
    }

    public static double calculateMoveTimeInMinutes(Village pSource, Village pTarget, double pMinPerField) {
        return calculateDistance(pSource, pTarget) * pMinPerField;
    }

    public static double calculateMoveTimeInSeconds(Village pSource, Village pTarget, double pMinPerField) {
        return calculateDistance(pSource, pTarget) * pMinPerField * 60.0;
    }

    public static double calculateMoveTimeInHours(Village pSource, Village pTarget, double pMinPerField) {
        return calculateDistance(pSource, pTarget) * pMinPerField / 60.0;
    }

    public static String formatTimeInMinutes(double pTime) {
        double dur = pTime;
        int hour = (int) Math.floor(dur / 60);
        dur -= hour * 60;
        int min = (int) Math.floor(dur);
        int sec = (int) Math.rint((dur - min) * 60);

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
}
