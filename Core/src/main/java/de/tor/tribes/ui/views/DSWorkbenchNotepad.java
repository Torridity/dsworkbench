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
import com.jidesoft.swing.TabEditingEvent;
import com.jidesoft.swing.TabEditingListener;
import com.jidesoft.swing.TabEditingValidator;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.NoteTableTab;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PropertyHelper;
import de.tor.tribes.util.note.NoteManager;
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
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Charon
 */
public class DSWorkbenchNotepad extends AbstractDSWorkbenchFrame implements GenericManagerListener, ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        NoteTableTab activeTab = getActiveTab();
        if (e.getActionCommand() != null && activeTab != null) {
            if (e.getActionCommand().equals("Copy")) {
                activeTab.transferSelection(NoteTableTab.TRANSFER_TYPE.COPY_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("BBCopy")) {
                activeTab.transferSelection(NoteTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
            } else if (e.getActionCommand().equals("BBCopy_Village")) {
                activeTab.copyVillagesAsBBCodes();
            } else if (e.getActionCommand().equals("Cut")) {
                activeTab.transferSelection(NoteTableTab.TRANSFER_TYPE.CUT_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Paste")) {
                activeTab.transferSelection(NoteTableTab.TRANSFER_TYPE.FROM_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Delete")) {
                activeTab.deleteSelection(true);
            } else if (e.getActionCommand().equals("Delete_Village")) {
                activeTab.deleteVillagesFromNotes();
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
                jxSearchPane.setVisible(true);
            }
        }
    }
    
    @Override
    public void dataChangedEvent() {
        generateNoteTabs();
    }
    
    @Override
    public void dataChangedEvent(String pGroup) {
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
        }
    }
    private static Logger logger = LogManager.getLogger("Notepad");
    private static DSWorkbenchNotepad SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    
    public static synchronized DSWorkbenchNotepad getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchNotepad();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchNotepad */
    DSWorkbenchNotepad() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jNotesPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jXNotePanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this);
        jNoteTabbedPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jNoteTabbedPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jNoteTabbedPane.setBoldActiveTab(true);
        jNoteTabbedPane.addTabEditingListener(new TabEditingListener() {
            
            @Override
            public void editingStarted(TabEditingEvent tee) {
            }
            
            @Override
            public void editingStopped(TabEditingEvent tee) {
                NoteManager.getSingleton().renameGroup(tee.getOldTitle(), tee.getNewTitle());
            }
            
            @Override
            public void editingCanceled(TabEditingEvent tee) {
            }
        });
        jNoteTabbedPane.setTabEditingValidator(new TabEditingValidator() {
            
            @Override
            public boolean alertIfInvalid(int tabIndex, String tabText) {
                if (tabText.trim().length() == 0) {
                    JOptionPaneHelper.showWarningBox(jNoteTabbedPane, "'" + tabText + "' ist ein ungültiger Name für ein Notizset", "Fehler");
                    return false;
                }
                
                if (NoteManager.getSingleton().groupExists(tabText)) {
                    JOptionPaneHelper.showWarningBox(jNoteTabbedPane, "Es existiert bereits ein Notizset mit dem Namen '" + tabText + "'", "Fehler");
                    return false;
                }
                return true;
            }
            
            @Override
            public boolean isValid(int tabIndex, String tabText) {
                return tabText.trim().length() != 0 && !NoteManager.getSingleton().groupExists(tabText);

            }
            
            @Override
            public boolean shouldStartEdit(int tabIndex, MouseEvent event) {
                return !(tabIndex == 0);
            }
        });
        jNoteTabbedPane.setCloseAction(new AbstractAction("closeAction") {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                NoteTableTab tab = (NoteTableTab) e.getSource();
                if (JOptionPaneHelper.showQuestionConfirmBox(jNoteTabbedPane, "Das Notizset '" + tab.getNoteSet() + "' und alle darin enthaltenen Notizen wirklich löschen? ", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    NoteManager.getSingleton().removeGroup(tab.getNoteSet());
                }
            }
        });
        
        jNoteTabbedPane.getModel().addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                NoteTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.updateSet();
                }
            }
        });
        
        setGlassPane(jxSearchPane);

        //<editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.notes_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
        //</editor-fold>

    }
    
    @Override
    public void toBack() {
        jAlwaysOnTopBox.setSelected(false);
        fireAlwaysOnTopChangedEvent(null);
        super.toBack();
    }
    
    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());
        
        int selectedIndex = jNoteTabbedPane.getModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            pConfig.setProperty(getPropertyPrefix() + ".tab.selection", selectedIndex);
        }
        
        
        NoteTableTab tab = ((NoteTableTab) jNoteTabbedPane.getComponentAt(0));
        PropertyHelper.storeTableProperties(tab.getNoteTable(), pConfig, getPropertyPrefix());
    }
    
    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        try {
            jNoteTabbedPane.setSelectedIndex(pConfig.getInteger(getPropertyPrefix() + ".tab.selection", 0));
        } catch (Exception ignored) {
        }
        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }
        
        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        
        NoteTableTab tab = ((NoteTableTab) jNoteTabbedPane.getComponentAt(0));
        PropertyHelper.restoreTableProperties(tab.getNoteTable(), pConfig, getPropertyPrefix());
    }
    
    @Override
    public String getPropertyPrefix() {
        return "notes.view";
    }
    
    private void buildMenu() {
        JXTaskPane editTaskPane = new JXTaskPane();
        editTaskPane.setTitle("Bearbeiten");
        
        JXButton newNote = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/document_new_24x24.png")));
        newNote.setToolTipText("Erstellt eine leere Notiz");
        newNote.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                NoteTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.createNote();
                }
            }
        });
        editTaskPane.getContentPane().add(newNote);
        
        
        JXTaskPane transferTaskPane = new JXTaskPane();
        transferTaskPane.setTitle("Übertragen");
        JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/center_ingame.png")));
        transferVillageList.setToolTipText("Zentriert das gewählte Notizdorf im Spiel");
        transferVillageList.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                NoteTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.centerNoteVillageInGame();
                }
            }
        });
        transferTaskPane.getContentPane().add(transferVillageList);
        
        if (!GlobalOptions.isMinimal()) {
            JXButton centerVillage = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/center_24x24.png")));
            centerVillage.setToolTipText("Zentriert das gewählte Notizdorf auf der Hauptkarte");
            centerVillage.setSize(transferVillageList.getSize());
            centerVillage.setMinimumSize(transferVillageList.getMinimumSize());
            centerVillage.setMaximumSize(transferVillageList.getMaximumSize());
            centerVillage.setPreferredSize(transferVillageList.getPreferredSize());
            centerVillage.addMouseListener(new MouseAdapter() {
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    NoteTableTab tab = getActiveTab();
                    if (tab != null) {
                        tab.centerNoteVillage();
                    }
                }
            });
            
            transferTaskPane.getContentPane().add(centerVillage);
        }
        centerPanel.setupTaskPane(editTaskPane, transferTaskPane);
    }

    /**Get the currently selected tab*/
    private NoteTableTab getActiveTab() {
        try {
            if (jNoteTabbedPane.getModel().getSelectedIndex() < 0) {
                return null;
            }
            return ((NoteTableTab) jNoteTabbedPane.getComponentAt(jNoteTabbedPane.getModel().getSelectedIndex()));
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**Initialize and add one tab for each note set to jTabbedPane1*/
    public void generateNoteTabs() {
        jNoteTabbedPane.invalidate();
        while (jNoteTabbedPane.getTabCount() > 0) {
            NoteTableTab tab = (NoteTableTab) jNoteTabbedPane.getComponentAt(0);
            tab.deregister();
            jNoteTabbedPane.removeTabAt(0);
        }
        
        LabelUIResource lr = new LabelUIResource();
        lr.setLayout(new BorderLayout());
        lr.add(jNewSetPanel, BorderLayout.CENTER);
        jNoteTabbedPane.setTabLeadingComponent(lr);
        String[] plans = NoteManager.getSingleton().getGroups();

        //insert default tab to first place
        int cnt = 0;
        for (String plan : plans) {
            NoteTableTab tab = new NoteTableTab(plan, this);
            jNoteTabbedPane.addTab(plan, tab);
            cnt++;
        }
        jNoteTabbedPane.setTabClosableAt(0, false);
        jNoteTabbedPane.revalidate();
        jNoteTabbedPane.setSelectedIndex(0);
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
        }
    }
    
    @Override
    public void resetView() {
        NoteManager.getSingleton().addManagerListener(this);
        generateNoteTabs();
    }
    
    public void addNoteForVillage(Village pVillage) {
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            String set = getActiveTab().getNoteSet();
            
            Note n = new Note();
            n.addVillage(pVillage);
            n.setNoteText("(kein Text)");
            n.setMapMarker(0);
            NoteManager.getSingleton().addManagedElement(set, n);
        }
    }
    
    public void addNoteForVillages(List<Village> pVillages) {
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            String set = getActiveTab().getNoteSet();
            
            Note n = new Note();
            for (Village v : pVillages) {
                n.addVillage(v);
            }
            n.setNoteText("(kein Text)");
            n.setMapMarker(0);
            NoteManager.getSingleton().addManagedElement(set, n);
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

        jXNotePanel = new org.jdesktop.swingx.JXPanel();
        jNoteTabbedPane = new com.jidesoft.swing.JideTabbedPane();
        jNewSetPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jxSearchPane = new org.jdesktop.swingx.JXPanel();
        jXPanel3 = new org.jdesktop.swingx.JXPanel();
        jButton16 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jFilterRows = new javax.swing.JCheckBox();
        jFilterCaseSensitive = new javax.swing.JCheckBox();
        jExportFormatDialog = new javax.swing.JDialog();
        bBPanel1 = new de.tor.tribes.ui.panels.BBPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jNotesPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jXNotePanel.setPreferredSize(new java.awt.Dimension(500, 400));
        jXNotePanel.setLayout(new java.awt.BorderLayout());

        jNoteTabbedPane.setShowCloseButton(true);
        jNoteTabbedPane.setShowCloseButtonOnTab(true);
        jNoteTabbedPane.setShowGripper(true);
        jNoteTabbedPane.setTabEditingAllowed(true);
        jXNotePanel.add(jNoteTabbedPane, java.awt.BorderLayout.CENTER);

        jNewSetPanel.setOpaque(false);
        jNewSetPanel.setLayout(new java.awt.BorderLayout());

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_new_24x24.png"))); // NOI18N
        jLabel4.setToolTipText("Leeres Notizset erstellen");
        jLabel4.setMaximumSize(new java.awt.Dimension(40, 40));
        jLabel4.setMinimumSize(new java.awt.Dimension(40, 40));
        jLabel4.setOpaque(true);
        jLabel4.setPreferredSize(new java.awt.Dimension(40, 40));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireEnterEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCreateNoteSetEvent(evt);
            }
        });
        jNewSetPanel.add(jLabel4, java.awt.BorderLayout.CENTER);

        jxSearchPane.setOpaque(false);
        jxSearchPane.setLayout(new java.awt.GridBagLayout());

        jXPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jXPanel3.setInheritAlpha(false);

        jButton16.setText("Anwenden");
        jButton16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButton16fireHideGlassPaneEvent(evt);
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

        javax.swing.GroupLayout jXPanel3Layout = new javax.swing.GroupLayout(jXPanel3);
        jXPanel3.setLayout(jXPanel3Layout);
        jXPanel3Layout.setHorizontalGroup(
            jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jXPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addGap(18, 18, 18)
                .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel3Layout.createSequentialGroup()
                        .addGap(176, 176, 176)
                        .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jFilterRows, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jFilterCaseSensitive, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton16)))
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
                .addComponent(jFilterCaseSensitive)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jFilterRows)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton16)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jxSearchPane.add(jXPanel3, new java.awt.GridBagConstraints());

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nicht gruppieren", "Gruppieren nach Dörfern", "Gruppieren nach Notizsymbolen" }));

        jLabel5.setText("Gruppierung");

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Vorschau"));

        jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        jScrollPane4.setViewportView(jTextPane1);

        jButton4.setText("Übernehmen");

        jButton5.setText("Abbrechen");

        javax.swing.GroupLayout jExportFormatDialogLayout = new javax.swing.GroupLayout(jExportFormatDialog.getContentPane());
        jExportFormatDialog.getContentPane().setLayout(jExportFormatDialogLayout);
        jExportFormatDialogLayout.setHorizontalGroup(
            jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jExportFormatDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jExportFormatDialogLayout.createSequentialGroup()
                        .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(bBPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jExportFormatDialogLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jExportFormatDialogLayout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addContainerGap())
        );
        jExportFormatDialogLayout.setVerticalGroup(
            jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jExportFormatDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jExportFormatDialogLayout.createSequentialGroup()
                        .addComponent(bBPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle("Notizen");
        setMinimumSize(new java.awt.Dimension(500, 400));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        jNotesPanel.setBackground(new java.awt.Color(239, 235, 223));
        jNotesPanel.setPreferredSize(new java.awt.Dimension(500, 400));
        jNotesPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 500;
        gridBagConstraints.ipady = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jNotesPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAlwaysOnTopChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangedEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopChangedEvent
    
    private void fireEnterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireEnterEvent
        jLabel4.setBackground(getBackground().darker());
}//GEN-LAST:event_fireEnterEvent
    
    private void fireMouseExitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMouseExitEvent
        jLabel4.setBackground(getBackground());
}//GEN-LAST:event_fireMouseExitEvent
    
    private void fireCreateNoteSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateNoteSetEvent
        int unusedId = 1;
        while (unusedId < 1000) {
            if (NoteManager.getSingleton().addGroup("Neues Set " + unusedId)) {
                break;
            }
            unusedId++;
        }
        if (unusedId == 1000) {
            JOptionPaneHelper.showErrorBox(DSWorkbenchNotepad.this, "Du hast mehr als 1000 Notizsets. Bitte lösche zuerst ein paar bevor du Neue erstellst.", "Fehler");
        }
}//GEN-LAST:event_fireCreateNoteSetEvent
    
    private void jButton16fireHideGlassPaneEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton16fireHideGlassPaneEvent
        jxSearchPane.setBackgroundPainter(null);
        jxSearchPane.setVisible(false);
}//GEN-LAST:event_jButton16fireHideGlassPaneEvent
    
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
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateFilter(jTextField1.getText(), jFilterCaseSensitive.isSelected(), jFilterRows.isSelected());
        }
    }
    
    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    
    // <editor-fold defaultstate="collapsed" desc="Gesture handling">
    @Override
    public void fireExportAsBBGestureEvent() {
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            tab.transferSelection(NoteTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
        }
    }
    
    @Override
    public void fireNextPageGestureEvent() {
        int current = jNoteTabbedPane.getSelectedIndex();
        int size = jNoteTabbedPane.getTabCount();
        if (current + 1 > size - 1) {
            current = 0;
        } else {
            current += 1;
        }
        jNoteTabbedPane.setSelectedIndex(current);
    }
    
    @Override
    public void firePlainExportGestureEvent() {
    }
    
    @Override
    public void firePreviousPageGestureEvent() {
        int current = jNoteTabbedPane.getSelectedIndex();
        int size = jNoteTabbedPane.getTabCount();
        if (current - 1 < 0) {
            current = size - 1;
        } else {
            current -= 1;
        }
        jNoteTabbedPane.setSelectedIndex(current);
    }
// </editor-fold>
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.panels.BBPanel bBPanel1;
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JDialog jExportFormatDialog;
    private javax.swing.JCheckBox jFilterCaseSensitive;
    private javax.swing.JCheckBox jFilterRows;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jNewSetPanel;
    private com.jidesoft.swing.JideTabbedPane jNoteTabbedPane;
    private javax.swing.JPanel jNotesPanel;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    private org.jdesktop.swingx.JXPanel jXNotePanel;
    private org.jdesktop.swingx.JXPanel jXPanel3;
    private org.jdesktop.swingx.JXPanel jxSearchPane;
    // End of variables declaration//GEN-END:variables
}
