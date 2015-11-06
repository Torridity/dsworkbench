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
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.ServerSettings;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class DateCellRenderer extends DefaultTableRenderer {

    private SimpleDateFormat specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");

    public DateCellRenderer() {
        super();
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        } else {
            specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
        }
    }

    public DateCellRenderer(String pPattern) {
        this();
        specialFormat = new SimpleDateFormat(pPattern);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        try {
            long val = ((Date) value).getTime();
            if (val > System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY * 365 * 10) {//more than ten year ago...invalid!
                label.setText((value == null) ? "" : specialFormat.format(value));
            } else {
                label.setText("-");
            }
        } catch (Exception e) {
            label.setText("-");
        }

        return label;
    }
}
