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
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.village.KnownVillageManager;
import de.tor.tribes.util.village.KnownVillage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.awt.Point;
import java.awt.Stroke;

/**
 *
 * @author extremeCrazyCoder
 */
public class WatchtowerLayerRenderer extends AbstractDirectLayerRenderer {

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        if (!pSettings.isLayerVisible()) {
            return;
        }
        renderRows(pSettings, pG2d);
    }

    private void renderRows(RenderSettings pSettings, Graphics2D pG2D) {
        //iterate through entire rows
        List<KnownVillage> watchtowerVillages = KnownVillageManager.getSingleton().getWatchtowerVillages();
        
        Color cb = pG2D.getColor();
        Composite com = pG2D.getComposite();
        Stroke st = pG2D.getStroke();

        for (KnownVillage village : watchtowerVillages) {
            Point villAbs = MapPanel.getSingleton().virtualPosToSceenPos(village.getVillage().getX(), village.getVillage().getY());
            int w = (int) (village.getWatchtowerRange() * pSettings.getFieldWidth() * 2);
            int h = (int) (village.getWatchtowerRange() * pSettings.getFieldHeight() * 2);
            int x = (int) (villAbs.x + (pSettings.getFieldWidth() - w) / 2.0);
            int y = (int) (villAbs.y + (pSettings.getFieldHeight()- h) / 2.0);
            pG2D.setColor(village.getRangeColor());
            pG2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            pG2D.setStroke(new BasicStroke(13.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                    0.0f, new float[] {15, 15}, 0.0f));
            pG2D.drawOval(x, y, w, h);
            pG2D.setComposite(com);
            pG2D.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0.0f, new float[] {15, 15}, 0.0f));
            pG2D.drawOval(x, y, w, h);
        }
        pG2D.setComposite(com);
        pG2D.setColor(cb);
        pG2D.setStroke(st);
    }

    void renderTempWatchtower(Graphics2D g2d, KnownVillage tmpVillage, Rectangle r) {
        int w = (int) (tmpVillage.getWatchtowerRange() * r.width * 2);
        int h = (int) (tmpVillage.getWatchtowerRange() * r.height * 2);
        int x = (int) (r.x + (r.width - w) / 2.0);
        int y = (int) (r.y + (r.height - h) / 2.0);
        Composite cb = g2d.getComposite();
        Stroke st = g2d.getStroke();
        Color cob = g2d.getColor();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
        g2d.setColor(Constants.DS_BACK_LIGHT);
        g2d.fillOval(x, y, w, h);
        g2d.setComposite(cb);
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0.0f, new float[] {15, 15}, 0.0f));
        g2d.setColor(Constants.DS_BACK);
        g2d.drawOval(x, y, w, h);
        g2d.setColor(cob);
        g2d.setStroke(st);
    }
}
