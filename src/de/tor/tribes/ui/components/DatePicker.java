package de.tor.tribes.ui.components;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public final class DatePicker extends JPanel {

    private static final Font smallFont = new Font("Dialog", 0, 10);
    private static final Font largeFont = new Font("Dialog", 0, 12);
    private static final Insets insets = new Insets(2, 2, 2, 2);
    private static final Color highlight = new Color(255, 255, 204);
    private static final Color white = new Color(255, 255, 255);
    private static final Color gray = new Color(204, 204, 204);
    private Component selectedDay;
    private GregorianCalendar selectedDate;
    private GregorianCalendar originalDate;
    private boolean hideOnSelect;
    private final JButton backButton;
    private final JLabel monthAndYear;
    private final JButton forwardButton;
    private final JTextField dayHeadings[] = {
        new JTextField("Mo"), new JTextField("Di"), new JTextField("Mi"), new JTextField("Do"), new JTextField("Fr"), new JTextField("Sa"), new JTextField("So")
    };
    private final JTextField daysInMonth[][] = {
        {
            new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField()
        }, {
            new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField()
        }, {
            new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField()
        }, {
            new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField()
        }, {
            new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField()
        }, {
            new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField()
        }
    };
    private final JButton todayButton;
    private final JButton cancelButton;

    public DatePicker() {
        selectedDay = null;
        selectedDate = null;
        originalDate = null;
        hideOnSelect = true;
        backButton = new JButton();
        monthAndYear = new JLabel();
        forwardButton = new JButton();
        todayButton = new JButton();
        cancelButton = new JButton();
        selectedDate = getToday();
        init();
    }

    public DatePicker(Date date) {
        selectedDay = null;
        selectedDate = null;
        originalDate = null;
        hideOnSelect = true;
        backButton = new JButton();
        monthAndYear = new JLabel();
        forwardButton = new JButton();
        todayButton = new JButton();
        cancelButton = new JButton();
        if (null == date) {
            selectedDate = getToday();
        } else {
            (selectedDate = new GregorianCalendar()).setTime(date);
        }
        originalDate = new GregorianCalendar(selectedDate.get(1), selectedDate.get(2), selectedDate.get(5));
        init();
    }

    public boolean isHideOnSelect() {
        return hideOnSelect;
    }

    public void setHideOnSelect(boolean flag) {
        if (hideOnSelect != flag) {
            hideOnSelect = flag;
            initButtons(false);
        }
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
        backButton.setText("<");
        backButton.setMargin(insets);
        backButton.setDefaultCapable(false);
        backButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                onBackClicked(actionevent);
            }
        });
        add(backButton, new AbsoluteConstraints(10, 10, 20, 20));
        monthAndYear.setFont(largeFont);
        monthAndYear.setHorizontalAlignment(0);
        monthAndYear.setText(formatDateText(selectedDate.getTime()));
        add(monthAndYear, new AbsoluteConstraints(30, 10, 100, 20));
        forwardButton.setFont(smallFont);
        forwardButton.setText(">");
        forwardButton.setMargin(insets);
        forwardButton.setDefaultCapable(false);
        forwardButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                onForwardClicked(actionevent);
            }
        });
        add(forwardButton, new AbsoluteConstraints(130, 10, 20, 20));
        int i = 10;
        for (int j = 0; j < dayHeadings.length; j++) {
            dayHeadings[j].setBackground(gray);
            dayHeadings[j].setEditable(false);
            dayHeadings[j].setFont(smallFont);
            dayHeadings[j].setHorizontalAlignment(0);
            dayHeadings[j].setFocusable(false);
            add(dayHeadings[j], new AbsoluteConstraints(i, 40, 21, 21));
            i += 20;
        }

        i = 10;
        int k = 60;
        for (int l = 0; l < daysInMonth.length; l++) {
            for (int i1 = 0; i1 < daysInMonth[l].length; i1++) {
                daysInMonth[l][i1].setBackground(gray);
                daysInMonth[l][i1].setEditable(false);
                daysInMonth[l][i1].setFont(smallFont);
                daysInMonth[l][i1].setHorizontalAlignment(4);
                daysInMonth[l][i1].setText("");
                daysInMonth[l][i1].setFocusable(false);
                daysInMonth[l][i1].addMouseListener(new MouseAdapter() {

                    public void mouseClicked(MouseEvent mouseevent) {
                        onDayClicked(mouseevent);
                    }
                });
                add(daysInMonth[l][i1], new AbsoluteConstraints(i, k, 21, 21));
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
            todayButton.setFont(largeFont);
            todayButton.setText("Today");
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
            cancelButton.setFont(largeFont);
            cancelButton.setText("Cancel");
            cancelButton.setMargin(insets);
            cancelButton.setMaximumSize(dimension);
            cancelButton.setMinimumSize(dimension);
            cancelButton.setPreferredSize(dimension);
            cancelButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent) {
                    onCancel(actionevent);
                }
            });
        } else {
            remove(todayButton);
            remove(cancelButton);
        }
        if (hideOnSelect) {
            add(todayButton, new AbsoluteConstraints(25, 190, 52, -1));
            add(cancelButton, new AbsoluteConstraints(87, 190, 52, -1));
        } else {
            add(todayButton, new AbsoluteConstraints(55, 190, 52, -1));
        }
    }

    private void onToday(ActionEvent actionevent) {
        selectedDate = getToday();
        setVisible(!hideOnSelect);
        if (isVisible()) {
            monthAndYear.setText(formatDateText(selectedDate.getTime()));
            calculateCalendar();
        }
    }

    private void onCancel(ActionEvent actionevent) {
        selectedDate = originalDate;
        setVisible(!hideOnSelect);
    }

    private void onForwardClicked(ActionEvent actionevent) {
        int i = selectedDate.get(5);
        selectedDate.set(5, 1);
        selectedDate.add(2, 1);
        selectedDate.set(5, Math.min(i, calculateDaysInMonth(selectedDate)));
        monthAndYear.setText(formatDateText(selectedDate.getTime()));
        calculateCalendar();
    }

    private void onBackClicked(ActionEvent actionevent) {
        int i = selectedDate.get(5);
        selectedDate.set(5, 1);
        selectedDate.add(2, -1);
        selectedDate.set(5, Math.min(i, calculateDaysInMonth(selectedDate)));
        monthAndYear.setText(formatDateText(selectedDate.getTime()));
        calculateCalendar();
    }

    private void onDayClicked(MouseEvent mouseevent) {
        JTextField jtextfield = (JTextField) mouseevent.getSource();
        if (!"".equals(jtextfield.getText())) {
            if (null != selectedDay) {
                selectedDay.setBackground(white);
            }
            jtextfield.setBackground(highlight);
            selectedDay = jtextfield;
            selectedDate.set(5, Integer.parseInt(jtextfield.getText()));
            setVisible(!hideOnSelect);
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
        //get calendar
        GregorianCalendar gregoriancalendar = new GregorianCalendar(selectedDate.get(1), selectedDate.get(2), 1);
        //get days in this month
        int daysInCurrentMonth = calculateDaysInMonth(gregoriancalendar);
        int j = Math.min(daysInCurrentMonth, selectedDate.get(GregorianCalendar.DAY_OF_MONTH));
        int k = gregoriancalendar.get(GregorianCalendar.DAY_OF_WEEK);
        //draw grey boxes for days which are not in this months beginning
        for (int i1 = 0; i1 < daysInMonth.length; i1++) {
            for (int i2 = 0; i2 < daysInMonth[0].length; i2++) {
                daysInMonth[i1][i2].setText("");
                daysInMonth[i1][i2].setBackground(gray);
            }
        }
        int i1;

        int dec = 1;
        do {
            i1 = gregoriancalendar.get(GregorianCalendar.WEEK_OF_MONTH);
            k = gregoriancalendar.get(GregorianCalendar.DAY_OF_WEEK);

            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy");
            //  System.out.println(f.format(gregoriancalendar.getTime()));
            if (k == 1) {
                //set sunday to be the last day, somehow the "setFirstDayOfWeek" does not work
                k = 8;
                i1 -= 1;
            }
            if (i1 == 0) {
                //current month starts with 0-week, skip decrementing week
                dec = 0;
            }

            // System.out.println("Set day " + k + " in week " + i1);
            JTextField jtextfield = daysInMonth[i1 - dec][k - 2];
            //set value of day
            jtextfield.setText(Integer.toString(gregoriancalendar.get(GregorianCalendar.DAY_OF_MONTH)));
            //hightlight currently selected day of month
            if (j == gregoriancalendar.get(GregorianCalendar.DAY_OF_MONTH)) {
                jtextfield.setBackground(highlight);
                selectedDay = jtextfield;
            } else {
                if (k == 8) {
                    //draw sundays different
                    jtextfield.setBackground(new Color(240, 240, 240));
                } else {
                    //draw white background
                    jtextfield.setBackground(white);
                }
            }
            if (gregoriancalendar.get(GregorianCalendar.DAY_OF_MONTH) >= daysInCurrentMonth) {
                //break if all days of this month where set
                break;
            }
            //increment to next day
            gregoriancalendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
        } while (gregoriancalendar.get(GregorianCalendar.DAY_OF_MONTH) <= daysInCurrentMonth);
        //set day of month eiter to the selected day or to the last day if the selected month has less days
        gregoriancalendar.set(GregorianCalendar.DAY_OF_MONTH, j);
        selectedDate = gregoriancalendar;
    }

    /**Calculate the number of days for the current month*/
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
        DateFormat dateformat = DateFormat.getDateInstance(1);
        StringBuffer stringbuffer = new StringBuffer();
        StringBuffer stringbuffer1 = new StringBuffer();
        FieldPosition fieldposition = new FieldPosition(2);
        FieldPosition fieldposition1 = new FieldPosition(1);
        dateformat.format(date, stringbuffer, fieldposition);
        dateformat.format(date, stringbuffer1, fieldposition1);
        return stringbuffer.toString().substring(fieldposition.getBeginIndex(), fieldposition.getEndIndex()) + " " + stringbuffer1.toString().substring(fieldposition1.getBeginIndex(), fieldposition1.getEndIndex());
    }
}

