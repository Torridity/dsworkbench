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
package de.tor.tribes.types.ext;

import java.text.NumberFormat;

/**
 *
 * @author Charon
 */
public class BarbarianAlly extends Ally {

    private static BarbarianAlly SINGLETON = null;

    public static synchronized BarbarianAlly getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new BarbarianAlly();
        }
        return SINGLETON;
    }

    public int getId() {
        return 0;
    }

    @Override
    public String toBBCode() {
        return "Barbaren";
    }

    public String getName() {
        return "Barbaren";
    }

    public String getTag() {
        return "-";
    }

    public short getMembers() {
        return 0;
    }

    public double getPoints() {
        return 0;
    }

    public int getRank() {
        return 0;
    }

    public Tribe[] getTribes() {
        return new Tribe[]{Barbarians.getSingleton()};
    }

    public String toString() {
        return "Barbaren";
    }

    @Override
    public void setVillages(int villages) {
        //do nothing
    }

    public String getToolTipText() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String res = "<html><table style='border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;'>";
        res += "<tr><td><b>Stamm:</b> </td><td>" + toString() + "</td></tr>";
        res += "</table></html>";
        return res;
    }
}
