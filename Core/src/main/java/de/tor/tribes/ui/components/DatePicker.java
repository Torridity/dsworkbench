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
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicArrowButton;
import org.apache.commons.lang3.time.DateUtils;

/**
 *
 * @author extremeCrazyCoder
 */
public class DatePicker extends javax.swing.JPanel {
    private static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    private static final Color GRAY = new Color(200, 200, 200);
    private static final Color BLACK = new Color(0, 0, 0);
    private static final SimpleDateFormat monthAndYear = new SimpleDateFormat("MMMMM yyyy");
    private static final int WEEKS_TO_SHOW = 6;
    
    private CrossedLabel daysInMonth[][];
    private Date datesInMonth[][]; //used for mapping labels with dates
    private Date selectedDate;
    private final Date originalDate;
    private final String dayNames[] = { "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So" };

    /**
     * Creates new form DatePicker
     */

    public DatePicker() {
        initComponents();
        originalDate = DateUtils.truncate(new Date(), Calendar.DATE);
        selectedDate = originalDate;
        init();
    }

    public DatePicker(Date date) {
        initComponents();
        if (date == null) {
            originalDate = DateUtils.truncate(new Date(), Calendar.DATE);
        } else {
            originalDate = DateUtils.truncate(date, Calendar.DATE);
        }
        selectedDate = originalDate;
        init();
    }
    
