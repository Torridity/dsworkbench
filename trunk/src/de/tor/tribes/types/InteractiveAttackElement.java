/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.util.DSCalculator;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Torridity
 */
public abstract class InteractiveAttackElement {

    private Village mVillage = null;
    private Point2D.Double mPosition = null;
    private Point2D.Double mLastPosition = null;

    public InteractiveAttackElement(Village pVillage, Point2D.Double pInitialPosition) {
        mVillage = pVillage;
        if (pInitialPosition == null) {
            mPosition = new Point2D.Double(0, 0);
            mLastPosition = new Point2D.Double(0, 0);
        } else {
            mPosition = new Point2D.Double(pInitialPosition.x, pInitialPosition.y);
            mLastPosition = new Point2D.Double(pInitialPosition.x, pInitialPosition.y);
        }
    }

    public boolean isLinkageAllowed(InteractiveAttackElement pElement) {
        return DSCalculator.calculateDistance(mVillage, pElement.getVillage()) < 10;
    }

    public Village getVillage() {
        return mVillage;
    }

    public InteractiveAttackElement(Village pVillage) {
        this(pVillage, null);
    }

    public void movePosition(double pDx, double pDy) {
        mLastPosition.setLocation(mPosition);
        mPosition.setLocation(pDx - getSize().width / 2, pDy - getSize().height / 2);
    }

    public void translatePosition(double pDx, double pDy) {
        mLastPosition.setLocation(mPosition);
        mPosition.setLocation(mPosition.getX() + pDx, mPosition.getY() + pDy);
    }

    public Point2D.Double getPosition() {
        return mPosition;
    }

    public void setPosition(double pX, double pY) {
        mLastPosition.setLocation(mPosition);
        mPosition.setLocation(pX, pY);
    }

    public Point2D.Double getLastPosition() {
        return mLastPosition;
    }

    public Point2D.Double getDelta() {
        return new Point2D.Double(mLastPosition.x - mPosition.x, mLastPosition.y - mPosition.y);
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(mPosition.getX(), mPosition.getY(), getSize().getWidth(), getSize().getHeight());
    }

    public abstract Dimension getSize();

    public abstract void render(Graphics2D g2d, boolean pHighlight);
}
