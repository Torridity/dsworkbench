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

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import java.awt.Color;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Church extends ManageableType {

    public static final int NO_CHURCH = 0;
    public static final int RANGE1 = 4;
    public static final int RANGE2 = 6;
    public static final int RANGE3 = 8;
    private Village village = null;
    private int range = NO_CHURCH;
    // private transient Color rangeColor = Color.WHITE;

    public Church() {
    }

    @Override
    public String toXml() {
        try {
            String xml = "<church>\n";
            xml += "<village>" + village.getId() + "</village>\n";
            xml += "<range>" + range + "</range>\n";
            xml += "</church>";
            return xml;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * @return the villageId
     */
    public Village getVillage() {
        return village;
    }

    /**
     * @param pVillage 
     * @param villageId the villageId to set
     */
    public void setVillage(Village pVillage) {
        this.village = pVillage;
    }

    /**
     * @return the range
     */
    public int getRange() {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * @return the rangeColor
     */
    public Color getRangeColor() {
        return village.getTribe().getMarkerColor();
    }

    @Override
    public String getElementIdentifier() {
        return "church";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "churches";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }

    @Override
    public void loadFromXml(Element pElement) {
        this.village = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("village").getText()));
        this.range = Integer.parseInt(pElement.getChild("range").getText());
    }
}
