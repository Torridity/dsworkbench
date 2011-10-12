/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.conquer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.Village;
import java.awt.Point;

/**
 *
 * @author Charon
 */
public class ContinentFilter implements ConquerFilterInterface {

    Point continentBounds = null;

    @Override
    public void setup(Object pFilterComponent) {
        try {
            continentBounds = (Point) pFilterComponent;
        } catch (Exception e) {
            continentBounds = null;
        }
    }

    @Override
    public boolean isValid(Conquer pConquer) {
        if (continentBounds == null) {
            return true;
        }
        Village v = pConquer.getVillage();
        if (v == null) {
            return false;
        }
        return v.getContinent() >= continentBounds.x && v.getContinent() <= continentBounds.y;
    }
}
