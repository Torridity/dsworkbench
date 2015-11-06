/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.types;

import java.awt.Color;

/**
 *
 * @author Torridity
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
