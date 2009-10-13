/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.js;

import de.tor.tribes.types.Attack;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
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
            block += "'send':'" + "02:00:00" + "',\n";
            block += "'arrive':'" + "12.12.2111 12:32:12" + "',\n";
            block += "'expired':" + (int)(System.currentTimeMillis()/1000) + "\n";
            block += "},\n";
            data += block;
        }

        //remove last comma
        data = data.substring(0, data.length() - 2);
        data += ");\n";

        tmpl = tmpl.replaceAll("//DATA_LOCATION", data);
        try{
        FileWriter f = new FileWriter("show.js");
        f.write(tmpl);
        f.flush();
        f.close();
        }catch(Exception e){
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
}
