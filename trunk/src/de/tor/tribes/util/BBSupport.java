/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

/**
 *
 * @author Torridity
 */
public interface BBSupport<C> {

    public String getStandardTemplate();

    public String getTemplateProperty();

    public String[] getBBVariables();

    public String[] getReplacements(boolean pExtended);
}
