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

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class CustomBooleanRenderer extends DefaultTableRenderer {
    public enum LayoutStyle {
        SENT_NOTSENT ("/res/ui/unsent_small.gif", "/res/ui/sent_small.gif"),
        DRAW_NOTDRAW ("/res/ui/not_draw_small.gif", "/res/ui/draw_small.gif"),
        VISIBLE_INVISIBLE ("/res/ui/eye_forbidden.png", "/res/ui/eye.png"),
        FAKE_NOFAKE ("/res/ui/no_fake.png", "/res/ui/fake.png"),
        RES_IN_STORAGE ("/res/nores.png", "/res/res.png");
        
        private final String trueImg;
        private final String falseImg;
        LayoutStyle(String falseImg, String trueImg) {
            this.falseImg = falseImg;
            this.trueImg = trueImg;
        }
        public String trueImg() { return trueImg; }
        public String falseImg() { return falseImg; }
    }

    private ImageIcon falseImg = null;
    private ImageIcon trueImg = null;

    public CustomBooleanRenderer(LayoutStyle style) {
        this(style.falseImg(), style.trueImg());
    }
    
    public CustomBooleanRenderer(String falseImgPath, String trueImgPath) {
        super();
        try {
            falseImg = new ImageIcon(CustomBooleanRenderer.class.getResource(falseImgPath));
            trueImg = new ImageIcon(CustomBooleanRenderer.class.getResource(trueImgPath));
        } catch (Exception ignored) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        label.setText(null);
        if (value != null && (Boolean) value) {
            label.setIcon(trueImg);
        } else {
            label.setIcon(falseImg);
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
