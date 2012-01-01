/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.control;

import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public abstract class ManageableType {

    public ManageableType() {
    }

    public ManageableType(Element e) {
        loadFromXml(e);
    }

    public abstract String getElementIdentifier();

    public abstract String getElementGroupIdentifier();

    public abstract String getGroupNameAttributeIdentifier();

    public abstract String toXml();

    public abstract void loadFromXml(Element e);
}
