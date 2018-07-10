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
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.models.TroopsTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.TroopTableTab;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.PropertyHelper;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Torridity
 */
public class DSWorkbenchTroopsFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ActionListener, DataHolderListener {

  @Override
  public void fireDataHolderEvent(String eventMessage) {
  }

  @Override
  public void fireDataLoadedEvent(boolean pSuccess) {
    if (pSuccess) {
      resetView();
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    TroopTableTab activeTab = getActiveTab();
    if (e.getActionCommand().equals("Delete")) {
      if (activeTab != null) {
        activeTab.deleteSelection();
      }
    } else if (e.getActionCommand().equals("BBCopy")) {
      if (activeTab != null) {
        activeTab.transferSelection(TroopTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
      }
    } else if (e.getActionCommand().equals("Find")) {
      BufferedImage back = ImageUtils.createCompatibleBufferedImage(3, 3, BufferedImage.TRANSLUCENT);
      Graphics g = back.getGraphics();
      g.setColor(new Color(120, 120, 120, 120));
      g.fillRect(0, 0, back.getWidth(), back.getHeight());
      g.setColor(new Color(120, 120, 120));
      g.drawLine(0, 0, 3, 3);
      g.dispose();
      TexturePaint paint = new TexturePaint(back, new Rectangle2D.Double(0, 0, back.getWidth(), back.getHeight()));
      jxSearchPane.setBackgroundPainter(new MattePainter(paint));
      updateTagList();
      jxSearchPane.setVisible(true);
    } else if (e.getActionCommand() != null && activeTab != null) {
      if (e.getActionCommand().equals("SelectionDone")) {
        activeTab.updateSelectionInfo();
      }
    }
  }
  private static Logger logger = LogManager.getLogger("TroopsDialog");
  private static DSWorkbenchTroopsFrame SINGLETON = null;
  private GenericTestPanel centerPanel = null;

  public static synchronized DSWorkbenchTroopsFrame getSingleton() {
    if (SINGLETON == null) {
      SINGLETON = new DSWorkbenchTroopsFrame();
    }
    return SINGLETON;
  }

  /**
   * Creates new form DSWorkbenchTroopsFrame
   */
  DSWorkbenchTroopsFrame() {
    initComponents();

    centerPanel = new GenericTestPanel(true);
    jTroopsPanel.add(centerPanel, BorderLayout.CENTER);
    centerPanel.setChildComponent(jXTroopsPanel);
    buildMenu();
    capabilityInfoPanel1.addActionListener(this);

    jTroopsTabPane.getModel().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        TroopTableTab activeTab = getActiveTab();
        if (activeTab != null) {
          activeTab.updateSet();
        }
      }
    });

    DataHolder.getSingleton().addDataHolderListener(DSWorkbenchTroopsFrame.this);

