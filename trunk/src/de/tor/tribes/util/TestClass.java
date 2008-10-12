/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *@TODO: Find a way to round to milliseconds
 * @author Charon
 */
public class TestClass {

    public static void main(String[] args) {
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
