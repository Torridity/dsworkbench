/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

/**
 *
 * @author Jejkal
 */
public interface AlgorithmListener {

    public void fireCalculationFinishedEvent(AbstractAttackAlgorithm pParent);
}