    private void init() {
        //build Header
        for(int i = 0; i < 7; i++) {
            JLabel head = new JLabel(dayNames[i]);
            head.setBackground(GRAY);
            head.setOpaque(true);
            head.setPreferredSize(new Dimension(20, 20));
            head.setMinimumSize(new Dimension(20, 20));
            head.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.PAGE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.insets = new Insets(2, 2, 6, 2);
            gbc.ipadx = 0;
            gbc.ipady = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            jPanelDaySelection.add(head, gbc);
        }
        
        daysInMonth = new CrossedLabel[WEEKS_TO_SHOW][7];
        datesInMonth = new Date[WEEKS_TO_SHOW][7];
        for(int i = 0; i < WEEKS_TO_SHOW; i++) {
            for(int j = 0; j < 7; j++) {
                daysInMonth[i][j] = new CrossedLabel();
                daysInMonth[i][j].setPreferredSize(new Dimension(20, 20));
                daysInMonth[i][j].setMinimumSize(new Dimension(20, 20));
                daysInMonth[i][j].setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                daysInMonth[i][j].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent mouseevent) {
                        dayClicked(mouseevent);
                    }
                });

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.PAGE_START;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridx = j;
                gbc.gridy = i + 1;
                gbc.insets = new Insets(2, 2, 2, 2);
                gbc.ipadx = 0;
                gbc.ipady = 0;
                gbc.weightx = 1.0;
                gbc.weighty = 0.0;
                jPanelDaySelection.add(daysInMonth[i][j], gbc);
            }
        }
        
        buildCalendar();
    }
    
    public void buildCalendar() {
        jLabelMonth.setText(monthAndYear.format(selectedDate));
        
        Calendar temp = new GregorianCalendar();
        temp.setTime(selectedDate);
        int maxDaysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstdayInMonth = temp.getActualMinimum(Calendar.DAY_OF_MONTH);
        
        temp.set(Calendar.DAY_OF_MONTH, firstdayInMonth);
        int dayOfWeek = mapDayOfWeek(temp.get(Calendar.DAY_OF_WEEK));
        
        int currentField = 0;
        //ensure that at least one day of prev month is shown
        int preDaysToAdd = ((dayOfWeek + 5) % 7) + 1;
        
        temp.add(Calendar.MONTH, -1);
        temp.set(Calendar.DAY_OF_MONTH, temp.getActualMaximum(Calendar.DAY_OF_MONTH) - preDaysToAdd + 1);
        for(; currentField < preDaysToAdd; currentField++) {
            //days belong to last month
            CrossedLabel current = daysInMonth[currentField / 7][currentField % 7];
            current.setText("" + temp.get(Calendar.DAY_OF_MONTH));
            current.setForeground(LIGHT_GRAY);
            datesInMonth[currentField / 7][currentField % 7] = temp.getTime();
            
            temp.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        //normal days of month
        for(int i = firstdayInMonth; i <= maxDaysInMonth; i++, currentField++) {
            CrossedLabel current = daysInMonth[currentField / 7][currentField % 7];
            current.setText("" + temp.get(Calendar.DAY_OF_MONTH));
            current.setForeground(BLACK);
            datesInMonth[currentField / 7][currentField % 7] = temp.getTime();
            
            temp.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        //post days of month
        for(; currentField < WEEKS_TO_SHOW*7; currentField++) {
            CrossedLabel current = daysInMonth[currentField / 7][currentField % 7];
            current.setText("" + temp.get(Calendar.DAY_OF_MONTH));
            current.setForeground(LIGHT_GRAY);
            datesInMonth[currentField / 7][currentField % 7] = temp.getTime();
            
            temp.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        for(int i = 0; i < WEEKS_TO_SHOW * 7; i++) {
            if(selectedDate.equals(datesInMonth[i / 7][i % 7])) {
                daysInMonth[i / 7][i % 7].cross();
            } else {
                daysInMonth[i / 7][i % 7].uncross();
            }
        }
        
    }
    
    public Date getDate() {
        return selectedDate;
    }
    
    /**
     * remaps the days of week of gregorian Calendar to internal values
     */
    private int mapDayOfWeek(int cal) {
        switch(cal) {
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
            case Calendar.SUNDAY:
                return 7;
            default:
                return 1; //should never happen
        }
    }
    
    /**
     * removes the ok button from the form
     */
    public void removeOk() {
        jButtonOK.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelMonthSelection = new javax.swing.JPanel();
        jButtonPrevious = new BasicArrowButton(BasicArrowButton.WEST);
        jLabelMonth = new javax.swing.JLabel();
        jButtonNext = new BasicArrowButton(BasicArrowButton.EAST);
        jPanelDaySelection = new javax.swing.JPanel();
        jPanelBottom = new javax.swing.JPanel();
        jButtonToday = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(170, 240));
        setPreferredSize(new java.awt.Dimension(170, 240));
        setVerifyInputWhenFocusTarget(false);
        setLayout(new java.awt.BorderLayout());

        jPanelMonthSelection.setMinimumSize(new java.awt.Dimension(170, 30));
        jPanelMonthSelection.setPreferredSize(new java.awt.Dimension(170, 30));

        jButtonPrevious.setToolTipText("");
        jButtonPrevious.setMaximumSize(new java.awt.Dimension(20, 20));
        jButtonPrevious.setMinimumSize(new java.awt.Dimension(20, 20));
        jButtonPrevious.setPreferredSize(new java.awt.Dimension(20, 20));
        jButtonPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firePrevNextAction(evt);
            }
        });

        jLabelMonth.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelMonth.setText("Juli 2018");

        jButtonNext.setToolTipText("");
        jButtonNext.setMaximumSize(new java.awt.Dimension(20, 20));
        jButtonNext.setMinimumSize(new java.awt.Dimension(20, 20));
        jButtonNext.setPreferredSize(new java.awt.Dimension(20, 20));
        jButtonNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firePrevNextAction(evt);
            }
        });

        javax.swing.GroupLayout jPanelMonthSelectionLayout = new javax.swing.GroupLayout(jPanelMonthSelection);
        jPanelMonthSelection.setLayout(jPanelMonthSelectionLayout);
        jPanelMonthSelectionLayout.setHorizontalGroup(
            jPanelMonthSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMonthSelectionLayout.createSequentialGroup()
                .addContainerGap(49, Short.MAX_VALUE)
                .addComponent(jButtonPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabelMonth)
                .addGap(4, 4, 4)
                .addComponent(jButtonNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanelMonthSelectionLayout.setVerticalGroup(
            jPanelMonthSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMonthSelectionLayout.createSequentialGroup()
                .addGroup(jPanelMonthSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMonth)
                    .addComponent(jButtonPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        add(jPanelMonthSelection, java.awt.BorderLayout.PAGE_START);
        jPanelMonthSelection.getAccessibleContext().setAccessibleName("");

        jPanelDaySelection.setMinimumSize(new java.awt.Dimension(170, 160));
        jPanelDaySelection.setPreferredSize(new java.awt.Dimension(170, 160));
        jPanelDaySelection.setRequestFocusEnabled(false);
        jPanelDaySelection.setLayout(new java.awt.GridBagLayout());
        add(jPanelDaySelection, java.awt.BorderLayout.CENTER);

        jPanelBottom.setMinimumSize(new java.awt.Dimension(170, 50));
        jPanelBottom.setPreferredSize(new java.awt.Dimension(170, 50));

        jButtonToday.setText("Heute");
        jButtonToday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireTodayAction(evt);
            }
        });

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireOkAcion(evt);
            }
        });

        javax.swing.GroupLayout jPanelBottomLayout = new javax.swing.GroupLayout(jPanelBottom);
        jPanelBottom.setLayout(jPanelBottomLayout);
        jPanelBottomLayout.setHorizontalGroup(
            jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBottomLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonToday)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 97, Short.MAX_VALUE)
                .addComponent(jButtonOK)
                .addContainerGap())
        );
        jPanelBottomLayout.setVerticalGroup(
            jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBottomLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonToday)
                    .addComponent(jButtonOK))
                .addContainerGap())
        );

        add(jPanelBottom, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void firePrevNextAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firePrevNextAction
        if(evt.getSource().equals(jButtonPrevious)) {
            selectedDate = DateUtils.addMonths(selectedDate, -1);
            buildCalendar();
        } else if(evt.getSource().equals(jButtonNext)) {
            selectedDate = DateUtils.addMonths(selectedDate, 1);
            buildCalendar();
        }
    }//GEN-LAST:event_firePrevNextAction

    private void fireTodayAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireTodayAction
        selectedDate = DateUtils.truncate(new Date(), Calendar.DATE);
        buildCalendar();
    }//GEN-LAST:event_fireTodayAction

    private void fireOkAcion(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireOkAcion
        setVisible(false);
    }//GEN-LAST:event_fireOkAcion

    private void dayClicked(MouseEvent mvt) {
        CrossedLabel evtSrc = (CrossedLabel) mvt.getSource();
        for(int i = 0; i < WEEKS_TO_SHOW * 7; i++) {
            if(evtSrc.equals(daysInMonth[i / 7][i % 7])) {
                selectedDate = datesInMonth[i / 7][i % 7];
                break;
            }
        }
        
        buildCalendar();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonNext;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonPrevious;
    private javax.swing.JButton jButtonToday;
    private javax.swing.JLabel jLabelMonth;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JPanel jPanelDaySelection;
    private javax.swing.JPanel jPanelMonthSelection;
    // End of variables declaration//GEN-END:variables
}
