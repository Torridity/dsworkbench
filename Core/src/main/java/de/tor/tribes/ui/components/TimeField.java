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
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

/**
 *
 * @author Torridity
 */
public class TimeField extends JPanel {

    private final JTextField timeText;
    private final JButton timeDropdownButton;
    private TimePicker dp;
    private JDialog dlg;
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm 'Uhr'");

    final class Listener extends ComponentAdapter {

        public void componentHidden(ComponentEvent componentevent) {
            Date date = ((TimePicker) componentevent.getSource()).getTime();
            if (null != date) {
                timeText.setText(TimeField.dateToTimeString(date));
            }
            dlg.dispose();
        }

        Listener() {
        }
    }

    public TimeField() {
        timeText = new JTextField();
        timeDropdownButton = new BasicArrowButton(BasicArrowButton.SOUTH);
        init();
    }

    public TimeField(Date date) {
        timeText = new JTextField();
        timeDropdownButton = new BasicArrowButton(BasicArrowButton.SOUTH);
        init();
        timeText.setText(dateToTimeString(date));
    }

    public Date getDate() {
        return stringToTime(timeText.getText());
    }

    private void init() {
        setLayout(new AbsoluteLayout());
        timeText.setText("");
        timeText.setEditable(false);
        timeText.setBackground(new Color(255, 255, 255));
        add(timeText, new AbsoluteConstraints(0, 0, 120, 20));
        timeDropdownButton.setText("...");
        timeDropdownButton.setMargin(new Insets(2, 2, 2, 2));
        timeDropdownButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                onButtonClick(actionevent);
            }
        });
        add(timeDropdownButton, new AbsoluteConstraints(125, 0, 20, 21));

        timeText.setText("");
        timeText.setEditable(false);
        timeText.setBackground(new Color(255, 255, 255));
    }

    private void onButtonClick(ActionEvent actionevent) {
        if (actionevent.getSource() == timeDropdownButton) {
            if (timeText.getText() != null && timeText.getText().isEmpty()) {
                dp = new TimePicker(Calendar.getInstance().getTime());
            } else {
                dp = new TimePicker(stringToTime(timeText.getText()));
            }
            dp.addComponentListener(new Listener());
            Point point = timeText.getLocationOnScreen();
            point.setLocation(point.getX(), (point.getY() - 1.0D) + timeText.getSize().getHeight());
            dlg = new JDialog(new JFrame(), true);
            dlg.setLocation(point);
            dp.setParent(dlg);
            dlg.setResizable(false);
            dlg.getContentPane().add(dp);
            dlg.pack();
            dlg.setVisible(true);
        }
    }

    /*  private void fitDialogPositionOnScreen(JDialog pDialog, Point pDialogLocation){
    
    dlg.setLocationRelativeTo(point);
    
    }*/
    private static String dateToTimeString(Date date) {
        if (null != date) {
            return format.format(date);
        } else {
            return null;
        }
    }

    private static Date stringToTime(String s) {
        try {
            return format.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        JFrame f = new JFrame();
        f.add(new TimeField(Calendar.getInstance().getTime()));
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}
