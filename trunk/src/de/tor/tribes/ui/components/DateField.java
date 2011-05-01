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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

// Referenced classes of package com.theotherbell.ui:
//            DatePicker
public final class DateField extends JPanel {

    private final JTextField dateText;
    private final JButton dateDropdownButton;
    private DatePicker dp;
    private JDialog dlg;

    final class Listener extends ComponentAdapter {

        public void componentHidden(ComponentEvent componentevent) {
            Date date = ((DatePicker) componentevent.getSource()).getDate();
            if (null != date) {
                dateText.setText(DateField.dateToDateString(date));
            }
            dlg.dispose();
        }

        Listener() {
        }
    }

    public DateField() {
        dateText = new JTextField();
        dateDropdownButton = new BasicArrowButton(BasicArrowButton.SOUTH);
        init();
    }

    public DateField(Date date) {
        dateText = new JTextField();
        dateDropdownButton = new BasicArrowButton(BasicArrowButton.SOUTH);
        init();
        dateText.setText(dateToDateString(date));
    }

    public Date getDate() {
        return stringToDate(dateText.getText());
    }

    private void init() {
        setLayout(new AbsoluteLayout());
        dateText.setText("");
        dateText.setEditable(false);
        dateText.setBackground(new Color(255, 255, 255));
        add(dateText, new AbsoluteConstraints(0, 0, 120, 20));
        dateDropdownButton.setText("...");
        dateDropdownButton.setMargin(new Insets(2, 2, 2, 2));
        dateDropdownButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                onButtonClick(actionevent);
            }
        });
        add(dateDropdownButton, new AbsoluteConstraints(125, 0, 20, 21));

        dateText.setText("");
        dateText.setEditable(false);
        dateText.setBackground(new Color(255, 255, 255));
    }

    private void onButtonClick(ActionEvent actionevent) {
        if (actionevent.getSource() == dateDropdownButton) {
            if ("".equals(dateText.getText())) {
                dp = new DatePicker();
            } else {
                dp = new DatePicker(stringToDate(dateText.getText()));
            }
            dp.addComponentListener(new Listener());
            Point point = dateText.getLocationOnScreen();
            point.setLocation(point.getX(), (point.getY() - 1.0D) + dateText.getSize().getHeight());
            dlg = new JDialog(new JFrame(), true);
            dlg.setLocation(point);
            dlg.setResizable(false);
            dlg.setUndecorated(true);
            JPanel p = new JPanel();
            p.add(dp);
            dlg.getContentPane().add(p);
            dlg.pack();
            dlg.setVisible(true);
        }
    }

    private static String dateToDateString(Date date) {
        if (null != date) {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            return df.format(date);
        } else {
            return null;
        }
    }

    private static Date stringToDate(String s) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            return df.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        JFrame f = new JFrame();
        f.add(new DateField(Calendar.getInstance().getTime()));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}
