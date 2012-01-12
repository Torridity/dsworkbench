/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.types.FightReport;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

/**
 *
 * @author Torridity
 */
public class AgeFilter implements ReportFilterInterface {

    private long maxAge = 0;

    @Override
    public void setup(Object pFilterComponent) {
        maxAge = (Long) pFilterComponent * DateUtils.MILLIS_PER_DAY * 365;
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
