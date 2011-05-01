/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class InteractiveAttackElementGroup {

    private ArrayList<InteractiveAttackElement> mElements = null;
    private Point mPosition = null;
    private Point delta = null;

    public InteractiveAttackElementGroup() {
        mElements = new ArrayList<InteractiveAttackElement>();
        delta = new Point(0, 0);
    }

    public void addElement(InteractiveAttackElement pElement) {
        if (pElement != null && !mElements.contains(pElement)) {
            mElements.add(pElement);
        }
    }

    public void removeElement(InteractiveAttackElement pElement) {
        mElements.remove(pElement);
    }

    public void removeAllElements() {
        mElements.clear();
    }

    public void addElements(List<InteractiveAttackElement> pElements) {
        if (pElements == null || pElements.isEmpty()) {
            return;
        }
        for (InteractiveAttackElement element : pElements) {
            addElement(element);
        }
    }

    public InteractiveAttackElement[] getGroupElements() {
        return mElements.toArray(new InteractiveAttackElement[]{});
    }

    public boolean isSourcesOnly() {
        for (InteractiveAttackElement elem : getGroupElements()) {
            if (elem instanceof InteractiveAttackTarget) {
                return false;
            }
        }
        return true;
    }

    public void resetPosition() {
        mPosition = null;
    }

    public void movePosition(int pDx, int pDy) {
        if (mPosition == null) {
            delta = new Point(0, 0);
            mPosition = new Point(pDx, pDy);
        } else {
            delta.setLocation(pDx - mPosition.x, pDy - mPosition.y);
            mPosition.setLocation(pDx, pDy);
        }
    }

    public boolean containsElement(InteractiveAttackElement pElement) {
        return mElements.contains(pElement);
    }

    public void setPosition(Point pPosition) {
        movePosition(pPosition.x, pPosition.y);
    }

    public Point getPosition() {
        return mPosition;
    }

    public void render(Graphics2D g2d) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (InteractiveAttackElement e : mElements) {
            e.translatePosition(delta.getX(), delta.getY());
            if (e.getPosition().x < minX) {
                minX = e.getPosition().x;
            }

            if (e.getPosition().x + e.getBounds().getWidth() > maxX) {
                maxX = e.getPosition().x + e.getBounds().getWidth();
            }
            if (e.getPosition().y < minY) {
                minY = e.getPosition().y;
            }

            if (e.getPosition().y + e.getBounds().getHeight() > maxY) {
                maxY = e.getPosition().y + e.getBounds().getHeight();
            }
        }

        g2d.drawRect((int) Math.ceil(minX - 10), (int) Math.ceil(minY - 10), (int) Math.floor(maxX - minX + 20), (int) Math.floor(maxY - minY + 20));
    }
}
