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
import java.util.Date;
import javax.swing.*;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;


// Referenced classes of package com.theotherbell.ui:
//            DatePicker
public final class DateField extends JPanel {

    final class Listener extends ComponentAdapter {

        public void componentHidden(ComponentEvent componentevent) {
            Date date = ((DatePicker) componentevent.getSource()).getDate();
            if (null != date) {
                dateText.setText(DateField.dateToString(date));
            }
            dlg.dispose();
        }

        Listener() {
        }
    }

    public DateField() {
        dateText = new JTextField();
        dropdownButton = new JButton();
        init();
    }

    public DateField(Date date) {
        dateText = new JTextField();
        dropdownButton = new JButton();
        init();
        dateText.setText(dateToString(date));
    }

    public Date getDate() {
        return stringToDate(dateText.getText());
    }

    private void init() {
        setLayout(new AbsoluteLayout());
        dateText.setText("");
        dateText.setEditable(false);
        dateText.setBackground(new Color(255, 255, 255));
        add(dateText, new AbsoluteConstraints(10, 10, 141, 20));
        dropdownButton.setText("...");
        dropdownButton.setMargin(new Insets(2, 2, 2, 2));
        dropdownButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                onButtonClick(actionevent);
            }
        });
        add(dropdownButton, new AbsoluteConstraints(151, 10, 20, 21));
    }

    private void onButtonClick(ActionEvent actionevent) {
        if ("".equals(dateText.getText())) {
            dp = new DatePicker();
        } else {
            dp = new DatePicker(stringToDate(dateText.getText()));
        }
        dp.setHideOnSelect(true);
        dp.addComponentListener(new Listener());
        Point point = dateText.getLocationOnScreen();
        point.setLocation(point.getX(), (point.getY() - 1.0D) + dateText.getSize().getHeight());
        dlg = new JDialog(new JFrame(), true);
        dlg.setLocation(point);
        dlg.setResizable(false);
        dlg.setUndecorated(true);
        dlg.getContentPane().add(dp);
        dlg.pack();
        dlg.setVisible(true);
    }

    private static String dateToString(Date date) {
        if (null != date) {
            return DateFormat.getDateInstance(1).format(date);
        } else {
            return null;
        }
    }

    private static Date stringToDate(String s) {
        try {
            return DateFormat.getDateInstance(1).parse(s);
        } catch (ParseException e) {
            return null;
        }
    }
    private final JTextField dateText;
    private final JButton dropdownButton;
    private DatePicker dp;
    private JDialog dlg;
}

