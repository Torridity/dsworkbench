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
package de.tor.tribes.util;

import com.jidesoft.swing.LabeledTextField;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Torridity
 */
public class UIHelper {

    public static int parseIntFromField(LabeledTextField jField) {
        return parseIntFromField(jField, 0);
    }

    public static int parseIntFromField(LabeledTextField jField, int pDefault) {
        int result = pDefault;
        try {
            result = Integer.parseInt(jField.getText());
        } catch (NumberFormatException nfe) {
            jField.setText(Integer.toString(pDefault));
            result = pDefault;
        }
        return result;
    }

    public static int parseIntFromLabel(JLabel jLabel) {
        return parseIntFromLabel(jLabel, 0);
    }

    public static int parseIntFromLabel(JLabel jLabel, int pDefault) {
        int result = pDefault;
        try {
            result = Integer.parseInt(jLabel.getText());
        } catch (NumberFormatException nfe) {
            jLabel.setText(Integer.toString(pDefault));
            result = pDefault;
        }
        return result;
    }

    public static int parseIntFromField(JTextField jField) {
        return parseIntFromField(jField, 0);
    }

    public static int parseIntFromField(JTextField jField, int pDefault) {
        int result = pDefault;
        try {
            result = Integer.parseInt(jField.getText());
        } catch (NumberFormatException nfe) {
            jField.setText(Integer.toString(pDefault));
            result = pDefault;
        }
        return result;
    }

    public static void setText(JTextField jField, Object pObject, Object pDefault) {
        if (jField == null) {
            return;
        }
        if (pObject != null) {
            jField.setText(pObject.toString());
        } else {
            if (pDefault != null) {
                jField.setText(pDefault.toString());
            } else {
                jField.setText("");
            }
        }
    }

    public static void applyCorrectViewPosition(JComponent targetComponent, JScrollPane scrollPane) {
        try {
            Point point = new Point(0, (int) (targetComponent.getSize().getHeight()));
            JViewport vp = scrollPane.getViewport();
            if (vp == null) {
                return;
            }
            vp.setViewPosition(point);
        } catch (Throwable ignored) {
        }
    }
}
