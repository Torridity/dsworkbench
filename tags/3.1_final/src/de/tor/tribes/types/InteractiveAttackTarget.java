/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author Torridity
 */
public class InteractiveAttackTarget extends InteractiveAttackElement {

    private BufferedImage mImage = null;
    private ArrayList<InteractiveAttackSource> linkedSources = null;

    public InteractiveAttackTarget(Village pVillage, Point2D.Double pInitialPosition) {
        super(pVillage, pInitialPosition);
        linkedSources = new ArrayList<InteractiveAttackSource>();
        try {
            mImage = ImageIO.read(new File("graphics/v5_planer.png"));
        } catch (Exception e) {
        }
    }

    public InteractiveAttackTarget(Village pVillage) {
        this(pVillage, null);
    }

    public void linkSource(InteractiveAttackSource pSource) {
        if (!linkedSources.contains(pSource)) {
            linkedSources.add(pSource);
        }
    }

    public void unlinkSource(InteractiveAttackSource pSource) {
        linkedSources.remove(pSource);
    }

    public int getLinkedElementCount() {
        return linkedSources.size();
    }

    public boolean supportsAttackSourceLinkage(InteractiveAttackElement pElement) {
        if (pElement == null || !(pElement instanceof InteractiveAttackSource)) {
            return false;
        }
        return isLinkageAllowed(pElement);
    }

    @Override
    public void render(Graphics2D g2d, boolean pHighlight) {
        g2d.drawImage(mImage, null, (int) Math.round(getPosition().getX()), (int) Math.round(getPosition().getY()));
        if (pHighlight) {
            g2d.setColor(Color.yellow);
            g2d.drawRect((int) Math.round(getPosition().getX()), (int) Math.round(getPosition().getY()), mImage.getWidth(), mImage.getHeight());
        }
        int cnt = 0;
        for (InteractiveAttackSource source : linkedSources) {
            source.render(g2d, (int) Math.round(getPosition().getX()), (int) Math.round(getPosition().getY()) + cnt, false);
            cnt += (pHighlight) ? 10 : 2;
        }
    }

    public void applyForce(double pFx, double pFy, Point2D.Double pAttractionPoint) {
        double d = getPosition().distance(pAttractionPoint);
        if (d < 40) {
            return;
        }
        double dx = pFx / 8 * Math.log10(d);
        double dy = pFy / 8 * Math.log10(d);

        translatePosition(-dx, -dy);
    }

    public void render(Graphics2D g2d) {
        render(g2d, false);
    }

    @Override
    public Dimension getSize() {
        return new Dimension(mImage.getWidth(), mImage.getHeight());
    }

    @Override
    public String toString() {
        return hashCode() + ")Target";
    }
}
