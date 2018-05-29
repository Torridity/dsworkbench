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

import de.tor.tribes.types.Marker;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Charon
 */
public class MarkerCellRenderer extends DefaultTableRenderer {
    private static ImageIcon PLAYER_ICON = null;
    private static ImageIcon ALLY_ICON = null;

    static {
        try {
            PLAYER_ICON = new javax.swing.ImageIcon(MarkerCellRenderer.class.getResource("/res/face.png"));
            ALLY_ICON = new javax.swing.ImageIcon(MarkerCellRenderer.class.getResource("/res/ally.png"));
        } catch (Exception ignored) {
        }
    }
    
    public MarkerCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel l = (JLabel) c;
        Marker mark = (Marker) value;
        if(mark == null) {
            l.setText("null Marker");
            return c;
        }
        
        if (mark.getMarkerType() == Marker.MarkerType.TRIBE) {
            Tribe mTribe = mark.getTribe();
            if(mTribe == null) {
                l.setText("Null Tribe");
                return c;
            }
            
            l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            l.setIcon(PLAYER_ICON);
            l.setText(mTribe.getName());
            l.setToolTipText(mTribe.getToolTipText());
        } else if (mark.getMarkerType() == Marker.MarkerType.ALLY) {
            Ally mAlly = mark.getAlly();
            if(mAlly == null)  {
                l.setText("Null Ally");
                return c;
            }
            
            l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            l.setIcon(ALLY_ICON);
            l.setText(mAlly.getName());
            l.setToolTipText(mAlly.getToolTipText());
        }
        return l;
    }
}
