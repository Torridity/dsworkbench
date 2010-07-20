/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Charon
 */
public class TestClass {

    public static void main(String[] args) throws Exception {


        //Runtime.getRuntime().exec(new String[]{"C:/Programme/Internet Explorer/iexplore.exe", "http://www.heise.de"});

        File dest = new File("C:/Users/Torridity/die stämme/dsworkbench/./servers/de25/settings.xml");
        System.out.println(new File("settings_tmp.xml").exists());
        System.out.println(dest.getCanonicalPath());
        if (new File("settings_tmp.xml").renameTo(dest)) {
            System.out.println("SUCCESS");
        } else {
            System.out.println("FAIL");
        }

        if (true) {
            return;
        }









        String url = "http://www.s213116505.online.de/village_interface.php?pw=a402daabeb2a1c861266544ef56c161c&villages=";
        BufferedReader fr = new BufferedReader(new FileReader(new File("H:/Software/DSWorkbench/servers/de28/serverdata")));
        String line = fr.readLine();
        int cnt = 80;
        while ((line = fr.readLine()) != null) {
            String[] split = line.split(",");
            url += split[0] + ";";
            cnt--;
            if (cnt <= 0) {
                break;
            }
        }
        url += "194925";
        System.out.println(url);

        long s = System.currentTimeMillis();
        URL u = new URL(url);
        URLConnection con = u.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        line = "";
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("" + (System.currentTimeMillis() - s));
        if (true) {
            return;
        }

        int x1 = 465;
        int y1 = 443;
        int x2 = 479;
        int y2 = 451;
        double ram = 29.9999999976;
        double snob = 34.9999999993;
        double d = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        double t = d * ram * 60000;
        //System.out.println("T (min) " + t);
        long arrive = 1223762399000l;
        //System.out.println("t " + t);
        long tl = (long) t;
        //System.out.println("t " + tl);
        long tt = arrive - tl;//15:56:14.873

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

        Date da = new Date(arrive - tl);
        Calendar c = Calendar.getInstance();
        c.setTime(da);
        System.out.println(c.get(Calendar.SECOND));
        System.out.println(c.get(Calendar.MILLISECOND));

        System.out.println(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date(arrive - tl)));
    }
}
