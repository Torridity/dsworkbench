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

import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.util.Constants;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Torridity
 */
public class TradeDirectionCellRenderer extends DefaultTableCellRenderer {

    private DefaultTableCellRenderer renderer = null;
    private ImageIcon tradeBoth;
    private ImageIcon tradeIn;
    private ImageIcon tradeOut;

    public TradeDirectionCellRenderer() {
        super();
        renderer = new DefaultTableCellRenderer();
        try {
            tradeBoth = new ImageIcon(this.getClass().getResource("/res/ui/trade_both.png"));
            tradeIn = new ImageIcon(this.getClass().getResource("/res/ui/trade_in.png"));
            tradeOut = new ImageIcon(this.getClass().getResource("/res/ui/trade_out.png"));
        } catch (Exception e) {
            e.printStackTrace();
            tradeBoth = null;
            tradeIn = null;
            tradeOut = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);
        label.setText("");

        if (!isSelected) {
            if (row % 2 == 0) {
                label.setBackground(Constants.DS_ROW_B);
            } else {
                label.setBackground(Constants.DS_ROW_A);
            }
        }
        VillageMerchantInfo.Direction dir = (VillageMerchantInfo.Direction) value;

        if (dir == VillageMerchantInfo.Direction.BOTH) {
            label.setIcon(tradeBoth);
        } else if (dir == VillageMerchantInfo.Direction.INCOMING) {
            label.setIcon(tradeIn);
        } else if (dir == VillageMerchantInfo.Direction.OUTGOING) {
            label.setIcon(tradeOut);
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
