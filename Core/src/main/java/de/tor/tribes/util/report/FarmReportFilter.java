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
import de.tor.tribes.util.farm.FarmManager;

/**
 *
 * @author Torridity
 */
public class FarmReportFilter implements ReportRuleInterface {

    @Override
    public void setup(Object pFilterComponent) {
    }

    @Override
    public boolean isValid(FightReport c) {
        return c != null && FarmManager.getSingleton().getFarmInformation(c.getTargetVillage()) != null;
    }

    @Override
    public String getDescription() {
        return "Filterung von Farmberichten";
    }

    @Override
    public String getStringRepresentation() {
        return "Farmberichte";
    }
}
