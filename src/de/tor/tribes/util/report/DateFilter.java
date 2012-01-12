/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class DateFilter implements ReportFilterInterface {

    private long start = 0;
    private long end = 0;

    @Override
    public void setup(Object pFilterComponent) {
        List<Long> dates = (List<Long>) pFilterComponent;
        start = dates.get(0);
        end = dates.get(1);
        if (start > end) {
            long tmp = end;
            end = start;
            start = tmp;
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
}
