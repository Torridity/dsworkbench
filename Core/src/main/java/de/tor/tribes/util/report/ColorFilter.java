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
package de.tor.tribes.util.report;

import de.tor.tribes.types.FightReport;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class ColorFilter implements ReportRuleInterface {

    private Integer color = 31;
    public static final int GREY = 1;
    public static final int BLUE = 2;
    public static final int RED = 4;
    public static final int YELLOW = 8;
    public static final int GREEN = 16;

    @Override
    public void setup(Object pFilterComponent) throws ReportRuleConfigurationException {
        try {
            color = (Integer) pFilterComponent;
        } catch (Throwable t) {
            throw new ReportRuleConfigurationException(t);
        }
    }

    @Override
    public boolean isValid(FightReport c) {
        int value = 0;
        if (c.areAttackersHidden()) {
            value = GREY;
        } else if (c.isSpyReport()) {
            value = BLUE;
        } else if (c.wasLostEverything()) {
            value = RED;
        } else if (c.wasLostNothing()) {
            value = GREEN;
        } else {
            value = YELLOW;
        }
        return ((color & value) > 0);
    }

    public static void main(String[] args) throws ReportRuleConfigurationException {
        ColorFilter f = new ColorFilter();
        f.setup(ColorFilter.RED + ColorFilter.BLUE + ColorFilter.GREEN);

    }

    @Override
    public String getDescription() {
        return "Filterung nach der Farbe eines Berichts";
    }

    @Override
    public String getStringRepresentation() {
        StringBuilder result = new StringBuilder();
        List<String> validFor = new LinkedList<>();
        if ((color & GREY) > 0) {
            validFor.add("grau");
        }
        if ((color & BLUE) > 0) {
            validFor.add("blau");
        }

        if ((color & GREEN) > 0) {
            validFor.add("grün");
        }

        if ((color & YELLOW) > 0) {
            validFor.add("gelb");
        }

        if ((color & RED) > 0) {
            validFor.add("rot");
        }

        result.append("Farben ").append(validFor.toString());
        return result.toString();
    }
}
