/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchStatsFrame.java
 *
 * Created on Dec 11, 2009, 5:34:00 PM
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.TribeStatsElement;
import de.tor.tribes.types.TribeStatsElement.Stats;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.StatTextBuilder;
import de.tor.tribes.util.stat.StatManager;
import java.awt.Color;
import java.awt.Point;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 * @TODO (DIFF) Fixed stats time frame problems
 * @author Torridity
 */
public class DSWorkbenchStatsFrame extends AbstractDSWorkbenchFrame {

    private static DSWorkbenchStatsFrame SINGLETON = null;
    private JFreeChart chart = null;
    private XYPointerAnnotation startPointer = null;
    private XYPointerAnnotation endPointer = null;

    public static synchronized DSWorkbenchStatsFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchStatsFrame();
        }
        return SINGLETON;
    }

    DSWorkbenchStatsFrame() {
        initComponents();

        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("stats.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        jAllyList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object[] allySelection = jAllyList.getSelectedValues();
                jTribeList.clearSelection();
                List<Tribe> tribes = new LinkedList<Tribe>();
                for (Object o : allySelection) {
                    Tribe[] tribesForAlly = StatManager.getSingleton().getMonitoredTribes((Ally) o);
                    for (Tribe t : tribesForAlly) {
                        if (!tribes.contains(t)) {
                            tribes.add(t);
                        }
                    }
                    Collections.sort(tribes);
                    DefaultListModel model = new DefaultListModel();
                    for (Tribe t : tribes) {
                        model.addElement(t);
                    }
                    jTribeList.setModel(model);
                }
            }
        });

        jTribeList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                fireUpdateChartEvent(null);
            }
        });

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        jStartDate.setDate(c.getTime());
        jEndDate.setDate(c.getTime());
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.stats_view", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>

        pack();
    }

    public void resetView() {
        Ally[] allies = StatManager.getSingleton().getMonitoredAllies();
        Arrays.sort(allies);
        DefaultListModel model = new DefaultListModel();
        for (Ally a : allies) {
            model.addElement(a);
        }
        jAllyList.setModel(model);
        jTribeList.setModel(new DefaultListModel());
    }

    public void updateChart(List<TribeStatsElement> pElems) {
        chart = null;
        startPointer = null;
        endPointer = null;
        if (jShowPoints.isSelected()) {
            TimeSeriesCollection pointsDataset = new TimeSeriesCollection();
            for (TribeStatsElement elem : pElems) {
                TimeSeries pointSeries = new TimeSeries("Punkte (" + elem.getTribe().getName() + ")");
                // TimeSeries pointSeries2 = new TimeSeries("Punkte2 (" + elem.getTribe().getName() + ")");
                Long[] timestamps = elem.getTimestamps();
                Long[] points = elem.getPoints();
                for (int i = 0; i < timestamps.length; i++) {
                    pointSeries.add(new Second(new Date(timestamps[i])), points[i]);
                    //  pointSeries2.add(new Second(new Date(timestamps[i])), points[i] - 1000l);
                }
                pointsDataset.addSeries(pointSeries);
                // pointsDataset.addSeries(pointSeries2);
            }
            addDataset("Punkte", pointsDataset);
        }

        if (jShowRank.isSelected()) {
            TimeSeriesCollection rankDataset = new TimeSeriesCollection();
            for (TribeStatsElement elem : pElems) {
                TimeSeries rankSeries = new TimeSeries("Rang (" + elem.getTribe().getName() + ")");
                Long[] timestamps = elem.getTimestamps();
                Integer[] ranks = elem.getRanks();
                for (int i = 0; i < timestamps.length; i++) {
                    rankSeries.add(new Second(new Date(timestamps[i])), ranks[i]);
                }
                rankDataset.addSeries(rankSeries);
            }
            addDataset("Rang", rankDataset);
        }

        if (jShowVillages.isSelected()) {
            TimeSeriesCollection villageDataset = new TimeSeriesCollection();
            for (TribeStatsElement elem : pElems) {
                TimeSeries villageSeries = new TimeSeries("Dörfer (" + elem.getTribe().getName() + ")");
                Long[] timestamps = elem.getTimestamps();
                Short[] villages = elem.getVillages();
                for (int i = 0; i < timestamps.length; i++) {
                    villageSeries.add(new Second(new Date(timestamps[i])), villages[i]);
                }
                villageDataset.addSeries(villageSeries);
            }
            addDataset("Dörfer", villageDataset);
        }

        if (jShowKillsOff.isSelected()) {
            TimeSeriesCollection killsOffDataset = new TimeSeriesCollection();
            for (TribeStatsElement elem : pElems) {
                TimeSeries bashOffSeries = new TimeSeries("Kills (Off) (" + elem.getTribe().getName() + ")");
                Long[] timestamps = elem.getTimestamps();
                Long[] bashOff = elem.getBashOffPoints();
                for (int i = 0; i < timestamps.length; i++) {
                    bashOffSeries.add(new Second(new Date(timestamps[i])), bashOff[i]);
                }
                killsOffDataset.addSeries(bashOffSeries);
            }
            addDataset("Kills (Off)", killsOffDataset);
        }

        if (jShowRankOff.isSelected()) {
            TimeSeriesCollection rankOffDataset = new TimeSeriesCollection();
            for (TribeStatsElement elem : pElems) {
                TimeSeries rankOffSeries = new TimeSeries("Rang (Off) (" + elem.getTribe().getName() + ")");
                Long[] timestamps = elem.getTimestamps();
                Short[] rankOff = elem.getBashOffRank();
                for (int i = 0; i < timestamps.length; i++) {
                    rankOffSeries.add(new Second(new Date(timestamps[i])), rankOff[i]);
                }
                rankOffDataset.addSeries(rankOffSeries);
            }
            addDataset("Rang (Off)", rankOffDataset);
        }

        if (jShowKillsDef.isSelected()) {
            TimeSeriesCollection killsDefDataset = new TimeSeriesCollection();
            for (TribeStatsElement elem : pElems) {
                TimeSeries bashDefSeries = new TimeSeries("Kills (Def) (" + elem.getTribe().getName() + ")");
                Long[] timestamps = elem.getTimestamps();
                Long[] bashDef = elem.getBashDefPoints();
                for (int i = 0; i < timestamps.length; i++) {
                    bashDefSeries.add(new Second(new Date(timestamps[i])), bashDef[i]);
                }
                killsDefDataset.addSeries(bashDefSeries);
            }
            addDataset("Kills (Def)", killsDefDataset);
        }

        if (jShowRankDef.isSelected()) {
            TimeSeriesCollection rankDefDataset = new TimeSeriesCollection();
            for (TribeStatsElement elem : pElems) {
                TimeSeries rankDefSeries = new TimeSeries("Rang (Def) (" + elem.getTribe().getName() + ")");
                Long[] timestamps = elem.getTimestamps();
                Short[] rankDef = elem.getBashDefRank();
                for (int i = 0; i < timestamps.length; i++) {
                    rankDefSeries.add(new Second(new Date(timestamps[i])), rankDef[i]);
                }
                rankDefDataset.addSeries(rankDefSeries);
            }
            addDataset("Rang (Def)", rankDefDataset);
        }

        jChartPanel.removeAll();
        ChartPanel p = new ChartPanel(chart);
        p.setDisplayToolTips(true);
        p.setMouseWheelEnabled(true);
        jChartPanel.add(p);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                jChartPanel.updateUI();
            }
        });

    }

    private void setupChart(String pInitialId, XYDataset pInitialDataset) {
        chart = ChartFactory.createTimeSeriesChart(
                "Spielerstatistiken", // title
                "Zeiten", // x-axis label
                pInitialId, // y-axis label
                pInitialDataset, // data
                jShowLegend.isSelected(), // create legend?
                true, // generate tooltips?
                false // generate URLs?
                );

        chart.setBackgroundPaint(Constants.DS_BACK);
        XYPlot plot = (XYPlot) chart.getPlot();
        setupPlotDrawing(plot);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, jShowLines.isSelected());
        renderer.setSeriesShapesVisible(0, jShowDataPoints.isSelected());

        plot.setRenderer(0, renderer);
        renderer.setBaseItemLabelsVisible(jShowItemValues.isSelected());
        renderer.setBaseItemLabelGenerator(new org.jfree.chart.labels.StandardXYItemLabelGenerator());
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"), NumberFormat.getInstance()));
        plot.getRangeAxis().setAxisLinePaint(plot.getLegendItems().get(plot.getDatasetCount() - 1).getLinePaint());
        plot.getRangeAxis().setLabelPaint(plot.getLegendItems().get(plot.getDatasetCount() - 1).getLinePaint());
        plot.getRangeAxis().setTickLabelPaint(plot.getLegendItems().get(plot.getDatasetCount() - 1).getLinePaint());
        plot.getRangeAxis().setTickMarkPaint(plot.getLegendItems().get(plot.getDatasetCount() - 1).getLinePaint());
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        try {
            ((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(nf);
        } catch (Exception e) {
        }
    }

    private void addDataset(String pId, XYDataset pDataset) {
        if (chart == null) {
            setupChart(pId, pDataset);
        } else {
            XYPlot plot = (XYPlot) chart.getPlot();
            plot.setDataset(plot.getDatasetCount(), pDataset);
            NumberAxis axis = new NumberAxis(pId);
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            axis.setNumberFormatOverride(nf);
            plot.setRangeAxis(plot.getDatasetCount() - 1, axis);
            plot.setRangeAxisLocation(plot.getDatasetCount() - 1, AxisLocation.TOP_OR_LEFT);
            plot.mapDatasetToRangeAxis(plot.getDatasetCount() - 1, plot.getDatasetCount() - 1);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesLinesVisible(0, jShowLines.isSelected());
            renderer.setSeriesShapesVisible(0, jShowDataPoints.isSelected());
            plot.setRenderer(plot.getDatasetCount() - 1, renderer);
            renderer.setBaseItemLabelsVisible(jShowItemValues.isSelected());
            renderer.setBaseItemLabelGenerator(new org.jfree.chart.labels.StandardXYItemLabelGenerator());
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"), NumberFormat.getInstance()));
            axis.setAxisLinePaint(plot.getLegendItems().get(plot.getDatasetCount() - 1).getLinePaint());
            axis.setLabelPaint(plot.getLegendItems().get(plot.getDatasetCount() - 1).getLinePaint());
            axis.setTickLabelPaint(plot.getLegendItems().get(plot.getDatasetCount() - 1).getLinePaint());
            axis.setTickMarkPaint(plot.getLegendItems().get(plot.getDatasetCount() - 1).getLinePaint());
        }
    }

    private void setupPlotDrawing(XYPlot pPlot) {
        pPlot.setBackgroundPaint(Constants.DS_BACK_LIGHT);
        pPlot.setDomainGridlinePaint(Color.DARK_GRAY);
        pPlot.setRangeGridlinePaint(Color.DARK_GRAY);
        pPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        pPlot.setDomainCrosshairVisible(true);
        pPlot.setRangeCrosshairVisible(true);

        DateAxis axis = (DateAxis) pPlot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jStatsCreateFrame = new javax.swing.JFrame();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jStatsTribeList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jWeeklyStats = new javax.swing.JButton();
        jMonthlyStats = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jPointsArea = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        jBashOffArea = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jBashDefArea = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jWinnerArea = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jLoserArea = new javax.swing.JTextArea();
        jUsedTribes = new javax.swing.JComboBox();
        jPanel10 = new javax.swing.JPanel();
        jUseBBCodesBox = new javax.swing.JCheckBox();
        jButton8 = new javax.swing.JButton();
        jStartDate = new de.tor.tribes.ui.components.DateTimeField();
        jEndDate = new de.tor.tribes.ui.components.DateTimeField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAllyList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTribeList = new javax.swing.JList();
        jChartPanel = new javax.swing.JPanel();
        jTaskPane1 = new com.l2fprod.common.swing.JTaskPane();
        jTaskPaneGroup1 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jExportToClipboardButton = new javax.swing.JButton();
        jTaskPaneGroup2 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jShowPoints = new javax.swing.JCheckBox();
        jShowRank = new javax.swing.JCheckBox();
        jShowVillages = new javax.swing.JCheckBox();
        jShowKillsOff = new javax.swing.JCheckBox();
        jShowRankOff = new javax.swing.JCheckBox();
        jShowKillsDef = new javax.swing.JCheckBox();
        jShowRankDef = new javax.swing.JCheckBox();
        jTaskPaneGroup3 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jShowItemValues = new javax.swing.JCheckBox();
        jShowLegend = new javax.swing.JCheckBox();
        jShowLines = new javax.swing.JCheckBox();
        jShowDataPoints = new javax.swing.JCheckBox();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        jStatsCreateFrame.setTitle("Statistiken erstellen");

        jPanel2.setBackground(new java.awt.Color(239, 235, 223));

        jScrollPane3.setPreferredSize(new java.awt.Dimension(100, 130));

        jScrollPane3.setViewportView(jStatsTribeList);

        jLabel3.setText("Berücksichtigte Spieler");

        jLabel4.setText("Zeitraum (Start)");

        jLabel5.setText("Zeitraum (Ende)");

        jWeeklyStats.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/calendar_7.png"))); // NOI18N
        jWeeklyStats.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jWeeklyStats.setMaximumSize(new java.awt.Dimension(40, 25));
        jWeeklyStats.setMinimumSize(new java.awt.Dimension(40, 25));
        jWeeklyStats.setPreferredSize(new java.awt.Dimension(40, 25));
        jWeeklyStats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeStatTimeEvent(evt);
            }
        });

        jMonthlyStats.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/calendar_31.png"))); // NOI18N
        jMonthlyStats.setMaximumSize(new java.awt.Dimension(40, 25));
        jMonthlyStats.setMinimumSize(new java.awt.Dimension(40, 25));
        jMonthlyStats.setPreferredSize(new java.awt.Dimension(40, 25));
        jMonthlyStats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeStatTimeEvent(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Auswertung"));

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPointsArea.setColumns(20);
        jPointsArea.setRows(5);
        jScrollPane4.setViewportView(jPointsArea);

        jPanel4.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Punkte", new javax.swing.ImageIcon(getClass().getResource("/res/goblet_gold.png")), jPanel4); // NOI18N

        jPanel5.setLayout(new java.awt.BorderLayout());

        jBashOffArea.setColumns(20);
        jBashOffArea.setRows(5);
        jScrollPane9.setViewportView(jBashOffArea);

        jPanel5.add(jScrollPane9, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Bash (Off)", new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jPanel5); // NOI18N

        jPanel6.setLayout(new java.awt.BorderLayout());

        jBashDefArea.setColumns(20);
        jBashDefArea.setRows(5);
        jScrollPane8.setViewportView(jBashDefArea);

        jPanel6.add(jScrollPane8, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Bash (Deff)", new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jPanel6); // NOI18N

        jPanel8.setLayout(new java.awt.BorderLayout());

        jWinnerArea.setColumns(20);
        jWinnerArea.setRows(5);
        jScrollPane6.setViewportView(jWinnerArea);

        jPanel8.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Gewinner", new javax.swing.ImageIcon(getClass().getResource("/res/up_plus.png")), jPanel8); // NOI18N

        jPanel9.setLayout(new java.awt.BorderLayout());

        jLoserArea.setColumns(20);
        jLoserArea.setRows(5);
        jScrollPane5.setViewportView(jLoserArea);

        jPanel9.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Verlierer", new javax.swing.ImageIcon(getClass().getResource("/res/down_minus.png")), jPanel9); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                .addContainerGap())
        );

        jUsedTribes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle", "Markierte", "Top 10" }));

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Sonstige Einstellungen"));
        jPanel10.setOpaque(false);

        jUseBBCodesBox.setText("BB-Codes verwenden");
        jUseBBCodesBox.setOpaque(false);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jUseBBCodesBox)
                .addContainerGap(318, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jUseBBCodesBox)
                .addGap(26, 26, 26))
        );

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/select.png"))); // NOI18N
        jButton8.setText("Erstellen");
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireGenerateStatsEvent(evt);
            }
        });

        jStartDate.setTimeEnabled(false);

        jEndDate.setTimeEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jUsedTribes, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jWeeklyStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMonthlyStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
                    .addComponent(jButton8, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jUsedTribes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jEndDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jMonthlyStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jWeeklyStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jStatsCreateFrameLayout = new javax.swing.GroupLayout(jStatsCreateFrame.getContentPane());
        jStatsCreateFrame.getContentPane().setLayout(jStatsCreateFrameLayout);
        jStatsCreateFrameLayout.setHorizontalGroup(
            jStatsCreateFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStatsCreateFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jStatsCreateFrameLayout.setVerticalGroup(
            jStatsCreateFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStatsCreateFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setTitle("Statistiken");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jLabel1.setText("Überwachte Stämme");
        jLabel1.setMaximumSize(new java.awt.Dimension(100, 14));
        jLabel1.setMinimumSize(new java.awt.Dimension(100, 14));
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 14));

        jLabel2.setText("Überwachte Spieler");
        jLabel2.setMaximumSize(new java.awt.Dimension(100, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(100, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(100, 14));

        jScrollPane1.setMinimumSize(new java.awt.Dimension(258, 130));

        jScrollPane1.setViewportView(jAllyList);

        jScrollPane2.setViewportView(jTribeList);

        jChartPanel.setBackground(new java.awt.Color(239, 235, 223));
        jChartPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jChartPanel.setLayout(new java.awt.BorderLayout());

        jTaskPane1.setOpaque(false);
        com.l2fprod.common.swing.PercentLayout percentLayout1 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout1.setGap(14);
        percentLayout1.setOrientation(1);
        jTaskPane1.setLayout(percentLayout1);

        jTaskPaneGroup1.setTitle("Verwaltung");
        com.l2fprod.common.swing.PercentLayout percentLayout2 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout2.setGap(2);
        percentLayout2.setOrientation(1);
        jTaskPaneGroup1.getContentPane().setLayout(percentLayout2);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton1.setToolTipText("Statistiken für markierte Spieler löschen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveMonitoredElementEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton1);

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/beginning.png"))); // NOI18N
        jButton2.setToolTipText("Markierten Datenpunkt als Start des ausgewählten Bereichs verwenden");
        jButton2.setMaximumSize(new java.awt.Dimension(49, 33));
        jButton2.setMinimumSize(new java.awt.Dimension(49, 33));
        jButton2.setPreferredSize(new java.awt.Dimension(49, 33));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveDateBeforeEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton2);

        jButton4.setBackground(new java.awt.Color(239, 235, 223));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/end.png"))); // NOI18N
        jButton4.setToolTipText("Markierten Datenpunkt als Ende des ausgewählten Bereichs verwenden");
        jButton4.setMaximumSize(new java.awt.Dimension(49, 33));
        jButton4.setMinimumSize(new java.awt.Dimension(49, 33));
        jButton4.setPreferredSize(new java.awt.Dimension(49, 33));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveDataAfterEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton4);

        jButton5.setBackground(new java.awt.Color(239, 235, 223));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/delete_region.png"))); // NOI18N
        jButton5.setToolTipText("Ausgewählten Bereich löschen");
        jButton5.setMaximumSize(new java.awt.Dimension(49, 33));
        jButton5.setMinimumSize(new java.awt.Dimension(49, 33));
        jButton5.setPreferredSize(new java.awt.Dimension(49, 33));
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveRegionEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton5);

        jExportToClipboardButton.setBackground(new java.awt.Color(239, 235, 223));
        jExportToClipboardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/medal.png"))); // NOI18N
        jExportToClipboardButton.setToolTipText("Statistiken erzeugen");
        jExportToClipboardButton.setMaximumSize(new java.awt.Dimension(49, 33));
        jExportToClipboardButton.setMinimumSize(new java.awt.Dimension(49, 33));
        jExportToClipboardButton.setPreferredSize(new java.awt.Dimension(49, 33));
        jExportToClipboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCreateStatisticsEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jExportToClipboardButton);

        jTaskPane1.add(jTaskPaneGroup1);

        jTaskPaneGroup2.setTitle("Angezeigte Daten");
        jTaskPaneGroup2.setToolTipText("");
        com.l2fprod.common.swing.PercentLayout percentLayout3 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout3.setGap(2);
        percentLayout3.setOrientation(1);
        jTaskPaneGroup2.getContentPane().setLayout(percentLayout3);

        jShowPoints.setSelected(true);
        jShowPoints.setText("Punkte anzeigen");
        jShowPoints.setOpaque(false);
        jShowPoints.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jShowPoints);

        jShowRank.setText("Rang anzeigen");
        jShowRank.setOpaque(false);
        jShowRank.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jShowRank);

        jShowVillages.setText("Dörfer anzeigen");
        jShowVillages.setOpaque(false);
        jShowVillages.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jShowVillages);

        jShowKillsOff.setText("Kills (Off) anzeigen");
        jShowKillsOff.setOpaque(false);
        jShowKillsOff.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jShowKillsOff);

        jShowRankOff.setText("Rang (Off) anzeigen");
        jShowRankOff.setOpaque(false);
        jShowRankOff.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jShowRankOff);

        jShowKillsDef.setText("Kills (Deff) anzeigen");
        jShowKillsDef.setOpaque(false);
        jShowKillsDef.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jShowKillsDef);

        jShowRankDef.setText("Rang (Deff) anzeigen");
        jShowRankDef.setOpaque(false);
        jShowRankDef.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jShowRankDef);

        jTaskPane1.add(jTaskPaneGroup2);

        jTaskPaneGroup3.setExpanded(false);
        jTaskPaneGroup3.setTitle("Diagrammoptionen");
        com.l2fprod.common.swing.PercentLayout percentLayout4 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout4.setGap(2);
        percentLayout4.setOrientation(1);
        jTaskPaneGroup3.getContentPane().setLayout(percentLayout4);

        jShowItemValues.setText("Werte anzeigen");
        jShowItemValues.setToolTipText("Zeigt die Werte der Datenpunkte im Diagramm an");
        jShowItemValues.setOpaque(false);
        jShowItemValues.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jShowItemValues);

        jShowLegend.setSelected(true);
        jShowLegend.setText("Legende anzeigen");
        jShowLegend.setOpaque(false);
        jShowLegend.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jShowLegend);

        jShowLines.setSelected(true);
        jShowLines.setText("Linien anzeigen");
        jShowLines.setOpaque(false);
        jShowLines.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jShowLines);

        jShowDataPoints.setSelected(true);
        jShowDataPoints.setText("Datenpunkte anzeigen");
        jShowDataPoints.setOpaque(false);
        jShowDataPoints.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jShowDataPoints);

        jTaskPane1.add(jTaskPaneGroup3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jChartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTaskPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTaskPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane2))
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jChartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jAlwaysOnTopBox)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAlwaysOnTopBox)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireRemoveMonitoredElementEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveMonitoredElementEvent
        //remove tribe element(s)
        Object[] tribesToRemove = jTribeList.getSelectedValues();
        if (tribesToRemove == null || tribesToRemove.length == 0) {
            JOptionPaneHelper.showInformationBox(this, "Kein Spieler ausgewählt", "Fehler");
            return;
        }
        String message = "";
        if (tribesToRemove.length == 1) {
            message = "Statistiken für markierten Spieler löschen?";
        } else {
            message = "Statistiken für markierte Spieler löschen?";
        }
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            for (Object o : tribesToRemove) {
                StatManager.getSingleton().removeTribeData((Tribe) o);
            }
        } else {
            return;
        }
        resetView();
    }//GEN-LAST:event_fireRemoveMonitoredElementEvent

    private void fireUpdateChartEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireUpdateChartEvent
        Object tribeSelection = jTribeList.getSelectedValue();
        if (tribeSelection == null) {
            jChartPanel.removeAll();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    jChartPanel.updateUI();
                }
            });

            return;
        }
        List<TribeStatsElement> elems = new LinkedList<TribeStatsElement>();

        Tribe t = (Tribe) tribeSelection;
        TribeStatsElement elem = StatManager.getSingleton().getStatsForTribe(t);
        if (elem != null) {
            elems.add(elem);
        }
        updateChart(elems);
    }//GEN-LAST:event_fireUpdateChartEvent

    private void fireRemoveDateBeforeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveDateBeforeEvent
        XYPlot plot = ((XYPlot) chart.getPlot());
        double x = plot.getDomainCrosshairValue();
        double y = plot.getRangeCrosshairValue();
        if (startPointer != null) {
            plot.removeAnnotation(startPointer);
        }

        if (startPointer != null && startPointer.getX() == x) {
            plot.removeAnnotation(startPointer);
            startPointer = null;
        } else {
            if (endPointer != null) {
                if (endPointer.getX() < x) {
                    //flip start and end
                    plot.removeAnnotation(endPointer);
                    startPointer = new XYPointerAnnotation("Start", endPointer.getX(), endPointer.getY(), 3 * Math.PI / 4.0);
                    startPointer.setPaint(Color.green);
                    plot.addAnnotation(startPointer);
                    endPointer = new XYPointerAnnotation("Ende", x, y, 3 * Math.PI / 4.0);
                    endPointer.setPaint(Color.red);
                    plot.addAnnotation(endPointer);
                } else {
                    startPointer = new XYPointerAnnotation("Start", x, y, 3 * Math.PI / 4.0);
                    startPointer.setPaint(Color.green);
                    plot.addAnnotation(startPointer);
                }
            } else {

                startPointer = new XYPointerAnnotation("Start", x, y, 3 * Math.PI / 4.0);
                startPointer.setPaint(Color.green);
                plot.addAnnotation(startPointer);
            }
        }

        jChartPanel.repaint();
    }//GEN-LAST:event_fireRemoveDateBeforeEvent

    private void fireRemoveDataAfterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveDataAfterEvent
        XYPlot plot = ((XYPlot) chart.getPlot());
        double x = plot.getDomainCrosshairValue();
        double y = plot.getRangeCrosshairValue();
        if (endPointer != null) {
            plot.removeAnnotation(endPointer);
        }

        if (endPointer != null && endPointer.getX() == x) {
            plot.removeAnnotation(endPointer);
            endPointer = null;
        } else {
            if (startPointer != null) {
                if (startPointer.getX() > x) {
                    //flip start and end
                    plot.removeAnnotation(startPointer);
                    endPointer = new XYPointerAnnotation("Ende", startPointer.getX(), startPointer.getY(), 3 * Math.PI / 4.0);
                    endPointer.setPaint(Color.red);
                    plot.addAnnotation(endPointer);
                    startPointer = new XYPointerAnnotation("Start", x, y, 3 * Math.PI / 4.0);
                    startPointer.setPaint(Color.green);
                    plot.addAnnotation(startPointer);
                } else {
                    endPointer = new XYPointerAnnotation("Ende", x, y, 3 * Math.PI / 4.0);
                    endPointer.setPaint(Color.red);
                    plot.addAnnotation(endPointer);
                }
            } else {
                endPointer = new XYPointerAnnotation("Ende", x, y, 3 * Math.PI / 4.0);
                endPointer.setPaint(Color.red);
                plot.addAnnotation(endPointer);
            }
        }

        jChartPanel.repaint();
    }//GEN-LAST:event_fireRemoveDataAfterEvent

    private void fireRemoveRegionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveRegionEvent
        if (startPointer == null && endPointer == null) {
            JOptionPaneHelper.showInformationBox(this, "Es wurde kein Bereich ausgewählt.", "Information");
            return;
        }
        Object tribeSelection = jTribeList.getSelectedValue();
        if (tribeSelection == null) {
            return;
        }

        if (startPointer == null) {
            //remove before end
            long v = (long) endPointer.getX();
            String date = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(v));
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle Werte vor dem " + date + " löschen?", "Werte löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                StatManager.getSingleton().removeDataBefore((Tribe) tribeSelection, new Date(v).getTime());
                fireUpdateChartEvent(null);
            }
        } else if (endPointer == null) {
            //remove after start
            long v = (long) startPointer.getX();
            String date = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(v));
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle Werte nach dem " + date + " löschen?", "Werte löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                StatManager.getSingleton().removeDataAfter((Tribe) tribeSelection, new Date(v).getTime());
                fireUpdateChartEvent(null);
            }
        } else {
            //remove date between
            long vstart = (long) startPointer.getX();
            long vend = (long) endPointer.getX();

            String startDate = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(vstart));
            String endDate = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(vend));

            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle Werte zwischen dem " + startDate + " und dem " + endDate + " löschen?", "Werte löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                StatManager.getSingleton().removeDataBetween((Tribe) tribeSelection, new Date(vstart).getTime(), new Date(vend).getTime());
                fireUpdateChartEvent(null);
            }
        }

        fireUpdateChartEvent(null);
    }//GEN-LAST:event_fireRemoveRegionEvent

    private void fireCreateStatisticsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateStatisticsEvent
        DefaultListModel tribeModel = (DefaultListModel) jTribeList.getModel();
        if (tribeModel.isEmpty()) {
            JOptionPaneHelper.showInformationBox(this, "Bitte zuerst einen Stamm auswählen.", "Information");
            return;
        }
        DefaultListModel statsTribeModel = new DefaultListModel();
        for (int i = 0; i < tribeModel.getSize(); i++) {
            statsTribeModel.addElement(tribeModel.get(i));
        }
        jStatsTribeList.setModel(statsTribeModel);
        jStatsCreateFrame.pack();
        jStatsCreateFrame.setVisible(true);
    }//GEN-LAST:event_fireCreateStatisticsEvent

    private void fireGenerateStatsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireGenerateStatsEvent
        long start = jStartDate.getSelectedDate().getTime();
        long end = jEndDate.getSelectedDate().getTime();
        List<Tribe> usedTribes = new LinkedList<Tribe>();
        if (jUsedTribes.getSelectedIndex() == 0 || jUsedTribes.getSelectedIndex() == 2) {
            //use all (if index == 2 select top 10 later
            DefaultListModel tribeModel = (DefaultListModel) jStatsTribeList.getModel();
            for (int i = 0; i < tribeModel.getSize(); i++) {
                usedTribes.add((Tribe) tribeModel.getElementAt(i));
            }
        } else if (jUsedTribes.getSelectedIndex() == 1) {
            //use selected
            Object[] tribes = jStatsTribeList.getSelectedValues();
            if (tribes == null || tribes.length == 0) {
                JOptionPaneHelper.showInformationBox(jStatsCreateFrame, "Keine Spieler ausgewählt.", "Information");
                return;
            } else {
                for (Object o : tribes) {
                    usedTribes.add((Tribe) o);
                }
            }
        }

        List<Stats> stats = new LinkedList<Stats>();
        for (Tribe t : usedTribes) {
            TribeStatsElement elem = StatManager.getSingleton().getStatsForTribe(t);
            Stats elemStat = elem.generateStats(start, end);
            stats.add(elemStat);
        }

        jPointsArea.setText(StatTextBuilder.buildPointsList(stats, jUseBBCodesBox.isSelected(), true, (jUsedTribes.getSelectedIndex() == 2)));
        jBashOffArea.setText(StatTextBuilder.buildBashOffList(stats, jUseBBCodesBox.isSelected(), true, (jUsedTribes.getSelectedIndex() == 2)));
        jBashDefArea.setText(StatTextBuilder.buildBashDefList(stats, jUseBBCodesBox.isSelected(), true, (jUsedTribes.getSelectedIndex() == 2)));
        jWinnerArea.setText(StatTextBuilder.buildWinnerStats(stats, jUseBBCodesBox.isSelected(), true, (jUsedTribes.getSelectedIndex() == 2)));
        jLoserArea.setText(StatTextBuilder.buildLoserStats(stats, jUseBBCodesBox.isSelected(), true, (jUsedTribes.getSelectedIndex() == 2)));
    }//GEN-LAST:event_fireGenerateStatsEvent

    private void fireChangeStatTimeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeStatTimeEvent
        if (evt.getSource() == jWeeklyStats) {
            //remove one week from end date
            long oneWeek = 1000l * 60l * 60l * 24l * 7l;
            Date end = jEndDate.getSelectedDate();
            long start = end.getTime() - oneWeek;
            jStartDate.setDate(new Date(start));
        } else {
            //remove one month from end date
            long oneMonth = 1000l * 60l * 60l * 24l * 31l;
            Date end = jEndDate.getSelectedDate();
            long start = end.getTime() - oneMonth;
            jEndDate.setDate(new Date(start));
        }

    }//GEN-LAST:event_fireChangeStatTimeEvent

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jAllyList;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JTextArea jBashDefArea;
    private javax.swing.JTextArea jBashOffArea;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton8;
    private javax.swing.JPanel jChartPanel;
    private de.tor.tribes.ui.components.DateTimeField jEndDate;
    private javax.swing.JButton jExportToClipboardButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextArea jLoserArea;
    private javax.swing.JButton jMonthlyStats;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTextArea jPointsArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JCheckBox jShowDataPoints;
    private javax.swing.JCheckBox jShowItemValues;
    private javax.swing.JCheckBox jShowKillsDef;
    private javax.swing.JCheckBox jShowKillsOff;
    private javax.swing.JCheckBox jShowLegend;
    private javax.swing.JCheckBox jShowLines;
    private javax.swing.JCheckBox jShowPoints;
    private javax.swing.JCheckBox jShowRank;
    private javax.swing.JCheckBox jShowRankDef;
    private javax.swing.JCheckBox jShowRankOff;
    private javax.swing.JCheckBox jShowVillages;
    private de.tor.tribes.ui.components.DateTimeField jStartDate;
    private javax.swing.JFrame jStatsCreateFrame;
    private javax.swing.JList jStatsTribeList;
    private javax.swing.JTabbedPane jTabbedPane1;
    private com.l2fprod.common.swing.JTaskPane jTaskPane1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup2;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup3;
    private javax.swing.JList jTribeList;
    private javax.swing.JCheckBox jUseBBCodesBox;
    private javax.swing.JComboBox jUsedTribes;
    private javax.swing.JButton jWeeklyStats;
    private javax.swing.JTextArea jWinnerArea;
    // End of variables declaration//GEN-END:variables
}
