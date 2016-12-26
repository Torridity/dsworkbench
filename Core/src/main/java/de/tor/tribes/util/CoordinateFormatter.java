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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import java.awt.Point;
import java.text.ParseException;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author Torridity
 */
public class CoordinateFormatter extends DefaultFormatter {

    private static JFormattedTextField.AbstractFormatter formatter;
    private static DoNothingFilter d = null;

    public synchronized static JFormattedTextField.AbstractFormatter getInstance() {
        if (formatter == null) {
            formatter = new CoordinateFormatter();
            d = new DoNothingFilter();
        }
        return formatter;
    }

    private CoordinateFormatter() {
        super();
    }

    @Override
    protected DocumentFilter getDocumentFilter() {
        return d;
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        List<Village> villages = PluginManager.getSingleton().executeVillageParser(text);
        if (villages.size() > 0) {
            return villages.get(0).getPosition().clone();
        } else {
            try {
                text = text.substring(text.lastIndexOf("(") + 1, text.lastIndexOf(")"));
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
            if (point.x > 0 && point.y > 0 && point.x < ServerSettings.getSingleton().getMapDimension().width && point.y < ServerSettings.getSingleton().getMapDimension().height) {
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

//    public static void main(String[] args) {
//        Point point = new Point(5, -5);
//        JFormattedTextField.AbstractFormatter formatter = CoordinateFormatter.getInstance();
//        String value;
//        try {
//            value = formatter.valueToString(point);
//        } catch (ParseException e) {
//            value = null;
//        }
//        System.out.println(value);
//        value = "(3|-3)";
//        try {
//            point = (Point) formatter.stringToValue(value);
//        } catch (ParseException e) {
//            point = null;
//        }
//        System.out.println(point);
//    }
}

class DoNothingFilter extends DocumentFilter {

    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text,
            AttributeSet attr) throws BadLocationException {
        fb.insertString(offset, text, attr);
    }

    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
            AttributeSet attrs) throws BadLocationException {
        fb.replace(offset, length, text, attrs);
    }

    @Override
    public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
    }
}