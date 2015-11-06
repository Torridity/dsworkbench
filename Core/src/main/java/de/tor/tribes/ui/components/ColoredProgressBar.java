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
package de.tor.tribes.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.SystemColor;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 *
 * @author Torridity
 */
public class ColoredProgressBar extends JProgressBar {

    public ColoredProgressBar(int start, int end) {
        setMinimum(start);
        setMaximum(end);
        setForeground(SystemColor.window);
        setBackground(SystemColor.window);
        setBorder(new EmptyBorder(3, 5, 3, 5));
        Dimension size = new Dimension(300, 20);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        BasicProgressBarUI ui = new BasicProgressBarUI() {

            protected Color getSelectionForeground() {
                return Color.BLACK;
            }

            protected Color getSelectionBackground() {
                return Color.BLACK;
            }
        };
        setUI(ui);
    }
}