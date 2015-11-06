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
