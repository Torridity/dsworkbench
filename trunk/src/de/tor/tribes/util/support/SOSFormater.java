/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.support;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.SOSRequest.TargetInformation;
import de.tor.tribes.types.SOSRequest.TimedAttack;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Jejkal
 */
public class SOSFormater {

    public static String format(Village pTarget, TargetInformation pTargetInformation) {
        StringBuffer buffer = new StringBuffer();
        String serverURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
        //main quote
        buffer.append("[quote]");
        //village info size
        buffer.append("[size=12]");
        //village info (<SOS_IMG> <VILLAGE_BB> (<ATT_IMG> <ATT_COUNT>)
        buffer.append("[img]" + serverURL + "/graphic/reqdef.png[/img] " + pTarget.toBBCode() + " ([img]" + serverURL + "/graphic/unit/att.png[/img]" + pTargetInformation.getAttacks().size() + ")\n");
        buffer.append("[/size]\n");
        //village details quote
        buffer.append("[quote]");
        buffer.append("[size=12]");
        //add units and wall
        buffer.append(buildUnitInfo(pTargetInformation) + "\n");
        buffer.append("[img]" + serverURL + "/graphic/buildings/wall.png[/img] " + buildWallInfo(pTargetInformation));
        buffer.append("[/size]");
        buffer.append("[/quote]\n");
        //build first-last-attack
        buffer.append("[size=12]\n");

        //get first and last attack
        long first = Long.MAX_VALUE;
        int firstId = 0;
        long last = Long.MIN_VALUE;
        int lastId = 0;
        int cnt = 0;
        for (TimedAttack attack : pTargetInformation.getAttacks()) {
            long arrive = attack.getlArriveTime();
            if (arrive < first) {
                first = arrive;
                firstId = cnt;
            }
            if (arrive > last) {
                last = arrive;
                lastId = cnt;
            }
            cnt++;
        }

        //add first and last attack information
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        buffer.append("[img]" + serverURL + "/graphic/map/attack.png[/img] " + dateFormat.format(new Date(pTargetInformation.getAttacks().get(firstId).getlArriveTime())) + "\n");
        buffer.append("[img]" + serverURL + "/graphic/map/return.png[/img] " + dateFormat.format(new Date(pTargetInformation.getAttacks().get(lastId).getlArriveTime())) + "\n");
        buffer.append("[/size]\n");
        buffer.append("[/quote]\n");
        return buffer.toString();
    }

    private static String buildUnitInfo(TargetInformation pTargetInfo) {
        StringBuffer buffer = new StringBuffer();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String infRow = "";
        String cavRow = "";
        String miscRow = "";
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Integer amount = pTargetInfo.getTroops().get(unit);
            if (amount != null && amount != 0) {
                if (unit.isInfantry()) {
                    infRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                } else if (unit.isCavalry()) {
                    cavRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                } else {
                    miscRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                }
            }
        }
        if (infRow.length() > 1) {
            buffer.append(infRow.trim() + "\n");
        }
        if (cavRow.length() > 1) {
            buffer.append(cavRow.trim() + "\n");
        }
        if (miscRow.length() > 1) {
            buffer.append(miscRow.trim() + "\n");
        }
        return buffer.toString();
    }

    private static String buildWallInfo(TargetInformation pTargetInfo) {
        StringBuffer buffer = new StringBuffer();
        double perc = pTargetInfo.getWallLevel() / 20.0;
        int filledFields = (int) Math.rint(perc * 15.0);
        buffer.append("[color=#00FF00]");
        for (int i = 0; i < filledFields; i++) {
            buffer.append("█");
        }
        buffer.append("[/color]");
        if (filledFields < 15) {
            buffer.append("[color=#EEEEEE]");
            for (int i = 0; i < (15 - filledFields); i++) {
                buffer.append("█");
            }
            buffer.append("[/color]");
        }

        buffer.append(" (" + pTargetInfo.getWallLevel() + ")");
        return buffer.toString();
    }
}
