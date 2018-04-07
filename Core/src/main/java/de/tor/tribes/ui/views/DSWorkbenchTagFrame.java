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

import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.LinkedTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.TagMapMarker;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.editors.TagMapMarkerCellEditor;
import de.tor.tribes.ui.models.TagTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.TagMapMarkerRenderer;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.LinkTagsDialog;
import de.tor.tribes.util.*;
import de.tor.tribes.util.bb.TagListFormatter;
import de.tor.tribes.util.bb.VillageListFormatter;
import de.tor.tribes.util.tag.TagManager;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * @author Torridity
 */
public class DSWorkbenchTagFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ListSelectionListener, ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Paste")) {
            pasteVillagesFromClipboard();
        } else if (e.getActionCommand().equals("BBCopy")) {
            transferSelectedTagsAsBBCodesToClipboard();
        } else if (e.getActionCommand().equals("BBCopy_Village")) {
            copyVillageAsBBCode();
        } else if (e.getActionCommand().equals("Delete")) {
            if (e.getSource() != null) {
                if (jTagsTable.hasFocus()) {
                    removeSelectedTags();
                } else {
                    untagSelectedVillages();
                }
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jTagsTable.getSelectedRowCount();

            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Gruppe gewählt" : " Gruppen gewählt"));
            }
            updateVillageList();
        }
    }

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        ((TagTableModel) jTagsTable.getModel()).fireTableDataChanged();
        updateVillageList();
    }
    private static Logger logger = Logger.getLogger("GroupView");
    private static DSWorkbenchTagFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;

    /** Creates new form DSWorkbenchTagFrame */
    DSWorkbenchTagFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jTagPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jTagsPanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this);
        jTagsTable.setModel(new TagTableModel());
        jTagsTable.getSelectionModel().addListSelectionListener(DSWorkbenchTagFrame.this);
        jTagsTable.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //ignore find
            }
        });

        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jTagsTable.registerKeyboardAction(DSWorkbenchTagFrame.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jTagsTable.registerKeyboardAction(DSWorkbenchTagFrame.this, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jVillageList.registerKeyboardAction(DSWorkbenchTagFrame.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jVillageList.registerKeyboardAction(DSWorkbenchTagFrame.this, "BBCopy_Village", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jTagsTable.registerKeyboardAction(DSWorkbenchTagFrame.this, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.tag_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>

        initialize();
        pack();
    }

    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());

        PropertyHelper.storeTableProperties(jTagsTable, pConfig, getPropertyPrefix());
    }

    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));

        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        PropertyHelper.restoreTableProperties(jTagsTable, pConfig, getPropertyPrefix());
    }

    public String getPropertyPrefix() {
        return "tag.view";
    }

    private void initialize() {
        String prop = GlobalOptions.getProperty("tag.frame.table.visibility");
        
        String[] split = prop.split(";");
        for (int i = 0; i < split.length; i++) {
            if (!Boolean.parseBoolean(split[i])) {
                TableColumnExt col = jTagsTable.getColumnExt(i);
                col.setVisible(false);
            }
        }
        centerPanel.setMenuVisible(GlobalOptions.getProperties().getBoolean("tag.frame.menu.visible"));
    }

    @Override
    public void toBack() {
        jAlwaysOnTopBox.setSelected(false);
        fireAlwaysOnTopEvent(null);
        super.toBack();
    }

    private void buildMenu() {
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bearbeiten");
        JXButton addButton = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/add.png")));
        addButton.setToolTipText("Erstellt einen neuen Gruppe");
        addButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                int unusedId = 1;
                while (unusedId < 1000) {
                    if (TagManager.getSingleton().getTagByName("Neue Gruppe " + unusedId) == null) {
                        TagManager.getSingleton().addTag("Neue Gruppe " + unusedId);
                        break;
                    }
                    unusedId++;
                }
                if (unusedId == 1000) {
                    JOptionPaneHelper.showErrorBox(DSWorkbenchTagFrame.this, "Du hast mehr als 1000 Gruppen. Bitte lösche zuerst ein paar bevor du Neue erstellst.", "Fehler");
                }
            }
        });
        editPane.getContentPane().add(addButton);
        JXButton linkButton = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/branch.png")));
        linkButton.setToolTipText("Verknüpft mehrerer Gruppen zu einem neuen Gruppe");
        linkButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                LinkedTag t = new LinkTagsDialog(DSWorkbenchTagFrame.this, true).setupAndShow();
                if (t != null) {
                    TagManager.getSingleton().addLinkedTag(t);
                }
            }
        });
        editPane.getContentPane().add(linkButton);
        JXButton colorRemoveButton = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/color_remove.png")));
        colorRemoveButton.setToolTipText("Entfernt die Farbe der Kartenmarkierung für die gewählten Gruppen");
        colorRemoveButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                for (Tag t : getSelectedTags()) {
                    t.setTagColor(null);

                }
                TagManager.getSingleton().fireDataChangedEvents();
            }
        });
        editPane.getContentPane().add(colorRemoveButton);

        JXTaskPane transferTaskPane = new JXTaskPane();
        transferTaskPane.setTitle("Übertragen");
        JXButton centerIngame = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/center_ingame.png")));
        centerIngame.setToolTipText("Zentriert das gewählte Dorf im Spiel");
        centerIngame.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                centerVillageInGame();
            }
        });
        transferTaskPane.getContentPane().add(centerIngame);

        JXButton transferJS = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/export_js.png")));
        transferJS.setToolTipText("<html>Alle D&ouml;rfer der gew&auml;hlten Gruppe in die Zwischenablage kopieren.<BR/>Auf diesem Wege ist es z.B. m&ouml;glich, verkn&uuml;pfte Gruppen aus DS Workbench ins Spiel zu &uuml;bertragen.<BR>F&uuml;r weitere Informationen sieh bitte in der Hilfe nach.</html>");
        transferJS.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                transferSelectedTagsToJS();
            }
        });
        transferTaskPane.getContentPane().add(transferJS);

        if (!GlobalOptions.isMinimal()) {
            JXButton centerButton = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/center_24x24.png")));
            centerButton.setToolTipText("Zentriert das erste gewählte Dorf auf der Hauptkarte");
            centerButton.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    centerVillage();
                }
            });
            transferTaskPane.getContentPane().add(centerButton);
        }
        centerPanel.setupTaskPane(editPane, transferTaskPane);
    }

    public static synchronized DSWorkbenchTagFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchTagFrame();
        }
        return SINGLETON;
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

    private void centerVillage() {
        Object villageSelection = jVillageList.getSelectedValue();
        if (villageSelection == null) {
            showInfo("Kein Dorf ausgewählt");
            return;
        }

        DSWorkbenchMainFrame.getSingleton().centerVillage((Village) villageSelection);
    }

    private void centerVillageInGame() {
        Object villageSelection = jVillageList.getSelectedValue();
        if (villageSelection == null) {
            showInfo("Kein Dorf ausgewählt");
            return;
        }

        BrowserInterface.centerVillage((Village) villageSelection);
    }

    private void updateVillageList() {
        List<Tag> selection = getSelectedTags();
        DefaultListModel model = new DefaultListModel();

        List<Village> villages = new LinkedList<>();
        for (Tag t : selection) {
            for (Integer id : t.getVillageIDs()) {
                Village v = DataHolder.getSingleton().getVillagesById().get(id);
                if (v != null && !villages.contains(v)) {
                    villages.add(v);
                }
            }
        }
        if (!selection.isEmpty() && !villages.isEmpty()) {
            Collections.sort(villages);
            for (Village v : villages) {
                model.addElement(v);
            }
        }
        jVillageList.setModel(model);
    }

    private void removeSelectedTags() {
        List<Tag> selection = getSelectedTags();
        if (selection.isEmpty()) {
            showInfo("Keine Gruppe ausgewählt");
            return;
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du die gewählte" + ((selection.size() == 1) ? " Gruppe " : "n Gruppen ") + "wirklich löschen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            TagManager.getSingleton().invalidate();
            for (Tag t : selection) {
                TagManager.getSingleton().removeElement(t);
            }
            TagManager.getSingleton().revalidate(true);
            showSuccess("Gruppe(n) erfolgreich gelöscht");
        }
    }

    private void untagSelectedVillages() {
        List villageSelection = jVillageList.getSelectedValuesList();
        if (villageSelection == null || villageSelection.isEmpty()) {
            showInfo("Keine Dörfer ausgewählt");
            return;
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du" + ((villageSelection.size() == 1) ? " das gewählte Dorf " : " die gewählten Dörfer ") + "wirklich aus den gewählten Gruppen entfernen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            List<Tag> selection = getSelectedTags();
            TagManager.getSingleton().invalidate();
            for (Tag t : selection) {
                for (Object o : villageSelection) {
                    Village v = (Village) o;
                    t.untagVillage(v.getId());
                }
            }
            TagManager.getSingleton().revalidate(true);
            showSuccess("Dörfer erfolgreich entfernt");
        }
    }

    private void copyVillageAsBBCode() {
        List villageSelection = jVillageList.getSelectedValuesList();
        if (villageSelection == null || villageSelection.isEmpty()) {
            showInfo("Keine Dörfer ausgewählt");
            return;
        }
        try {
            List<Village> villages = new LinkedList<>();
            for (Object o : villageSelection) {
                villages.add((Village) o);
            }
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Dorfliste[/size][/u]\n\n");
            } else {
                buffer.append("[u]Dorfliste[/u]\n\n");
            }
            buffer.append(new VillageListFormatter().formatElements(villages, extended));

            if (extended) {
                buffer.append("\n[size=8]Erstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/size]\n");
            } else {
                buffer.append("\nErstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "\n");
            }

            String b = buffer.toString();
            StringTokenizer t = new StringTokenizer(b, "[");
            int cnt = t.countTokens();
            if (cnt > 1000) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Dörfer benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            String result = "Dörfer in Zwischenablage kopiert.";
            showSuccess(result);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            showError(result);
        }
    }

    private void pasteVillagesFromClipboard() {
        List<Village> villages = null;
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));
            if (villages == null || villages.isEmpty()) {
                showInfo("Keine Dörfer in der Zwischenablage gefunden");
                return;
            }
        } catch (Exception e) {
            logger.error("Failed to read data from clipboard", e);
            showError("Fehler beim Lesen aus der Zwischenablage");
        }

        fireVillagesDraggedEvent(villages, null);
    }

    private void transferSelectedTagsAsBBCodesToClipboard() {
        List<Tag> selection = getSelectedTags();
        if (selection.isEmpty()) {
            showInfo("Keine Gruppe ausgewählt");
            return;
        }
        boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

        try {
            String formatted = new TagListFormatter().formatElements(selection, extended);
            StringTokenizer t = new StringTokenizer(formatted, "[");
            int cnt = t.countTokens();
            if (cnt > 1000) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Gruppen benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(formatted), null);
            showSuccess("BB-Codes in die Zwischenablage kopiert");
        } catch (HeadlessException he) {
            logger.error("Failed to copy data to clipboard", he);
            showError("Fehler beim Kopieren in die Zwischenablage");
        }
    }

    private void transferSelectedTagsToJS() {
        List<Tag> selection = getSelectedTags();
        if (selection.isEmpty()) {
            showInfo("Keine Gruppe ausgewählt");
            return;
        }

        List<Integer> villageIds = new LinkedList<>();
        for (Tag t : selection) {
            for (Integer id : t.getVillageIDs()) {
                if (!villageIds.contains(id)) {
                    villageIds.add(id);
                }
            }
        }

        if (villageIds.isEmpty()) {
            showInfo("Die gewählten Gruppen enthalten keine Dörfer");
        }
        StringBuilder data = new StringBuilder();

        try {
            for (Integer id : villageIds) {
                data.append(id).append(";");
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data.toString()), null);
            showSuccess("<html>D&ouml;rfer erfolgreich in die Zwischenablage kopiert.<br/>F&uuml;ge sie nun in der Gruppen&uuml;bersicht in das entsprechende Feld unterhalb einer leeren Gruppe ein und weise sie dadurch dieser Gruppe zu.</html>");
        } catch (HeadlessException he) {
            logger.error("Failed to copy data to clipboard", he);
            showError("Fehler beim Kopieren in die Zwischenablage");
        }
    }

    @Override
    public void resetView() {
        TagManager.getSingleton().addManagerListener(this);
        jTagsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        ((TagTableModel) jTagsTable.getModel()).fireTableDataChanged();
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

        jTagsPanel = new javax.swing.JPanel();
        jTagTablePanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        villageListPanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jVillageList = new javax.swing.JList();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jTagPanel = new org.jdesktop.swingx.JXPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jTagsPanel.setLayout(new java.awt.BorderLayout(10, 0));

        jTagTablePanel.setLayout(new java.awt.BorderLayout());

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setText("Keine Meldung");
        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jXLabel1fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jTagTablePanel.add(infoPanel, java.awt.BorderLayout.SOUTH);

        jTagsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(jTagsTable);

        jTagTablePanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jTagsPanel.add(jTagTablePanel, java.awt.BorderLayout.CENTER);

        villageListPanel.setPreferredSize(new java.awt.Dimension(180, 80));
        villageListPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane5.setBorder(javax.swing.BorderFactory.createTitledBorder("Zugeordnete Dörfer"));

        jScrollPane5.setViewportView(jVillageList);

        villageListPanel.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jTagsPanel.add(villageListPanel, java.awt.BorderLayout.WEST);

        setTitle("Gruppen");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        jTagPanel.setBackground(new java.awt.Color(239, 235, 223));
        jTagPanel.setPreferredSize(new java.awt.Dimension(500, 300));
        jTagPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jTagPanel, gridBagConstraints);

        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAlwaysOnTopEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

    private List<Tag> getSelectedTags() {
        final List<Tag> elements = new LinkedList<>();
        int[] selectedRows = jTagsTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            return elements;
        }
        for (Integer selectedRow : selectedRows) {
            Tag c = (Tag) TagManager.getSingleton().getAllElements().get(jTagsTable.convertRowIndexToModel(selectedRow));
            if (c != null) {
                elements.add(c);
            }
        }
        return elements;
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
        List<Tag> selection = getSelectedTags();
        if (selection.isEmpty()) {
            showInfo("Keine Gruppe ausgewählt, zu denen Dörfer hinzugefügt werden können");
            return;
        }
        String tag_string = (selection.size() == 1) ? "der gewählten Gruppe " : "den gewählten Gruppen ";
        String village_string = (pVillages.size() == 1) ? " Dorf " : " Dörfer ";
        String village_count_string = (pVillages.size() == 1) ? " dieses " : " diese ";

        if (JOptionPaneHelper.showQuestionConfirmBox(this, pVillages.size() + village_string + "in der Zwischenablage gefunden.\nMöchtest du " + village_count_string + tag_string + " hinzufügen?\n(Bereits vorhandene Dörfer werden nicht hinzugefügt)", "Dörfer hinzufügen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            TagManager.getSingleton().invalidate();
            for (Tag t : selection) {
                for (Village v : pVillages) {
                    t.tagVillage(v.getId());
                }
            }
            TagManager.getSingleton().revalidate(true);
            showSuccess("Dörfer erfolgreich zugewiesen");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Gesture handling">
    @Override
    public void fireExportAsBBGestureEvent() {
        transferSelectedTagsAsBBCodesToClipboard();
    }

    // </editor-fold>
    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
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

        Tag t = new Tag("Mein Tag", true);
        t.tagVillage(DataHolder.getSingleton().getRandomVillage().getId());
        TagManager.getSingleton().addManagedElement(t);

        DSWorkbenchTagFrame.getSingleton().setSize(600, 400);
        DSWorkbenchTagFrame.getSingleton().resetView();
        DSWorkbenchTagFrame.getSingleton().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        DSWorkbenchTagFrame.getSingleton().setVisible(true);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private org.jdesktop.swingx.JXPanel jTagPanel;
    private org.jdesktop.swingx.JXPanel jTagTablePanel;
    private javax.swing.JPanel jTagsPanel;
    private static final org.jdesktop.swingx.JXTable jTagsTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JList jVillageList;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private javax.swing.JPanel villageListPanel;
    // End of variables declaration//GEN-END:variables

    static {
        HighlightPredicate.ColumnHighlightPredicate colu = new HighlightPredicate.ColumnHighlightPredicate(0, 1, 3);
        jTagsTable.setHighlighters(new CompoundHighlighter(colu, HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));
        jTagsTable.setColumnControlVisible(true);
        jTagsTable.setDefaultRenderer(TagMapMarker.class, new TagMapMarkerRenderer());
        jTagsTable.setDefaultEditor(TagMapMarker.class, new TagMapMarkerCellEditor());
        BufferedImage back = ImageUtils.createCompatibleBufferedImage(5, 5, BufferedImage.BITMASK);
        Graphics2D g = back.createGraphics();
        GeneralPath p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(5, 0);
        p.lineTo(5, 5);
        p.closePath();
        g.setColor(Color.GREEN.darker());
        g.fill(p);
        g.dispose();
        jTagsTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));
    }
}
