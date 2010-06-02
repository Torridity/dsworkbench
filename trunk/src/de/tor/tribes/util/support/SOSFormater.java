/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.support;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.SOSRequest.TargetInformation;
import de.tor.tribes.types.SOSRequest.TimedAttack;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class SOSFormater {

    public static String format(Village pTarget, TargetInformation pTargetInformation, boolean pDetailed) {
        StringBuffer buffer = new StringBuffer();
        String serverURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
        //main quote
        buffer.append("[quote]");
        //village info size
        buffer.append("[size=12]");
        //village info (<SOS_IMG> <VILLAGE_BB> (<ATT_IMG> <ATT_COUNT>)
        buffer.append("[img]" + serverURL + "/graphic/reqdef.png[/img] " + pTarget.toBBCode() + " ([img]" + serverURL + "/graphic/unit/att.png[/img] " + pTargetInformation.getAttacks().size() + ")\n");
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

        List<TimedAttack> attacks = pTargetInformation.getAttacks();

        Collections.sort(attacks, SOSRequest.ARRIVE_TIME_COMPARATOR);

        //add first and last attack information
        SimpleDateFormat dateFormat = null;
        if (ServerSettings.getSingleton().isMillisArrival()) {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        } else {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
        buffer.append("[img]" + serverURL + "/graphic/map/attack.png[/img] " + dateFormat.format(new Date(attacks.get(0).getlArriveTime())) + "\n");
        if (pDetailed && attacks.size() > 2) {
            buffer.append("[/size]\n");
            //add details for all attacks
            for (int i = 0; i < attacks.size(); i++) {
                try {
                    TimedAttack attack = attacks.get(i);
                    buffer.append(attack.getSource().toBBCode() + " (" + attack.getSource().getTribe().toBBCode() + ") " + dateFormat.format(new Date(attack.getlArriveTime())) + "\n");
                } catch (Exception e) {
                }
            }
            buffer.append("[size=12]\n");
        }
        buffer.append("[img]" + serverURL + "/graphic/map/return.png[/img] " + dateFormat.format(new Date(attacks.get(attacks.size() - 1).getlArriveTime())) + "\n");
        buffer.append("[/size]\n");
        buffer.append("[/quote]\n");
        return buffer.toString().trim();
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
