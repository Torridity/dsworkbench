/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.awt.Color;

/**
 *
 * @author Jejkal
 */
public class TagMapMarker {

    private int tagIcon = -1;
    private Color tagColor = null;

    /**
     * @return the tagIcon
     */
    public int getTagIcon() {
        return tagIcon;
    }

    /**
     * @param tagIcon the tagIcon to set
     */
    public void setTagIcon(int tagIcon) {
        this.tagIcon = tagIcon;
    }

    /**
     * @return the tagColor
     */
    public Color getTagColor() {
        return tagColor;
    }

    /**
     * @param tagColor the tagColor to set
     */
    public void setTagColor(Color tagColor) {
        this.tagColor = tagColor;
    }
}
