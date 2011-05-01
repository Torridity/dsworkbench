/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchTagFrame.java
 *
 * Created on 08.07.2009, 14:30:47
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.LinkedTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.TagMapMarker;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.LinkTagsDialog;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.editors.TagMapMarkerCellEditor;
import de.tor.tribes.ui.models.TagTableModel;
import de.tor.tribes.ui.renderer.AlternatingColorCellRenderer;
import de.tor.tribes.ui.renderer.BooleanCellRenderer;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.ui.renderer.map.TagMapMarkerRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.TagToBBCodeFormater;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;

/**
 * @author Jejkal
 */
public class DSWorkbenchTagFrame extends AbstractDSWorkbenchFrame {

    private static Logger logger = Logger.getLogger("TagView");
    private static DSWorkbenchTagFrame SINGLETON = null;
    private TableCellRenderer mHeaderRenderer = null;

    /** Creates new form DSWorkbenchTagFrame */
    DSWorkbenchTagFrame() {
        initComponents();
        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("tag.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        mHeaderRenderer = new SortableTableHeaderRenderer();

        jTagTable.setColumnSelectionAllowed(false);
        TagTableModel.getSingleton().resetRowSorter(jTagTable);
        TagTableModel.getSingleton().loadColumnState();
        AlternatingColorCellRenderer rend = new AlternatingColorCellRenderer();
        jTagTable.setDefaultRenderer(TagMapMarker.class, new TagMapMarkerRenderer());
        jTagTable.setDefaultEditor(TagMapMarker.class, new TagMapMarkerCellEditor());
        jTagTable.setDefaultRenderer(String.class, rend);
        jTagTable.setDefaultRenderer(Integer.class, rend);
        jTagTable.setDefaultRenderer(Boolean.class, new BooleanCellRenderer());

        MouseListener l = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON2) {
                    TagTableModel.getSingleton().getPopup().show(jTagTable, e.getX(), e.getY());
                    TagTableModel.getSingleton().getPopup().requestFocusInWindow();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };

        jTagTable.addMouseListener(l);
        jScrollPane1.addMouseListener(l);
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.tag_view", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>

        jAddTagDialog.pack();
        jTagTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateTaggedVillageList();
            }
        });


        pack();
    }

    public static synchronized DSWorkbenchTagFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchTagFrame();
        }
        return SINGLETON;
    }

    public void resetView() {
        //load tags on server change
        jTagTable.invalidate();
        jTagTable.getTableHeader().setReorderingAllowed(false);

        //setup renderer and general view
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        //update view
        fireRebuildTableEvent();
        jTagTable.revalidate();
        TagTableModel.getSingleton().resetRowSorter(jTagTable);
        TagTableModel.getSingleton().loadColumnState();
        jTagTable.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jAddTagDialog = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        jNewTagName = new javax.swing.JTextField();
        jCreateTagButton = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTagTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTaggedVillageList = new javax.swing.JList();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        jAddTagDialog.setTitle("Neuer Tag");
        jAddTagDialog.setAlwaysOnTop(true);

        jLabel1.setText("Name");

        jCreateTagButton.setText("OK");
        jCreateTagButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireNewTagEvent(evt);
            }
        });

        jButton5.setText("Abbrechen");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireNewTagEvent(evt);
            }
        });

        javax.swing.GroupLayout jAddTagDialogLayout = new javax.swing.GroupLayout(jAddTagDialog.getContentPane());
        jAddTagDialog.getContentPane().setLayout(jAddTagDialogLayout);
        jAddTagDialogLayout.setHorizontalGroup(
            jAddTagDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddTagDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddTagDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jAddTagDialogLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jNewTagName, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAddTagDialogLayout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCreateTagButton)))
                .addContainerGap())
        );
        jAddTagDialogLayout.setVerticalGroup(
            jAddTagDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddTagDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddTagDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jNewTagName, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jAddTagDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCreateTagButton)
                    .addComponent(jButton5))
                .addContainerGap())
        );

        setTitle("Tags");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jTagTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTagTable);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton1.setToolTipText("Löscht die markierten Tags");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTagsEvent(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/color_remove.png"))); // NOI18N
        jButton2.setToolTipText("Farbmarkierung der markierten Tags löschen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveColorEvent(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/add.png"))); // NOI18N
        jButton3.setToolTipText("Neuen Tag hinzufügen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddTagEvent(evt);
            }
        });

        jScrollPane2.setViewportView(jTaggedVillageList);

        jButton4.setBackground(new java.awt.Color(239, 235, 223));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/center.png"))); // NOI18N
        jButton4.setToolTipText("Markiertes Dorf auf der Karte zentrieren");
        jButton4.setMaximumSize(new java.awt.Dimension(59, 35));
        jButton4.setMinimumSize(new java.awt.Dimension(59, 35));
        jButton4.setPreferredSize(new java.awt.Dimension(59, 35));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterVillageEvent(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(239, 235, 223));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton6.setToolTipText("Alle markierten Tags für das gewählte Dorf löschen");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUntagVillage(evt);
            }
        });

        jButton7.setBackground(new java.awt.Color(239, 235, 223));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        jButton7.setToolTipText("Markierte Tags als BB-Codes in die Zwischenablage kopieren");
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyTagsAsBBCodeToClipboardEvent(evt);
            }
        });

        jButton8.setBackground(new java.awt.Color(239, 235, 223));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/from_clipboard.png"))); // NOI18N
        jButton8.setToolTipText("Dörfer aus der Zwischenablage mit dem gewählten Tag versehen");
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTagVillagesFromClipboardEvent(evt);
            }
        });

        jButton9.setBackground(new java.awt.Color(239, 235, 223));
        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/branch.png"))); // NOI18N
        jButton9.setToolTipText("Gewählte Tags hinzufügen");
        jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireLinkTagsEvent(evt);
            }
        });

        jButton10.setBackground(new java.awt.Color(239, 235, 223));
        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/export_js.png"))); // NOI18N
        jButton10.setToolTipText("Dörfer in der gewählten Gruppe in die Zwischenablage kopieren, um sie per DS Workbench Userscript im Spiel zuzuweisen");
        jButton10.setMaximumSize(new java.awt.Dimension(59, 35));
        jButton10.setMinimumSize(new java.awt.Dimension(59, 35));
        jButton10.setPreferredSize(new java.awt.Dimension(59, 35));
        jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyVillagesForScriptEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButton3)
                        .addComponent(jButton9))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jButton2)
                            .addComponent(jButton6)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jButton7)
                        .addComponent(jButton8))
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                .addContainerGap())
        );

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAlwaysOnTopBox, javax.swing.GroupLayout.Alignment.TRAILING)
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

    private void fireAlwaysOnTopEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopEvent

    private void fireRemoveTagsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveTagsEvent
        int[] rows = jTagTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }
        String message = (rows.length == 1) ? "Tag " : rows.length + " Tags ";
        message += "wirklich löschen?";
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Tags löschen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
            //return if no delete was requested
            return;
        }

        List<String> toRemove = new LinkedList<String>();
        for (int row : rows) {
            int realRow = jTagTable.convertRowIndexToModel(row);
            toRemove.add((String) TagTableModel.getSingleton().getOriginalValueAt(realRow, 0));
        }
        for (String tag : toRemove) {
            TagManager.getSingleton().removeTagFastByName(tag);
        }
        TagManager.getSingleton().forceUpdate();
        //update map
        try {
            MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
        } catch (Exception e) {
        }
    }//GEN-LAST:event_fireRemoveTagsEvent

    private void fireRemoveColorEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveColorEvent
        int[] rows = jTagTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }

        for (int row : rows) {
            row = jTagTable.convertRowIndexToModel(row);
            String name = (String) TagTableModel.getSingleton().getOriginalValueAt(row, 0);
            TagManager.getSingleton().getTagByName(name).setTagColor(null);
        }
        jTagTable.repaint();
        try {
            MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
        } catch (Exception e) {
        }
    }//GEN-LAST:event_fireRemoveColorEvent

    private void fireAddTagEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddTagEvent
        jAddTagDialog.setLocationRelativeTo(this);
        jAddTagDialog.setVisible(true);
    }//GEN-LAST:event_fireAddTagEvent

    private void fireNewTagEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireNewTagEvent
        if (evt.getSource() != jCreateTagButton) {
            //cancel pressed  
            jAddTagDialog.setVisible(false);
            return;
        }
        String name = jNewTagName.getText();
        if (name.length() < 1) {
            JOptionPaneHelper.showWarningBox(jAddTagDialog, "Kein Name angegeben.", "Warnung");
            return;
        }
        Tag t = TagManager.getSingleton().getTagByName(name);
        if (t != null) {
            JOptionPaneHelper.showWarningBox(jAddTagDialog, "Ein Tag mit dem Namen existiert bereits.", "Warnung");
            return;
        }
        TagManager.getSingleton().addTag(name);
        jAddTagDialog.setVisible(false);
    }//GEN-LAST:event_fireNewTagEvent

    private void fireCenterVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterVillageEvent
        Village selection = (Village) jTaggedVillageList.getSelectedValue();
        if (selection != null) {
            DSWorkbenchMainFrame.getSingleton().centerVillage(selection);
        }

    }//GEN-LAST:event_fireCenterVillageEvent

    private void fireUntagVillage(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUntagVillage
        Object[] selection = jTaggedVillageList.getSelectedValues();
        if (selection != null) {
            int[] rows = jTagTable.getSelectedRows();
            if (rows == null || rows.length == 0) {
                return;
            }

            String message = (rows.length == 1) ? "Tag " : rows.length + " Tags ";
            message += "für alle markierten Dörfer löschen?";
            if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Tags löschen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                //return if no delete was requested
                return;
            }

            for (int row : rows) {
                row = jTagTable.convertRowIndexToModel(row);
                String name = (String) TagTableModel.getSingleton().getOriginalValueAt(row, 0);
                Tag t = TagManager.getSingleton().getTagByName(name);
                if (t != null) {
                    for (Object o : selection) {
                        t.untagVillage(((Village) o).getId());
                    }
                }
            }
        }
        updateTaggedVillageList();
        try {
            MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
        } catch (Exception e) {
        }
    }//GEN-LAST:event_fireUntagVillage

    private void fireCopyTagsAsBBCodeToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyTagsAsBBCodeToClipboardEvent

        int[] rows = jTagTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }

        boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

        String sUrl = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
        String result = "";
        for (int row : rows) {
            int r = jTagTable.convertRowIndexToModel(row);
            String name = (String) TagTableModel.getSingleton().getOriginalValueAt(r, 0);
            Tag t = TagManager.getSingleton().getTagByName(name);
            if (t != null) {
                result += TagToBBCodeFormater.formatTag(t, sUrl, extended);
            }
        }

        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
            JOptionPaneHelper.showInformationBox(this, "Daten in Zwischenablage kopiert.", "Information");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage.", "Fehler");
        }
    }//GEN-LAST:event_fireCopyTagsAsBBCodeToClipboardEvent

    private void fireTagVillagesFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTagVillagesFromClipboardEvent
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            List<Village> villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));
            if (villages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dorfdaten in der Zwischenablage gefunden", "Information");
                return;
            }

            int row = jTagTable.getSelectedRow();
            if (row == -1) {
                return;
            }

            String tagName = (String) TagTableModel.getSingleton().getOriginalValueAt(row, 0);
            Tag tag = TagManager.getSingleton().getTagByName(tagName);
            for (Village v : villages) {
                tag.tagVillage(v.getId());
            }
            updateTaggedVillageList();
            MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
            if (villages.size() == 1) {
                JOptionPaneHelper.showInformationBox(this, "Ein Dorf wurden mit dem gewähltem Tag versehen", "Information");
            } else {
                JOptionPaneHelper.showInformationBox(this, villages.size() + " Dörfer wurden mit dem gewähltem Tag versehen", "Information");
            }
        } catch (Exception e) {
            logger.error("Failed to get village from clipboard", e);
        }
    }//GEN-LAST:event_fireTagVillagesFromClipboardEvent

    private void fireLinkTagsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireLinkTagsEvent

        LinkedTag t = new LinkTagsDialog(this, true).setupAndShow();
        if (t != null) {
            TagManager.getSingleton().addLinkedTag(t);
        }
    }//GEN-LAST:event_fireLinkTagsEvent

    private void fireCopyVillagesForScriptEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyVillagesForScriptEvent
        DefaultListModel model = ((DefaultListModel) jTaggedVillageList.getModel());
        StringBuilder data = new StringBuilder();
        if (model.getSize() == 0) {
            JOptionPaneHelper.showInformationBox(this, "Keine Tag gewählt oder es sind dem Tag keine Dörfer zugeordnet", null);
            return;
        }
        try {
            for (int i = 0; i < model.getSize(); i++) {
                data.append(((Village) model.getElementAt(i)).getId()).append(";");
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data.toString()), null);
            JOptionPaneHelper.showInformationBox(this, "Dörfer erfolgreich in die Zwischenablage kopiert.\nFüge sie nun in der Gruppenübersicht in den entsprechende Feld ein\nund weise sie einer neuen Gruppe zu.", null);
        } catch (Exception e) {
            logger.error("Failed to copy villages to clipboard", e);
            JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
        }
    }//GEN-LAST:event_fireCopyVillagesForScriptEvent

    private void updateTaggedVillageList() {
        int[] rows = jTagTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }
        DefaultListModel model = new DefaultListModel();
        List<Village> villages = new LinkedList<Village>();
        for (int row : rows) {
            row = jTagTable.convertRowIndexToModel(row);
            String name = (String) TagTableModel.getSingleton().getOriginalValueAt(row, 0);
            for (Integer i : TagManager.getSingleton().getTagByName(name).getVillageIDs()) {
                Village v = DataHolder.getSingleton().getVillagesById().get(i);
                if (v != null && !villages.contains(v)) {
                    villages.add(v);
                }
            }
        }
        Collections.sort(villages);
        for (Village v : villages) {
            model.addElement(v);
        }
        jTaggedVillageList.setModel(model);
    }

    public void fireRebuildTableEvent() {
        try {
            jTagTable.invalidate();
            for (int i = 0; i < jTagTable.getColumnCount(); i++) {
                jTagTable.getColumn(jTagTable.getColumnName(i)).setHeaderRenderer(mHeaderRenderer);
            }
            jTagTable.revalidate();
            jTagTable.repaint();
        } catch (Exception e) {
            logger.error("Failed to update tag table", e);
        }
        TagTableModel.getSingleton().resetRowSorter(jTagTable);
        TagTableModel.getSingleton().loadColumnState();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
        try {
            Rectangle bounds = jTaggedVillageList.getBounds();
            Point locationOnScreen = jTaggedVillageList.getLocationOnScreen();
            bounds.setLocation(locationOnScreen);
            pDropLocation.move(locationOnScreen.x, locationOnScreen.y);
            if (bounds.contains(pDropLocation)) {
                int[] rows = jTagTable.getSelectedRows();
                if (rows == null || rows.length == 0) {
                    return;
                }

                for (int row : rows) {
                    row = jTagTable.convertRowIndexToModel(row);
                    String name = (String) TagTableModel.getSingleton().getOriginalValueAt(row, 0);
                    Tag t = TagManager.getSingleton().getTagByName(name);
                    for (Village v : pVillages) {
                        if (v != null && v.getTribe() != Barbarians.getSingleton()) {
                            t.tagVillage(v.getId());
                        }
                    }
                }
                updateTaggedVillageList();
            }
            MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
        } catch (Exception e) {
            logger.error("Failed to insert dropped villages", e);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Gesture handling">
    @Override
    public void fireExportAsBBGestureEvent() {
        fireCopyTagsAsBBCodeToClipboardEvent(null);
    }

    // </editor-fold>
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchTagFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog jAddTagDialog;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jCreateTagButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jNewTagName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTagTable;
    private javax.swing.JList jTaggedVillageList;
    // End of variables declaration//GEN-END:variables
}
