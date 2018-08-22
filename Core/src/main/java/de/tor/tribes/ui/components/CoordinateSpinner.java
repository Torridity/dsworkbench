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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.models.CoordinateSpinnerModel;
import de.tor.tribes.util.CoordinateFormatter;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author Torridity
 */
public class CoordinateSpinner extends JSpinner {

    /**
     * Constructs a complete spinner with pair of next/previous buttons and an editor for the
     * <code>SpinnerModel</code>.
     *
     * @param model
     */
    public CoordinateSpinner(CoordinateSpinnerModel model) {
        super(model);
    }

    /**
     * Constructs a spinner with an
     * <code>Integer SpinnerNumberModel</code> with initial value 0 and no minimum or maximum limits.
     */
    public CoordinateSpinner() {
        this(new CoordinateSpinnerModel());
    }

    @Override
    protected JComponent createEditor(SpinnerModel model) {
        return new CoordinateSpinner.CoordinateEditor(this);
    }

    /**
     *
     */
    public static class CoordinateEditor extends JSpinner.DefaultEditor {

        /**
         * @param spinner
         */
        public CoordinateEditor(JSpinner spinner) {
            super(spinner);
            if (!(spinner.getModel() instanceof CoordinateSpinnerModel)) {
                throw new IllegalArgumentException(
                        "model not a SpinnerCoordinateModel");
            }
            final CoordinateSpinnerModel model = (CoordinateSpinnerModel) spinner.getModel();

            JFormattedTextField.AbstractFormatter formatter = CoordinateFormatter.getInstance();
            DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
            final JFormattedTextField ftf = getTextField();
            ftf.setEditable(true);
            ftf.setFormatterFactory(factory);
            ftf.setHorizontalAlignment(JTextField.TRAILING);

            /*
             * TBD - initializing the column width of the text field is imprecise and doing it here is tricky because the developer may
             * configure the formatter later.
             */
            String min = Integer.toString(Integer.MIN_VALUE);
            ftf.setColumns(4 + 2 * min.length());
            ftf.addPropertyChangeListener("value", new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    try {
                        String text = ftf.getText();
                        int comma = text.indexOf('|');
                        int open = text.indexOf('(');
                        int close = text.indexOf(')');
                        if (comma < 0 || open < 0 || close < 0) {
                            ftf.select(0, text.length());
                        } else {
                            String digit;
                            int number;
                            if (model.getField() == CoordinateSpinnerModel.FIELD_X) {
                                digit = text.substring(open + 1, comma).trim();
                                number = text.indexOf(digit);
                            } else {
                                digit = text.substring(comma + 1, close).trim();
                                number = text.lastIndexOf(digit);
                            }
                            ftf.select(number, number + digit.length());
                        }
                    } catch (StringIndexOutOfBoundsException ignored) {
                    }
                }
            });

        }
    }

    private void updateField() {
        JComponent editor = getEditor();
        if (editor instanceof CoordinateEditor && getModel() instanceof CoordinateSpinnerModel) {
            JFormattedTextField ftf = ((CoordinateEditor) editor).getTextField();
            CoordinateSpinnerModel model = (CoordinateSpinnerModel) getModel();
            int comma = ftf.getText().indexOf('|');
            int caret = ftf.getCaretPosition();
            model.setField(caret <= comma ? CoordinateSpinnerModel.FIELD_X : CoordinateSpinnerModel.FIELD_Y);
        }
    }

    @Override
    public Point getNextValue() {
        updateField();
        return (Point) super.getNextValue();
    }

    @Override
    public Point getPreviousValue() {
        updateField();
        return (Point) super.getPreviousValue();
    }

    @Override
    public Point getValue() {
        return (Point) super.getValue();
    }

    public Village getVillage() {
        try {
            return DataHolder.getSingleton().getVillages()[getValue().x][getValue().y];
        } catch (Exception e) {
            return null;
        }
    }
}