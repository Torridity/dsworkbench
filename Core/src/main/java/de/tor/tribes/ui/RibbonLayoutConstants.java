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
package de.tor.tribes.ui;

import de.tor.tribes.util.GlobalOptions;
import java.awt.Font;
import javax.swing.JButton;

/**
 * used at RibbonConfigurator
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
        if (GlobalOptions.getProperties().getBoolean("half.ribbon.size")) {
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
