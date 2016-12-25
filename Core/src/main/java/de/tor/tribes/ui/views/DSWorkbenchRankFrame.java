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

import com.jidesoft.swing.JideTabbedPane;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.RankTableTab;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.PropertyHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author Charon
 */
public class DSWorkbenchRankFrame extends AbstractDSWorkbenchFrame implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        RankTableTab activeTab = getActiveTab();
        if (e.getActionCommand() != null && activeTab != null) {
            if (e.getActionCommand().equals("Find")) {
                BufferedImage back = ImageUtils.createCompatibleBufferedImage(3, 3, BufferedImage.TRANSLUCENT);
                Graphics g = back.getGraphics();
                g.setColor(new Color(120, 120, 120, 120));
                g.fillRect(0, 0, back.getWidth(), back.getHeight());
                g.setColor(new Color(120, 120, 120));
                g.drawLine(0, 0, 3, 3);
                g.dispose();
                TexturePaint paint = new TexturePaint(back, new Rectangle2D.Double(0, 0, back.getWidth(), back.getHeight()));
                jxSearchPane.setBackgroundPainter(new MattePainter(paint));
                DefaultListModel model = new DefaultListModel();
                
                for (int i = 0; i < activeTab.getRankTable().getColumnCount(); i++) {
                    TableColumnExt col = activeTab.getRankTable().getColumnExt(i);
                    if (col.isVisible()) {
                        if (col.getTitle().equals("Name") || col.getTitle().equals("Tag") || col.getTitle().equals("Stamm")) {
                            model.addElement(col.getTitle());
                        }
                        
                    }
                }
                jXColumnList.setModel(model);
                jXColumnList.setSelectedIndex(0);
                jxSearchPane.setVisible(true);
            }
        }
        
    }
    private static Logger logger = Logger.getLogger("RankDialog");
    private static DSWorkbenchRankFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    
    public static synchronized DSWorkbenchRankFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchRankFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchRankFrame */
    DSWorkbenchRankFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jRankPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jXRankPanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this);
        jRankTabPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jRankTabPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jRankTabPane.setBoldActiveTab(true);
        jRankTabPane.getModel().addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                RankTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.updateTab();
                }
            }
        });
        jXColumnList.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateFilter();
            }
        });
        setGlassPane(jxSearchPane);
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.ranking_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
// </editor-fold>
        // updateRankTable();
    }
    
    @Override
    public void toBack() {
        jAlwaysOnTop.setSelected(false);
        fireRankFrameAlwaysOnTopEvent(null);
        super.toBack();
    }
    
    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTop.isSelected());
        
        int selectedIndex = jRankTabPane.getModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            pConfig.setProperty(getPropertyPrefix() + ".tab.selection", selectedIndex);
        }
        
        
        RankTableTab tab = ((RankTableTab) jRankTabPane.getComponentAt(0));
        PropertyHelper.storeTableProperties(tab.getRankTable(), pConfig, getPropertyPrefix());
    }
    
    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        try {
            jRankTabPane.setSelectedIndex(pConfig.getInteger(getPropertyPrefix() + ".tab.selection", 0));
        } catch (Exception ignored) {
        }
        try {
            jAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }
        
        setAlwaysOnTop(jAlwaysOnTop.isSelected());
        
        RankTableTab tab = ((RankTableTab) jRankTabPane.getComponentAt(0));
        PropertyHelper.restoreTableProperties(tab.getRankTable(), pConfig, getPropertyPrefix());
    }
    
    public String getPropertyPrefix() {
        return "rank.view";
    }
    
    private void buildMenu() {
        JXTaskPane statsTaskPane = new JXTaskPane();
        statsTaskPane.setTitle("Statistiken");
        JXButton openDSRealButton = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/dsreal.png")));
        openDSRealButton.setToolTipText("DS Real Seite im Browser öffnen");
        openDSRealButton.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                RankTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.openDSReal();
                }
            }
        });
        statsTaskPane.getContentPane().add(openDSRealButton);
        JXButton dsRealStatsButton = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/chart.png")));
        dsRealStatsButton.setToolTipText("DS Real Verlaufsgrafik anzeigen");
        dsRealStatsButton.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                RankTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.showDSRealChart();
                }
            }
        });
        statsTaskPane.getContentPane().add(dsRealStatsButton);
        
        centerPanel.setupTaskPane(statsTaskPane);
    }
    
    public void resetView() {
        generateTabs();
    }

    /**Get the currently selected tab*/
    private RankTableTab getActiveTab() {
        try {
            if (jRankTabPane.getModel().getSelectedIndex() < 0) {
                return null;
            }
            return ((RankTableTab) jRankTabPane.getComponentAt(jRankTabPane.getModel().getSelectedIndex()));
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**Initialize and add one tab for each attack plan to jTabbedPane1*/
    public void generateTabs() {
        jRankTabPane.invalidate();
        while (jRankTabPane.getTabCount() > 0) {
            RankTableTab tab = (RankTableTab) jRankTabPane.getComponentAt(0);
            tab.deregister();
            jRankTabPane.removeTabAt(0);
        }
        
        RankTableTab tribeTab = new RankTableTab(RankTableTab.RANK_TYPE.TRIBE, this);
        jRankTabPane.addTab("Spieler", tribeTab);
        RankTableTab allyTab = new RankTableTab(RankTableTab.RANK_TYPE.ALLY, this);
        jRankTabPane.addTab("Stämme", allyTab);
        RankTableTab tribeBashTab = new RankTableTab(RankTableTab.RANK_TYPE.TRIBE_BASH, this);
        jRankTabPane.addTab("Spieler (Bash)", tribeBashTab);
        RankTableTab allyBashTab = new RankTableTab(RankTableTab.RANK_TYPE.ALLY_BASH, this);
        jRankTabPane.addTab("Stämme (Bash)", allyBashTab);
        jRankTabPane.revalidate();
        RankTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateTab();
        }
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

        jXRankPanel = new org.jdesktop.swingx.JXPanel();
        jRankTabPane = new com.jidesoft.swing.JideTabbedPane();
        jxSearchPane = new org.jdesktop.swingx.JXPanel();
        jXPanel2 = new org.jdesktop.swingx.JXPanel();
        jButton12 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jFilterRows = new javax.swing.JCheckBox();
        jFilterCaseSensitive = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXColumnList = new org.jdesktop.swingx.JXList();
        jLabel22 = new javax.swing.JLabel();
        jAlwaysOnTop = new javax.swing.JCheckBox();
        jRankPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jXRankPanel.setLayout(new java.awt.BorderLayout());

        jRankTabPane.setScrollSelectedTabOnWheel(true);
        jRankTabPane.setShowGripper(true);
        jRankTabPane.setTabEditingAllowed(true);
        jXRankPanel.add(jRankTabPane, java.awt.BorderLayout.CENTER);

        jxSearchPane.setOpaque(false);
        jxSearchPane.setLayout(new java.awt.GridBagLayout());

        jXPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jXPanel2.setInheritAlpha(false);

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
        jScrollPane2.setViewportView(jXColumnList);

        jLabel22.setText("Spalten");

        javax.swing.GroupLayout jXPanel2Layout = new javax.swing.GroupLayout(jXPanel2);
        jXPanel2.setLayout(jXPanel2Layout);
        jXPanel2Layout.setHorizontalGroup(
            jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jXPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jFilterRows, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jFilterCaseSensitive, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton12)))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jXPanel2Layout.setVerticalGroup(
            jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jXPanel2Layout.createSequentialGroup()
                            .addComponent(jFilterCaseSensitive)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jFilterRows)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton12))
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel22))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jxSearchPane.add(jXPanel2, new java.awt.GridBagConstraints());

        setTitle("Rangliste");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAlwaysOnTop.setText("Immer im Vordergrund");
        jAlwaysOnTop.setOpaque(false);
        jAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireRankFrameAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTop, gridBagConstraints);

        jRankPanel.setBackground(new java.awt.Color(239, 235, 223));
        jRankPanel.setPreferredSize(new java.awt.Dimension(300, 400));
        jRankPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 668;
        gridBagConstraints.ipady = 471;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jRankPanel, gridBagConstraints);

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

    private void fireRankFrameAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireRankFrameAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireRankFrameAlwaysOnTopEvent
    
    private void jButton12fireHideGlassPaneEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton12fireHideGlassPaneEvent
        jxSearchPane.setBackgroundPainter(null);
        jxSearchPane.setVisible(false);
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
    /**Update the attack plan filter*/
    private void updateFilter() {
        RankTableTab tab = getActiveTab();
        if (tab != null) {
            final List<String> selection = new LinkedList<>();
            for (Object o : jXColumnList.getSelectedValues()) {
                selection.add((String) o);
            }
            tab.updateFilter(jTextField1.getText(), selection, jFilterCaseSensitive.isSelected(), jFilterRows.isSelected());
        }
    }
    
    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    
    public static void main(String[] args) {
        GlobalOptions.setSelectedServer("de43");
        DataHolder.getSingleton().loadData(false);
        MouseGestures mMouseGestures = new MouseGestures();
        mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
        mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
        mMouseGestures.start();
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        DSWorkbenchRankFrame.getSingleton().setSize(800, 600);
        
        
        
        DSWorkbenchRankFrame.getSingleton().resetView();
        DSWorkbenchRankFrame.getSingleton().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        DSWorkbenchRankFrame.getSingleton().setVisible(true);
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JCheckBox jAlwaysOnTop;
    private javax.swing.JButton jButton12;
    private javax.swing.JCheckBox jFilterCaseSensitive;
    private javax.swing.JCheckBox jFilterRows;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JPanel jRankPanel;
    private com.jidesoft.swing.JideTabbedPane jRankTabPane;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private org.jdesktop.swingx.JXList jXColumnList;
    private org.jdesktop.swingx.JXPanel jXPanel2;
    private org.jdesktop.swingx.JXPanel jXRankPanel;
    private org.jdesktop.swingx.JXPanel jxSearchPane;
    // End of variables declaration//GEN-END:variables
}
