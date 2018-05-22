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
package de.tor.tribes.ui.views;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.TribeStatsElement;
import de.tor.tribes.types.TribeStatsElement.Stats;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.util.BBCodeFormatter;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.bb.DefStatsFormatter;
import de.tor.tribes.util.bb.KillStatsFormatter;
import de.tor.tribes.util.bb.PointStatsFormatter;
import de.tor.tribes.util.bb.WinnerLoserStatsFormatter;
import de.tor.tribes.util.stat.StatManager;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 * @author Torridity
 */
public class DSWorkbenchStatsFrame extends AbstractDSWorkbenchFrame implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("BBCopy")) {
            transferBBCodeToClipboard();
        } else if (e.getActionCommand().equals("Delete")) {
            removeMonitoredElements();
        }
    }
    private static DSWorkbenchStatsFrame SINGLETON = null;
    private JFreeChart chart = null;
    private ValueMarker startPointer = null;
    private ValueMarker endPointer = null;
    private GenericTestPanel centerPanel = null;
    private ChartPanel theChartPanel = null;
    private String sPointStats = null;
    private String sBashOffStats = null;
    private String sBashDefStats = null;
    private String sWinnerLoserStats = null;
    
    public static synchronized DSWorkbenchStatsFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchStatsFrame();
        }
        return SINGLETON;
    }
    
    DSWorkbenchStatsFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jStatsPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jMainStatPanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jTabbedPane1.registerKeyboardAction(DSWorkbenchStatsFrame.this, "BBCopy", bbCopy, JComponent.WHEN_IN_FOCUSED_WINDOW);
        jTribeList.registerKeyboardAction(DSWorkbenchStatsFrame.this, "Delete", delete, JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        jAlwaysOnTopBox.setSelected(GlobalOptions.getProperties().getBoolean("stats.frame.alwaysOnTop"));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        
        jAllyList.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                List allySelection = jAllyList.getSelectedValuesList();
                jTribeList.clearSelection();
                List<Tribe> tribes = new LinkedList<>();
                for (Object o : allySelection) {
                    Tribe[] tribesForAlly = StatManager.getSingleton().getMonitoredTribes((Ally) o);
                    for (Tribe t : tribesForAlly) {
                        if (!tribes.contains(t)) {
                            tribes.add(t);
                        }
                    }
                    Collections.sort(tribes);
                    DefaultListModel<Tribe> model = new DefaultListModel<>();
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
        jStatCreatePanel.setVisible(false);
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.stats_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>

        pack();
    }
    
    @Override
    public void toBack() {
        jAlwaysOnTopBox.setSelected(false);
        fireAlwaysOnTopEvent(null);
        super.toBack();
    }
    
    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());
    }
    
    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        
        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }
        
        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        
    }
    
    @Override
    public String getPropertyPrefix() {
        return "stats.view";
    }
    
    private void buildMenu() {
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bearbeiten");
        final JToggleButton createStats = new JToggleButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/medal.png")));
        createStats.setToolTipText("Umschalten zwischen dem Erzeugen von Statistiken und der Anzeige von Verlaufsgrafiken");
        createStats.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                boolean showChartView = !createStats.isSelected();
                if (showChartView) {
                    createStats.setIcon(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/medal.png")));
                } else {
                    createStats.setIcon(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/chart.png")));
                }
                switchStatChartView(showChartView);
            }
        });
        
        
        JXButton selectStart = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/beginning.png")));
        selectStart.setToolTipText("Setzt eine Startmarkierung beim gewählten Datenpunkt");
        selectStart.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                setStartAnnotation();
            }
        });
        selectStart.setSize(createStats.getSize());
        selectStart.setMinimumSize(createStats.getMinimumSize());
        selectStart.setMaximumSize(createStats.getMaximumSize());
        selectStart.setPreferredSize(createStats.getPreferredSize());
        editPane.getContentPane().add(selectStart);
        JXButton selectEnd = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/end.png")));
        selectEnd.setToolTipText("Setzt eine Endmarkierung beim gewählten Datenpunkt");
        selectEnd.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                setEndAnnotation();
            }
        });
        selectEnd.setSize(createStats.getSize());
        selectEnd.setMinimumSize(createStats.getMinimumSize());
        selectEnd.setMaximumSize(createStats.getMaximumSize());
        selectEnd.setPreferredSize(createStats.getPreferredSize());
        editPane.getContentPane().add(selectEnd);
        
        JXButton removeSelection = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/delete_region.png")));
        removeSelection.setToolTipText("Löscht alle Datenpunkte zwischen der Start- und Endmarkierung");
        removeSelection.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                removeSelection();
            }
        });
        removeSelection.setSize(createStats.getSize());
        removeSelection.setMinimumSize(createStats.getMinimumSize());
        removeSelection.setMaximumSize(createStats.getMaximumSize());
        removeSelection.setPreferredSize(createStats.getPreferredSize());
        editPane.getContentPane().add(removeSelection);
        
        
        createStats.setSize(removeSelection.getSize());
        createStats.setMinimumSize(removeSelection.getMinimumSize());
        createStats.setMaximumSize(removeSelection.getMaximumSize());
        createStats.setPreferredSize(removeSelection.getPreferredSize());
        
        editPane.getContentPane().add(createStats);
        
        
        JXTaskPane viewPane = new JXTaskPane();
        viewPane.setTitle("Anzeige");
        viewPane.getContentPane().add(jViewSelectionBox);
        
        JXTaskPane settingsPane = new JXTaskPane();
        settingsPane.setTitle("Einstellungen");
        
        settingsPane.getContentPane().add(jShowItemValues);
        settingsPane.getContentPane().add(jShowLegend);
        settingsPane.getContentPane().add(jShowLines);
        settingsPane.getContentPane().add(jShowDataPoints);
        
        centerPanel.setupTaskPane(editPane, viewPane, settingsPane);
    }
    
    @Override
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
        int idx = jViewSelectionBox.getSelectedIndex();
        if (idx == 0) {
            TimeSeriesCollection pointsDataset = new TimeSeriesCollection();
            for (TribeStatsElement elem : pElems) {
                TimeSeries pointSeries = new TimeSeries("Punkte (" + elem.getTribe().getName() + ")");
                // TimeSeries pointSeries2 = new TimeSeries("Punkte2 (" + elem.getTribe().getName() + ")");
                Long[] timestamps = elem.getTimestamps();
                Long[] points = elem.getPoints();
                for (int i = 0; i < timestamps.length; i++) {
                    pointSeries.add(new Second(new Date(timestamps[i])), points[i]);
                }
                pointsDataset.addSeries(pointSeries);
            }
            addDataset("Punkte", pointsDataset);
        } else if (idx == 1) {
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
        } else if (idx == 2) {
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
        } else if (idx == 3) {
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
        } else if (idx == 4) {
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
        } else if (idx == 5) {
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
        } else if (idx == 6) {
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
        theChartPanel = new ChartPanel(chart);
        theChartPanel.setDisplayToolTips(true);
        theChartPanel.setMouseWheelEnabled(true);
        jChartPanel.add(theChartPanel);
        
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
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
        for (int i = 0; i < plot.getSeriesCount(); i++) {
            renderer.setSeriesLinesVisible(i, jShowLines.isSelected());
            renderer.setSeriesShapesVisible(i, jShowDataPoints.isSelected());
            plot.setRenderer(i, renderer);
        }
        
        renderer.setBaseItemLabelsVisible(jShowItemValues.isSelected());
        renderer.setBaseItemLabelGenerator(new org.jfree.chart.labels.StandardXYItemLabelGenerator());
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"), NumberFormat.getInstance()));
        int lastDataset = plot.getDatasetCount() - 1;
        if (lastDataset > 0) {
            plot.getRangeAxis().setAxisLinePaint(plot.getLegendItems().get(lastDataset).getLinePaint());
            plot.getRangeAxis().setLabelPaint(plot.getLegendItems().get(lastDataset).getLinePaint());
            plot.getRangeAxis().setTickLabelPaint(plot.getLegendItems().get(lastDataset).getLinePaint());
            plot.getRangeAxis().setTickMarkPaint(plot.getLegendItems().get(lastDataset).getLinePaint());
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        NumberAxis na = ((NumberAxis) plot.getRangeAxis());
        if (na != null) {
            na.setNumberFormatOverride(nf);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jMainStatPanel = new javax.swing.JPanel();
        jChartPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAllyList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTribeList = new javax.swing.JList();
        jShowPoints = new javax.swing.JCheckBox();
        jShowRank = new javax.swing.JCheckBox();
        jShowVillages = new javax.swing.JCheckBox();
        jShowKillsOff = new javax.swing.JCheckBox();
        jShowRankOff = new javax.swing.JCheckBox();
        jShowKillsDef = new javax.swing.JCheckBox();
        jShowRankDef = new javax.swing.JCheckBox();
        jShowItemValues = new javax.swing.JCheckBox();
        jShowLegend = new javax.swing.JCheckBox();
        jShowLines = new javax.swing.JCheckBox();
        jShowDataPoints = new javax.swing.JCheckBox();
        jViewSelectionBox = new javax.swing.JComboBox();
        jStatCreatePanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        jPointsPane = new javax.swing.JEditorPane();
        jScrollPane10 = new javax.swing.JScrollPane();
        jBashOffPane = new javax.swing.JEditorPane();
        jScrollPane11 = new javax.swing.JScrollPane();
        jBashDefPane = new javax.swing.JEditorPane();
        jScrollPane12 = new javax.swing.JScrollPane();
        jWinnerLoserPane = new javax.swing.JEditorPane();
        jButton8 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jStartDate = new de.tor.tribes.ui.components.DateTimeField();
        jLabel5 = new javax.swing.JLabel();
        jEndDate = new de.tor.tribes.ui.components.DateTimeField();
        jPanel11 = new javax.swing.JPanel();
        jWeeklyStats = new javax.swing.JButton();
        jMonthlyStats = new javax.swing.JButton();
        jUseTop10Box = new javax.swing.JCheckBox();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jStatsPanel = new org.jdesktop.swingx.JXPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jMainStatPanel.setMinimumSize(new java.awt.Dimension(516, 300));
        jMainStatPanel.setLayout(new java.awt.BorderLayout());

        jChartPanel.setBackground(new java.awt.Color(239, 235, 223));
        jChartPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jChartPanel.setPreferredSize(new java.awt.Dimension(499, 300));
        jChartPanel.setLayout(new java.awt.BorderLayout());
        jMainStatPanel.add(jChartPanel, java.awt.BorderLayout.CENTER);

        jPanel7.setPreferredSize(new java.awt.Dimension(516, 150));
        jPanel7.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Überwachte Stämme"));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(258, 100));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(258, 150));

        jScrollPane1.setViewportView(jAllyList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel7.add(jScrollPane1, gridBagConstraints);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Überwachte Spieler"));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(258, 100));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(258, 150));

        jScrollPane2.setViewportView(jTribeList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel7.add(jScrollPane2, gridBagConstraints);

        jMainStatPanel.add(jPanel7, java.awt.BorderLayout.NORTH);

        jShowPoints.setSelected(true);
        jShowPoints.setText("Punkte anzeigen");
        jShowPoints.setOpaque(false);
        jShowPoints.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowRank.setText("Rang anzeigen");
        jShowRank.setOpaque(false);
        jShowRank.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowVillages.setText("Dörfer anzeigen");
        jShowVillages.setOpaque(false);
        jShowVillages.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowKillsOff.setText("Kills (Off) anzeigen");
        jShowKillsOff.setOpaque(false);
        jShowKillsOff.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowRankOff.setText("Rang (Off) anzeigen");
        jShowRankOff.setOpaque(false);
        jShowRankOff.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowKillsDef.setText("Kills (Deff) anzeigen");
        jShowKillsDef.setOpaque(false);
        jShowKillsDef.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowRankDef.setText("Rang (Deff) anzeigen");
        jShowRankDef.setOpaque(false);
        jShowRankDef.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowItemValues.setText("Werte anzeigen");
        jShowItemValues.setToolTipText("Zeigt die Werte der Datenpunkte im Diagramm an");
        jShowItemValues.setOpaque(false);
        jShowItemValues.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowLegend.setSelected(true);
        jShowLegend.setText("Legende anzeigen");
        jShowLegend.setOpaque(false);
        jShowLegend.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowLines.setSelected(true);
        jShowLines.setText("Linien anzeigen");
        jShowLines.setOpaque(false);
        jShowLines.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jShowDataPoints.setSelected(true);
        jShowDataPoints.setText("Datenpunkte anzeigen");
        jShowDataPoints.setOpaque(false);
        jShowDataPoints.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUpdateChartEvent(evt);
            }
        });

        jViewSelectionBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Punkte", "Rang (Punkte)", "Dörfer", "Kills (Off)", "Rang (Off)", "Kills (Def)", "Rang (Def)" }));
        jViewSelectionBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireViewChangedEvent(evt);
            }
        });

        jStatCreatePanel.setBackground(new java.awt.Color(239, 235, 223));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Auswertung"));

        jPointsPane.setContentType("text/html");
        jPointsPane.setEditable(false);
        jScrollPane7.setViewportView(jPointsPane);

        jTabbedPane1.addTab("Punkte", new javax.swing.ImageIcon(getClass().getResource("/res/goblet_gold.png")), jScrollPane7); // NOI18N

        jBashOffPane.setContentType("text/html");
        jBashOffPane.setEditable(false);
        jScrollPane10.setViewportView(jBashOffPane);

        jTabbedPane1.addTab("Bash (Off)", new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jScrollPane10); // NOI18N

        jBashDefPane.setContentType("text/html");
        jBashDefPane.setEditable(false);
        jScrollPane11.setViewportView(jBashDefPane);

        jTabbedPane1.addTab("Bash (Deff)", new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jScrollPane11); // NOI18N

        jWinnerLoserPane.setContentType("text/html");
        jWinnerLoserPane.setEditable(false);
        jScrollPane12.setViewportView(jWinnerLoserPane);

        jTabbedPane1.addTab("Gewinner/Verlierer", new javax.swing.ImageIcon(getClass().getResource("/res/up_plus.png")), jScrollPane12); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/select.png"))); // NOI18N
        jButton8.setText("Auswertung erstellen");
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireGenerateStatsEvent(evt);
            }
        });

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel4.setText("Zeitraum (Start)");
        jPanel1.add(jLabel4, new java.awt.GridBagConstraints());

        jStartDate.setTimeEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel1.add(jStartDate, gridBagConstraints);

        jLabel5.setText("Zeitraum (Ende)");
        jPanel1.add(jLabel5, new java.awt.GridBagConstraints());

        jEndDate.setTimeEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jEndDate, gridBagConstraints);

        jPanel11.setOpaque(false);
        jPanel11.setPreferredSize(new java.awt.Dimension(520, 100));
        jPanel11.setLayout(new java.awt.GridBagLayout());

        jWeeklyStats.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/calendar_7.png"))); // NOI18N
        jWeeklyStats.setText("Statistik für eine Woche (Heute - 7 Tage)");
        jWeeklyStats.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jWeeklyStats.setMaximumSize(new java.awt.Dimension(40, 25));
        jWeeklyStats.setMinimumSize(new java.awt.Dimension(40, 25));
        jWeeklyStats.setPreferredSize(new java.awt.Dimension(260, 25));
        jWeeklyStats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeStatTimeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel11.add(jWeeklyStats, gridBagConstraints);

        jMonthlyStats.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/calendar_31.png"))); // NOI18N
        jMonthlyStats.setText("Statistik für einen Monat (Heute - 31 Tage)");
        jMonthlyStats.setMaximumSize(new java.awt.Dimension(40, 25));
        jMonthlyStats.setMinimumSize(new java.awt.Dimension(40, 25));
        jMonthlyStats.setPreferredSize(new java.awt.Dimension(260, 25));
        jMonthlyStats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeStatTimeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel11.add(jMonthlyStats, gridBagConstraints);

        jUseTop10Box.setText("Nur Top-10 anzeigen");
        jUseTop10Box.setOpaque(false);

        javax.swing.GroupLayout jStatCreatePanelLayout = new javax.swing.GroupLayout(jStatCreatePanel);
        jStatCreatePanel.setLayout(jStatCreatePanelLayout);
        jStatCreatePanelLayout.setHorizontalGroup(
            jStatCreatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStatCreatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jStatCreatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jStatCreatePanelLayout.createSequentialGroup()
                        .addGroup(jStatCreatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jStatCreatePanelLayout.createSequentialGroup()
                        .addGroup(jStatCreatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jStatCreatePanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 287, Short.MAX_VALUE)
                                .addComponent(jUseTop10Box)
                                .addGap(18, 18, 18)
                                .addComponent(jButton8)))
                        .addGap(14, 14, 14))))
        );
        jStatCreatePanelLayout.setVerticalGroup(
            jStatCreatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStatCreatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jStatCreatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jUseTop10Box, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setTitle("Statistiken");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        jStatsPanel.setBackground(new java.awt.Color(239, 235, 223));
        jStatsPanel.setMinimumSize(new java.awt.Dimension(700, 500));
        jStatsPanel.setPreferredSize(new java.awt.Dimension(700, 500));
        jStatsPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jStatsPanel, gridBagConstraints);

        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireUpdateChartEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireUpdateChartEvent
        if (!jStatCreatePanel.isVisible()) {
            List tribeSelection = jTribeList.getSelectedValuesList();
            if (tribeSelection == null) {
                jChartPanel.removeAll();
                SwingUtilities.invokeLater(new Runnable() {
                    
                    @Override
                    public void run() {
                        jChartPanel.updateUI();
                    }
                });
                
                return;
            }
            List<TribeStatsElement> elems = new LinkedList<>();
            
            for (Object o : tribeSelection) {
                TribeStatsElement elem = StatManager.getSingleton().getStatsForTribe((Tribe) o);
                if (elem != null) {
                    elems.add(elem);
                }
            }
            updateChart(elems);
        }
    }//GEN-LAST:event_fireUpdateChartEvent
    
    private void fireViewChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireViewChangedEvent
        if ((evt == null || evt.getStateChange() == ItemEvent.SELECTED) && (theChartPanel != null && theChartPanel.isVisible())) {
            List tribeSelection = jTribeList.getSelectedValuesList();
            if (tribeSelection == null || tribeSelection.isEmpty()) {
                jChartPanel.removeAll();
                SwingUtilities.invokeLater(new Runnable() {
                    
                    @Override
                    public void run() {
                        jChartPanel.updateUI();
                    }
                });
                
                return;
            }
            List<TribeStatsElement> elems = new LinkedList<>();
            
            for (Object o : tribeSelection) {
                TribeStatsElement elem = StatManager.getSingleton().getStatsForTribe((Tribe) o);
                if (elem != null) {
                    elems.add(elem);
                }
            }
            updateChart(elems);
        }
    }//GEN-LAST:event_fireViewChangedEvent
    
    private void fireGenerateStatsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireGenerateStatsEvent
        long start = jStartDate.getSelectedDate().getTime();
        long end = jEndDate.getSelectedDate().getTime();
        List<Tribe> usedTribes = new LinkedList<>();
        //use selected
        List tribes = jTribeList.getSelectedValuesList();
        if (tribes == null || tribes.isEmpty()) {
            List allies = jAllyList.getSelectedValuesList();
            if (allies == null || allies.isEmpty()) {
                //nothing selected
                JOptionPaneHelper.showInformationBox(DSWorkbenchStatsFrame.this, "Keine Stämme/Spieler ausgewählt.", "Information");
                return;
            } else {
                //allies selected ... add monitored members to tribe list
                List<Object> lTribes = new LinkedList<>();
                for (Object a : allies) {
                    Tribe[] tribesForAlly = StatManager.getSingleton().getMonitoredTribes((Ally) a);
                    lTribes.addAll(Arrays.asList(tribesForAlly));
                }
                tribes = lTribes;
            }
        }
        if (tribes.isEmpty()) {
            JOptionPaneHelper.showInformationBox(DSWorkbenchStatsFrame.this, "Die gewählten Stämme enthalten keine überwachten Spieler", "Information");
            return;
        }
        for (Object o : tribes) {
            usedTribes.add((Tribe) o);
        }
        
        List<Stats> stats = new LinkedList<>();
        for (Tribe t : usedTribes) {
            TribeStatsElement elem = StatManager.getSingleton().getStatsForTribe(t);
            Stats elemStat = elem.generateStats(start, end);
            stats.add(elemStat);
        }
        
        sPointStats = new PointStatsFormatter().formatElements(stats, !jUseTop10Box.isSelected());
        jPointsPane.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(sPointStats) + "</body></html>");
        sBashOffStats = new KillStatsFormatter().formatElements(stats, !jUseTop10Box.isSelected());
        jBashOffPane.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(sBashOffStats) + "</body></html>");
        sBashDefStats = new DefStatsFormatter().formatElements(stats, !jUseTop10Box.isSelected());
        jBashDefPane.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(sBashDefStats) + "</body></html>");
        sWinnerLoserStats = new WinnerLoserStatsFormatter().formatElements(stats, !jUseTop10Box.isSelected());
        jWinnerLoserPane.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(sWinnerLoserStats) + "</body></html>");
}//GEN-LAST:event_fireGenerateStatsEvent
    
    private void fireChangeStatTimeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeStatTimeEvent
        if (evt.getSource() == jWeeklyStats) {
            //remove one week from end date
            long oneWeek = DateUtils.MILLIS_PER_DAY * 7;// 1000l * 60l * 60l * 24l * 7l;
            Date end = new Date();//jEndDate.getSelectedDate();
            jEndDate.setDate(end);
            //long start = end.getTime() - oneWeek;
            jStartDate.setDate(new Date(end.getTime() - oneWeek));
        } else {
            //remove one month from end date
            long oneMonth = DateUtils.MILLIS_PER_DAY * 31;// 1000l * 60l * 60l * 24l * 31l;
            Date end = new Date();//jEndDate.getSelectedDate();
            jEndDate.setDate(end);
            //  long start = end.getTime() - oneMonth;
            jStartDate.setDate(new Date(end.getTime() - oneMonth));
        }
    }//GEN-LAST:event_fireChangeStatTimeEvent
    
