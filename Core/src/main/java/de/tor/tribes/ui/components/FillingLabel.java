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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Torridity
 */
public class FillingLabel extends JLabel {

    private double[] fillings = null;
    private Color[] colors = null;
    private String text = "";

    public void setData(double[] fillings, double capacity) {
        this.fillings = fillings;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        double res = 0;
        for (Double v : fillings) {
            res += v * capacity;
        }
        res /= 1000;
        text = nf.format(res) + " K";
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        if (fillings == null || colors == null || fillings.length != colors.length) {
            return;
        }
        int h = getHeight() / fillings.length;
        if (h == 0) {
            return;
        }
        for (int i = 0; i < fillings.length; i++) {
            g2d.setColor(colors[i]);
            g2d.fill3DRect(0, i * h, (int) Math.rint(getWidth() * fillings[i]), h, true);
        }
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 1, getHeight() - 1);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FillingLabel l = new FillingLabel();
        l.setPreferredSize(new Dimension(100, 24));
        l.setData(new double[]{134644.0 / 400000.0, 180000.0 / 400000.0, 161743.0 / 400000.0}, 400000.0);
        l.setColors(new Color[]{new Color(187, 148, 70), new Color(242, 131, 30), new Color(224, 211, 209)});
        f.getContentPane().add(l);
        f.pack();
        f.setVisible(true);
    }
}
