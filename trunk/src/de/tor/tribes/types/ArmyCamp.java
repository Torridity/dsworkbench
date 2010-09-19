/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.awt.Point;

/**
 *
 * @author Torridity
 */
public class ArmyCamp extends Village{

    private Ally ally = null;
   
    public ArmyCamp(Ally pAlly, short pX, short pY) {
        ally = pAlly;
       setX(pX);
       setY(pY);
    }

    /**
     * @return the ally
     */
    public Ally getAlly() {
        return ally;
    }

    /**
     * @param ally the ally to set
     */
    public void setAlly(Ally ally) {
        this.ally = ally;
    }
    
}
