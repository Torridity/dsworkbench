/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author jejkal
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