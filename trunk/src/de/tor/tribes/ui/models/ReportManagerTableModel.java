/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ReportSet;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchReportFrame;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.report.ReportManagerListener;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class ReportManagerTableModel extends AbstractDSWorkbenchTableModel {

    private final String PROPERTY_BASE_ID = "report.table.model";
    protected static Class[] types;
    protected static String[] colNames;
    protected static List<String> internalNames;
    protected static boolean[] editableColumns = null;
    private static Logger logger = Logger.getLogger("ReportTable");
    private String sReportSet = ReportManager.DEFAULT_SET;
    private static ReportManagerTableModel SINGLETON = null;

    static {
        internalNames = Arrays.asList(new String[]{"Status", "Gesendet", "Angreifer", "Herkunft", "Verteidiger", "Ziel", "Typ", "Sonstiges"});
        colNames = new String[]{"Status", "Gesendet", "Angreifer", "Herkunft", "Verteidiger", "Ziel", "Typ", "Sonstiges"};
        types = new Class[]{FightReport.class, Date.class, Tribe.class, Village.class, Tribe.class, Village.class, Integer.class, Byte.class};
        editableColumns = new boolean[]{false, false, false, false, false, false, false, false};
    }

    public static synchronized ReportManagerTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ReportManagerTableModel();
        }
        return SINGLETON;
    }

    ReportManagerTableModel() {
        super();
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

    public void removeRow(int pRow) {
        ReportManager.getSingleton().removeReport(sReportSet, pRow);
    }

    @Override
    public int getRowCount() {
        return ReportManager.getSingleton().getReportSet(sReportSet).getReports().length;
    }


    public FightReport getReportAtRow(int pRow){
       ReportSet set = ReportManager.getSingleton().getReportSet(sReportSet);
    //@TODO Include filter
       return set.getReports()[pRow];
    }

    /* @Override
    public int getColumnCount() {
    return colNames.length;
    }*/
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReportSet set = ReportManager.getSingleton().getReportSet(sReportSet);
        columnIndex = getRealColumnId(columnIndex);
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
            case 5:
                return set.getReports()[rowIndex].getTargetVillage();
            case 6:
                return set.getReports()[rowIndex].guessType();
            default:
                return set.getReports()[rowIndex].getVillageEffects();
        }
    }

    @Override
    public String getPropertyBaseID() {
        return PROPERTY_BASE_ID;
    }

    @Override
    public Class[] getColumnClasses() {
        return types;
    }

    @Override
    public String[] getColumnNames() {
        return colNames;
    }

    @Override
    public List<String> getInternalColumnNames() {
        return internalNames;
    }

    @Override
    public boolean[] getEditableColumns() {
        return editableColumns;
    }

    @Override
    public void doNotifyOnColumnChange() {
        DSWorkbenchReportFrame.getSingleton().fireReportsChangedEvent("");
    }
}
