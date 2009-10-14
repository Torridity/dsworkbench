/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.js;

import de.tor.tribes.types.Attack;
import de.tor.tribes.util.DSCalculator;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Charon
 */
public class AttackScriptWriter {

    public static void writeAttackScript(List<Attack> pAttacks) {
        String tmpl = "";
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream("./Scripts/show.tmpl")));
            String line = "";
            while ((line = r.readLine()) != null) {
                tmpl += line + "\n";
            }
            r.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            block += "'targetName':'" + a.getTarget().toString() + "',\n";
            //unit
            block += "'unit':'" + a.getUnit().getPlainName() + ".png',\n";
            //times
            long sendTime = a.getArriveTime().getTime() - ((long) DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
            //long remaining = Math.round((sendTime - System.currentTimeMillis()) / 1000);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(sendTime);
            int hours = c.get(Calendar.HOUR_OF_DAY);
            int minutes = c.get(Calendar.MINUTE);
            int seconds = c.get(Calendar.SECOND);


            block += "'timerValue':'" + (hours * 3600 + minutes * 60 + seconds) + "',\n";
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
           /* if (isToday(sendTime)) {
                df = new SimpleDateFormat("'heute, 'HH:mm:ss");
            } else if (isTomorrow(sendTime)) {
                df = new SimpleDateFormat("'morgen, 'HH:mm:ss");
            } else {
                df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            }*/

            block += "'send':'" + df.format(new Date(sendTime)) + "',\n";

            /*if (isToday(a.getArriveTime().getTime())) {
                df = new SimpleDateFormat("'heute, 'HH:mm:ss");
            } else if (isTomorrow(a.getArriveTime().getTime())) {
                df = new SimpleDateFormat("'morgen, 'HH:mm:ss");
            } else {
                df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            }*/
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
            FileWriter f = new FileWriter("show.js");
            f.write(tmpl);
            f.flush();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done");
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

    private static boolean isToday(long pTime) {
        Calendar c = Calendar.getInstance();
        int thisDay = c.get(Calendar.DAY_OF_MONTH);
        int thisMonth = c.get(Calendar.MONTH);
        int thisYear = c.get(Calendar.YEAR);
        c.setTimeInMillis(pTime);
        int theOtherDay = c.get(Calendar.DAY_OF_MONTH);
        int theOtherMonth = c.get(Calendar.MONTH);
        int theOtherYear = c.get(Calendar.YEAR);
        return ((thisDay == theOtherDay) && (thisMonth == theOtherMonth) && (thisYear == theOtherYear));
    }

    private static boolean isTomorrow(long pTime) {
        Calendar c = Calendar.getInstance();
        int thisDay = c.get(Calendar.DAY_OF_MONTH);
        int thisMonth = c.get(Calendar.MONTH);
        int thisYear = c.get(Calendar.YEAR);
        c.setTimeInMillis(pTime);
        int theOtherDay = c.get(Calendar.DAY_OF_MONTH);
        int theOtherMonth = c.get(Calendar.MONTH);
        int theOtherYear = c.get(Calendar.YEAR);
        return ((thisDay + 1 == theOtherDay) && (thisMonth == theOtherMonth) && (thisYear == theOtherYear));

    }
}
