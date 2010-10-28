/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchTroopsFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

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
        List<Village> visibleVillages = new LinkedList<Village>();
        for (int i = 0; i < pSettings.getVisibleVillages().length; i++) {
            for (int j = 0; j < pSettings.getVisibleVillages()[0].length; j++) {
                if (pSettings.getVisibleVillages()[i][j] != null) {
                    visibleVillages.add(pSettings.getVisibleVillages()[i][j]);
                }
            }
        }
        pG2d.setStroke(new BasicStroke(2.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        renderSupports(mapPos, visibleVillages, pSettings, pG2d);
        pG2d.setStroke(s);
        pG2d.setColor(b);
    }

    private void renderSupports(Point2D.Double pMapPos, List<Village> pVisibleVillages, RenderSettings pSettings, Graphics2D pG2D) {

        for (Village v : DSWorkbenchTroopsFrame.getSingleton().getSelectedTroopsVillages()) {
            //process source villages

            List<Village> villages = new LinkedList<Village>();

            for (Village target : TroopsManager.getSingleton().getTroopsForVillage(v).getSupportTargets()) {
                villages.add(target);
            }

            Enumeration<Village> sources = TroopsManager.getSingleton().getTroopsForVillage(v).getSupports().keys();
            while (sources.hasMoreElements()) {
                Village source = sources.nextElement();
                if (!villages.contains(source)) {
                    villages.add(source);
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
                    pG2D.setClip((int) Math.rint(xStart - 50), (int) Math.rint(yStart - 50), 100, 100);
//                  supportLine = new Line2D.Double((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                    pG2D.drawLine((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                    pG2D.setClip(null);
                    Line2D.Double top = new Line2D.Double(xStart - 50.0, yStart - 50.0, xStart + 50.0, yStart - 50.0);
                    Line2D.Double right = new Line2D.Double(xStart + 50.0, yStart - 50.0, xStart + 50.0, yStart + 50.0);
                    Line2D.Double bottom = new Line2D.Double(xStart - 50.0, yStart + 50.0, xStart + 50.0, yStart + 50.0);
                    Line2D.Double left = new Line2D.Double(xStart - 50.0, yStart - 50.0, xStart - 50.0, yStart + 50.0);
                    double x1 = xStart;
                    double x2 = xEnd;
                    double y1 = yStart;
                    double y2 = yEnd;

                    double x3 = xStart + 50.0;
                    double x4 = xStart + 50.0;
                    double y3 = yStart - 50.0;
                    double y4 = yStart + 50.0;

                    if (supportLine.intersectsLine(top)) {
                        x3 = top.x1;
                        x4 = top.x2;
                        y3 = top.y1;
                        y4 = top.y2;
                    } else if (supportLine.intersectsLine(right)) {
                        x3 = right.x1;
                        x4 = right.x2;
                        y3 = right.y1;
                        y4 = right.y2;
                    } else if (supportLine.intersectsLine(bottom)) {
                        x3 = bottom.x1;
                        x4 = bottom.x2;
                        y3 = bottom.y1;
                        y4 = bottom.y2;
                    } else {
                        x3 = left.x1;
                        x4 = left.x2;
                        y3 = left.y1;
                        y4 = left.y2;
                    }

                    double u0 = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
                    //double u1 = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
                    double x = x1 + u0 * (x2 - x1);
                    double y = y1 + u0 * (y2 - y1);

                    double dist = DSCalculator.calculateDistance(v, target);
                    String d = NumberFormat.getInstance().format(dist);
                    Rectangle2D bb = pG2D.getFontMetrics().getStringBounds(d, pG2D);
                    pG2D.fillRect((int) (x + bb.getX()), (int) (y + bb.getY()), (int) bb.getWidth(), (int) bb.getHeight());
                    pG2D.setColor(Color.BLACK);
                    pG2D.drawString(d, (int) x, (int) y);
                } else if (!pVisibleVillages.contains(v) && pVisibleVillages.contains(target)) {
                    pG2D.setColor(Color.RED);
                    pG2D.setClip((int) Math.rint(xEnd - 50), (int) Math.rint(yEnd - 50), 100, 100);
//                  supportLine = new Line2D.Double((int) Math.rint(xEnd), (int) Math.rint(yEnd), (int) Math.rint(xStart), (int) Math.rint(yStart));
                    pG2D.drawLine((int) Math.rint(xEnd), (int) Math.rint(yEnd), (int) Math.rint(xStart), (int) Math.rint(yStart));
                    pG2D.setClip(null);
                    Line2D.Double top = new Line2D.Double(xEnd - 50.0, yEnd - 50.0, xEnd + 50.0, yEnd - 50.0);
                    Line2D.Double right = new Line2D.Double(xEnd + 50.0, yEnd - 50.0, xEnd + 50.0, yEnd + 50.0);
                    Line2D.Double bottom = new Line2D.Double(xEnd - 50.0, yEnd + 50.0, xEnd + 50.0, yEnd + 50.0);
                    Line2D.Double left = new Line2D.Double(xEnd - 50.0, yEnd - 50.0, xEnd - 50.0, yEnd + 50.0);
                    double x1 = xEnd;
                    double x2 = xStart;
                    double y1 = yEnd;
                    double y2 = yStart;

                    double x3 = xStart + 50.0;
                    double x4 = xStart + 50.0;
                    double y3 = yStart - 50.0;
                    double y4 = yStart + 50.0;

                    if (supportLine.intersectsLine(top)) {
                        x3 = top.x1;
                        x4 = top.x2;
                        y3 = top.y1;
                        y4 = top.y2;
                    } else if (supportLine.intersectsLine(right)) {
                        x3 = right.x1;
                        x4 = right.x2;
                        y3 = right.y1;
                        y4 = right.y2;
                    } else if (supportLine.intersectsLine(bottom)) {
                        x3 = bottom.x1;
                        x4 = bottom.x2;
                        y3 = bottom.y1;
                        y4 = bottom.y2;
                    } else {
                        x3 = left.x1;
                        x4 = left.x2;
                        y3 = left.y1;
                        y4 = left.y2;
                    }

                    double u0 = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
                    //double u1 = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
                    double x = x1 + u0 * (x2 - x1);
                    double y = y1 + u0 * (y2 - y1);

                    double dist = DSCalculator.calculateDistance(v, target);
                    String d = NumberFormat.getInstance().format(dist);
                    Rectangle2D bb = pG2D.getFontMetrics().getStringBounds(d, pG2D);
                    pG2D.fillRect((int) Math.floor(x + bb.getX()), (int) Math.floor(y + bb.getY()), (int) Math.floor(bb.getWidth()), (int) Math.floor(bb.getHeight()));
                    pG2D.setColor(Color.BLACK);
                    pG2D.drawString(d, (int) x, (int) y);
                }
            }
        }
    }
}
