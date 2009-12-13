/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchStatsFrame.java
 *
 * Created on Dec 11, 2009, 5:34:00 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.TribeStatsElement;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.stat.StatManager;
import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
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
import org.jfree.ui.TextAnchor;

/**
 *
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
    }

    public void setup() {
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


        jChartPanel.updateUI();
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
    }

    private void addDataset(String pId, XYDataset pDataset) {
        if (chart == null) {
            setupChart(pId, pDataset);
        } else {
            XYPlot plot = (XYPlot) chart.getPlot();

            plot.setDataset(plot.getDatasetCount(), pDataset);
            NumberAxis axis = new NumberAxis(pId);
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
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
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

        jScrollPane1.setViewportView(jAllyList);

        jScrollPane2.setViewportView(jTribeList);

        jChartPanel.setBackground(new java.awt.Color(239, 235, 223));
        jChartPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jChartPanel.setLayout(new java.awt.BorderLayout());

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

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/replace2.png"))); // NOI18N
        jButton3.setToolTipText("Aktuelle Daten in Statistiken aufnehmen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTakeSnapshotEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton3);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/beginning.png"))); // NOI18N
        jButton2.setToolTipText("Aktuellen Datenpunkt als Start des ausgewählten Bereichs verwenden");
        jButton2.setMaximumSize(new java.awt.Dimension(49, 33));
        jButton2.setMinimumSize(new java.awt.Dimension(49, 33));
        jButton2.setPreferredSize(new java.awt.Dimension(49, 33));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveDateBeforeEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton2);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/end.png"))); // NOI18N
        jButton4.setToolTipText("Aktuellen Datenpunkt als Ende des ausgewählten Bereichs verwenden");
        jButton4.setMaximumSize(new java.awt.Dimension(49, 33));
        jButton4.setMinimumSize(new java.awt.Dimension(49, 33));
        jButton4.setPreferredSize(new java.awt.Dimension(49, 33));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveDataAfterEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton4);

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
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jChartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTaskPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTaskPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jChartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)))
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

    private void fireTakeSnapshotEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTakeSnapshotEvent
        StatManager.getSingleton().takeSnapshot();
        jAllyList.getSelectionModel().clearSelection();
        jAllyList.getSelectionModel().setValueIsAdjusting(false);
    }//GEN-LAST:event_fireTakeSnapshotEvent

    private void fireRemoveMonitoredElementEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveMonitoredElementEvent
        //remove tribe element(s)
        Object[] tribesToRemove = jTribeList.getSelectedValues();
        if (tribesToRemove == null || tribesToRemove.length == 0) {
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
        setup();
    }//GEN-LAST:event_fireRemoveMonitoredElementEvent

    private void fireUpdateChartEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireUpdateChartEvent
        Object tribeSelection = jTribeList.getSelectedValue();
        if (tribeSelection == null) {
            jChartPanel.removeAll();
            jChartPanel.updateUI();
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
        /* try {
        Object tribeSelection = jTribeList.getSelectedValue();
        if (tribeSelection == null) {
        return;
        }

        long v = (long) ((XYPlot) chart.getPlot()).getDomainCrosshairValue();
        if (v == 0) {
        JOptionPaneHelper.showInformationBox(this, "Kein Datenpunkt ausgewählt", "Information");
        return;
        }
        String date = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(v));
        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle Werte vor dem " + date + " löschen?", "Werte löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        StatManager.getSingleton().removeDataBefore((Tribe) tribeSelection, new Date(v).getTime());
        fireUpdateChartEvent(null);
        }
        } catch (Exception e) {
        }*/

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
        /*  try {
        Object tribeSelection = jTribeList.getSelectedValue();
        if (tribeSelection == null) {
        return;
        }

        long v = (long) ((XYPlot) chart.getPlot()).getDomainCrosshairValue();

        if (v == 0) {
        JOptionPaneHelper.showInformationBox(this, "Kein Datenpunkt ausgewählt", "Information");
        return;
        }
        String date = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(v));
        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle Werte ab dem " + date + " löschen?", "Werte löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        StatManager.getSingleton().removeDataAfter((Tribe) tribeSelection, new Date(v).getTime());
        fireUpdateChartEvent(null);
        }
        } catch (Exception e) {
        e.printStackTrace();
        }*/
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jAllyList;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jChartPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
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
    private com.l2fprod.common.swing.JTaskPane jTaskPane1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup2;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup3;
    private javax.swing.JList jTribeList;
    // End of variables declaration//GEN-END:variables
}
