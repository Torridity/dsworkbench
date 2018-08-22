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
package de.tor.tribes.ui.editors;

import de.tor.tribes.ui.components.DateTimeField;
import java.awt.Component;
import java.util.Date;
import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author Torridity
 */
public class DateSpinEditor extends DefaultCellEditor {

    private final JSpinner spinnerComponent = new javax.swing.JSpinner();
    private DateTimeField dtf = new DateTimeField();
    
    public DateSpinEditor() {
        super(new JTextField(""));
        setClickCountToStart(2);
    }

    @Override
    public Object getCellEditorValue() {
        return dtf.getSelectedDate();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        dtf.setDate((Date)value);
        return dtf;
    }
}
