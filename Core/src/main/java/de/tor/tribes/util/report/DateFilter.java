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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DateFilter implements ReportRuleInterface {

    private long start = 0;
    private long end = 0;

    @Override
    public void setup(Object pFilterComponent) throws ReportRuleConfigurationException {
        try {
            List<Long> dates = (List<Long>) pFilterComponent;
            start = dates.get(0);
            end = dates.get(1);
            if (start > end) {
                long tmp = end;
                end = start;
                start = tmp;
            }
        } catch (Throwable t) {
            throw new ReportRuleConfigurationException(t);
        }
    }

    @Override
    public boolean isValid(FightReport c) {
        return (c.getTimestamp() >= start && c.getTimestamp() <= end);
    }

    @Override
    public String getDescription() {
        return "Filterung nach Datum";
    }

    @Override
    public String getStringRepresentation() {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy");
        return "Gesendet zwischen " + df.format(new Date(start)) + " und " + df.format(new Date(end));
    }

    @Override
    public String toXml() {
        //TODO create function
        return "";
    }
}
