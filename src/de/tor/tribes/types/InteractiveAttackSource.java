/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author Torridity
 */
public class InteractiveAttackSource extends InteractiveAttackElement {

    private BufferedImage mImage = null;
    private InteractiveAttackTarget mLinkedTarget = null;

    public InteractiveAttackSource(Village pVillage, Point2D.Double pInitialPosition, String pImage) {
        super(pVillage, pInitialPosition);
        try {
            mImage = ImageIO.read(new File(pImage));
        } catch (Exception e) {
        }
    }

    public InteractiveAttackSource(Village pVillage, Point2D.Double pInitialPosition) {
        this(pVillage, pInitialPosition, "graphics/icons/snob.png");
    }

    public InteractiveAttackSource(Village pVillage) {
        this(pVillage, null);

    }

    public void linkTarget(InteractiveAttackTarget pTarget) {
        mLinkedTarget = pTarget;
    }

    public void unlinkTarget() {
        if (isLinked()) {
            mLinkedTarget.unlinkSource(this);
            mLinkedTarget = null;
        }
    }

    public boolean isLinked() {
        return mLinkedTarget != null;
    }

    public void render(Graphics2D g2d) {
        render(g2d, (int) Math.round(getPosition().getX()), (int) Math.round(getPosition().getY()), false);
    }

    @Override
    public void render(Graphics2D g2d, boolean pHighlight) {
        render(g2d, (int) Math.round(getPosition().getX()), (int) Math.round(getPosition().getY()), pHighlight);
    }

    public void render(Graphics2D g2d, double pX, double pY, boolean pHighlight) {
        setPosition(pX, pY);
        g2d.setColor(Color.RED);
        g2d.fillOval((int) Math.round(getPosition().getX()) - 5, (int) Math.round(getPosition().getY()) - 5, mImage.getWidth() + 6, mImage.getHeight() + 6);
        if (pHighlight) {
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.BLACK);
        }
        g2d.drawOval((int) Math.round(getPosition().getX()) - 5, (int) Math.round(getPosition().getY()) - 5, mImage.getWidth() + 6, mImage.getHeight() + 6);
        g2d.drawImage(mImage, null, (int) Math.round(getPosition().getX()), (int) Math.round(getPosition().getY()));
    }

    @Override
    public Dimension getSize() {
        return new Dimension(mImage.getWidth(), mImage.getHeight());
    }

    @Override
    public String toString() {
        return hashCode() + ") Source " + ((isLinked()) ? "-> " + mLinkedTarget : "none");
    }
}