    jXGroupsList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateFilter();
      }
    });

    jTroopAddTribe.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          updateTroopAddVillageList();
        }
      }
    });
    jTroopsAddDialog.pack();


    // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
    if (!Constants.DEBUG) {
      GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.troops_view", GlobalOptions.getHelpBroker().getHelpSet());
    }
    // </editor-fold>
    setGlassPane(jxSearchPane);
    pack();
  }

  @Override
  public void toBack() {
    jTroopsInformationAlwaysOnTop.setSelected(false);
    fireTroopsFrameOnTopEvent(null);
    super.toBack();
  }

  @Override
  public void storeCustomProperties(Configuration pConfig) {
    pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
    pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jTroopsInformationAlwaysOnTop.isSelected());

    int selectedIndex = jTroopsTabPane.getModel().getSelectedIndex();
    if (selectedIndex >= 0) {
      pConfig.setProperty(getPropertyPrefix() + ".tab.selection", selectedIndex);
    }


    TroopTableTab tab = ((TroopTableTab) jTroopsTabPane.getComponentAt(0));
    PropertyHelper.storeTableProperties(tab.getTroopTable(), pConfig, getPropertyPrefix());
  }

  @Override
  public void restoreCustomProperties(Configuration pConfig) {
    centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
    try {
      jTroopsTabPane.setSelectedIndex(pConfig.getInteger(getPropertyPrefix() + ".tab.selection", 0));
    } catch (Exception ignored) {
    }
    try {
      jTroopsInformationAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
    } catch (Exception ignored) {
    }

    setAlwaysOnTop(jTroopsInformationAlwaysOnTop.isSelected());

    TroopTableTab tab = ((TroopTableTab) jTroopsTabPane.getComponentAt(0));
    PropertyHelper.restoreTableProperties(tab.getTroopTable(), pConfig, getPropertyPrefix());
  }

  @Override
  public String getPropertyPrefix() {
    return "troops.view";
  }

  private void buildMenu() {

    JXTaskPane editTaskPane = new JXTaskPane();
    editTaskPane.setTitle("Bearbeiten");

    JXButton createTroopInfo = new JXButton(new ImageIcon(DSWorkbenchTroopsFrame.class.getResource("/res/ui/troop_info_new.png")));
    createTroopInfo.setToolTipText("<html>Truppeninformationen für einzelne D&ouml;rfer manuell einf&uuml;gen.<br/>"
            + "Die eingef&uuml;gten Informationen beziehen sich nur auf die aktuell gew&auml;hlte Ansicht.<br/>"
            + "Unterst&uuml;tzungen k&ouml;nnen auf diese Weise <b>nicht</b> eingef&uuml;gt werden.</html>");
    createTroopInfo.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        TroopTableTab tab = (TroopTableTab) getActiveTab();
        if (tab.getTroopSet().equals(TroopsManager.SUPPORT_GROUP)) {
          tab.showError("Diese Funktion ist für Unterstützungen nicht verfügbar");
          return;
        }
        addTroopsManuallyEvent();
        /*  Village[] va = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
         TroopsManager.getSingleton().addManagedElement(null);
        
         for (Village v : va) {
         VillageTroopsHolder h = new VillageTroopsHolder(v, new Date());
         for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
         h.setAmountForUnit(u, 1000);
         TroopsManager.getSingleton().addManagedElement(TroopsManager.OWN_GROUP, h);
         }
         }*/

      }
    });
    editTaskPane.getContentPane().add(createTroopInfo);

    JXTaskPane transferTaskPane = new JXTaskPane();

    transferTaskPane.setTitle("Übertragen");
    JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchTroopsFrame.class.getResource("/res/ui/center_ingame.png")));

    transferVillageList.setToolTipText("Zentriert das gewählte Dorf im Spiel");
    transferVillageList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        TroopTableTab tab = getActiveTab();
        if (tab != null) {
          tab.centerVillageInGame();
        }
      }
    });
    transferTaskPane.getContentPane().add(transferVillageList);

    JXButton openPlace = new JXButton(new ImageIcon(DSWorkbenchTroopsFrame.class.getResource("/res/ui/place.png")));

    openPlace.setToolTipText("Öffnet den Versammlungsplatz des gewählten Dorfes im Spiel");
    openPlace.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        TroopTableTab tab = getActiveTab();
        if (tab != null) {
          tab.openPlaceInGame();
        }
      }
    });
    openPlace.setSize(transferVillageList.getSize());
    openPlace.setMinimumSize(transferVillageList.getMinimumSize());
    openPlace.setMaximumSize(transferVillageList.getMaximumSize());
    openPlace.setPreferredSize(transferVillageList.getPreferredSize());
    transferTaskPane.getContentPane().add(openPlace);
    if (!GlobalOptions.isMinimal()) {
      JXButton centerVillage = new JXButton(new ImageIcon(DSWorkbenchTroopsFrame.class.getResource("/res/center_24x24.png")));
      centerVillage.setToolTipText("Zentriert das gewählte Dorf auf der Hauptkarte");
      centerVillage.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
          TroopTableTab tab = getActiveTab();
          if (tab != null) {
            tab.centerVillage();
          }
        }
      });

      transferTaskPane.getContentPane().add(centerVillage);
    }
    JXTaskPane miscPane = new JXTaskPane();

    miscPane.setTitle("Sonstiges");
    JXButton selectionDetailsButton = new JXButton(new ImageIcon(DSWorkbenchTroopsFrame.class.getResource("/res/ui/information.png")));

    selectionDetailsButton.setToolTipText("Zeigt Details zu den gewählten Dörfern an");
    selectionDetailsButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        TroopTableTab tab = (TroopTableTab) getActiveTab();
        if (tab != null) {
          tab.showSelectionDetails();
        }
      }
    });
    selectionDetailsButton.setSize(transferVillageList.getSize());
    selectionDetailsButton.setMinimumSize(transferVillageList.getMinimumSize());
    selectionDetailsButton.setMaximumSize(transferVillageList.getMaximumSize());
    selectionDetailsButton.setPreferredSize(transferVillageList.getPreferredSize());

    miscPane.getContentPane().add(selectionDetailsButton);

    centerPanel.setupTaskPane(editTaskPane, transferTaskPane, miscPane);
  }

  /**
   * Get the currently selected tab
   */
  private TroopTableTab getActiveTab() {
    try {
      if (jTroopsTabPane.getModel().getSelectedIndex() < 0) {
        return null;
      }
      return ((TroopTableTab) jTroopsTabPane.getComponentAt(jTroopsTabPane.getModel().getSelectedIndex()));
    } catch (ClassCastException cce) {
      return null;
    }
  }

  private void updateTagList() {
    DefaultListModel m = new DefaultListModel();
    m.addElement(NoTag.getSingleton());
    for (ManageableType t : TagManager.getSingleton().getAllElements()) {
      Tag ta = (Tag) t;
      m.addElement(ta);
    }
    jXGroupsList.setModel(m);
  }

  /**
   * Initialize and add one tab for each marker set to jTabbedPane1
   */
  public void generateTroopTabs() {
    if (jTroopsTabPane.getTabCount() == 0) {
      jTroopsTabPane.invalidate();
      String[] sets = TroopsManager.getSingleton().getGroups();

      //insert default tab to first place
      int cnt = 0;
      for (String set : sets) {
        TroopTableTab tab = new TroopTableTab(set, DSWorkbenchTroopsFrame.this);
        if (cnt == 0) {
          ((TroopsTableModel) tab.getTroopTable().getModel()).fireTableStructureChanged();
        }
        jTroopsTabPane.addTab(set, tab);

        cnt++;
      }

      jTroopsTabPane.revalidate();
    }
    TroopTableTab tab = getActiveTab();
    if (tab != null) {
      tab.updateSet();
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jXTroopsPanel = new org.jdesktop.swingx.JXPanel();
        jTroopsTabPane = new javax.swing.JTabbedPane();
        jxSearchPane = new org.jdesktop.swingx.JXPanel();
        jXPanel2 = new org.jdesktop.swingx.JXPanel();
        jButton12 = new javax.swing.JButton();
        jFilterRows = new javax.swing.JCheckBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        jXGroupsList = new org.jdesktop.swingx.JXList();
        jLabel22 = new javax.swing.JLabel();
        jRelationType1 = new javax.swing.JCheckBox();
        jTroopsAddDialog = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTroopAddTribe = new javax.swing.JComboBox();
        jTroopAddVillage = new javax.swing.JComboBox();
        jApplyTroopAddButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTroopsInformationAlwaysOnTop = new javax.swing.JCheckBox();
        jTroopsPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jXTroopsPanel.setMinimumSize(new java.awt.Dimension(700, 500));
        jXTroopsPanel.setPreferredSize(new java.awt.Dimension(700, 500));
        jXTroopsPanel.setLayout(new java.awt.BorderLayout());
        jXTroopsPanel.add(jTroopsTabPane, java.awt.BorderLayout.CENTER);

        jxSearchPane.setOpaque(false);
        jxSearchPane.setLayout(new java.awt.GridBagLayout());

        jXPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jXPanel2.setInheritAlpha(false);

        jButton12.setText("Anwenden");
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideGlassPaneEvent(evt);
            }
        });

        jFilterRows.setText("Nur gefilterte Zeilen anzeigen");
        jFilterRows.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jFilterRowsfireUpdateFilterEvent(evt);
            }
        });

        jXGroupsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane4.setViewportView(jXGroupsList);

        jLabel22.setText("Gruppen");

        jRelationType1.setSelected(true);
        jRelationType1.setText("Verknüpfung");
        jRelationType1.setToolTipText("Verknüpfung der gewählten Dorfgruppen (UND = Dorf muss in allen Gruppen sein, ODER = Dorf muss in mindestens einer Gruppe sein)");
        jRelationType1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jRelationType1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_or.png"))); // NOI18N
        jRelationType1.setRolloverEnabled(false);
        jRelationType1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_and.png"))); // NOI18N
        jRelationType1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireRelationChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jXPanel2Layout = new javax.swing.GroupLayout(jXPanel2);
        jXPanel2.setLayout(jXPanel2Layout);
        jXPanel2Layout.setHorizontalGroup(
            jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel22)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jRelationType1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                    .addComponent(jFilterRows, javax.swing.GroupLayout.PREFERRED_SIZE, 215, Short.MAX_VALUE)
                    .addComponent(jButton12))
                .addContainerGap())
        );
        jXPanel2Layout.setVerticalGroup(
            jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jXPanel2Layout.createSequentialGroup()
                        .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addComponent(jRelationType1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFilterRows)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton12))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jxSearchPane.add(jXPanel2, new java.awt.GridBagConstraints());

        jTroopsAddDialog.setTitle("Truppen hinzufügen");
        jTroopsAddDialog.setModal(true);
        jTroopsAddDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Spieler");
        jLabel1.setMaximumSize(new java.awt.Dimension(60, 14));
        jLabel1.setMinimumSize(new java.awt.Dimension(60, 14));
        jLabel1.setPreferredSize(new java.awt.Dimension(60, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopsAddDialog.getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setText("Dorf");
        jLabel2.setMaximumSize(new java.awt.Dimension(60, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(60, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(60, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopsAddDialog.getContentPane().add(jLabel2, gridBagConstraints);

        jTroopAddTribe.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jTroopAddTribe.setMinimumSize(new java.awt.Dimension(200, 25));
        jTroopAddTribe.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopsAddDialog.getContentPane().add(jTroopAddTribe, gridBagConstraints);

        jTroopAddVillage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jTroopAddVillage.setMinimumSize(new java.awt.Dimension(200, 20));
        jTroopAddVillage.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopsAddDialog.getContentPane().add(jTroopAddVillage, gridBagConstraints);

        jApplyTroopAddButton.setText("Hinzufügen");
        jApplyTroopAddButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireApplyTroopAddEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopsAddDialog.getContentPane().add(jApplyTroopAddButton, gridBagConstraints);

        jButton2.setText("Abbrechen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireApplyTroopAddEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jTroopsAddDialog.getContentPane().add(jButton2, gridBagConstraints);

        setTitle("Truppen");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jTroopsInformationAlwaysOnTop.setText("Immer im Vordergrund");
        jTroopsInformationAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireTroopsFrameOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jTroopsInformationAlwaysOnTop, gridBagConstraints);

        jTroopsPanel.setBackground(new java.awt.Color(239, 235, 223));
        jTroopsPanel.setMinimumSize(new java.awt.Dimension(700, 500));
        jTroopsPanel.setPreferredSize(new java.awt.Dimension(700, 500));
        jTroopsPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jTroopsPanel, gridBagConstraints);

        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setPastable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireTroopsFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireTroopsFrameOnTopEvent
  setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireTroopsFrameOnTopEvent

private void fireHideGlassPaneEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideGlassPaneEvent
  jxSearchPane.setBackgroundPainter(null);
  jxSearchPane.setVisible(false);
}//GEN-LAST:event_fireHideGlassPaneEvent

private void jFilterRowsfireUpdateFilterEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jFilterRowsfireUpdateFilterEvent
  updateFilter();
}//GEN-LAST:event_jFilterRowsfireUpdateFilterEvent

private void fireRelationChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireRelationChangedEvent
    updateFilter();
}//GEN-LAST:event_fireRelationChangedEvent

private void fireApplyTroopAddEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireApplyTroopAddEvent
  if (evt.getSource().equals(jApplyTroopAddButton)) {
    TroopTableTab tab = (TroopTableTab) getActiveTab();
    if (tab != null) {
      Village v = null;
      try {
        v = (Village) jTroopAddVillage.getSelectedItem();
      } catch (Exception e) {
        tab.showError("Kein gültiges Dorf gewählt");
        return;
      }
      TroopsManager.getSingleton().addManagedElement(tab.getTroopSet(), new VillageTroopsHolder(v, new Date()));
    }
  }
  jTroopsAddDialog.setVisible(false);
}//GEN-LAST:event_fireApplyTroopAddEvent

  private void addTroopsManuallyEvent() {
    HashMap<Integer, Tribe> tribes = DataHolder.getSingleton().getTribes();
    
    List<Tribe> tribesList = new LinkedList<>();
    for(Tribe t: tribes.values()) {
        tribesList.add(t);
    }
    Collections.sort(tribesList, Tribe.CASE_INSENSITIVE_ORDER);

    DefaultComboBoxModel model = new DefaultComboBoxModel(tribesList.toArray(new Tribe[tribesList.size()]));
    jTroopAddTribe.setModel(model);
    model.setSelectedItem(GlobalOptions.getSelectedProfile().getTribe());
    jTroopsAddDialog.setLocationRelativeTo(DSWorkbenchTroopsFrame.this);
    jTroopsAddDialog.setVisible(true);
  }

  private void updateTroopAddVillageList() {
    Tribe t = (Tribe) jTroopAddTribe.getSelectedItem();
    if (t != null) {
      Village[] villageList = t.getVillageList();
      Arrays.sort(villageList);
      jTroopAddVillage.setModel(new DefaultComboBoxModel(villageList));
    } else {
      jTroopAddVillage.setModel(new DefaultComboBoxModel(new String[]{"Bitte Spieler wählen"}));
    }
  }

  /**
   * Update the troop set filter
   */
  private void updateFilter() {
    TroopTableTab tab = getActiveTab();
    if (tab != null) {
      final List<Tag> selection = new LinkedList<>();
      for (Object o : jXGroupsList.getSelectedValues()) {
        selection.add((Tag) o);
      }
      if (!selection.isEmpty()) {
        tab.updateFilter(selection, jRelationType1.isSelected(), jFilterRows.isSelected());
      }
    }
  }

  public List<Village> getSelectedSupportVillages() {
    TroopTableTab tab = (TroopTableTab) getActiveTab();
    if (tab != null && tab.getTroopSet().equals(TroopsManager.SUPPORT_GROUP)) {
      return tab.getSelectedVillages();
    }
    return new LinkedList<>();
  }

  @Override
  public void resetView() {
    TroopsManager.getSingleton().addManagerListener(this);
    generateTroopTabs();
  }

  // <editor-fold defaultstate="collapsed" desc="Gesture handling">
  @Override
  public void fireExportAsBBGestureEvent() {
    TroopTableTab tab = getActiveTab();
    if (tab != null) {
      tab.transferSelection(TroopTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
    }
  }

  @Override
  public void fireNextPageGestureEvent() {
    int current = jTroopsTabPane.getSelectedIndex();
    int size = jTroopsTabPane.getTabCount();
    if (current + 1 > size - 1) {
      current = 0;
    } else {
      current += 1;
    }
    jTroopsTabPane.setSelectedIndex(current);
  }

  @Override
  public void firePreviousPageGestureEvent() {
    int current = jTroopsTabPane.getSelectedIndex();
    int size = jTroopsTabPane.getTabCount();
    if (current - 1 < 0) {
      current = size - 1;
    } else {
      current -= 1;
    }
    jTroopsTabPane.setSelectedIndex(current);
  }
// </editor-fold>
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JButton jApplyTroopAddButton;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jFilterRows;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JCheckBox jRelationType1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JComboBox jTroopAddTribe;
    private javax.swing.JComboBox jTroopAddVillage;
    private javax.swing.JDialog jTroopsAddDialog;
    private javax.swing.JCheckBox jTroopsInformationAlwaysOnTop;
    private javax.swing.JPanel jTroopsPanel;
    private javax.swing.JTabbedPane jTroopsTabPane;
    private org.jdesktop.swingx.JXList jXGroupsList;
    private org.jdesktop.swingx.JXPanel jXPanel2;
    private org.jdesktop.swingx.JXPanel jXTroopsPanel;
    private org.jdesktop.swingx.JXPanel jxSearchPane;
    // End of variables declaration//GEN-END:variables

  @Override
  public void dataChangedEvent() {
    generateTroopTabs();
  }

  @Override
  public void dataChangedEvent(String pGroup) {
    TroopTableTab tab = getActiveTab();
    if (tab != null) {
      tab.updateSet();
    }
  }

  @Override
  public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
  }
}
