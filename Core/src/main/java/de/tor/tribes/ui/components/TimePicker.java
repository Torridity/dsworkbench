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

import de.tor.tribes.dssim.Constants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import org.apache.commons.lang.time.DateUtils;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TimePicker.java
 *
 * Created on 18.02.2010, 14:25:29
 */
/**
 *
 * @author Torridity
 */
public class TimePicker extends javax.swing.JPanel {

    private static final Color highlight = new Color(255, 255, 204);
    private JPanel minutePanel = new JPanel();
    private boolean minutesExpanded = false;
    private static final Font smallFont = new Font("Dialog", 0, 10);
    private static final Font largeFont = new Font("Dialog", 0, 12);
    private int pHour = 20;
    private int pMinute = 55;
    private CrossedLabel selectedHour = null;
    private CrossedLabel selectedMinute = null;
    private JPanel hourPanel = new JPanel();
    private CrossedLabel[][] hourLabels = new CrossedLabel[12][2];
    private CrossedLabel[][] minuteLabels = null;
    private JDialog pParent;

    /** Creates new form TimePicker */
    public TimePicker(Date pDate) {
        initComponents();
        Calendar cal = Calendar.getInstance();
        if (pDate != null) {
            cal.setTime(pDate);
        }
        pHour = cal.get(Calendar.HOUR_OF_DAY);
        pMinute = cal.get(Calendar.MINUTE);
        init();
        setBorder(new javax.swing.plaf.BorderUIResource.EtchedBorderUIResource());
    }

    public TimePicker() {
        this(null);
    }

    public void setParent(JDialog parent) {
        pParent = parent;
    }

    public Date getTime() {

        Date d = new GregorianCalendar(0, 0, 0, pHour, pMinute).getTime();
        d = DateUtils.setSeconds(d, 0);
        d = DateUtils.setMilliseconds(d, 0);
        return d;
    }

    private void init() {
        setLayout(new AbsoluteLayout());
        hourPanel.setLayout(new AbsoluteLayout());
        hourPanel.setBackground(Color.RED);
        add(hourPanel, new AbsoluteConstraints(10, 10, 240, 40));
        addHourLabels();
        addMinuteLabels(false);
    }

    private void updateSize() {
        if (minutesExpanded) {
            setMinimumSize(new Dimension(260, 260));
            setMaximumSize(getMinimumSize());
            setPreferredSize(getMinimumSize());
        } else {
            setMinimumSize(new Dimension(260, 160));
            setMaximumSize(getMinimumSize());
            setPreferredSize(getMinimumSize());
        }
        if (pParent != null) {
            pParent.pack();
        }
    }

    public void addHourLabels() {
        int row = -1;
        for (int j = 0; j < 24; j++) {
            int col = j % 12;
            if (j % 12 == 0) {
                row++;
            }
            hourLabels[col][row] = new CrossedLabel();
            CrossedLabel label = hourLabels[col][row];

            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            if (j < 10) {
                label.setText("0" + Integer.toString(j));
            } else {
                label.setText(Integer.toString(j));
            }

            label.setBorder(javax.swing.BorderFactory.createLineBorder(Constants.DS_BACK));
            label.setHorizontalAlignment(0);
            label.setOpaque(true);
            label.setBackground(Constants.DS_BACK_LIGHT);
            label.setFont(smallFont);
            label.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedHour != null) {
                        selectedHour.uncross();
                        selectedHour.setBackground(Constants.DS_BACK_LIGHT);
                    }
                    selectedHour = ((CrossedLabel) e.getSource());
                    selectedHour.cross();
                    pHour = Integer.parseInt(selectedHour.getText());
                }

                public void mouseEntered(MouseEvent e) {
                    ((CrossedLabel) e.getSource()).setBackground(highlight);
                }

