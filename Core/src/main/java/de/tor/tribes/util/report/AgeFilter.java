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
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

/**
 *
 * @author Torridity
 */
public class AgeFilter implements ReportRuleInterface {

    private long maxAge = 0;

    @Override
    public void setup(Object pFilterComponent) throws ReportRuleConfigurationException {
        try {
            maxAge = (Long) pFilterComponent * DateUtils.MILLIS_PER_DAY * 365;
        } catch (Throwable t) {
            throw new ReportRuleConfigurationException(t);
        }
    }

    @Override
    public boolean isValid(FightReport c) {
        return (c.getTimestamp() < maxAge);
    }

    @Override
    public String getDescription() {
        return "Filterung nach Alter";
    }

    @Override
    public String getStringRepresentation() {
        return "Bericht älter als " + DurationFormatUtils.formatDuration(maxAge, "dd", false) + " Tag(e)";
    }
}
