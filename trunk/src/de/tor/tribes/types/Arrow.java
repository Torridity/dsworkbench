/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.ui.MapPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Torridity
 */
public class Arrow extends Line {

    private GeneralPath path = null;

    public Arrow() {
    }

    public void renderForm(Graphics2D g2d) {
        Point2D.Double s = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        Point2D.Double e = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPosEnd(), getYPosEnd());
        java.awt.Rectangle mapBounds = MapPanel.getSingleton().getBounds();

        /* setVisibleOnMap(mapBounds.intersectsLine(new Line2D.Double(s, e)));
        if (!isVisibleOnMap()) {
        return;
        }*/

        //store properties
        Stroke before = g2d.getStroke();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        Color cBefore = g2d.getColor();
        checkShowMode(g2d, getDrawColor());
        //start draw
        //g2d.setFont(fBefore.deriveFont((float) getTextSize()));
        //g2d.setStroke(getStroke());
        // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getDrawAlpha()));

        double w = Math.abs(s.x - e.x);
        double h = Math.abs(s.y - e.y);
        double c = s.distance(e);
        double a = Math.asin(h / c);

        /*
         * x > 0, y > 0: quadrant I.
        x < 0, y > 0: quadrant II.
        x < 0, y < 0: quadrant III
        x > 0, y < 0: quadrant IV
         */
        path = new GeneralPath();
        path.moveTo(10, 10);
        path.lineTo(0, 5);
        path.quadTo(50, -10, 100, 5);
        path.lineTo(95, 0);
        path.lineTo(110, 10);
        path.lineTo(95, 20);
        path.lineTo(100, 15);
        path.quadTo(50, 0, 0, 15);
        path.closePath();
        double rot = 0;

        if (e.x > s.x && e.y > s.y) {
            rot = Math.toDegrees(a);
        } else if (e.x < s.x && e.y > s.y) {
            rot = 180 - Math.toDegrees(a);
        } else if (e.x > s.x && e.y < s.y) {
            rot = 360 - Math.toDegrees(a);
        } else {
            rot = 180 + Math.toDegrees(a);
        }

        a = Math.toRadians(rot);
        AffineTransform trans = AffineTransform.getScaleInstance(c / 110.0, c / 110.0);
        path.transform(trans);
        trans = AffineTransform.getTranslateInstance(path.getBounds2D().getX(), 0);
        path.transform(trans);
        trans = AffineTransform.getRotateInstance(a, 0, 0);
        path.transform(trans);
        trans = AffineTransform.getTranslateInstance(s.x, s.y);
        path.transform(trans);
        g2d.draw(path);
        //reset properties
        g2d.setStroke(before);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
        g2d.setColor(cBefore);
    }
}