                public void mouseExited(MouseEvent e) {
                    ((CrossedLabel) e.getSource()).setBackground(Constants.DS_BACK_LIGHT);
                }
            });

            if (j == pHour) {
                selectedHour = label;
                selectedHour.cross();
            }

            hourPanel.add(label, new AbsoluteConstraints(col * 20, row * 20, 20, 20));
        }
    }

    public void addMinuteLabels(boolean pEachMinute) {
        minutePanel.removeAll();
        minutesExpanded = pEachMinute;
        minutePanel.setLayout(new AbsoluteLayout());
        minutePanel.setBackground(Color.WHITE);
        int max = 0;
        int elemsPerRow = 0;
        if (pEachMinute) {
            //12 elems per row, 5 rows
            max = 60;
            elemsPerRow = 8;
            minuteLabels = new CrossedLabel[8][8];
        } else {
            //6 elems per row, 2 rows
            max = 12;
            elemsPerRow = 6;
            minuteLabels = new CrossedLabel[6][2];
        }
        int rowWidth = 240 / elemsPerRow;
        int rowHeight = 20;
        add(minutePanel, new AbsoluteConstraints(10, 50, 240, rowHeight * (max / elemsPerRow) + 3 * rowHeight));
        int row = 0;
        for (int j = 0; j < max; j++) {
            int col = j % elemsPerRow;
            if (j != 0 && j % elemsPerRow == 0) {
                row++;
            }
            minuteLabels[col][row] = new CrossedLabel();
            CrossedLabel label = minuteLabels[col][row];
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            int num = j;
            if (!pEachMinute) {
                num = j * 5;
            }
            if (num < 10) {
                label.setText(":0" + Integer.toString(num));
            } else {
                label.setText(":" + Integer.toString(num));
            }
            label.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
            label.setHorizontalAlignment(0);
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setFont(smallFont);

            label.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    if (selectedMinute != null) {
                        selectedMinute.uncross();
                        selectedMinute.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
                    }
                    selectedMinute = ((CrossedLabel) e.getSource());
                    pMinute = Integer.parseInt(selectedMinute.getText().replaceAll(":", ""));
                    selectedMinute.cross();
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                    ((CrossedLabel) e.getSource()).setBackground(highlight);
                }

                public void mouseExited(MouseEvent e) {
                    ((CrossedLabel) e.getSource()).setBackground(Color.WHITE);
                }
            });

            if (pEachMinute) {
                if (j == pMinute) {
                    selectedMinute = label;
                    selectedMinute.cross();
                }
            } else {
                int min = (int) Math.rint(pMinute / 5) * 5;
                if (j * 5 == min) {
                    selectedMinute = label;
                    selectedMinute.cross();
                }
            }

            minutePanel.add(label, new AbsoluteConstraints(col * rowWidth, row * rowHeight, rowWidth, rowHeight));
        }
        BasicArrowButton expandButton = null;

        if (pEachMinute) {
            expandButton = new BasicArrowButton(BasicArrowButton.WEST);
        } else {
            expandButton = new BasicArrowButton(BasicArrowButton.EAST);
        }
        expandButton.setFont(smallFont);
        expandButton.setMargin(new Insets(2, 2, 2, 2));
        expandButton.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                addMinuteLabels(!minutesExpanded);
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
        minutePanel.add(expandButton, new AbsoluteConstraints(240 - rowWidth, rowHeight * (max / elemsPerRow), rowWidth, rowHeight));

        JButton okButton = new JButton("OK");
        JButton nowButton = new JButton("Jetzt");
        okButton.setMargin(new Insets(2, 2, 2, 2));
        nowButton.setMargin(new Insets(2, 2, 2, 2));
        okButton.setFont(largeFont);
        nowButton.setFont(largeFont);
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                setVisible(false);
            }
        });
        nowButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
            }
        });
        minutePanel.add(okButton, new AbsoluteConstraints(100, rowHeight * (max / elemsPerRow) + rowHeight + 10, 50, 20));
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                minutePanel.updateUI();
            }
        });
        updateSize();
    }

    public void updateTime() {
        int col = pHour % 12;
        int row = (int) Math.floor(pHour / 12);
        if (selectedHour != null) {
            selectedHour.uncross();
        }
        hourLabels[col][row].cross();
        if (minutesExpanded) {
            col = pMinute % 8;
            row = (int) Math.floor(pMinute / 8);
        } else {
            col = pMinute % 6;
            row = (int) Math.floor(pMinute / 6);
        }
        if (selectedMinute != null) {
            selectedMinute.uncross();
        }
        minuteLabels[col][row].cross();
    }

    private void onNow(ActionEvent actionevent) {
        Calendar cal = Calendar.getInstance();
        pHour = cal.get(Calendar.HOUR_OF_DAY);
        pMinute = cal.get(Calendar.MINUTE);

        /*selectedDate = getToday();
        if (isVisible()) {
        monthAndYear.setText(formatDateText(selectedDate.getTime()));
        calculateCalendar();
        }*/
    }

    public static void main(String[] args) {
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        JDialog f = new JDialog();
        f.getContentPane().add(new TimePicker(Calendar.getInstance().getTime()));
        //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
