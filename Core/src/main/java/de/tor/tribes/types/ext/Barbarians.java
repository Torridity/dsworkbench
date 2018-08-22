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
public class Barbarians extends Tribe {

    private static Barbarians SINGLETON = null;

    public static synchronized Barbarians getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new Barbarians();
        }
        return SINGLETON;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String toBBCode() {
        return "Barbaren";
    }

    @Override
    public String getName() {
        return "Barbaren";
    }

    @Override
    public int getAllyID() {
        return BarbarianAlly.getSingleton().getId();
    }

    @Override
    public short getVillages() {
        return 0;
    }

    @Override
    public double getPoints() {
        return 0;
    }

    @Override
    public int getRank() {
        return 0;
    }

    @Override
    public Ally getAlly() {
        return BarbarianAlly.getSingleton();
    }

    @Override
    public boolean removeVillage(Village pVillage) {
        return true;
    }

    @Override
    public void addVillage(Village v) {
    }

    @Override
    public void addVillage(Village v, boolean pChecked) {
    }

    @Override
    public boolean ownsVillage(Village pVillage) {
        return pVillage != null && pVillage.getTribe().equals(Barbarians.getSingleton());
    }

    @Override
    public String getToolTipText() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String res = "<html><table style='border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;'>";
        res += "<tr><td><b>Name:</b> </td><td>" + getName() + "</td></tr>";
        res += "</table></html>";
        return res;
    }
}
