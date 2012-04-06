/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import de.tor.tribes.util.GlobalOptions;
import java.awt.Font;
import javax.swing.JButton;

/**
 *
 * @author Torridity
 */
public class RibbonLayoutConstants {

    public static int TASK_BAR_HEIGHT = 24;//24
    public static int TASK_TOGGLE_BUTTON_HEIGHT = 22;//22
    public static int TILE_SIZE = 32;//32
    public static int MAX_SIZE = 32;//32
    public static int MED_SIZE = 16;//16
    public static int MIN_SIZE = 16;//16
    public static Font FONT = new JButton("").getFont();//12

    static {
        if (Boolean.parseBoolean(GlobalOptions.getProperty("half.ribbon.size"))) {
            TASK_BAR_HEIGHT = 16;//24
            TASK_TOGGLE_BUTTON_HEIGHT = 14;//22
            TILE_SIZE = 16;//32
            MAX_SIZE = 16;//32
            MED_SIZE = 8;//16
            MIN_SIZE = 8;//16
            FONT = new JButton("").getFont().deriveFont(10f);//12
        }
    }
}
