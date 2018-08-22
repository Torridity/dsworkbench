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

import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JDialog;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;
import org.apache.commons.lang3.time.DateUtils;

/**
 *
 * @author Torridity
 */
public class TimePicker extends javax.swing.JPanel {

    private static final Color highlight = new Color(255, 255, 204);
    private boolean minutesExpanded = false;
    private static final Font smallFont = new Font("Dialog", 0, 10);
    private int pHour = 20;
    private int pMinute = 55;
    private CrossedLabel selectedHour = null;
    private CrossedLabel selectedMinute = null;
    private final CrossedLabel[] hourLabels = new CrossedLabel[24];
    private final CrossedLabel[] minuteLabels = new CrossedLabel[60];
    private JDialog pParent;
    
    /**
     * Creates new form TimePicker
     */
    public TimePicker(Date pDate) {
        Calendar cal = Calendar.getInstance();
        if (pDate != null) {
            cal.setTime(pDate);
        }
        pHour = cal.get(Calendar.HOUR_OF_DAY);
        pMinute = cal.get(Calendar.MINUTE);
        initComponents();
        initSpecialComponents();
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

    private void initSpecialComponents() {
        for(int i = 0; i < hourLabels.length; i++) {
            CrossedLabel label = new CrossedLabel();
            hourLabels[i] = label;
            
            if (i < 10) {
                label.setText("0" + Integer.toString(i));
            } else {
                label.setText(Integer.toString(i));
            }
            
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            label.setBorder(javax.swing.BorderFactory.createLineBorder(Constants.DS_BACK));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(Constants.DS_BACK_LIGHT);
            label.setFont(smallFont);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedHour != null) {
                        selectedHour.uncross();
                    }
                    selectedHour = ((CrossedLabel) e.getSource());
                    selectedHour.cross();
                    
                    for(int i = 0; i < hourLabels.length; i++)
                        if(hourLabels[i] == selectedHour) {
                            pHour = i;
                            break;
                        }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    ((CrossedLabel) e.getSource()).setBackground(highlight);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    ((CrossedLabel) e.getSource()).setBackground(Constants.DS_BACK_LIGHT);
                }
            });
        }
        addHourLabels();
        
        for(int i = 0; i < minuteLabels.length; i++) {
            CrossedLabel label = new CrossedLabel();
            minuteLabels[i] = label;
            
            if (i < 10) {
                label.setText(":0" + Integer.toString(i));
            } else {
                label.setText(":" + Integer.toString(i));
            }
            
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            label.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setFont(smallFont);
            label.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedMinute != null) {
                        selectedMinute.uncross();
                    }
                    selectedMinute = ((CrossedLabel) e.getSource());
                    selectedMinute.cross();
                    
                    for(int i = 0; i < minuteLabels.length; i++)
                        if(minuteLabels[i] == selectedMinute) {
                            pMinute = i;
                            break;
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
                    ((CrossedLabel) e.getSource()).setBackground(highlight);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    ((CrossedLabel) e.getSource()).setBackground(Color.WHITE);
                }
            });
        }
        addMinuteLabels(false);
    }

    private void updateSize() {
        if (minutesExpanded) {
            setMinimumSize(new Dimension(260, 220));
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
        jPanelHour.removeAll();
        for (int i = 0; i < hourLabels.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % 12;
            gbc.gridy = i / 12;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.weighty = 1;
            
            hourLabels[i].uncross();
            jPanelHour.add(hourLabels[i], gbc);
        }
        selectedHour = hourLabels[pHour];
        selectedHour.cross();
    }

    public void addMinuteLabels(boolean pEachMinute) {
        jPanelMinute.removeAll();
        minutesExpanded = pEachMinute;
        int every, elemsPerRow;
        if (pEachMinute) {
            //12 elems per row, 5 rows
            every = 1;
            elemsPerRow = 12;
        } else {
            //6 elems per row, 2 rows
            every = 5;
            elemsPerRow = 6;
        }
        int rowHeight = 20;
        jPanelMinute.setPreferredSize(new Dimension(240, rowHeight * minuteLabels.length / (elemsPerRow * every) + rowHeight));
        jPanelMinute.setMinimumSize(jPanelMinute.getPreferredSize());
        jPanelMinute.setMaximumSize(jPanelMinute.getPreferredSize());
        
        for (int i = 0; i < minuteLabels.length / every; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % elemsPerRow;
            gbc.gridy = i / elemsPerRow;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.weighty = 1;
            
            minuteLabels[i * every].uncross();
            jPanelMinute.add(minuteLabels[i * every], gbc);
        }
        selectedMinute = minuteLabels[pMinute];
        selectedMinute.cross();
        BasicArrowButton expandButton = null;

        if (pEachMinute) {
            expandButton = new BasicArrowButton(BasicArrowButton.WEST);
        } else {
            expandButton = new BasicArrowButton(BasicArrowButton.EAST);
        }
        expandButton.setFont(smallFont);
        expandButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMinuteLabels(!minutesExpanded);
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = elemsPerRow - 1;
        gbc.gridy = minuteLabels.length / (every * elemsPerRow);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        jPanelMinute.add(expandButton, gbc);
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                jPanelMinute.updateUI();
            }
        });
        updateSize();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelHour = new javax.swing.JPanel();
        jPanelMinute = new javax.swing.JPanel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonOK = new javax.swing.JButton();

        setBorder(new javax.swing.plaf.BorderUIResource.EtchedBorderUIResource());
        setLayout(new java.awt.GridBagLayout());

        jPanelHour.setMaximumSize(new java.awt.Dimension(240, 40));
        jPanelHour.setMinimumSize(new java.awt.Dimension(240, 40));
        jPanelHour.setPreferredSize(new java.awt.Dimension(240, 40));
        jPanelHour.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(jPanelHour, gridBagConstraints);

        jPanelMinute.setMaximumSize(new java.awt.Dimension(240, 60));
        jPanelMinute.setMinimumSize(new java.awt.Dimension(240, 60));
        jPanelMinute.setName(""); // NOI18N
        jPanelMinute.setPreferredSize(new java.awt.Dimension(240, 60));
        jPanelMinute.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jPanelMinute, gridBagConstraints);

        jPanelButtons.setMaximumSize(new java.awt.Dimension(240, 40));
        jPanelButtons.setMinimumSize(new java.awt.Dimension(240, 40));
        jPanelButtons.setPreferredSize(new java.awt.Dimension(240, 40));
        jPanelButtons.setLayout(new java.awt.GridBagLayout());

        jButtonOK.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButtonOK.setText("OK");
        jButtonOK.setMaximumSize(new java.awt.Dimension(60, 20));
        jButtonOK.setMinimumSize(new java.awt.Dimension(60, 20));
        jButtonOK.setPreferredSize(new java.awt.Dimension(60, 20));
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanelButtons.add(jButtonOK, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        add(jPanelButtons, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButtonOKActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOK;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelHour;
    private javax.swing.JPanel jPanelMinute;
    // End of variables declaration//GEN-END:variables
}
