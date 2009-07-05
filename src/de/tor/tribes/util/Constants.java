/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.awt.Color;
import java.util.Hashtable;

/**
 *
 * @author Jejkal
 */
public class Constants {

    public final static double VERSION = 1.6;
    public final static String VERSION_ADDITION = "";
    public final static Color DS_BACK = new Color(225, 213, 190);
    public final static Color DS_BACK_LIGHT = new Color(239, 235, 223);
    public final static String SERVER_DIR = "./servers";
    public static Hashtable<String, Integer> LAYERS = null;
    public final static int LAYER_COUNT = 6;

    static {
        LAYERS = new Hashtable<String, Integer>();
        LAYERS.put("Tagmarkierungen",0);
        LAYERS.put("Dorfinfos",1);
        LAYERS.put("Notizmarkierungen",2);
        LAYERS.put("Angriffsvektoren",3);
        LAYERS.put("Formen",4);
        LAYERS.put("Kirchenradien",5);
    }
}
