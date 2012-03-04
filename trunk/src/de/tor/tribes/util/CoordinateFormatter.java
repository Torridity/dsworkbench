/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import java.awt.Point;
import java.text.ParseException;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatter;

/**
 *
 * @author Torridity
 */
public class CoordinateFormatter extends DefaultFormatter {

    private static JFormattedTextField.AbstractFormatter formatter;

    public synchronized static JFormattedTextField.AbstractFormatter getInstance() {
        if (formatter == null) {
            formatter = new CoordinateFormatter();
        }
        return formatter;
    }

    private CoordinateFormatter() {
        super();
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        //text = text.trim();
        List<Village> villages = PluginManager.getSingleton().executeVillageParser(text);
        if (villages.size() > 0) {
            return (Point) villages.get(0).getPosition().clone();
        } else {
            try {
                text = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
                String[] splition = text.split("\\|");
                return new Point(Integer.parseInt(splition[0].trim()), Integer.parseInt(splition[1].trim()));
            } catch (Exception e) {
                throw new ParseException(text, 0);
            }
        }
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value instanceof Point) {
            Point point = (Point) value;
            Village v = null;
            if (point.x > 0 && point.y > 0) {
                v = DataHolder.getSingleton().getVillages()[point.x][point.y];
            }

            if (v == null) {
                return "Kein Dorf (" + point.x + "|" + point.y + ")";
            } else {
                return v.getFullName();
            }
        } else {
            return super.valueToString(value);
        }
    }

    public static void main(String[] args) {
        Point point = new Point(5, -5);
        JFormattedTextField.AbstractFormatter formatter = CoordinateFormatter.getInstance();
        String value;
        try {
            value = formatter.valueToString(point);
        } catch (ParseException e) {
            value = null;
        }
        System.out.println(value);
        value = "(3|-3)";
        try {
            point = (Point) formatter.stringToValue(value);
        } catch (ParseException e) {
            point = null;
        }
        System.out.println(point);
    }
}
