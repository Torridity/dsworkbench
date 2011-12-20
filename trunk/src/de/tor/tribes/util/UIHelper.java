/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import com.jidesoft.swing.LabeledTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

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
}
