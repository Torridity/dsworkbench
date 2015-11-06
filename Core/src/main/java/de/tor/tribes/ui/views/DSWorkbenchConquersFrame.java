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

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.models.ConquerTableModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.PropertyHelper;
import de.tor.tribes.util.conquer.ConquerManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.decorator.PatternPredicate;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author Charon
 */
public class DSWorkbenchConquersFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ListSelectionListener, ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Find")) {
            BufferedImage back = ImageUtils.createCompatibleBufferedImage(3, 3, BufferedImage.TRANSLUCENT);
            Graphics g = back.getGraphics();
            g.setColor(new Color(120, 120, 120, 120));
            g.fillRect(0, 0, back.getWidth(), back.getHeight());
            g.setColor(new Color(120, 120, 120));
            g.drawLine(0, 0, 3, 3);
            g.dispose();
            TexturePaint paint = new TexturePaint(back, new Rectangle2D.Double(0, 0, back.getWidth(), back.getHeight()));
            jxFilterPane.setBackgroundPainter(new MattePainter(paint));
            DefaultListModel model = new DefaultListModel();

            for (int i = 0; i < jConquersTable.getColumnCount(); i++) {
                TableColumnExt col = jConquersTable.getColumnExt(i);
                if (col.isVisible() && !col.getTitle().equals("Entfernung") && !col.getTitle().equals("Dorfpunkte")) {
                    model.addElement(col.getTitle());
                }
            }
            jXColumnList.setModel(model);
            jXColumnList.setSelectedIndex(0);
            jxFilterPane.setVisible(true);
        }
    }
    private static Logger logger = Logger.getLogger("ConquerView");
    private static DSWorkbenchConquersFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    private PainterHighlighter highlighter = null;

    DSWorkbenchConquersFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jConquerPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jConquersPanel);
        buildMenu();
        try {
            jConquersFrameAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("conquers.frame.alwaysOnTop")));
            setAlwaysOnTop(jConquersFrameAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        jConquersTable.setModel(new ConquerTableModel());
        jConquersTable.getSelectionModel().addListSelectionListener(DSWorkbenchConquersFrame.this);
        capabilityInfoPanel1.addActionListener(this);
        jConquersTable.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchConquersFrame.getSingleton().actionPerformed(new ActionEvent(jConquersTable, 0, "Find"));
            }
        });

        jXColumnList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateFilter();
            }
        });
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.conquers_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
        setGlassPane(jxFilterPane);
        pack();
    }

    @Override
    public void toBack() {
        jConquersFrameAlwaysOnTop.setSelected(false);
        fireConquersFrameAlwaysOnTopEvent(null);
        super.toBack();
    }

    public static synchronized DSWorkbenchConquersFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchConquersFrame();
        }
        return SINGLETON;
    }

    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jConquersFrameAlwaysOnTop.isSelected());
        PropertyHelper.storeTableProperties(jConquersTable, pConfig, getPropertyPrefix());

    }

    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));

        try {
            jConquersFrameAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception e) {
        }

        setAlwaysOnTop(jConquersFrameAlwaysOnTop.isSelected());

        PropertyHelper.restoreTableProperties(jConquersTable, pConfig, getPropertyPrefix());
    }

    public String getPropertyPrefix() {
        return "conquers.view";
    }

    private void buildMenu() {
        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");

        JXButton button2 = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/att_browser.png")));
        button2.setToolTipText("Zentriert die markierte Eroberungen im Spiel");
        button2.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                centerVillageInGame();
            }
        });

        transferPane.getContentPane().add(button2);
        if (!GlobalOptions.isMinimal()) {
            JXButton centerOnMap = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/center_24x24.png")));
            centerOnMap.setToolTipText("Zentriert die markierte Eroberung auf der Hauptkarte");
            centerOnMap.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    centerVillageOnMap();
                }
            });

            transferPane.getContentPane().add(centerOnMap);
        }

       /* JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");

        JXButton borderControlButton = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/att_browser.png")));
        borderControlButton.setToolTipText("Zentriert die markierte Eroberungen im Spiel");
        borderControlButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
               DSWorkbenchBorderControl.getSingleton().setupAndShow();
            }
        });
        miscPane.getContentPane().add(borderControlButton);*/

        centerPanel.setupTaskPane(transferPane);//, miscPane);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jConquersPanel = new javax.swing.JPanel();
        jConquerTablePanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jXPanel2 = new org.jdesktop.swingx.JXPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLastUpdateLabel = new javax.swing.JLabel();
        jGreyConquersLabel = new javax.swing.JLabel();
        jFriendlyConquersLabel = new javax.swing.JLabel();
        jxFilterPane = new org.jdesktop.swingx.JXPanel();
        jXPanel3 = new org.jdesktop.swingx.JXPanel();
        jButton12 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jFilterRows = new javax.swing.JCheckBox();
        jFilterCaseSensitive = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXColumnList = new org.jdesktop.swingx.JXList();
        jLabel22 = new javax.swing.JLabel();
        jConquersFrameAlwaysOnTop = new javax.swing.JCheckBox();
        jConquerPanel = new org.jdesktop.swingx.JXPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jConquersPanel.setBackground(new java.awt.Color(239, 235, 223));
        jConquersPanel.setLayout(new java.awt.BorderLayout());

        jConquerTablePanel.setLayout(new java.awt.BorderLayout());

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setText("Keine Meldung");
        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jConquerTablePanel.add(infoPanel, java.awt.BorderLayout.SOUTH);

        jConquersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(jConquersTable);

        jConquerTablePanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jConquersPanel.add(jConquerTablePanel, java.awt.BorderLayout.CENTER);

        jXPanel2.setOpaque(false);
        jXPanel2.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jXPanel2.add(jSeparator1, gridBagConstraints);

        jLastUpdateLabel.setText("Letzte Aktualisierung:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jXPanel2.add(jLastUpdateLabel, gridBagConstraints);

        jGreyConquersLabel.setBackground(new java.awt.Color(255, 204, 204));
        jGreyConquersLabel.setText("Grau-Adelungen:");
        jGreyConquersLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jXPanel2.add(jGreyConquersLabel, gridBagConstraints);

        jFriendlyConquersLabel.setBackground(new java.awt.Color(0, 255, 255));
        jFriendlyConquersLabel.setText("Aufadelungen:");
        jFriendlyConquersLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jXPanel2.add(jFriendlyConquersLabel, gridBagConstraints);

        jConquersPanel.add(jXPanel2, java.awt.BorderLayout.SOUTH);

        jxFilterPane.setOpaque(false);
        jxFilterPane.setLayout(new java.awt.GridBagLayout());

        jXPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jXPanel3.setInheritAlpha(false);

        jButton12.setText("Anwenden");
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButton12fireHideGlassPaneEvent(evt);
            }
        });

        jTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField1fireHighlightEvent(evt);
            }
        });

        jLabel21.setText("Suchbegriff");

        jFilterRows.setText("Nur gefilterte Zeilen anzeigen");
        jFilterRows.setOpaque(false);
        jFilterRows.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jFilterRowsfireUpdateFilterEvent(evt);
            }
        });

        jFilterCaseSensitive.setText("Groß-/Kleinschreibung beachten");
        jFilterCaseSensitive.setOpaque(false);
        jFilterCaseSensitive.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jFilterCaseSensitivefireUpdateFilterEvent(evt);
            }
        });

        jXColumnList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jXColumnList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jXColumnList);

        jLabel22.setText("Spalten");

        javax.swing.GroupLayout jXPanel3Layout = new javax.swing.GroupLayout(jXPanel3);
        jXPanel3.setLayout(jXPanel3Layout);
        jXPanel3Layout.setHorizontalGroup(
            jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jXPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jFilterRows, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jFilterCaseSensitive, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton12)))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jXPanel3Layout.setVerticalGroup(
            jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jXPanel3Layout.createSequentialGroup()
                            .addComponent(jFilterCaseSensitive)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jFilterRows)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton12))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel22))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jxFilterPane.add(jXPanel3, new java.awt.GridBagConstraints());

        setTitle("Eroberungen");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jConquersFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jConquersFrameAlwaysOnTop.setOpaque(false);
        jConquersFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireConquersFrameAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jConquersFrameAlwaysOnTop, gridBagConstraints);

        jConquerPanel.setBackground(new java.awt.Color(239, 235, 223));
        jConquerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 661;
        gridBagConstraints.ipady = 354;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jConquerPanel, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setDeletable(false);
        capabilityInfoPanel1.setPastable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireConquersFrameAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireConquersFrameAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireConquersFrameAlwaysOnTopEvent
    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_fireHideInfoEvent

    private void jButton12fireHideGlassPaneEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton12fireHideGlassPaneEvent
        jxFilterPane.setBackgroundPainter(null);
        jxFilterPane.setVisible(false);
}//GEN-LAST:event_jButton12fireHideGlassPaneEvent

    private void jTextField1fireHighlightEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField1fireHighlightEvent
        updateFilter();
}//GEN-LAST:event_jTextField1fireHighlightEvent

    private void jFilterRowsfireUpdateFilterEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jFilterRowsfireUpdateFilterEvent
        updateFilter();
}//GEN-LAST:event_jFilterRowsfireUpdateFilterEvent

    private void jFilterCaseSensitivefireUpdateFilterEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jFilterCaseSensitivefireUpdateFilterEvent
        updateFilter();
}//GEN-LAST:event_jFilterCaseSensitivefireUpdateFilterEvent
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jConquersTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Eroberung gewählt" : " Eroberungen gewählt"));
            }
        }
    }

    private void updateFilter() {
        if (highlighter != null) {
            jConquersTable.removeHighlighter(highlighter);
        }
        final List<String> columns = new LinkedList<String>();
        for (Object o : jXColumnList.getSelectedValues()) {
            columns.add((String) o);
        }
        if (!jFilterRows.isSelected()) {
            jConquersTable.setRowFilter(null);
            final List<Integer> relevantCols = new LinkedList<Integer>();
            List<TableColumn> cols = jConquersTable.getColumns(true);
            for (int i = 0; i < jConquersTable.getColumnCount(); i++) {
                TableColumnExt col = jConquersTable.getColumnExt(i);
                if (col.isVisible() && columns.contains(col.getTitle())) {
                    relevantCols.add(cols.indexOf(col));
                }
            }
            for (Integer col : relevantCols) {
                PatternPredicate patternPredicate0 = new PatternPredicate((jFilterCaseSensitive.isSelected() ? "" : "(?i)") + Matcher.quoteReplacement(jTextField1.getText()), col);
                MattePainter mp = new MattePainter(new Color(0, 0, 0, 120));
                highlighter = new PainterHighlighter(new HighlightPredicate.NotHighlightPredicate(patternPredicate0), mp);
                jConquersTable.addHighlighter(highlighter);
            }
        } else {
            jConquersTable.setRowFilter(new RowFilter<TableModel, Integer>() {

                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    final List<Integer> relevantCols = new LinkedList<Integer>();
                    List<TableColumn> cols = jConquersTable.getColumns(true);
                    for (int i = 0; i < jConquersTable.getColumnCount(); i++) {
                        TableColumnExt col = jConquersTable.getColumnExt(i);
                        if (col.isVisible() && columns.contains(col.getTitle())) {
                            relevantCols.add(cols.indexOf(col));
                        }
                    }

                    for (Integer col : relevantCols) {
                        if (jFilterCaseSensitive.isSelected()) {
                            if (entry.getStringValue(col).indexOf(jTextField1.getText()) > -1) {
                                return true;
                            }
                        } else {
                            if (entry.getStringValue(col).toLowerCase().indexOf(jTextField1.getText().toLowerCase()) > -1) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(getBackground()));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    /**
     *
     * @param pMessage
     */
    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.RED));
        jXLabel1.setForeground(Color.WHITE);
        jXLabel1.setText(pMessage);
    }

    private void centerVillageOnMap() {
        List<Conquer> selection = getSelectedConquers();
        if (selection.isEmpty()) {
            showError("Keine Eroberung gewählt");
            return;
        }
        DSWorkbenchMainFrame.getSingleton().centerVillage(selection.get(0).getVillage());

    }

    private void centerVillageInGame() {
        List<Conquer> selection = getSelectedConquers();
        if (selection.isEmpty()) {
            showError("Keine Eroberung gewählt");
            return;
        }
        BrowserCommandSender.centerVillage(selection.get(0).getVillage());
    }

    @Override
    public void resetView() {
        ConquerManager.getSingleton().addManagerListener(this);
        //update view
        jConquersTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        String[] cols = new String[]{"Dorfpunkte", "Kontinent", "Entfernung"};
        for (String col : cols) {
            TableColumnExt columns = jConquersTable.getColumnExt(col);
            columns.setPreferredWidth(80);
            columns.setMaxWidth(80);
            columns.setWidth(80);
        }
        cols = new String[]{"Zustimmung"};
        for (String col : cols) {
            TableColumnExt columns = jConquersTable.getColumnExt(col);
            columns.setPreferredWidth(90);
            columns.setMaxWidth(90);
            columns.setWidth(90);
        }
        ((ConquerTableModel) jConquersTable.getModel()).fireTableDataChanged();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    private List<Conquer> getSelectedConquers() {
        final List<Conquer> elements = new LinkedList<Conquer>();
        int[] selectedRows = jConquersTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return elements;
        }
        for (Integer selectedRow : selectedRows) {
            Conquer c = (Conquer) ConquerManager.getSingleton().getAllElements().get(jConquersTable.convertRowIndexToModel(selectedRow));
            if (c != null) {
                elements.add(c);
            }
        }
        return elements;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //  UIManager.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        DSWorkbenchConquersFrame.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        for (int i = 0; i < 50; i++) {
            Conquer c = new Conquer();
            c.setTimestamp((int) System.currentTimeMillis());
            DummyVillage v = new DummyVillage((short) (i * Math.random()), (short) i);
            Ally a = new Ally();
            a.setName("Los Locos");
            a.setTag("L~L");
            Tribe t = new Tribe();
            t.setName("Someone");
            t.setAlly(a);
            v.setTribe(t);
            c.setVillage(v);
            c.setWinner(v.getTribe());
            c.setLoser(v.getTribe());
            ConquerManager.getSingleton().addConquer(c);
        }
        DSWorkbenchConquersFrame.getSingleton().resetView();
        DSWorkbenchConquersFrame.getSingleton().dataChangedEvent();
        DSWorkbenchConquersFrame.getSingleton().setVisible(true);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JButton jButton12;
    private org.jdesktop.swingx.JXPanel jConquerPanel;
    private org.jdesktop.swingx.JXPanel jConquerTablePanel;
    private javax.swing.JCheckBox jConquersFrameAlwaysOnTop;
    private javax.swing.JPanel jConquersPanel;
    private static final org.jdesktop.swingx.JXTable jConquersTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JCheckBox jFilterCaseSensitive;
    private javax.swing.JCheckBox jFilterRows;
    private javax.swing.JLabel jFriendlyConquersLabel;
    private javax.swing.JLabel jGreyConquersLabel;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLastUpdateLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private org.jdesktop.swingx.JXList jXColumnList;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXPanel jXPanel2;
    private org.jdesktop.swingx.JXPanel jXPanel3;
    private org.jdesktop.swingx.JXPanel jxFilterPane;
    // End of variables declaration//GEN-END:variables

    static {
        jConquersTable.setColumnControlVisible(true);
        ColorHighlighter p = new ColorHighlighter(new PatternPredicate(Pattern.compile("Barbaren"), 4), Color.PINK, Color.BLACK);
        ColorHighlighter p1 = new ColorHighlighter(new ColumnEqualsPredicate(5, 7), Color.CYAN, Color.BLACK);
        jConquersTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B), p, p1);
        jConquersTable.setDefaultRenderer(Date.class, new DateCellRenderer());
    }

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ConquerManager.getSingleton().getLastUpdate());
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        jLastUpdateLabel.setText("<html><b>Letzte Aktualisierung:</b> " + f.format(c.getTime()) + "</html>");

        int[] conquerStats = ConquerManager.getSingleton().getConquersStats();
        int conquers = ConquerManager.getSingleton().getConquerCount();
        int percGrey = (int) Math.rint(100.0 * (double) conquerStats[0] / (double) conquers);
        int percFriendly = (int) Math.rint(100.0 * (double) conquerStats[1] / (double) conquers);

        jGreyConquersLabel.setText("<html><b>Grau-Adelungen:</b> " + conquerStats[0] + " von " + conquers + " (" + percGrey + "%)" + "</html>");
        jFriendlyConquersLabel.setText("<html><b>Aufadelungen:</b> " + conquerStats[1] + " von " + conquers + " (" + percFriendly + "%)" + "</html>");
        ((ConquerTableModel) jConquersTable.getModel()).fireTableDataChanged();
    }
}

