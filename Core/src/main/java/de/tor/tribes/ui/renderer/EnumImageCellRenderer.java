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

import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.VillageMerchantInfo;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class EnumImageCellRenderer extends DefaultTableRenderer {
    private final static Logger logger = LogManager.getLogger("FarmResultRenderer");
    
    public enum LayoutStyle {
        FarmResult(new HashMap<Integer, String>() {{
            put(FarmInformation.FARM_RESULT.OK.ordinal(), "/res/ui/bullet_ball_green.png");
            put(FarmInformation.FARM_RESULT.FAILED.ordinal(), "/res/ui/bullet_ball_yellow.png");
            put(FarmInformation.FARM_RESULT.IMPOSSIBLE.ordinal(), "/res/ui/bullet_ball_red.png");
            put(FarmInformation.FARM_RESULT.FARM_INACTIVE.ordinal(), "/res/ui/bullet_ball_grey.png");
            put(FarmInformation.FARM_RESULT.UNKNOWN.ordinal(), "/res/ui/bullet_ball_empty.png");
        };}),
        FarmStatus(new HashMap<Integer, String>() {{
            put(FarmInformation.FARM_STATUS.CONQUERED.ordinal(), "/res/ui/snob_lock.png");
            put(FarmInformation.FARM_STATUS.FARMING.ordinal(), "/res/ui/trade_in.png");
            put(FarmInformation.FARM_STATUS.LOCKED.ordinal(), "/res/ui/lock.png");
            put(FarmInformation.FARM_STATUS.NOT_INITIATED.ordinal(), "/res/ui/spy_needed.png");
            put(FarmInformation.FARM_STATUS.NOT_SPYED.ordinal(), "/res/checkbox_disabled.png");
            put(FarmInformation.FARM_STATUS.READY.ordinal(), "/res/checkbox.png");
            put(FarmInformation.FARM_STATUS.REPORT_EXPECTED.ordinal(), "/res/ui/report.png");
            put(FarmInformation.FARM_STATUS.TROOPS_FOUND.ordinal(), "/res/ui/red_report_lock.png");
        };}),
        SiegeStatus(new HashMap<Integer, String>() {{
            put(FarmInformation.SIEGE_STATUS.BOTH_ON_WAY.ordinal(), "/res/ui/both_siege.png");
            put(FarmInformation.SIEGE_STATUS.CATA_ON_WAY.ordinal(), "/res/ui/unit_catapult.png");
            put(FarmInformation.SIEGE_STATUS.RAM_ON_WAY.ordinal(), "/res/ui/rams.png");
            put(FarmInformation.SIEGE_STATUS.AT_HOME.ordinal(), "/res/ui/checkbox.png");
            put(FarmInformation.SIEGE_STATUS.NOT_INITIATED.ordinal(), "/res/ui/spy_needed.png");
            put(FarmInformation.SIEGE_STATUS.FINAL_FARM.ordinal(), "/res/ui/final_farm.png");
        };}),
        FightReportStatus(new HashMap<Integer, String>() {{
            put(FightReport.status.LOST_NOTHING.ordinal(), "/res/ui/bullet_ball_green.png");
            put(FightReport.status.WON_WITH_LOSSES.ordinal(), "/res/ui/bullet_ball_yellow.png");
            put(FightReport.status.LOST_EVERYTHING.ordinal(), "/res/ui/bullet_ball_red.png");
            put(FightReport.status.SPY.ordinal(), "/res/ui/bullet_ball_blue.png");
            put(FightReport.status.HIDDEN.ordinal(), "/res/ui/bullet_ball_grey.png");
        };}),
        TradeDirection(new HashMap<Integer, String>() {{
            put(VillageMerchantInfo.Direction.BOTH.ordinal(), "/res/ui/trade_both.png");
            put(VillageMerchantInfo.Direction.INCOMING.ordinal(), "/res/ui/trade_in.png");
            put(VillageMerchantInfo.Direction.OUTGOING.ordinal(), "/res/ui/trade_out.png");
        };});
        
        private Map<Integer, ImageIcon> images;
        LayoutStyle(Map<Integer, String> pImages) {
            images = new HashMap<>();
            
            for(Entry<Integer, String> entry: pImages.entrySet()) {
                try {
                    images.put(entry.getKey(), new ImageIcon(EnumImageCellRenderer.class.getResource(entry.getValue())));
                } catch (Exception e) {
                    logger.warn("Failed to load image " + entry.getValue(), e);
                }
            }
        }
        
        public ImageIcon getIcon(int key) {
            return images.get(key);
        }
    }
    
    private final LayoutStyle style;

    public EnumImageCellRenderer(LayoutStyle pStyle) {
        super();
        this.style = pStyle;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);
        try {
            label.setText("");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setIcon(style.getIcon(((Enum) value).ordinal()));
        } catch (Exception e) {
            label.setText("?");
            label.setIcon(null);
        }
        return label;
    }
}
