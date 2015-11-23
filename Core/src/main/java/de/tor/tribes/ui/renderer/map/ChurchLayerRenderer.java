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

import de.tor.tribes.types.Church;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.util.algo.ChurchRangeCalculator;
import de.tor.tribes.util.church.ChurchManager;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
        HashMap<Tribe, Area> churchAreas = new HashMap<Tribe, Area>();
        List<Village> churchVillages = ChurchManager.getSingleton().getChurchVillages();

        for (Village v : churchVillages) {
            if (v != null && v.isVisibleOnMap()) {
                processField(v, pSettings.getFieldWidth(), pSettings.getFieldHeight(), churchAreas);
            }
        }

        Color cb = pG2D.getColor();
        Composite com = pG2D.getComposite();
        Set<Entry<Tribe, Area>> entries = churchAreas.entrySet();

        for (Entry<Tribe, Area> entry : entries) {
            Tribe t = entry.getKey();
            Area a = entry.getValue();
            pG2D.setColor(t.getMarkerColor());
            pG2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            pG2D.setStroke(new BasicStroke(13.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            pG2D.draw(a);
            pG2D.setComposite(com);
            pG2D.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            pG2D.draw(a);
        }
        pG2D.setComposite(com);
        pG2D.setColor(cb);
    }

    private void processField(Village v, int pFieldWidth, int pFieldHeight, HashMap<Tribe, Area> pChurchAreas) {
        Church c = ChurchManager.getSingleton().getChurch(v);
        GeneralPath p = calculateChurchPath(c, v, pFieldWidth, pFieldHeight);
        Tribe t = v.getTribe();
        Area a = pChurchAreas.get(t);
        if (a == null) {
            a = new Area();
            pChurchAreas.put(t, a);
        }
        a.add(new Area(p));
    }

    public static GeneralPath calculateChurchPath(Church c, Village v, int pFieldWidth, int pFieldHeight) {
        int vx = MapPanel.getSingleton().virtualPosToSceenPos(v.getX(), v.getY()).x;
        int vy = MapPanel.getSingleton().virtualPosToSceenPos(v.getX(), v.getY()).y;
        Rectangle g = new Rectangle(vx, vy, (int) Math.rint(pFieldWidth), (int) Math.rint(pFieldHeight));
        List<Point2D.Double> positions = ChurchRangeCalculator.getChurchRange(v.getX(), v.getY(), c.getRange());
        GeneralPath p = new GeneralPath();
        p.moveTo(g.getX(), g.getY() - (c.getRange() - 1) * pFieldHeight);
        int quad = 0;
        Point2D.Double lastPos = positions.get(0);
        for (Point2D.Double pos : positions) {
            if (quad == 0) {
                //north village
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() - g.getHeight());
                p.lineTo(p.getCurrentPoint().getX() + g.getWidth(), p.getCurrentPoint().getY());
                quad = 1;
            } else if (pos.getX() == v.getX() + c.getRange() && pos.getY() == v.getY()) {
                //east village
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                p.lineTo(p.getCurrentPoint().getX() + g.getWidth(), p.getCurrentPoint().getY());
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                quad = 2;
            } else if (pos.getX() == v.getX() && pos.getY() == v.getY() + c.getRange()) {
                //south village
                p.lineTo(p.getCurrentPoint().getX() - g.getWidth(), p.getCurrentPoint().getY());
                p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                p.lineTo(p.getCurrentPoint().getX() - g.getWidth(), p.getCurrentPoint().getY());
                quad = 3;
            } else if (pos.getX() == v.getX() - c.getRange() && pos.getY() == v.getY()) {
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
