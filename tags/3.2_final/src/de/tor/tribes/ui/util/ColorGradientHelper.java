/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.util;

import java.awt.Color;

/**
 *
 * @author Torridity
 */
public class ColorGradientHelper {

    public static Color getGradientColor(float pGradientPercentage, Color pEmptyColor, Color pDefaultColor) {
        Color result = null;
        if (pGradientPercentage == 0.0f) {
            result = pEmptyColor;
        } else if (pGradientPercentage <= 100.0f && pGradientPercentage > 50.0f) {
            float ratio = (pGradientPercentage - 50.0f) / 50.0f;
            Color c1 = Color.YELLOW;
            Color c2 = Color.GREEN;
            int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
            int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
            int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
            result = new Color(red, green, blue);
        } else if (pGradientPercentage <= 50.0f) {
            float ratio = pGradientPercentage / 50.0f;
            Color c1 = Color.RED;
            Color c2 = Color.YELLOW;
            int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
            int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
            int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));

            red = Math.max(0, red);
            green = Math.max(0, green);
            blue = Math.max(0, blue);

            red = Math.min(255, red);
            green = Math.min(255, green);
            blue = Math.min(255, blue);
            result = new Color(red, green, blue);
        } else {
            //default renderer and color
            result = pDefaultColor;
        }
        return result;
    }
}
