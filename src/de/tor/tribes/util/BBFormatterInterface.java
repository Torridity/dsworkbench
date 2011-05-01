/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Torridity
 */
public interface BBFormatterInterface {

    public final static String LIST_START = "%LIST_START%";
    public final static String LIST_END = "%LIST_END%";
    public final static String ELEMENT_ID = "%ELEMENT_ID%";
    public final static String ELEMENT_COUNT = "%ELEMENT_COUNT%";

    public String getPropertyKey();

    public void storeProperty();

    public String getStandardTemplate();

    public String getTemplate();

    public String[] getTemplateVariables();

    public void setCustomTemplate(String pTemplate);
}
