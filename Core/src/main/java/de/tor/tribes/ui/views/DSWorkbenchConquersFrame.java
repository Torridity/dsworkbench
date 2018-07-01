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
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.models.ConquerTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.*;
import de.tor.tribes.util.conquer.ConquerManager;
import java.awt.*;
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
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.*;
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
    private static Logger logger = LogManager.getLogger("ConquerView");
    private static DSWorkbenchConquersFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    private PainterHighlighter highlighter = null;

    DSWorkbenchConquersFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jConquerPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jConquersPanel);
        buildMenu();
        
        jConquersFrameAlwaysOnTop.setSelected(GlobalOptions.getProperties().getBoolean("conquers.frame.alwaysOnTop"));
            setAlwaysOnTop(jConquersFrameAlwaysOnTop.isSelected());
        
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

    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jConquersFrameAlwaysOnTop.isSelected());
        PropertyHelper.storeTableProperties(jConquersTable, pConfig, getPropertyPrefix());

    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));

        try {
            jConquersFrameAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jConquersFrameAlwaysOnTop.isSelected());

        PropertyHelper.restoreTableProperties(jConquersTable, pConfig, getPropertyPrefix());
    }

    @Override
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
        jSelfConquersLabel = new javax.swing.JLabel();
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

        jSelfConquersLabel.setBackground(new java.awt.Color(213, 255, 128));
        jSelfConquersLabel.setText("Selbstadelungen:");
        jSelfConquersLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jXPanel2.add(jSelfConquersLabel, gridBagConstraints);

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
        jFilterRows.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jFilterRowsfireUpdateFilterEvent(evt);
            }
        });

        jFilterCaseSensitive.setText("Groß-/Kleinschreibung beachten");
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
        final List<String> columns = new LinkedList<>();
        for (Object o : jXColumnList.getSelectedValues()) {
            columns.add((String) o);
        }
        if (!jFilterRows.isSelected()) {
            jConquersTable.setRowFilter(null);
            final List<Integer> relevantCols = new LinkedList<>();
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
                    final List<Integer> relevantCols = new LinkedList<>();
                    List<TableColumn> cols = jConquersTable.getColumns(true);
                    for (int i = 0; i < jConquersTable.getColumnCount(); i++) {
                        TableColumnExt col = jConquersTable.getColumnExt(i);
                        if (col.isVisible() && columns.contains(col.getTitle())) {
                            relevantCols.add(cols.indexOf(col));
                        }
                    }

                    for (Integer col : relevantCols) {
                        if (jFilterCaseSensitive.isSelected()) {
                            if (entry.getStringValue(col).contains(jTextField1.getText())) {
                                return true;
                            }
                        } else {
                            if (entry.getStringValue(col).toLowerCase().contains(jTextField1.getText().toLowerCase())) {
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
        BrowserInterface.centerVillage(selection.get(0).getVillage());
    }

    @Override
    public void resetView() {
        ConquerManager.getSingleton().addManagerListener(this);
        //update view
        jConquersTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        UIHelper.initTableColums(jConquersTable, "Dorfpunkte", "Kontinent", "Entfernung");
        UIHelper.initTableColums(jConquersTable, 90, "Zustimmung");

        ((ConquerTableModel) jConquersTable.getModel()).fireTableDataChanged();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    private List<Conquer> getSelectedConquers() {
        final List<Conquer> elements = new LinkedList<>();
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
    private javax.swing.JLabel jSelfConquersLabel;
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
        ColorHighlighter p = new ColorHighlighter(new ConquerPredicate(ConquerPredicate.PType.BARBARIAN), Color.PINK, Color.BLACK);
        ColorHighlighter p1 = new ColorHighlighter(new ConquerPredicate(ConquerPredicate.PType.ALLY), Color.CYAN, Color.BLACK);
        ColorHighlighter p2 = new ColorHighlighter(new ConquerPredicate(ConquerPredicate.PType.OWN), new Color(0xD5FF80), Color.BLACK);
        jConquersTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B), p, p1, p2);
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
        int percSelf = (int) Math.rint(100.0 * (double) conquerStats[2] / (double) conquers);

        jGreyConquersLabel.setText("<html><b>Grau-Adelungen:</b> " + conquerStats[0] + " von " + conquers + " (" + percGrey + "%)" + "</html>");
        jFriendlyConquersLabel.setText("<html><b>Aufadelungen:</b> " + conquerStats[1] + " von " + conquers + " (" + percFriendly + "%)" + "</html>");
        jSelfConquersLabel.setText("<html><b>Selbstadelungen:</b> " + conquerStats[2] + " von " + conquers + " (" + percSelf + "%)" + "</html>");
        ((ConquerTableModel) jConquersTable.getModel()).fireTableDataChanged();
    }
}

class ConquerPredicate implements HighlightPredicate {
    public enum PType {
        BARBARIAN, ALLY, OWN
    }

    private final PType type;
    
    public ConquerPredicate(PType t) {
        this.type = t;
    }
    
    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        switch(type) {
            case BARBARIAN:
                return adapter.getValue(4).equals("Barbaren");
            case ALLY:
                //Ally same, but tribes not
                return adapter.getValue(5).equals(adapter.getValue(7))
                        && !adapter.getValue(4).equals(adapter.getValue(6));
            case OWN:
                //Tribes are the same
                return adapter.getValue(4).equals(adapter.getValue(6));
        }
        return false;
    }
}
