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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public class DatePicker extends JPanel {

    private static final Font smallFont = new Font("Dialog", 0, 10);
    // private static final Font largeFont = new Font("Dialog", 0, 10);
    private static final Insets insets = new Insets(1, 1, 1, 1);
    private static final Color highlight = new Color(255, 255, 204);
    private static final Color white = new Color(255, 255, 255);
    private static final Color gray = new Color(204, 204, 204);
    private CrossedLabel selectedDay;
    private GregorianCalendar selectedDate;
    private GregorianCalendar originalDate;
    private final JButton backButton;
    private final JLabel monthAndYear;
    private final JButton forwardButton;
    private final JTextField dayHeadings[] = {
        new JTextField("Mo"), new JTextField("Di"), new JTextField("Mi"), new JTextField("Do"), new JTextField("Fr"), new JTextField("Sa"), new JTextField("So")
    };
    private final CrossedLabel daysInMonth[][] = {
        {
            new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel()
        }, {
            new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel()
        }, {
            new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel()
        }, {
            new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel()
        }, {
            new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel()
        }, {
            new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel(), new CrossedLabel()
        }
    };
    private final JButton todayButton;
    private final JButton cancelButton;

    public DatePicker() {
        selectedDay = null;
        selectedDate = null;
        originalDate = null;
        backButton = new BasicArrowButton(BasicArrowButton.WEST);//JButton();
        monthAndYear = new JLabel();
        forwardButton = new BasicArrowButton(BasicArrowButton.EAST);//new JButton();
        todayButton = new JButton();
        cancelButton = new JButton();
        selectedDate = getToday();
        init();
    }

    public DatePicker(Date date) {
        selectedDay = null;
        selectedDate = null;
        originalDate = null;
        backButton = new BasicArrowButton(BasicArrowButton.WEST);//new JButton();
        monthAndYear = new JLabel();
        forwardButton = new BasicArrowButton(BasicArrowButton.EAST);//new JButton();
        todayButton = new JButton();
        cancelButton = new JButton();
        if (date == null) {
            selectedDate = getToday();
        } else {
            selectedDate = new GregorianCalendar();
            selectedDate.setTime(date);
        }
        originalDate = new GregorianCalendar(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
        init();
    }

    public Date getDate() {
        if (null != selectedDate) {
            return selectedDate.getTime();
        } else {
            return null;
        }
    }

    private void init() {
        setLayout(new AbsoluteLayout());
        setMinimumSize(new Dimension(161, 220));
        setMaximumSize(getMinimumSize());
        setPreferredSize(getMinimumSize());
        setBorder(new javax.swing.plaf.BorderUIResource.EtchedBorderUIResource());
        backButton.setFont(smallFont);
        //  backButton.setText("<");
        backButton.setMargin(insets);
        backButton.setDefaultCapable(false);
        backButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                onBackClicked(actionevent);
            }
        });
        add(backButton, new AbsoluteConstraints(10, 10, 20, 20));
        //    monthAndYear.setFont(largeFont);
        monthAndYear.setHorizontalAlignment(0);
        monthAndYear.setText(formatDateText(selectedDate.getTime()));
        add(monthAndYear, new AbsoluteConstraints(30, 10, 100, 20));
        forwardButton.setFont(smallFont);
        //forwardButton.setText(">");
        forwardButton.setMargin(insets);
        forwardButton.setDefaultCapable(false);
        forwardButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                onForwardClicked(actionevent);
            }
        });
        add(forwardButton, new AbsoluteConstraints(130, 10, 20, 20));
        int i = 10;
        for (JTextField dayHeading : dayHeadings) {
            dayHeading.setBackground(gray);
            dayHeading.setEditable(false);
            dayHeading.setFont(smallFont);
            dayHeading.setHorizontalAlignment(0);
            dayHeading.setBorder(BorderFactory.createEmptyBorder());
            dayHeading.setFocusable(false);
            add(dayHeading, new AbsoluteConstraints(i, 40, 21, 21));
            i += 20;
        }

        i = 10;
        int k = 60;
        for (CrossedLabel[] aDaysInMonth : daysInMonth) {
            for (int i1 = 0; i1 < aDaysInMonth.length; i1++) {
                aDaysInMonth[i1].setBackground(gray);
                aDaysInMonth[i1].setFont(smallFont);
                aDaysInMonth[i1].setHorizontalAlignment(4);
                aDaysInMonth[i1].setText("");
                aDaysInMonth[i1].setFocusable(false);
                aDaysInMonth[i1].addMouseListener(new MouseAdapter() {

                    public void mouseClicked(MouseEvent mouseevent) {
                        onDayClicked(mouseevent);
                    }
                });
                add(aDaysInMonth[i1], new AbsoluteConstraints(i, k, 21, 21));
                i += 20;
            }

            i = 10;
            k += 20;
        }

        initButtons(true);
        calculateCalendar();
    }

    private void initButtons(boolean flag) {
        if (flag) {
            Dimension dimension = new Dimension(68, 24);
            //  todayButton.setFont(largeFont);
            todayButton.setText("Heute");
            todayButton.setMargin(insets);
            todayButton.setMaximumSize(dimension);
            todayButton.setMinimumSize(dimension);
            todayButton.setPreferredSize(dimension);
            todayButton.setDefaultCapable(true);
            todayButton.setSelected(true);
            todayButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent) {
                    onToday(actionevent);
                }
            });
            //   cancelButton.setFont(largeFont);
            cancelButton.setText("OK");
            cancelButton.setMargin(insets);
            cancelButton.setMaximumSize(dimension);
            cancelButton.setMinimumSize(dimension);
            cancelButton.setPreferredSize(dimension);
            cancelButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent) {
                    setVisible(false);
                }
            });
        } else {
            remove(todayButton);
            remove(cancelButton);
        }

        add(todayButton, new AbsoluteConstraints(9, 190, 68, -1));
        add(cancelButton, new AbsoluteConstraints(87, 190, 48, -1));
    }

    public void removeOk() {
        remove(cancelButton);
    }

    private void onToday(ActionEvent actionevent) {
        selectedDate = getToday();
        if (isVisible()) {
            monthAndYear.setText(formatDateText(selectedDate.getTime()));
            calculateCalendar();
        }
    }

    private void onCancel(ActionEvent actionevent) {
        selectedDate = originalDate;
        setVisible(false);
    }

    private void onForwardClicked(ActionEvent actionevent) {
        int i = selectedDate.get(Calendar.DAY_OF_MONTH);
        selectedDate.set(Calendar.DAY_OF_MONTH, 1);
        selectedDate.add(Calendar.MONTH, 1);
        selectedDate.set(Calendar.DAY_OF_MONTH, Math.min(i, calculateDaysInMonth(selectedDate)));
        monthAndYear.setText(formatDateText(selectedDate.getTime()));
        if (selectedDay != null) {
            selectedDay.uncross();
        }
        calculateCalendar();
    }

    private void onBackClicked(ActionEvent actionevent) {
        int i = selectedDate.get(Calendar.DAY_OF_MONTH);
        selectedDate.set(Calendar.DAY_OF_MONTH, 1);
        selectedDate.add(Calendar.MONTH, -1);
        selectedDate.set(Calendar.DAY_OF_MONTH, Math.min(i, calculateDaysInMonth(selectedDate)));
        monthAndYear.setText(formatDateText(selectedDate.getTime()));
        if (selectedDay != null) {
            selectedDay.uncross();
        }
        calculateCalendar();
    }

    private void onDayClicked(MouseEvent mouseevent) {
        CrossedLabel jtextfield = (CrossedLabel) mouseevent.getSource();
        if (!jtextfield.getForeground().equals(Color.LIGHT_GRAY)) {
            if (null != selectedDay) {
                selectedDay.setBackground(white);
                selectedDay.uncross();
            }
            jtextfield.setBackground(highlight);
            jtextfield.cross();
            selectedDay = jtextfield;
            selectedDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(jtextfield.getText()));
        }
    }

    private static GregorianCalendar getToday() {
        GregorianCalendar gregoriancalendar = new GregorianCalendar();
        gregoriancalendar.set(GregorianCalendar.HOUR, 0);
        gregoriancalendar.set(GregorianCalendar.MINUTE, 0);
        gregoriancalendar.set(GregorianCalendar.SECOND, 0);
        gregoriancalendar.set(GregorianCalendar.MILLISECOND, 0);
        return gregoriancalendar;
    }

    private void calculateCalendar() {
        if (null != selectedDay) {
            selectedDay.setBackground(white);
            selectedDay = null;
        }

        //get days of this and the last month and current calendar
        GregorianCalendar calLast = new GregorianCalendar(selectedDate.get(1), selectedDate.get(2), 1);
        calLast.add(Calendar.MONTH, -1);
        int daysInLastMonth = calculateDaysInMonth(calLast);
        GregorianCalendar calCurrent = new GregorianCalendar(selectedDate.get(1), selectedDate.get(2), 1);
        int daysInCurrentMonth = calculateDaysInMonth(calCurrent);

        int dayToSelect = Math.min(daysInCurrentMonth, selectedDate.get(GregorianCalendar.DAY_OF_MONTH));

        //reset all boxes
        for (CrossedLabel[] aDaysInMonth : daysInMonth) {
            for (int i2 = 0; i2 < daysInMonth[0].length; i2++) {
                aDaysInMonth[i2].setText("");
                aDaysInMonth[i2].setBackground(Color.WHITE);
            }
        }

        //re-map the calendars day and get leading and trailing village amount
        int startDay = 0;
        int leadingDays = 0;
        int calendarDay = calCurrent.get(GregorianCalendar.DAY_OF_WEEK);
        switch (calendarDay) {
            case Calendar.TUESDAY:
                startDay = 1;
                leadingDays = 1;
                break;
            case Calendar.WEDNESDAY:
                startDay = 2;
                leadingDays = 2;
                break;
            case Calendar.THURSDAY:
                startDay = 3;
                leadingDays = 3;
                break;
            case Calendar.FRIDAY:
                startDay = 4;
                leadingDays = 4;
                break;
            case Calendar.SATURDAY:
                startDay = 5;
                leadingDays = 5;
                break;
            case Calendar.SUNDAY:
                startDay = 6;
                leadingDays = 6;
                break;
            default:
                //monday
                startDay = 0;
                leadingDays = 7;
        }

        for (int i = 1; i <= leadingDays; i++) {
            CrossedLabel leadingField = daysInMonth[0][i - 1];
            leadingField.setText(Integer.toString(daysInLastMonth - leadingDays + i));
            leadingField.setForeground(Color.LIGHT_GRAY);
            leadingField.setHorizontalAlignment(SwingConstants.CENTER);
        }

        int week = 0;
        startDay = leadingDays;
        do {
            //check if we've reached sunday
            if (startDay != 0 && startDay % 7 == 0) {
                //increment week and reset week day
                week++;
                startDay = 0;
            }
            //get current calendar field
            CrossedLabel currentDayField = daysInMonth[week][startDay];
            //increment day of week
            startDay += 1;
            //set value of day from calendar
            currentDayField.setText(Integer.toString(calCurrent.get(GregorianCalendar.DAY_OF_MONTH)));
            currentDayField.setForeground(Color.BLACK);
            currentDayField.setHorizontalAlignment(SwingConstants.CENTER);
            //hightlight currently selected day of month
            if (dayToSelect == calCurrent.get(GregorianCalendar.DAY_OF_MONTH)) {
                //current field is selected, mark it
                currentDayField.setBackground(highlight);
                selectedDay = currentDayField;
                currentDayField.cross();
            } else {
                //draw white background
                currentDayField.setBackground(Color.WHITE);
                currentDayField.uncross();
            }
            if (calCurrent.get(GregorianCalendar.DAY_OF_MONTH) >= daysInCurrentMonth) {
                //break if all days of this month where set
                break;
            }
            //increment to next day
            calCurrent.add(GregorianCalendar.DAY_OF_MONTH, 1);

        } while (startDay <= daysInCurrentMonth);

        int trailingDay = 1;
        //add trailing days beginning with the last day of this month
        for (int i = week * 7 + startDay + 1; i <= 42; i++) {
            if (startDay % 7 == 0) {
                week++;
                startDay = 0;
            }
            //get trailing field
            CrossedLabel trailingField = daysInMonth[week][startDay];
            startDay++;
            //set trailing value and increment trailing day
            trailingField.setForeground(Color.LIGHT_GRAY);
            trailingField.setText(Integer.toString(trailingDay));
            trailingField.setHorizontalAlignment(SwingConstants.CENTER);
            trailingDay++;
        }
        //set day of month eiter to the selected day or to the last day if the selected month has less days
        calCurrent.set(GregorianCalendar.DAY_OF_MONTH, dayToSelect);
        selectedDate = calCurrent;
    }

    /**
     * Calculate the number of days for the current month
     */
    private static int calculateDaysInMonth(Calendar calendar) {
        byte days = 0;
        switch (calendar.get(GregorianCalendar.MONTH)) {
            case GregorianCalendar.JANUARY:
            case GregorianCalendar.MARCH:
            case GregorianCalendar.MAY:
            case GregorianCalendar.JULY:
            case GregorianCalendar.AUGUST:
            case GregorianCalendar.OCTOBER:
            case GregorianCalendar.DECEMBER:
                days = 31;
                break;

            case GregorianCalendar.APRIL:
            case GregorianCalendar.JUNE:
            case GregorianCalendar.SEPTEMBER:
            case GregorianCalendar.NOVEMBER:
                days = 30;
                break;

            case GregorianCalendar.FEBRUARY:
                int i = calendar.get(GregorianCalendar.YEAR);
                days = 0 != i % 1000 ? 0 != i % 100 ? ((byte) (0 != i % 4 ? 28 : 29)) : 28 : 29;
                break;
        }
        return days;
    }

    private static String formatDateText(Date date) {
        DateFormat dateformat = DateFormat.getDateInstance(0, Locale.GERMAN);
        StringBuffer stringbuffer = new StringBuffer();
        StringBuffer stringbuffer1 = new StringBuffer();
        FieldPosition fieldposition = new FieldPosition(2);
        FieldPosition fieldposition1 = new FieldPosition(1);
        dateformat.format(date, stringbuffer, fieldposition);
        dateformat.format(date, stringbuffer1, fieldposition1);
        return stringbuffer.toString().substring(fieldposition.getBeginIndex(), fieldposition.getEndIndex()) + " " + stringbuffer1.toString().substring(fieldposition1.getBeginIndex(), fieldposition1.getEndIndex());
    }

    public static void main(String[] args) {
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        JFrame f = new JFrame();
        f.add(new DatePicker());
        f.setVisible(true);
    }
}