private void fireAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireAlwaysOnTopEvent
    
    private void transferBBCodeToClipboard() {
        int idx = jTabbedPane1.getSelectedIndex();
        if (idx == 0) {
            copyStatsToClipboard("Punkte", sPointStats);
        } else if (idx == 1) {
            copyStatsToClipboard("Bash (Off)", sBashOffStats);
        } else if (idx == 2) {
            copyStatsToClipboard("Bash (Deff)", sBashDefStats);
        } else if (idx == 3) {
            copyStatsToClipboard("Gewinner/Verlierer", sWinnerLoserStats);
        } else {
            JOptionPaneHelper.showInformationBox(DSWorkbenchStatsFrame.this, "Bitte wähle den Abschnitt der Auswertung den du kopieren möchtest", "Information");
        }
    }
    
    private void copyStatsToClipboard(String pType, String pStatText) {
        if (pStatText == null) {
            JOptionPaneHelper.showInformationBox(DSWorkbenchStatsFrame.this, "Bitte erstelle erst eine Auswertung bevor du die Daten kopierst", "Information");
            return;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(pStatText), null);
            JOptionPaneHelper.showInformationBox(DSWorkbenchStatsFrame.this, "Daten der Auswertung '" + pType + "' in Zwischenablage kopiert", "Information");
        } catch (HeadlessException he) {
            JOptionPaneHelper.showErrorBox(DSWorkbenchStatsFrame.this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
        }
    }
    
    private void setStartAnnotation() {
        XYPlot plot = ((XYPlot) chart.getPlot());
        
        double x = plot.getDomainCrosshairValue();
        
        if (startPointer != null) {
            plot.removeDomainMarker(startPointer);
        }
        
        if (startPointer != null && startPointer.getValue() == x) {
            plot.removeDomainMarker(startPointer);
            startPointer = null;
        } else {
            if (endPointer != null) {
                if (endPointer.getValue() < x) {
                    //flip start and end
                    plot.removeDomainMarker(endPointer);
                    startPointer = new ValueMarker(endPointer.getValue());
                    startPointer.setLabel("Start");
                    startPointer.setPaint(Color.green);
                    plot.addDomainMarker(startPointer);
                    endPointer = new ValueMarker(x);
                    endPointer.setLabel("Ende");
                    endPointer.setPaint(Color.red);
                    plot.addDomainMarker(endPointer);
                } else {
                    startPointer = new ValueMarker(x);
                    startPointer.setLabel("Start");
                    startPointer.setPaint(Color.green);
                    plot.addDomainMarker(startPointer);
                }
            } else {
                startPointer = new ValueMarker(x);
                startPointer.setLabel("Start");
                startPointer.setPaint(Color.green);
                plot.addDomainMarker(startPointer);
            }
        }
        
        jChartPanel.repaint();
    }
    
    private void setEndAnnotation() {
        XYPlot plot = ((XYPlot) chart.getPlot());
        double x = plot.getDomainCrosshairValue();
        if (endPointer != null) {
            plot.removeDomainMarker(endPointer);
        }
        
        if (endPointer != null && endPointer.getValue() == x) {
            plot.removeDomainMarker(endPointer);
            endPointer = null;
        } else {
            if (startPointer != null) {
                if (startPointer.getValue() > x) {
                    //flip start and end
                    plot.removeDomainMarker(startPointer);
                    endPointer = new ValueMarker(startPointer.getValue());
                    endPointer.setLabel("Ende");
                    endPointer.setPaint(Color.red);
                    plot.addDomainMarker(endPointer);
                    startPointer = new ValueMarker(x);
                    startPointer.setLabel("Start");
                    startPointer.setPaint(Color.green);
                    plot.addDomainMarker(startPointer);
                } else {
                    endPointer = new ValueMarker(x);
                    endPointer.setLabel("Ende");
                    endPointer.setPaint(Color.red);
                    plot.addDomainMarker(endPointer);
                }
            } else {
                endPointer = new ValueMarker(x);
                endPointer.setLabel("Ende");
                endPointer.setPaint(Color.red);
                plot.addDomainMarker(endPointer);
            }
        }
        
        jChartPanel.repaint();
    }
    
    private void removeSelection() {
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
            long v = (long) endPointer.getValue();
            String date = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(v));
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle Werte vor dem " + date + " löschen?", "Werte löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                StatManager.getSingleton().removeDataBefore((Tribe) tribeSelection, new Date(v).getTime());
                fireUpdateChartEvent(null);
            }
        } else if (endPointer == null) {
            //remove after start
            long v = (long) startPointer.getValue();
            String date = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(v));
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle Werte nach dem " + date + " löschen?", "Werte löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                StatManager.getSingleton().removeDataAfter((Tribe) tribeSelection, new Date(v).getTime());
                fireUpdateChartEvent(null);
            }
        } else {
            //remove date between
            long vstart = (long) startPointer.getValue();
            long vend = (long) endPointer.getValue();
            String startDate = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(vstart));
            String endDate = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss").format(new Date(vend));
            
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle Werte zwischen dem " + startDate + " und dem " + endDate + " löschen?", "Werte löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                StatManager.getSingleton().removeDataBetween((Tribe) tribeSelection, new Date(vstart).getTime(), new Date(vend).getTime());
                fireUpdateChartEvent(null);
            }
        }
        
        fireUpdateChartEvent(null);
    }
    
    private void switchStatChartView(boolean pShowChart) {
        jChartPanel.invalidate();
        if (pShowChart) {
            jChartPanel.remove(jStatCreatePanel);
            jStatCreatePanel.setVisible(false);
            if (theChartPanel != null) {
                jChartPanel.add(theChartPanel, BorderLayout.CENTER);
                theChartPanel.setVisible(true);
                fireViewChangedEvent(null);
            }
        } else {
            if (theChartPanel != null) {
                jChartPanel.remove(theChartPanel);
                theChartPanel.setVisible(false);
            }
            jStatCreatePanel.setVisible(true);
            jChartPanel.add(jStatCreatePanel, BorderLayout.CENTER);
        }
        jChartPanel.revalidate();
        jChartPanel.repaint();
    }
    
    private void removeMonitoredElements() {
        //remove tribe element(s)
        List tribesToRemove = jTribeList.getSelectedValuesList();
        if (tribesToRemove == null || tribesToRemove.isEmpty()) {
            JOptionPaneHelper.showInformationBox(this, "Kein Spieler ausgewählt", "Fehler");
            return;
        }
        String message;
        if (tribesToRemove.isEmpty()) {
            message = "Erfasste Daten für markierten Spieler löschen?";
        } else {
            message = "Erfasste Daten für markierte Spieler löschen?";
        }
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            for (Object o : tribesToRemove) {
                StatManager.getSingleton().removeTribeData((Tribe) o);
            }
        } else {
            return;
        }
        resetView();
    }
    
    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    
    public static void main(String args[]) {
        
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //  UIManager.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        GlobalOptions.setSelectedServer("de68");
        DataHolder.getSingleton().loadData(false);
        StatManager.getSingleton().setup();
        
        DSWorkbenchStatsFrame.getSingleton().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        DSWorkbenchStatsFrame.getSingleton().resetView();
        DSWorkbenchStatsFrame.getSingleton().setVisible(true);
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JList jAllyList;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JEditorPane jBashDefPane;
    private javax.swing.JEditorPane jBashOffPane;
    private javax.swing.JButton jButton8;
    private javax.swing.JPanel jChartPanel;
    private de.tor.tribes.ui.components.DateTimeField jEndDate;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jMainStatPanel;
    private javax.swing.JButton jMonthlyStats;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JEditorPane jPointsPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane7;
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
    private javax.swing.JPanel jStatCreatePanel;
    private org.jdesktop.swingx.JXPanel jStatsPanel;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList<Tribe> jTribeList;
    private javax.swing.JCheckBox jUseTop10Box;
    private javax.swing.JComboBox jViewSelectionBox;
    private javax.swing.JButton jWeeklyStats;
    private javax.swing.JEditorPane jWinnerLoserPane;
    // End of variables declaration//GEN-END:variables
}
