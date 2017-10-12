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

import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchTroopsFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.Intersection;
import de.tor.tribes.util.troops.SupportVillageTroopsHolder;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Torridity
 */
public class SupportLayerRenderer extends AbstractDirectLayerRenderer {

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        // RenderSettings settings = getRenderSettings(pVirtualBounds);
        if (!pSettings.isLayerVisible()) {
            return;
        }
        Point2D.Double mapPos = new Point2D.Double(pSettings.getMapBounds().getX(), pSettings.getMapBounds().getY());
        Stroke s = pG2d.getStroke();
        Color b = pG2d.getColor();
        List<Village> visibleVillages = new LinkedList<>();
        for (int i = 0; i < pSettings.getVillagesInX(); i++) {
            for (int j = 0; j < pSettings.getVillagesInY(); j++) {
                Village v = pSettings.getVisibleVillage(i, j);
                if (v != null) {
                    visibleVillages.add(v);
                }
            }
        }
        pG2d.setStroke(new BasicStroke(2.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        renderSupports(mapPos, visibleVillages, pSettings, pG2d);
        pG2d.setStroke(s);
        pG2d.setColor(b);
    }

    private void renderSupports(Point2D.Double pMapPos, List<Village> pVisibleVillages, RenderSettings pSettings, Graphics2D pG2D) {
        for (Village v : DSWorkbenchTroopsFrame.getSingleton().getSelectedSupportVillages()) {
            //process source villages
            if (v.isVisibleOnMap()) {
                List<Village> villages = new LinkedList<>();
                SupportVillageTroopsHolder holder = (SupportVillageTroopsHolder) TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.SUPPORT);

                HashMap<Village, TroopAmountFixed> incs = holder.getIncomingSupports();
                HashMap<Village, TroopAmountFixed> outs = holder.getOutgoingSupports();
                
                Set<Village> keys = incs.keySet();
                for(Village key: keys) {
                    if (!villages.contains(key)) {
                        villages.add(key);
                    }
                }
                keys = outs.keySet();
                for(Village key: keys) {
                    if (!villages.contains(key)) {
                        villages.add(key);
                    }
                }

                for (Village target : villages) {
                    Line2D.Double supportLine = new Line2D.Double(v.getX() * pSettings.getFieldWidth(), v.getY() * pSettings.getFieldHeight(), target.getX() * pSettings.getFieldWidth(), target.getY() * pSettings.getFieldHeight());

                    //draw full line
                    double xStart = (supportLine.getX1() - pMapPos.getX() * pSettings.getFieldWidth()) + pSettings.getFieldWidth() / 2;
                    double yStart = (supportLine.getY1() - pMapPos.getY() * pSettings.getFieldHeight()) + pSettings.getFieldHeight() / 2;
                    double xEnd = (supportLine.getX2() - pMapPos.getX() * pSettings.getFieldWidth()) + pSettings.getFieldWidth() / 2;
                    double yEnd = (supportLine.getY2() - pMapPos.getY() * pSettings.getFieldHeight()) + pSettings.getFieldHeight() / 2;

                    if (pVisibleVillages.contains(v) && pVisibleVillages.contains(target)) {
                        pG2D.setColor(Color.YELLOW);
                        pG2D.drawLine((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                        pG2D.drawOval((int) xEnd - 2, (int) yEnd - 2, 4, 4);
                    } else if (pVisibleVillages.contains(v) && !pVisibleVillages.contains(target)) {
                        pG2D.setColor(Color.GREEN);
                        //draw clipped support line
                        pG2D.setClip((int) Math.rint(xStart - 50), (int) Math.rint(yStart - 50), 100, 100);
                        pG2D.drawLine((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                        pG2D.setClip(null);
                        //get bounding rectangle sides
                        Line2D.Double top = new Line2D.Double(xStart - 50.0, yStart - 50.0, xStart + 50.0, yStart - 50.0);
                        Line2D.Double right = new Line2D.Double(xStart + 50.0, yStart - 50.0, xStart + 50.0, yStart + 50.0);
                        Line2D.Double bottom = new Line2D.Double(xStart - 50.0, yStart + 50.0, xStart + 50.0, yStart + 50.0);
                        Line2D.Double left = new Line2D.Double(xStart - 50.0, yStart - 50.0, xStart - 50.0, yStart + 50.0);

                        //get intersection point
                        Point2D inter = new Point.Double();
                        try {
                            inter = Intersection.getIntersection(new Line2D.Double(xStart, yStart, xEnd, yEnd), top);
                            if (inter == null) {
                                throw new Exception();
                            }
                        } catch (Exception e1) {
                            try {
                                inter = Intersection.getIntersection(new Line2D.Double(xStart, yStart, xEnd, yEnd), right);
                                if (inter == null) {
                                    throw new Exception();
                                }
                            } catch (Exception e2) {
                                try {
                                    inter = Intersection.getIntersection(new Line2D.Double(xStart, yStart, xEnd, yEnd), bottom);
                                    if (inter == null) {
                                        throw new Exception();
                                    }
                                } catch (Exception e3) {
                                    try {
                                        inter = Intersection.getIntersection(new Line2D.Double(xStart, yStart, xEnd, yEnd), left);
                                        if (inter == null) {
                                            throw new Exception();
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                        //draw distance label
                        if (inter != null) {
                            double dist = DSCalculator.calculateDistance(v, target);
                            String d = NumberFormat.getInstance().format(dist);
                            Rectangle2D bb = pG2D.getFontMetrics().getStringBounds(d, pG2D);
                            pG2D.fillRect((int) (inter.getX() + bb.getX()), (int) (inter.getY() + bb.getY()), (int) bb.getWidth(), (int) bb.getHeight());
                            pG2D.setColor(Color.BLACK);
                            pG2D.drawString(d, (int) inter.getX(), (int) inter.getY());
                        }
                    } else if (!pVisibleVillages.contains(v) && pVisibleVillages.contains(target)) {
                        pG2D.setColor(Color.RED);
                        //draw clipped support line
                        pG2D.setClip((int) Math.rint(xEnd - 50), (int) Math.rint(yEnd - 50), 100, 100);
                        pG2D.drawLine((int) Math.rint(xEnd), (int) Math.rint(yEnd), (int) Math.rint(xStart), (int) Math.rint(yStart));
                        pG2D.setClip(null);
                        //get bounding rectangle sides
                        Line2D.Double top = new Line2D.Double(xEnd - 50.0, yEnd - 50.0, xEnd + 50.0, yEnd - 50.0);
                        Line2D.Double right = new Line2D.Double(xEnd + 50.0, yEnd - 50.0, xEnd + 50.0, yEnd + 50.0);
                        Line2D.Double bottom = new Line2D.Double(xEnd - 50.0, yEnd + 50.0, xEnd + 50.0, yEnd + 50.0);
                        Line2D.Double left = new Line2D.Double(xEnd - 50.0, yEnd - 50.0, xEnd - 50.0, yEnd + 50.0);

                        //get intersection point
                        Point2D inter = new Point.Double();
                        try {
                            inter = Intersection.getIntersection(new Line2D.Double(xEnd, yEnd, xStart, yStart), top);
                            if (inter == null) {
                                throw new Exception();
                            }
                        } catch (Exception e1) {
                            try {
                                inter = Intersection.getIntersection(new Line2D.Double(xEnd, yEnd, xStart, yStart), right);
                                if (inter == null) {
                                    throw new Exception();
                                }
                            } catch (Exception e2) {
                                try {
                                    inter = Intersection.getIntersection(new Line2D.Double(xEnd, yEnd, xStart, yStart), bottom);
                                    if (inter == null) {
                                        throw new Exception();
                                    }
                                } catch (Exception e3) {
                                    try {
                                        inter = Intersection.getIntersection(new Line2D.Double(xEnd, yEnd, xStart, yStart), left);
                                        if (inter == null) {
                                            throw new Exception();
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }

                        //draw distance label
                        if (inter != null) {
                            double dist = DSCalculator.calculateDistance(v, target);
                            String d = NumberFormat.getInstance().format(dist);
                            Rectangle2D bb = pG2D.getFontMetrics().getStringBounds(d, pG2D);
                            pG2D.fillRect((int) Math.floor(inter.getX() + bb.getX()), (int) Math.floor(inter.getY() + bb.getY()), (int) Math.floor(bb.getWidth()), (int) Math.floor(bb.getHeight()));
                            pG2D.setColor(Color.BLACK);
                            pG2D.drawString(d, (int) inter.getX(), (int) inter.getY());
                        }
                    }
                }
            }
        }
    }

    private boolean Intersects(Point2D.Double line1Point1, Point2D.Double line1Point2, Point2D.Double line2Point1, Point2D.Double line2Point2, Point2D.Double intersection) { // Based on the 2d line intersection method from "comp.graphics.algorithmsFrequently Asked Questions"

        /*
        (Ay-Cy)(Dx-Cx)-(Ax-Cx)(Dy-Cy)
        r = -----------------------------  (eqn 1)
        (Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx)
         */

        double q = (line1Point1.x - line2Point1.y) * (line2Point2.x - line2Point1.x) - (line1Point1.x - line2Point1.x) * (line2Point2.y - line2Point1.x);
        double d = (line1Point2.x - line1Point1.x) * (line2Point2.y - line2Point1.y) - (line1Point2.y - line1Point1.y) * (line2Point2.y - line2Point1.x);

        if (d == 0) // parallel lines so no intersection anywhere in space (incurved space, maybe, but not here in Euclidian space.)
        {
            return false;
        }

        double r = q / d;

        /*
        (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
        s = -----------------------------  (eqn 2)
        (Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx)
         */

        q = (line1Point1.y - line2Point1.y) * (line1Point2.x - line1Point1.x) - (line1Point1.x - line2Point1.x) * (line1Point2.y - line1Point1.y);
        double s = q / d;

        /*
        If r>1, P is located on extension of AB
        If r<0, P is located on extension of BA
        If s>1, P is located on extension of CD
        If s<0, P is located on extension of DC
        
        The above basically checks if the intersection is located at an
        extrapolated
        point outside of the line segments. To ensure the intersection is
        only within
        the line segments then the above must all be false, ie r between 0
        and 1
        and s between 0 and 1.
         */

        if (r < 0 || r > 1 || s < 0 || s > 1) {
            return false;
        }

        /*
        Px=Ax+r(Bx-Ax)
        Py=Ay+r(By-Ay)
         */

        intersection.x = line1Point1.x + (int) (0.5f + r * (line1Point2.x - line1Point1.x));
        intersection.y = line1Point1.y + (int) (0.5f + r * (line1Point2.y - line1Point1.y));
        return true;
    }
}
