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
package de.tor.tribes.util.js;

import de.tor.tribes.types.Attack;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @author Charon
 */
public class AttackScriptWriter {

    private static Logger logger = Logger.getLogger("AttackScriptWriter");

    public static boolean writeAttackScript(List<Attack> pAttacks,
            boolean pDrawAttacks,
            int pLineWidth,
            boolean pStraightLine,
            Color pStartColor,
            Color pEndColor,
            boolean pShowAttacksInVillageInfo,
            boolean pShowAttacksOnConfirmPage,
            boolean pShowAttacksOnCommandPage,
            boolean pShowAttacksInOverview) {
        logger.debug("Start writing attack script");
        String tmpl = "";
        try {
            logger.debug(" - reading template");
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream("./scripts/show.tmpl")));
            String line = "";
            while ((line = r.readLine()) != null) {
                tmpl += line + "\n";
            }
            r.close();
        } catch (Exception e) {
            logger.error("Failed to read template", e);
            return false;
        }

        logger.debug(" - building data array");
        String data = "win.attacks = new Array(";
        for (Attack a : pAttacks) {
            //set type
            String block = "{\n";
            switch (a.getType()) {
                case Attack.NO_TYPE:
                    //block += ",";
                    break;
                case Attack.CLEAN_TYPE:
                    block += "'type':'axe.png',\n";
                    break;
                case Attack.FAKE_TYPE:
                    block += "'type':'fake.png',\n";
                    break;
                case Attack.FAKE_DEFF_TYPE:
                    block += "'type':'def_fake.png',\n";
                    break;
                case Attack.SNOB_TYPE:
                    block += "'type':'snob.png',\n";
                    break;
                case Attack.SUPPORT_TYPE:
                    block += "'type':'def.png',\n";
                    break;
            }
            //set source and target

            block += "'sourceName':'" + toUnicode(a.getSource().toString()) + "',\n";
            block += "'source':" + a.getSource().getId() + ",\n";
            block += "'xs':" + a.getSource().getX() + ",\n";
            block += "'ys':" + a.getSource().getY() + ",\n";
            block += "'target':" + a.getTarget().getId() + ",\n";
            block += "'xt':" + a.getTarget().getX() + ",\n";
            block += "'yt':" + a.getTarget().getY() + ",\n";
            block += "'targetName':'" + toUnicode(a.getTarget().toString()) + "',\n";
            //unit
            block += "'unit':'" + a.getUnit().getPlainName() + ".png',\n";
            //times
            SimpleDateFormat df = null;
            if (ServerSettings.getSingleton().isMillisArrival()) {
                df = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
            } else {
                df = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
            }
            block += "'send':'" + df.format(a.getSendTime()) + "',\n";
            block += "'arrive':'" + df.format(a.getArriveTime()) + "',\n";
            block += "'expired':" + (long) Math.floor(a.getSendTime().getTime() / 1000.0) + "\n";
            block += "},\n";
            data += block;
        }

        //remove last comma
        data = data.substring(0, data.length() - 2);
        data += ");\n";


        String param = "";
        param += "win.showAttacksInVillageInfo = " + ((pShowAttacksInVillageInfo) ? 1 : 0) + ";\n";
        param += "win.showAttacksOnConfirmPage = " + ((pShowAttacksOnConfirmPage) ? 1 : 0) + ";\n";
        param += "win.showAttackOnCommandPage = " + ((pShowAttacksOnCommandPage) ? 1 : 0) + ";\n";
        param += "win.showAttacksInOverview = " + ((pShowAttacksInOverview) ? 1 : 0) + ";\n";

        tmpl = tmpl.replaceAll("\\$\\$DATA_LOCATION", data);
        tmpl = tmpl.replaceAll("\\$\\$PARAMETER_LOCATION", param);
        try {
            logger.debug(" - writing data to 'zz_attack_info.user.js'");
            FileWriter f = new FileWriter("./zz_attack_info.user.js");
            f.write(tmpl);
            f.flush();
            f.close();
        } catch (Exception e) {
            logger.error("Failed to write script to target file 'attack_info.user.js'", e);
            return false;
        }

        logger.info("Script written successfully");
        return true;
        /*var attacks = new Array({
        'type':0,
        'source':111217,
        'target':123456,
        'unit':0,
        'send':'02:00:00',
        'arrive':'27.09.2009 12:00:00',
        'finished':1255168681
        },
        {
        'type':0,
        'source':111217,
        'target':123456,
        'unit':0,
        'send':'27.09.2009 02:00:00',
        'arrive':'27.09.2009 12:00:00',
        'finished':1255168681
        },
        {
        'type':2,
        'source':104232,
        'target':123456,
        'unit':0,
        'send':'27.09.2009 02:00:00',
        'arrive':'27.09.2009 12:00:00',
        'finished':1255168681
        }
        );*/

    }

    public static String char2Unicode(char c) {
        StringBuilder sb = new StringBuilder(Integer.toHexString(c));
        int len = sb.length();
        switch (len) {
            case 0:
                sb.insert(0, "\\u0000");
                break;
            case 1:
                sb.insert(0, "\\u000");
                break;
            case 2:
                sb.insert(0, "\\u00");
                break;
            case 3:
                sb.insert(0, "\\u0");
                break;
            case 4:
                sb.insert(0, "\\u");
        }
        return sb.toString();
    }

    private static String toUnicode(String pString) {
        String res = "";
        for (char c : pString.toCharArray()) {
            res += "\\" + char2Unicode(c);
        }
        return res;
    }
}
