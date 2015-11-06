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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Charon
 */
public class UnitOrderBuilder {

    private static JFrame frame = null;
    private static UnitHolder unit = null;
    private static String server = null;

    public static void showUnitOrder(Component pParent, UnitHolder pUnit) {
        if ((frame == null) || (unit != pUnit) || ((server != null) && (!server.equals(GlobalOptions.getSelectedServer())))) {
            Point oldLoc = null;
            if (frame != null) {
                oldLoc = frame.getLocation();
                frame.setVisible(false);
                frame.dispose();
            }
            unit = pUnit;
            frame = new JFrame();
            frame.setTitle("Einheitenübersicht");
            frame.getContentPane().setLayout(new java.awt.GridLayout(DataHolder.getSingleton().getUnits().size() + 1, 0, 0, 3));

            UnitHolder[] units = DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{});
            Arrays.sort(units, new Comparator<UnitHolder>() {

                @Override
                public int compare(UnitHolder o1, UnitHolder o2) {
                    if (o1.getSpeed() == o2.getSpeed()) {
                        return 0;
                    } else if (o1.getSpeed() < o2.getSpeed()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(0);
            for (UnitHolder u : units) {
                JLabel l = new JLabel(u.getName() + " (" + nf.format(u.getSpeed()) + " Minuten pro Feld) ");
                l.setIcon(ImageManager.getUnitIcon(u));
                if ((unit != null) && (u.equals(unit))) {
                    l.setBackground(Constants.DS_BACK_LIGHT);
                } else {
                    l.setBackground(Constants.DS_BACK);
                }
                l.setOpaque(true);
                l.setIconTextGap(4);
                frame.getContentPane().add(l);
            }
            JButton close = new JButton("Schließen");
            close.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        frame.dispose();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            close.setBackground(Constants.DS_BACK_LIGHT);
            frame.getContentPane().add(close);
            frame.pack();
            frame.setLocationRelativeTo(pParent);
            if (oldLoc != null) {
                frame.setLocation(oldLoc);
            }
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);
        } else {
            if (!frame.isVisible()) {
                frame.setLocationRelativeTo(pParent);
                frame.setVisible(true);
            }
        }
    }
}
