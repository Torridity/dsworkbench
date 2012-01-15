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

    public final static double VERSION = 3.1;
    public final static String VERSION_ADDITION = "";
    public final static Color DS_BACK = new Color(225, 213, 190);
    public final static Color DS_BACK_LIGHT = new Color(239, 235, 223);
    public final static Color DS_ROW_A = new Color(246, 235, 202);
    public final static Color DS_ROW_B = new Color(251, 244, 221);
    public final static Color DS_DEFAULT_MARKER = new Color(130, 60, 10);
    public final static Color ENEMY_MARKER = Color.RED;
    public final static Color NAP_MARKER = new Color(127, 0, 127);
    public final static Color ALLY_MARKER = new Color(0, 160, 244);
    public final static String SERVER_DIR = "./servers";
    public static Hashtable<String, Integer> LAYERS = null;
    public final static int LAYER_COUNT = 9;
    public final static boolean DEBUG = false;

    static {
        LAYERS = new Hashtable<String, Integer>();
        LAYERS.put("Markierungen", 0);
        LAYERS.put("Dörfer", 1);
        LAYERS.put("Dorfsymbole", 2);
        LAYERS.put("Truppendichte", 3);
        LAYERS.put("Notizmarkierungen", 4);
        LAYERS.put("Angriffe", 5);
        LAYERS.put("Unterstützungen", 6);
        LAYERS.put("Zeichnungen", 7);
        LAYERS.put("Kirchenradien", 8);
        if (DEBUG) {
            System.err.println("DEBUG MODE ENABLED!");
        }
    }
}