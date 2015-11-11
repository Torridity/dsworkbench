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

import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.farm.FarmManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Torridity
 */
public class FarmLayerRenderer extends AbstractDirectLayerRenderer {

    private HashMap<Village, Rectangle> positions = null;

    public FarmLayerRenderer(HashMap<Village, Rectangle> pPositions) {
        positions = pPositions;
    }

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        if (!pSettings.isLayerVisible()) {
            return;
        }
        renderRows(pG2d);
    }

    private void renderRows(Graphics2D pG2D) {
        Set<Entry<Village, Rectangle>> entries = positions.entrySet();

        for (Entry<Village, Rectangle> entry : entries) {
            FarmInformation info = FarmManager.getSingleton().getFarmInformation(entry.getKey());
            if (info != null) {
                // g2d.drawImage(mConquerWarning, col * pFieldWidth + pFieldWidth - conquerSize, row * pFieldHeight + pFieldHeight - conquerSize, conquerSize, conquerSize, null);
                int h = 2;
                Color[] colors = new Color[]{new Color(187, 148, 70), new Color(242, 131, 30), new Color(224, 211, 209)};
                for (int i = 0; i < 3; i++) {
                    pG2D.setColor(colors[i]);
                    int x = entry.getValue().x + 1;
                    int y = entry.getValue().y - 1;
                    y += entry.getValue().height;
                    y -= (3 - i) * h;
                    pG2D.fillRect(x, y, (int) Math.rint((entry.getValue().width - 1) * (info.getWoodInStorage() / info.getStorageCapacity())), h);
                }
            }
        }
    }
}
