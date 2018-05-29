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
import de.tor.tribes.types.drawing.AbstractForm;
import de.tor.tribes.types.drawing.Circle;
import de.tor.tribes.types.drawing.Line;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.models.FormTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.renderer.CustomBooleanRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.FormConfigFrame;
import de.tor.tribes.util.*;
import de.tor.tribes.util.bb.FormListFormatter;
import de.tor.tribes.util.map.FormManager;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Charon
 */
public class DSWorkbenchFormFrame extends AbstractDSWorkbenchFrame implements ListSelectionListener, GenericManagerListener, ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (e.getActionCommand().equals("Copy")) {
            copySelectionToInternalClipboard();
        } else if (e.getActionCommand().equals("BBCopy")) {
            copySelectionToInternalClipboardAsBBCodes();
        } else if (e.getActionCommand().equals("Delete")) {
            deleteSelection();
        }
    }
    private static Logger logger = Logger.getLogger("FormFrame");
    private static DSWorkbenchFormFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    
    public static synchronized DSWorkbenchFormFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchFormFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchFormFrame */
    DSWorkbenchFormFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jFormPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jFormTablePanel);
        jFormsTable.setModel(new FormTableModel());
        jFormsTable.getSelectionModel().addListSelectionListener(DSWorkbenchFormFrame.this);
        buildMenu();
        
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        KeyStroke find = KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK, false);
        capabilityInfoPanel1.addActionListener(this);
        jFormsTable.registerKeyboardAction(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchFormFrame.getSingleton().actionPerformed(new ActionEvent(jFormsTable, 0, "Copy"));
            }
        }, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        jFormsTable.registerKeyboardAction(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchFormFrame.getSingleton().actionPerformed(new ActionEvent(jFormsTable, 0, "BBCopy"));
            }
        }, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        jFormsTable.registerKeyboardAction(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchFormFrame.getSingleton().actionPerformed(new ActionEvent(jFormsTable, 0, "Delete"));
            }
        }, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        jFormsTable.registerKeyboardAction(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                //no find
            }
        }, "Find", find, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.form_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
    }
    
    @Override
    public void toBack() {
        jAlwaysOnTop.setSelected(false);
        fireFormFrameAlwaysOnTopEvent(null);
        super.toBack();
    }
    
    @Override
    public void resetView() {
        //update view
        FormManager.getSingleton().addManagerListener(DSWorkbenchFormFrame.this);
        jFormsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        UIHelper.initTableColums(jFormsTable, "X", "Y", "Höhe", "Breite", "Sichtbar");

        ((FormTableModel) jFormsTable.getModel()).fireTableDataChanged();
    }
    
    private void buildMenu() {
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bearbeiten");
        JXButton editButton = new JXButton(new ImageIcon("./graphics/icons/replace2.png"));
        editButton.setToolTipText("Die ausgewählte Zeichnung bearbeiten");
        editButton.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                showEditFrame();
            }
        });
        
        editPane.getContentPane().add(editButton);
        
        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");
        JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/center_ingame.png")));
        transferVillageList.setToolTipText("Zentriert den Mittelpunkt der gewählten Zeichnung im Spiel");
        transferVillageList.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                centerFormInGame();
            }
        });
        transferPane.getContentPane().add(transferVillageList);
        if (!GlobalOptions.isMinimal()) {
            JXButton centerButton = new JXButton(new ImageIcon(DSWorkbenchFormFrame.class.getResource("/res/center_24x24.png")));
            centerButton.setToolTipText("Zentriert die Zeichnung auf der Hauptkarte");
            centerButton.addMouseListener(new MouseAdapter() {
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    centerFormOnMap();
                }
            });
            
            transferPane.getContentPane().add(centerButton);
        }
        
        JXTaskPane miscPane = new JXTaskPane();
        if (!GlobalOptions.isMinimal()) {
            miscPane.setTitle("Sonstiges");
            JXButton searchButton = new JXButton(new ImageIcon("./graphics/icons/search.png"));
            searchButton.setToolTipText("Lässt die gewählten Zeichnungen auf der Hauptkarte kurz aufblinken");
            searchButton.addMouseListener(new MouseAdapter() {
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    showFormOnMap();
                }
            });
            miscPane.getContentPane().add(searchButton);
        }
        if (!GlobalOptions.isMinimal()) {
            centerPanel.setupTaskPane(editPane, transferPane, miscPane);
        } else {
            centerPanel.setupTaskPane(editPane, transferPane);
        }
    }
    
    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTop.isSelected());
        
        PropertyHelper.storeTableProperties(jFormsTable, pConfig, getPropertyPrefix());
    }
    
    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        
        try {
            jAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }
        
        setAlwaysOnTop(jAlwaysOnTop.isSelected());
        
        PropertyHelper.restoreTableProperties(jFormsTable, pConfig, getPropertyPrefix());
    }
    
    @Override
    public String getPropertyPrefix() {
        return "forms.view";
    }
    
    private void copySelectionToInternalClipboardAsBBCodes() {
        try {
            List<AbstractForm> forms = getSelectedForms();
            if (forms.isEmpty()) {
                showInfo("Keine Zeichnungen ausgewählt");
                return;
            }
            int ignoredForms = 0;
            for (AbstractForm form : forms) {
                if (!form.allowsBBExport()) {
                    ignoredForms++;
                }
            }
            
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);
            
            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Zeichnungen[/size][/u]\n\n");
            } else {
                buffer.append("[u]Zeichnungen[/u]\n\n");
            }
            
            buffer.append(new FormListFormatter().formatElements(forms, extended));
            
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
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Zeichnungen benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            String result = null;
            if (ignoredForms != forms.size()) {
                result = "<html>Daten in Zwischenablage kopiert.";
                if (ignoredForms > 0) {
                    result += ((ignoredForms == 1) ? " Eine Zeichnung wurde" : " " + ignoredForms + " Zeichnungen wurden") + " ignoriert, da der BB-Export nur für Rechtecke, Kreise und Freihandzeichnungen verf&uuml;gbar ist.";
                }
                result += "</html>";
            } else {
                showError("<html>Keine Zeichnungen exportiert, da der BB-Export nur für Rechtecke, Kreise und Freihandzeichnungen verf&uuml;gbar ist.</html>");
                return;
            }
            showSuccess(result);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            showError(result);
        }
    }
    
    private void copySelectionToInternalClipboard() {
        try {
            List<AbstractForm> selection = getSelectedForms();
            if (selection.isEmpty()) {
                showInfo("Keine Zeichnung gewählt");
                return;
            }
            List<Village> villages = new ArrayList<>();
            for (AbstractForm f : selection) {
                for (Village v : f.getContainedVillages()) {
                    if (!villages.contains(v)) {
                        villages.add(v);
                    }
                }
            }
            StringBuilder builder = new StringBuilder();
            for (Village v : villages) {
                builder.append(v.toString()).append("\n");
            }
            
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
            showSuccess(villages.size() + ((villages.size() == 1) ? " Dorf " : " Dörfer ") + "in die Zwischenablage kopiert");
        } catch (Exception e) {
            logger.error("Failed to copy villages to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            showError(result);
        }
    }
    
    private void deleteSelection() {
        List<AbstractForm> selection = getSelectedForms();
        if (selection.isEmpty()) {
            showInfo("Keine Zeichnung gewählt");
            return;
        }
        
        String message = ((selection.size() == 1) ? "Zeichnung " : (selection.size() + " Zeichnungen")) + "wirklich löschen?";
        
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            FormManager.getSingleton().invalidate();
            for (AbstractForm f : selection) {
                FormManager.getSingleton().removeElement(f);
            }
            
            showSuccess(((selection.size() == 1) ? "Zeichnung " : (selection.size() + " Zeichnungen ")) + "gelöscht");
            FormManager.getSingleton().revalidate(true);
        }
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jFormsTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Zeichnung gewählt" : " Zeichnungen gewählt"));
            }
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
    
    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.RED));
        jXLabel1.setForeground(Color.WHITE);
        jXLabel1.setText(pMessage);
    }
    
    public void updateVisibility() {
        ((FormTableModel) jFormsTable.getModel()).fireTableDataChanged();
    }
    
    private void centerFormOnMap() {
        List<AbstractForm> selection = getSelectedForms();
        if (selection.isEmpty()) {
            showError("Keine Zeichnung gewählt");
            return;
        }
        Rectangle r = selection.get(0).getBounds();
        
        if (r != null) {
            DSWorkbenchMainFrame.getSingleton().centerPosition((int) Math.rint(r.getCenterX()), (int) Math.rint(r.getCenterY()));
        } else {
            showInfo("Ein Mittelpunkt kann für diese Zeichnung nicht bestimmt werden");
        }
    }
    
    private void centerFormInGame() {
        List<AbstractForm> selection = getSelectedForms();
        if (selection.isEmpty()) {
            showError("Keine Zeichnung gewählt");
            return;
        }
        Rectangle r = selection.get(0).getBounds();
        
        if (r != null) {
            BrowserInterface.centerCoordinate((int) Math.rint(r.getCenterX()), (int) Math.rint(r.getCenterY()));
        } else {
            showInfo("Ein Mittelpunkt kann für diese Zeichnung nicht bestimmt werden");
        }
    }
    
    private void showEditFrame() {
        List<AbstractForm> selection = getSelectedForms();
        if (selection.isEmpty()) {
            showError("Keine Zeichnung gewählt");
            return;
        }
        
        AbstractForm toEdit = selection.get(0);
        if (toEdit != null) {
            if ((MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_CIRCLE)
                    || (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_LINE)
                    || (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_RECT)
                    || (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_TEXT)
                    || (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_FREEFORM)) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
            }
            FormConfigFrame.getSingleton().setupAndShowInEditMode(toEdit);
        }
    }
    
    private void showFormOnMap() {
        List<AbstractForm> selection = getSelectedForms();
        if (selection.isEmpty()) {
            showError("Keine Zeichnung gewählt");
            return;
        }
        for (AbstractForm f : selection) {
            f.setShowMode(true);
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

        jFormTablePanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jFormPanel = new javax.swing.JPanel();
        jAlwaysOnTop = new javax.swing.JCheckBox();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jFormTablePanel.setLayout(new java.awt.BorderLayout());

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

        jFormTablePanel.add(infoPanel, java.awt.BorderLayout.SOUTH);

        jFormsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(jFormsTable);

        jFormTablePanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        setTitle("Zeichnungen");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jFormPanel.setBackground(new java.awt.Color(239, 235, 223));
        jFormPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 439;
        gridBagConstraints.ipady = 319;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jFormPanel, gridBagConstraints);

        jAlwaysOnTop.setText("Immer im Vordergrund");
        jAlwaysOnTop.setOpaque(false);
        jAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireFormFrameAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTop, gridBagConstraints);

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

    private void fireFormFrameAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireFormFrameAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireFormFrameAlwaysOnTopEvent
    
    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_fireHideInfoEvent
    
    private List<AbstractForm> getSelectedForms() {
        final List<AbstractForm> selectedForms = new LinkedList<>();
        int[] selectedRows = jFormsTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedForms;
        }
        for (Integer selectedRow : selectedRows) {
            AbstractForm f = (AbstractForm) FormManager.getSingleton().getAllElements().get(jFormsTable.convertRowIndexToModel(selectedRow));
            if (f != null) {
                selectedForms.add(f);
            }
        }
        return selectedForms;
    }
    
    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    
    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }
    
    @Override
    public void dataChangedEvent(String pGroup) {
        ((FormTableModel) jFormsTable.getModel()).fireTableDataChanged();
    }
    
    public static void main(String[] args) {
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        DSWorkbenchFormFrame.getSingleton().setSize(800, 600);
        FormManager.getSingleton().addForm(new Circle());
        FormManager.getSingleton().addForm(new de.tor.tribes.types.drawing.Rectangle());
        FormManager.getSingleton().addForm(new Circle());
        FormManager.getSingleton().addForm(new de.tor.tribes.types.drawing.Rectangle());
        FormManager.getSingleton().addForm(new Circle());
        FormManager.getSingleton().addForm(new de.tor.tribes.types.drawing.Rectangle());
        FormManager.getSingleton().addForm(new Circle());
        FormManager.getSingleton().addForm(new de.tor.tribes.types.drawing.Rectangle());
        FormManager.getSingleton().addForm(new Line());
        
        DSWorkbenchFormFrame.getSingleton().resetView();
        DSWorkbenchFormFrame.getSingleton().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        DSWorkbenchFormFrame.getSingleton().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jAlwaysOnTop;
    private javax.swing.JPanel jFormPanel;
    private org.jdesktop.swingx.JXPanel jFormTablePanel;
    private static final org.jdesktop.swingx.JXTable jFormsTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JScrollPane jScrollPane4;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables

    static {
        jFormsTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jFormsTable.setColumnControlVisible(true);
        jFormsTable.setDefaultRenderer(Boolean.class, new CustomBooleanRenderer(CustomBooleanRenderer.LayoutStyle.VISIBLE_INVISIBLE));
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
        jFormsTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));
    }
}
