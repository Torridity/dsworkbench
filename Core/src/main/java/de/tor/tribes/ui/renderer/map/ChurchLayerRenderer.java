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

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.algo.ChurchRangeCalculator;
import de.tor.tribes.util.village.KnownVillageManager;
import de.tor.tribes.util.village.KnownVillage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Torridity
 */
public class ChurchLayerRenderer extends AbstractDirectLayerRenderer {

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        if (!pSettings.isLayerVisible()) {
            return;
        }
        renderRows(pSettings, pG2d);
    }

    private void renderRows(RenderSettings pSettings, Graphics2D pG2D) {
        //iterate through entire rows
        HashMap<Tribe, Area> churchAreas = new HashMap<>();
        List<KnownVillage> churchVillages = KnownVillageManager.getSingleton().getChurchVillages();

        for (KnownVillage v : churchVillages) {
            if (v != null && v.getVillage().isVisibleOnMap()) {
                processField(v, pSettings.getFieldWidth(), pSettings.getFieldHeight(), churchAreas);
            }
        }

        Color cb = pG2D.getColor();
        Composite com = pG2D.getComposite();
        Stroke st = pG2D.getStroke();
        Set<Entry<Tribe, Area>> entries = churchAreas.entrySet();

        for (Entry<Tribe, Area> entry : entries) {
            Tribe t = entry.getKey();
            Area a = entry.getValue();
            pG2D.setColor(t.getMarkerColor());
            pG2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            pG2D.setStroke(new BasicStroke(13.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0.0f, new float[] {3, 5}, 0.0f));
            pG2D.draw(a);
            pG2D.setComposite(com);
            pG2D.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0.0f, new float[] {3, 5}, 0.0f));
            pG2D.draw(a);
        }
        pG2D.setComposite(com);
        pG2D.setColor(cb);
        pG2D.setStroke(st);
    }

    void renderTempChurch(Graphics2D g2d, KnownVillage tmpVillage, Rectangle r) {
        Composite cb = g2d.getComposite();
        Color cob = g2d.getColor();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
        GeneralPath p = ChurchLayerRenderer.calculateChurchPath(tmpVillage, r.width, r.height);
        g2d.setColor(Constants.DS_BACK_LIGHT);
        g2d.fill(p);
        g2d.setComposite(cb);
        g2d.setColor(Constants.DS_BACK);
        g2d.draw(p);
        g2d.setColor(cob);
    }

    private void processField(KnownVillage v, int pFieldWidth, int pFieldHeight, HashMap<Tribe, Area> pChurchAreas) {
        GeneralPath p = calculateChurchPath(v, pFieldWidth, pFieldHeight);
        Tribe t = v.getVillage().getTribe();
        Area a = pChurchAreas.get(t);
        if (a == null) {
            a = new Area();
            pChurchAreas.put(t, a);
        }
        a.add(new Area(p));
    }

    public static GeneralPath calculateChurchPath(KnownVillage v, int pFieldWidth, int pFieldHeight) {
        int villageX = v.getVillage().getX();
        int villageY = v.getVillage().getY();
        int vx = MapPanel.getSingleton().virtualPosToSceenPos(villageX, villageY).x;
        int vy = MapPanel.getSingleton().virtualPosToSceenPos(villageX, villageY).y;
        Rectangle g = new Rectangle(vx, vy, (int) Math.rint(pFieldWidth), (int) Math.rint(pFieldHeight));
        List<Point2D.Double> positions = ChurchRangeCalculator.getChurchRange(villageX, villageY, v.getChurchRange());
        GeneralPath p = new GeneralPath();
        p.moveTo(g.getX(), g.getY() - (v.getChurchRange() - 1) * pFieldHeight);
        int quad = 0;
        Point2D.Double lastPos = positions.get(0);
        for (Point2D.Double pos : positions) {
            if (quad == 0) {
                //north village
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() - g.getHeight());
                p.lineTo(p.getCurrentPoint().getX() + g.getWidth(), p.getCurrentPoint().getY());
                quad = 1;
            } else if (pos.getX() == villageX + v.getChurchRange() && pos.getY() == villageY) {
                //east village
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                p.lineTo(p.getCurrentPoint().getX() + g.getWidth(), p.getCurrentPoint().getY());
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                quad = 2;
            } else if (pos.getX() == villageX && pos.getY() == villageY + v.getChurchRange()) {
                //south village
                p.lineTo(p.getCurrentPoint().getX() - g.getWidth(), p.getCurrentPoint().getY());
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                p.lineTo(p.getCurrentPoint().getX() - g.getWidth(), p.getCurrentPoint().getY());
                quad = 3;
            } else if (pos.getX() == villageX - v.getChurchRange() && pos.getY() == villageY) {
                //west village
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() - g.getHeight());
                p.lineTo(p.getCurrentPoint().getX() - g.getWidth(), p.getCurrentPoint().getY());
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() - g.getHeight());
                quad = 4;
            } else {
                //no special point
                int dx = (int) (pos.getX() - lastPos.getX());
                int dy = (int) (pos.getY() - lastPos.getY());
                if (quad == 1) {
                    p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + dy * g.getHeight());
                    p.lineTo(p.getCurrentPoint().getX() + dx * g.getWidth(), p.getCurrentPoint().getY());
                } else if (quad == 2) {
                    p.lineTo(p.getCurrentPoint().getX() + dx * g.getWidth(), p.getCurrentPoint().getY());
                    p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + dy * g.getHeight());
                } else if (quad == 3) {
                    p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + dy * g.getHeight());
                    p.lineTo(p.getCurrentPoint().getX() + dx * g.getWidth(), p.getCurrentPoint().getY());
                } else if (quad == 4) {
                    p.lineTo(p.getCurrentPoint().getX() + dx * g.getWidth(), p.getCurrentPoint().getY());
                    p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + dy * g.getHeight());
                }
            }
            lastPos = pos;
        }

        p.closePath();
        return p;
    }
}
