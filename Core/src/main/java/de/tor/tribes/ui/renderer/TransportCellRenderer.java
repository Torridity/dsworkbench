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

import de.tor.tribes.types.Resource;
import de.tor.tribes.types.Transport;
import java.awt.Component;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class TransportCellRenderer extends DefaultTableRenderer {

    private NumberFormat nf;
    private List<String> iconsUrls = null;

    public TransportCellRenderer() {
        super();
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        try {
            iconsUrls = new LinkedList<String>();
            iconsUrls.add(this.getClass().getResource("/res/ui/holz.png").toString());
            iconsUrls.add(this.getClass().getResource("/res/ui/lehm.png").toString());
            iconsUrls.add(this.getClass().getResource("/res/ui/eisen.png").toString());
        } catch (Exception e) {
            iconsUrls = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;

        Transport t = (Transport) value;

        Resource woodTransport = t.getSingleTransports().get(0);
        Resource clayTransport = t.getSingleTransports().get(1);
        Resource ironTransport = t.getSingleTransports().get(2);

        StringBuilder text = new StringBuilder();
        text.append("<html>");
        text.append(nf.format(woodTransport.getAmount()));
        text.append(" ");
        text.append("<img src='").append(iconsUrls.get(0)).append("'/>");
        text.append(nf.format(clayTransport.getAmount()));
        text.append(" ");
        text.append("<img src='").append(iconsUrls.get(1)).append("'/>");
        text.append(nf.format(ironTransport.getAmount()));
        text.append(" ");
        text.append("<img src='").append(iconsUrls.get(2)).append("'/>");
        text.append("</html>");
        label.setText(text.toString());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
