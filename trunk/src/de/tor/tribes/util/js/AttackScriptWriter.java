/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.js;

import de.tor.tribes.types.Attack;
import de.tor.tribes.util.DSCalculator;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 * @TODO (DIFF) Milliseconds shown for exported times
 */
public class AttackScriptWriter {

    private static Logger logger = Logger.getLogger("AttackScriptWriter");

    public static boolean writeAttackScript(List<Attack> pAttacks) {
        logger.debug("Start writing attack script");
        String tmpl = "";
        try {
            logger.debug(" - reading template");
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream("./Scripts/show.tmpl")));
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
        String data = "var attacks = new Array(";
        for (Attack a : pAttacks) {
            //set type
            String block = "{\n'type':";
            switch (a.getType()) {
                case Attack.NO_TYPE:
                    block += ",";
                    break;
                case Attack.CLEAN_TYPE:
                    block += "'axe.png',\n";
                    break;
                case Attack.FAKE_TYPE:
                    block += "'fake.png',\n";
                    break;
                case Attack.SNOB_TYPE:
                    block += "'snob.png',\n";
                    break;
                case Attack.SUPPORT_TYPE:
                    block += "'def.png',\n";
                    break;
            }
            //set source and target
            block += "'source':" + a.getSource().getId() + ",\n";
            block += "'target':" + a.getTarget().getId() + ",\n";
            block += "'targetName':'" + toUnicode(a.getTarget().toString()) + "',\n";
            //unit
            block += "'unit':'" + a.getUnit().getPlainName() + ".png',\n";
            //times
            long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);

            Calendar midnight = Calendar.getInstance();
            midnight.set(Calendar.HOUR_OF_DAY, 0);
            midnight.set(Calendar.MINUTE, 0);
            midnight.set(Calendar.SECOND, 0);
            midnight.set(Calendar.MILLISECOND, 0);
            //calculate difference from today midnight
            long diff = sendTime - midnight.getTimeInMillis();
            block += "'timerValue':'" + (int) Math.round((double) diff / 1000.0) + "',\n";
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
            block += "'send':'" + df.format(new Date(sendTime)) + "',\n";
            block += "'arrive':'" + df.format(a.getArriveTime()) + "',\n";
            block += "'expired':" + (long) Math.floor((long) sendTime / 1000) + "\n";
            block += "},\n";
            data += block;
        }

        //remove last comma
        data = data.substring(0, data.length() - 2);
        data += ");\n";

        tmpl = tmpl.replaceAll("//DATA_LOCATION", data);
        try {
            logger.debug(" - writing data to 'attack_info.user.js'");
            FileWriter f = new FileWriter("./attack_info.user.js");
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
        StringBuffer sb = new StringBuffer(Integer.toHexString(c));
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

    public static void main(String[] args) {
        int v = 136628250;
        int w = 24 * 60 * 60 * 1000;
        System.out.println(v);
        System.out.println(w);
        System.out.println(Math.floor(v / (24 * 60 * 60 * 1000)));
    }
}
