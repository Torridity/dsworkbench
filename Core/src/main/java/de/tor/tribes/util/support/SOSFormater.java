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
package de.tor.tribes.util.support;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.TargetInformation;
import de.tor.tribes.types.TimedAttack;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class SOSFormater {

    public static String format(Village pTarget, TargetInformation pTargetInformation, boolean pDetailed) {
        StringBuilder buffer = new StringBuilder();
        String serverURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
        //main quote
        buffer.append("[quote]");
        //village info size
        // buffer.append("[size=12]");
        //village info (<SOS_IMG> <VILLAGE_BB> (<ATT_IMG> <ATT_COUNT>)
        buffer.append("[img]").append(serverURL).append("/graphic/reqdef.png[/img] ").append(pTarget.toBBCode()).append(" ([img]").append(serverURL).append("/graphic/unit/att.png[/img] ").append(pTargetInformation.getAttacks().size()).append(")\n");
        // buffer.append("[/size]\n");
        //village details quote
        buffer.append("[quote]");
        // buffer.append("[size=12]");
        //add units and wall
        buffer.append(buildUnitInfo(pTargetInformation)).append("\n");
        buffer.append("[img]").append(serverURL).append("/graphic/buildings/wall.png[/img] ").append(buildWallInfo(pTargetInformation));
        // buffer.append("[/size]");
        buffer.append("[/quote]\n");
        //build first-last-attack
        // buffer.append("[size=12]\n");

        List<TimedAttack> attacks = pTargetInformation.getAttacks();

        Collections.sort(attacks, SOSRequest.ARRIVE_TIME_COMPARATOR);

        //add first and last attack information
        SimpleDateFormat dateFormat = null;
        if (ServerSettings.getSingleton().isMillisArrival()) {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        } else {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
        buffer.append("[img]").append(serverURL).append("/graphic/map/attack.png[/img] ").append(dateFormat.format(new Date(attacks.get(0).getlArriveTime()))).append("\n");

        // buffer.append("[/size]\n");
        //add details for all attacks
        int fakeCount = 0;
        int snobCount = 0;
        for (TimedAttack attack1 : attacks) {
            try {
                TimedAttack attack = attack1;
                if (attack.isPossibleFake()) {
                    fakeCount++;
                } else if (attack.isPossibleSnob()) {
                    snobCount++;
                }
                if (pDetailed) {
                    if (attack.isPossibleFake()) {
                        buffer.append(attack.getSource().toBBCode()).append(" (").append(attack.getSource().getTribe().toBBCode()).append(") ").append(dateFormat.format(new Date(attack.getlArriveTime()))).append(" [b](Fake)[/b]").append("\n");
                    } else if (attack.isPossibleSnob()) {
                        buffer.append(attack.getSource().toBBCode()).append(" (").append(attack.getSource().getTribe().toBBCode()).append(") ").append(dateFormat.format(new Date(attack.getlArriveTime()))).append(" [b](AG)[/b]").append("\n");
                    } else {
                        buffer.append(attack.getSource().toBBCode()).append(" (").append(attack.getSource().getTribe().toBBCode()).append(") ").append(dateFormat.format(new Date(attack.getlArriveTime()))).append("\n");
                    }
                }
            } catch (Exception ignored) {
            }
            //buffer.append("[size=12]\n");
        }
        buffer.append("[img]").append(serverURL).append("/graphic/map/return.png[/img] ").append(dateFormat.format(new Date(attacks.get(attacks.size() - 1).getlArriveTime()))).append("\n");
        if (!pDetailed && fakeCount > 0) {
            buffer.append("\n");
            buffer.append("[u]Mögliche Fakes:[/u] ").append(fakeCount).append("\n");
            buffer.append("[u]Mögliche AGs:[/u] ").append(snobCount).append("\n");
        }
        // buffer.append("[/size]\n");
        buffer.append("[/quote]\n");
        return buffer.toString().trim();
    }

    private static String buildUnitInfo(TargetInformation pTargetInfo) {
        StringBuilder buffer = new StringBuilder();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String defRow = "";
        String offRow = "";

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Integer amount = pTargetInfo.getTroops().get(unit);
            if (amount != null && amount != 0) {
                if (unit.getPlainName().equals("spear") || unit.getPlainName().equals("sword") || unit.getPlainName().equals("archer") || unit.getPlainName().equals("spy") || unit.getPlainName().equals("heavy") || unit.getPlainName().equals("knight")) {
                    defRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                } else {
                    offRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                }
            }
        }
        if (defRow.length() > 1) {
            buffer.append(defRow.trim()).append("\n");
        }
        if (offRow.length() > 1) {
            buffer.append(offRow.trim()).append("\n");
        }
        return buffer.toString();
    }

    private static String buildWallInfo(TargetInformation pTargetInfo) {
        StringBuilder buffer = new StringBuilder();
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

        buffer.append(" (").append(pTargetInfo.getWallLevel()).append(")");
        return buffer.toString();
    }
}
