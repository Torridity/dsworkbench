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

import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Village;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Charon
 */
public class AttackToBBCodeFormater {

    public static final String STANDARD_TEMPLATE = "%TYPE% von %ATTACKER% aus %SOURCE% mit %UNIT% auf %DEFENDER% in %TARGET% startet am [color=#ff0e0e]%SEND%[/color] und kommt am [color=#2eb92e]%ARRIVE%[/color] an (%PLACE%)";

    public static String formatAttack(Attack pAttack, String pServerURL, boolean pExtended) {
        String sendtime = null;
        String arrivetime = null;
        String template = GlobalOptions.getProperty("attack.bbexport.template");
        
        //bot protection
        template = template.replaceAll("red", getRandomRed());
        template = template.replaceAll("green", getRandomGreen());

        Date aTime = pAttack.getArriveTime();
        Date sTime = new Date(aTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(pAttack.getSource(), pAttack.getTarget(), pAttack.getUnit().getSpeed()) * 1000));
        if (pExtended) {
            if (ServerSettings.getSingleton().isMillisArrival()) {
                sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(sTime);
                arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(aTime);
            } else {
                sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(sTime);
                arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(aTime);
            }
        } else {
            if (ServerSettings.getSingleton().isMillisArrival()) {
                sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(sTime);
                arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(aTime);
            } else {
                sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(sTime);
                arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(aTime);
            }
        }

        switch (pAttack.getType()) {
            case Attack.CLEAN_TYPE: {
                template = template.replaceAll("%TYPE%", "Angriff (Clean-Off)");
                break;
            }
            case Attack.FAKE_TYPE: {
                template = template.replaceAll("%TYPE%", "Angriff (Fake)");
                break;
            }
            case Attack.SNOB_TYPE: {
                template = template.replaceAll("%TYPE%", "Angriff (AG)");
                break;
            }
            case Attack.SUPPORT_TYPE: {
                template = template.replaceAll("%TYPE%", "Unterstützung");
                break;
            }
            default: {
                template = template.replaceAll("%TYPE%", "Angriff");
            }
        }

        if (pAttack.getSource().getTribe() != Barbarians.getSingleton()) {
            template = template.replaceAll("%ATTACKER%", pAttack.getSource().getTribe().toBBCode());
        } else {
            template = template.replaceAll("%ATTACKER%", "Barbaren");
        }
        template = template.replaceAll("%SOURCE%", pAttack.getSource().toBBCode());
        if (pExtended) {
            template = template.replaceAll("%UNIT%", "[img]" + pServerURL + "/graphic/unit/unit_" + pAttack.getUnit().getPlainName() + ".png[/img]");
        } else {
            template = template.replaceAll("%UNIT%", pAttack.getUnit().getName());
        }
        if (pAttack.getTarget().getTribe() != Barbarians.getSingleton()) {
            template = template.replaceAll("%DEFENDER%", pAttack.getTarget().getTribe().toBBCode());
        } else {
            template = template.replaceAll("%DEFENDER%", "Barbaren");
        }
        template = template.replaceAll("%TARGET%", pAttack.getTarget().toBBCode());
        template = template.replaceAll("%SEND%", sendtime);
        template = template.replaceAll("%ARRIVE%", arrivetime);
        //replace place var
        String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer()) + "/";
        String placeURL = baseURL + "game.php?village=";
        int uvID = -1;
        if (GlobalOptions.getSelectedProfile() != null) {
            uvID = GlobalOptions.getSelectedProfile().getUVId();
        }

        if (uvID >= 0) {
            placeURL = baseURL + "game.php?t=" + uvID + "&village=";
        }
        placeURL += pAttack.getSource().getId() + "&screen=place&mode=command&target=" + pAttack.getTarget().getId();

        String placeLink = "[url=\"" + placeURL + "\"]Versammlungsplatz[/url]";
        template = template.replaceAll("%PLACE%", placeLink);
        template = template.replaceAll("%PLACE_URL%", placeURL);
        template += "\n";
        return template;
    }

    public static String formatAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pSendTime, int pType, String pServerURL, boolean pExtended) {
        String sendtime = null;
        String arrivetime = null;
        String template = GlobalOptions.getProperty("attack.bbexport.template");

        Date aTime = new Date(pSendTime.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(pSource, pTarget, pUnit.getSpeed()) * 1000));

        if (pExtended) {
            sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(pSendTime);
            arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(aTime);
        } else {
            sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(pSendTime);
            arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(aTime);
        }

        switch (pType) {
            case Attack.CLEAN_TYPE: {
                template = template.replaceAll("%TYPE%", "Angriff (Clean-Off)");
                break;
            }
            case Attack.FAKE_TYPE: {
                template = template.replaceAll("%TYPE%", "Angriff (Fake)");
                break;
            }
            case Attack.SNOB_TYPE: {
                template = template.replaceAll("%TYPE%", "Angriff (AG)");
                break;
            }
            case Attack.SUPPORT_TYPE: {
                template = template.replaceAll("%TYPE%", "Unterstützung");
                break;
            }
            default: {
                template = template.replaceAll( "%TYPE%", "Angriff");
            }
        }

        if (pSource.getTribe() != Barbarians.getSingleton()) {
            template = template.replaceAll("%ATTACKER%", pSource.getTribe().toBBCode());
        } else {
            template = template.replaceAll("%ATTACKER%", "Barbaren");
        }
        template = template.replaceAll("%SOURCE%", pSource.toBBCode());
        if (pExtended) {
            template = template.replaceAll("%UNIT%", "[img]" + pServerURL + "/graphic/unit/unit_" + pUnit.getPlainName() + ".png[/img]");
        } else {
            template = template.replaceAll("%UNIT%", pUnit.getName());
        }
        if (pTarget.getTribe() != Barbarians.getSingleton()) {
            template = template.replaceAll("%DEFENDER%", pTarget.getTribe().toBBCode());
        } else {
            template = template.replaceAll("%DEFENDER%", "Barbaren");
        }
        template = template.replaceAll("%TARGET%", pTarget.toBBCode());
        template = template.replaceAll("%SEND%", sendtime);
        template = template.replaceAll("%ARRIVE%", arrivetime);
        //replace place var
        String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer()) + "/";
        String placeURL = baseURL + "game.php?village=";
        int uvID = GlobalOptions.getSelectedProfile().getUVId();
        if (uvID >= 0) {
            placeURL = baseURL + "game.php?t=" + uvID + "&village=";
        }
        placeURL += pSource.getId() + "&screen=place&mode=command&target=" + pTarget.getId();

        String placeLink = "[url=\"" + placeURL + "\"]Versammlungsplatz[/url]";
        template = template.replaceAll("%PLACE%", placeLink);
        template += "\n";

        return template;
    }

    public static String getRandomRed() {
        Color c = Color.decode("#ff0e0e");
        int randomColor = c.getRGB() + (int) Math.rint(Math.random() * 10);
        return "#" + Integer.toHexString(new Color(randomColor).getRGB()).substring(2);
    }

    public static String getRandomGreen() {
        Color c = Color.decode("#2eb92e");
        int randomColor = c.getRGB() + (int) Math.rint(Math.random() * 10);
        return "#" + Integer.toHexString(new Color(randomColor).getRGB()).substring(2);
    }
}
