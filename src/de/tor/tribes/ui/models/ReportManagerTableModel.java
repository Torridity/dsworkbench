/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ReportSet;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.report.ReportManagerListener;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class ReportManagerTableModel extends AbstractTableModel {

    private static Logger logger = Logger.getLogger("ReportTable");
    Class[] types = new Class[]{
        FightReport.class, Date.class, Tribe.class, Village.class, Tribe.class, Village.class
    };
    String[] colNames = new String[]{
        "", "Gesendet", "Angreifer", "Herkunft", "Verteidiger", "Ziel"
    };
    private String sReportSet = ReportManager.DEFAULT_SET;
    private static ReportManagerTableModel SINGLETON = null;

    public static synchronized ReportManagerTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ReportManagerTableModel();
        }
        return SINGLETON;
    }

    ReportManagerTableModel() {

        ReportManager.getSingleton().addReportManagerListener(new ReportManagerListener() {

            @Override
            public void fireReportsChangedEvent(String pPlan) {
                fireTableDataChanged();
            }
        });
    }

    public synchronized void setActiveReportSet(String pSet) {
        logger.debug("Setting active report set to '" + pSet + "'");
        sReportSet = pSet;
    }

    public synchronized String getActiveReportSet() {
        return sReportSet;
    }

    @Override
    public String getColumnName(int col) {
        return colNames[col];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    public void removeRow(int pRow) {
        ReportManager.getSingleton().removeReport(sReportSet, pRow);
    }

    @Override
    public int getRowCount() {
        return ReportManager.getSingleton().getReportSet(sReportSet).getReports().length;
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReportSet set = ReportManager.getSingleton().getReportSet(sReportSet);
        switch (columnIndex) {
            case 0:
                return set.getReports()[rowIndex];
            case 1:
                return new Date(set.getReports()[rowIndex].getTimestamp());
            case 2:
                return set.getReports()[rowIndex].getAttacker();
            case 3:
                return set.getReports()[rowIndex].getSourceVillage();
            case 4:
                return set.getReports()[rowIndex].getDefender();
            default:
                return set.getReports()[rowIndex].getTargetVillage();
        }
    }
}