class ColumnEqualsPredicate implements HighlightPredicate {

    public static final int ALL = -1;
    private int highlightColumn;
    private int[] testColumn;
    private Pattern pattern;

    /**
     * Instantiates a Predicate with the given Pattern and testColumn index (in model coordinates) highlighting all columns. A column index
     * of -1 is interpreted as "all".
     *
     * @param pattern the Pattern to test the cell value against
     * @param testColumn the column index in model coordinates of the cell which contains the value to test against the pattern
     */
    public ColumnEqualsPredicate(int... testColumn) {
        this.testColumn = testColumn;
    }

    /**
     *
     * @inherited <p>
     *
     * Implemented to return true if the match of cell content's String representation against the Pattern if found and the adapter's view
     * column maps to the decorateColumn/s. Otherwise returns false.
     *
     */
    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        if (isHighlightCandidate(adapter)) {
            return test(adapter);
        }
        return false;
    }

    /**
     * Test the value. This is called only if the pre-check returned true, because accessing the value might be potentially costly
     *
     * @param adapter
     * @return
     */
    private boolean test(ComponentAdapter adapter) {
        // test all
        Object value = null;
        for (int column = 0; column < testColumn.length; column++) {
            if (value == null) {
                value = adapter.getValue(testColumn[column]);
            } else if (!value.equals(adapter.getValue(testColumn[column]))) {
                return false;
            }
        }
        return true;
    }

    /**
     * A quick pre-check.
     *
     * @param adapter
     *
     * @return
     */
    private boolean isHighlightCandidate(ComponentAdapter adapter) {
        return true;
    }

    /**
     *
     * @return returns the column index to decorate (in model coordinates)
     */
    public int getHighlightColumn() {
        return highlightColumn;
    }

    /**
     *
     * @return returns the Pattern to test the cell value against
     */
    public Pattern getPattern() {
        return pattern;
    }
}
